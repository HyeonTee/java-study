package study.chapter19.routing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import study.chapter19.core.Handler;
import study.chapter19.core.Middleware;
import study.chapter19.core.Middlewares;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link Router#asHandler} — 라우팅 + 경로변수 주입 + 미들웨어 합성을 단일 {@link Handler}로 닫는다(프레임워크가 ch18
 * HttpHandler 자리에 꽂힐 최종 형태).
 */
@DisplayName("Router.asHandler — 라우팅+미들웨어를 단일 핸들러로")
class RouterDispatchTest {

    @Test
    void asHandler가_경로변수_주입하고_미들웨어로_감싼다() {
        List<String> sink = new ArrayList<>();
        Router<TestRoute, String> r = new Router<TestRoute, String>()
                .route("GET", "/users/{id}", c -> "user:" + c.pathVar("id"));

        Handler<TestRoute, String> app = r.asHandler(Middlewares.logging("mw", sink));

        assertEquals("user:7", app.handle(TestRoute.of("GET", "/users/7")), "경로변수가 핸들러에 주입됨");
        assertEquals(List.of("mw-before", "mw-after"), sink, "라우팅이 미들웨어 양파 안에서 동작");
    }

    @Test
    void NotFound가_asHandler_경유로_전파된다() {
        // 코어는 status를 모른다 — asHandler를 거쳐도 예외 그대로 전파(번역은 HttpAdapter의 일).
        Router<TestRoute, String> r = new Router<TestRoute, String>()
                .route("GET", "/users/{id}", c -> "x");
        Handler<TestRoute, String> app = r.asHandler(Middleware.identity());
        assertThrows(NotFoundException.class, () -> app.handle(TestRoute.of("GET", "/missing")));
    }
}
