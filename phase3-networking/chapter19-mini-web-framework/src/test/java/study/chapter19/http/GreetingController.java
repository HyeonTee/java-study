package study.chapter19.http;

import study.chapter19.json.JsonReflect;
import study.chapter19.json.JsonValue;
import study.chapter19.json.User;
import study.chapter17.HttpResponse;

/**
 * 수동 라우트 등록 데모용 컨트롤러 — {@link FrameworkIntegrationTest}가 {@code controller::getUser}처럼 메서드 참조로
 * {@code Handler<HttpContext, HttpResponse>}에 꽂는다. <strong>완성 제공 픽스처</strong>.
 *
 * <p>각 메서드가 곧 핸들러다 — 도메인 객체를 {@link JsonReflect}/{@link JsonValue}로 만들어 {@link HttpResponses#json}으로
 * 응답한다(핸들러는 JSON 문자열을 직접 짜지 않는다 — 직렬화는 응답 경계가).
 */
public final class GreetingController {

    /** {@code GET /users/{id}} — 경로변수 id로 User를 만들어 JSON 응답. */
    public HttpResponse getUser(HttpContext ctx) {
        long id = Long.parseLong(ctx.pathVar("id"));
        return HttpResponses.json(JsonReflect.toJson(new User(id, "user-" + id)));
    }

    /** {@code GET /hello} — 고정 인사 JSON. */
    public HttpResponse hello(HttpContext ctx) {
        return HttpResponses.json(JsonValue.of("hello"));
    }
}
