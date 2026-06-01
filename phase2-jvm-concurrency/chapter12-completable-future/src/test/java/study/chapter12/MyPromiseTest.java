package study.chapter12;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MyPromise — 콜백 등록 기반 미니 promise (수동 완료, 결정적)")
class MyPromiseTest {

    @Nested
    @DisplayName("complete / get")
    class CompleteGet {

        @Test
        void complete한_값을_get한다() throws Exception {
            MyPromise<Integer> p = new MyPromise<>();
            assertTrue(p.complete(7));
            assertTrue(p.isDone());
            assertEquals(7, p.get());
        }

        @Test
        void 두번째_complete는_무시된다() throws Exception {
            MyPromise<Integer> p = new MyPromise<>();
            assertTrue(p.complete(1));
            assertFalse(p.complete(2), "두 번째 complete는 false");
            assertEquals(1, p.get(), "값은 첫 완료로 고정");
        }

        @Test
        void completed_팩토리는_즉시_완료() throws Exception {
            assertEquals("hi", MyPromise.completed("hi").get());
        }

        @Test
        void 예외로_완료하면_get은_ExecutionException으로_감싼다() {
            MyPromise<Integer> p = new MyPromise<>();
            p.completeExceptionally(new IllegalStateException("boom"));
            ExecutionException ee = assertThrows(ExecutionException.class, p::get);
            assertInstanceOf(IllegalStateException.class, ee.getCause(), "원인은 getCause()");
        }
    }

    @Nested
    @DisplayName("thenApply — 완료 전/후 등록이 같은 결과로 수렴")
    class ThenApply {

        @Test
        void 완료_전_등록후_complete하면_변환값으로_발화() throws Exception {
            MyPromise<Integer> p = new MyPromise<>();
            MyPromise<String> mapped = p.thenApply(n -> "v" + n);   // 아직 미완료 — 등록만
            assertFalse(mapped.isDone(), "상류 완료 전엔 하류도 미완료");
            p.complete(7);
            assertEquals("v7", mapped.get());
        }

        @Test
        void 완료_후_등록하면_즉시_발화() throws Exception {
            MyPromise<Integer> p = new MyPromise<>();
            p.complete(7);
            MyPromise<String> mapped = p.thenApply(n -> "v" + n);   // 이미 완료 — 즉시
            assertEquals("v7", mapped.get());
        }

        @Test
        void 여러_단계_체인() throws Exception {
            MyPromise<Integer> p = new MyPromise<>();
            MyPromise<Integer> r = p.thenApply(n -> n + 1).thenApply(n -> n * 10);
            p.complete(2);
            assertEquals(30, r.get());
        }

        @Test
        void 예외_완료는_thenApply를_건너뛰고_전파() {
            MyPromise<Integer> p = new MyPromise<>();
            AtomicBoolean mapperRan = new AtomicBoolean(false);
            MyPromise<Integer> mapped = p.thenApply(n -> {
                mapperRan.set(true);
                return n + 1;
            });
            p.completeExceptionally(new IllegalStateException("boom"));
            assertFalse(mapperRan.get(), "예외 완료 시 변환 함수는 실행되지 않아야");
            ExecutionException ee = assertThrows(ExecutionException.class, mapped::get);
            assertInstanceOf(IllegalStateException.class, ee.getCause());
        }
    }

    @Nested
    @DisplayName("thenCompose — 중첩 promise 평탄화")
    class ThenCompose {

        @Test
        void 평탄화되어_내부값을_준다() throws Exception {
            MyPromise<Integer> p = new MyPromise<>();
            MyPromise<String> r = p.thenCompose(n -> MyPromise.completed("composed:" + n));
            p.complete(5);
            assertEquals("composed:5", r.get());   // CF<CF<>>가 아니라 CF<String>
        }

        @Test
        void 내부_promise가_나중에_완료돼도_전파() throws Exception {
            MyPromise<Integer> p = new MyPromise<>();
            MyPromise<String> inner = new MyPromise<>();
            MyPromise<String> r = p.thenCompose(n -> inner);
            p.complete(5);               // 바깥 완료 — 하지만 inner는 아직
            assertFalse(r.isDone(), "inner가 완료되기 전엔 결과도 미완료");
            inner.complete("late");
            assertEquals("late", r.get());
        }
    }

    @Nested
    @DisplayName("whenComplete — 등록 콜백 발화")
    class WhenComplete {

        @Test
        void 성공이면_값과_null예외로_호출() {
            MyPromise<Integer> p = new MyPromise<>();
            AtomicReference<Integer> seenValue = new AtomicReference<>();
            AtomicReference<Throwable> seenEx = new AtomicReference<>();
            p.whenComplete((v, ex) -> {
                seenValue.set(v);
                seenEx.set(ex);
            });
            p.complete(42);
            assertEquals(42, seenValue.get());
            assertEquals(null, seenEx.get());
        }
    }

    @Nested
    @DisplayName("get 블로킹 — 다른 스레드의 complete까지 기다린다")
    class BlockingGet {

        @Test
        @Timeout(5)
        void complete전_get은_블록되었다가_깨어난다() throws Exception {
            MyPromise<String> p = new MyPromise<>();
            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch returned = new CountDownLatch(1);
            AtomicReference<String> got = new AtomicReference<>();

            Thread t = new Thread(() -> {
                started.countDown();
                try {
                    got.set(p.get());
                    returned.countDown();
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
            t.start();
            started.await();
            assertFalse(returned.await(50, MILLISECONDS), "complete 전엔 get이 블록되어야 한다");
            p.complete("done");
            assertTrue(returned.await(2, SECONDS), "complete 후 get이 깨어나야 한다");
            assertEquals("done", got.get());
        }
    }
}
