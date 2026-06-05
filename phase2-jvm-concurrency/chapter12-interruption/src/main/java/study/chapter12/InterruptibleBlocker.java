package study.chapter12;

import java.util.concurrent.CountDownLatch;

/**
 * 블로킹 호출({@link Thread#sleep(long)}) 도중 인터럽트를 받았을 때의 올바른 처리 —
 * {@link InterruptedException}의 의미와 인터럽트 비트 <strong>복원(restore)</strong>.
 *
 * <p>{@link InterruptibleCounter}는 플래그만 폴링했지만, 실제 코드는 대부분
 * {@code sleep}/{@code wait}/{@code take} 같은 <strong>블로킹 호출</strong> 안에서 멈춰 기다린다.
 * 이때 누가 {@code interrupt()}를 걸면 블로킹 메서드는 즉시 {@link InterruptedException}을 던지며
 * 깨어난다. <strong>함정</strong>: 이 예외를 던지는 순간 JVM은 스레드의 인터럽트 비트를
 * <strong>꺼버린다(clear)</strong>. 그래서 {@code catch} 블록에 들어온 시점에는
 * {@code isInterrupted()}가 {@code false}다 — 마치 인터럽트가 없었던 것처럼 보인다.
 *
 * <p>그러므로 예외를 직접 처리하고 끝낼 수 없을 때는, 호출 스택 위쪽이 "이 스레드는 인터럽트
 * 되었다"는 사실을 알 수 있도록 {@code Thread.currentThread().interrupt()}로 비트를
 * <strong>다시 켜 줘야</strong> 한다(restore interrupt status). 이 클래스는 그 전후 상태를 모두
 * 기록해 비트가 꺼졌다가 복원되는 과정을 눈으로 확인하게 한다.
 *
 * <p>{@link #ready}는 테스트가 "워커가 sleep에 들어갔다"는 시점을 알 수 있게 하는 신호용 래치다.
 */
public final class InterruptibleBlocker implements Runnable {

    private final CountDownLatch ready;

    private volatile boolean threwInterrupted;
    private volatile boolean flagClearedInCatch;
    private volatile boolean flagAfterRestore;

    /**
     * @param ready 워커가 블로킹에 들어가기 직전 한 번 카운트다운할 신호용 래치
     */
    public InterruptibleBlocker(CountDownLatch ready) {
        this.ready = ready;
    }

    /**
     * {@code ready}로 준비 신호를 보낸 뒤 오래 블로킹하고, 인터럽트로 깨어나면 그 전후의
     * 인터럽트 비트 상태를 기록하고 비트를 복원한다.
     *
     * <p>힌트:
     * <ol>
     *   <li>{@code ready.countDown()} 으로 "이제 블로킹에 들어간다"고 알린다.</li>
     *   <li>{@code Thread.sleep(Long.MAX_VALUE)} 로 사실상 무한 블로킹한다.</li>
     *   <li>{@code catch (InterruptedException e)} 안에서:
     *     <ul>
     *       <li>{@code threwInterrupted = true} — 예외가 실제로 던져졌음을 표시.</li>
     *       <li>{@code flagClearedInCatch = Thread.currentThread().isInterrupted()}
     *           — IE가 비트를 꺼서 {@code false}일 것이다.</li>
     *       <li>{@code Thread.currentThread().interrupt()} 로 비트를 복원한다.</li>
     *       <li>{@code flagAfterRestore = Thread.currentThread().isInterrupted()}
     *           — 복원했으니 {@code true}일 것이다.</li>
     *     </ul>
     *   </li>
     * </ol>
     */
    @Override
    public void run() {
        throw new UnsupportedOperationException(
                "TODO: ready.countDown() 후 sleep(Long.MAX_VALUE); catch에서 비트 상태 기록 + interrupt()로 복원");
    }

    /**
     * 블로킹 중 {@link InterruptedException}이 실제로 던져졌으면 {@code true}.
     */
    public boolean threwInterrupted() {
        return threwInterrupted;
    }

    /**
     * {@code catch}에 진입한 직후 인터럽트 비트 상태. 예외가 비트를 꺼서 {@code false}여야 한다.
     */
    public boolean flagClearedInCatch() {
        return flagClearedInCatch;
    }

    /**
     * {@code interrupt()}로 복원한 뒤의 인터럽트 비트 상태. {@code true}여야 한다.
     */
    public boolean flagAfterRestore() {
        return flagAfterRestore;
    }
}
