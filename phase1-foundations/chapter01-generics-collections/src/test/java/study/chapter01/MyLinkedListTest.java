package study.chapter01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MyLinkedList")
class MyLinkedListTest {

    @Nested
    @DisplayName("size / isEmpty")
    class SizeAndEmpty {

        @Test
        void 새로_만들면_size는_0이고_비어있다() {
            MyLinkedList<String> list = new MyLinkedList<>();
            assertEquals(0, list.size());
            assertTrue(list.isEmpty());
        }

        @Test
        void add_하면_size가_1증가한다() {
            MyLinkedList<String> list = new MyLinkedList<>();
            list.add("a");
            list.add("b");
            assertEquals(2, list.size());
            assertFalse(list.isEmpty());
        }
    }

    @Nested
    @DisplayName("addFirst / addLast / add")
    class AddVariants {

        @Test
        void addLast_후_get_순서() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.addLast(1);
            list.addLast(2);
            list.addLast(3);
            assertEquals(1, list.get(0));
            assertEquals(2, list.get(1));
            assertEquals(3, list.get(2));
        }

        @Test
        void addFirst는_head_앞에_넣는다() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.addFirst(1);
            list.addFirst(2);
            list.addFirst(3);
            // 3, 2, 1
            assertEquals(3, list.get(0));
            assertEquals(2, list.get(1));
            assertEquals(1, list.get(2));
        }

        @Test
        void add는_addLast와_동등하다() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            list.add(2);
            list.add(3);
            assertEquals(1, list.get(0));
            assertEquals(3, list.get(2));
        }

        @Test
        void 인덱스_삽입은_중간을_벌린다() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            list.add(2);
            list.add(3);
            list.add(1, 99); // [1, 99, 2, 3]
            assertEquals(4, list.size());
            assertEquals(1, list.get(0));
            assertEquals(99, list.get(1));
            assertEquals(2, list.get(2));
            assertEquals(3, list.get(3));
        }

        @Test
        void size_위치에_add하면_끝에_추가된다() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            list.add(1, 2);
            assertEquals(2, list.size());
            assertEquals(2, list.get(1));
        }

        @Test
        void 빈_리스트의_addFirst와_addLast는_같은_결과() {
            MyLinkedList<Integer> a = new MyLinkedList<>();
            a.addFirst(42);
            MyLinkedList<Integer> b = new MyLinkedList<>();
            b.addLast(42);
            assertEquals(1, a.size());
            assertEquals(1, b.size());
            assertEquals(42, a.get(0));
            assertEquals(42, b.get(0));
        }
    }

    @Nested
    @DisplayName("get / set / remove")
    class AccessAndMutate {

        @Test
        void 범위_밖_get은_IndexOutOfBoundsException() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
        }

        @Test
        void set은_기존값을_반환한다() {
            MyLinkedList<String> list = new MyLinkedList<>();
            list.add("a");
            list.add("b");
            String prev = list.set(0, "A");
            assertEquals("a", prev);
            assertEquals("A", list.get(0));
            assertEquals("b", list.get(1));
            assertEquals(2, list.size());
        }

        @Test
        void head_제거_후_새_head는_두번째_원소() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            list.add(2);
            list.add(3);
            int removed = list.remove(0);
            assertEquals(1, removed);
            assertEquals(2, list.size());
            assertEquals(2, list.get(0));
            assertEquals(3, list.get(1));
        }

        @Test
        void tail_제거_후_새_tail은_뒤에서_두번째() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            list.add(2);
            list.add(3);
            int removed = list.remove(2);
            assertEquals(3, removed);
            assertEquals(2, list.size());
            // 이후 add는 새 tail 뒤에 붙어야 함
            list.add(99);
            assertEquals(99, list.get(2));
        }

        @Test
        void 중간_제거는_앞뒤_노드를_다시_연결한다() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            list.add(2);
            list.add(3);
            list.add(4);
            int removed = list.remove(1);
            assertEquals(2, removed);
            assertEquals(List.of(1, 3, 4), toList(list));
        }

        @Test
        void 모두_제거하면_isEmpty() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            list.add(2);
            list.remove(0);
            list.remove(0);
            assertEquals(0, list.size());
            assertTrue(list.isEmpty());
        }
    }

    @Nested
    @DisplayName("indexOf / contains / clear / iterator")
    class SearchAndIterate {

        @Test
        void indexOf와_contains() {
            MyLinkedList<String> list = new MyLinkedList<>();
            list.add("a");
            list.add(null);
            list.add("b");
            assertEquals(0, list.indexOf("a"));
            assertEquals(1, list.indexOf(null));
            assertEquals(2, list.indexOf("b"));
            assertEquals(-1, list.indexOf("z"));
            assertTrue(list.contains(null));
            assertFalse(list.contains("z"));
        }

        @Test
        void clear_후_isEmpty() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            list.add(2);
            list.clear();
            assertTrue(list.isEmpty());
            // 다시 add 해도 정상
            list.add(10);
            assertEquals(10, list.get(0));
        }

        @Test
        void iterator는_head부터_tail까지_순회() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            list.add(2);
            list.add(3);
            List<Integer> visited = new ArrayList<>();
            for (int v : list) {
                visited.add(v);
            }
            assertEquals(List.of(1, 2, 3), visited);
        }

        @Test
        void 더_없는데_next하면_NoSuchElementException() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            Iterator<Integer> it = list.iterator();
            it.next();
            assertThrows(NoSuchElementException.class, it::next);
        }
    }

    @Nested
    @DisplayName("규모 테스트")
    class Scale {

        @Test
        void 만개를_addLast_해도_순서가_유지된다() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            for (int i = 0; i < 10_000; i++) {
                list.add(i);
            }
            assertEquals(10_000, list.size());
            assertEquals(0, list.get(0));
            assertEquals(9_999, list.get(9_999));
        }
    }

    @Nested
    @DisplayName("엣지 케이스 — 인덱스 경계")
    class IndexBoundary {

        @Test
        void add_int_T에_음수_인덱스는_IndexOutOfBoundsException() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, 99));
        }

        @Test
        void add_int_T에_size보다_큰_인덱스는_IndexOutOfBoundsException() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            list.add(2);
            assertThrows(IndexOutOfBoundsException.class, () -> list.add(3, 99));
            assertThrows(IndexOutOfBoundsException.class, () -> list.add(99, 99));
        }

        @Test
        void 빈_리스트에_add_0은_정상_동작() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(0, 42);
            assertEquals(1, list.size());
            assertEquals(42, list.get(0));
        }

        @Test
        void 빈_리스트에_add_1은_IndexOutOfBoundsException() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            assertThrows(IndexOutOfBoundsException.class, () -> list.add(1, 42));
        }

        @Test
        void 빈_리스트에_set은_IndexOutOfBoundsException() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            assertThrows(IndexOutOfBoundsException.class, () -> list.set(0, 42));
        }

        @Test
        void 빈_리스트에_remove는_IndexOutOfBoundsException() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        }

        @Test
        void 범위_밖_set은_IndexOutOfBoundsException() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, 99));
            assertThrows(IndexOutOfBoundsException.class, () -> list.set(1, 99));
        }
    }

    @Nested
    @DisplayName("엣지 케이스 — 객체 동치성")
    class EqualsContract {

        @Test
        void indexOf와_contains는_equals로_비교한다() {
            MyLinkedList<String> list = new MyLinkedList<>();
            list.add(new String("hello"));
            assertEquals(0, list.indexOf(new String("hello")));
            assertTrue(list.contains(new String("hello")));
        }

        @Test
        void indexOf는_같은_값이_여러_개라도_첫번째_인덱스() {
            MyLinkedList<String> list = new MyLinkedList<>();
            list.add("a");
            list.add("b");
            list.add("a");
            list.add("a");
            assertEquals(0, list.indexOf("a"));
        }

        @Test
        void null이_여러_개여도_indexOf는_첫번째_null() {
            MyLinkedList<String> list = new MyLinkedList<>();
            list.add("a");
            list.add(null);
            list.add("b");
            list.add(null);
            assertEquals(1, list.indexOf(null));
        }
    }

    @Nested
    @DisplayName("엣지 케이스 — iterator")
    class IteratorEdge {

        @Test
        void hasNext는_멱등이라_여러번_호출해도_cursor가_안_움직인다() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            Iterator<Integer> it = list.iterator();
            assertTrue(it.hasNext());
            assertTrue(it.hasNext());
            assertTrue(it.hasNext());
            assertEquals(1, it.next());
            assertFalse(it.hasNext());
            assertFalse(it.hasNext());
        }

        @Test
        void 빈_리스트_iterator의_next는_NoSuchElementException() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            Iterator<Integer> it = list.iterator();
            assertFalse(it.hasNext());
            assertThrows(NoSuchElementException.class, it::next);
        }

        @Test
        void 단일_원소_iterator_시나리오() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(42);
            Iterator<Integer> it = list.iterator();
            assertTrue(it.hasNext());
            assertEquals(42, it.next());
            assertFalse(it.hasNext());
            assertThrows(NoSuchElementException.class, it::next);
        }
    }

    @Nested
    @DisplayName("복합 시나리오")
    class CombinedScenario {

        @Test
        void 단일_원소를_add후_remove하면_다시_비어있다() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(42);
            assertEquals(42, list.remove(0));
            assertEquals(0, list.size());
            assertTrue(list.isEmpty());
            list.add(100);
            assertEquals(100, list.get(0));
        }

        @Test
        void clear_후에도_정상_동작() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            for (int i = 0; i < 5; i++) list.add(i);
            list.clear();

            assertEquals(0, list.size());
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));

            for (int i = 0; i < 3; i++) list.add(i * 10);
            assertEquals(3, list.size());
            assertEquals(0, list.get(0));
            assertEquals(20, list.get(2));
        }

        @Test
        void 앞에서_size번_remove_0을_반복하면_비어진다() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            for (int i = 0; i < 10; i++) list.add(i);
            for (int i = 0; i < 10; i++) {
                assertEquals(i, list.remove(0));
            }
            assertTrue(list.isEmpty());
        }

        @Test
        void 맨_뒤에서_size번_remove를_반복하면_비어진다() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            for (int i = 0; i < 10; i++) list.add(i);
            for (int i = 9; i >= 0; i--) {
                assertEquals(i, list.remove(list.size() - 1));
            }
            assertTrue(list.isEmpty());
        }

        @Test
        void add_remove_add_사이클에서도_순서가_정확하다() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1);
            list.add(2);
            list.remove(0);
            list.add(3);
            list.add(0, 0);
            list.remove(1);
            assertEquals(2, list.size());
            assertEquals(0, list.get(0));
            assertEquals(3, list.get(1));
        }

        @Test
        void set은_size를_바꾸지_않는다() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.add(1); list.add(2); list.add(3);
            int before = list.size();
            list.set(1, 99);
            assertEquals(before, list.size());
        }
    }

    @Nested
    @DisplayName("LinkedList 특화 — addFirst/addLast 혼용")
    class FirstLastMix {

        @Test
        void addFirst와_addLast를_번갈아_호출() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.addLast(2);    // [2]
            list.addFirst(1);   // [1, 2]
            list.addLast(3);    // [1, 2, 3]
            list.addFirst(0);   // [0, 1, 2, 3]
            list.addLast(4);    // [0, 1, 2, 3, 4]

            assertEquals(5, list.size());
            assertEquals(List.of(0, 1, 2, 3, 4), toList(list));
        }

        @Test
        void addFirst만_n번_호출하면_역순() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            for (int i = 0; i < 5; i++) list.addFirst(i);   // [4,3,2,1,0]
            assertEquals(List.of(4, 3, 2, 1, 0), toList(list));
        }

        @Test
        void 빈_리스트의_addFirst_후_remove() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            list.addFirst(1);
            assertEquals(1, list.remove(0));
            assertTrue(list.isEmpty());
            // 그 다음 addLast도 정상
            list.addLast(2);
            assertEquals(2, list.get(0));
        }
    }

    @Nested
    @DisplayName("LinkedList 특화 — 양방향 탐색 분기")
    class TwoWayTraversal {

        @Test
        void 큰_리스트의_끝쪽_인덱스_접근도_정확() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            for (int i = 0; i < 1000; i++) list.add(i);
            // tail 분기를 타는 인덱스들 (size/2 초과)
            assertEquals(999, list.get(999));
            assertEquals(998, list.get(998));
            assertEquals(750, list.get(750));
            assertEquals(501, list.get(501));
            // head 분기 인덱스
            assertEquals(0, list.get(0));
            assertEquals(1, list.get(1));
            assertEquals(499, list.get(499));
        }

        @Test
        void 큰_리스트의_중간_set도_정확() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            for (int i = 0; i < 1000; i++) list.add(i);

            assertEquals(750, list.set(750, -1));
            assertEquals(-1, list.get(750));

            assertEquals(100, list.set(100, -2));
            assertEquals(-2, list.get(100));

            // 다른 위치는 그대로
            assertEquals(0, list.get(0));
            assertEquals(999, list.get(999));
        }

        @Test
        void 큰_리스트의_끝쪽_remove도_정확() {
            MyLinkedList<Integer> list = new MyLinkedList<>();
            for (int i = 0; i < 100; i++) list.add(i);

            // tail 직전 인덱스 제거
            assertEquals(98, list.remove(98));
            assertEquals(99, list.get(98));   // 마지막이 한 칸 당겨짐
            assertEquals(99, list.size());
        }
    }

    private static <T> List<T> toList(MyLinkedList<T> list) {
        List<T> out = new ArrayList<>();
        for (T v : list) out.add(v);
        return out;
    }
}
