package study.chapter05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OptionalPractice")
class OptionalPracticeTest {

    @Nested
    @DisplayName("findFirstMatch")
    class FindFirstMatch {

        @Test
        void 조건에_맞는_첫_번째를_반환한다() {
            Optional<String> result = OptionalPractice.findFirstMatch(
                    List.of("apple", "banana", "avocado"), s -> s.startsWith("b"));
            assertEquals(Optional.of("banana"), result);
        }

        @Test
        void 조건에_맞는_것이_없으면_empty() {
            Optional<String> result = OptionalPractice.findFirstMatch(
                    List.of("apple", "banana"), s -> s.startsWith("z"));
            assertEquals(Optional.empty(), result);
        }

        @Test
        void 빈_리스트면_empty() {
            assertEquals(Optional.empty(),
                    OptionalPractice.findFirstMatch(List.of(), s -> true));
        }

        @Test
        void 여러_개_매칭되면_첫_번째만() {
            Optional<String> result = OptionalPractice.findFirstMatch(
                    List.of("alpha", "ace", "beta"), s -> s.startsWith("a"));
            assertEquals(Optional.of("alpha"), result);
        }
    }

    @Nested
    @DisplayName("getOrDefault")
    class GetOrDefault {

        @Test
        void 값이_있으면_그_값() {
            assertEquals("hello", OptionalPractice.getOrDefault(Optional.of("hello"), "default"));
        }

        @Test
        void 값이_없으면_default() {
            assertEquals("default", OptionalPractice.getOrDefault(Optional.empty(), "default"));
        }
    }

    @Nested
    @DisplayName("getOrCompute")
    class GetOrCompute {

        @Test
        void 값이_있으면_supplier를_호출하지_않는다() {
            AtomicBoolean called = new AtomicBoolean(false);
            String result = OptionalPractice.getOrCompute(Optional.of("hello"), () -> {
                called.set(true);
                return "computed";
            });
            assertEquals("hello", result);
            assertFalse(called.get());
        }

        @Test
        void 값이_없으면_supplier를_호출한다() {
            assertEquals("computed",
                    OptionalPractice.getOrCompute(Optional.empty(), () -> "computed"));
        }
    }

    @Nested
    @DisplayName("getOrThrow")
    class GetOrThrow {

        @Test
        void 값이_있으면_반환() {
            assertEquals("hello", OptionalPractice.getOrThrow(Optional.of("hello")));
        }

        @Test
        void 값이_없으면_IllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> OptionalPractice.getOrThrow(Optional.empty()));
        }
    }

    @Nested
    @DisplayName("safeParseInt")
    class SafeParseInt {

        @Test
        void 유효한_정수_문자열() {
            assertEquals(Optional.of(42), OptionalPractice.safeParseInt("42"));
        }

        @Test
        void 음수도_파싱() {
            assertEquals(Optional.of(-7), OptionalPractice.safeParseInt("-7"));
        }

        @Test
        void 유효하지_않은_문자열이면_empty() {
            assertEquals(Optional.empty(), OptionalPractice.safeParseInt("abc"));
        }

        @Test
        void null이면_empty() {
            assertEquals(Optional.empty(), OptionalPractice.safeParseInt(null));
        }

        @Test
        void 빈_문자열이면_empty() {
            assertEquals(Optional.empty(), OptionalPractice.safeParseInt(""));
        }
    }

    @Nested
    @DisplayName("toUpperIfPresent")
    class ToUpperIfPresent {

        @Test
        void 값이_있으면_대문자로() {
            assertEquals(Optional.of("HELLO"), OptionalPractice.toUpperIfPresent(Optional.of("hello")));
        }

        @Test
        void 비어있으면_empty() {
            assertEquals(Optional.empty(), OptionalPractice.toUpperIfPresent(Optional.empty()));
        }
    }

    @Nested
    @DisplayName("firstChar")
    class FirstChar {

        @Test
        void 첫_글자를_반환한다() {
            assertEquals(Optional.of('h'), OptionalPractice.firstChar(Optional.of("hello")));
        }

        @Test
        void 빈_문자열이면_empty() {
            assertEquals(Optional.empty(), OptionalPractice.firstChar(Optional.of("")));
        }

        @Test
        void Optional이_비어있으면_empty() {
            assertEquals(Optional.empty(), OptionalPractice.firstChar(Optional.empty()));
        }
    }

    @Nested
    @DisplayName("filterByLength")
    class FilterByLength {

        @Test
        void 길이_이상이면_유지() {
            assertEquals(Optional.of("hello"),
                    OptionalPractice.filterByLength(Optional.of("hello"), 3));
        }

        @Test
        void 길이_미만이면_empty() {
            assertEquals(Optional.empty(),
                    OptionalPractice.filterByLength(Optional.of("hi"), 3));
        }

        @Test
        void 정확히_같은_길이면_유지() {
            assertEquals(Optional.of("abc"),
                    OptionalPractice.filterByLength(Optional.of("abc"), 3));
        }

        @Test
        void 비어있으면_empty() {
            assertEquals(Optional.empty(),
                    OptionalPractice.filterByLength(Optional.empty(), 1));
        }
    }
}
