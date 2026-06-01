package study.chapter12;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 표준 {@link java.util.concurrent.CompletableFuture}로 비동기 파이프라인을 <strong>조립</strong>하고
 * 예외를 전파·복구하는 연습. {@link MyPromise}에서 손으로 만든 콜백·합성이 표준 API에선 어떤 메서드인지
 * 1:1로 대응시킨다(thenApply≈map, thenCompose≈flatMap, exceptionally≈orElse).
 *
 * <p>모든 메서드는 {@code CompletableFuture}를 반환한다(블로킹하지 않음). 결과는 호출자가 {@code join()}으로
 * 회수한다.
 *
 * <p><strong>예외 래핑 함정</strong>: 비동기 단계가 던진 예외는 future를 예외 완료시키고 하류 변환은
 * <strong>건너뛴다</strong>. {@code join()}은 그 예외를 {@link CompletionException}(비검사)으로, {@code get()}은
 * {@code ExecutionException}(검사)으로 <strong>감싼다</strong> — 원래 예외는 {@code getCause()}로 꺼낸다.
 */
public final class CompletableFuturePractice {

    private CompletableFuturePractice() {
    }

    /**
     * {@code supplier}를 {@code executor}에서 비동기로 실행해 결과를 담는 future를 반환한다.
     * 힌트: {@code CompletableFuture.supplyAsync(supplier, executor)}.
     */
    public static <T> CompletableFuture<T> runAsync(Supplier<T> supplier, Executor executor) {
        throw new UnsupportedOperationException("TODO: supplyAsync로 비동기 시작");
    }

    /**
     * 입력 future의 값에 {@code fn}을 적용한 결과 future를 반환한다(map). 힌트: {@code thenApply}.
     */
    public static <T, U> CompletableFuture<U> mapResult(
            CompletableFuture<T> source, Function<? super T, ? extends U> fn) {
        throw new UnsupportedOperationException("TODO: thenApply로 값 변환");
    }

    /**
     * 입력 future의 값을 <strong>또 다른 future</strong>로 변환하고 평탄화한다(flatMap, 중첩 제거).
     * 힌트: {@code thenCompose}. {@code thenApply}로 하면 {@code CF<CF<U>>}로 중첩됨에 주의.
     */
    public static <T, U> CompletableFuture<U> flatMapResult(
            CompletableFuture<T> source, Function<? super T, ? extends CompletableFuture<U>> fn) {
        throw new UnsupportedOperationException("TODO: thenCompose로 평탄화");
    }

    /**
     * 두 future가 모두 완료되면 두 값을 {@code combiner}로 합친 future를 반환한다(zip). 힌트: {@code thenCombine}.
     */
    public static <A, B, R> CompletableFuture<R> combine(
            CompletableFuture<A> a, CompletableFuture<B> b,
            BiFunction<? super A, ? super B, ? extends R> combiner) {
        throw new UnsupportedOperationException("TODO: a.thenCombine(b, combiner)");
    }

    /**
     * 여러 future가 <strong>모두</strong> 완료되면 결과들을 입력 순서대로 리스트에 담아 완료되는 future.
     *
     * <p>함정: {@code CompletableFuture.allOf(...)}는 {@code CompletableFuture<Void>}라 결과를 직접 주지 않는다 —
     * 완료만 알린다. 힌트: {@code allOf(...)}로 전부 기다린 뒤 {@code thenApply} 안에서 각 future의 {@code join()}
     * (이미 완료라 즉시 반환)으로 결과를 순서대로 모은다.
     */
    public static <T> CompletableFuture<List<T>> allOfResults(List<CompletableFuture<T>> futures) {
        throw new UnsupportedOperationException(
                "TODO: allOf로 전부 대기 → thenApply 안에서 각 future.join()을 순서대로 수집");
    }

    /**
     * future가 예외로 완료되면 {@code fallback} 값으로 복구하고, 성공이면 원래 값을 그대로 통과시킨다.
     * 힌트: {@code exceptionally(ex -> fallback)}. 정상 완료면 호출되지 않는다.
     */
    public static <T> CompletableFuture<T> recover(CompletableFuture<T> source, T fallback) {
        throw new UnsupportedOperationException("TODO: exceptionally로 fallback 복구");
    }

    /**
     * 성공·실패 양쪽을 하나의 {@code handler}로 처리해 항상 정상 값을 내는 future를 반환한다. 성공이면
     * (값, null), 실패면 (null, 예외)로 <strong>항상</strong> 호출된다(= {@code handle}). {@code exceptionally}와
     * 달리 성공도 변환할 수 있다.
     */
    public static <T, U> CompletableFuture<U> handleResult(
            CompletableFuture<T> source, BiFunction<? super T, Throwable, ? extends U> handler) {
        throw new UnsupportedOperationException("TODO: handle로 성공/실패 통합 처리");
    }

    /**
     * future가 예외로 완료되면 그 <strong>원래 원인</strong>의 메시지를, 성공이면 {@code "ok:" + 값}을 담은
     * <strong>성공</strong> future로 만든다.
     *
     * <p>핵심 함정: {@code handle}이 받는 {@code Throwable}은 보통 {@link CompletionException} 래퍼다 —
     * {@code getCause()}로 언래핑해 진짜 원인의 메시지를 써야 한다. 힌트:
     * {@code ex instanceof CompletionException ? ex.getCause() : ex}.
     */
    public static CompletableFuture<String> describe(CompletableFuture<?> source) {
        throw new UnsupportedOperationException(
                "TODO: handle + CompletionException 언래핑으로 원인 메시지 추출(성공이면 \"ok:\"+값)");
    }
}
