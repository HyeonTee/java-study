package study.chapter11;

/**
 * 여러 스레드가 동시에 증가시켜도 값을 잃지 않는 카운터 — {@code synchronized}로 상호배제.
 *
 * <p>{@link UnsafeCounter}를 먼저 보라. {@code count++}는 한 줄처럼 보이지만 실제로는
 * <strong>읽기 → 증가 → 쓰기</strong> 세 단계(read-modify-write)다. 두 스레드가 같은 값을 읽고
 * 각자 +1 해서 같은 값을 쓰면, 한 번의 증가가 통째로 <strong>사라진다(lost update)</strong>.
 *
 * <p>{@code synchronized}는 이 세 단계를 <strong>하나의 임계영역(critical section)</strong>으로
 * 묶어, 한 번에 한 스레드만 들어가게 한다(상호배제). 같은 모니터에 대한 unlock→lock은
 * happens-before 관계라서(ch08 규칙 2), 상호배제뿐 아니라 <strong>가시성</strong>까지 덤으로
 * 해결된다 — 그래서 읽기({@link #get()})도 같은 모니터로 보호해야 최신값이 보인다.
 *
 * <p>두 가지 표기를 모두 연습한다:
 * <ul>
 *   <li>{@link #increment()} — {@code synchronized} <strong>메서드</strong>로 구현하라.</li>
 *   <li>{@link #add(long)} — {@code synchronized(this) { ... }} <strong>블록</strong>으로 구현하라.</li>
 * </ul>
 * 인스턴스 메서드에 붙인 {@code synchronized}의 락은 {@code this}이므로, 위 둘은 결국 같은
 * 모니터({@code this})를 공유한다 — 그래서 서로 안전하게 배타적이다. (서로 다른 락을 잡으면
 * 상호배제가 깨진다는 점이 흔한 함정이다.)
 */
public final class SafeCounter {

    private long count = 0;

    /**
     * 값을 1 증가시킨다. <strong>{@code synchronized} 메서드</strong>로 구현하라.
     */
    public void increment() {
        throw new UnsupportedOperationException(
                "TODO: count++ 를 synchronized 메서드로 보호하라 (read-modify-write가 원자적이어야 함)");
    }

    /**
     * 값에 {@code delta}를 더한다({@code delta}는 음수도 가능). 이 메서드는
     * <strong>{@code synchronized(this) { ... }} 블록</strong>으로 구현하라.
     */
    public void add(long delta) {
        throw new UnsupportedOperationException(
                "TODO: count += delta 를 synchronized(this) 블록으로 보호하라");
    }

    /**
     * 현재 값을 반환한다.
     *
     * <p>힌트: 쓰기와 <strong>같은 모니터</strong>로 보호해야 다른 스레드가 마지막에 쓴 값이
     * 보장되어 보인다(가시성). 보호하지 않으면 오래된 값을 읽을 수 있다.
     */
    public long get() {
        throw new UnsupportedOperationException(
                "TODO: 쓰기와 같은 모니터로 보호하며 현재 count를 반환하라");
    }

    /**
     * 값을 0으로 되돌린다.
     */
    public void reset() {
        throw new UnsupportedOperationException("TODO: count를 0으로 (역시 보호 필요)");
    }
}
