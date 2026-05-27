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
        return strings.stream().filter(predicate).findFirst();
    }

    /** 값이 있으면 반환, 없으면 defaultValue를 반환한다. */
    public static String getOrDefault(Optional<String> opt, String defaultValue) {
        return opt.orElse(defaultValue);
    }

    /** 값이 있으면 반환, 없으면 supplier를 호출하여 반환한다. */
    public static String getOrCompute(Optional<String> opt, Supplier<String> supplier) {
        return opt.orElseGet(supplier);
    }

    /**
     * 값이 있으면 반환, 없으면 IllegalArgumentException을 던진다.
     *
     * @throws IllegalArgumentException 값이 없을 때
     */
    public static String getOrThrow(Optional<String> opt) {
        return opt.orElseThrow(IllegalArgumentException::new);
    }

    /**
     * 문자열을 정수로 파싱하여 Optional로 반환한다.
     * 파싱 실패(NumberFormatException) 또는 null 입력 시 empty.
     */
    public static Optional<Integer> safeParseInt(String s) {
        try {
            int parsed = Integer.parseInt(s);

            return Optional.of(parsed);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** Optional 안의 문자열을 대문자로 변환한다. 비어있으면 empty 유지. */
    public static Optional<String> toUpperIfPresent(Optional<String> opt) {
        return opt.map(String::toUpperCase);
    }

    /**
     * Optional 안의 문자열에서 첫 글자를 Optional로 반환한다.
     * 비어있거나 빈 문자열이면 empty.
     */
    public static Optional<Character> firstChar(Optional<String> opt) {
        return opt.flatMap(str -> {
            if (str.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(str.charAt(0));
        });
    }

    /** 문자열 길이가 minLength 이상인 경우만 유지한다. 미만이면 empty. */
    public static Optional<String> filterByLength(Optional<String> opt, int minLength) {
        return opt.filter(str -> str.length() >= minLength);
    }
}
