package study.chapter19.json;

import java.util.Map;

/**
 * 임의 객체(특히 {@code record} 컴포넌트) → {@link JsonValue} 매핑 — ch10 {@code ObjectMapper.toMap}의 JSON판
 * (<strong>리플렉션 회수</strong>). {@link JsonWriter}(직렬화)와 분리해 분량·결합을 통제한다.
 *
 * <p><strong>리플렉션 실물 회수는 이 한 곳</strong>으로 단일화한다(ch10 {@code RouteScanner}와 중복 회피). ch10가
 * {@code Field.setAccessible} + {@code Method.invoke}였다면, 여기선 <strong>record 접근자</strong>를 쓴다 —
 * "같은 리플렉션 패턴의 다른 적용".
 */
public final class JsonReflect {

    private JsonReflect() {
    }

    /**
     * 객체를 {@link JsonValue}로 변환한다:
     * <ul>
     *   <li>{@code null} → {@code JsonNull}</li>
     *   <li>{@code String}/{@code Boolean}/{@code Number} → {@code JsonValue.of(...)}</li>
     *   <li>{@code java.util.Map} → {@link #fromMap}</li>
     *   <li>{@code java.util.List} → {@code JsonArray}(원소마다 {@code toJson} 재귀)</li>
     *   <li>{@code record} → {@code JsonObject}(컴포넌트명 → 컴포넌트값 {@code toJson} 재귀, 선언 순서)</li>
     * </ul>
     *
     * <p><strong>함정(ch10 회수)</strong>: record 컴포넌트는 {@code type.getRecordComponents()}로 얻고, 값은
     * {@code component.getAccessor().invoke(obj)}로 읽는다(필드 {@code setAccessible} 대신 <strong>접근자 메서드</strong>).
     * 접근자가 내부에서 예외를 던지면 {@code InvocationTargetException}으로 감싸여 나오므로, ch10 {@code Reflect.invoke}
     * 정신대로 <strong>{@code getCause()}로 원래 예외를 꺼내 그대로 다시 던져라</strong>(원래가 비검사면 그대로). 순환 참조·중첩
     * 깊이 제한은 경계 밖.
     *
     * <p>힌트: 타입별 {@code instanceof} 분기 + record는 {@code getRecordComponents()} 순회로 {@code LinkedHashMap}을
     * 채워 {@code JsonValue.object(...)}. 알 수 없는 타입은 {@link IllegalArgumentException}.
     */
    public static JsonValue toJson(Object obj) {
        throw new UnsupportedOperationException(
                "TODO: null/String/Boolean/Number/Map/List/record 분기. record는 getRecordComponents()+getAccessor().invoke (ITE는 getCause로 풀어 재던짐). 미지 타입은 IllegalArgumentException");
    }

    /**
     * {@code Map}을 {@code JsonObject}로 — 각 엔트리의 키는 {@code toString()}, 값은 {@link #toJson} 재귀. 키 순서를
     * 보존하라({@code LinkedHashMap}).
     *
     * <p>힌트: 엔트리 순회로 {@code LinkedHashMap<String,JsonValue>}를 채워 {@code JsonValue.object(...)} 반환.
     */
    public static JsonValue fromMap(Map<String, ?> map) {
        throw new UnsupportedOperationException(
                "TODO: 엔트리마다 key.toString()→toJson(value) 재귀로 LinkedHashMap 채워 JsonValue.object(...). 순서 보존");
    }
}
