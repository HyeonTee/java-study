package study.chapter04;

/**
 * 커스텀 함수형 인터페이스 연습.
 *
 * <p>
 * {@code T} 타입 입력을 {@code R} 타입 출력으로 변환한다.
 * 표준 {@code Function<T, R>}과 비슷하지만, 직접 만들어보며
 * default/static 메서드 합성 패턴을 이해하는 것이 목적이다.
 *
 * @param <T> 입력 타입
 * @param <R> 출력 타입
 */
@FunctionalInterface
public interface Converter<T, R> {

    R convert(T input);

    /**
     * 이 Converter의 결과를 after에 넘겨 연쇄 변환하는 새 Converter를 반환한다.
     *
     * <p>예: intToString.andThen(stringToDouble) → int → String → Double
     */
    default <V> Converter<T, V> andThen(Converter<R, V> after) {
        throw new UnsupportedOperationException(
                "TODO: input -> after.convert(this.convert(input))");
    }

    /**
     * 입력을 그대로 반환하는 항등(identity) Converter를 반환한다.
     */
    static <T> Converter<T, T> identity() {
        throw new UnsupportedOperationException("TODO: input -> input");
    }
}
