package study.chapter12;

/**
 * 인터럽트 플래그를 매 사이클마다 폴링해 협력적으로 멈추는 작업 루프 —
 * {@link Thread#isInterrupted()}로 취소 신호 읽기.
 *
 * <p>ch11 {@code StoppableWorker}는 직접 만든 {@code volatile boolean stop} 플래그로 취소를
 * 구현했다. 사실 모든 스레드는 JVM이 기본 제공하는 <strong>인터럽트 플래그(interrupt status)</strong>를
 * 하나씩 갖고 있어, 굳이 내 플래그를 만들 필요가 없다. 다른 스레드가
 * {@code thread.interrupt()}를 호출하면 이 비트가 켜지고, 작업 스레드는
 * {@code Thread.currentThread().isInterrupted()}로 그 신호를 본다.
 *
 * <p>이 클래스는 <strong>블로킹 호출 없이</strong> 순수하게 플래그만 폴링하는 형태다 — 즉
 * {@code Thread.sleep}/{@code wait} 같은 것을 쓰지 않고, CPU를 계속 돌리며 매 반복마다 인터럽트
 * 비트를 확인한다(busy loop). 블로킹 호출이 끼어 있을 때의 처리는 {@link InterruptibleBlocker}에서
 * 따로 연습한다.
 *
 * <p>{@code Runnable}을 구현하므로 {@code new Thread(counter).start()}로 실행한다.
 */
public final class InterruptibleCounter implements Runnable {

    private volatile long count;
    private volatile boolean exitedByInterrupt;

    /**
     * 인터럽트가 걸릴 때까지 카운트를 계속 1씩 증가시킨다. 루프가 인터럽트 때문에 끝났다면
     * 그 사실을 기록한다.
     *
     * <p>힌트: {@code while (!Thread.currentThread().isInterrupted()) { count++ }} 형태로 돌고,
     * 루프를 빠져나온 뒤 {@code exitedByInterrupt}를 {@code true}로 설정하라. 블로킹 호출
     * ({@code sleep}/{@code wait})은 쓰지 말고, 인터럽트 비트만 폴링한다.
     */
    @Override
    public void run() {
        throw new UnsupportedOperationException(
                "TODO: 인터럽트 비트가 켜질 때까지 count를 증가시키고, 루프 종료 후 exitedByInterrupt=true");
    }

    /**
     * 지금까지 증가시킨 카운트 값을 반환한다.
     */
    public long count() {
        return count;
    }

    /**
     * 작업 루프가 인터럽트 때문에 종료되었으면 {@code true}.
     */
    public boolean exitedByInterrupt() {
        return exitedByInterrupt;
    }
}
