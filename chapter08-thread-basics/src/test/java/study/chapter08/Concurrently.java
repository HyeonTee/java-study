package study.chapter08;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 테스트 전용 헬퍼 — {@code threads}개의 스레드가 같은 작업을 실행하되,
 * {@link CountDownLatch} 출발 게이트로 <strong>동시에 출발</strong>시켜 인터리빙(겹침)을
 * 극대화한다. 그래야 동기화가 빠진 잘못된 구현이 높은 확률로 깨진다.
 *
 * <p>모든 스레드가 끝날 때까지 기다린 뒤 리턴한다({@code join} 포함) — 그래서 호출한
 * 테스트는 리턴 직후 공유 객체의 최종 상태를 안전하게 읽을 수 있다.
 *
 * <p>이 클래스 자체는 채점 대상이 아니다(테스트 인프라).
 */
final class Concurrently {

    private Concurrently() {
    }

    /**
     * {@code threads}개의 스레드에서 {@code task}를 동시에 실행하고 전부 끝날 때까지 기다린다.
     */
    static void run(int threads, Runnable task) {
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);
        Thread[] workers = new Thread[threads];

        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                try {
                    startGate.await();   // 출발선에서 대기
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneGate.countDown();
                }
            });
            workers[i].start();
        }

        startGate.countDown();           // 일제히 출발 → 겹침 극대화

        try {
            if (!doneGate.await(30, TimeUnit.SECONDS)) {
                throw new AssertionError("테스트 스레드가 30초 안에 끝나지 않았다");
            }
            for (Thread w : workers) {
                w.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("테스트가 인터럽트되었다", e);
        }
    }
}
