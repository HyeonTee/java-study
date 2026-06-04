package study.chapter18.routing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PathPattern — 세그먼트 매칭 + 경로변수 추출")
class PathPatternTest {

    @Test
    void 정적_패턴은_정확히_일치할_때만() {
        PathPattern hello = PathPattern.compile("/hello");
        assertEquals(Map.of(), hello.match("/hello").orElseThrow());
        assertTrue(hello.match("/world").isEmpty());
    }

    @Test
    void 변수_세그먼트_추출() {
        PathPattern users = PathPattern.compile("/users/{id}");
        assertEquals(Map.of("id", "42"), users.match("/users/42").orElseThrow());
    }

    @Test
    void 세그먼트_개수가_다르면_불일치() {
        PathPattern users = PathPattern.compile("/users/{id}");
        assertTrue(users.match("/users").isEmpty());
        assertTrue(users.match("/users/42/extra").isEmpty());
    }

    @Test
    void 리터럴_불일치() {
        PathPattern users = PathPattern.compile("/users/{id}");
        assertTrue(users.match("/posts/1").isEmpty());
    }

    @Test
    void 다중_변수_순서_보존() {
        PathPattern multi = PathPattern.compile("/users/{uid}/posts/{pid}");
        Map<String, String> vars = multi.match("/users/7/posts/9").orElseThrow();
        assertEquals("7", vars.get("uid"));
        assertEquals("9", vars.get("pid"));
        assertEquals(List.of("uid", "pid"), multi.variableNames());
    }

    @Test
    void 선행_후행_슬래시_정규화() {
        assertEquals(Map.of("id", "42"),
                PathPattern.compile("/users/{id}/").match("/users/42").orElseThrow());
        assertEquals(Map.of("id", "42"),
                PathPattern.compile("/users/{id}").match("/users/42/").orElseThrow());
    }
}
