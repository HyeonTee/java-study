package study.chapter09;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("ObjectMapper — 필드 리플렉션으로 객체↔맵")
class ObjectMapperTest {

    final ObjectMapper mapper = new ObjectMapper();

    @Nested
    @DisplayName("toMap")
    class ToMap {

        @Test
        void Column으로_키를_매핑하고_필드명_기본값을_쓴다() {
            Map<String, Object> expected = new LinkedHashMap<>();
            expected.put("user_name", "Alice");  // @Column("user_name")
            expected.put("age", 30);              // 애너테이션 없음 → 필드명

            assertEquals(expected, mapper.toMap(new Person("Alice", 30, "tmp")));
        }

        @Test
        void Ignore_필드는_제외된다() {
            assertFalse(mapper.toMap(new Person("Alice", 30, "tmp")).containsKey("temp"));
        }

        @Test
        void static_필드는_제외된다() {
            assertFalse(mapper.toMap(new Person("Alice", 30, "tmp")).containsKey("CONSTANT"));
        }
    }

    @Nested
    @DisplayName("fromMap (역연산)")
    class FromMap {

        @Test
        void 맵에서_객체로_채운다() {
            Map<String, Object> source = Map.of("user_name", "Bob", "age", 25);
            Person p = mapper.fromMap(Person.class, source);

            assertEquals("Bob", Reflect.getField(p, "name"));
            assertEquals(25, Reflect.getField(p, "age"));
        }

        @Test
        void toMap과_fromMap은_라운드트립한다() {
            Person original = new Person("Carol", 41, null);
            Person restored = mapper.fromMap(Person.class, mapper.toMap(original));

            assertEquals("Carol", Reflect.getField(restored, "name"));
            assertEquals(41, Reflect.getField(restored, "age"));
        }
    }
}
