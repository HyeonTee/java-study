package study.chapter07;

import java.util.Map;

/**
 * 문자열 키-값 설정을 읽는 작은 래퍼 — "예외로 실패를 표현한다"는 단원 주제의 무대.
 *
 * <p>{@link #require}는 "없으면 실패"를, {@link #getInt}는 "형식이 틀리면 실패하되 원인을 보존"을 연습한다.
 * 두 메서드 모두 {@link ConfigException} 계층으로 실패를 신호한다.
 */
public final class Config {

    private final Map<String, String> values;

    public Config(Map<String, String> values) {
        this.values = Map.copyOf(values);
    }

    /**
     * 키에 해당하는 값을 반환한다. 키가 없으면 {@link MissingKeyException}을 던진다.
     *
     * <p>힌트: {@code values.get(key)}가 {@code null}이면 {@code new MissingKeyException(key)}를 던지고,
     * 아니면 그 값을 반환하라.
     */
    public String require(String key) {
        throw new UnsupportedOperationException(
                "TODO: 값이 있으면 반환하고, 없으면 MissingKeyException(key)을 던져라");
    }

    /**
     * 키에 해당하는 값을 정수로 파싱해 반환한다.
     *
     * <p>힌트: {@link #require}로 문자열을 얻은 뒤 {@link Integer#parseInt}로 변환하라.
     * {@link NumberFormatException}이 나면 그것을 catch해서
     * {@code new MalformedValueException(key, 원본문자열, e)}로 다시 던지되 — <strong>cause(e)를 인자로
     * 넘겨 원인을 보존</strong>하는 것이 이 메서드의 핵심이다.
     */
    public int getInt(String key) {
        throw new UnsupportedOperationException(
                "TODO: require로 문자열을 얻어 parseInt; 실패 시 cause를 보존한 MalformedValueException을 던져라");
    }
}
