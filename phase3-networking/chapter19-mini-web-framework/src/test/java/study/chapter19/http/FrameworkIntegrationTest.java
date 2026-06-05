package study.chapter19.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import study.chapter19.core.Middlewares;
import study.chapter19.routing.Router;
import study.chapter17.Headers;
import study.chapter17.HttpRequest;
import study.chapter17.HttpResponse;
import study.chapter17.RequestLine;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ★ <strong>캡스톤 통합</strong> — 라우터 + 미들웨어 + JSON 직렬화 + ch18 봉합선이 한 흐름으로. <strong>코어만 채우면
 * 저절로 초록불</strong>이 되는 마무리 카타르시스("프레임워크는 18단원의 합성이다").
 *
 * <p>main에선 ch17/ch18 스텁 때문에 빨간불 — 챕터 순서대로 풀면 GET /users/{id}가 JSON·200·경로변수까지 끝까지 흐른다.
 */
@DisplayName("프레임워크 통합 — 라우터×미들웨어×JSON×ch18 봉합선 (전 계층)")
class FrameworkIntegrationTest {

    private static HttpRequest req(String method, String path) {
        return new HttpRequest(new RequestLine(method, path, "HTTP/1.1"), new Headers(), null);
    }

    @Test
    void GET_users_id가_JSON_200_경로변수까지_끝까지() {
        List<String> sink = new ArrayList<>();
        GreetingController controller = new GreetingController();
        Router<HttpContext, HttpResponse> router = new Router<HttpContext, HttpResponse>()
                .route("GET", "/users/{id}", controller::getUser)
                .route("GET", "/hello", controller::hello);

        HttpAdapter app = new HttpAdapter(router, Middlewares.logging("req", sink));

        HttpResponse res = app.handle(req("GET", "/users/7"));
        assertEquals(200, res.line().code());
        assertEquals("{\"id\":7,\"name\":\"user-7\"}", new String(res.body(), StandardCharsets.UTF_8));
        assertEquals(List.of("req-before", "req-after"), sink, "전 흐름이 미들웨어 양파를 통과");
    }

    @Test
    void 미등록_경로는_404() {
        Router<HttpContext, HttpResponse> router = new Router<HttpContext, HttpResponse>()
                .route("GET", "/hello", new GreetingController()::hello);
        HttpAdapter app = new HttpAdapter(router);
        assertEquals(404, app.handle(req("GET", "/missing")).line().code());
    }
}
