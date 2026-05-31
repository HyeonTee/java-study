package study.chapter07;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("SafeCounter — synchronized로 잃어버린 갱신 방지")
class SafeCounterTest {

    static final int THREADS = 16;
    static final int ITERS = 10_000;   // 총 160,000회 증가

    @Nested
    @DisplayName("단일 스레드 계약")
    class SingleThread {

        @Test
        void increment는_1씩_증가한다() {
            SafeCounter c = new SafeCounter();
            c.increment();
            c.increment();
            assertEquals(2L, c.get());
        }

        @Test
        void add는_delta만큼_더한다_음수도() {
            SafeCounter c = new SafeCounter();
            c.add(10);
            c.add(-3);
            assertEquals(7L, c.get());
        }

        @Test
        void reset하면_0이_된다() {
            SafeCounter c = new SafeCounter();
            c.add(99);
            c.reset();
            assertEquals(0L, c.get());
        }
    }

    @Nested
    @DisplayName("고경합 불변식 (올바른 구현은 항상 N×M)")
    class HighContention {

        @RepeatedTest(10)
        @Timeout(10)
        @DisplayName("16스레드 × 10000회 increment 후 값은 정확히 160000")
        void 동시_increment_후_최종값은_정확히_곱이다() {
            SafeCounter counter = new SafeCounter();

            Concurrently.run(THREADS, () -> {
                for (int i = 0; i < ITERS; i++) {
                    counter.increment();
                }
            });

            assertEquals((long) THREADS * ITERS, counter.get());
        }

        @RepeatedTest(10)
        @Timeout(10)
        @DisplayName("16스레드 × 10000회 add(1) 후에도 값은 정확히 160000")
        void 동시_add_후_최종값은_정확히_곱이다() {
            SafeCounter counter = new SafeCounter();

            Concurrently.run(THREADS, () -> {
                for (int i = 0; i < ITERS; i++) {
                    counter.add(1);
                }
            });

            assertEquals((long) THREADS * ITERS, counter.get());
        }
    }
}
