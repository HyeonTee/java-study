package study.chapter05;

/**
 * 정수 범위를 나타내는 불변 Record.
 *
 * <p>{@code from}은 {@code to} 이하여야 한다. 위반 시 {@link IllegalArgumentException}.
 *
 * @param from 시작값 (포함)
 * @param to   끝값 (포함)
 */
public record Range(int from, int to) {

    public Range {
        // TODO: from이 to보다 크면 예외를 던져라
    }

    /** 범위의 길이를 반환한다. (to - from) */
    public int length() {
        throw new UnsupportedOperationException("TODO: 끝값과 시작값의 차이를 구하라");
    }

    /** 값이 범위 안에 있는지 반환한다. (from <= value <= to) */
    public boolean contains(int value) {
        throw new UnsupportedOperationException("TODO: value가 from과 to 사이에 있는지 판별하라 (경계 포함)");
    }

    /** 다른 범위와 겹치는 부분이 있는지 반환한다. */
    public boolean overlaps(Range other) {
        throw new UnsupportedOperationException("TODO: 두 범위가 겹치는 조건을 생각해보라");
    }
}
