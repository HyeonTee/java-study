package study.chapter05;

/**
 * 정수 범위를 나타내는 불변 Record.
 *
 * <p>
 * {@code from}은 {@code to} 이하여야 한다. 위반 시 {@link IllegalArgumentException}.
 *
 * @param from 시작값 (포함)
 * @param to   끝값 (포함)
 */
public record Range(int from, int to) {

    public Range {
        if (from > to) {
            throw new IllegalArgumentException("from > to");
        }
    }

    /** 범위의 길이를 반환한다. (to - from) */
    public int length() {
        return to - from;
    }

    /** 값이 범위 안에 있는지 반환한다. (from <= value <= to) */
    public boolean contains(int value) {
        return from <= value && value <= to;
    }

    /** 다른 범위와 겹치는 부분이 있는지 반환한다. */
    public boolean overlaps(Range other) {
        return !(Math.min(this.to, other.to) < Math.max(this.from, other.from));
    }
}
