package study.chapter11;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MyThreadPool — 미니 고정 스레드풀")
class MyThreadPoolTest {

    @Nested
    @DisplayName("작업 실행 — 정확히 한 번")
    class ExactlyOnce {

        @RepeatedTest(10)
        @Timeout(10)
        @DisplayName("제출한 모든 작업이 정확히 한 번씩 실행된다")
        void 모든_작업이_정확히_한번() throws Exception {
            int workers = 4, tasks = 1000;
            MyThreadPool pool = new MyThreadPool(workers);
            AtomicInteger counter = new AtomicInteger();
            CountDownLatch done = new CountDownLatch(tasks);

            for (int i = 0; i < tasks; i++) {
                pool.submit(() -> {
                    counter.incrementAndGet();
                    done.countDown();
                });
            }
            assertTrue(done.await(10, SECONDS), "모든 작업이 실행 완료되어야 한다");
            pool.shutdown();
            assertTrue(pool.awaitTermination(5, SECONDS));
            assertEquals(tasks, counter.get(), "정확히 tasks번 실행 (중복·누락 없음)");
        }

        @Test
        @Timeout(10)
        @DisplayName("워커를 재사용한다 — 서로 다른 실행 스레드 수 ≤ 워커 수")
        void 워커_재사용() throws Exception {
            int workers = 3, tasks = 500;
            MyThreadPool pool = new MyThreadPool(workers);
            Set<Long> threadIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
            CountDownLatch done = new CountDownLatch(tasks);

            for (int i = 0; i < tasks; i++) {
                pool.submit(() -> {
                    threadIds.add(Thread.currentThread().threadId());
                    done.countDown();
                });
            }
            assertTrue(done.await(10, SECONDS));
            pool.shutdown();
            assertTrue(pool.awaitTermination(5, SECONDS));
            assertTrue(threadIds.size() <= workers,
                    "작업마다 새 스레드를 만들지 않고 워커를 재사용해야 한다: " + threadIds.size());
        }
    }

    @Nested
    @DisplayName("생명주기")
    class Lifecycle {

        @Test
        @Timeout(10)
        @DisplayName("graceful shutdown — 이미 제출된 작업은 모두 완료된다")
        void graceful_shutdown() throws Exception {
            int tasks = 200;
            MyThreadPool pool = new MyThreadPool(2);
            AtomicInteger counter = new AtomicInteger();
            for (int i = 0; i < tasks; i++) {
                pool.submit(counter::incrementAndGet);
            }
            pool.shutdown();                       // 곧바로 종료 요청
            assertTrue(pool.awaitTermination(5, SECONDS));
            assertEquals(tasks, counter.get(), "shutdown 후에도 제출된 작업은 전부 실행");
            assertTrue(pool.isShutdown());
            assertTrue(pool.isTerminated());
        }

        @Test
        @Timeout(10)
        @DisplayName("shutdown 이후 submit은 거부된다")
        void shutdown후_submit_거부() throws Exception {
            MyThreadPool pool = new MyThreadPool(2);
            pool.shutdown();
            assertThrows(RejectedExecutionException.class, () -> pool.submit(() -> { }));
            pool.awaitTermination(5, SECONDS);
        }

        @Test
        @Timeout(10)
        @DisplayName("한 작업이 예외를 던져도 나머지는 모두 실행된다")
        void 예외_격리() throws Exception {
            int tasks = 100;
            MyThreadPool pool = new MyThreadPool(3);
            AtomicInteger ok = new AtomicInteger();
            for (int i = 0; i < tasks; i++) {
                final int n = i;
                pool.submit(() -> {
                    if (n % 10 == 0) {
                        throw new RuntimeException("의도적 실패");
                    }
                    ok.incrementAndGet();
                });
            }
            pool.shutdown();
            assertTrue(pool.awaitTermination(5, SECONDS));
            assertEquals(tasks - tasks / 10, ok.get(), "예외 작업을 빼고 나머지는 전부 실행");
        }
    }
}
