package study.chapter18.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON 값의 <strong>sealed 모델</strong> — ch06 sealed/record의 정확한 사용처. <strong>완성 제공</strong>(채우지 않는다).
 * 직렬화 {@code switch}(={@link JsonWriter#write})만 학습자가 채운다.
 *
 * <p>6개 변형을 {@code permits}로 닫아두면, {@link JsonWriter#write}의 {@code switch}가 <strong>모든 변형을 다뤘는지
 * 컴파일러가 망라성 검사</strong>한다({@code default} 불필요). 새 변형을 추가하면 그 즉시 {@code write}가 컴파일 에러로
 * "여기도 처리하라"고 알려준다 — sealed의 값어치.
 *
 * <p><strong>숫자의 정수/실수 구분</strong>은 팩토리 {@link #of(Number)}가 흡수한다: 정수형은 {@code 1}로, 실수형은
 * {@code 3.14}로 미리 문자열 리터럴을 만들어 {@link JsonNumber}에 담는다. 그래서 {@code write}는 리터럴을 그대로 내보내면
 * 되고, 학습자가 정수/실수 포맷을 고민할 필요가 없다(예: {@code record User(long id,...)}의 {@code id}가 {@code 1.0}이 아닌
 * {@code 1}로 직렬화).
 */
public sealed interface JsonValue
        permits JsonValue.JsonString, JsonValue.JsonNumber, JsonValue.JsonBool,
        JsonValue.JsonNull, JsonValue.JsonArray, JsonValue.JsonObject {

    /** 문자열 값(직렬화 시 이스케이프 + 따옴표). */
    record JsonString(String value) implements JsonValue {
    }

    /** 숫자 값 — 이미 포맷된 JSON 리터럴(예: {@code "1"}, {@code "3.14"})을 담는다. {@link #of(Number)}로 만든다. */
    record JsonNumber(String literal) implements JsonValue {
    }

    /** 불리언 값. */
    record JsonBool(boolean value) implements JsonValue {
    }

    /** {@code null} — 싱글톤({@link #NULL}). */
    record JsonNull() implements JsonValue {
        /** 공유 인스턴스. */
        public static final JsonNull NULL = new JsonNull();
    }

    /** 배열 — 순서 보존. */
    record JsonArray(List<JsonValue> items) implements JsonValue {
    }

    /** 객체 — 키 순서 보존({@code LinkedHashMap}). */
    record JsonObject(Map<String, JsonValue> entries) implements JsonValue {
    }

    // ── 팩토리 (완성 제공) ──

    static JsonValue of(String value) {
        return value == null ? JsonNull.NULL : new JsonString(value);
    }

    static JsonValue of(boolean value) {
        return new JsonBool(value);
    }

    /** 정수형은 {@code 1}, 실수형은 {@code 3.14}로 포맷해 {@link JsonNumber}에 담는다(정수/실수 구분을 여기서 흡수). */
    static JsonValue of(Number value) {
        if (value == null) {
            return JsonNull.NULL;
        }
        if (value instanceof Double || value instanceof Float) {
            return new JsonNumber(String.valueOf(value.doubleValue()));
        }
        return new JsonNumber(String.valueOf(value.longValue()));
    }

    static JsonValue nul() {
        return JsonNull.NULL;
    }

    static JsonArray array(List<JsonValue> items) {
        return new JsonArray(List.copyOf(items));
    }

    static JsonObject object(Map<String, JsonValue> entries) {
        return new JsonObject(new LinkedHashMap<>(entries));
    }
}
