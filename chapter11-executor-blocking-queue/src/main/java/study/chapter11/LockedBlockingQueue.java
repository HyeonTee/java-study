package study.chapter11;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link MyBlockingQueue}와 <strong>똑같은 동작</strong>을, 이번엔 {@link ReentrantLock}과 두 개의
 * {@link Condition}({@code notFull}, {@code notEmpty})으로 구현한다 — 표준 {@code ArrayBlockingQueue}가
 * 내부에서 쓰는 바로 그 구조.
 *
 * <p>{@code synchronized}+{@code wait}/{@code notifyAll}과의 1:1 대응을 익혀라:
 * <pre>{@code synchronized(this){...}  →  lock.lock(); try {...} finally { lock.unlock(); }
 * wait()                     →  someCondition.await()
 * notifyAll()                →  someCondition.signalAll()}</pre>
 *
 * <p><strong>핵심 이득 — 대기 조건을 둘로 나눈다.</strong> 한 락에 {@code Condition}을 여러 개 걸 수 있다.
 * 자리가 없어 막힌 생산자는 {@code notFull}에서, 원소가 없어 막힌 소비자는 {@code notEmpty}에서 따로
 * 잔다. 그래서 원소를 하나 넣은 뒤엔 {@code notEmpty.signal()}로 <strong>소비자만 콕 집어</strong> 깨우면
 * 된다 — {@code notifyAll}로 무관한 생산자까지 깨우던 비효율이 사라진다.
 *
 * <p>여전히 {@code while (조건) await();} 가드는 필수이고, {@code unlock()}은 반드시 {@code finally}에
 * 둬라(임계영역에서 예외가 나도 락이 풀려야 다른 스레드가 굶지 않는다). {@code await}는 대기 동안 락을
 * 자동으로 놓았다가 깨어날 때 다시 잡는다.
 *
 * @param <T> 원소 타입
 */
public class LockedBlockingQueue<T> {

    // 락과 두 개의 대기 줄은 미리 준비되어 있다. 이걸 써서 put/take/offer를 구현하라.
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    /**
     * @param capacity 최대 원소 수 (1 이상)
     * @throws IllegalArgumentException {@code capacity < 1}
     */
    public LockedBlockingQueue(int capacity) {
        throw new UnsupportedOperationException(
                "TODO: capacity 검증 후 저장 버퍼와 필드 초기화 (lock/Condition은 이미 준비됨)");
    }

    /**
     * 큐 끝에 넣는다. 가득 차 있으면 {@code notFull.await()}로 대기.
     *
     * <p>힌트: {@code lock.lock(); try { while(가득) notFull.await(); 넣기; notEmpty.signal(); }
     * finally { lock.unlock(); }}
     *
     * @throws InterruptedException 대기 중 인터럽트되면
     * @throws NullPointerException  {@code item}이 null이면
     */
    public void put(T item) throws InterruptedException {
        throw new UnsupportedOperationException(
                "TODO: lock/try/finally + while(가득) notFull.await(), 넣은 뒤 notEmpty.signal()");
    }

    /**
     * 맨 앞 원소를 꺼낸다(FIFO). 비어 있으면 {@code notEmpty.await()}로 대기.
     *
     * @throws InterruptedException 대기 중 인터럽트되면
     */
    public T take() throws InterruptedException {
        throw new UnsupportedOperationException(
                "TODO: lock/try/finally + while(빔) notEmpty.await(), 꺼낸 뒤 notFull.signal()");
    }

    /**
     * 정해진 시간까지만 넣기를 시도한다. 시간 안에 자리가 나면 넣고 {@code true}, 끝까지 자리가 안 나면
     * 넣지 않고 {@code false}.
     *
     * <p>힌트: {@code Condition.awaitNanos(long)}의 <strong>반환값(남은 시간)</strong>을 추적하라. 깰 때마다
     * 남은 시간이 0 이하면 포기. 이게 무한 {@code await()}와 결정적 타임아웃의 차이다.
     *
     * @return 넣었으면 {@code true}, 시간 초과면 {@code false}
     * @throws InterruptedException 대기 중 인터럽트되면
     */
    public boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException(
                "TODO: awaitNanos로 남은 시간을 추적하며 시도, 시간 내 자리 나면 넣고 true / 아니면 false");
    }

    /** 현재 원소 수(보호된 스냅샷). */
    public int size() {
        throw new UnsupportedOperationException("TODO: lock으로 보호하며 크기 반환");
    }

    /** 최대 용량. */
    public int capacity() {
        throw new UnsupportedOperationException("TODO: capacity 반환");
    }
}
