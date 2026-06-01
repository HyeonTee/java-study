package study.chapter04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ThrowingFunctionPractice — 검사 예외 래핑")
class ThrowingFunctionPracticeTest {

    @Nested
    @DisplayName("unchecked")
    class Unchecked {

        @Test
        void 정상_입력은_그대로_변환한다() {
            Function<String, Integer> parse = ThrowingFunctionPractice.unchecked(Integer::parseInt);
            assertEquals(42, parse.apply("42"));
        }

        @Test
        void 검사_예외는_RuntimeException으로_감싸_다시_던진다() {
            ThrowingFunction<String, String> boom = s -> {
                throw new Exception("checked!");
            };
            Function<String, String> wrapped = ThrowingFunctionPractice.unchecked(boom);
            assertThrows(RuntimeException.class, () -> wrapped.apply("x"));
        }

        @Test
        void 감싼_예외의_원인이_보존된다() {
            ThrowingFunction<String, String> boom = s -> {
                throw new IllegalStateException("original");
            };
            Function<String, String> wrapped = ThrowingFunctionPractice.unchecked(boom);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> wrapped.apply("x"));
            assertNotNull(ex.getCause(), "원래 예외가 cause로 보존되어야 한다");
            assertEquals("original", ex.getCause().getMessage());
        }
    }

    @Nested
    @DisplayName("withDefault")
    class WithDefault {

        @Test
        void 정상_입력은_그대로_변환한다() {
            Function<String, Integer> parse = ThrowingFunctionPractice.withDefault(Integer::parseInt, -1);
            assertEquals(42, parse.apply("42"));
        }

        @Test
        void 예외가_나면_기본값을_반환한다() {
            Function<String, Integer> parse = ThrowingFunctionPractice.withDefault(Integer::parseInt, -1);
            assertEquals(-1, parse.apply("not a number"));
        }
    }
}
