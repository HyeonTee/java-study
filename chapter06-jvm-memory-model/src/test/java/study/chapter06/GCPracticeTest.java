package study.chapter06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GCPractice - Garbage Collection 이해")
class GCPracticeTest {

    private final GCPractice gc = new GCPractice();

    // ── GC 대상 판별 ──────────────────────────────────────────────

    @Nested
    @DisplayName("GC 대상 판별 - 참조 해제")
    class GCEligibility_Unreferenced {

        @Test
        void null_할당된_객체는_GC_대상이다() {
            // Object obj = new Object(); obj = null;
            // → obj가 null이 되면 원래 객체에 대한 참조가 사라지므로 GC 대상
            assertTrue(gc.isEligibleForGC_unreferenced());
        }

        @Test
        void null_할당은_GC_root에서_도달_불가능하게_만든다() {
            // 핵심: GC는 root(스택 변수, static 필드 등)에서 도달 가능한 객체만 살린다.
            // null 할당 → root에서 도달 불가 → GC 대상
            assertTrue(gc.isEligibleForGC_unreferenced(),
                    "참조를 null로 만들면 GC root에서 도달할 수 없으므로 반드시 GC 대상이다");
        }
    }

    @Nested
    @DisplayName("GC 대상 판별 - 참조 재할당")
    class GCEligibility_Reassigned {

        @Test
        void 재할당으로_참조가_끊긴_객체는_GC_대상이다() {
            // Object obj = new Object(); obj = new Object();
            // → 첫 번째 객체는 더 이상 참조되지 않으므로 GC 대상
            assertTrue(gc.isEligibleForGC_reassigned());
        }

        @Test
        void 재할당은_이전_객체의_참조_카운트를_0으로_만든다() {
            // 참조 변수가 새 객체를 가리키면, 이전 객체를 가리키는 참조가 없어진다.
            // (다른 곳에서도 참조하지 않는다고 가정)
            assertTrue(gc.isEligibleForGC_reassigned(),
                    "재할당은 이전 객체에 대한 유일한 참조를 끊으므로 GC 대상이 된다");
        }
    }

    @Nested
    @DisplayName("GC 대상 판별 - 순환 참조")
    class GCEligibility_CircularReference {

        @Test
        void 순환_참조_객체도_GC_대상이다() {
            // A → B, B → A이지만 GC root에서 둘 다 도달 불가능
            // Java는 tracing GC를 사용하므로 순환 참조는 문제가 되지 않는다.
            assertTrue(gc.isEligibleForGC_circularReference());
        }

        @Test
        void Java는_reference_counting이_아닌_tracing_GC를_사용한다() {
            // reference counting 방식이라면 순환 참조 시 카운트가 0이 되지 않아 수거 불가.
            // 하지만 Java는 GC root에서 도달 가능성을 추적(tracing)하므로
            // root에서 도달 불가능한 순환 참조 객체는 정상적으로 GC된다.
            assertTrue(gc.isEligibleForGC_circularReference(),
                    "tracing GC는 root 도달성으로 판단하므로 순환 참조도 수거 가능하다");
        }
    }

    // ── GC 세대 ────────────────────────────────────────────────────

    @Nested
    @DisplayName("GC 세대 - 객체 할당과 승격")
    class GCGeneration_Allocation {

        @Test
        void 새_객체는_Young_Generation에_할당된다() {
            assertEquals(GCGeneration.YOUNG, gc.whichGCGeneration_newObject());
        }

        @Test
        void 오래_살아남은_객체는_Old_Generation으로_승격된다() {
            assertEquals(GCGeneration.OLD, gc.whichGCGeneration_survivedObject());
        }

        @Test
        void 새_객체와_오래된_객체는_서로_다른_세대에_속한다() {
            // Generational Hypothesis: 대부분의 객체는 금방 죽고, 오래 사는 객체는 계속 산다.
            // 따라서 JVM은 객체를 세대별로 분리하여 GC 효율을 높인다.
            assertNotEquals(gc.whichGCGeneration_newObject(),
                    gc.whichGCGeneration_survivedObject(),
                    "세대 가설에 따라 새 객체와 오래된 객체는 다른 세대에서 관리된다");
        }
    }

    // ── 이해 확인 ──────────────────────────────────────────────────

    @Nested
    @DisplayName("이해 확인 - GC 종합")
    class ComprehensionCheck {

        @Test
        void 도달_불가능한_모든_경우는_GC_대상이다() {
            // GC의 핵심: 도달 가능성(reachability)
            // 어떤 방식으로든 GC root에서 도달 불가능한 객체는 GC 대상이다.
            assertAll("도달 불가능 = GC 대상",
                    () -> assertTrue(gc.isEligibleForGC_unreferenced(),
                            "null 할당 → 도달 불가 → GC 대상"),
                    () -> assertTrue(gc.isEligibleForGC_reassigned(),
                            "재할당 → 도달 불가 → GC 대상"),
                    () -> assertTrue(gc.isEligibleForGC_circularReference(),
                            "순환 참조(root에서 도달 불가) → GC 대상")
            );
        }

        @Test
        void GCGeneration_enum은_두_가지_값을_가진다() {
            GCGeneration[] values = GCGeneration.values();
            assertEquals(2, values.length,
                    "Generational GC는 크게 YOUNG과 OLD 두 세대로 나뉜다");
        }

        @Test
        void Young에서_Old로의_승격_순서가_올바르다() {
            // 객체 생명주기: 생성(Young) → Minor GC 생존 → 승격(Old)
            GCGeneration birth = gc.whichGCGeneration_newObject();
            GCGeneration promoted = gc.whichGCGeneration_survivedObject();
            assertEquals(GCGeneration.YOUNG, birth, "객체는 Young에서 태어난다");
            assertEquals(GCGeneration.OLD, promoted, "살아남으면 Old로 승격된다");
        }
    }
}
