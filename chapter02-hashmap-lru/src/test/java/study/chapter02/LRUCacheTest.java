package study.chapter02;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LRUCache")
class LRUCacheTest {

    @Nested
    @DisplayName("생성자")
    class Constructor {

        @Test
        void capacity_0_이하면_IllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> new LRUCache<>(0));
            assertThrows(IllegalArgumentException.class, () -> new LRUCache<>(-1));
        }

        @Test
        void 새로_만들면_size는_0() {
            LRUCache<String, Integer> cache = new LRUCache<>(3);
            assertEquals(0, cache.size());
            assertEquals(3, cache.capacity());
        }
    }

    @Nested
    @DisplayName("put / get 기본")
    class PutAndGet {

        @Test
        void put한_값을_get할_수_있다() {
            LRUCache<String, Integer> cache = new LRUCache<>(3);
            cache.put("a", 1);
            cache.put("b", 2);
            assertEquals(1, cache.get("a"));
            assertEquals(2, cache.get("b"));
        }

        @Test
        void 없는_key는_null() {
            LRUCache<String, Integer> cache = new LRUCache<>(3);
            assertNull(cache.get("z"));
        }

        @Test
        void put은_size를_증가시킨다() {
            LRUCache<String, Integer> cache = new LRUCache<>(3);
            cache.put("a", 1);
            cache.put("b", 2);
            assertEquals(2, cache.size());
        }

        @Test
        void 같은_key_put은_값을_갱신하고_size는_그대로() {
            LRUCache<String, Integer> cache = new LRUCache<>(3);
            cache.put("a", 1);
            cache.put("a", 99);
            assertEquals(99, cache.get("a"));
            assertEquals(1, cache.size());
        }
    }

    @Nested
    @DisplayName("eviction 정책")
    class Eviction {

        @Test
        void capacity_초과시_가장_오래된_것_제거() {
            // capacity 3에 a, b, c 채우고 d를 넣으면 a가 빠져야 함.
            LRUCache<String, Integer> cache = new LRUCache<>(3);
            cache.put("a", 1);
            cache.put("b", 2);
            cache.put("c", 3);
            cache.put("d", 4);   // a 제거

            assertEquals(3, cache.size());
            assertNull(cache.get("a"));
            assertEquals(2, cache.get("b"));
            assertEquals(3, cache.get("c"));
            assertEquals(4, cache.get("d"));
        }

        @Test
        void get은_LRU_순서를_갱신한다() {
            // a, b, c 순서로 넣고 → a를 get → b가 가장 오래된 것이 됨
            // d 넣으면 b가 빠져야 함.
            LRUCache<String, Integer> cache = new LRUCache<>(3);
            cache.put("a", 1);
            cache.put("b", 2);
            cache.put("c", 3);

            cache.get("a");   // a가 다시 최근으로

            cache.put("d", 4);   // 이제 b가 가장 오래됨 → 제거 대상

            assertNull(cache.get("b"));
            assertEquals(1, cache.get("a"));
            assertEquals(3, cache.get("c"));
            assertEquals(4, cache.get("d"));
        }

        @Test
        void 같은_key_put도_LRU_순서를_갱신한다() {
            // a, b, c 순서. a에 새 값 put → a가 최근. d 넣으면 b 제거.
            LRUCache<String, Integer> cache = new LRUCache<>(3);
            cache.put("a", 1);
            cache.put("b", 2);
            cache.put("c", 3);

            cache.put("a", 100);   // a가 최근으로

            cache.put("d", 4);

            assertNull(cache.get("b"));
            assertEquals(100, cache.get("a"));
            assertEquals(3, cache.get("c"));
            assertEquals(4, cache.get("d"));
        }
    }

    @Nested
    @DisplayName("containsKey")
    class Contains {

        @Test
        void put한_key는_true() {
            LRUCache<String, Integer> cache = new LRUCache<>(3);
            cache.put("a", 1);
            assertTrue(cache.containsKey("a"));
            assertFalse(cache.containsKey("b"));
        }

        @Test
        void eviction된_key는_false() {
            LRUCache<String, Integer> cache = new LRUCache<>(2);
            cache.put("a", 1);
            cache.put("b", 2);
            cache.put("c", 3);   // a 제거
            assertFalse(cache.containsKey("a"));
        }
    }

    @Nested
    @DisplayName("capacity 1 엣지 케이스")
    class CapacityOne {

        @Test
        void capacity_1이면_새_put마다_이전_값_제거() {
            LRUCache<String, Integer> cache = new LRUCache<>(1);
            cache.put("a", 1);
            cache.put("b", 2);
            assertNull(cache.get("a"));
            assertEquals(2, cache.get("b"));
            assertEquals(1, cache.size());
        }

        @Test
        void capacity_1에서_같은_key_재put() {
            LRUCache<String, Integer> cache = new LRUCache<>(1);
            cache.put("a", 1);
            cache.put("a", 2);
            assertEquals(2, cache.get("a"));
            assertEquals(1, cache.size());
        }
    }
}
