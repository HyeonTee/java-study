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

@DisplayName("LockedBlockingQueue — ReentrantLock + 두 Condition 블로킹 큐")
class LockedBlockingQueueTest {

    @Nested
    @DisplayName("단일 스레드 계약 (MyBlockingQueue와 같은 계약)")
    class Contract {

        @Test
        void put_take는_FIFO() throws Exception {
            LockedBlockingQueue<Integer> q = new LockedBlockingQueue<>(2);
            q.put(10);
            q.put(20);
            assertEquals(10, q.take());
            assertEquals(20, q.take());
            assertEquals(0, q.size());
        }

        @Test
        void capacity가_1미만이면_예외() {
            assertThrows(IllegalArgumentException.class, () -> new LockedBlockingQueue<Integer>(0));
        }

        @Test
        void null_put은_NPE() {
            assertThrows(NullPointerException.class, () -> new LockedBlockingQueue<String>(2).put(null));
        }
    }

    @Nested
    @DisplayName("블로킹")
    class Blocking {

        @Test
        @Timeout(5)
        void 빈_큐_take는_put까지_블록() throws Exception {
            LockedBlockingQueue<String> q = new LockedBlockingQueue<>(1);
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
            assertFalse(returned.await(50, MILLISECONDS));
            q.put("v");
            assertTrue(returned.await(2, SECONDS));
            assertEquals("v", got.get());
        }
    }

    @Nested
    @DisplayName("offer(timeout) — 시한부 넣기")
    class TimedOffer {

        @Test
        @Timeout(5)
        void 가득_차면_시간초과후_false() throws Exception {
            LockedBlockingQueue<String> q = new LockedBlockingQueue<>(1);
            q.put("full");
            long start = System.nanoTime();
            boolean ok = q.offer("x", 100, MILLISECONDS);
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            assertFalse(ok, "가득 찬 큐에 시한부 offer는 false");
            assertTrue(elapsedMs >= 90, "최소한 타임아웃만큼은 기다려야 한다: " + elapsedMs + "ms");
        }

        @Test
        @Timeout(5)
        void 자리가_있으면_즉시_true() throws Exception {
            LockedBlockingQueue<String> q = new LockedBlockingQueue<>(1);
            assertTrue(q.offer("x", 100, MILLISECONDS));
            assertEquals(1, q.size());
        }
    }

    @Nested
    @DisplayName("고경합 보존")
    class Conservation {

        static final Integer SENTINEL = Integer.MIN_VALUE;

        @RepeatedTest(5)
        @Timeout(20)
        void 다수_생산자_소비자_보존() throws Exception {
            int producers = 6, consumers = 6, perProducer = 2000, capacity = 64;
            LockedBlockingQueue<Integer> q = new LockedBlockingQueue<>(capacity);
            AtomicLong producedSum = new AtomicLong();
            AtomicLong consumedSum = new AtomicLong();
            AtomicInteger consumedCount = new AtomicInteger();

            List<Thread> all = new ArrayList<>();
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
                all.add(t);
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
                all.add(t);
            }

            all.forEach(Thread::start);
            for (Thread p : producerThreads) {
                p.join();
            }
            for (int c = 0; c < consumers; c++) {
                q.put(SENTINEL);
            }
            for (Thread t : all) {
                t.join();
            }

            assertEquals(producers * perProducer, consumedCount.get());
            assertEquals(producedSum.get(), consumedSum.get());
        }
    }
}
