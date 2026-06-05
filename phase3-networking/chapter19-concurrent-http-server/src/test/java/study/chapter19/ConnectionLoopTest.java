package study.chapter19;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import study.chapter18.HttpProtocolException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 이 단원의 <strong>무게중심</strong> — keep-alive 연결 루프({@link ConnectionLoop#serve})를 소켓·스레드·sleep 없이
 * {@code ByteArrayInputStream}/{@code ByteArrayOutputStream}으로 <strong>100% 결정적</strong>으로 검증한다.
 *
 * <p>비결정성(동시성)은 {@link HttpServerIntegrationTest}로 분리했다 — 여기선 "한 소켓 안에서 요청 N개를 어떻게
 * 무는가"의 프로토콜 로직만, in-memory로. 그래서 flaky가 0이다(결정성 = 비결정성을 곱이 아니라 분리된 합으로).
 */
@DisplayName("ConnectionLoop — keep-alive 연결 루프 (in-memory, 결정적)")
class ConnectionLoopTest {

    private static final java.nio.charset.Charset UTF_8 = StandardCharsets.UTF_8;

    /** 요청을 응답 바디로 target을 echo하는 핸들러 — 루프가 핸들러를 정말 구동하는지 확인용. */
    private static final HttpHandler ECHO = req -> Responses.text(200, "OK", req.line().target());

    private static final ServerLimits LIMITS = ServerLimits.defaults();

    /** GET 요청 한 개의 와이어 바이트. {@code close=true}면 {@code Connection: close} 헤더를 붙인다. */
    private static String request(String target, boolean close) {
        String conn = close ? "Connection: close\r\n" : "";
        return "GET " + target + " HTTP/1.1\r\nHost: x\r\n" + conn + "\r\n";
    }

    private static InputStream wire(String s) {
        return new ByteArrayInputStream(s.getBytes(UTF_8));
    }

    /** haystack에서 needle이 겹치지 않게 몇 번 나오는지. 응답이 N개 직렬화됐는지 셀 때 쓴다. */
    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) >= 0) {
            count++;
            idx += needle.length();
        }
        return count;
    }

    @Nested
    @DisplayName("루프 무게중심 — 한 스트림에 요청 N개")
    class Loop {

        @Test
        void keep_alive면_연속_3요청_모두_처리() throws Exception {
            // HTTP/1.1 기본 keep-alive — 한 스트림에 3요청을 이어 보내면 셋 다 처리되고, 4번째 읽기는 EOF로 정상 종료.
            String stream = request("/a", false) + request("/b", false) + request("/c", false);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int handled = ConnectionLoop.serve(wire(stream), out, ECHO, LIMITS);

            assertEquals(3, handled, "keep-alive 루프가 3요청을 모두 처리해야 한다");
            assertEquals(3, countOccurrences(out.toString(StandardCharsets.UTF_8), "200 OK"),
                    "응답도 3개 직렬화돼야 한다");
        }

        @Test
        void Connection_close면_그_요청만_처리하고_종료() throws Exception {
            // 첫 요청이 Connection: close → 처리 후 루프 종료. 뒤따르는 둘째 요청은 절대 읽지 않는다.
            String stream = request("/first", true) + request("/second", false);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int handled = ConnectionLoop.serve(wire(stream), out, ECHO, LIMITS);

            assertEquals(1, handled, "close면 첫 요청만 처리하고 둘째는 안 읽는다");
            assertEquals(1, countOccurrences(out.toString(StandardCharsets.UTF_8), "200 OK"));
        }

        @Test
        void 빈_입력은_조용한_EOF로_0요청() throws Exception {
            // 요청 전 EOF는 예외가 아니라 정상 종료(parseOrNull==null). 응답도 없다.
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int handled = ConnectionLoop.serve(wire(""), out, ECHO, LIMITS);

            assertEquals(0, handled);
            assertEquals(0, out.size(), "처리할 요청이 없으면 아무것도 쓰지 않는다");
        }
    }

    @Nested
    @DisplayName("에러는 1회 응답 후 연결 종료 (accept 루프 밖으로 새지 않음)")
    class Errors {

        @Test
        void malformed_요청은_400_1회응답_후_종료() throws Exception {
            // 문법이 깨진 요청라인 → HttpProtocolException → 루프가 400으로 응답하고 종료. 정상 처리 수는 0.
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int handled = ConnectionLoop.serve(wire("THIS-IS-NOT-HTTP\r\n\r\n"), out, ECHO, LIMITS);

            assertEquals(0, handled, "malformed는 정상 처리로 세지 않는다");
            assertTrue(out.toString(StandardCharsets.UTF_8).contains("400"),
                    "에러는 삼키지 말고 400으로 응답해야 한다(클라가 응답을 받아야 함)");
        }

        @Test
        void errorStatusFor_한계초과와_문법오류를_상태코드로() {
            // RequestLimitException은 자신의 statusCode(431/413), 그 외 HttpProtocolException은 400.
            assertEquals(431, ConnectionLoop.errorStatusFor(new RequestLimitException(431, "header too large")));
            assertEquals(413, ConnectionLoop.errorStatusFor(new RequestLimitException(413, "body too large")));
            assertEquals(400, ConnectionLoop.errorStatusFor(new HttpProtocolException("malformed request line")));
        }
    }
}
