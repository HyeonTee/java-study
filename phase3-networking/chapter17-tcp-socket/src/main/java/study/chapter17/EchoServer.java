package study.chapter17;

import java.io.IOException;

/**
 * Layer B — <strong>진짜 {@link java.net.ServerSocket}</strong>로 소켓 수명주기를 직접 만지는 echo 서버.
 *
 * <p>Layer A(프로토콜)는 소켓을 몰랐다. 여기서는 그 프로토콜({@link LineProtocol})을 진짜 소켓 스트림에
 * <strong>꽂기만</strong> 한다 — 배선은 얇다. 단일 연결·동기 처리(다중 연결 동시성은 Phase 2에서 다뤘고,
 * flaky 온상이라 본 단원 밖이다).
 *
 * <p><strong>flaky를 길들이는 규율</strong>: 포트는 항상 ephemeral(<strong>{@code :0}</strong> → OS가 빈 포트
 * 할당, 하드코딩 금지로 포트 충돌 0), 주소는 loopback only, accept한 소켓엔 {@code setSoTimeout}으로 무한
 * 블로킹 방지. 자원은 try-with-resources / ch16 {@code closeAll}로 <strong>LIFO</strong> close.
 *
 * <p>{@link AutoCloseable}이다 — try-with-resources로 서버 소켓을 닫는다.
 */
public final class EchoServer implements AutoCloseable {

    /**
     * 새 {@code ServerSocket}을 만들어 {@code setReuseAddress(true)} 후 <strong>loopback의 ephemeral 포트
     * ({@code :0})</strong>에 bind한다.
     *
     * <p>힌트: {@code new ServerSocket()} → {@code setReuseAddress(true)} →
     * {@code bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0))}. 포트 0 = OS가 빈 포트를 골라줘
     * 포트 충돌을 원천 차단한다.
     *
     * @throws IOException bind 실패 등
     */
    public EchoServer() throws IOException {
        throw new UnsupportedOperationException(
                "TODO: new ServerSocket() → setReuseAddress(true) → bind(InetSocketAddress(getLoopbackAddress(), 0))");
    }

    /**
     * bind된 실제 포트를 반환한다(테스트가 이 포트로 {@link EchoClient}를 접속시킨다).
     *
     * <p>힌트: {@code serverSocket.getLocalPort()}.
     */
    public int port() {
        throw new UnsupportedOperationException("TODO: serverSocket.getLocalPort() 반환");
    }

    /**
     * {@code accept()}로 단일 연결을 받아(블로킹), 그 소켓의 in/out 스트림을 echo 프로토콜에 위임해 상대가
     * half-close(EOF)할 때까지 한 줄씩 그대로 되돌려준다.
     *
     * <p>힌트: {@code accept()}로 받은 {@code Socket}을 try-with-resources로 감싸라(끝나면 LIFO close — ch16
     * 수명주기 직결). 그 소켓에 {@code setSoTimeout(soTimeoutMillis)}를 걸어 무한 블로킹을 막고(flaky 가드),
     * {@code getInputStream()}/{@code getOutputStream()}을 {@code new LineProtocol(in, out).serve(s -> s)}에 넘긴다.
     * <strong>서버 소켓 자체는 닫지 않는다</strong>(다음 {@code serveOne} 호출을 위해).
     *
     * @param soTimeoutMillis accept된 소켓의 읽기 타임아웃(ms). 0이면 무한(권장하지 않음).
     * @throws IOException I/O 오류
     */
    public void serveOne(int soTimeoutMillis) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: accept() → 받은 Socket을 try-with-resources로. setSoTimeout 후 스트림을 LineProtocol.serve(s->s)에 위임(에코)");
    }

    /**
     * 서버 소켓을 닫는다 — <strong>멱등</strong>(여러 번 호출해도 안전).
     *
     * <p>힌트: 이미 닫혔으면 그냥 return, 아니면 {@code serverSocket.close()}. 블로킹된 {@code accept()}가
     * 있었다면 닫힘과 함께 {@code SocketException}으로 깨어난다 — 블로킹 서버를 멈추는 정석이다.
     *
     * @throws IOException close 오류
     */
    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException(
                "TODO: 이미 닫혔으면 return, 아니면 serverSocket.close() (멱등). 닫힌 소켓의 accept는 SocketException으로 깨어난다");
    }
}
