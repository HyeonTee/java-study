package study.chapter13;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MyTaskScope — 가상 스레드 위 미니 structured concurrency")
class MyTaskScopeTest {

    @Nested
    @DisplayName("전부 성공 → 결과 수집")
    class AllSucceed {

        @Test
        @Timeout(5)
        void fork한_모든_작업_결과를_join후_수집() throws Exception {
            try (MyTaskScope scope = new MyTaskScope()) {
                MyTaskScope.Subtask<Integer> a = scope.fork(() -> 1);
                MyTaskScope.Subtask<Integer> b = scope.fork(() -> 2);
                MyTaskScope.Subtask<Integer> c = scope.fork(() -> 3);
                scope.join();
                scope.throwIfFailed();
                assertEquals(MyTaskScope.Subtask.State.SUCCESS, a.state());
                assertEquals(1, a.get());
                assertEquals(2, b.get());
                assertEquals(3, c.get());
            }
        }

        @Test
        @Timeout(5)
        void 미완료_작업의_get은_예외() throws Exception {
            CountDownLatch release = new CountDownLatch(1);
            try (MyTaskScope scope = new MyTaskScope()) {
                MyTaskScope.Subtask<Integer> a = scope.fork(() -> {
                    release.await();   // 풀어줄 때까지 실행 중(미완료) 상태 유지
                    return 1;
                });
                assertThrows(IllegalStateException.class, a::get, "미완료(UNAVAILABLE) 작업의 get은 예외");
                release.countDown();
                scope.join();
                assertEquals(1, a.get());   // 완료 후엔 결과 반환
            }
        }
    }

    @Nested
    @DisplayName("하나 실패 → 형제 취소 + 첫 예외 전파")
    class ShutdownOnFailure {

        @Test
        @Timeout(5)
        void 한_작업_실패시_느린_형제가_취소되고_예외_전파() throws Exception {
            CountDownLatch slowStarted = new CountDownLatch(1);
            CountDownLatch slowInterrupted = new CountDownLatch(1);
            AtomicBoolean slowFinishedNormally = new AtomicBoolean(false);

            try (MyTaskScope scope = new MyTaskScope()) {
                scope.fork(() -> {
                    slowStarted.countDown();
                    try {
                        Thread.sleep(10_000);          // 오래 블록
                        slowFinishedNormally.set(true);
                    } catch (InterruptedException e) {
                        slowInterrupted.countDown();   // 취소(인터럽트) 관측
                        throw e;
                    }
                    return "slow";
                });
                slowStarted.await();                    // 느린 형제가 확실히 sleep에 진입
                scope.fork(() -> {
                    throw new IllegalStateException("boom");
                });
                scope.join();

                assertTrue(slowInterrupted.await(2, SECONDS), "느린 형제가 인터럽트로 취소되어야 한다");
                assertFalse_(slowFinishedNormally.get());
                ExecutionException ee = assertThrows(ExecutionException.class, scope::throwIfFailed);
                assertInstanceOf(IllegalStateException.class, ee.getCause());
            }
        }

        private static void assertFalse_(boolean b) {
            org.junit.jupiter.api.Assertions.assertFalse(b, "취소됐으므로 정상 종료되면 안 된다");
        }
    }

    @Nested
    @DisplayName("생명주기")
    class Lifecycle {

        @Test
        @Timeout(5)
        void join_전_throwIfFailed는_예외() throws Exception {
            try (MyTaskScope scope = new MyTaskScope()) {
                scope.fork(() -> 1);
                assertThrows(IllegalStateException.class, scope::throwIfFailed);
                scope.join();
            }
        }

        @Test
        @Timeout(5)
        void close는_미완료_작업을_정리한다() throws Exception {
            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch interrupted = new CountDownLatch(1);
            MyTaskScope scope = new MyTaskScope();
            scope.fork(() -> {
                started.countDown();
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    interrupted.countDown();
                    throw e;
                }
                return 1;
            });
            started.await();
            scope.close();   // try-with-resources 없이 직접 — 남은 작업 취소·정리
            assertTrue(interrupted.await(2, SECONDS), "close가 남은 작업을 취소해야");
        }
    }
}
