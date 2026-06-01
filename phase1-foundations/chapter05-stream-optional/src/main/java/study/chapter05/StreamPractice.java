package study.chapter05;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Stream API 연습 문제.
 *
 * <p>
 * 모든 메서드는 {@code static}이며, 파라미터로 받은 컬렉션을 변경하지 않는다.
 * 각 메서드의 Javadoc과 TODO 힌트를 참고하여 구현하라.
 */
public class StreamPractice {

    private StreamPractice() {
    }

    /** 짝수만 필터링하여 리스트로 반환한다. 순서 유지. */
    public static List<Integer> filterEven(List<Integer> numbers) {
        return numbers.stream()
                .filter(number -> (number & 1) == 0)
                .toList();
    }

    /** 모든 문자열을 대문자로 변환하여 리스트로 반환한다. */
    public static List<String> mapToUpperCase(List<String> strings) {
        return strings.stream()
                .map(String::toUpperCase)
                .toList();
    }

    /** 정수 리스트의 합을 반환한다. 빈 리스트면 0. */
    public static int sumAll(List<Integer> numbers) {
        return numbers.stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /** 리스트에서 최댓값을 Optional로 반환한다. */
    public static Optional<Integer> findMax(List<Integer> numbers) {
        return numbers.stream().max(Integer::compare);
    }

    /** 중첩 리스트를 하나로 펼치고 오름차순 정렬하여 반환한다. */
    public static List<Integer> flattenAndSort(List<List<Integer>> nested) {
        return nested.stream()
                .flatMap(list -> list.stream())
                .sorted()
                .toList();
    }

    /** 중복을 제거하고 입력 순서를 유지하여 반환한다. */
    public static List<Integer> uniqueElements(List<Integer> numbers) {
        return numbers.stream().distinct().toList();
    }

    /** 내림차순 정렬 후 상위 n개를 반환한다. n이 리스트 크기보다 크면 전부 반환. */
    public static List<Integer> topN(List<Integer> numbers, int n) {
        return numbers.stream().sorted(Comparator.reverseOrder()).limit(n).toList();
    }

    /** 문자열을 길이별로 그룹핑한다. */
    public static Map<Integer, List<String>> groupByLength(List<String> strings) {
        return strings.stream().collect(Collectors.groupingBy(String::length));
    }

    /** 양수/비양수로 파티셔닝한다. true = 양수, false = 0 이하. */
    public static Map<Boolean, List<Integer>> partitionByPositive(List<Integer> numbers) {
        return numbers.stream().collect(Collectors.partitioningBy(number -> number > 0));
    }

    /** 문자열을 delimiter로 연결한다. */
    public static String joinStrings(List<String> strings, String delimiter) {
        return strings.stream().collect(Collectors.joining(delimiter));
    }

    /** 정수 리스트의 평균을 반환한다. 빈 리스트면 0.0. */
    public static double average(List<Integer> numbers) {
        return numbers.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    /**
     * 문장 리스트에서 단어별 등장 횟수를 구한다.
     *
     * <ul>
     * <li>각 문장은 공백 기준으로 단어를 분리한다.</li>
     * <li>단어는 소문자로 통일한다.</li>
     * <li>반환 Map의 key = 단어, value = 등장 횟수.</li>
     * </ul>
     */
    public static Map<String, Long> wordFrequency(List<String> sentences) {
        return sentences.stream()
                .flatMap(sentence -> Arrays.stream(sentence.split(" ")))
                .map(String::toLowerCase)
                .collect(Collectors.groupingBy(word -> word, Collectors.counting()));
    }
}
