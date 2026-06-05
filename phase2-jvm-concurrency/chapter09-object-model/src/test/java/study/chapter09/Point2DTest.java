package study.chapter09;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Point2D / ColorPoint — 정체성 vs 동등성, 상속 equals 대칭성")
class Point2DTest {

    @Nested
    @DisplayName("정체성(==) vs 동등성(equals)")
    class IdentityVsEquality {

        @Test
        void 다른_객체지만_값이_같으면_equals는_true() {
            Point2D a = new Point2D(1, 2);
            Point2D b = new Point2D(1, 2);
            assertNotSame(a, b);              // 서로 다른 객체 (정체성 다름)
            assertEquals(a, b);               // 그러나 값은 같음 (동등성)
        }

        @Test
        void 같은_값이면_hashCode도_같다() {
            assertEquals(new Point2D(3, 4).hashCode(), new Point2D(3, 4).hashCode());
        }

        @Test
        void HashSet은_값이_같은_점을_중복으로_보지_않는다() {
            Set<Point2D> set = new HashSet<>();
            set.add(new Point2D(1, 2));
            set.add(new Point2D(1, 2));
            assertEquals(1, set.size());
        }
    }

    @Nested
    @DisplayName("equals 일반 계약")
    class EqualsContract {

        @Test
        void 반사성() {
            Point2D p = new Point2D(1, 2);
            assertEquals(p, p);
        }

        @Test
        void 대칭성() {
            Point2D a = new Point2D(1, 2);
            Point2D b = new Point2D(1, 2);
            assertEquals(a.equals(b), b.equals(a));
            assertTrue(a.equals(b));
        }

        @Test
        void 추이성() {
            Point2D a = new Point2D(1, 2);
            Point2D b = new Point2D(1, 2);
            Point2D c = new Point2D(1, 2);
            assertTrue(a.equals(b) && b.equals(c) && a.equals(c));
        }

        @Test
        void 일관성_반복호출해도_같다() {
            Point2D a = new Point2D(1, 2);
            Point2D b = new Point2D(1, 2);
            for (int i = 0; i < 100; i++) {
                assertTrue(a.equals(b));
            }
        }

        @Test
        void null과_비교하면_false_예외아님() {
            assertFalse(new Point2D(1, 2).equals(null));
        }

        @Test
        void 좌표가_다르면_다르다() {
            assertNotEquals(new Point2D(1, 2), new Point2D(1, 9));
        }
    }

    @Nested
    @DisplayName("상속 계층의 대칭성 함정")
    class InheritanceSymmetry {

        @Test
        void 좌표만_같고_타입이_다르면_동등하지_않다() {
            Point2D p = new Point2D(1, 2);
            ColorPoint cp = new ColorPoint(1, 2, "RED");
            assertNotEquals(p, cp);   // getClass 기반이면 서로 다른 타입 → false
        }

        @Test
        void Point2D와_ColorPoint_비교는_대칭이어야_한다() {
            Point2D p = new Point2D(1, 2);
            ColorPoint cp = new ColorPoint(1, 2, "RED");
            // instanceof 기반 equals면 p.equals(cp)=true, cp.equals(p)=false 로 비대칭 → 실패.
            assertEquals(p.equals(cp), cp.equals(p));
        }

        @Test
        void 좌표와_색이_모두_같아야_ColorPoint가_동등() {
            assertEquals(new ColorPoint(1, 2, "RED"), new ColorPoint(1, 2, "RED"));
            assertNotEquals(new ColorPoint(1, 2, "RED"), new ColorPoint(1, 2, "BLUE"));
        }

        @Test
        void 동등한_ColorPoint는_hashCode도_같다() {
            assertEquals(new ColorPoint(1, 2, "RED").hashCode(),
                    new ColorPoint(1, 2, "RED").hashCode());
        }
    }
}
