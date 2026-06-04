package study.chapter18.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("JsonWriter — sealed switch 직렬화 + 이스케이프")
class JsonWriterTest {

    @Test
    void escape_특수문자() {
        assertEquals("a\\\"b", JsonWriter.escape("a\"b"));   // 따옴표
        assertEquals("a\\\\b", JsonWriter.escape("a\\b"));   // 백슬래시
        assertEquals("line\\nbreak", JsonWriter.escape("line\nbreak"));
        assertEquals("tab\\t", JsonWriter.escape("tab\t"));
        assertEquals("\\u0000", JsonWriter.escape(String.valueOf((char) 0)));  // 제어문자 NUL
    }

    @Test
    void 스칼라_리터럴() {
        assertEquals("\"hi\"", JsonWriter.write(JsonValue.of("hi")));
        assertEquals("1", JsonWriter.write(JsonValue.of(1)));        // 정수는 1 (1.0 아님)
        assertEquals("true", JsonWriter.write(JsonValue.of(true)));
        assertEquals("null", JsonWriter.write(JsonValue.nul()));
    }

    @Test
    void 배열_콤마_구분() {
        JsonValue arr = JsonValue.array(List.of(JsonValue.of(1), JsonValue.of(2), JsonValue.of(3)));
        assertEquals("[1,2,3]", JsonWriter.write(arr), "마지막 원소 뒤 콤마 없음");
    }

    @Test
    void 객체_키순서_보존_및_중첩() {
        Map<String, JsonValue> inner = new LinkedHashMap<>();
        inner.put("id", JsonValue.of(1));
        inner.put("name", JsonValue.of("Ann"));

        Map<String, JsonValue> outer = new LinkedHashMap<>();
        outer.put("user", JsonValue.object(inner));
        outer.put("active", JsonValue.of(true));

        assertEquals("{\"user\":{\"id\":1,\"name\":\"Ann\"},\"active\":true}",
                JsonWriter.write(JsonValue.object(outer)));
    }

    @Test
    void 객체_키도_이스케이프() {
        Map<String, JsonValue> m = new LinkedHashMap<>();
        m.put("a\"b", JsonValue.of("v"));
        assertEquals("{\"a\\\"b\":\"v\"}", JsonWriter.write(JsonValue.object(m)));
    }
}
