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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MyTreeMap — 정렬 맵 (ch02 HashMap 대비)")
class MyTreeMapTest {

    @Nested
    @DisplayName("put / get (ch02와 동일 계약)")
    class PutGet {

        @Test
        void put한_값을_get한다() {
            MyTreeMap<String, Integer> m = new MyTreeMap<>();
            assertNull(m.put("a", 1));
            assertEquals(1, m.get("a"));
            assertEquals(1, m.size());
        }

        @Test
        void 같은_키_put은_이전값_반환하고_덮어쓴다() {
            MyTreeMap<String, Integer> m = new MyTreeMap<>();
            m.put("a", 1);
            assertEquals(1, m.put("a", 2));
            assertEquals(2, m.get("a"));
            assertEquals(1, m.size());
        }

        @Test
        void 없는_키는_null() {
            assertNull(new MyTreeMap<String, Integer>().get("missing"));
            assertFalse(new MyTreeMap<String, Integer>().containsKey("missing"));
        }

        @Test
        void remove는_값을_반환하고_제거한다() {
            MyTreeMap<String, Integer> m = new MyTreeMap<>();
            m.put("a", 1);
            m.put("b", 2);
            assertEquals(1, m.remove("a"));
            assertNull(m.get("a"));
            assertEquals(1, m.size());
            assertNull(m.remove("nope"));
        }
    }

    @Nested
    @DisplayName("keySet은 정렬 순서 (HashMap과 다른 점)")
    class OrderedKeys {

        @Test
        void 삽입_순서가_아니라_정렬_순서로_순회() {
            MyTreeMap<String, Integer> m = new MyTreeMap<>();
            m.put("c", 1);
            m.put("a", 2);
            m.put("b", 3);
            assertEquals(List.of("a", "b", "c"), m.keySet());  // 삽입순서 [c,a,b]가 아님
        }

        @Test
        void 랜덤_put후_keySet은_정렬됨() {
            MyTreeMap<Integer, Integer> m = new MyTreeMap<>();
            List<Integer> keys = new ArrayList<>();
            for (int i = 0; i < 500; i++) {
                keys.add(i);
            }
            List<Integer> shuffled = new ArrayList<>(keys);
            Collections.shuffle(shuffled, new Random(42));
            shuffled.forEach(k -> m.put(k, k * 10));
            assertEquals(keys, m.keySet());
        }
    }

    @Nested
    @DisplayName("floorKey / ceilingKey 경계")
    class Navigation {

        private MyTreeMap<Integer, String> sample() {
            MyTreeMap<Integer, String> m = new MyTreeMap<>();
            m.put(10, "x");
            m.put(20, "y");
            m.put(30, "z");
            return m;
        }

        @Test
        void floorKey() {
            MyTreeMap<Integer, String> m = sample();
            assertEquals(20, m.floorKey(20));   // 정확히 일치
            assertEquals(20, m.floorKey(25));   // 이하 중 최대
            assertEquals(30, m.floorKey(99));   // 최댓값 위
            assertNull(m.floorKey(5));          // 최솟값 아래 → 없음
        }

        @Test
        void ceilingKey() {
            MyTreeMap<Integer, String> m = sample();
            assertEquals(20, m.ceilingKey(20));  // 정확히 일치
            assertEquals(20, m.ceilingKey(15));  // 이상 중 최소
            assertEquals(10, m.ceilingKey(5));   // 최솟값 아래
            assertNull(m.ceilingKey(99));        // 최댓값 위 → 없음
        }

        @Test
        void firstKey() {
            assertEquals(10, sample().firstKey());
        }
    }

    @Nested
    @DisplayName("빈 맵 경계")
    class Empty {

        @Test
        void 빈_맵_firstKey는_예외() {
            assertThrows(NoSuchElementException.class, () -> new MyTreeMap<Integer, String>().firstKey());
        }

        @Test
        void 빈_맵_floor_ceiling은_null이고_keySet은_빈리스트() {
            MyTreeMap<Integer, String> m = new MyTreeMap<>();
            assertNull(m.floorKey(1));
            assertNull(m.ceilingKey(1));
            assertTrue(m.keySet().isEmpty());
        }
    }
}
