package study.chapter07;

/**
 * 요청한 설정 키가 존재하지 않을 때 던지는 예외 — 지원 코드.
 *
 * <p>문제가 된 키를 {@link #key()}로 보존해, 호출자가 어떤 키가 빠졌는지 메시지 파싱 없이 알 수 있게 한다.
 */
public final class MissingKeyException extends ConfigException {

    private final String key;

    public MissingKeyException(String key) {
        super("missing config key: " + key);
        this.key = key;
    }

    public String key() {
        return key;
    }
}
