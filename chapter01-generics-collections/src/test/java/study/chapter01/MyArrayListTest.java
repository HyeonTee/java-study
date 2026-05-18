package study.chapter01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MyArrayList")
class MyArrayListTest {

    @Nested
    @DisplayName("size / isEmpty")
    class SizeAndEmpty {

        @Test
        void 새로_만들면_size는_0이고_비어있다() {
            MyList<String> list = new MyArrayList<>();
            assertEquals(0, list.size());
            assertTrue(list.isEmpty());
        }

        @Test
        void add_하면_size가_1증가한다() {
            MyList<String> list = new MyArrayList<>();
            list.add("a");
            assertEquals(1, list.size());
            assertFalse(list.isEmpty());
            list.add("b");
            assertEquals(2, list.size());
        }
    }

    @Nested
    @DisplayName("add / get")
    class AddAndGet {

        @Test
        void 끝에_add한_원소를_get할_수_있다() {
            MyList<Integer> list = new MyArrayList<>();
            list.add(10);
            list.add(20);
            list.add(30);
            assertEquals(10, list.get(0));
            assertEquals(20, list.get(1));
            assertEquals(30, list.get(2));
        }

        @Test
        void 인덱스_삽입은_뒤쪽_원소를_밀어낸다() {
            MyList<Integer> list = new MyArrayList<>();
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
            MyList<Integer> list = new MyArrayList<>();
            list.add(1);
            list.add(2);
            list.add(2, 3); // index == size 허용
            assertEquals(3, list.get(2));
        }

        @Test
        void 음수_인덱스에_get하면_IndexOutOfBoundsException() {
            MyList<Integer> list = new MyArrayList<>();
            list.add(1);
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        }

        @Test
        void size_이상_인덱스에_get하면_IndexOutOfBoundsException() {
            MyList<Integer> list = new MyArrayList<>();
            list.add(1);
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(99));
        }

        @Test
        void null도_저장하고_꺼낼_수_있다() {
            MyList<String> list = new MyArrayList<>();
            list.add(null);
            list.add("x");
            assertNull(list.get(0));
            assertEquals("x", list.get(1));
        }
    }

    @Nested
    @DisplayName("set / remove")
    class SetAndRemove {

        @Test
        void set은_기존값을_반환하고_새값을_저장한다() {
            MyList<String> list = new MyArrayList<>();
            list.add("a");
            list.add("b");
            String prev = list.set(1, "B");
            assertEquals("b", prev);
            assertEquals("B", list.get(1));
            assertEquals(2, list.size()); // 크기는 그대로
        }

        @Test
        void remove는_제거된_값을_반환하고_size가_감소한다() {
            MyList<Integer> list = new MyArrayList<>();
            list.add(10);
            list.add(20);
            list.add(30);
            int removed = list.remove(1);
            assertEquals(20, removed);
            assertEquals(2, list.size());
            assertEquals(10, list.get(0));
            assertEquals(30, list.get(1));
        }

        @Test
        void 마지막_원소를_remove해도_정상_동작() {
            MyList<Integer> list = new MyArrayList<>();
            list.add(1);
            list.add(2);
            assertEquals(2, list.remove(1));
            assertEquals(1, list.size());
            assertEquals(1, list.get(0));
        }

        @Test
        void 범위_밖_remove는_IndexOutOfBoundsException() {
            MyList<Integer> list = new MyArrayList<>();
            list.add(1);
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(1));
        }

        @Test
        void 배열이_꽉_찬_상태에서_remove해도_터지지_않는다() {
            // capacity == size 인 상황에서 시프트 루프가 배열 끝을 넘어 읽으면
            // ArrayIndexOutOfBoundsException 발생. 회귀 방지.
            MyArrayList<Integer> list = new MyArrayList<>(3);
            list.add(1);
            list.add(2);
            list.add(3);
            assertEquals(1, list.remove(0));
            assertEquals(2, list.size());
            assertEquals(2, list.get(0));
            assertEquals(3, list.get(1));
        }
    }

    @Nested
    @DisplayName("indexOf / contains")
    class IndexOfAndContains {

        @Test
        void indexOf는_첫_매칭_인덱스를_반환() {
            MyList<String> list = new MyArrayList<>();
            list.add("a");
            list.add("b");
            list.add("a");
            assertEquals(0, list.indexOf("a"));
            assertEquals(1, list.indexOf("b"));
            assertEquals(-1, list.indexOf("z"));
        }

        @Test
        void indexOf는_null도_검색_가능() {
            MyList<String> list = new MyArrayList<>();
            list.add("a");
            list.add(null);
            list.add("c");
            assertEquals(1, list.indexOf(null));
        }

        @Test
        void contains는_indexOf와_일관된다() {
            MyList<Integer> list = new MyArrayList<>();
            list.add(1);
            list.add(2);
            assertTrue(list.contains(1));
            assertTrue(list.contains(2));
            assertFalse(list.contains(3));
        }

        @Test
        void contains는_size_범위_밖의_빈_슬롯과_혼동하지_않는다() {
            // capacity 10인 기본 생성자. add("a") 한 번 → 슬롯 1~9는 null로 남음.
            // contains(null)이 이 null 슬롯을 검출하면 안 됨. 회귀 방지.
            MyList<String> list = new MyArrayList<>();
            list.add("a");
            assertFalse(list.contains(null));
        }

        @Test
        void indexOf와_contains는_참조가_아닌_equals로_비교한다() {
            // 참조 비교(==)면 실패하는 케이스. new String으로 다른 인스턴스 만듦.
            MyList<String> list = new MyArrayList<>();
            list.add(new String("hello"));
            assertEquals(0, list.indexOf(new String("hello")));
            assertTrue(list.contains(new String("hello")));
        }
    }

    @Nested
    @DisplayName("clear / 용량 확장")
    class ClearAndCapacity {

        @Test
        void clear_후에는_size가_0이고_비어있다() {
            MyList<Integer> list = new MyArrayList<>();
            list.add(1);
            list.add(2);
            list.clear();
            assertEquals(0, list.size());
            assertTrue(list.isEmpty());
        }

        @Test
        void 초기_용량을_넘는_add도_문제없이_동작한다() {
            // 기본 용량(10)을 훨씬 넘는 1000개를 넣어도 모두 잘 들어가야 한다.
            MyList<Integer> list = new MyArrayList<>();
            for (int i = 0; i < 1000; i++) {
                list.add(i);
            }
            assertEquals(1000, list.size());
            for (int i = 0; i < 1000; i++) {
                assertEquals(i, list.get(i));
            }
        }

        @Test
        void 초기용량_0으로_시작해도_add가_동작한다() {
            MyArrayList<Integer> list = new MyArrayList<>(0);
            list.add(1);
            list.add(2);
            assertEquals(2, list.size());
            assertEquals(1, list.get(0));
        }
    }

    @Nested
    @DisplayName("Iterator")
    class IteratorTest {

        @Test
        void 빈_리스트의_iterator는_hasNext가_false() {
            MyList<Integer> list = new MyArrayList<>();
            assertFalse(list.iterator().hasNext());
        }

        @Test
        void iterator는_삽입_순서대로_순회한다() {
            MyList<Integer> list = new MyArrayList<>();
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
            MyList<Integer> list = new MyArrayList<>();
            list.add(1);
            Iterator<Integer> it = list.iterator();
            it.next();
            assertThrows(NoSuchElementException.class, it::next);
        }
    }
}
