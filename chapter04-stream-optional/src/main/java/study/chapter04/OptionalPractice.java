package study.chapter04;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Optional 연습 문제.
 *
 * <p>
 * 모든 메서드는 {@code static}이다.
 * 각 메서드의 Javadoc과 TODO 힌트를 참고하여 구현하라.
 */
public class OptionalPractice {

    private OptionalPractice() {
    }

    /** 조건에 맞는 첫 번째 문자열을 Optional로 반환한다. */
    public static Optional<String> findFirstMatch(List<String> strings, Predicate<String> predicate) {
        throw new UnsupportedOperationException("TODO: stream + filter + findFirst");
    }

    /** 값이 있으면 반환, 없으면 defaultValue를 반환한다. */
    public static String getOrDefault(Optional<String> opt, String defaultValue) {
        throw new UnsupportedOperationException("TODO: Optional의 기본값 반환 메서드를 활용하라");
    }

    /** 값이 있으면 반환, 없으면 supplier를 호출하여 반환한다. */
    public static String getOrCompute(Optional<String> opt, Supplier<String> supplier) {
        throw new UnsupportedOperationException("TODO: Optional의 지연 평가 기본값 메서드를 활용하라");
    }

    /**
     * 값이 있으면 반환, 없으면 IllegalArgumentException을 던진다.
     *
     * @throws IllegalArgumentException 값이 없을 때
     */
    public static String getOrThrow(Optional<String> opt) {
        throw new UnsupportedOperationException("TODO: 값이 없을 때 예외를 던지는 Optional 메서드를 활용하라");
    }

    /**
     * 문자열을 정수로 파싱하여 Optional로 반환한다.
     * 파싱 실패(NumberFormatException) 또는 null 입력 시 empty.
     */
    public static Optional<Integer> safeParseInt(String s) {
        throw new UnsupportedOperationException("TODO: try-catch + Optional.of / Optional.empty");
    }

    /** Optional 안의 문자열을 대문자로 변환한다. 비어있으면 empty 유지. */
    public static Optional<String> toUpperIfPresent(Optional<String> opt) {
        throw new UnsupportedOperationException("TODO: Optional의 변환 메서드로 대문자 변환을 적용하라");
    }

    /**
     * Optional 안의 문자열에서 첫 글자를 Optional로 반환한다.
     * 비어있거나 빈 문자열이면 empty.
     */
    public static Optional<Character> firstChar(Optional<String> opt) {
        throw new UnsupportedOperationException(
                "TODO: 변환 결과가 Optional일 때 중첩을 방지하는 메서드를 활용하라");
    }

    /** 문자열 길이가 minLength 이상인 경우만 유지한다. 미만이면 empty. */
    public static Optional<String> filterByLength(Optional<String> opt, int minLength) {
        throw new UnsupportedOperationException("TODO: Optional의 조건 필터링 메서드를 활용하라");
    }
}
