package study.chapter03;

import java.util.function.Function;

/**
 * 검사 예외를 던지는 함수를 표준 {@link Function}으로 변환하는 어댑터 연습.
 *
 * <p>핵심 패턴은 <strong>wrap-and-rethrow</strong>(감싸서 다시 던지기)다.
 * 검사 예외를 비검사 예외로 바꿔 표준 함수형 인터페이스 자리에 끼워 넣는다.
 * 이 패턴은 Phase 2의 {@code CompletableFuture} 예외 전파(ch09)에서 다시 만난다.
 */
public class ThrowingFunctionPractice {

    private ThrowingFunctionPractice() {
    }

    /**
     * {@link ThrowingFunction}을 표준 {@link Function}으로 변환한다.
     * 내부에서 검사 예외가 발생하면 {@link RuntimeException}으로 감싸 다시 던진다.
     *
     * <p>예:
     * <pre>{@code
     * Function<String, Integer> parse = unchecked(Integer::parseInt);
     * parse.apply("42");   // 42
     * parse.apply("oops"); // RuntimeException (원인은 NumberFormatException)
     * }</pre>
     *
     * <p>힌트: 람다 안에서 try/catch로 {@code f.apply(t)}를 감싸고,
     * 예외를 잡으면 {@code new RuntimeException(e)}로 다시 던진다.
     */
    public static <T, R> Function<T, R> unchecked(ThrowingFunction<T, R> f) {
        throw new UnsupportedOperationException(
                "TODO: f.apply를 try/catch로 감싸 검사 예외를 RuntimeException으로 다시 던지는 Function을 반환하라");
    }

    /**
     * {@link ThrowingFunction}을 표준 {@link Function}으로 변환하되,
     * 예외가 발생하면 다시 던지는 대신 {@code defaultValue}를 반환한다.
     *
     * <p>예:
     * <pre>{@code
     * Function<String, Integer> parse = withDefault(Integer::parseInt, -1);
     * parse.apply("42");   // 42
     * parse.apply("oops"); // -1
     * }</pre>
     *
     * <p>힌트: try에서 {@code f.apply(t)}를 반환하고, catch에서 {@code defaultValue}를 반환한다.
     */
    public static <T, R> Function<T, R> withDefault(ThrowingFunction<T, R> f, R defaultValue) {
        throw new UnsupportedOperationException(
                "TODO: 예외 발생 시 defaultValue를 반환하는 Function을 반환하라");
    }
}
