package study.chapter10;

/**
 * 락 없이(lock-free) 원자적으로 갱신되는 계좌 — {@code java.util.concurrent.atomic}과 CAS.
 *
 * <p>{@link SafeCounter}는 {@code synchronized}로 임계영역을 만들어 문제를 풀었다. 같은 문제를
 * <strong>락 없이</strong> 푸는 방법이 {@code AtomicLong} 같은 원자적 타입이다. 내부적으로
 * <strong>CAS(Compare-And-Swap)</strong>라는 하드웨어 명령을 쓴다: "메모리의 값이 내가 방금 읽은
 * 값 그대로일 때만 새 값으로 바꿔라. 그 사이 누가 바꿨으면 실패를 알려라."
 *
 * <p>{@code incrementAndGet}/{@code addAndGet}처럼 단순 누적은 한 줄로 끝나지만,
 * <strong>조건부 갱신</strong>(예: "잔액이 충분할 때만 출금")은 직접 <strong>CAS 재시도 루프</strong>를
 * 짜야 한다. 단순히 {@code get()} 후 {@code set()}하면 그 사이 다른 스레드가 끼어드는
 * <strong>check-then-act 경쟁조건</strong>이 생기기 때문이다.
 *
 * <pre>{@code
 * // CAS 재시도 루프의 형태 (의사코드 — 직접 채워야 함)
 * while (true) {
 *     long current = ...읽기...;
 *     if (조건 불만족) return 실패;
 *     long next = current 로부터 계산;
 *     if (compareAndSet(current, next)) return 성공;  // 성공하면 끝
 *     // 실패하면(누가 먼저 바꿈) 루프 처음으로 — 다시 읽고 재시도
 * }
 * }</pre>
 */
public final class AtomicAccount {

    /**
     * 초기 잔액으로 계좌를 만든다.
     *
     * <p>힌트: 잔액과 입금 횟수를 각각 원자적 타입(예: {@code AtomicLong})으로 보관하라.
     *
     * @throws IllegalArgumentException {@code initialBalance < 0}
     */
    public AtomicAccount(long initialBalance) {
        throw new UnsupportedOperationException(
                "TODO: 잔액(과 입금 횟수)을 원자적 타입으로 보관하라");
    }

    /**
     * 현재 잔액을 반환한다.
     */
    public long balance() {
        throw new UnsupportedOperationException("TODO: 현재 잔액 반환");
    }

    /**
     * {@code amount}를 입금하고 <strong>갱신된 잔액</strong>을 반환한다. 입금 횟수도 1 증가한다.
     *
     * <p>힌트: 단순 누적이므로 {@code addAndGet}/{@code incrementAndGet}으로 충분하다 —
     * 여기엔 CAS 루프를 직접 짤 필요가 없다.
     *
     * @throws IllegalArgumentException {@code amount <= 0}
     */
    public long deposit(long amount) {
        throw new UnsupportedOperationException(
                "TODO: amount를 원자적으로 더하고 새 잔액 반환, 입금 횟수도 증가");
    }

    /**
     * 잔액이 충분하면 {@code amount}만큼 출금하고 {@code true}, 부족하면 잔액을 그대로 두고
     * {@code false}를 반환한다.
     *
     * <p>힌트: 이 메서드가 이 문제의 핵심이다. <strong>CAS 재시도 루프</strong>로 구현하라.
     * 현재 잔액을 읽고 → 부족하면 즉시 {@code false} → 충분하면
     * {@code compareAndSet(current, current - amount)}를 시도 → 실패하면(다른 스레드가 먼저
     * 잔액을 바꿈) 다시 읽고 재시도. {@code get()} 후 {@code set()}으로 나눠 하면 그 틈에
     * 잔액이 음수로 내려갈 수 있다.
     *
     * @throws IllegalArgumentException {@code amount <= 0}
     */
    public boolean withdraw(long amount) {
        throw new UnsupportedOperationException(
                "TODO: CAS 재시도 루프로 '충분할 때만' 원자적으로 출금하라");
    }

    /**
     * 지금까지 성공한 입금 횟수를 반환한다.
     */
    public long depositCount() {
        throw new UnsupportedOperationException("TODO: 성공한 입금 횟수 반환");
    }
}
