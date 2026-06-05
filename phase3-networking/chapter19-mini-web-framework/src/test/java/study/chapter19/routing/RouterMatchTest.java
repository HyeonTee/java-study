package study.chapter19.routing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link Router#match} — 매칭·경로변수·404/405·중복·순서. 요청은 경량 {@link TestRoute}(ch17/http 무관)를 쓴다 —
 * 라우팅 로직만 격리 검증(코어/라우팅만 풀면 green).
 */
@DisplayName("Router.match — 라우트 테이블 디스패치 (ch10 RouteScanner 승격)")
class RouterMatchTest {

    /** match → 경로변수 주입 → 핸들러 호출까지 한 번에(테스트 편의). */
    private static String run(Router<TestRoute, String> r, String method, String path) {
        RouteMatch<TestRoute, String> m = r.match(method, path);
        return m.handler().handle(TestRoute.of(method, path).withPathVars(m.pathVars()));
    }

    @Test
    void 매칭은_핸들러와_경로변수를_준다() {
        Router<TestRoute, String> r = new Router<TestRoute, String>()
                .route("GET", "/users/{id}", c -> "user:" + c.pathVar("id"));
        RouteMatch<TestRoute, String> m = r.match("GET", "/users/42");
        assertEquals(Map.of("id", "42"), m.pathVars());
        assertEquals("user:42", run(r, "GET", "/users/42"));
    }

    @Test
    void 메서드_불일치는_405() {
        Router<TestRoute, String> r = new Router<TestRoute, String>()
                .route("GET", "/users/{id}", c -> "x");
        assertThrows(MethodNotAllowedException.class, () -> r.match("POST", "/users/42"));
    }

    @Test
    void 경로_없으면_404() {
        Router<TestRoute, String> r = new Router<TestRoute, String>()
                .route("GET", "/users/{id}", c -> "x");
        assertThrows(NotFoundException.class, () -> r.match("GET", "/nope"));
    }

    @Test
    void 같은_method_pattern_중복은_IllegalState() {
        assertThrows(IllegalStateException.class, () -> new Router<TestRoute, String>()
                .route("GET", "/x", c -> "a")
                .route("GET", "/x", c -> "b"));
    }

    @Test
    void 등록_순서대로_첫_매칭_우선() {
        // 정적 /users/me를 변수 /users/{id}보다 먼저 등록 → "me"가 우선 매칭
        Router<TestRoute, String> r = new Router<TestRoute, String>()
                .route("GET", "/users/me", c -> "me")
                .route("GET", "/users/{id}", c -> "id:" + c.pathVar("id"));
        assertEquals("me", run(r, "GET", "/users/me"));
        assertEquals("id:42", run(r, "GET", "/users/42"));
    }
}
