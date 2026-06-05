package study.chapter15;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("VirtualThreads — 가상 스레드 정체성과 경량성")
class VirtualThreadsTest {

    @Nested
    @DisplayName("정체성 (결정적)")
    class Identity {

        @Test
        @Timeout(5)
        void 가상_스레드_안에서는_isVirtual_true() {
            AtomicBoolean onVirtual = new AtomicBoolean(false);
            VirtualThreads.runAll(List.of(() -> onVirtual.set(VirtualThreads.runningOnVirtualThread())));
            assertTrue(onVirtual.get(), "가상 스레드 안이면 true");
        }

        @Test
        void 메인_스레드는_isVirtual_false() {
            assertFalse(VirtualThreads.runningOnVirtualThread());
        }

        @Test
        @Timeout(5)
        void newNamedVirtual은_미시작_가상스레드() throws Exception {
            AtomicBoolean ran = new AtomicBoolean(false);
            Thread t = VirtualThreads.newNamedVirtual("worker-1", () -> ran.set(true));
            assertTrue(t.isVirtual());
            assertFalse(ran.get(), "start 전에는 실행되지 않아야");
            t.start();
            t.join();
            assertTrue(ran.get());
        }
    }

    @Nested
    @DisplayName("실행 — 모든 작업 정확히 한 번 (결정적)")
    class Execution {

        @Test
        @Timeout(5)
        void runAll은_모든_작업을_실행하고_기다린다() {
            AtomicInteger counter = new AtomicInteger();
            List<Runnable> tasks = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                tasks.add(counter::incrementAndGet);
            }
            VirtualThreads.runAll(tasks);
            assertEquals(100, counter.get(), "반환 시점엔 전부 완료(join)되어 있어야");
        }

        @Test
        void null_작업은_NPE() {
            assertThrows(NullPointerException.class,
                    () -> VirtualThreads.runAll(java.util.Arrays.asList((Runnable) null)));
        }

        @Test
        void 음수_count는_예외() {
            assertThrows(IllegalArgumentException.class, () -> VirtualThreads.runManyVirtual(-1, i -> { }));
        }
    }

    @Nested
    @DisplayName("경량성 — 1만 개가 OOM 없이 전부 완료 (손실 없음)")
    class Lightweight {

        @RepeatedTest(3)
        @Timeout(30)
        void 만개_가상스레드_전부_완료() {
            int n = 10_000;
            AtomicInteger counter = new AtomicInteger();
            VirtualThreads.runManyVirtual(n, i -> counter.incrementAndGet());
            assertEquals(n, counter.get(), "1만 개가 손실 없이 전부 실행");
        }
    }
}
