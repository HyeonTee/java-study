package study.chapter15;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ParallelFanOut — 순서 보존 병렬 map")
class ParallelFanOutTest {

    @Nested
    @DisplayName("순서·값 보존 (결정적)")
    class OrderPreserving {

        @Test
        @Timeout(10)
        void 결과가_입력_순서대로_정확() {
            List<Integer> inputs = IntStream.rangeClosed(1, 500).boxed().toList();
            List<Integer> out = ParallelFanOut.parallelMap(inputs, n -> n * 2);
            assertEquals(inputs.stream().map(n -> n * 2).toList(), out);
        }

        @Test
        @Timeout(10)
        void 완료_순서가_섞여도_입력_순서로_반환() {
            // 무작위 지연으로 완료 순서를 흩뜨려도 결과 리스트는 입력 인덱스 순서여야 한다.
            Random rnd = new Random(42);
            List<Integer> inputs = IntStream.rangeClosed(0, 49).boxed().toList();
            Function<Integer, Integer> mapper = n -> {
                try {
                    Thread.sleep(rnd.nextInt(5));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return n * n;
            };
            assertEquals(inputs.stream().map(n -> n * n).toList(),
                    ParallelFanOut.parallelMap(inputs, mapper));
        }

        @Test
        @Timeout(5)
        void 빈_입력은_빈_결과() {
            assertTrue(ParallelFanOut.parallelMap(List.of(), n -> n).isEmpty());
        }
    }

    @Nested
    @DisplayName("실패 전파 (all-or-nothing)")
    class Failure {

        @Test
        @Timeout(5)
        void 한_원소_변환이_실패하면_전파() {
            List<Integer> inputs = List.of(1, 2, 3, 4, 5);
            assertThrows(RuntimeException.class,
                    () -> ParallelFanOut.parallelMap(inputs, n -> {
                        if (n == 3) {
                            throw new IllegalStateException("boom at 3");
                        }
                        return n * 2;
                    }));
        }
    }
}
