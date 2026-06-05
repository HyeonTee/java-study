package study.chapter17;

import java.io.IOException;

/**
 * Layer B — <strong>진짜 {@link java.net.Socket}</strong>로 connect·타임아웃·<strong>half-close</strong>를 직접 만지는
 * 클라이언트. {@link EchoServer}의 짝.
 *
 * <p>이 단원의 가장 중요한 개념인 <strong>half-close({@link #finish()})</strong>를 손으로 친다 — 출력 방향만 닫아
 * 서버에 "더 안 보냄(EOF)"을 통지하는 것이 서버의 {@code readLine()}을 {@code null}로 만들어 serve 루프를
 * <strong>임의 sleep 없이 확정적으로</strong> 끝낸다. 이것이 request/response 데드락의 해법이자 동시에 테스트
 * flaky 제거 장치다.
 *
 * <p>{@link AutoCloseable}이다 — try-with-resources로 소켓을 닫는다.
 */
public final class EchoClient implements AutoCloseable {

    /**
     * loopback의 {@code port}에 연결하고 읽기 타임아웃을 건다.
     *
     * <p>힌트: {@code new Socket()} →
     * {@code connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), timeoutMillis)} →
     * {@code setSoTimeout(timeoutMillis)}. 연결도 읽기도 타임아웃을 걸어 영원히 매달리지 않게 한다(flaky 가드).
     *
     * @throws IOException 연결 실패/타임아웃 등
     */
    public EchoClient(int port, int timeoutMillis) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: new Socket() → connect(InetSocketAddress(getLoopbackAddress(), port), timeoutMillis) → setSoTimeout");
    }

    /**
     * 한 줄을 보내고(UTF-8 + CRLF + flush) 한 줄 응답을 받아 반환한다. 소켓은 살아 있어 연속 호출이 가능하다.
     *
     * <p>힌트: 소켓의 {@code getInputStream()}/{@code getOutputStream()}을 {@link LineProtocol}로 감싸
     * {@code writeLine(line)} 후 {@code readLine()}. (스트림/래퍼는 필드에 한 번 만들어 재사용하는 게 좋다.)
     *
     * @throws IOException I/O 오류
     */
    public String send(String line) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: 소켓 스트림을 LineProtocol로 감싸 writeLine 후 readLine. 살아있는 소켓이라 반복 호출 가능");
    }

    /**
     * {@code shutdownOutput()}으로 <strong>half-close</strong>한다 — 출력 방향만 닫아 서버에 FIN(="더 안 보냄,
     * EOF")을 통지한다. 이것이 서버의 {@code readLine()}을 {@code null}로 만들어 serve 루프를 확정 종료시킨다.
     * 소켓 전체는 아직 살아 있어 이미 보낸 요청의 응답은 받을 수 있다.
     *
     * <p>힌트: {@code socket.shutdownOutput()}. 이게 없으면 서버는 "더 올 줄" 알고 영원히 블로킹한다(고전 데드락).
     *
     * @throws IOException I/O 오류
     */
    public void finish() throws IOException {
        throw new UnsupportedOperationException(
                "TODO: socket.shutdownOutput() — 출력 반만 닫아 FIN 전송. 서버 readLine이 null→루프 종료. 안 하면 데드락");
    }

    /**
     * 소켓을 닫는다 — <strong>멱등</strong>(여러 번 호출해도 안전).
     *
     * @throws IOException close 오류
     */
    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("TODO: 이미 닫혔으면 return, 아니면 socket.close() (멱등)");
    }
}
