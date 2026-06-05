package study.chapter20.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("JsonReflect — record 컴포넌트 리플렉션 → JsonValue (ch10 회수)")
class JsonReflectTest {

    /** 짧은 표기를 위해 toJson→write를 한 번에. */
    private static String json(Object obj) {
        return JsonWriter.write(JsonReflect.toJson(obj));
    }

    @Test
    void record는_컴포넌트_순서대로_객체() {
        assertEquals("{\"id\":1,\"name\":\"Ann\"}", json(new User(1, "Ann")));
    }

    @Test
    void 스칼라_기본_매핑() {
        assertEquals("null", json(null));
        assertEquals("\"hi\"", json("hi"));
        assertEquals("7", json(7L));
        assertEquals("true", json(true));
    }

    @Test
    void list는_배열로_재귀() {
        assertEquals("[1,2,3]", json(List.of(1, 2, 3)));
    }

    @Test
    void 중첩_record는_중첩_객체로() {
        assertEquals("{\"owner\":{\"id\":1,\"name\":\"Ann\"},\"active\":true}",
                json(new Account(new User(1, "Ann"), true)));
    }

    @Test
    void map은_fromMap으로_객체() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("a", 1);
        m.put("b", "two");
        assertEquals("{\"a\":1,\"b\":\"two\"}", JsonWriter.write(JsonReflect.fromMap(m)));
    }

    @Test
    void 접근자가_던진_예외는_getCause로_풀려_재던져진다() {
        // record 접근자가 내부에서 던진 예외는 InvocationTargetException으로 감싸여 나오는데,
        // toJson은 ch10 Reflect.invoke 정신대로 getCause()로 풀어 원래 예외(IllegalStateException)를 다시 던져야 한다.
        assertThrows(IllegalStateException.class, () -> JsonReflect.toJson(new Exploding("x")));
    }

    // ── 픽스처 ──

    /** 중첩 record 직렬화 검증용. */
    record Account(User owner, boolean active) {
    }

    /** 접근자가 항상 던지는 record — getCause 언랩 검증용. */
    record Exploding(String tag) {
        @Override
        public String tag() {
            throw new IllegalStateException("accessor boom");
        }
    }
}
