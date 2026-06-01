package study.chapter11;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MyBlockingQueue — synchronized + wait/notifyAll 블로킹 큐")
class MyBlockingQueueTest {

    @Nested
    @DisplayName("단일 스레드 계약")
    class Contract {

        @Test
        void put_take는_FIFO() throws Exception {
            MyBlockingQueue<Integer> q = new MyBlockingQueue<>(3);
            q.put(1);
            q.put(2);
            q.put(3);
            assertEquals(3, q.size());
            assertEquals(1, q.take());
            assertEquals(2, q.take());
            assertEquals(3, q.take());
            assertEquals(0, q.size());
        }

        @Test
        void capacity가_1미만이면_예외() {
            assertThrows(IllegalArgumentException.class, () -> new MyBlockingQueue<Integer>(0));
        }

        @Test
        void null_put은_NPE() {
            assertThrows(NullPointerException.class, () -> new MyBlockingQueue<String>(2).put(null));
        }
    }

    @Nested
    @DisplayName("블로킹 — 조건이 맞을 때까지 깨지 않는다")
    class Blocking {

        @Test
        @Timeout(5)
        @DisplayName("빈 큐 take는 put이 올 때까지 블록되었다가 값을 받는다")
        void 빈_큐_take는_블록() throws Exception {
            MyBlockingQueue<String> q = new MyBlockingQueue<>(1);
            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch returned = new CountDownLatch(1);
            AtomicReference<String> got = new AtomicReference<>();

            Thread t = new Thread(() -> {
                started.countDown();
                try {
                    got.set(q.take());
                    returned.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            t.start();
            started.await();
            // put 전에는 절대 깨면 안 된다 (올바른 구현이면 이 단언은 항상 참 → flaky red 없음)
            assertFalse(returned.await(50, MILLISECONDS), "put 전에 take가 깨면 안 된다");
            q.put("x");
            assertTrue(returned.await(2, SECONDS), "put 후에는 take가 깨어나야 한다");
            assertEquals("x", got.get());
        }

        @Test
        @Timeout(5)
        @DisplayName("가득 찬 큐 put은 take가 자리를 낼 때까지 블록되었다가 들어간다")
        void 가득_찬_큐_put은_블록() throws Exception {
            MyBlockingQueue<String> q = new MyBlockingQueue<>(1);
            q.put("first");                       // 큐가 가득 참
            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch returned = new CountDownLatch(1);

            Thread t = new Thread(() -> {
                started.countDown();
                try {
                    q.put("second");              // 블록되어야 함
                    returned.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            t.start();
            started.await();
            assertFalse(returned.await(50, MILLISECONDS), "자리 나기 전에 put이 깨면 안 된다");
            assertEquals("first", q.take());      // 자리를 냄
            assertTrue(returned.await(2, SECONDS), "take 후 put이 깨어나야 한다");
            assertEquals(1, q.size());
        }

        @Test
        @Timeout(5)
        @DisplayName("블록된 take를 인터럽트하면 InterruptedException")
        void 블록된_take_인터럽트() throws Exception {
            MyBlockingQueue<String> q = new MyBlockingQueue<>(1);
            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch interrupted = new CountDownLatch(1);

            Thread t = new Thread(() -> {
                started.countDown();
                try {
                    q.take();
                } catch (InterruptedException e) {
                    interrupted.countDown();      // IE를 받음
                }
            });
            t.start();
            started.await();
            assertFalse(interrupted.await(50, MILLISECONDS));
            t.interrupt();
            assertTrue(interrupted.await(2, SECONDS), "블록된 take는 인터럽트로 IE를 던져야 한다");
        }
    }

    @Nested
    @DisplayName("고경합 보존 — 넣은 합 == 받은 합, 손실·중복 없음")
    class Conservation {

        static final Integer SENTINEL = Integer.MIN_VALUE;

        @RepeatedTest(5)
        @Timeout(20)
        void 다수_생산자_소비자_보존() throws Exception {
            int producers = 6, consumers = 6, perProducer = 2000, capacity = 64;
            MyBlockingQueue<Integer> q = new MyBlockingQueue<>(capacity);
            AtomicLong producedSum = new AtomicLong();
            AtomicLong consumedSum = new AtomicLong();
            AtomicInteger consumedCount = new AtomicInteger();

            List<Thread> threads = new ArrayList<>();
            List<Thread> producerThreads = new ArrayList<>();
            for (int p = 0; p < producers; p++) {
                Thread t = new Thread(() -> {
                    long local = 0;
                    for (int i = 1; i <= perProducer; i++) {
                        try {
                            q.put(i);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        local += i;
                    }
                    producedSum.addAndGet(local);
                });
                producerThreads.add(t);
                threads.add(t);
            }
            for (int c = 0; c < consumers; c++) {
                Thread t = new Thread(() -> {
                    while (true) {
                        try {
                            Integer v = q.take();
                            if (v.equals(SENTINEL)) {
                                return;
                            }
                            consumedSum.addAndGet(v);
                            consumedCount.incrementAndGet();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                });
                threads.add(t);
            }

            threads.forEach(Thread::start);
            for (Thread p : producerThreads) {
                p.join();
            }
            // 생산 끝 → 소비자 수만큼 종료 신호
            for (int c = 0; c < consumers; c++) {
                q.put(SENTINEL);
            }
            for (Thread t : threads) {
                t.join();
            }

            assertEquals(producers * perProducer, consumedCount.get(), "받은 개수 == 넣은 개수");
            assertEquals(producedSum.get(), consumedSum.get(), "받은 합 == 넣은 합 (손실·중복 없음)");
        }
    }
}
