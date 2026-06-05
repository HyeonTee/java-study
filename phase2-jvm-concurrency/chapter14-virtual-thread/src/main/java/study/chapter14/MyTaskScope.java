package study.chapter14;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * 구조적 동시성(structured concurrency) 미니 스코프 — 이 단원의 간판. 가상 스레드 위에 직접 구현한다.
 *
 * <p>표준 {@code java.util.concurrent.StructuredTaskScope}는 Java 21 <strong>프리뷰</strong>라
 * ({@code --enable-preview} 필요, 버전마다 시그니처가 바뀜) 쓰지 않는다. 대신 그 핵심 의미론을 가상
 * 스레드 위에 손으로 만든다(ch12 {@code MyThreadPool}, ch13 {@code MyPromise}처럼 "표준 내부를 직접 구현").
 *
 * <p><strong>구조적 동시성</strong>이란: 한 블록 안에서 띄운 모든 하위 작업의 수명이 그 블록을 벗어나지
 * 않게 묶는 것이다. ch12 스레드풀의 작업들은 서로 무관했지만, 여기서는 {@link #fork}로 띄운 작업들을
 * 하나의 단위로 보고 {@link #join}에서 한꺼번에 기다린다(ch13 {@code allOf}의 "수명 묶기" 버전).
 *
 * <p><strong>shutdown-on-failure 의미론.</strong> fork한 작업 중 <strong>하나라도 예외로 실패</strong>하면,
 * 아직 실행 중인 나머지 작업을 {@code interrupt()}로 <strong>취소</strong>하고, {@link #throwIfFailed()}가
 * <strong>첫 번째 예외</strong>를 전파한다. {@link AutoCloseable}이라 try-with-resources로 쓰며,
 * {@link #close}가 블록을 벗어날 때 모든 작업이 끝났음(고아 작업 없음)을 보장한다.
 *
 * <pre>{@code
 * try (MyTaskScope scope = new MyTaskScope()) {
 *     var a = scope.fork(() -> fetchUser());
 *     var b = scope.fork(() -> fetchAge());
 *     scope.join();           // 둘 다 끝날 때까지(또는 하나 실패 시 나머지 취소) 대기
 *     scope.throwIfFailed();  // 실패했으면 첫 예외 전파
 *     use(a.get(), b.get());  // 둘 다 성공했을 때만 도달
 * }
 * }</pre>
 */
public class MyTaskScope implements AutoCloseable {

    /** 하위 작업 목록·상태·첫 예외를 보호하는 모니터. (준비됨 — 이걸 써서 구현하라.) */
    private final Object lock = new Object();

    // TODO: 하위 작업(Subtask) 목록과 그 가상 스레드 목록, 첫 예외, join/close 여부를 담을 필드를 직접 선언하라.

    public MyTaskScope() {
        throw new UnsupportedOperationException("TODO: 하위 작업/스레드 목록과 상태 필드 초기화");
    }

    /**
     * {@code task}를 <strong>새 가상 스레드</strong>로 즉시 시작하고, 그 핸들 {@link Subtask}를 반환한다.
     * 작업이 성공하면 결과를, 예외를 던지면 그 예외를 {@code Subtask}가 보관한다. 작업이 예외로 끝나면
     * 이 스코프를 실패 상태로 만들고 <strong>다른 하위 작업들을 취소(interrupt)</strong>해야 한다.
     *
     * <p>힌트: 가상 스레드 안에서 {@code task.call()}을 try/catch로 감싸 결과/예외를 Subtask에 기록하라.
     * 예외 경로에서 "첫 실패"면 첫 예외를 기록하고 나머지 하위 작업 스레드를 {@code interrupt()}하라.
     *
     * @throws IllegalStateException 이미 {@link #join}되었거나 {@link #close}된 뒤 fork하면
     * @throws NullPointerException  {@code task}가 null이면
     */
    public <T> Subtask<T> fork(Callable<? extends T> task) {
        throw new UnsupportedOperationException(
                "TODO: Subtask 생성 → 가상 스레드로 task.call() 실행(성공=결과 / 예외=첫 실패면 기록+나머지 interrupt) → Subtask 반환");
    }

    /**
     * fork된 모든 하위 작업이 끝날 때까지(성공이든 실패·취소든) 기다린다. 같은 스코프에서 한 번만 호출한다.
     *
     * <p>힌트: 모든 하위 작업의 가상 스레드를 {@code join()}하라(취소된 스레드도 곧 끝난다).
     *
     * @return 체이닝을 위한 {@code this}
     * @throws InterruptedException 이 스코프를 호출한 스레드 자신이 인터럽트되면
     */
    public MyTaskScope join() throws InterruptedException {
        throw new UnsupportedOperationException("TODO: 모든 하위 작업 스레드를 join. this 반환");
    }

    /**
     * {@link #join} 후 호출한다. 하위 작업 중 하나라도 실패했으면 <strong>첫 번째 예외</strong>를
     * {@link ExecutionException}으로 감싸 던진다(원인은 {@code getCause()}). 전부 성공이면 아무 일도 안 한다.
     *
     * @throws ExecutionException    하위 작업이 실패한 경우(원인을 감쌈)
     * @throws IllegalStateException {@link #join} 전에 호출하면
     */
    public void throwIfFailed() throws ExecutionException {
        throw new UnsupportedOperationException(
                "TODO: join 전이면 IllegalStateException. 첫 예외가 있으면 ExecutionException(원인)으로 던지기");
    }

    /**
     * 스코프를 닫는다(try-with-resources 종료 시 자동 호출). 아직 끝나지 않은 하위 작업이 있으면 취소하고
     * 모두 종료될 때까지 정리한다 — 작업이 스코프 밖으로 새어 나가지 않음을 보장한다. 여러 번 호출해도 안전.
     */
    @Override
    public void close() {
        throw new UnsupportedOperationException(
                "TODO: 남은 하위 작업 취소(interrupt)+종료 대기 후 닫힌 상태로 (idempotent)");
    }

    /**
     * {@link #fork}로 띄운 하나의 하위 작업 핸들. ch13 {@code MyPromise}의 축소판 — 상태는
     * 미완료 → (성공 | 실패) 중 하나로만 전이한다. 생성은 {@link MyTaskScope#fork}만 담당한다.
     *
     * @param <T> 작업 결과 타입
     */
    public static final class Subtask<T> {

        // TODO: 상태(UNAVAILABLE/SUCCESS/FAILED), 결과, 예외 필드와, scope가 호출할 완료 메서드를 직접 선언하라.

        Subtask() {
            throw new UnsupportedOperationException("TODO: 상태=UNAVAILABLE로 초기화");
        }

        /** 현재 상태. 완료 전에는 {@link State#UNAVAILABLE}. */
        public State state() {
            throw new UnsupportedOperationException("TODO: 현재 상태 반환");
        }

        /**
         * 성공한 작업의 결과.
         *
         * @throws IllegalStateException 아직 끝나지 않았거나({@link State#UNAVAILABLE}) 실패한 경우
         */
        public T get() {
            throw new UnsupportedOperationException("TODO: SUCCESS면 결과, 아니면 IllegalStateException");
        }

        /**
         * 실패한 작업이 던진 예외.
         *
         * @throws IllegalStateException 아직 끝나지 않았거나 성공한 경우
         */
        public Throwable exception() {
            throw new UnsupportedOperationException("TODO: FAILED면 예외, 아니면 IllegalStateException");
        }

        /** 하위 작업의 완료 상태. */
        public enum State { UNAVAILABLE, SUCCESS, FAILED }
    }
}
