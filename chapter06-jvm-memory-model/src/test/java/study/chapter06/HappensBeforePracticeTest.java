package study.chapter06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HappensBeforePractice - Java Memory Model의 happens-before 규칙")
class HappensBeforePracticeTest {

    private final HappensBeforePractice hb = new HappensBeforePractice();

    // ── volatile ───────────────────────────────────────────────────

    @Nested
    @DisplayName("volatile 변수 규칙")
    class VolatileRule {

        @Test
        void volatile_쓰기는_후속_읽기에_대해_happens_before를_보장한다() {
            // JLS 17.4.5: volatile 변수에 대한 쓰기는
            // 그 변수에 대한 모든 후속 읽기에 대해 happens-before 관계를 형성한다.
            assertTrue(hb.doesVolatileWriteHappenBefore());
        }

        @Test
        void volatile은_가시성을_보장하는_경량_동기화_메커니즘이다() {
            // volatile 쓰기 시: CPU 캐시 → 메인 메모리 플러시
            // volatile 읽기 시: 메인 메모리에서 최신 값 로드
            // 따라서 한 스레드의 volatile 쓰기 결과가 다른 스레드에 반드시 보인다.
            assertTrue(hb.doesVolatileWriteHappenBefore(),
                    "volatile은 메인 메모리를 통한 가시성을 보장한다");
        }
    }

    // ── synchronized ───────────────────────────────────────────────

    @Nested
    @DisplayName("synchronized 블록 규칙")
    class SynchronizedRule {

        @Test
        void synchronized_unlock은_후속_lock에_대해_happens_before를_보장한다() {
            // JLS 17.4.5: 모니터의 unlock은 같은 모니터에 대한
            // 모든 후속 lock에 대해 happens-before 관계를 형성한다.
            assertTrue(hb.doesSynchronizedBlockHappenBefore());
        }

        @Test
        void synchronized는_상호_배제와_가시성을_모두_제공한다() {
            // synchronized의 두 가지 보장:
            // 1. 상호 배제(mutual exclusion): 한 번에 하나의 스레드만 진입
            // 2. 가시성(visibility): unlock 시 변경 사항 플러시, lock 시 최신 값 로드
            // happens-before는 이 중 '가시성' 보장의 형식적 표현이다.
            assertTrue(hb.doesSynchronizedBlockHappenBefore(),
                    "synchronized는 상호 배제뿐 아니라 happens-before(가시성)도 보장한다");
        }
    }

    // ── Thread.start() ─────────────────────────────────────────────

    @Nested
    @DisplayName("Thread.start() 규칙")
    class ThreadStartRule {

        @Test
        void Thread_start_호출은_run_시작에_대해_happens_before를_보장한다() {
            // JLS 17.4.5: Thread.start() 호출은
            // 시작된 스레드의 모든 액션에 대해 happens-before 관계를 형성한다.
            assertTrue(hb.doesThreadStartHappenBefore());
        }

        @Test
        void start_전에_설정한_값은_새_스레드에서_반드시_보인다() {
            // 예시:
            //   data = 42;              // (1) 메인 스레드에서 값 설정
            //   thread.start();         // (2) start() 호출
            //   // thread의 run():
            //   // assert data == 42;   // (3) 반드시 42가 보인다
            //
            // (1) happens-before (2), (2) happens-before (3)
            // 따라서 transitivity에 의해 (1) happens-before (3)
            assertTrue(hb.doesThreadStartHappenBefore(),
                    "start() 전의 모든 쓰기는 새 스레드의 run()에서 보인다");
        }
    }

    // ── Thread.join() ──────────────────────────────────────────────

    @Nested
    @DisplayName("Thread.join() 규칙")
    class ThreadJoinRule {

        @Test
        void Thread_join_반환_후_해당_스레드의_작업은_happens_before가_성립한다() {
            // JLS 17.4.5: 스레드의 모든 액션은
            // 그 스레드의 join()이 반환되기 전에 happens-before 관계를 형성한다.
            assertTrue(hb.doesThreadJoinHappenBefore());
        }

        @Test
        void join_후에는_종료된_스레드의_모든_쓰기가_보인다() {
            // 예시:
            //   // thread의 run():
            //   // result = compute();   // (1) 워커 스레드에서 결과 저장
            //   thread.join();           // (2) join() 반환
            //   // assert result != null; // (3) 반드시 결과가 보인다
            //
            // (1) happens-before (2), (2) happens-before (3)
            assertTrue(hb.doesThreadJoinHappenBefore(),
                    "join() 이후에는 종료된 스레드의 모든 쓰기 결과가 가시적이다");
        }
    }

    // ── 이해 확인 ──────────────────────────────────────────────────

    @Nested
    @DisplayName("이해 확인 - happens-before 종합")
    class ComprehensionCheck {

        @Test
        void 네_가지_happens_before_규칙_모두_성립한다() {
            // Java Memory Model은 다양한 happens-before 규칙을 정의한다.
            // 이 규칙들은 멀티스레드 프로그래밍에서 가시성을 보장하는 기반이다.
            assertAll("모든 happens-before 규칙은 true",
                    () -> assertTrue(hb.doesVolatileWriteHappenBefore(),
                            "volatile 쓰기 → 후속 읽기"),
                    () -> assertTrue(hb.doesSynchronizedBlockHappenBefore(),
                            "synchronized unlock → 후속 lock"),
                    () -> assertTrue(hb.doesThreadStartHappenBefore(),
                            "Thread.start() → run() 시작"),
                    () -> assertTrue(hb.doesThreadJoinHappenBefore(),
                            "스레드 종료 → Thread.join() 반환")
            );
        }

        @Test
        void happens_before가_없으면_가시성이_보장되지_않는다() {
            // happens-before 관계가 없는 경우:
            // - 일반(non-volatile) 변수를 동기화 없이 공유
            // - 한 스레드의 쓰기가 다른 스레드에서 영원히 안 보일 수 있음
            // → 이것이 happens-before 규칙을 이해해야 하는 이유
            //
            // 이 테스트는 위 네 가지 규칙이 모두 true인지 다시 확인하여,
            // 올바른 동기화 메커니즘을 사용하면 가시성이 보장됨을 강조한다.
            assertTrue(hb.doesVolatileWriteHappenBefore(),
                    "동기화 메커니즘 없이는 다른 스레드의 쓰기가 보이지 않을 수 있다");
            assertTrue(hb.doesSynchronizedBlockHappenBefore(),
                    "synchronized는 가시성을 보장하는 가장 강력한 메커니즘이다");
        }
    }
}
