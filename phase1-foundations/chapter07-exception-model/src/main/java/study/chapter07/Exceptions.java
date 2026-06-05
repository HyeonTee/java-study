package study.chapter07;

import java.util.function.Function;

/**
 * 예외 모델을 다루는 정적 유틸리티 — "검사/비검사 경계 옮기기"와 "원인 체인 탐색".
 *
 * <p>{@link #rootCause}는 {@code getCause()} 체인을 끝까지 따라가 근본 원인을 찾는다.
 * {@link #uncheck}/{@link #unchecked}는 검사 예외를 던지는 람다를 표준 함수형 인터페이스로 끌어올려,
 * 호출 지점에서 try-catch를 강제당하지 않게 한다(검사 예외는 비검사로 감싸 cause를 보존).
 */
public final class Exceptions {

    private Exceptions() {
    }

    /**
     * {@code t}에서 시작해 {@code getCause()} 체인을 더 깊은 원인이 없을 때까지 따라가 마지막 예외를 반환한다.
     *
     * <p>힌트: 현재 노드의 {@code getCause()}가 {@code null}이거나 자기 자신({@code cause == current})을
     * 가리키면 멈춰라 — cause가 자기 자신을 가리키는 <strong>사이클</strong>에서 무한 루프를 막아야 한다.
     */
    public static Throwable rootCause(Throwable t) {
        throw new UnsupportedOperationException(
                "TODO: getCause 체인을 끝(또는 자기참조 사이클)까지 따라가 마지막 Throwable을 반환하라");
    }

    /**
     * {@code supplier}를 실행해 결과를 반환한다. 예외 처리 규칙은 다음과 같다.
     *
     * <ul>
     *   <li>{@link RuntimeException}/{@link Error}는 그대로 전파한다(감싸지 않는다).
     *   <li>검사 예외는 {@link RuntimeException}으로 감싸 던지되 — <strong>cause로 보존</strong>한다.
     * </ul>
     *
     * <p>힌트: try에서 {@code supplier.get()}을 반환하고, {@code catch (RuntimeException | Error e)}는
     * {@code throw e;}, 그 밖의 {@code catch (Exception e)}는 {@code throw new RuntimeException(e);}.
     */
    public static <T> T uncheck(ThrowingSupplier<T> supplier) {
        throw new UnsupportedOperationException(
                "TODO: supplier 실행; 비검사/Error는 그대로, 검사 예외는 RuntimeException(cause)로 감싸 던져라");
    }

    /**
     * {@code f}를 {@link #uncheck}와 같은 규칙으로 감싼 {@link Function}을 반환한다.
     *
     * <p>힌트: {@code t -> { ... }} 람다를 반환하되, 본문에서 {@code f.apply(t)}에 동일한 try-catch 규칙을
     * 적용하라(검사 예외 → RuntimeException(cause), 비검사/Error는 그대로).
     */
    public static <T, R> Function<T, R> unchecked(ThrowingFunction<T, R> f) {
        throw new UnsupportedOperationException(
                "TODO: f.apply에 uncheck와 같은 규칙을 적용하는 Function<T, R>을 반환하라");
    }
}
