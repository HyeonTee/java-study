package study.chapter04;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StreamPractice")
class StreamPracticeTest {

    @Nested
    @DisplayName("filterEven")
    class FilterEven {

        @Test
        void 짝수만_남긴다() {
            assertEquals(List.of(2, 4, 6), StreamPractice.filterEven(List.of(1, 2, 3, 4, 5, 6)));
        }

        @Test
        void 짝수가_없으면_빈_리스트() {
            assertEquals(List.of(), StreamPractice.filterEven(List.of(1, 3, 5)));
        }

        @Test
        void 빈_리스트면_빈_리스트() {
            assertEquals(List.of(), StreamPractice.filterEven(List.of()));
        }

        @Test
        void 음수_짝수도_포함() {
            assertEquals(List.of(-2, 0, 4), StreamPractice.filterEven(List.of(-2, -1, 0, 3, 4)));
        }
    }

    @Nested
    @DisplayName("mapToUpperCase")
    class MapToUpperCase {

        @Test
        void 대문자로_변환() {
            assertEquals(List.of("HELLO", "WORLD"), StreamPractice.mapToUpperCase(List.of("hello", "world")));
        }

        @Test
        void 빈_리스트면_빈_리스트() {
            assertEquals(List.of(), StreamPractice.mapToUpperCase(List.of()));
        }

        @Test
        void 이미_대문자면_그대로() {
            assertEquals(List.of("ABC"), StreamPractice.mapToUpperCase(List.of("ABC")));
        }
    }

    @Nested
    @DisplayName("sumAll")
    class SumAll {

        @Test
        void 합을_구한다() {
            assertEquals(15, StreamPractice.sumAll(List.of(1, 2, 3, 4, 5)));
        }

        @Test
        void 빈_리스트면_0() {
            assertEquals(0, StreamPractice.sumAll(List.of()));
        }

        @Test
        void 음수_포함() {
            assertEquals(0, StreamPractice.sumAll(List.of(-3, -2, -1, 0, 1, 2, 3)));
        }
    }

    @Nested
    @DisplayName("findMax")
    class FindMax {

        @Test
        void 최댓값을_찾는다() {
            assertEquals(Optional.of(9), StreamPractice.findMax(List.of(3, 1, 9, 4, 7)));
        }

        @Test
        void 빈_리스트면_empty() {
            assertEquals(Optional.empty(), StreamPractice.findMax(List.of()));
        }

        @Test
        void 음수만_있어도_최댓값() {
            assertEquals(Optional.of(-1), StreamPractice.findMax(List.of(-5, -3, -1)));
        }

        @Test
        void 원소가_하나면_그것이_최댓값() {
            assertEquals(Optional.of(42), StreamPractice.findMax(List.of(42)));
        }
    }

    @Nested
    @DisplayName("flattenAndSort")
    class FlattenAndSort {

        @Test
        void 펼치고_정렬한다() {
            List<List<Integer>> nested = List.of(List.of(3, 1), List.of(4, 1, 5), List.of(9, 2));
            assertEquals(List.of(1, 1, 2, 3, 4, 5, 9), StreamPractice.flattenAndSort(nested));
        }

        @Test
        void 빈_내부_리스트_포함() {
            List<List<Integer>> nested = List.of(List.of(5), List.of(), List.of(2, 8));
            assertEquals(List.of(2, 5, 8), StreamPractice.flattenAndSort(nested));
        }

        @Test
        void 전체가_비어있으면_빈_리스트() {
            assertEquals(List.of(), StreamPractice.flattenAndSort(List.of()));
        }
    }

    @Nested
    @DisplayName("uniqueElements")
    class UniqueElements {

        @Test
        void 중복을_제거하고_순서를_유지한다() {
            assertEquals(List.of(3, 1, 4, 5, 9), StreamPractice.uniqueElements(List.of(3, 1, 4, 1, 5, 9, 3)));
        }

        @Test
        void 중복이_없으면_그대로() {
            assertEquals(List.of(1, 2, 3), StreamPractice.uniqueElements(List.of(1, 2, 3)));
        }

        @Test
        void 빈_리스트면_빈_리스트() {
            assertEquals(List.of(), StreamPractice.uniqueElements(List.of()));
        }
    }

    @Nested
    @DisplayName("topN")
    class TopN {

        @Test
        void 상위_3개를_반환한다() {
            assertEquals(List.of(9, 7, 5), StreamPractice.topN(List.of(3, 1, 9, 5, 7), 3));
        }

        @Test
        void n이_리스트_크기보다_크면_전부_반환() {
            assertEquals(List.of(5, 3, 1), StreamPractice.topN(List.of(3, 1, 5), 10));
        }

        @Test
        void n이_0이면_빈_리스트() {
            assertEquals(List.of(), StreamPractice.topN(List.of(1, 2, 3), 0));
        }

        @Test
        void 빈_리스트면_빈_리스트() {
            assertEquals(List.of(), StreamPractice.topN(List.of(), 5));
        }
    }

    @Nested
    @DisplayName("groupByLength")
    class GroupByLength {

        @Test
        void 길이별로_그룹핑한다() {
            Map<Integer, List<String>> result =
                    StreamPractice.groupByLength(List.of("hi", "bye", "ok", "hello", "hey"));

            assertEquals(List.of("hi", "ok"), result.get(2));
            assertEquals(List.of("bye", "hey"), result.get(3));
            assertEquals(List.of("hello"), result.get(5));
        }

        @Test
        void 빈_리스트면_빈_맵() {
            assertTrue(StreamPractice.groupByLength(List.of()).isEmpty());
        }
    }

    @Nested
    @DisplayName("partitionByPositive")
    class PartitionByPositive {

        @Test
        void 양수와_비양수로_나눈다() {
            Map<Boolean, List<Integer>> result =
                    StreamPractice.partitionByPositive(List.of(-2, -1, 0, 1, 2, 3));

            assertEquals(List.of(1, 2, 3), result.get(true));
            assertEquals(List.of(-2, -1, 0), result.get(false));
        }

        @Test
        void 양수만_있으면_false_그룹은_비어있다() {
            Map<Boolean, List<Integer>> result =
                    StreamPractice.partitionByPositive(List.of(1, 2, 3));

            assertEquals(List.of(1, 2, 3), result.get(true));
            assertTrue(result.get(false).isEmpty());
        }
    }

    @Nested
    @DisplayName("joinStrings")
    class JoinStrings {

        @Test
        void delimiter로_연결한다() {
            assertEquals("a, b, c", StreamPractice.joinStrings(List.of("a", "b", "c"), ", "));
        }

        @Test
        void 원소_하나면_delimiter_없이() {
            assertEquals("hello", StreamPractice.joinStrings(List.of("hello"), "-"));
        }

        @Test
        void 빈_리스트면_빈_문자열() {
            assertEquals("", StreamPractice.joinStrings(List.of(), ", "));
        }
    }

    @Nested
    @DisplayName("average")
    class Average {

        @Test
        void 평균을_구한다() {
            assertEquals(3.0, StreamPractice.average(List.of(1, 2, 3, 4, 5)), 0.001);
        }

        @Test
        void 빈_리스트면_0점0() {
            assertEquals(0.0, StreamPractice.average(List.of()), 0.001);
        }

        @Test
        void 원소_하나면_그_값() {
            assertEquals(7.0, StreamPractice.average(List.of(7)), 0.001);
        }
    }

    @Nested
    @DisplayName("wordFrequency")
    class WordFrequency {

        @Test
        void 단어별_빈도수를_구한다() {
            Map<String, Long> freq = StreamPractice.wordFrequency(
                    List.of("hello world", "hello java", "world hello"));

            assertEquals(3L, freq.get("hello"));
            assertEquals(2L, freq.get("world"));
            assertEquals(1L, freq.get("java"));
        }

        @Test
        void 대소문자를_통일한다() {
            Map<String, Long> freq = StreamPractice.wordFrequency(
                    List.of("Hello HELLO hello"));

            assertEquals(3L, freq.get("hello"));
            assertNull(freq.get("Hello"));
            assertNull(freq.get("HELLO"));
        }

        @Test
        void 빈_리스트면_빈_맵() {
            assertTrue(StreamPractice.wordFrequency(List.of()).isEmpty());
        }

        @Test
        void 단어_사이_공백이_하나면_정상_분리() {
            Map<String, Long> freq = StreamPractice.wordFrequency(List.of("a b"));
            assertEquals(1L, freq.get("a"));
            assertEquals(1L, freq.get("b"));
        }
    }
}
