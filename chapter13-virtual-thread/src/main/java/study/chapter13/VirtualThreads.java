package study.chapter13;

import java.util.List;
import java.util.function.IntConsumer;

/**
 * 가상 스레드 입문 — "작업마다 스레드"가 다시 싸진 세계.
 *
 * <p>ch11에서는 스레드가 비싸서 <strong>풀링</strong>으로 N개를 재사용했다. 가상 스레드는 OS 스레드가
 * 아니라 JVM이 스케줄링하는 경량 스레드라, 수만 개를 만들어도 괜찮다 — 그래서 풀을 버리고 <strong>작업마다
 * 새 스레드</strong>(thread-per-task)로 돌아간다. API는 ch10의 {@code Thread}/{@code join}과 <strong>똑같고</strong>,
 * 달라진 건 <strong>생성 방식과 비용</strong>뿐이다.
 *
 * <p>생성 3대 경로(모두 결과는 가상 스레드, {@code isVirtual()==true}):
 * <ul>
 *   <li>{@code Thread.ofVirtual().start(r)} / {@code .name(n).unstarted(r)} — 빌더</li>
 *   <li>{@code Thread.startVirtualThread(r)} — 단축형(즉시 시작)</li>
 *   <li>{@code Executors.newVirtualThreadPerTaskExecutor()} — 작업마다 새 스레드(풀링 아님!)</li>
 * </ul>
 *
 * <p>경량성은 "스레드가 몇 개냐/얼마나 빠르냐"로 단언하지 않는다(비결정적·환경의존). 대신 "요청한 N개가
 * <strong>전부 완료됐다</strong>(카운터 == N)"는 불변식으로만 검증한다.
 */
public final class VirtualThreads {

    private VirtualThreads() {
    }

    /**
     * 각 작업을 <strong>별도의 가상 스레드</strong>로 실행하고, 모든 스레드가 끝날 때까지 기다린 뒤 반환한다
     * (작업마다 thread-per-task + 전부 join).
     *
     * <p>힌트: 각 {@code Runnable}마다 {@link Thread#startVirtualThread(Runnable)}로 띄워 리스트에 모으고
     * (start 따로 / join 따로 — ch10 ParallelSum 복습), 전부 {@code join()}하라. {@code join()}의
     * {@link InterruptedException}은 {@code Thread.currentThread().interrupt()}로 상태 복원 후
     * {@link RuntimeException}으로 감싸 던진다(ch04 wrap-and-rethrow).
     *
     * @throws NullPointerException {@code tasks} 또는 그 원소가 null이면
     */
    public static void runAll(List<Runnable> tasks) {
        throw new UnsupportedOperationException(
                "TODO: 각 작업을 가상 스레드로 띄워 모으고 전부 join (InterruptedException은 wrap-and-rethrow)");
    }

    /**
     * {@code count}개의 가상 스레드를 띄워 각자 {@code body.accept(i)}(i = 0..count-1)를 정확히 한 번씩
     * 실행시키고, 모두 끝나면 반환한다. 경량성 검증용 — {@code count}가 수만이어도 동작해야 한다.
     *
     * @throws IllegalArgumentException {@code count < 0}
     * @throws NullPointerException     {@code body}가 null이면
     */
    public static void runManyVirtual(int count, IntConsumer body) {
        throw new UnsupportedOperationException(
                "TODO: 0..count-1 각 i마다 가상 스레드로 body.accept(i) 실행 후 전부 join (runAll 재사용 가능)");
    }

    /**
     * 현재 실행 중인 스레드가 가상 스레드이면 {@code true}.
     *
     * <p>힌트: {@code Thread.currentThread().isVirtual()}.
     */
    public static boolean runningOnVirtualThread() {
        throw new UnsupportedOperationException("TODO: 현재 스레드가 가상 스레드인지 반환");
    }

    /**
     * {@code name}을 가진 <strong>시작되지 않은</strong> 가상 스레드를 만들어 반환한다(호출자가 {@code start()}
     * 해야 실행). 반환된 스레드는 {@link Thread#isVirtual()}이 {@code true}여야 한다.
     *
     * <p>힌트: {@code Thread.ofVirtual().name(name).unstarted(task)}.
     *
     * @throws NullPointerException {@code name} 또는 {@code task}가 null이면
     */
    public static Thread newNamedVirtual(String name, Runnable task) {
        throw new UnsupportedOperationException(
                "TODO: Thread.ofVirtual().name(name).unstarted(task)로 미시작 가상 스레드 생성");
    }
}
