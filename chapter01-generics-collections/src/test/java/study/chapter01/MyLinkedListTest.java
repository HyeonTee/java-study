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

    private static <T> List<T> toList(MyLinkedList<T> list) {
        List<T> out = new ArrayList<>();
        for (T v : list) out.add(v);
        return out;
    }
}
