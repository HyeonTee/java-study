package study.chapter18.json;

/**
 * {@link JsonValue} → JSON 문자열 <strong>직렬화기</strong> — 프레임워크가 응답 바디를 만드는 자리. <strong>직렬화기만</strong>
 * (파서는 경계 밖 — 이론 박스). ch06 sealed {@code switch} 망라성의 정확한 사용처.
 *
 * <p>핸들러는 도메인 객체({@link JsonValue})를 반환하고, 문자열로 짜는 일은 이 "응답 경계의 어댑터"가 한다 —
 * 핸들러가 {@code "{\"id\":..."} 같은 문자열을 손으로 조립하지 않는다.
 */
public final class JsonWriter {

    private JsonWriter() {
    }

    /**
     * JSON 문자열 <strong>이스케이프</strong>: 백슬래시({@code \\})·따옴표({@code "})·{@code \n}·{@code \r}·{@code \t} 및
     * 제어문자({@code < 0x20})를 {@code \\u00XX}로. <strong>따옴표로 감싸지는 말 것</strong>(내부 이스케이프만 — 감싸는 건
     * {@link #write}의 일).
     *
     * <p><strong>함정</strong>: 백슬래시를 <strong>가장 먼저</strong> 이스케이프하지 않으면 이중 이스케이프된다(나중에 넣은
     * {@code \\}들이 또 변환됨).
     *
     * <p>힌트: 문자별로 순회하며 특수문자를 치환한 {@code StringBuilder}를 반환. {@code String.format("\\u%04x", (int) c)}.
     */
    public static String escape(String raw) {
        throw new UnsupportedOperationException(
                "TODO: 문자별 순회 — \\(먼저!)·\"·\\n·\\r·\\t 치환, 그 외 제어문자(<0x20)는 \\u00XX. 따옴표로 감싸지 말 것");
    }

    /**
     * sealed {@link JsonValue}를 {@code switch} 패턴 매칭으로 <strong>망라</strong> 분기해 JSON 문자열을 만든다 —
     * ch06의 정확한 사용처({@code switch}가 모든 {@code permits}를 다루면 {@code default} 불필요, 컴파일러가 망라성 보장).
     *
     * <ul>
     *   <li>{@code JsonString} → {@code "} + {@link #escape} + {@code "}</li>
     *   <li>{@code JsonNumber} → 리터럴 그대로(따옴표 없음)</li>
     *   <li>{@code JsonBool} → {@code true}/{@code false}, {@code JsonNull} → {@code null}</li>
     *   <li>{@code JsonArray} → {@code [<v>,<v>,...]} 재귀</li>
     *   <li>{@code JsonObject} → <code>{"k":&lt;v&gt;,...}</code> 재귀(키도 {@link #escape}, 키 순서 보존)</li>
     * </ul>
     *
     * <p><strong>함정</strong>: 배열·객체의 콤마 — <strong>마지막 원소 뒤 콤마 금지</strong>({@code String.join} 또는
     * 인덱스 가드). pretty-print(들여쓰기)는 경계 밖.
     *
     * <p>힌트: 각 변형마다 {@code write}를 재귀 호출. {@code switch (value) { case JsonString s -> ...; ... }}.
     */
    public static String write(JsonValue value) {
        throw new UnsupportedOperationException(
                "TODO: switch 패턴 매칭으로 6변형 망라. String=escape+따옴표, Number=리터럴, Bool/Null=리터럴, Array/Object=재귀(콤마 구분, 마지막 뒤 콤마 금지, Object 키도 escape)");
    }
}
