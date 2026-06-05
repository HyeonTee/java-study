package study.chapter18;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import study.chapter17.Headers;
import study.chapter17.HttpRequest;
import study.chapter17.HttpResponse;
import study.chapter17.RequestLine;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 진짜 소켓 통합 — <strong>여기서만 OS 소켓·스레드가 등장</strong>한다. {@link ConnectionLoopTest}가 프로토콜 로직을
 * 결정적으로 다 검증했으므로, 여기선 비결정성(동시성)에 대해 <strong>소수의 강한 불변식</strong>만 본다(결정성을 곱이
 * 아니라 분리된 합으로 — ch16 4규율 계승).
 *
 * <p>비결정성 길들이기: loopback + ephemeral port({@code :0}) + {@link Timeout}(데드락 시 영원 매달림 대신 실패).
 * 동시성 테스트는 임의 {@code sleep}이 아니라 <strong>{@link CountDownLatch} 출발 배리어</strong>로 N개를 동시에
 * 풀어놓고, 끝남도 {@code CountDownLatch}로 확정 대기한다.
 *
 * <p><strong>main에선 전부 빨간불</strong>: {@link HttpServer} 생성자 스텁이 {@code UnsupportedOperationException}을
 * 던져 즉시 실패한다(소켓 로직엔 닿지도 않음). ch17·ch18을 solve에서 다 푼 뒤 초록불이 된다. {@code @Timeout}이
 * 혹시 모를 매달림을 막는다.
 */
@DisplayName("진짜 소켓 통합 — accept 루프 × 주입 Executor (loopback + @Timeout + CountDownLatch)")
class HttpServerIntegrationTest {

    private static final java.nio.charset.Charset UTF_8 = StandardCharsets.UTF_8;

    /** 요청 target을 그대로 응답 바디로 echo — 어느 연결이 어느 응답을 받았는지 바디로 식별하기 위함. */
    private static final HttpHandler ECHO = req -> Responses.text(200, "OK", req.line().target());

    private static HttpRequest get(String target) {
        return new HttpRequest(new RequestLine("GET", target, "HTTP/1.1"), new Headers().with("Host", "x"), null);
    }

    @Nested
    @DisplayName("기본 왕복 — 한 연결")
    class Basic {

        @Test
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        @DisplayName("1요청 → 1응답, 바디는 target echo")
        void 단일_왕복() throws Exception {
            try (HttpServer server = new HttpServer(
                    ThreadingStrategy.virtualThreadPerConnection(), ECHO, ServerLimits.defaults())) {
                server.start();
                try (KeepAliveClient client = new KeepAliveClient(server.port(), 3000)) {
                    HttpResponse res = client.exchange(get("/hello"));
                    assertEquals(200, res.line().code());
                    assertEquals("/hello", new String(res.body(), UTF_8));
                }
            }
        }

        @Test
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        @DisplayName("keep-alive — 한 소켓으로 2요청을 연속 처리")
        void keep_alive_한_소켓_2요청() throws Exception {
            try (HttpServer server = new HttpServer(
                    ThreadingStrategy.virtualThreadPerConnection(), ECHO, ServerLimits.defaults())) {
                server.start();
                try (KeepAliveClient client = new KeepAliveClient(server.port(), 3000)) {
                    // half-close 없이 같은 소켓에서 두 번 — 연결이 살아 있어야(keep-alive) 둘째가 응답을 받는다.
                    assertEquals("/one", new String(client.exchange(get("/one")).body(), UTF_8));
                    assertEquals("/two", new String(client.exchange(get("/two")).body(), UTF_8));
                }
            }
        }
    }

    @Nested
    @DisplayName("동시성 불변식 — N개 동시 클라이언트")
    class Concurrency {

        @Test
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        @DisplayName("50개 동시 클라(가상 스레드) → 응답 바디 집합 == 보낸 target 집합")
        void 동시_50요청_각자_제_응답을_받는다() throws Exception {
            int n = 50;
            // 비결정성을 단 하나의 강한 불변식으로 압축: "각 연결이 '자기' 응답을 받는다"(응답 뒤섞임·유실 0).
            // 순서·타이밍은 보지 않는다 — 집합 동일성만 본다.
            Set<String> received = ConcurrentHashMap.newKeySet();
            CountDownLatch startBarrier = new CountDownLatch(1);
            CountDownLatch allDone = new CountDownLatch(n);

            try (HttpServer server = new HttpServer(
                    ThreadingStrategy.virtualThreadPerConnection(), ECHO, ServerLimits.defaults())) {
                server.start();
                int port = server.port();

                for (int i = 0; i < n; i++) {
                    final String target = "/req-" + i;
                    Thread t = new Thread(() -> {
                        try (KeepAliveClient client = new KeepAliveClient(port, 5000)) {
                            startBarrier.await();   // 모두 여기서 대기 → 한꺼번에 출발(진짜 동시성)
                            HttpResponse res = client.exchange(get(target));
                            received.add(new String(res.body(), UTF_8));
                        } catch (Exception ignored) {
                            // 실패한 연결은 received에 안 들어가므로 아래 집합 비교에서 드러난다.
                        } finally {
                            allDone.countDown();
                        }
                    });
                    t.setDaemon(true);
                    t.start();
                }

                startBarrier.countDown();   // 출발 배리어 개방
                assertTrue(allDone.await(8, TimeUnit.SECONDS), "모든 클라이언트가 8초 안에 끝나야 한다");

                Set<String> expected = new HashSet<>();
                for (int i = 0; i < n; i++) {
                    expected.add("/req-" + i);
                }
                assertEquals(expected, received, "각 연결이 정확히 제 target을 바디로 돌려받아야 한다(뒤섞임·유실 0)");
            }
        }
    }
}
