package study.chapter04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FunctionalPractice")
class FunctionalPracticeTest {

    // ── chainFunctions ──────────────────────────────────────────

    @Nested
    @DisplayName("chainFunctions")
    class ChainFunctions {

        @Test
        void 함수_하나면_그_함수만_적용() {
            List<Function<String, String>> fns = List.of(String::toUpperCase);
            assertEquals("HELLO", FunctionalPractice.chainFunctions(fns, "hello"));
        }

        @Test
        void 여러_함수를_순서대로_적용() {
            List<Function<String, String>> fns = List.of(String::trim, String::toUpperCase);
            assertEquals("HELLO", FunctionalPractice.chainFunctions(fns, "  hello  "));
        }

        @Test
        void 빈_리스트면_원본_반환() {
            assertEquals("hello", FunctionalPractice.chainFunctions(List.of(), "hello"));
        }

        @Test
        void 세_개_이상_체이닝() {
            List<Function<String, String>> fns = List.of(
                    String::trim,
                    String::toUpperCase,
                    s -> s + "!");
            assertEquals("HI!", FunctionalPractice.chainFunctions(fns, "  hi  "));
        }
    }

    // ── allMatch ────────────────────────────────────────────────

    @Nested
    @DisplayName("allMatch")
    class AllMatch {

        @Test
        void 모두_만족하면_true() {
            List<Predicate<Integer>> preds = List.of(n -> n > 0, n -> n < 100, n -> n % 2 == 0);
            assertTrue(FunctionalPractice.allMatch(preds, 42));
        }

        @Test
        void 하나라도_불만족이면_false() {
            List<Predicate<Integer>> preds = List.of(n -> n > 0, n -> n < 100, n -> n % 2 == 0);
            assertFalse(FunctionalPractice.allMatch(preds, 43));
        }

        @Test
        void 빈_리스트면_true() {
            assertTrue(FunctionalPractice.allMatch(List.of(), "anything"));
        }

        @Test
        void predicate_하나에_실패() {
            List<Predicate<String>> preds = List.of(s -> s.length() > 5);
            assertFalse(FunctionalPractice.allMatch(preds, "hi"));
        }
    }

    // ── anyMatch ────────────────────────────────────────────────

    @Nested
    @DisplayName("anyMatch")
    class AnyMatch {

        @Test
        void 하나라도_만족하면_true() {
            List<Predicate<Integer>> preds = List.of(n -> n > 100, n -> n < 0, n -> n == 42);
            assertTrue(FunctionalPractice.anyMatch(preds, 42));
        }

        @Test
        void 모두_불만족이면_false() {
            List<Predicate<Integer>> preds = List.of(n -> n > 100, n -> n < 0);
            assertFalse(FunctionalPractice.anyMatch(preds, 42));
        }

        @Test
        void 빈_리스트면_false() {
            assertFalse(FunctionalPractice.anyMatch(List.of(), "anything"));
        }
    }

    // ── negatePredicate ─────────────────────────────────────────

    @Nested
    @DisplayName("negatePredicate")
    class NegatePredicate {

        @Test
        void true를_false로() {
            Predicate<Integer> positive = n -> n > 0;
            Predicate<Integer> notPositive = FunctionalPractice.negatePredicate(positive);
            assertFalse(notPositive.test(5));
        }

        @Test
        void false를_true로() {
            Predicate<Integer> positive = n -> n > 0;
            Predicate<Integer> notPositive = FunctionalPractice.negatePredicate(positive);
            assertTrue(notPositive.test(-3));
        }

        @Test
        void 경계값() {
            Predicate<Integer> positive = n -> n > 0;
            Predicate<Integer> notPositive = FunctionalPractice.negatePredicate(positive);
            assertTrue(notPositive.test(0));
        }
    }

    // ── createMultiplier ────────────────────────────────────────

    @Nested
    @DisplayName("createMultiplier")
    class CreateMultiplier {

        @Test
        void 삼배_곱셈기() {
            Function<Integer, Integer> triple = FunctionalPractice.createMultiplier(3);
            assertEquals(15, triple.apply(5));
            assertEquals(0, triple.apply(0));
        }

        @Test
        void 영배_곱셈기() {
            Function<Integer, Integer> zero = FunctionalPractice.createMultiplier(0);
            assertEquals(0, zero.apply(999));
        }

        @Test
        void 음수배_곱셈기() {
            Function<Integer, Integer> negate = FunctionalPractice.createMultiplier(-1);
            assertEquals(-7, negate.apply(7));
            assertEquals(3, negate.apply(-3));
        }

        @Test
        void 반환된_함수는_재사용_가능() {
            Function<Integer, Integer> doubler = FunctionalPractice.createMultiplier(2);
            assertEquals(10, doubler.apply(5));
            assertEquals(20, doubler.apply(10));
        }
    }

    // ── createRangeChecker ──────────────────────────────────────

    @Nested
    @DisplayName("createRangeChecker")
    class CreateRangeChecker {

        @Test
        void 범위_안이면_true() {
            Predicate<Integer> inRange = FunctionalPractice.createRangeChecker(1, 10);
            assertTrue(inRange.test(5));
        }

        @Test
        void 경계값_포함() {
            Predicate<Integer> inRange = FunctionalPractice.createRangeChecker(1, 10);
            assertTrue(inRange.test(1));
            assertTrue(inRange.test(10));
        }

        @Test
        void 범위_밖이면_false() {
            Predicate<Integer> inRange = FunctionalPractice.createRangeChecker(1, 10);
            assertFalse(inRange.test(0));
            assertFalse(inRange.test(11));
        }

        @Test
        void 음수_범위() {
            Predicate<Integer> inRange = FunctionalPractice.createRangeChecker(-5, -1);
            assertTrue(inRange.test(-3));
            assertFalse(inRange.test(0));
        }
    }

    // ── createFormatter ─────────────────────────────────────────

    @Nested
    @DisplayName("createFormatter")
    class CreateFormatter {

        @Test
        void prefix와_suffix를_붙인다() {
            UnaryOperator<String> fmt = FunctionalPractice.createFormatter("[", "]");
            assertEquals("[hello]", fmt.apply("hello"));
        }

        @Test
        void 빈_prefix_suffix() {
            UnaryOperator<String> fmt = FunctionalPractice.createFormatter("", "");
            assertEquals("hello", fmt.apply("hello"));
        }

        @Test
        void HTML_태그_감싸기() {
            UnaryOperator<String> bold = FunctionalPractice.createFormatter("<b>", "</b>");
            assertEquals("<b>text</b>", bold.apply("text"));
        }
    }

    // ── applyN ──────────────────────────────────────────────────

    @Nested
    @DisplayName("applyN")
    class ApplyN {

        @Test
        void n번_반복_적용() {
            assertEquals(16, FunctionalPractice.applyN(x -> x * 2, 1, 4));
        }

        @Test
        void n이_0이면_initial_그대로() {
            assertEquals(5, FunctionalPractice.applyN(x -> x * 2, 5, 0));
        }

        @Test
        void n이_1이면_한번만() {
            assertEquals(10, FunctionalPractice.applyN(x -> x + 3, 7, 1));
        }

        @Test
        void 누적_덧셈() {
            assertEquals(25, FunctionalPractice.applyN(x -> x + 5, 0, 5));
        }
    }

    // ── conditionalApply ────────────────────────────────────────

    @Nested
    @DisplayName("conditionalApply")
    class ConditionalApply {

        @Test
        void 조건_만족하면_변환() {
            UnaryOperator<String> op = FunctionalPractice.conditionalApply(
                    s -> s.length() > 3, String::toUpperCase);
            assertEquals("HELLO", op.apply("hello"));
        }

        @Test
        void 조건_불만족이면_원본_그대로() {
            UnaryOperator<String> op = FunctionalPractice.conditionalApply(
                    s -> s.length() > 3, String::toUpperCase);
            assertEquals("hi", op.apply("hi"));
        }

        @Test
        void 경계값_정확히_조건과_같을_때() {
            UnaryOperator<Integer> op = FunctionalPractice.conditionalApply(
                    n -> n > 0, n -> n * 10);
            assertEquals(0, op.apply(0));
            assertEquals(10, op.apply(1));
        }
    }

    // ── memoize ─────────────────────────────────────────────────

    @Nested
    @DisplayName("memoize")
    class Memoize {

        @Test
        void 첫_호출에서_계산한다() {
            Supplier<String> memoized = FunctionalPractice.memoize(() -> "computed");
            assertEquals("computed", memoized.get());
        }

        @Test
        void 두_번째_호출부터_캐시를_사용한다() {
            AtomicInteger callCount = new AtomicInteger(0);
            Supplier<String> memoized = FunctionalPractice.memoize(() -> {
                callCount.incrementAndGet();
                return "result";
            });

            assertEquals("result", memoized.get());
            assertEquals("result", memoized.get());
            assertEquals("result", memoized.get());
            assertEquals(1, callCount.get());
        }

        @Test
        void null_결과도_캐시한다() {
            AtomicInteger callCount = new AtomicInteger(0);
            Supplier<String> memoized = FunctionalPractice.memoize(() -> {
                callCount.incrementAndGet();
                return null;
            });

            assertNull(memoized.get());
            assertNull(memoized.get());
            assertEquals(1, callCount.get());
        }
    }
}
