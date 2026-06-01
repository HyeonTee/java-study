package study.chapter06;

/**
 * Switch expression + Pattern matching 연습.
 *
 * <p>모든 메서드는 switch expression(화살표 문법)으로 구현한다.
 */
public class SwitchPractice {

    private SwitchPractice() {
    }

    /**
     * 요일 문자열을 받아 "WEEKDAY" 또는 "WEEKEND"를 반환한다.
     *
     * <ul>
     *   <li>MONDAY ~ FRIDAY → "WEEKDAY"</li>
     *   <li>SATURDAY, SUNDAY → "WEEKEND"</li>
     *   <li>그 외 → IllegalArgumentException</li>
     * </ul>
     *
     * <p>힌트: {@code case "MONDAY", "TUESDAY", ... -> "WEEKDAY";}
     */
    public static String dayType(String day) {
        throw new UnsupportedOperationException(
                "TODO: switch expression — 여러 case를 콤마로 묶어 화살표 문법 사용");
    }

    /**
     * 월(1~12)을 받아 계절을 반환한다.
     *
     * <ul>
     *   <li>3, 4, 5 → "SPRING"</li>
     *   <li>6, 7, 8 → "SUMMER"</li>
     *   <li>9, 10, 11 → "FALL"</li>
     *   <li>12, 1, 2 → "WINTER"</li>
     *   <li>그 외 → IllegalArgumentException</li>
     * </ul>
     */
    public static String seasonOf(int month) {
        throw new UnsupportedOperationException(
                "TODO: switch expression — 정수 case 묶기");
    }

    /**
     * 객체의 타입에 따라 설명 문자열을 반환한다 (패턴 매칭 switch).
     *
     * <ul>
     *   <li>Integer → "Integer: 42"</li>
     *   <li>String → "String: hello"</li>
     *   <li>null → "null"</li>
     *   <li>그 외 → "Unknown: " + obj.toString()</li>
     * </ul>
     */
    public static String describe(Object obj) {
        throw new UnsupportedOperationException(
                "TODO: 패턴 매칭 switch로 타입별 분기를 작성하라");
    }

    /**
     * 숫자 객체를 포맷팅한다 (guarded pattern — when 절).
     *
     * <ul>
     *   <li>양수 Integer → "positive: 42"</li>
     *   <li>0 이하 Integer → "non-positive: -3"</li>
     *   <li>Double → "double: 3.14"</li>
     *   <li>그 외 → "not a number"</li>
     * </ul>
     *
     * <p>힌트: {@code case Integer i when i > 0 -> ...}
     */
    public static String formatNumber(Object obj) {
        throw new UnsupportedOperationException(
                "TODO: guarded pattern(when 절)을 활용해 타입 + 조건으로 분기하라");
    }
}
