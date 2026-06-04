package study.chapter17;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * 4가지 스레딩 모델을 <strong>{@link Executor} 팩토리 한 줄씩</strong>으로 — "진화는 서버 코드가 아니라 주입하는
 * Executor를 갈아끼우는 것"을 체득하는 자리.
 *
 * <p>핵심 통찰: {@link HttpServer}는 스레딩 모델을 <strong>모른다</strong>(연결을 {@code executor.execute}에 넘기는
 * 한 줄뿐). 그래서 모델 4종이 서버 코드를 한 줄도 안 바꾸고 갈린다 — "동시성 ⟂ 연결 루프" 직교성.
 *
 * <p>스레드풀·가상스레드는 ch11/ch13에서 <strong>이미 직접 만들었다</strong>. 여기선 다시 만들지 않고 <strong>선택</strong>을
 * 배운다 — 각 팩토리는 1~3줄짜리 표준 API 위임이다.
 */
public final class ThreadingStrategy {

    private ThreadingStrategy() {
    }

    /**
     * <strong>caller-runs</strong> — 호출 스레드에서 즉시 실행. accept 스레드가 연결을 직접 처리하므로 그 사이 다음
     * {@code accept}를 못 한다(진짜 직렬, 동시성 0). <strong>단위 테스트가 accept 루프를 결정적으로 검증</strong>하는 용도.
     *
     * <p>주의(README 박스): 이건 README의 "단일스레드 <em>서버 모델</em>"(별도 워커 1개 = {@code newSingleThreadExecutor},
     * accept와 처리가 분리)과는 <strong>다르다</strong>. caller-runs는 accept 자체가 막힌다.
     *
     * <p>힌트: {@code command -> command.run()} 형태의 {@code Executor} 반환.
     */
    public static Executor callerRuns() {
        throw new UnsupportedOperationException("TODO: command -> command.run() 반환(호출 스레드 즉시 실행, 동시성 0)");
    }

    /**
     * <strong>연결당 새 스레드</strong>(ch10 회수) — 풀 없음. 연결이 폭증하면 스레드 고갈(C10K 문제).
     *
     * <p>힌트: 매 {@code execute(r)}마다 {@code new Thread(r)}를 만들어 {@code setDaemon(true)} 후 {@code start}하는
     * {@code Executor}.
     */
    public static Executor threadPerConnection() {
        throw new UnsupportedOperationException("TODO: execute마다 new Thread(r) 만들어 setDaemon(true) 후 start하는 Executor");
    }

    /**
     * <strong>고정 스레드풀</strong>(ch11 회수) — 워커 {@code nThreads}개 재사용, 연결 &gt; 워커면 큐 대기.
     *
     * <p>힌트: 표준 {@code Executors.newFixedThreadPool(nThreads)} 반환(ch11에서 직접 만든 {@code MyThreadPool}의
     * 표준판 — 여기선 만들지 않고 선택). 블로킹 IO에서 모든 워커가 묶이면 풀 고갈이 일어남(README 진화표).
     */
    public static ExecutorService fixedPool(int nThreads) {
        throw new UnsupportedOperationException("TODO: Executors.newFixedThreadPool(nThreads) 반환(ch11 풀의 표준판)");
    }

    /**
     * <strong>연결당 가상 스레드</strong>(ch13 회수) — 풀링 안 함(가상 스레드는 싸다). IO 바운드 연결에 최적이고,
     * keep-alive의 긴 블로킹을 공짜로 흡수한다(고정 풀은 워커가 묶여 고갈).
     *
     * <p>힌트: {@code Executors.newVirtualThreadPerTaskExecutor()} 반환(ch13 회수 — <strong>풀링 금지</strong>가 핵심).
     * 가상스레드가 thread-per-connection의 부활인 이유는 README 진화표 참조.
     */
    public static ExecutorService virtualThreadPerConnection() {
        throw new UnsupportedOperationException("TODO: Executors.newVirtualThreadPerTaskExecutor() 반환(연결당 가상스레드, 풀링 금지)");
    }
}
