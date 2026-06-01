package study.chapter03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MyBinarySearchTree — 이진탐색트리")
class MyBinarySearchTreeTest {

    private static MyBinarySearchTree<Integer> treeOf(int... values) {
        MyBinarySearchTree<Integer> t = new MyBinarySearchTree<>();
        for (int v : values) {
            t.insert(v);
        }
        return t;
    }

    @Nested
    @DisplayName("insert / contains")
    class InsertContains {

        @Test
        void insert한_값은_contains() {
            MyBinarySearchTree<Integer> t = treeOf(5, 3, 8);
            assertTrue(t.contains(5));
            assertTrue(t.contains(3));
            assertFalse(t.contains(99));
            assertEquals(3, t.size());
        }

        @Test
        void 중복_insert는_false이고_size불변() {
            MyBinarySearchTree<Integer> t = treeOf(5);
            assertFalse(t.insert(5));
            assertEquals(1, t.size());
        }

        @Test
        void null_insert는_NPE() {
            assertThrows(NullPointerException.class, () -> new MyBinarySearchTree<Integer>().insert(null));
        }
    }

    @Nested
    @DisplayName("중위순회 = 정렬 순서")
    class Inorder {

        @Test
        void 어떤_순서로_넣어도_inorder는_정렬됨() {
            assertEquals(List.of(1, 3, 4, 5, 8), treeOf(5, 3, 8, 1, 4).inorder());
            assertEquals(List.of(1, 3, 4, 5, 8), treeOf(1, 3, 4, 5, 8).inorder());  // 정렬 입력
        }

        @Test
        void 랜덤_1000개_삽입후_inorder는_완전정렬() {
            MyBinarySearchTree<Integer> t = new MyBinarySearchTree<>();
            List<Integer> expected = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                expected.add(i);
            }
            List<Integer> shuffled = new ArrayList<>(expected);
            java.util.Collections.shuffle(shuffled, new Random(42));
            shuffled.forEach(t::insert);
            assertEquals(expected, t.inorder());
        }
    }

    @Nested
    @DisplayName("delete — 3가지 경우")
    class Delete {

        @Test
        void 리프_삭제() {
            MyBinarySearchTree<Integer> t = treeOf(5, 3, 8, 1);
            assertTrue(t.delete(1));
            assertEquals(List.of(3, 5, 8), t.inorder());
        }

        @Test
        void 자식_하나_삭제() {
            MyBinarySearchTree<Integer> t = treeOf(5, 3, 8, 1);  // 3은 왼자식 1만 가짐
            assertTrue(t.delete(3));
            assertEquals(List.of(1, 5, 8), t.inorder());
        }

        @Test
        void 자식_둘_삭제_후속자교체() {
            MyBinarySearchTree<Integer> t = treeOf(5, 3, 8, 1, 4);  // 3은 자식 1,4 둘
            assertTrue(t.delete(3));
            assertEquals(List.of(1, 4, 5, 8), t.inorder());
        }

        @Test
        void 루트_삭제() {
            MyBinarySearchTree<Integer> t = treeOf(5, 3, 8);
            assertTrue(t.delete(5));
            assertEquals(List.of(3, 8), t.inorder());
        }

        @Test
        void 없는_값_삭제는_false() {
            assertFalse(treeOf(5, 3, 8).delete(99));
        }

        @Test
        void 삭제_반복후에도_정렬_유지() {
            MyBinarySearchTree<Integer> t = new MyBinarySearchTree<>();
            for (int i = 0; i < 200; i++) {
                t.insert(i);
            }
            for (int i = 0; i < 200; i += 2) {
                t.delete(i);  // 짝수 삭제
            }
            List<Integer> odds = new ArrayList<>();
            for (int i = 1; i < 200; i += 2) {
                odds.add(i);
            }
            assertEquals(odds, t.inorder());
        }
    }

    @Nested
    @DisplayName("min / max / height / 경계")
    class MinMaxHeight {

        @Test
        void min과_max() {
            MyBinarySearchTree<Integer> t = treeOf(5, 3, 8, 1, 9);
            assertEquals(1, t.min());
            assertEquals(9, t.max());
        }

        @Test
        void 빈_트리_min_max는_예외() {
            MyBinarySearchTree<Integer> t = new MyBinarySearchTree<>();
            assertThrows(NoSuchElementException.class, t::min);
            assertThrows(NoSuchElementException.class, t::max);
        }

        @Test
        @DisplayName("정렬된 입력은 편향되어 높이가 size-1 (연결 리스트로 퇴화)")
        void 정렬입력은_편향() {
            MyBinarySearchTree<Integer> t = treeOf(1, 2, 3, 4, 5, 6, 7);
            assertEquals(6, t.height());   // 간선 6개 = 한쪽으로 치우침
        }

        @Test
        void 균형있게_넣으면_높이가_낮다() {
            MyBinarySearchTree<Integer> t = treeOf(4, 2, 6, 1, 3, 5, 7);
            assertEquals(2, t.height());
        }

        @Test
        void 빈_트리_높이는_minus1() {
            assertEquals(-1, new MyBinarySearchTree<Integer>().height());
        }
    }

    @Nested
    @DisplayName("제네릭 바운드 — 상속 타입도 동작")
    class GenericBound {

        @Test
        void 상속받은_Comparable로도_트리를_만든다() {
            // Dog은 Comparable<Animal>을 상속 — <T extends Comparable<? super T>> 바운드라야 컴파일된다.
            MyBinarySearchTree<Dog> tree = new MyBinarySearchTree<>();
            tree.insert(new Dog(3));
            tree.insert(new Dog(1));
            tree.insert(new Dog(2));
            assertEquals(1, tree.min().weight);
            assertEquals(3, tree.max().weight);
        }
    }
}
