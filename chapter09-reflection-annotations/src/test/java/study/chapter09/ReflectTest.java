package study.chapter09;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Reflect — 리플렉션 기초와 예외 모델")
class ReflectTest {

    @Nested
    @DisplayName("load / newInstance")
    class LoadAndCreate {

        @Test
        void FQCN으로_클래스를_로드한다() {
            assertEquals(String.class, Reflect.load("java.lang.String"));
        }

        @Test
        void 없는_클래스는_ReflectionException() {
            assertThrows(ReflectionException.class, () -> Reflect.load("no.such.Klass"));
        }

        @Test
        void 기본_생성자로_인스턴스를_만든다() {
            Calculator c = Reflect.newInstance(Calculator.class);
            assertEquals(7, Reflect.getField(c, "hidden"));
        }
    }

    @Nested
    @DisplayName("invoke")
    class Invoke {

        @Test
        void public_메서드를_호출한다() {
            Calculator c = new Calculator();
            Object r = Reflect.invoke(c, "add", new Class<?>[]{int.class, int.class}, new Object[]{3, 4});
            assertEquals(7, r);
        }

        @Test
        @DisplayName("대상 메서드가 던진 예외는 InvocationTargetException이 아니라 원래 타입으로 드러나야 한다")
        void 대상_예외는_원래_타입으로() {
            Calculator c = new Calculator();
            // divide(1, 0) → ArithmeticException. Method.invoke는 이를 InvocationTargetException으로
            // 감싸므로, 학습자가 getCause()를 풀어 다시 던져야 이 단언이 통과한다.
            assertThrows(ArithmeticException.class,
                    () -> Reflect.invoke(c, "divide", new Class<?>[]{int.class, int.class}, new Object[]{1, 0}));
        }

        @Test
        void 없는_메서드는_ReflectionException() {
            Calculator c = new Calculator();
            assertThrows(ReflectionException.class,
                    () -> Reflect.invoke(c, "nope", new Class<?>[]{}, new Object[]{}));
        }
    }

    @Nested
    @DisplayName("field get / set (private 포함)")
    class Fields {

        @Test
        void private_필드를_읽는다() {
            assertEquals(7, Reflect.getField(new Calculator(), "hidden"));
        }

        @Test
        void private_필드에_쓴다() {
            Calculator c = new Calculator();
            Reflect.setField(c, "hidden", 99);
            assertEquals(99, Reflect.getField(c, "hidden"));
        }
    }
}
