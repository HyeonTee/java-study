package study.chapter03;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * 함수형 인터페이스 연습 문제.
 *
 * <p>
 * 표준 함수형 인터페이스의 합성, 고차 함수, 지연 평가 패턴을 직접 구현한다.
 */
public class FunctionalPractice {

    private FunctionalPractice() {
    }

    // ── 합성 (Composition) ──────────────────────────────────────

    /**
     * 함수 리스트를 왼쪽부터 오른쪽으로 순서대로 적용한다.
     * 빈 리스트면 input을 그대로 반환한다.
     *
     * <p>예: functions = [trim, toUpperCase], input = "  hi  " → "HI"
     */
    public static String chainFunctions(List<Function<String, String>> functions, String input) {
        throw new UnsupportedOperationException(
                "TODO: 함수 리스트를 순회하며 andThen 또는 직접 apply 체이닝");
    }

    /**
     * 모든 Predicate가 만족하면 true. 빈 리스트면 true (vacuous truth).
     * Stream 사용 금지 — 반복문과 Predicate API만 사용할 것.
     */
    public static <T> boolean allMatch(List<Predicate<T>> predicates, T value) {
        throw new UnsupportedOperationException(
                "TODO: 모든 predicate.test(value)가 true인지 확인");
    }

    /**
     * Predicate 중 하나라도 만족하면 true. 빈 리스트면 false.
     * Stream 사용 금지 — 반복문과 Predicate API만 사용할 것.
     */
    public static <T> boolean anyMatch(List<Predicate<T>> predicates, T value) {
        throw new UnsupportedOperationException(
                "TODO: 하나라도 predicate.test(value)가 true이면 true");
    }

    /**
     * 주어진 Predicate의 반대를 반환한다.
     */
    public static <T> Predicate<T> negatePredicate(Predicate<T> predicate) {
        throw new UnsupportedOperationException("TODO: Predicate.negate()");
    }

    // ── 팩토리 — 고차 함수 (Higher-Order Function) ──────────────

    /**
     * factor배 곱셈 함수를 반환한다.
     *
     * <p>예: createMultiplier(3).apply(5) → 15
     */
    public static Function<Integer, Integer> createMultiplier(int factor) {
        throw new UnsupportedOperationException("TODO: x -> x * factor");
    }

    /**
     * min 이상 max 이하인지 검사하는 Predicate를 반환한다.
     *
     * <p>예: createRangeChecker(1, 10).test(5) → true
     */
    public static Predicate<Integer> createRangeChecker(int min, int max) {
        throw new UnsupportedOperationException("TODO: x -> x >= min && x <= max");
    }

    /**
     * prefix와 suffix를 붙이는 UnaryOperator를 반환한다.
     *
     * <p>예: createFormatter("[", "]").apply("hello") → "[hello]"
     */
    public static UnaryOperator<String> createFormatter(String prefix, String suffix) {
        throw new UnsupportedOperationException("TODO: s -> prefix + s + suffix");
    }

    // ── 고급 패턴 ──────────────────────────────────────────────

    /**
     * UnaryOperator를 initial 값에 n번 반복 적용한 결과를 반환한다.
     * n이 0이면 initial을 그대로 반환한다.
     *
     * <p>예: applyN(x -> x * 2, 3, 4) → 3 → 6 → 12 → 24 → 48  (4번)
     */
    public static int applyN(UnaryOperator<Integer> operator, int initial, int n) {
        throw new UnsupportedOperationException("TODO: n번 반복 apply");
    }

    /**
     * 조건을 만족하면 operator를 적용하고, 아니면 원본을 그대로 반환하는 UnaryOperator를 만든다.
     *
     * <p>예: conditionalApply(s -> s.length() > 3, String::toUpperCase)
     *        .apply("hello") → "HELLO"
     *        .apply("hi") → "hi"
     */
    public static <T> UnaryOperator<T> conditionalApply(Predicate<T> condition, UnaryOperator<T> operator) {
        throw new UnsupportedOperationException(
                "TODO: t -> condition.test(t) ? operator.apply(t) : t");
    }

    /**
     * Supplier를 감싸서, 첫 호출에서만 실제 계산하고 이후에는 캐시된 값을 반환하는 Supplier를 만든다.
     *
     * <p>예:
     * <pre>
     * Supplier<String> heavy = () -> expensiveComputation();
     * Supplier<String> cached = memoize(heavy);
     * cached.get();  // 계산 실행
     * cached.get();  // 캐시 반환 (계산 안 함)
     * </pre>
     */
    public static <T> Supplier<T> memoize(Supplier<T> supplier) {
        throw new UnsupportedOperationException(
                "TODO: 첫 get()에서 supplier 호출 후 결과 저장, 이후 저장값 반환");
    }
}
