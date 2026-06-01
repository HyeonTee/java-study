package study.chapter11;

import java.util.concurrent.TimeUnit;

/**
 * 고정 개수의 워커 스레드를 가진 미니 스레드풀 — 표준 {@code Executors.newFixedThreadPool(n)}의 뼈대.
 * 작업을 <strong>작업 큐</strong>에 쌓고, 미리 만들어 둔 워커 N개가 큐에서 하나씩 꺼내 실행한다.
 * 스레드를 작업마다 새로 만들지 않고 <strong>재사용</strong>한다는 게 풀의 본질이다.
 *
 * <p>부품 재사용: 작업 큐로 {@link MyBlockingQueue}(또는 {@link LockedBlockingQueue})를 그대로 쓴다.
 * 큐가 비면 워커는 {@code take()}에서 자연스럽게 잠들고(busy-wait 안 함), 작업이 들어오면 깨어난다 —
 * 앞서 만든 조건부 대기가 정확히 여기 쓰인다. 각 워커의 루프는 본질적으로:
 * <pre>{@code while (살아있음) { Runnable r = queue.take(); r.run(); }}</pre>
 * 한 작업이 예외를 던져도 워커는 죽지 않고 다음 작업으로 넘어가야 한다(작업 하나의 실패가 풀 전체를
 * 망가뜨리면 안 됨 — try/catch로 격리).
 *
 * <p><strong>graceful shutdown.</strong> {@link #shutdown()}은 새 작업 접수만 막고, 이미 큐에 쌓인 작업은
 * <strong>전부 처리</strong>한 뒤 워커가 종료되게 한다. 흔한 기법은 워커 수만큼 <em>독약 토큰</em>(poison
 * pill — 특별한 센티넬 작업)을 큐에 넣어, 각 워커가 그 토큰을 꺼내면 루프를 빠져나오게 하는 것이다(FIFO라
 * 앞의 진짜 작업들이 먼저 소비된다). ch10의 협력적 취소(volatile 플래그)와 같은 철학이되 "큐를 통한" 종료다.
 *
 * <p>이 단원에서는 결과를 돌려받는 {@code Future}/{@code Callable}을 다루지 않는다 — 그건 ch12
 * (CompletableFuture). 여기서는 "제출한 작업이 빠짐없이 정확히 한 번씩 실행된다"는 불변식에 집중한다.
 */
public class MyThreadPool {

    /**
     * @param nThreads 워커 스레드 개수 (1 이상)
     * @throws IllegalArgumentException {@code nThreads < 1}
     */
    public MyThreadPool(int nThreads) {
        throw new UnsupportedOperationException(
                "TODO: 작업 큐를 만들고 nThreads개의 워커 스레드를 생성·start하라 (각 워커는 큐에서 take→run 반복)");
    }

    /**
     * 작업을 큐에 제출한다. 워커 중 하나가 곧 꺼내 실행한다. {@link #shutdown()} 이후 제출하면 거부한다.
     *
     * @throws NullPointerException {@code task}가 null이면
     * @throws java.util.concurrent.RejectedExecutionException 이미 shutdown된 풀에 제출하면
     */
    public void submit(Runnable task) {
        throw new UnsupportedOperationException(
                "TODO: shutdown 여부 확인 후 거부하거나 작업 큐에 넣어라");
    }

    /**
     * 더 이상 새 작업을 받지 않는다. 이미 제출된 작업은 모두 실행된 뒤 워커들이 종료된다(graceful).
     * 종료를 <strong>요청</strong>만 하고 곧 반환한다 — 완료를 기다리려면 {@link #awaitTermination}.
     * 여러 번 호출해도 안전(idempotent)해야 한다.
     *
     * <p>힌트: 종료 플래그를 세우고, 워커가 루프를 빠져나오도록 큐에 워커 수만큼 독약 토큰을 넣는다.
     */
    public void shutdown() {
        throw new UnsupportedOperationException(
                "TODO: 접수 차단 + 워커가 큐를 비운 뒤 멈추도록 종료 신호(독약 토큰 등) 보내기");
    }

    /**
     * {@link #shutdown()} 이후, 모든 워커가 종료될 때까지(또는 시간 초과까지) 기다린다.
     *
     * <p>힌트: 각 워커 스레드를 {@code join(남은 시간)} 하라.
     *
     * @return 시간 안에 모두 종료됐으면 {@code true}, 아니면 {@code false}
     * @throws InterruptedException 대기 중 인터럽트되면
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException(
                "TODO: 모든 워커를 남은 시간 안에서 join, 전부 끝났으면 true");
    }

    /** {@link #shutdown()}이 호출됐으면 {@code true}. */
    public boolean isShutdown() {
        throw new UnsupportedOperationException("TODO: 종료 요청 여부 반환");
    }

    /** 모든 워커가 실제로 종료됐으면 {@code true}(shutdown + 큐 소진 + 워커 종료 완료). */
    public boolean isTerminated() {
        throw new UnsupportedOperationException("TODO: 모든 워커가 종료됐는지 반환");
    }
}
