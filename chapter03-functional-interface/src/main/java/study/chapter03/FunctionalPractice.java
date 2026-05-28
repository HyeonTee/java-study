package study.chapter03;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
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
        throw new UnsupportedOperationException("TODO: Predicate의 반전 메서드를 활용하라");
    }

    // ── 팩토리 — 고차 함수 (Higher-Order Function) ──────────────

    /**
     * factor배 곱셈 함수를 반환한다.
     *
     * <p>예: createMultiplier(3).apply(5) → 15
     */
    public static Function<Integer, Integer> createMultiplier(int factor) {
        throw new UnsupportedOperationException("TODO: factor를 캡처하는 람다를 반환하라");
    }

    /**
     * min 이상 max 이하인지 검사하는 Predicate를 반환한다.
     *
     * <p>예: createRangeChecker(1, 10).test(5) → true
     */
    public static Predicate<Integer> createRangeChecker(int min, int max) {
        throw new UnsupportedOperationException("TODO: min, max를 캡처하는 범위 판별 람다를 반환하라");
    }

    /**
     * prefix와 suffix를 붙이는 UnaryOperator를 반환한다.
     *
     * <p>예: createFormatter("[", "]").apply("hello") → "[hello]"
     */
    public static UnaryOperator<String> createFormatter(String prefix, String suffix) {
        throw new UnsupportedOperationException("TODO: prefix, suffix를 캡처하는 문자열 변환 람다를 반환하라");
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
                "TODO: 조건 충족 시 operator 적용, 아니면 원본 유지하는 람다를 반환하라");
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

    // ── 2-arity / Consumer / 지연 평가 ──────────────────────────

    /**
     * items를 identity부터 시작해 combiner로 왼쪽부터 누적(fold)한다.
     * 빈 리스트면 identity를 그대로 반환한다.
     *
     * <p>예: reduce([1, 2, 3, 4], 0, Integer::sum) → 10
     *
     * <p>입력 2개를 하나로 합치는 {@link BinaryOperator}를 처음 다루는 문제다.
     * Chapter 04의 {@code Stream.reduce}를 손으로 직접 구현해보는 셈이다.
     */
    public static <T> T reduce(List<T> items, T identity, BinaryOperator<T> combiner) {
        throw new UnsupportedOperationException(
                "TODO: identity에서 시작해 각 원소를 combiner로 누적하라");
    }

    /**
     * 여러 Consumer를 하나로 합쳐, 같은 입력에 대해 순서대로 모두 실행하는 Consumer를 반환한다.
     * 빈 리스트면 아무 일도 하지 않는 Consumer를 반환한다.
     *
     * <p>Function 합성(앞 함수의 결과를 뒤 함수에 <em>파이프</em>)과 달리,
     * Consumer 합성은 <em>같은 입력</em>에 부수효과를 차례로 적용한다.
     *
     * <p>힌트: {@code Consumer.andThen}을 활용할 수 있다.
     */
    public static <T> Consumer<T> chainConsumers(List<Consumer<T>> consumers) {
        throw new UnsupportedOperationException(
                "TODO: 모든 consumer를 순서대로 같은 입력에 적용하는 Consumer를 반환하라");
    }

    /**
     * value가 null이 아니면 그대로 반환하고, null이면 defaultSupplier를 호출해 그 결과를 반환한다.
     *
     * <p><strong>핵심</strong>: value가 null이 아닐 때는 defaultSupplier를 <em>절대 호출하지 않아야</em> 한다.
     * Supplier를 받는 이유가 바로 이 <strong>지연 평가(lazy evaluation)</strong> 때문이다 —
     * 기본값 계산이 비싸더라도 필요할 때만 수행한다.
     *
     * <p>{@code Optional.orElseGet}과 동일한 패턴이다 ({@code orElse}가 아니라 {@code orElseGet}).
     */
    public static <T> T lazyOrElse(T value, Supplier<T> defaultSupplier) {
        throw new UnsupportedOperationException(
                "TODO: value가 있으면 그대로, 없으면 그때서야 defaultSupplier.get()을 호출하라");
    }
}
