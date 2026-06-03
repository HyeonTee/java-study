package study.chapter17;

import study.chapter16.HttpMessageWriter;
import study.chapter16.HttpParser;
import study.chapter16.HttpRequest;
import study.chapter16.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 통합 테스트의 짝(완성 제공 픽스처) — ch16 {@code MiniHttpClient}(1왕복·half-close)의 <strong>keep-alive 진화형</strong>.
 *
 * <p>한 소켓에 요청 N개를 {@code exchange}로 연속해 보낸다 — {@code MiniHttpClient}와 달리 <strong>half-close
 * ({@code shutdownOutput})를 하지 않는다</strong>(소켓을 살려둬 다음 요청을 보낼 수 있게). 이게 keep-alive의 클라이언트 쪽이다.
 *
 * <p>ch16 {@code HttpMessageWriter}/{@code HttpParser}를 그대로 쓰므로, ch16을 풀기 전(main)에는 이 메서드들도 ch16
 * 스텁 때문에 동작하지 않는다(통합 테스트가 빨간불인 이유 중 하나).
 */
final class KeepAliveClient implements AutoCloseable {

    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    KeepAliveClient(int port, int timeoutMillis) throws IOException {
        this.socket = new Socket();
        socket.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), timeoutMillis);
        socket.setSoTimeout(timeoutMillis);
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }

    /** 요청 하나를 보내고(half-close 없이) 응답 하나를 받는다. 소켓이 살아 있어 연속 호출 가능 = keep-alive. */
    HttpResponse exchange(HttpRequest req) throws IOException {
        HttpMessageWriter.writeRequest(out, req);   // ch16 — flush 포함
        return HttpParser.parseResponse(in);        // ch16 — 같은 소켓에서 다음 응답을 이어 읽음
    }

    @Override
    public void close() throws IOException {
        socket.close();   // 멱등
    }
}
