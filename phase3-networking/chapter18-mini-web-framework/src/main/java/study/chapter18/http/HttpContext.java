package study.chapter18.http;

import study.chapter18.core.Routable;
import study.chapter16.HttpRequest;

import java.util.Map;

/**
 * ch16 {@link HttpRequest}를 <strong>그대로 감싸고</strong> 추출된 경로변수를 곁들인 라우팅 컨텍스트 — {@code routing.Router}의
 * {@code Req}로 쓰인다. <strong>완성 제공</strong>.
 *
 * <p><strong>이전 단원 재사용</strong>: 별도 요청 모델을 새로 만들지 않고 진짜 ch16 {@link HttpRequest}(요청라인·헤더·바디)를
 * 합성한다. {@link #method}/{@link #path}는 {@code request.line()}에서 끌어온다. 경로변수는 {@code request} 본문엔 없는
 * 정보라 여기서 곁들인다({@link Routable#withPathVars}로 매칭 후 주입).
 *
 * <p>{@code Routable<HttpContext>}를 구현해 자기참조 제네릭({@link Routable}의 {@code Self})을 만족시킨다 —
 * {@link #withPathVars}가 {@code HttpContext}를 그대로 돌려주므로 {@code Router<HttpContext, ...>}의 {@code Req}가 흐트러지지 않는다.
 *
 * @param request  진짜 ch16 요청
 * @param pathVars 패턴에서 추출된 경로변수(불변)
 */
public record HttpContext(HttpRequest request, Map<String, String> pathVars) implements Routable<HttpContext> {

    public HttpContext {
        pathVars = Map.copyOf(pathVars);   // 방어적 복사(불변)
    }

    /** 경로변수 없는 초기 컨텍스트 — {@link HttpAdapter}가 매칭 전에 만든다. */
    public static HttpContext of(HttpRequest request) {
        return new HttpContext(request, Map.of());
    }

    @Override
    public String method() {
        return request.line().method();
    }

    @Override
    public String path() {
        return request.line().target();
    }

    @Override
    public HttpContext withPathVars(Map<String, String> pathVars) {
        return new HttpContext(request, pathVars);
    }

    /** 경로변수 단일 조회 편의(예: {@code ctx.pathVar("id")}). 없으면 {@code null}. */
    public String pathVar(String name) {
        return pathVars.get(name);
    }
}
