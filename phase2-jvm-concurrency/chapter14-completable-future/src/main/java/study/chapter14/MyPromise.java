package study.chapter14;

import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * {@code CompletableFuture}의 정수를 손으로 구현하는 미니 promise — 이 단원의 간판.
 * "결과를 <strong>기다리지 않고</strong>, 결과가 오면 할 일(콜백)을 미리 등록해 둔다"가 전부다.
 *
 * <p>ch13의 {@code BlockingQueue.take()}는 결과가 올 때까지 <strong>블록</strong>했다(pull). 여기서는
 * 블록하지 않고 {@link #thenApply}로 <strong>변환 파이프라인을 미리 조립</strong>해 둔다(push). 나중에
 * {@link #complete}가 호출되면 등록된 콜백이 도미노처럼 발화하며 파이프라인이 흐른다. 이것이
 * CompletableFuture의 핵심이고, ch05의 {@code Optional.map}/{@code flatMap}/{@code orElse}를 "값이
 * 나중에 온다"로 일반화한 <strong>비동기 모나드</strong>다.
 *
 * <p><strong>완료는 단 한 번.</strong> 상태는 미완료 → (값으로 완료 | 예외로 완료) 중 하나로만 한 번
 * 전이한다. 두 번째 완료 시도는 무시한다({@link #complete}/{@link #completeExceptionally}가
 * {@code false} 반환 — 표준 {@code CompletableFuture}와 같은 멱등 계약).
 *
 * <p><strong>콜백 발화 규칙(핵심).</strong> 완료 <em>전</em> 등록한 콜백은 모아 뒀다가 완료 시점에 등록
 * 순서대로 발화하고, 완료 <em>후</em> 등록한 콜백은 등록 즉시 발화한다. 어느 경우든 정확히 한 번 실행된다.
 *
 * <p>이 클래스는 수동 {@link #complete}만으로 완전히 동작한다(스레드 불필요) — 그래서 결정적으로 검증된다.
 * {@link #get()}만은 ch13에서 익힌 {@code wait}/{@code notifyAll}로 완료까지 블록한다.
 *
 * @param <T> 이 promise가 완료될 값의 타입
 */
public class MyPromise<T> {

    /** 상태·콜백 보호 + get()의 블로킹 대기에 쓰는 모니터. (준비되어 있음 — 이걸 써서 구현하라.) */
    private final Object lock = new Object();

    // TODO: 상태(미완료/값완료/예외완료), 값, 예외, 그리고 콜백 리스트를 담을 필드를 직접 선언하라.
    //       콜백은 (값, 예외) 둘 다 받는 형태(BiConsumer<T, Throwable>)로 통일하면 편하다.

    public MyPromise() {
        throw new UnsupportedOperationException(
                "TODO: 상태=미완료, 콜백 리스트 초기화");
    }

    /**
     * 이 promise를 값으로 완료한다. 처음 완료될 때만 성공이며, 그때 대기 중이던 콜백을 등록 순서대로
     * 발화하고 {@link #get}으로 블록된 스레드를 깨운다({@code notifyAll}).
     *
     * @return 이 호출로 완료시켰으면 {@code true}, 이미 완료돼 있었으면 {@code false}
     */
    public boolean complete(T value) {
        throw new UnsupportedOperationException(
                "TODO: 이미 완료면 false. 아니면 값 저장+상태 전이, 대기 콜백 발화, get 대기자 깨우기, true");
    }

    /**
     * 이 promise를 예외로 완료한다. 하류 {@link #thenApply}/{@link #thenCompose}는 변환을 <strong>건너뛰고</strong>
     * 이 예외를 그대로 전파받는다.
     *
     * @return 이 호출로 완료시켰으면 {@code true}, 이미 완료돼 있었으면 {@code false}
     * @throws NullPointerException {@code ex}가 null이면
     */
    public boolean completeExceptionally(Throwable ex) {
        throw new UnsupportedOperationException(
                "TODO: null 검사, 이미 완료면 false. 아니면 예외 저장+상태 전이, 콜백 발화(예외 경로), 깨우기, true");
    }

    /** 값이든 예외든 완료됐으면 {@code true}. */
    public boolean isDone() {
        throw new UnsupportedOperationException("TODO: 완료 여부 반환");
    }

    /**
     * 완료 시 (값, 예외)로 호출되는 콜백을 등록한다. 성공이면 (값, null), 예외면 (null, 예외). 이미 완료돼
     * 있으면 즉시 발화한다. 가장 원초적인 등록 메서드 — {@link #thenApply}/{@link #thenCompose}를 이 위에
     * 얹을 수 있다.
     *
     * @return 체이닝을 위한 {@code this}
     */
    public MyPromise<T> whenComplete(BiConsumer<? super T, ? super Throwable> callback) {
        throw new UnsupportedOperationException(
                "TODO: 완료 상태면 즉시 callback 발화, 아니면 콜백 리스트에 추가. this 반환");
    }

    /**
     * 이 promise가 값으로 완료되면 {@code fn}을 적용한 결과로 완료되는 <strong>새</strong> promise를 반환한다
     * (= {@code Optional.map}의 비동기판). 이 promise가 예외로 완료되면 {@code fn}을 건너뛰고 새 promise도
     * 같은 예외로 완료된다. {@code fn} 자체가 예외를 던지면 새 promise는 그 예외로 완료된다.
     *
     * <p>힌트: 결과 promise를 하나 만들고, {@code this}에 콜백을 등록하라 — 값이 오면 {@code fn} 적용해
     * 결과 promise를 {@code complete}, 예외가 오면 {@code completeExceptionally}로 전파. 그리고 결과 promise를 반환.
     */
    public <U> MyPromise<U> thenApply(Function<? super T, ? extends U> fn) {
        throw new UnsupportedOperationException(
                "TODO: 새 MyPromise<U> 생성 → this에 콜백 등록(값→fn→complete / 예외→전파 / fn 예외→completeExceptionally) → 새 promise 반환");
    }

    /**
     * {@link #thenApply}와 같되 {@code fn}이 값을 또 다른 <strong>promise</strong>로 변환할 때 쓴다. 중첩
     * ({@code MyPromise<MyPromise<U>>})을 펼쳐 평평한 {@code MyPromise<U>}를 반환한다(= {@code Optional.flatMap}의
     * 비동기판 — 비동기 작업이 또 다른 비동기 작업으로 이어질 때).
     *
     * <p>힌트: 결과 promise를 만들고, 값이 오면 {@code fn} 적용 → 그 <em>inner</em> promise에 <strong>다시
     * 콜백을 걸어</strong> inner가 완료되면 결과 promise를 완료하라(콜백 안의 콜백 = 평탄화).
     */
    public <U> MyPromise<U> thenCompose(Function<? super T, ? extends MyPromise<U>> fn) {
        throw new UnsupportedOperationException(
                "TODO: thenApply와 같되, fn 결과(promise)에 다시 콜백을 걸어 평탄화(inner 완료 시 결과 promise 완료)");
    }

    /**
     * 완료될 때까지 <strong>블록</strong>하고 값을 반환한다(ch13에서 익힌 {@code wait}/{@code notifyAll}).
     * 예외로 완료됐으면 그 예외를 {@link ExecutionException}으로 <strong>감싸</strong> 던진다(표준
     * {@code Future.get} 규약 — 원인은 {@code getCause()}로 꺼낸다).
     *
     * @throws ExecutionException   예외로 완료된 경우(원인을 감쌈)
     * @throws InterruptedException 대기 중 인터럽트되면
     */
    public T get() throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException(
                "TODO: synchronized + while(!완료) wait. 값이면 반환, 예외면 ExecutionException(원인)으로 감싸 던지기");
    }

    /** 이미 값으로 완료된 promise를 만든다(= {@code CompletableFuture.completedFuture}). */
    public static <U> MyPromise<U> completed(U value) {
        throw new UnsupportedOperationException("TODO: 새 promise 만들고 complete(value) 후 반환");
    }

    /** 이미 예외로 완료된 promise를 만든다(= {@code CompletableFuture.failedFuture}). */
    public static <U> MyPromise<U> failed(Throwable ex) {
        throw new UnsupportedOperationException("TODO: 새 promise 만들고 completeExceptionally(ex) 후 반환");
    }
}
