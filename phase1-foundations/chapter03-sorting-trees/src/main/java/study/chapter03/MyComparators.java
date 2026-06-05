package study.chapter03;

import java.util.Comparator;
import java.util.function.Function;

/**
 * {@link java.util.Comparator}의 합성 연산들을 직접 구현하는 정적 유틸리티 — "순서란 무엇인가".
 *
 * <p>
 * 트리·힙·정렬 맵은 전부 "두 원소 중 누가 먼저냐"에 의존한다. 그 순서를 정의하는 두 길이
 * <strong>자연 순서</strong>({@link Comparable})와 <strong>주입된
 * 순서</strong>({@code Comparator})다.
 * JDK의
 * {@code Comparator.reversed()}/{@code thenComparing()}/{@code nullsFirst()}/{@code comparing()}이
 * 내부적으로 "기존 비교기를 감싼 <strong>새</strong> 비교기를 반환하는 고차 함수"임을 직접 만들어
 * 확인한다(ch04 함수 합성과 직결). 어떤 메서드도 입력 비교기를 변형하지 않는다(불변).
 *
 * <p>
 * 함정: {@code reversed}를 {@code -base.compare(a,b)}로 짜면
 * {@code Integer.MIN_VALUE}에서 부호가
 * 뒤집히지 않는다(오버플로). 인자를 바꿔 {@code base.compare(b,a)}로 해야 안전하다.
 */
public final class MyComparators {

    private MyComparators() {
    }

    /**
     * key extractor가 뽑은 {@link Comparable} 키의 자연 순서로 비교하는 비교기를 만든다.
     *
     * <p>
     * 힌트: {@code (a, b) -> keyExtractor.apply(a).compareTo(keyExtractor.apply(b))}.
     */
    public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
            Function<? super T, ? extends U> keyExtractor) {
        return (a, b) -> keyExtractor.apply(a).compareTo(keyExtractor.apply(b));
    }

    /**
     * {@code base}의 정렬 방향을 뒤집은 새 비교기. (오버플로 함정 주의 — 인자를 교환하라.)
     */
    public static <T> Comparator<T> reversed(Comparator<T> base) {
        return (a, b) -> base.compare(b, a);
    }

    /**
     * {@code first}가 동률(0)을 반환할 때만 {@code second}로 비교하는 새 비교기.
     */
    public static <T> Comparator<T> thenComparing(Comparator<T> first, Comparator<? super T> second) {
        return (a, b) -> first.compare(a, b) == 0 ? second.compare(a, b) : first.compare(a, b);
    }

    /**
     * {@code null}을 non-null보다 <strong>앞</strong>에 두는 비교기. 둘 다 null이면 0, 둘 다
     * non-null이면
     * {@code inner}로 비교한다.
     *
     * <p>
     * 힌트: (null,null)→0, (null,x)→음수, (x,null)→양수,
     * (x,y)→{@code inner.compare(x,y)}.
     */
    public static <T> Comparator<T> nullsFirst(Comparator<? super T> inner) {
        return (a, b) -> {
            if (a != null && b != null) {
                return inner.compare(a, b);
            } else if (a == null && b == null) {
                return 0;
            } else if (a == null) {
                return -1;
            } else {
                return 1;
            }
        };
    }

    /**
     * {@code null}을 non-null보다 <strong>뒤</strong>에 두는 변형. 나머지는 {@link #nullsFirst}와
     * 동일.
     */
    public static <T> Comparator<T> nullsLast(Comparator<? super T> inner) {
        return (a, b) -> {
            if (a != null && b != null) {
                return inner.compare(a, b);
            } else if (a == null && b == null) {
                return 0;
            } else if (a == null) {
                return 1;
            } else {
                return -1;
            }
        };
    }
}
