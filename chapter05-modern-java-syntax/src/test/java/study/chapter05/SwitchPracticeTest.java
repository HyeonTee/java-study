package study.chapter05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SwitchPractice")
class SwitchPracticeTest {

    @Nested
    @DisplayName("dayType")
    class DayType {

        @Test
        void 평일() {
            assertEquals("WEEKDAY", SwitchPractice.dayType("MONDAY"));
            assertEquals("WEEKDAY", SwitchPractice.dayType("WEDNESDAY"));
            assertEquals("WEEKDAY", SwitchPractice.dayType("FRIDAY"));
        }

        @Test
        void 주말() {
            assertEquals("WEEKEND", SwitchPractice.dayType("SATURDAY"));
            assertEquals("WEEKEND", SwitchPractice.dayType("SUNDAY"));
        }

        @Test
        void 잘못된_값이면_IllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> SwitchPractice.dayType("HOLIDAY"));
        }
    }

    @Nested
    @DisplayName("seasonOf")
    class SeasonOf {

        @Test
        void 봄() {
            assertEquals("SPRING", SwitchPractice.seasonOf(3));
            assertEquals("SPRING", SwitchPractice.seasonOf(5));
        }

        @Test
        void 여름() {
            assertEquals("SUMMER", SwitchPractice.seasonOf(6));
            assertEquals("SUMMER", SwitchPractice.seasonOf(8));
        }

        @Test
        void 가을() {
            assertEquals("FALL", SwitchPractice.seasonOf(9));
            assertEquals("FALL", SwitchPractice.seasonOf(11));
        }

        @Test
        void 겨울() {
            assertEquals("WINTER", SwitchPractice.seasonOf(12));
            assertEquals("WINTER", SwitchPractice.seasonOf(1));
            assertEquals("WINTER", SwitchPractice.seasonOf(2));
        }

        @Test
        void 범위_밖이면_IllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> SwitchPractice.seasonOf(0));
            assertThrows(IllegalArgumentException.class, () -> SwitchPractice.seasonOf(13));
        }
    }

    @Nested
    @DisplayName("describe")
    class Describe {

        @Test
        void Integer_타입() {
            assertEquals("Integer: 42", SwitchPractice.describe(42));
        }

        @Test
        void String_타입() {
            assertEquals("String: hello", SwitchPractice.describe("hello"));
        }

        @Test
        void null이면_null() {
            assertEquals("null", SwitchPractice.describe(null));
        }

        @Test
        void 그_외_타입() {
            assertEquals("Unknown: 3.14", SwitchPractice.describe(3.14));
        }
    }

    @Nested
    @DisplayName("formatNumber")
    class FormatNumber {

        @Test
        void 양수_Integer() {
            assertEquals("positive: 42", SwitchPractice.formatNumber(42));
        }

        @Test
        void 영_Integer() {
            assertEquals("non-positive: 0", SwitchPractice.formatNumber(0));
        }

        @Test
        void 음수_Integer() {
            assertEquals("non-positive: -3", SwitchPractice.formatNumber(-3));
        }

        @Test
        void Double() {
            assertEquals("double: 3.14", SwitchPractice.formatNumber(3.14));
        }

        @Test
        void 그_외_타입() {
            assertEquals("not a number", SwitchPractice.formatNumber("hello"));
        }
    }
}
