package study.chapter17;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;

/**
 * accept 루프 엔진 — 진짜 {@code ServerSocket}을 열고, 받은 연결마다 <strong>주입받은 {@link Executor}에 위임</strong>한다.
 * ch15 {@code EchoServer}를 한 겹 위로 계승.
 *
 * <p>스레딩 모델을 <strong>모른다</strong>: {@code executor.execute(() -> handleConnection(s))} 한 줄이 "모델 ⟂ 연결 루프"
 * 직교성의 물리적 구현이다. 단일/연결당/풀/가상 — 주입하는 {@link Executor}만 갈아끼우면 서버 코드는 그대로다.
 *
 * <p>비결정성 길들이기(ch15 4규율): loopback + ephemeral port({@code :0}) + accept 소켓 {@code setSoTimeout} +
 * accept 디스패처는 <strong>데몬 스레드</strong>. {@code AutoCloseable}이다.
 */
public final class HttpServer implements AutoCloseable {

    /**
     * {@code ServerSocket}을 loopback의 ephemeral port({@code :0})에 bind한다(ch15 {@code EchoServer} 회수).
     *
     * <p>힌트: {@code new ServerSocket()} → {@code setReuseAddress(true)} →
     * {@code bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0))}. executor/handler/limits를 필드에 보관.
     * <strong>정지 플래그는 반드시 {@code volatile}</strong>(accept 스레드와 {@link #close} 스레드 사이 가시성 — ch07/ch10 회수).
     *
     * @throws IOException bind 실패 등
     */
    public HttpServer(Executor executor, HttpHandler handler, ServerLimits limits) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: ServerSocket()→setReuseAddress(true)→bind(InetSocketAddress(getLoopbackAddress(),0)). 필드 저장. 정지 플래그는 volatile");
    }

    /**
     * bind된 실제 포트(테스트가 클라이언트를 붙인다).
     *
     * <p>힌트: {@code serverSocket.getLocalPort()}.
     */
    public int port() {
        throw new UnsupportedOperationException("TODO: serverSocket.getLocalPort() 반환");
    }

    /**
     * accept 루프를 <strong>별도 데몬 디스패처 스레드</strong>에서 시작한다(블로킹 아님 — 테스트가 background를 직접 관리하지
     * 않아도 됨). 디스패처 스레드는 주입 {@link Executor}와 별개(항상 1개 전용).
     *
     * <p>힌트: 데몬 스레드 하나를 만들어 그 안에서 {@code while (!closed) { Socket s = accept(); executor.execute(() -> handleConnection(s)); }}.
     * 닫힌 {@code ServerSocket}의 {@code accept()}는 {@code SocketException}으로 깨어나므로 그것을 종료 신호로 삼아
     * 루프를 빠져나간다(ch15 회수). {@code closed}는 volatile.
     */
    public void start() {
        throw new UnsupportedOperationException(
                "TODO: 데몬 스레드에서 while(!closed){ Socket s=accept(); executor.execute(()->handleConnection(s)); }. close 시 accept의 SocketException으로 종료");
    }

    /**
     * 한 연결을 처리한다: {@code setSoTimeout} 후 {@link ConnectionLoop#serve}에 위임하고, 끝나면 소켓을 닫는다(LIFO).
     * 한 연결의 예외는 <strong>그 연결만 격리</strong>한다(소켓만 닫고 다음 연결·서버 전체엔 영향 0 — ch11 "작업 하나
     * 실패가 풀을 안 죽임" 회수).
     *
     * <p>힌트: {@code try (s) { s.setSoTimeout(limits.soTimeoutMillis()); ConnectionLoop.serve(s.getInputStream(), s.getOutputStream(), handler, limits); } catch (IOException ignored) {}}.
     * 400/431 같은 프로토콜 에러는 {@code serve} 안에서 이미 응답으로 처리되므로, 여기 catch는 "이미 응답한 뒤의 전송
     * 실패/소켓 타임아웃"만 삼킨다(프로토콜 에러를 여기서 삼키면 안 됨).
     */
    public void handleConnection(Socket s) {
        throw new UnsupportedOperationException(
                "TODO: try(s){ setSoTimeout; ConnectionLoop.serve(in,out,handler,limits) } catch(IOException ignored){}. 연결당 격리");
    }

    /**
     * 서버를 닫는다 — <strong>멱등</strong>. {@code closed=true}(volatile) + {@code serverSocket.close()}(블로킹 accept를
     * {@code SocketException}으로 깨움, ch15 회수). 그다음 주입 {@link Executor}를 graceful하게 정리한다.
     *
     * <p>힌트: 이미 닫혔으면 return. 그다음 {@code executor instanceof ExecutorService es}면 {@code es.shutdown()} +
     * {@code es.awaitTermination(...)}, {@code AutoCloseable}이면 {@code close()}. (caller-runs {@code Executor}는
     * 정리할 게 없어 어느 분기도 안 탄다 — 이 {@code instanceof} 분기가 4모델을 모두 안전하게 정리하는 열쇠.)
     *
     * @throws IOException close 오류
     */
    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException(
                "TODO: 멱등. closed=true + serverSocket.close()(accept 깨움). executor instanceof ExecutorService면 shutdown+awaitTermination, AutoCloseable이면 close");
    }
}
