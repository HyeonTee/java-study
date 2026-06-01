package study.chapter05;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("MyCollectors — Collector 직접 구현")
class MyCollectorsTest {

    @Nested
    @DisplayName("counting")
    class Counting {

        @Test
        void 원소_개수를_센다() {
            assertEquals(3L, Stream.of("a", "b", "c").collect(MyCollectors.counting()));
        }

        @Test
        void 빈_스트림은_0() {
            assertEquals(0L, Stream.<String>of().collect(MyCollectors.counting()));
        }

        @Test
        @DisplayName("병렬 스트림에서도 정확 (combiner 검증)")
        void 병렬에서도_정확() {
            long n = IntStream.range(0, 10_000).boxed().parallel().collect(MyCollectors.counting());
            assertEquals(10_000L, n);
        }
    }

    @Nested
    @DisplayName("joining")
    class Joining {

        @Test
        void 구분자로_이어붙인다() {
            assertEquals("a, b, c", Stream.of("a", "b", "c").collect(MyCollectors.joining(", ")));
        }

        @Test
        void 빈_스트림은_빈_문자열() {
            assertEquals("", Stream.<CharSequence>of().collect(MyCollectors.joining(", ")));
        }

        @Test
        void 원소_하나는_구분자_없음() {
            assertEquals("solo", Stream.<CharSequence>of("solo").collect(MyCollectors.joining(", ")));
        }

        @Test
        @DisplayName("병렬에서도 구분자가 한 번씩만 (combiner 검증)")
        void 병렬에서도_정확() {
            String joined = IntStream.rangeClosed(1, 1000)
                    .mapToObj(Integer::toString)
                    .parallel()
                    .collect(MyCollectors.joining(","));
            // 1000개 → 999개의 구분자, 콤마로 split하면 1000조각
            assertEquals(1000, joined.split(",").length);
            assertEquals("1", joined.split(",")[0]);
        }
    }

    @Nested
    @DisplayName("groupingBy")
    class GroupingBy {

        @Test
        void 분류기로_묶는다() {
            Map<Integer, List<String>> byLength =
                    Stream.of("a", "bb", "cc", "ddd").collect(MyCollectors.groupingBy(String::length));
            assertEquals(List.of("a"), byLength.get(1));
            assertEquals(List.of("bb", "cc"), byLength.get(2));
            assertEquals(List.of("ddd"), byLength.get(3));
            assertEquals(3, byLength.size());
        }

        @Test
        @DisplayName("병렬에서도 같은 그룹화 (맵 병합 combiner 검증)")
        void 병렬에서도_정확() {
            Map<Boolean, List<Integer>> byParity = IntStream.range(0, 1000).boxed()
                    .parallel()
                    .collect(MyCollectors.groupingBy(i -> i % 2 == 0));
            assertEquals(500, byParity.get(true).size());
            assertEquals(500, byParity.get(false).size());
        }
    }
}
