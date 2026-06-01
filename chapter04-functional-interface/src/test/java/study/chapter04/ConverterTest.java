package study.chapter04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Converter")
class ConverterTest {

    // ── 기본 사용 ───────────────────────────────────────────────

    @Nested
    @DisplayName("기본 사용")
    class BasicUsage {

        @Test
        void 람다로_생성하여_변환() {
            Converter<String, Integer> toLength = String::length;
            assertEquals(5, toLength.convert("hello"));
        }

        @Test
        void 메서드_참조로_생성() {
            Converter<String, Integer> parseInt = Integer::parseInt;
            assertEquals(42, parseInt.convert("42"));
        }

        @Test
        void 다른_타입_변환() {
            Converter<Integer, String> intToString = Object::toString;
            assertEquals("123", intToString.convert(123));
        }
    }

    // ── andThen ─────────────────────────────────────────────────

    @Nested
    @DisplayName("andThen")
    class AndThen {

        @Test
        void 두_컨버터를_체이닝한다() {
            Converter<String, Integer> toLength = String::length;
            Converter<Integer, Boolean> isEven = n -> n % 2 == 0;

            Converter<String, Boolean> isLengthEven = toLength.andThen(isEven);

            assertTrue(isLengthEven.convert("hi"));
            assertFalse(isLengthEven.convert("hey"));
        }

        @Test
        void 세_단계_체이닝() {
            Converter<String, String> trim = String::trim;
            Converter<String, String> upper = String::toUpperCase;
            Converter<String, Integer> length = String::length;

            Converter<String, Integer> pipeline = trim.andThen(upper).andThen(length);

            assertEquals(5, pipeline.convert("  hello  "));
        }

        @Test
        void 중간값이_올바르게_전달된다() {
            Converter<Integer, String> intToString = Object::toString;
            Converter<String, Integer> stringLength = String::length;

            Converter<Integer, Integer> digitCount = intToString.andThen(stringLength);

            assertEquals(1, digitCount.convert(5));
            assertEquals(3, digitCount.convert(100));
            assertEquals(4, digitCount.convert(1000));
        }
    }

    // ── identity ────────────────────────────────────────────────

    @Nested
    @DisplayName("identity")
    class Identity {

        @Test
        void 문자열을_그대로_반환() {
            Converter<String, String> id = Converter.identity();
            assertEquals("hello", id.convert("hello"));
        }

        @Test
        void 정수를_그대로_반환() {
            Converter<Integer, Integer> id = Converter.identity();
            assertEquals(42, id.convert(42));
        }

        @Test
        void andThen과_조합하면_원래_컨버터와_동일() {
            Converter<String, String> id = Converter.identity();
            Converter<String, Integer> toLength = String::length;

            Converter<String, Integer> combined = id.andThen(toLength);
            assertEquals(5, combined.convert("hello"));
        }
    }
}
