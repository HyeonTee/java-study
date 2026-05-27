package study.chapter02;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MyHashMap")
class MyHashMapTest {

    @Nested
    @DisplayName("size / isEmpty")
    class SizeAndEmpty {

        @Test
        void 새로_만들면_size는_0이고_비어있다() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            assertEquals(0, map.size());
            assertTrue(map.isEmpty());
        }

        @Test
        void put_하면_size가_1증가한다() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            map.put("a", 1);
            assertEquals(1, map.size());
            map.put("b", 2);
            assertEquals(2, map.size());
            assertFalse(map.isEmpty());
        }

        @Test
        void 같은_key를_다시_put해도_size는_그대로() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            map.put("a", 1);
            map.put("a", 2);
            assertEquals(1, map.size());
        }
    }

    @Nested
    @DisplayName("put / get")
    class PutAndGet {

        @Test
        void put한_값을_get할_수_있다() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            map.put("a", 1);
            map.put("b", 2);
            map.put("c", 3);
            assertEquals(1, map.get("a"));
            assertEquals(2, map.get("b"));
            assertEquals(3, map.get("c"));
        }

        @Test
        void 없는_key는_null을_반환한다() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            map.put("a", 1);
            assertNull(map.get("z"));
        }

        @Test
        void put은_새_key면_null을_반환하고_기존_key면_이전_값을_반환한다() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            assertNull(map.put("a", 1));
            assertEquals(1, map.put("a", 2));
            assertEquals(2, map.get("a"));
        }

        @Test
        void null_value도_저장할_수_있다() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            map.put("a", null);
            assertNull(map.get("a"));
            assertTrue(map.containsKey("a"));   // null value는 "없음"과 구분되어야 함
        }

        @Test
        void null_key도_저장할_수_있다() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            map.put(null, 99);
            assertEquals(99, map.get(null));
            assertTrue(map.containsKey(null));
        }
    }

    @Nested
    @DisplayName("equals / hashCode 계약")
    class EqualsContract {

        @Test
        void key_비교는_equals로_한다() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            map.put(new String("a"), 1);
            // 다른 인스턴스지만 equals는 true → 같은 key로 봐야 함
            assertEquals(1, map.get(new String("a")));
        }

        @Test
        void hashCode가_같지만_equals가_다른_key는_별개로_저장된다() {
            // 일부러 충돌시키는 키 두 개 — 같은 버킷에 들어가야 함
            MyHashMap<SameHashKey, String> map = new MyHashMap<>();
            SameHashKey k1 = new SameHashKey("a");
            SameHashKey k2 = new SameHashKey("b");
            map.put(k1, "v1");
            map.put(k2, "v2");
            assertEquals(2, map.size());
            assertEquals("v1", map.get(k1));
            assertEquals("v2", map.get(k2));
        }
    }

    @Nested
    @DisplayName("remove")
    class Remove {

        @Test
        void remove는_제거한_값을_반환한다() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            map.put("a", 1);
            assertEquals(1, map.remove("a"));
            assertNull(map.get("a"));
            assertFalse(map.containsKey("a"));
            assertEquals(0, map.size());
        }

        @Test
        void 없는_key를_remove하면_null() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            assertNull(map.remove("z"));
        }

        @Test
        void remove한_key를_다시_put할_수_있다() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            map.put("a", 1);
            map.remove("a");
            map.put("a", 2);
            assertEquals(2, map.get("a"));
            assertEquals(1, map.size());
        }

        @Test
        void 충돌하는_key를_remove후_재삽입() {
            MyHashMap<SameHashKey, String> map = new MyHashMap<>();
            SameHashKey k1 = new SameHashKey("a");
            SameHashKey k2 = new SameHashKey("b");
            map.put(k1, "v1");
            map.put(k2, "v2");
            map.remove(k1);
            map.put(k1, "v1-new");
            assertEquals("v1-new", map.get(k1));
            assertEquals("v2", map.get(k2));
            assertEquals(2, map.size());
        }

        @Test
        void 충돌하는_여러_key를_차례로_remove() {
            MyHashMap<SameHashKey, String> map = new MyHashMap<>();
            SameHashKey k1 = new SameHashKey("a");
            SameHashKey k2 = new SameHashKey("b");
            SameHashKey k3 = new SameHashKey("c");
            map.put(k1, "v1");
            map.put(k2, "v2");
            map.put(k3, "v3");

            assertEquals("v2", map.remove(k2));   // 중간 노드 제거
            assertEquals(2, map.size());
            assertEquals("v1", map.get(k1));
            assertEquals("v3", map.get(k3));
            assertNull(map.get(k2));
        }
    }

    @Nested
    @DisplayName("containsKey")
    class Contains {

        @Test
        void put한_key는_containsKey가_true() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            map.put("a", 1);
            assertTrue(map.containsKey("a"));
            assertFalse(map.containsKey("b"));
        }
    }

    @Nested
    @DisplayName("clear")
    class Clear {

        @Test
        void clear_후_size는_0() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            map.put("a", 1);
            map.put("b", 2);
            map.clear();
            assertEquals(0, map.size());
            assertTrue(map.isEmpty());
            assertNull(map.get("a"));
        }

        @Test
        void clear_후에_다시_put_가능() {
            MyHashMap<String, Integer> map = new MyHashMap<>();
            map.put("a", 1);
            map.clear();
            map.put("x", 100);
            assertEquals(100, map.get("x"));
            assertEquals(1, map.size());
        }
    }

    @Nested
    @DisplayName("resize / 규모")
    class ResizeAndScale {

        @Test
        void 초기_capacity_넘어도_정상_동작_resize() {
            // 초기 capacity 4 → threshold = 3. 4개 넣으면 resize 한 번 발생.
            MyHashMap<Integer, Integer> map = new MyHashMap<>(4);
            for (int i = 0; i < 100; i++) {
                map.put(i, i * 10);
            }
            assertEquals(100, map.size());
            for (int i = 0; i < 100; i++) {
                assertEquals(i * 10, map.get(i), "key=" + i);
            }
        }

        @Test
        void 만개_삽입_후_조회() {
            MyHashMap<Integer, String> map = new MyHashMap<>();
            for (int i = 0; i < 10_000; i++) {
                map.put(i, "v" + i);
            }
            assertEquals(10_000, map.size());
            assertEquals("v0", map.get(0));
            assertEquals("v9999", map.get(9_999));
            assertNull(map.get(10_000));
        }
    }

    /** 의도적으로 hashCode를 같게 만들어 충돌을 강제하는 키. */
    private static final class SameHashKey {
        final String s;

        SameHashKey(String s) {
            this.s = s;
        }

        @Override
        public int hashCode() {
            return 42;   // 전부 같은 버킷으로
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SameHashKey && ((SameHashKey) o).s.equals(this.s);
        }
    }
}
