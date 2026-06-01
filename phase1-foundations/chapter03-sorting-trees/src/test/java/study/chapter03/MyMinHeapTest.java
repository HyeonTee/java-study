package study.chapter03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MyMinHeap — 배열 기반 우선순위 큐")
class MyMinHeapTest {

    @Nested
    @DisplayName("offer / peek")
    class OfferPeek {

        @Test
        void peek은_최솟값() {
            MyMinHeap<Integer> h = new MyMinHeap<>();
            h.offer(5);
            h.offer(3);
            h.offer(8);
            assertEquals(3, h.peek());
            assertEquals(3, h.size());
        }

        @Test
        void peek은_제거하지_않는다() {
            MyMinHeap<Integer> h = new MyMinHeap<>();
            h.offer(5);
            h.offer(3);
            assertEquals(3, h.peek());
            assertEquals(3, h.peek());   // 두 번 호출해도 동일
            assertEquals(2, h.size());   // 크기 불변
        }

        @Test
        void null_offer는_NPE() {
            assertThrows(NullPointerException.class, () -> new MyMinHeap<Integer>().offer(null));
        }
    }

    @Nested
    @DisplayName("poll은 오름차순 (힙 속성)")
    class Poll {

        @Test
        void poll_순서는_오름차순() {
            MyMinHeap<Integer> h = new MyMinHeap<>();
            for (int v : new int[]{5, 1, 8, 3, 9, 2}) {
                h.offer(v);
            }
            List<Integer> out = new ArrayList<>();
            while (h.size() > 0) {
                out.add(h.poll());
            }
            assertEquals(List.of(1, 2, 3, 5, 8, 9), out);
        }

        @Test
        void 중복값도_정확히_poll() {
            MyMinHeap<Integer> h = new MyMinHeap<>();
            for (int v : new int[]{3, 1, 3, 1}) {
                h.offer(v);
            }
            assertEquals(List.of(1, 1, 3, 3), List.of(h.poll(), h.poll(), h.poll(), h.poll()));
        }

        @Test
        @DisplayName("용량(16)을 넘겨 grow가 일어나도 정렬 유지 — 랜덤 1000개")
        void 대량_offer후_poll_오름차순() {
            MyMinHeap<Integer> h = new MyMinHeap<>();
            List<Integer> expected = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                expected.add(i);
            }
            List<Integer> shuffled = new ArrayList<>(expected);
            Collections.shuffle(shuffled, new Random(42));
            shuffled.forEach(h::offer);

            List<Integer> out = new ArrayList<>();
            while (h.size() > 0) {
                out.add(h.poll());
            }
            assertEquals(expected, out);
        }
    }

    @Nested
    @DisplayName("빈 힙 경계")
    class Empty {

        @Test
        void 빈_힙_peek_poll은_예외() {
            MyMinHeap<Integer> h = new MyMinHeap<>();
            assertThrows(NoSuchElementException.class, h::peek);
            assertThrows(NoSuchElementException.class, h::poll);
        }

        @Test
        void offer후_poll하면_다시_빈다() {
            MyMinHeap<Integer> h = new MyMinHeap<>();
            h.offer(7);
            assertEquals(7, h.poll());
            assertTrue(h.isEmpty());
        }
    }
}
