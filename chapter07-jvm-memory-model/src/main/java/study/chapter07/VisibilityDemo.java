package study.chapter07;

/**
 * 멀티스레드 환경에서 가시성(visibility) 문제를 보여주는 데모 클래스.
 *
 * <p>이 클래스 자체는 연습 문제가 아니다. README와 함께 읽으면서
 * volatile의 효과를 관찰하는 용도.
 */
public class VisibilityDemo {

    private boolean running = true;          // volatile 아님 — 가시성 문제 가능
    private volatile boolean runningV = true; // volatile — 가시성 보장

    /**
     * running 플래그로 루프를 제어한다.
     * 메인 스레드에서 {@link #stopNonVolatile()}을 호출해도
     * 이 루프가 멈추지 않을 수 있다 (가시성 미보장).
     */
    public void loopWithoutVolatile() {
        while (running) {
            // busy-wait
        }
    }

    /**
     * volatile {@code runningV} 플래그로 루프를 제어한다.
     * 메인 스레드에서 {@link #stopVolatile()}을 호출하면
     * 이 루프는 반드시 멈춘다 (가시성 보장).
     */
    public void loopWithVolatile() {
        while (runningV) {
            // busy-wait
        }
    }

    public void stopNonVolatile() {
        running = false;
    }

    public void stopVolatile() {
        runningV = false;
    }
}
