package study.chapter17;

import java.io.IOException;

/**
 * 미니 HTTP 클라이언트 — 클라이언트↔서버 <strong>대칭</strong>의 회수(ch16 {@code EchoClient}↔{@code EchoServer}).
 *
 * <p>서버는 "요청 파싱 + 응답 직렬화", 클라이언트는 "요청 직렬화 + 응답 파싱" — <strong>같은 코드, 방향만 반대</strong>다.
 * 즉 {@link HttpMessageWriter#writeRequest}로 보내고 {@link HttpParser#parseResponse}로 받는다.
 *
 * <p>실소켓 1왕복만 한다(ch16 4규율: loopback + {@code shutdownOutput} half-close). <strong>연결 재사용·keep-alive는
 * ch18</strong> — 여기서는 요청 하나 보내고 응답 하나 받으면 끝이다.
 */
public final class MiniHttpClient {

    private final String host;
    private final int port;

    /**
     * @param host 접속 호스트(테스트는 loopback)
     * @param port 접속 포트
     */
    public MiniHttpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 요청 하나를 보내고 응답 하나를 받아 반환한다(1왕복).
     *
     * <p>힌트: {@code Socket}을 {@code host:port}로 연결(try-with-resources, 타임아웃 권장) →
     * {@link HttpMessageWriter#writeRequest}로 요청 전송 → {@code socket.shutdownOutput()}으로 half-close(ch16
     * {@code EchoClient.finish} 패턴 — 서버에 "요청 끝"을 FIN으로 통지) → {@link HttpParser#parseResponse}로 응답을
     * 파싱해 반환. keep-alive 루프 금지(1왕복 후 close).
     *
     * @throws IOException I/O 오류 또는 {@link HttpProtocolException}(응답 malformed)
     */
    public HttpResponse exchange(HttpRequest req) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: Socket 연결 → writeRequest → shutdownOutput()(half-close) → parseResponse 반환. try-with-resources close. keep-alive는 ch18");
    }
}
