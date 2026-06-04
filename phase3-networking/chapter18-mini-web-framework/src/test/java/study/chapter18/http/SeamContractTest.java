package study.chapter18.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import study.chapter18.json.JsonReflect;
import study.chapter18.json.User;
import study.chapter18.routing.Router;
import study.chapter16.Headers;
import study.chapter16.HttpRequest;
import study.chapter16.HttpResponse;
import study.chapter16.RequestLine;
import study.chapter17.HttpHandler;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ★ <strong>봉합선 계약</strong> — {@link HttpAdapter}가 진짜 ch17 {@link HttpHandler}로서 ch16
 * {@link HttpRequest}→{@link HttpResponse}를 처리한다. status 번역(404/405/500)을 검증.
 *
 * <p><strong>main에선 빨간불</strong>: ch16 {@code Headers.with/get}·ch17 {@code Responses}·ch18 {@code Router} 스텁
 * 때문(챕터 순서대로 ch16·ch17·ch18을 풀면 초록불). 단, 소켓은 없다 — {@code HttpHandler}는 순수 함수라 100% 인메모리.
 */
@DisplayName("봉합선 — HttpAdapter는 진짜 ch17 HttpHandler (404/405/500 번역)")
class SeamContractTest {

    private static HttpRequest req(String method, String path) {
        return new HttpRequest(new RequestLine(method, path, "HTTP/1.1"), new Headers(), null);
    }

    private static Router<HttpContext, HttpResponse> userRouter() {
        return new Router<HttpContext, HttpResponse>()
                .route("GET", "/users/{id}", c ->
                        HttpResponses.json(JsonReflect.toJson(new User(Long.parseLong(c.pathVar("id")), "Ann"))));
    }

    @Test
    void 어댑터는_ch17_HttpHandler로서_JSON을_응답() {
        HttpHandler handler = new HttpAdapter(userRouter());   // IS-A 진짜 ch17 HttpHandler
        HttpResponse res = handler.handle(req("GET", "/users/42"));
        assertEquals(200, res.line().code());
        assertEquals("application/json; charset=utf-8", res.headers().get("Content-Type").orElseThrow());
        assertEquals("{\"id\":42,\"name\":\"Ann\"}", new String(res.body(), StandardCharsets.UTF_8));
    }

    @Test
    void NotFound는_404로_번역() {
        assertEquals(404, new HttpAdapter(userRouter()).handle(req("GET", "/nope")).line().code());
    }

    @Test
    void MethodNotAllowed는_405로_번역() {
        assertEquals(405, new HttpAdapter(userRouter()).handle(req("POST", "/users/42")).line().code());
    }

    @Test
    void 핸들러_RuntimeException은_500으로_번역() {
        Router<HttpContext, HttpResponse> boom = new Router<HttpContext, HttpResponse>()
                .route("GET", "/boom", c -> {
                    throw new IllegalStateException("handler blew up");
                });
        assertEquals(500, new HttpAdapter(boom).handle(req("GET", "/boom")).line().code());
    }
}
