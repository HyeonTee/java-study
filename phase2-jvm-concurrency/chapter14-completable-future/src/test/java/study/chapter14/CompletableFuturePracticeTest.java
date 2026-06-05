package study.chapter14;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CompletableFuturePractice — 표준 CF 파이프라인·예외 복구 (join으로 결과만 단언)")
@Timeout(10)
class CompletableFuturePracticeTest {

    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    @AfterEach
    void tearDown() {
        pool.shutdownNow();
    }

    @Nested
    @DisplayName("파이프라인 조립")
    class Pipeline {

        @Test
        void runAsync_후_join하면_결과() {
            CompletableFuture<Integer> cf = CompletableFuturePractice.runAsync(() -> 21, pool);
            assertEquals(21, cf.join());
        }

        @Test
        void mapResult는_값을_변환() {
            CompletableFuture<String> cf = CompletableFuturePractice.mapResult(
                    CompletableFuture.completedFuture(20), n -> "n=" + (n + 1));
            assertEquals("n=21", cf.join());
        }

        @Test
        void flatMapResult는_평탄화() {
            CompletableFuture<String> cf = CompletableFuturePractice.flatMapResult(
                    CompletableFuture.completedFuture(3),
                    n -> CompletableFuture.completedFuture("id-" + n));
            assertEquals("id-3", cf.join());   // CF<CF<String>>가 아님
        }

        @Test
        void combine은_두_결과를_합친다() {
            CompletableFuture<Integer> r = CompletableFuturePractice.combine(
                    CompletableFuture.completedFuture(20),
                    CompletableFuture.completedFuture(22),
                    Integer::sum);
            assertEquals(42, r.join());
        }

        @Test
        void allOfResults는_순서대로_모은다() {
            List<CompletableFuture<Integer>> futures = List.of(
                    CompletableFuture.completedFuture(1),
                    CompletableFuture.completedFuture(2),
                    CompletableFuture.completedFuture(3));
            assertEquals(List.of(1, 2, 3), CompletableFuturePractice.allOfResults(futures).join());
        }
    }

    @Nested
    @DisplayName("예외 전파·복구")
    class ErrorHandling {

        private static CompletableFuture<Integer> failing() {
            return CompletableFuture.failedFuture(new IllegalStateException("boom"));
        }

        @Test
        void recover는_실패를_fallback으로() {
            assertEquals(-1, CompletableFuturePractice.recover(failing(), -1).join());
        }

        @Test
        void recover는_성공이면_원래값_통과() {
            assertEquals(99, CompletableFuturePractice.recover(
                    CompletableFuture.completedFuture(99), -1).join());
        }

        @Test
        void handleResult는_성공_실패_모두_처리() {
            assertEquals("recovered", CompletableFuturePractice.handleResult(
                    failing(), (v, ex) -> ex != null ? "recovered" : "ok:" + v).join());
            assertEquals("ok:5", CompletableFuturePractice.handleResult(
                    CompletableFuture.completedFuture(5), (v, ex) -> ex != null ? "recovered" : "ok:" + v).join());
        }

        @Test
        void describe는_CompletionException을_언래핑해_원인_메시지() {
            assertEquals("boom", CompletableFuturePractice.describe(failing()).join());
        }

        @Test
        void describe는_성공이면_ok_접두() {
            assertEquals("ok:7", CompletableFuturePractice.describe(
                    CompletableFuture.completedFuture(7)).join());
        }

        @Test
        void join은_CompletionException으로_감싼다() {
            CompletableFuture<Integer> f = failing();
            CompletionException ce = org.junit.jupiter.api.Assertions.assertThrows(
                    CompletionException.class, f::join);
            assertTrue(ce.getCause() instanceof IllegalStateException);
        }
    }
}
