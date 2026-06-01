package study.chapter06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Range")
class RangeTest {

    @Nested
    @DisplayName("컴팩트 생성자")
    class Constructor {

        @Test
        void 유효한_범위는_정상_생성() {
            Range r = new Range(1, 5);
            assertEquals(1, r.from());
            assertEquals(5, r.to());
        }

        @Test
        void from과_to가_같으면_정상() {
            Range r = new Range(3, 3);
            assertEquals(3, r.from());
        }

        @Test
        void from이_to보다_크면_IllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> new Range(5, 1));
        }

        @Test
        void 음수_범위도_유효() {
            Range r = new Range(-10, -3);
            assertEquals(-10, r.from());
            assertEquals(-3, r.to());
        }
    }

    @Nested
    @DisplayName("length")
    class Length {

        @Test
        void 길이를_반환한다() {
            assertEquals(4, new Range(1, 5).length());
        }

        @Test
        void from과_to가_같으면_0() {
            assertEquals(0, new Range(3, 3).length());
        }

        @Test
        void 음수_범위() {
            assertEquals(7, new Range(-10, -3).length());
        }
    }

    @Nested
    @DisplayName("contains")
    class Contains {

        @Test
        void 범위_안이면_true() {
            assertTrue(new Range(1, 10).contains(5));
        }

        @Test
        void 경계값_포함() {
            Range r = new Range(1, 10);
            assertTrue(r.contains(1));
            assertTrue(r.contains(10));
        }

        @Test
        void 범위_밖이면_false() {
            Range r = new Range(1, 10);
            assertFalse(r.contains(0));
            assertFalse(r.contains(11));
        }
    }

    @Nested
    @DisplayName("overlaps")
    class Overlaps {

        @Test
        void 겹치면_true() {
            assertTrue(new Range(1, 5).overlaps(new Range(3, 8)));
        }

        @Test
        void 경계만_겹쳐도_true() {
            assertTrue(new Range(1, 5).overlaps(new Range(5, 10)));
        }

        @Test
        void 안_겹치면_false() {
            assertFalse(new Range(1, 5).overlaps(new Range(6, 10)));
        }

        @Test
        void 포함_관계도_true() {
            assertTrue(new Range(1, 10).overlaps(new Range(3, 7)));
        }

        @Test
        void 대칭이다() {
            Range a = new Range(1, 5);
            Range b = new Range(3, 8);
            assertEquals(a.overlaps(b), b.overlaps(a));
        }
    }
}
