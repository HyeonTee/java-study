package study.chapter12;

/**
 * 인터럽트 비트를 <strong>읽는</strong> 두 가지 방법의 차이 —
 * {@link Thread#interrupted()} (읽고 지움) vs {@link Thread#isInterrupted()} (읽기만).
 *
 * <p>인터럽트 상태를 확인하는 API가 두 개인데, 이름이 비슷해 자주 헷갈린다:
 * <ul>
 *   <li><strong>{@code Thread.interrupted()}</strong> — 정적 메서드. 현재 스레드의 비트를 읽어
 *       반환하면서 <strong>동시에 비트를 끈다(clear)</strong>. 즉 두 번 연속 호출하면 두 번째는
 *       (그 사이 다시 인터럽트되지 않는 한) {@code false}다. "인터럽트 신호를 소비(drain)"하는
 *       용도.</li>
 *   <li><strong>{@code thread.isInterrupted()}</strong> — 인스턴스 메서드. 비트를 읽기만 하고
 *       <strong>지우지 않는다</strong>. 몇 번을 호출해도 상태가 그대로 유지된다.</li>
 * </ul>
 *
 * <p>유틸리티 클래스이므로 인스턴스를 만들지 않는다({@code private} 생성자).
 */
public final class InterruptStatus {

    private InterruptStatus() {
    }

    /**
     * 현재 스레드의 인터럽트 비트를 <strong>읽고 지운다</strong>. 읽은 값을 반환한다.
     *
     * <p>힌트: {@code Thread.interrupted()} 의 반환값을 그대로 돌려주면 된다(이 정적 메서드가
     * 곧 read-and-clear다).
     */
    public static boolean drain() {
        throw new UnsupportedOperationException(
                "TODO: Thread.interrupted() 로 현재 스레드 인터럽트 비트를 읽고 지운 값을 반환하라");
    }

    /**
     * 현재 스레드의 인터럽트 비트를 <strong>읽되 지우지 않는다</strong>. 읽은 값을 반환한다.
     *
     * <p>힌트: {@code Thread.currentThread().isInterrupted()} 의 반환값을 그대로 돌려주면 된다.
     */
    public static boolean isSet() {
        throw new UnsupportedOperationException(
                "TODO: Thread.currentThread().isInterrupted() 로 비트를 지우지 않고 읽은 값을 반환하라");
    }
}
