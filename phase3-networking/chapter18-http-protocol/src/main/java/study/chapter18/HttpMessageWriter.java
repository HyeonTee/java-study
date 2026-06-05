package study.chapter18;

import java.io.IOException;
import java.io.OutputStream;

/**
 * HTTP 요청/응답 <strong>직렬화</strong> — 파싱의 역함수. ch17 {@code LineProtocol.writeLine}(CRLF·flush) +
 * {@code KvCommandHandler.reply} 순수함수 감각의 확장.
 *
 * <p>메시지 구조: <strong>시작줄 CRLF</strong> + (헤더 줄들, {@link Headers#writeTo}) + <strong>빈 줄 CRLF</strong>(헤더 종료)
 * + 바디. round-trip(write→parse 항등)의 한 축이다.
 *
 * <p>설계 메모: Content-Length를 {@code body.length}로 <strong>자동 부여하지 않는다</strong>(호출자 책임). 직렬화는 받은
 * 메시지를 그대로 바이트로 옮길 뿐 — 헤더와 바디의 일관성은 호출자가 보장하고, round-trip 테스트가 이를 검증한다.
 */
public final class HttpMessageWriter {

    private HttpMessageWriter() {
    }

    /**
     * 요청을 직렬화한다: {@code METHOD SP target SP version}CRLF + 헤더 + 빈 CRLF + 바디, 그리고 <strong>flush</strong>.
     *
     * <p>힌트: 요청라인을 UTF-8로 write 후 {@code "\r\n"}, {@code req.headers().writeTo(out)}, 헤더 종료 빈 줄
     * {@code "\r\n"}, {@code req.body()} 그대로 write, 마지막에 {@code flush}(누락 시 상대가 영원히 대기 — ch17 데드락 회수).
     *
     * @throws IOException I/O 오류
     */
    public static void writeRequest(OutputStream out, HttpRequest req) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: 요청라인+CRLF → headers.writeTo → 빈 CRLF(헤더 종료) → body write → flush");
    }

    /**
     * 응답을 직렬화한다: {@code version SP code SP reason}CRLF + 헤더 + 빈 CRLF + 바디 + flush.
     * {@link #writeRequest}와 동일 구조(시작줄만 상태줄로).
     *
     * <p>힌트: reason이 빈 문자열이면 {@code "HTTP/1.1 204 "}(trailing SP) 형태가 되어 {@code parseStatusLine}과
     * 라운드트립된다.
     *
     * @throws IOException I/O 오류
     */
    public static void writeResponse(OutputStream out, HttpResponse res) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: 상태줄+CRLF → headers.writeTo → 빈 CRLF → body write → flush");
    }
}
