package study.chapter02;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Money — equals/hashCode 계약")
class MoneyTest {

    // ── equals 계약 ────────────────────────────────────────────

    @Nested
    @DisplayName("equals 계약")
    class EqualsContract {

        @Test
        void 값이_같으면_equals는_true() {
            assertEquals(new Money("USD", 100), new Money("USD", 100));
        }

        @Test
        void 통화가_다르면_false() {
            assertNotEquals(new Money("USD", 100), new Money("KRW", 100));
        }

        @Test
        void 금액이_다르면_false() {
            assertNotEquals(new Money("USD", 100), new Money("USD", 200));
        }

        @Test
        void 반사성_자기자신과_같다() {
            Money m = new Money("USD", 100);
            assertEquals(m, m);
        }

        @Test
        void 대칭성_x가y면_y도x() {
            Money x = new Money("USD", 100);
            Money y = new Money("USD", 100);
            assertTrue(x.equals(y) && y.equals(x));
        }

        @Test
        void 추이성_x가y고_y가z면_x도z() {
            Money x = new Money("USD", 100);
            Money y = new Money("USD", 100);
            Money z = new Money("USD", 100);
            assertTrue(x.equals(y) && y.equals(z) && x.equals(z));
        }

        @Test
        void null과_비교하면_false() {
            assertNotEquals(null, new Money("USD", 100));
        }

        @Test
        void 다른_타입과_비교하면_false() {
            assertNotEquals("USD 100", new Money("USD", 100));
        }
    }

    // ── hashCode 계약 ──────────────────────────────────────────

    @Nested
    @DisplayName("hashCode 계약")
    class HashCodeContract {

        @Test
        void equals가_같으면_hashCode도_같다() {
            assertEquals(new Money("USD", 100).hashCode(), new Money("USD", 100).hashCode());
        }

        @Test
        void 일관성_여러번_호출해도_동일() {
            Money m = new Money("USD", 100);
            int first = m.hashCode();
            assertEquals(first, m.hashCode());
            assertEquals(first, m.hashCode());
        }
    }

    // ── 컬렉션 키로 사용 ───────────────────────────────────────

    @Nested
    @DisplayName("HashMap / HashSet 키로 사용")
    class AsCollectionKey {

        @Test
        void HashMap_키로_넣고_동등한_객체로_조회() {
            Map<Money, String> map = new HashMap<>();
            map.put(new Money("USD", 100), "one dollar");
            // 다른 인스턴스지만 값이 같으면 찾을 수 있어야 한다
            assertEquals("one dollar", map.get(new Money("USD", 100)));
        }

        @Test
        void HashSet은_값이_같은_중복을_제거한다() {
            Set<Money> set = new HashSet<>();
            set.add(new Money("USD", 100));
            set.add(new Money("USD", 100));
            set.add(new Money("KRW", 100));
            assertEquals(2, set.size());
        }
    }

    // ── 가변 키 함정 (이해 확인) ───────────────────────────────

    @Nested
    @DisplayName("가변 키 함정")
    class MutableKeyTrap {

        /** equals/hashCode가 가변 필드에 의존하는 잘못 설계된 키. */
        static final class MutablePoint {
            int x;
            int y;

            MutablePoint(int x, int y) {
                this.x = x;
                this.y = y;
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof MutablePoint p)) {
                    return false;
                }
                return x == p.x && y == p.y;
            }

            @Override
            public int hashCode() {
                return 31 * x + y;
            }
        }

        @Test
        void 키를_넣은_뒤_변경하면_찾지_못한다() {
            Map<MutablePoint, String> map = new HashMap<>();
            MutablePoint key = new MutablePoint(1, 2);
            map.put(key, "origin");

            key.x = 99;   // 삽입 후 키를 변경 → hashCode가 바뀜

            // 변경된 hashCode로는 원래 버킷을 찾지 못한다
            assertNull(map.get(key),
                    "가변 키를 삽입 후 변경하면 해시 버킷이 어긋나 조회에 실패한다");
            // 그래서 키는 불변(immutable)이어야 한다 — Money가 final 필드인 이유다
        }
    }
}
