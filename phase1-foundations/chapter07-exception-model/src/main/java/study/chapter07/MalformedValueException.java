package study.chapter07;

/**
 * 설정 키는 있으나 값이 기대한 형식이 아닐 때(예: 정수 변환 실패) 던지는 예외 — 지원 코드.
 *
 * <p>핵심은 원인 예외({@code cause})를 {@code super(message, cause)}로 보존한다는 점이다. 이렇게 해야
 * {@link Exceptions#rootCause}로 근본 원인({@link NumberFormatException} 등)을 거슬러 올라갈 수 있다.
 * 키와 원본 문자열도 {@link #key()}/{@link #rawValue()}로 함께 보존한다.
 */
public final class MalformedValueException extends ConfigException {

    private final String key;
    private final String rawValue;

    public MalformedValueException(String key, String rawValue, Throwable cause) {
        super("malformed value for key '" + key + "': " + rawValue, cause);
        this.key = key;
        this.rawValue = rawValue;
    }

    public String key() {
        return key;
    }

    public String rawValue() {
        return rawValue;
    }
}
