package study.chapter07;

/**
 * 다른 스레드가 보낸 정지 신호를 받고 협력적으로(cooperatively) 멈추는 작업 루프 —
 * {@code volatile}의 가시성.
 *
 * <p>스레드를 바깥에서 강제로 죽이는 {@code Thread.stop()}은 위험해서 폐기되었다. 올바른 방법은
 * <strong>플래그를 두고, 작업 스레드가 매 사이클마다 그 플래그를 확인</strong>해 스스로 멈추는
 * 것이다(cooperative cancellation). 핵심 함정: 이 플래그가 {@code volatile}이 아니면, 작업
 * 스레드가 자기 CPU 캐시에 들고 있는 옛 값({@code false})만 계속 읽어 정지 신호를
 * <strong>영영 못 볼 수 있다</strong> — 무한 루프(ch06 가시성 데모 {@code VisibilityDemo} 참고).
 *
 * <p>{@code volatile}은 한 스레드의 쓰기가 다른 스레드에 <strong>즉시 보이게</strong> 보장한다
 * (ch06 happens-before 규칙 1). 여기서 정확히 그게 필요하다.
 *
 * <p>{@code Runnable}을 구현하므로 {@code new Thread(worker).start()}로 실행한다.
 */
public final class StoppableWorker implements Runnable {

    private final Runnable unitOfWork;

    // TODO: 정지 플래그와 실행 상태/사이클 수를 담을 필드를 직접 선언하라.
    //       정지 플래그는 반드시 volatile 이어야 한다 (다른 스레드의 쓰기를 봐야 하므로).

    /**
     * @param unitOfWork 한 사이클마다 한 번씩 실행할 작업
     */
    public StoppableWorker(Runnable unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    /**
     * 정지 요청이 올 때까지 {@link #unitOfWork}를 반복 실행한다. 매 반복마다 완료 사이클 수를
     * 1 증가시킨다. 정지 요청을 확인하면 루프를 빠져나오고 종료한다.
     *
     * <p>힌트: {@code while (!정지요청) { unitOfWork.run(); 사이클++ }} 형태. 루프 진입 전후로
     * "실행 중" 상태를 적절히 갱신하라.
     */
    @Override
    public void run() {
        throw new UnsupportedOperationException(
                "TODO: 정지 플래그가 설정될 때까지 unitOfWork를 반복 실행하라 (사이클 수 증가)");
    }

    /**
     * 다른 스레드에서 호출해 정지를 <strong>요청</strong>한다. 이 호출 이후 작업 스레드는
     * 진행 중인 사이클을 마치고 곧 멈춰야 한다.
     *
     * <p>힌트: 정지 플래그를 {@code true}로. 이 메서드는 작업 스레드가 아닌 다른 스레드가
     * 호출하므로, 플래그의 가시성이 핵심이다.
     */
    public void requestStop() {
        throw new UnsupportedOperationException("TODO: 정지 플래그를 true로 설정하라");
    }

    /**
     * 작업 루프가 현재 돌고 있으면 {@code true}. {@link #run()} 시작 전과 종료 후에는
     * {@code false}여야 한다.
     */
    public boolean isRunning() {
        throw new UnsupportedOperationException("TODO: 현재 실행 중인지 반환");
    }

    /**
     * 지금까지 완료한 사이클 수를 반환한다.
     */
    public long completedCycles() {
        throw new UnsupportedOperationException("TODO: 완료한 사이클 수 반환");
    }
}
