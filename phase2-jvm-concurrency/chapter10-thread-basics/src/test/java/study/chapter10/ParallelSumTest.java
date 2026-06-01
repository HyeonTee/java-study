package study.chapter10;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ParallelSum — 여러 스레드로 나눠 합산 (공유 상태 없음 → 결정적)")
class ParallelSumTest {

    @Nested
    @DisplayName("정확성 (스레드 수와 무관하게 항상 같은 합)")
    class Correctness {

        @Test
        void 한_스레드로_합산하면_순차합과_같다() {
            int[] numbers = {1, 2, 3, 4, 5};
            assertEquals(15L, ParallelSum.sum(numbers, 1));
        }

        @Test
        void 여러_스레드로_나눠도_합은_같다() {
            int[] numbers = new int[1000];
            for (int i = 0; i < numbers.length; i++) {
                numbers[i] = i + 1;          // 1..1000 → 합 500500
            }
            assertEquals(500500L, ParallelSum.sum(numbers, 4));
            assertEquals(500500L, ParallelSum.sum(numbers, 7));   // 균등 분할이 안 떨어지는 수
            assertEquals(500500L, ParallelSum.sum(numbers, 16));
        }

        @Test
        void 음수가_섞여도_정확하다() {
            int[] numbers = {-5, 10, -3, 8, -1};
            assertEquals(9L, ParallelSum.sum(numbers, 3));
        }

        @Test
        @Timeout(5)
        void 큰_배열도_int_오버플로_없이_long으로_합산한다() {
            int[] numbers = new int[1_000_000];
            java.util.Arrays.fill(numbers, 3000);  // 3000 * 1_000_000 = 30억 > Integer.MAX_VALUE
            assertEquals(3_000_000_000L, ParallelSum.sum(numbers, 8));
        }
    }

    @Nested
    @DisplayName("경계 조건")
    class Edge {

        @Test
        void 빈_배열의_합은_0() {
            assertEquals(0L, ParallelSum.sum(new int[0], 4));
        }

        @Test
        void 스레드_수가_원소_수보다_많아도_된다() {
            int[] numbers = {10, 20, 30};
            assertEquals(60L, ParallelSum.sum(numbers, 8));   // 빈 구간 스레드가 생겨도 OK
        }

        @Test
        void 스레드_수가_1미만이면_예외() {
            assertThrows(IllegalArgumentException.class, () -> ParallelSum.sum(new int[]{1}, 0));
        }
    }
}
