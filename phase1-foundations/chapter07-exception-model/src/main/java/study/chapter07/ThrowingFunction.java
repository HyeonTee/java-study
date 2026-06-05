package study.chapter07;

/**
 * 검사 예외(checked exception)를 던질 수 있는 {@link java.util.function.Function} 변형 — 지원 코드.
 *
 * <p>{@link ThrowingSupplier}와 같은 동기다. {@code apply}가 {@code throws Exception}을 허용하므로,
 * {@link Exceptions#unchecked}가 이를 검사 예외 없는 {@link java.util.function.Function}으로 감쌀 수 있다.
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> {

    R apply(T t) throws Exception;
}
