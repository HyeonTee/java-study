package study.chapter05;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * {@code java.util.stream.Collector}를 <strong>직접 구현</strong>한다 — Stream의 가장
 * 강력하고
 * 가장 덜 이해되는 부분.
 *
 * <p>
 * {@code StreamPractice}에서 {@code Collectors.groupingBy} 등을
 * <strong>사용</strong>했다면,
 * 여기서는 그 내부를 만든다. {@code Collector}는 네 조각으로 이루어진다:
 * <ul>
 * <li><strong>supplier</strong> — 누적용 빈 컨테이너를 만든다 ({@code () -> new ...}).</li>
 * <li><strong>accumulator</strong> — 컨테이너에 원소 하나를 접어 넣는다
 * ({@code (acc, e) -> ...}).</li>
 * <li><strong>combiner</strong> — 부분 결과 둘을 합친다 ({@code (a, b) -> ...}).
 * <strong>병렬 스트림</strong>에서
 * 여러 스레드의 부분 누적을 합칠 때 호출된다 — 이게 왜 reduce에 combiner가 필요한지의 답이다.</li>
 * <li><strong>finisher</strong> — 누적 컨테이너를 최종 결과로 변환한다 (없으면 항등).</li>
 * </ul>
 *
 * <p>
 * {@code Collector.of(supplier, accumulator, combiner[, finisher])}로 조립한다. 모든
 * 메서드는
 * <strong>병렬 스트림에서도 정확</strong>해야 한다(테스트가 {@code parallel()}로 combiner를 강제한다).
 */
public final class MyCollectors {

    private MyCollectors() {
    }

    /**
     * 원소 개수를 세는 Collector. (예:
     * {@code Stream.of("a","b","c").collect(counting()) == 3L})
     *
     * <p>
     * 힌트: supplier로 가변 카운터(예: {@code long[1]}), accumulator로 증가, combiner로 두 카운터를
     * 더하고, finisher로 {@code Long}을 꺼낸다.
     */
    public static <T> Collector<T, ?, Long> counting() {
        return Collector.of(
                () -> new long[1],
                (acc, e) -> {
                    acc[0]++;
                },
                (a, b) -> {
                    a[0] += b[0];
                    return a;
                },
                acc -> acc[0]);
    }

    /**
     * 원소들을 {@code delimiter}로 이어 붙이는 Collector.
     * (예: {@code Stream.of("a","b","c").collect(joining(", ")) == "a, b, c"}, 빈
     * 스트림은 {@code ""}.)
     *
     * <p>
     * 힌트: supplier는 {@code StringBuilder}. combiner에서 두 부분 문자열을 합칠 때 사이에 한 번만
     * 구분자가 들어가도록 빈 조각을 조심하라(병렬에서 중요).
     */
    public static Collector<CharSequence, ?, String> joining(String delimiter) {
        return Collector.of(
                StringBuilder::new,
                (sb, e) -> {
                    if (sb.length() != 0) {
                        sb.append(delimiter).append(e);
                    } else {
                        sb.append(e);
                    }
                },
                (a, b) -> {
                    if (a.length() == 0 || b.length() == 0) {
                        return a.append(b);
                    }
                    return a.append(delimiter).append(b);
                },
                sb -> sb.toString());
    }

    /**
     * {@code classifier}가 같은 키를 내는 원소들을 한 리스트로 묶는 Collector.
     * (예: 길이로 분류 → {@code {1=[a], 2=[bb], ...}}.) {@code Collectors.groupingBy}의
     * 축소판.
     *
     * <p>
     * 힌트: supplier는 {@code HashMap}. accumulator는 키별 리스트에 추가, combiner는 두 맵을 키 단위로
     * 병합(같은 키의 리스트끼리 합침).
     */
    public static <T, K> Collector<T, ?, Map<K, List<T>>> groupingBy(
            Function<? super T, ? extends K> classifier) {
        return Collector.of(
                HashMap::new,
                (acc, e) -> {
                    K key = classifier.apply(e);
                    if (acc.containsKey(key)) {
                        List<T> list = acc.get(key);
                        list.add(e);
                    } else {
                        List<T> newList = new ArrayList<>();
                        newList.add(e);
                        acc.put(key, newList);
                    }
                },
                (a, b) -> {
                    Map<K, List<T>> result = new HashMap<>();

                    a.forEach((key, list) -> {
                        if (b.containsKey(key)) {
                            list.addAll(b.get(key));
                        }

                        result.put(key, list);
                    });

                    b.forEach((key, list) -> {
                        if (!a.containsKey(key)) {
                            result.put(key, list);
                        }
                    });

                    return result;
                });
    }
}
