package study.chapter17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 진짜 소켓 — loopback 통합.
 *
 * <p>여기서만 진짜 OS 소켓이 등장한다. 비결정성을 "태그로 숨기지" 않고 <strong>4규율로 길들여</strong> 기본
 * {@code ./gradlew :chapter17-tcp-socket:test}에 포함한다: ephemeral port({@code :0}), loopback only,
 * {@link Timeout}(데드락 시 영원 매달림 대신 실패), <strong>half-close로 서버 루프를 확정 종료</strong>
 * (임의 {@code sleep} 절대 금지). 서버는 별 스레드에서 {@code serveOne}을 돌리고, 클라가 {@code finish()}로
 * EOF를 보내 루프가 결정적으로 끝난다.
 */
@DisplayName("진짜 소켓 — loopback 통합 (ephemeral port + @Timeout + half-close)")
class SocketIntegrationTest {

    /** 백그라운드에서 단일 연결을 echo 처리하는 서버 스레드를 띄운다. */
    private static Thread serveOneInBackground(EchoServer server, int soTimeoutMillis) {
        Thread t = new Thread(() -> {
            try {
                server.serveOne(soTimeoutMillis);
            } catch (IOException ignored) {
                // 연결 종료/타임아웃은 테스트 흐름상 정상 — 무시
            }
        }, "echo-server");
        t.setDaemon(true);
        t.start();
        return t;
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @DisplayName("echo 왕복 — 보낸 줄이 그대로 돌아온다")
    void echo_왕복() throws Exception {
        try (EchoServer server = new EchoServer()) {
            int port = server.port();
            Thread serverThread = serveOneInBackground(server, 2000);

            try (EchoClient client = new EchoClient(port, 2000)) {
                assertEquals("hello", client.send("hello"));
                assertEquals("world", client.send("world"));
                client.finish();   // half-close → 서버 serve 루프 종료
            }

            serverThread.join(5000);
            assertFalse(serverThread.isAlive(), "half-close 후 serve 루프가 끝나야 한다");
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @DisplayName("half-close가 서버 루프를 확정 종료시킨다 (sleep 아니라 shutdownOutput으로)")
    void half_close가_서버를_종료시킨다() throws Exception {
        try (EchoServer server = new EchoServer()) {
            // soTimeout은 넉넉히 5초. 그래도 thread는 그보다 훨씬 빨리 끝나야 한다 —
            // 끝내는 건 타임아웃이 아니라 클라의 shutdownOutput(FIN)이기 때문.
            Thread serverThread = serveOneInBackground(server, 5000);

            try (EchoClient client = new EchoClient(server.port(), 5000)) {
                assertEquals("ping", client.send("ping"));
                client.finish();   // 이 한 줄이 서버 readLine()을 null로 만들어 루프를 끝낸다
            }

            serverThread.join(3000);   // soTimeout(5초)보다 짧게 join — half-close가 동작하면 이미 끝나 있다
            assertFalse(serverThread.isAlive(),
                    "shutdownOutput(FIN) 없이는 서버가 데드락한다 — half-close가 루프를 끝낸다");
        }
    }
}
