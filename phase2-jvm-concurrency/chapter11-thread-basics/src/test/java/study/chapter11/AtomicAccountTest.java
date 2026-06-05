package study.chapter11;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AtomicAccount — Atomic과 CAS 재시도 루프")
class AtomicAccountTest {

    static final int THREADS = 16;
    static final int ITERS = 10_000;

    @Nested
    @DisplayName("단일 스레드 계약")
    class SingleThread {

        @Test
        void 초기_잔액과_입금_출금() {
            AtomicAccount acc = new AtomicAccount(100);
            assertEquals(100L, acc.balance());
            assertEquals(150L, acc.deposit(50));
            assertTrue(acc.withdraw(30));
            assertEquals(120L, acc.balance());
        }

        @Test
        void 잔액이_부족하면_출금은_false이고_잔액은_그대로() {
            AtomicAccount acc = new AtomicAccount(40);
            assertFalse(acc.withdraw(100));
            assertEquals(40L, acc.balance());
        }

        @Test
        void 입금_횟수를_센다() {
            AtomicAccount acc = new AtomicAccount(0);
            acc.deposit(1);
            acc.deposit(1);
            acc.deposit(1);
            assertEquals(3L, acc.depositCount());
        }

        @Test
        void 음수나_0_금액은_예외() {
            AtomicAccount acc = new AtomicAccount(10);
            assertThrows(IllegalArgumentException.class, () -> acc.deposit(0));
            assertThrows(IllegalArgumentException.class, () -> acc.deposit(-1));
            assertThrows(IllegalArgumentException.class, () -> acc.withdraw(-5));
        }

        @Test
        void 초기_잔액이_음수면_예외() {
            assertThrows(IllegalArgumentException.class, () -> new AtomicAccount(-1));
        }
    }

    @Nested
    @DisplayName("고경합 불변식 (올바른 구현은 결정적)")
    class HighContention {

        @RepeatedTest(10)
        @Timeout(10)
        @DisplayName("동시 입금: 잔액 = 초기 + N×M, 입금 횟수 = N×M")
        void 동시_입금은_정확히_누적된다() {
            AtomicAccount acc = new AtomicAccount(0);

            Concurrently.run(THREADS, () -> {
                for (int i = 0; i < ITERS; i++) {
                    acc.deposit(1);
                }
            });

            assertEquals((long) THREADS * ITERS, acc.balance());
            assertEquals((long) THREADS * ITERS, acc.depositCount());
        }

        @RepeatedTest(10)
        @Timeout(10)
        @DisplayName("입금/출금 혼합: 돈은 생기지도 사라지지도 않고, 잔액은 음수가 되지 않는다")
        void 입금과_출금이_섞여도_보존된다() {
            long initial = 1_000_000;
            AtomicAccount acc = new AtomicAccount(initial);

            AtomicLong totalDeposited = new AtomicLong(0);
            AtomicLong totalWithdrawn = new AtomicLong(0);

            Concurrently.run(THREADS, () -> {
                long localDep = 0;
                long localWd = 0;
                for (int i = 0; i < ITERS; i++) {
                    if ((i & 1) == 0) {
                        acc.deposit(2);
                        localDep += 2;
                    } else if (acc.withdraw(1)) {   // 성공한 출금만 집계
                        localWd += 1;
                    }
                    // 어느 순간에도 잔액이 음수면 안 된다 (CAS로 '충분할 때만' 출금)
                    assertTrue(acc.balance() >= 0, "잔액이 음수가 되었다 — CAS 조건이 깨짐");
                }
                totalDeposited.addAndGet(localDep);
                totalWithdrawn.addAndGet(localWd);
            });

            // 보존 법칙: 최종 잔액 = 초기 + 입금합 − 성공한 출금합
            assertEquals(initial + totalDeposited.get() - totalWithdrawn.get(), acc.balance());
        }
    }
}
