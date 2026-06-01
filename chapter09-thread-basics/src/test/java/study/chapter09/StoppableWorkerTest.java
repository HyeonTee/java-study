package study.chapter09;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StoppableWorker — volatile 정지 플래그로 협력적 종료")
class StoppableWorkerTest {

    @Test
    @DisplayName("시작 전에는 실행 중이 아니다")
    void 시작_전에는_실행중이_아니다() {
        StoppableWorker worker = new StoppableWorker(() -> { });
        assertFalse(worker.isRunning());
    }

    @Test
    @Timeout(5)
    @DisplayName("requestStop을 호출하면 워커 스레드가 종료된다 (올바르면 즉시, 틀리면 무한 루프→타임아웃)")
    void 정지_요청을_받으면_종료한다() throws InterruptedException {
        AtomicLong work = new AtomicLong(0);
        StoppableWorker worker = new StoppableWorker(work::incrementAndGet);

        Thread t = new Thread(worker);
        t.start();

        // 워커가 실제로 루프에 진입할 때까지 잠깐 기다린다 (상한 있는 대기).
        long deadline = System.nanoTime() + 2_000_000_000L; // 2초
        while (!worker.isRunning() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertTrue(worker.isRunning(), "워커가 루프에 진입하지 않았다");

        worker.requestStop();
        t.join();                       // @Timeout이 무한 루프를 실패로 전환

        assertFalse(t.isAlive());
        assertFalse(worker.isRunning(), "종료 후에는 실행 중이 아니어야 한다");
        assertTrue(worker.completedCycles() > 0, "정지 전에 최소 한 사이클은 돌았어야 한다");
    }
}
