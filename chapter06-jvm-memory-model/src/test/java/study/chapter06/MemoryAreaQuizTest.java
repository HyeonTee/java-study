package study.chapter06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MemoryAreaQuiz - JVM 메모리 영역 이해")
class MemoryAreaQuizTest {

    private final MemoryAreaQuiz quiz = new MemoryAreaQuiz();

    // ── 스택 영역 ──────────────────────────────────────────────────

    @Nested
    @DisplayName("스택(Stack) 영역")
    class StackArea {

        @Test
        void 지역_프리미티브는_스택에_저장된다() {
            assertEquals(MemoryArea.STACK, quiz.whereIsLocalPrimitive());
        }

        @Test
        void 지역_프리미티브가_스택인_이유는_고정_크기이고_메서드_범위이기_때문이다() {
            // 프리미티브 타입(int, double 등)은 크기가 컴파일 타임에 결정되고,
            // 메서드 호출과 생명주기가 같으므로 스택 프레임에 저장된다.
            MemoryArea result = quiz.whereIsLocalPrimitive();
            assertNotEquals(MemoryArea.HEAP, result,
                    "프리미티브는 new로 생성하지 않으므로 힙에 저장되지 않는다");
            assertNotEquals(MemoryArea.METHOD_AREA, result,
                    "프리미티브 지역 변수는 클래스 메타데이터가 아니다");
        }
    }

    // ── 힙 영역 ────────────────────────────────────────────────────

    @Nested
    @DisplayName("힙(Heap) 영역")
    class HeapArea {

        @Test
        void 객체_인스턴스는_힙에_저장된다() {
            assertEquals(MemoryArea.HEAP, quiz.whereIsObjectInstance());
        }

        @Test
        void 객체가_힙인_이유는_동적_크기이고_여러_메서드에서_공유될_수_있기_때문이다() {
            // 객체는 런타임에 크기가 결정되고, 여러 참조 변수가 가리킬 수 있으므로
            // 메서드 종료와 무관하게 생존할 수 있는 힙에 저장된다.
            MemoryArea result = quiz.whereIsObjectInstance();
            assertNotEquals(MemoryArea.STACK, result,
                    "객체는 크기가 동적이므로 고정 크기 스택에 직접 저장되지 않는다");
        }
    }

    // ── 메서드 영역 ────────────────────────────────────────────────

    @Nested
    @DisplayName("메서드(Method) 영역")
    class MethodArea {

        @Test
        void static_필드는_메서드_영역에_저장된다() {
            assertEquals(MemoryArea.METHOD_AREA, quiz.whereIsStaticField());
        }

        @Test
        void 메서드_바이트코드_정보는_메서드_영역에_저장된다() {
            assertEquals(MemoryArea.METHOD_AREA, quiz.whereIsMethodInfo());
        }

        @Test
        void static_필드와_메서드_정보는_같은_영역에_있다() {
            // static 필드와 메서드 바이트코드 모두 클래스 메타데이터의 일부이므로
            // 같은 메모리 영역(Method Area)에 저장된다.
            assertEquals(quiz.whereIsStaticField(), quiz.whereIsMethodInfo(),
                    "static 필드와 메서드 정보는 모두 클래스 메타데이터로서 같은 영역에 속한다");
        }
    }

    // ── 스택 프레임 생명주기 ────────────────────────────────────────

    @Nested
    @DisplayName("스택 프레임 생명주기")
    class StackFrameLifecycle {

        @Test
        void 메서드_리턴_시_스택_프레임이_제거된다() {
            assertEquals("스택 프레임이 제거되고 지역 변수가 소멸한다",
                    quiz.whatHappensWhenMethodReturns());
        }

        @Test
        void 리턴_설명에_스택_프레임_제거가_포함되어야_한다() {
            String result = quiz.whatHappensWhenMethodReturns();
            assertNotNull(result, "null이 아닌 설명 문자열을 반환해야 한다");
            assertTrue(result.contains("스택 프레임"),
                    "메서드 리턴 시 '스택 프레임'에 대한 설명이 있어야 한다");
            assertTrue(result.contains("지역 변수"),
                    "스택 프레임 제거 시 지역 변수의 소멸도 설명해야 한다");
        }
    }

    // ── 이해 확인: 종합 시나리오 ────────────────────────────────────

    @Nested
    @DisplayName("이해 확인 - 종합 시나리오")
    class ComprehensionCheck {

        @Test
        void 세_영역은_모두_서로_다르다() {
            MemoryArea stack = quiz.whereIsLocalPrimitive();
            MemoryArea heap = quiz.whereIsObjectInstance();
            MemoryArea methodArea = quiz.whereIsStaticField();

            assertNotEquals(stack, heap, "스택과 힙은 다른 영역이다");
            assertNotEquals(heap, methodArea, "힙과 메서드 영역은 다른 영역이다");
            assertNotEquals(stack, methodArea, "스택과 메서드 영역은 다른 영역이다");
        }

        @Test
        void 참조_변수_자체는_스택에_저장되고_참조_대상_객체는_힙에_저장된다() {
            // String name = new String("hello");
            // → 'name' 참조 변수 자체는 스택, String 객체 인스턴스는 힙
            // 지역 변수(참조든 프리미티브든)의 저장 위치는 스택이다.
            assertEquals(MemoryArea.STACK, quiz.whereIsLocalPrimitive(),
                    "지역 변수(참조 포함)는 스택에 저장된다");
            assertEquals(MemoryArea.HEAP, quiz.whereIsObjectInstance(),
                    "참조가 가리키는 객체 인스턴스는 힙에 저장된다");
        }

        @Test
        void MemoryArea_enum은_세_가지_값을_가진다() {
            MemoryArea[] values = MemoryArea.values();
            assertEquals(3, values.length,
                    "JVM 메모리는 크게 STACK, HEAP, METHOD_AREA 세 영역으로 나뉜다");
        }
    }
}
