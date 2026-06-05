package study.chapter19;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import study.chapter18.HttpRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("BoundedHttpParser — DoS 한계 강제 + keep-alive EOF=null 계약")
class BoundedHttpParserTest {

    private static final java.nio.charset.Charset UTF_8 = StandardCharsets.UTF_8;

    private static InputStream wire(String s) {
        return new ByteArrayInputStream(s.getBytes(UTF_8));
    }

    // 라인/헤더는 작게, 바디는 8바이트로 제한한 테스트용 한계
    private static final ServerLimits TIGHT = new ServerLimits(32, 32, 3, 8, 100, 5000);

    @Nested
    @DisplayName("정상 파싱 + 종료 신호")
    class Normal {

        @Test
        void 정상_요청_파싱() throws Exception {
            HttpRequest req = BoundedHttpParser.parseOrNull(
                    wire("GET /x HTTP/1.1\r\nHost: a\r\n\r\n"), TIGHT);
            assertEquals("GET", req.line().method());
            assertEquals("/x", req.line().target());
        }

        @Test
        void 요청_전_조용한_EOF는_null_정상종료() throws Exception {
            // ch18 parse는 여기서 예외를 던지지만, keep-alive 루프는 null(정상 종료)이어야 한다.
            assertNull(BoundedHttpParser.parseOrNull(wire(""), TIGHT));
        }
    }

    @Nested
    @DisplayName("DoS 한계 enforce — 누적 중 즉시 차단")
    class Limits {

        @Test
        void 요청라인이_너무_길면_431() {
            String longTarget = "/" + "a".repeat(100);
            assertThrows(RequestLimitException.class,
                    () -> BoundedHttpParser.parseOrNull(wire("GET " + longTarget + " HTTP/1.1\r\n\r\n"), TIGHT));
        }

        @Test
        void 헤더가_너무_많으면_431() {
            String many = "GET /x HTTP/1.1\r\nA: 1\r\nB: 2\r\nC: 3\r\nD: 4\r\nE: 5\r\n\r\n";
            assertThrows(RequestLimitException.class, () -> BoundedHttpParser.parseOrNull(wire(many), TIGHT));
        }

        @Test
        void 바디가_maxBodyBytes_초과면_413() {
            // maxBodyBytes=8, Content-Length 20 → 거부
            String big = "POST /x HTTP/1.1\r\nContent-Length: 20\r\n\r\n12345678901234567890";
            RequestLimitException ex = assertThrows(RequestLimitException.class,
                    () -> BoundedHttpParser.parseOrNull(wire(big), TIGHT));
            assertEquals(413, ex.statusCode());
        }

        @Test
        void readLineBounded_한도_초과_즉시_차단() {
            assertThrows(RequestLimitException.class,
                    () -> BoundedHttpParser.readLineBounded(wire("a".repeat(50) + "\r\n"), 32));
        }
    }
}
