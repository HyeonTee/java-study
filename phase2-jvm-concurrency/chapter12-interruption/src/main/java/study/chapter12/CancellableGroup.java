package study.chapter12;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 여러 워커 스레드를 한꺼번에 만들고, 인터럽트로 일괄 취소하고, 종료를 기다리는 묶음 —
 * 협력적 취소(cooperative cancellation)를 그룹 단위로 적용.
 *
 * <p>{@link InterruptibleCounter}가 스레드 한 개의 취소라면, 실제 서버/배치에서는 여러 워커를
 * 동시에 띄웠다가 종료 시 <strong>전부 함께</strong> 멈춰야 한다. 패턴은 동일하다: 각 워커는
 * {@code while (!Thread.currentThread().isInterrupted())} 로 협력 루프를 돌고, 종료 시점에
 * 그룹이 모든 워커에게 {@code interrupt()}를 보낸 뒤 {@code join()}으로 정리(reaping)를 기다린다.
 *
 * <p>{@code stopped} 카운터는 여러 워커 스레드가 동시에 증가시키므로 {@link AtomicInteger}로
 * 원자성을 보장한다(ch11 {@code AtomicAccount} 참고). {@code allReady} 래치는 테스트가 "모든
 * 워커가 루프에 진입했다"는 시점을 안 뒤에 취소를 걸 수 있게 한다.
 */
public final class CancellableGroup {

    private final List<Thread> workers = new ArrayList<>();
    private final AtomicInteger stopped = new AtomicInteger();

    /**
     * {@code n}개의 워커 스레드를 만들어 시작한다. 각 워커는 시작하자마자 {@code allReady}를
     * 한 번 카운트다운한 뒤, 인터럽트가 걸릴 때까지 협력 루프를 돌고, 루프를 빠져나오면
     * 멈춘 워커 수를 1 증가시킨다.
     *
     * <p>힌트: 각 워커의 본문은 대략
     * {@code allReady.countDown(); while (!Thread.currentThread().isInterrupted()) { Thread.onSpinWait(); } stopped.incrementAndGet();}
     * 형태다. 만든 {@code Thread}는 {@code start()}하고 {@link #workers} 리스트에 담아 둔다
     * (나중에 {@link #cancelAll()}/{@link #joinAll(long)}이 참조한다).
     *
     * @param n        만들 워커 수
     * @param allReady 각 워커가 루프 진입 직전 한 번씩 카운트다운할 래치 (count가 {@code n}이어야 함)
     */
    public void startWorkers(int n, CountDownLatch allReady) {
        throw new UnsupportedOperationException(
                "TODO: n개의 워커 스레드를 만들어 allReady.countDown 후 협력 루프를 돌게 하고, start + workers에 추가하라");
    }

    /**
     * 모든 워커에게 취소 신호(인터럽트)를 보낸다.
     *
     * <p>힌트: {@link #workers}의 각 스레드에 {@code interrupt()}를 호출하라.
     */
    public void cancelAll() {
        throw new UnsupportedOperationException(
                "TODO: workers의 모든 스레드에 interrupt()를 호출하라");
    }

    /**
     * 모든 워커가 끝날 때까지 기다린다. 각 워커마다 최대 {@code millisEach}밀리초까지 기다린다.
     *
     * <p>힌트: {@link #workers}의 각 스레드에 {@code join(millisEach)}를 호출하라. {@code join}
     * 자체도 블로킹 호출이라 {@link InterruptedException}을 던질 수 있다 — 이 메서드를 호출한
     * 스레드가 기다리던 중 인터럽트되면 그 신호를 잃지 않도록 적절히 처리하라
     * ({@link InterruptibleBlocker}에서 본 비트 복원을 떠올려라).
     *
     * @param millisEach 각 워커를 기다릴 최대 시간(밀리초)
     */
    public void joinAll(long millisEach) {
        throw new UnsupportedOperationException(
                "TODO: workers의 각 스레드를 join(millisEach)하라 (InterruptedException은 적절히 처리)");
    }

    /**
     * 지금까지 협력 루프를 빠져나와 멈춘 워커 수를 반환한다.
     */
    public int stoppedCount() {
        return stopped.get();
    }
}
