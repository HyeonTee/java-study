package study.chapter12;

/**
 * 용량이 고정된(bounded) 블로킹 큐를 {@code synchronized} + {@code wait}/{@code notifyAll}로
 * 직접 구현한다 — 이 단원의 간판. 조건부 대기(condition wait)의 가장 원초적 형태.
 *
 * <p>ch11의 {@code SafeCounter}는 "값을 잃지 않게 보호"가 전부였고 <strong>대기가 없었다</strong>
 * (임계영역에 들어가면 즉시 일하고 나옴). 여기서 처음으로 "조건이 만족될 때까지 잔다"가 등장한다:
 * 큐가 가득 차면 {@link #put}은 자리가 날 때까지, 큐가 비면 {@link #take}는 원소가 들어올 때까지
 * <strong>모니터를 놓고 잠든다</strong>({@code wait}). 다른 스레드가 상태를 바꾸면 {@code notifyAll}로
 * 깨운다. {@code wait}가 락을 <strong>반납</strong>하기에 다른 스레드가 들어와 조건을 바꿔줄 수 있다 —
 * 이게 ch11에 없던 결정적 차이다.
 *
 * <p><strong>함정 1 — {@code if}가 아니라 반드시 {@code while}로 가드하라.</strong>
 * <pre>{@code while (가득 참) wait();   // O
 * if    (가득 참) wait();   // X}</pre>
 * 이유: (1) <em>가짜 깨어남</em>(spurious wakeup) — 아무도 안 깨웠는데 깰 수 있다. (2) {@code notifyAll}로
 * 여러 대기자가 동시에 깨면, 먼저 깬 스레드가 유일한 빈자리/원소를 가져가 나중 스레드는 조건이 다시
 * 거짓이다. <strong>깨어남은 "조건을 재확인하라"는 신호일 뿐</strong>이다.
 *
 * <p><strong>함정 2 — {@code notify}(하나)가 아니라 {@code notifyAll}(전부).</strong> 이 큐 하나의
 * 모니터에 생산자와 소비자가 <strong>섞여서</strong> 대기한다. {@code notify}는 아무나 하나를 깨우는데
 * 하필 진행 불가한 쪽(자리를 비웠는데 또 다른 소비자)을 깨우면 신호를 잃고 멈출 수 있다(lost wakeup).
 * 안전하게 전부 깨운다. (이 비효율을 {@link LockedBlockingQueue}가 Condition 둘로 없앤다.)
 *
 * <p>{@code wait}/{@code notifyAll}은 반드시 그 객체의 모니터를 쥔 상태(= {@code synchronized} 안)에서만
 * 호출할 수 있다 — 아니면 {@link IllegalMonitorStateException}.
 *
 * @param <T> 원소 타입
 */
public class MyBlockingQueue<T> {

    /**
     * @param capacity 최대 원소 수 (1 이상)
     * @throws IllegalArgumentException {@code capacity < 1}
     */
    public MyBlockingQueue(int capacity) {
        throw new UnsupportedOperationException(
                "TODO: capacity 검증 후 저장 버퍼(배열 등)와 capacity 필드를 초기화하라");
    }

    /**
     * 큐 끝에 원소를 넣는다(FIFO). 가득 차 있으면 자리가 날 때까지 <strong>대기</strong>한다.
     *
     * <p>힌트: {@code synchronized} 안에서 {@code while (가득) wait();} → 넣기 → {@code notifyAll()}.
     *
     * @throws InterruptedException 대기 중 인터럽트되면 (삼키지 말고 던져라 — ch11 협력적 취소와 같은 철학)
     * @throws NullPointerException  {@code item}이 null이면 (표준 BlockingQueue 규약)
     */
    public void put(T item) throws InterruptedException {
        throw new UnsupportedOperationException(
                "TODO: synchronized + while(가득) wait, 넣은 뒤 notifyAll");
    }

    /**
     * 큐 맨 앞 원소를 꺼낸다(FIFO). 비어 있으면 원소가 들어올 때까지 <strong>대기</strong>한다.
     *
     * <p>힌트: {@code while (빔) wait();} → 꺼내기 → {@code notifyAll()}.
     *
     * @throws InterruptedException 대기 중 인터럽트되면
     */
    public T take() throws InterruptedException {
        throw new UnsupportedOperationException(
                "TODO: synchronized + while(빔) wait, 꺼낸 뒤 notifyAll");
    }

    /** 현재 원소 수(보호된 스냅샷 — 호출 직후 값은 바뀔 수 있다). */
    public int size() {
        throw new UnsupportedOperationException("TODO: 같은 모니터로 보호하며 현재 크기 반환");
    }

    /** 최대 용량. */
    public int capacity() {
        throw new UnsupportedOperationException("TODO: capacity 반환");
    }
}
