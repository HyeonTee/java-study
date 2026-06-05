package study.chapter12;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ch12 — 인터럽트(interruption) 기반 협력적 취소.
 *
 * <p>동시성 테스트지만 타이밍에 의존하지 않는다: 출발 배리어용 {@link CountDownLatch}로
 * "워커가 준비됐다"는 시점만 확인하고, 그 뒤엔 {@code interrupt()} → {@code join()} → 불변식 단언만 한다.
 * {@link Timeout}이 무한 루프/미종료를 실패로 전환한다.
 */
@DisplayName("ch12 — 인터럽트로 협력적 취소하기")
@Timeout(5)
class InterruptionTest {

    @Nested
    @DisplayName("InterruptibleCounter — 인터럽트 비트 폴링")
    class Counter {

        @Test
        @DisplayName("인터럽트되면 협력적으로 종료한다")
        void 인터럽트되면_협력적으로_종료한다() throws InterruptedException {
            var c = new InterruptibleCounter();
            var t = new Thread(c);
            t.start();
            t.interrupt();
            t.join(2000);

            assertFalse(t.isAlive(), "인터럽트 후 워커는 종료되어야 한다");
            assertTrue(c.exitedByInterrupt(), "루프는 인터럽트 때문에 끝났어야 한다");
        }
    }

    @Nested
    @DisplayName("InterruptibleBlocker — IE와 비트 복원")
    class Blocker {

        @Test
        @DisplayName("블로킹 중 인터럽트는 IE를 던지고 비트를 지우며 복원하면 다시 선다")
        void 블로킹중_인터럽트는_IE를_던지고_비트를_지우며_복원하면_다시_선다() throws InterruptedException {
            var ready = new CountDownLatch(1);
            var b = new InterruptibleBlocker(ready);
            var t = new Thread(b);
            t.start();

            assertTrue(ready.await(2, TimeUnit.SECONDS), "워커가 블로킹 진입 신호를 보내야 한다");
            t.interrupt();
            t.join(2000);

            assertFalse(t.isAlive(), "인터럽트 후 워커는 종료되어야 한다");
            assertTrue(b.threwInterrupted(), "블로킹 호출은 InterruptedException을 던졌어야 한다");
            assertFalse(b.flagClearedInCatch(), "IE를 던지는 순간 인터럽트 비트는 꺼진다");
            assertTrue(b.flagAfterRestore(), "interrupt()로 복원하면 비트가 다시 켜진다");
        }
    }

    @Nested
    @DisplayName("InterruptStatus — drain vs isSet")
    class Status {

        @Test
        @DisplayName("drain은 읽고 지우고 isSet은 안 지운다")
        void drain은_읽고_지우고_isSet은_안_지운다() throws InterruptedException {
            // 현재 스레드의 인터럽트 비트를 다루므로 별도 워커 스레드 안에서 호출하고
            // 결과를 boolean 홀더에 캡처한다(공유 변수는 join 이후에 안전하게 읽는다).
            boolean[] r = new boolean[4];
            var worker = new Thread(() -> {
                Thread.currentThread().interrupt();
                r[0] = InterruptStatus.isSet();   // 비트 켜짐 → true, 지우지 않음
                r[1] = InterruptStatus.isSet();   // 여전히 켜짐 → true
                r[2] = InterruptStatus.drain();   // 읽고 지움 → true
                r[3] = InterruptStatus.drain();   // 이미 지워짐 → false
            });
            worker.start();
            worker.join(2000);

            assertFalse(worker.isAlive(), "워커는 종료되어야 한다");
            assertTrue(r[0], "isSet은 켜진 비트를 true로 읽는다");
            assertTrue(r[1], "isSet은 비트를 지우지 않으므로 두 번째도 true");
            assertTrue(r[2], "drain은 켜진 비트를 true로 읽는다");
            assertFalse(r[3], "drain이 비트를 지웠으므로 두 번째는 false");
        }
    }

    @Nested
    @DisplayName("CancellableGroup — 취소 전파")
    class Group {

        @Test
        @DisplayName("취소는 모든 워커에 전파된다")
        void 취소는_모든_워커에_전파된다() throws InterruptedException {
            var g = new CancellableGroup();
            var ready = new CountDownLatch(4);
            g.startWorkers(4, ready);

            assertTrue(ready.await(2, TimeUnit.SECONDS), "워커 4개가 모두 준비되어야 한다");
            g.cancelAll();
            g.joinAll(1000);

            assertEquals(4, g.stoppedCount(), "취소 신호는 워커 4개 모두에 전파되어야 한다");
        }
    }
}
