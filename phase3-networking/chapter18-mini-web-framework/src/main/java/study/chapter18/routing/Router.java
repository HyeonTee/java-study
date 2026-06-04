package study.chapter18.routing;

import study.chapter18.core.Handler;
import study.chapter18.core.Middleware;
import study.chapter18.core.Routable;

/**
 * 메서드 + 경로패턴 매칭으로 {@link Handler}를 고르는 코어 디스패처 — <strong>이 단원의 간판</strong>.
 * ch02 라우트 테이블 + ch01 제네릭 + {@link PathPattern} 위에 선다.
 *
 * <p><strong>ch09 회수</strong>: ch09 {@code RouteScanner.scan}은 {@code @Route}를 리플렉션으로 읽어 {@code 경로 → Method}
 * 맵을 <strong>손배선</strong>했다. 여기 {@link #route}는 같은 일을 <strong>제네릭 {@code Handler} 값</strong>으로 일급화한다
 * (Method 대신 진짜 핸들러 객체). "{@code route(...)} 수동 등록 = {@code @Route} 스캔이 자동화하는 그것"이다 — 자동 스캔
 * 실물은 ch09가 소유하므로 여기서 다시 만들지 않는다(README 상호참조 박스).
 *
 * <p><strong>Router는 status 숫자를 모른다</strong>(프레임워크-중립 코어): 매칭 실패는 {@link NotFoundException},
 * 메서드 불일치는 {@link MethodNotAllowedException}을 <strong>던진다</strong>(404/405 같은 HTTP 숫자를 들지 않는다).
 * 숫자 번역은 {@code http.HttpAdapter}의 일이다. {@code method}는 ch16처럼 {@code String}(enum으로 닫지 않음 — "405는 응답
 * 단계의 결정").
 *
 * <p>요청 타입 {@code Req}는 {@link Routable}로 바운드된다 — {@link #asHandler}가 {@code req.method()}/{@code req.path()}로
 * 디스패치하고 추출한 경로변수를 {@code req.withPathVars(...)}로 되돌려 주입하기 때문(ch01·ch03 바운드 제네릭 회수).
 *
 * @param <Req> 요청 타입(메서드·경로를 알고 경로변수를 받을 수 있어야 함)
 * @param <Res> 응답 타입
 */
public final class Router<Req extends Routable<Req>, Res> {

    public Router() {
        // 등록된 라우트를 담을 필드는 구현하며 추가하라(등록 순서 보존 — List 권장).
    }

    /**
     * 라우트를 등록하고 {@code this}를 반환한다(빌더 체이닝). {@code pattern}은 {@link PathPattern#compile}로 컴파일해 보관.
     *
     * <p><strong>함정</strong>: 같은 {@code method + pattern} <strong>중복 등록</strong>은 {@link IllegalStateException}
     * (메시지 규약: {@code "duplicate route: <method> <pattern>"}). 등록 순서를 보존하라(먼저 등록한 라우트가 먼저 매칭).
     *
     * <p>힌트: (method, PathPattern.compile(pattern), handler)를 리스트에 추가. 중복 검사 후 {@code return this}.
     */
    public Router<Req, Res> route(String method, String pattern, Handler<Req, Res> handler) {
        throw new UnsupportedOperationException(
                "TODO: PathPattern.compile(pattern)→(method,pattern,handler)를 등록(순서보존). 같은 method+pattern 중복은 IllegalStateException(\"duplicate route: ...\"). return this");
    }

    /**
     * 등록된 라우트를 순회하며 {@code method}가 같고 {@link PathPattern#match}가 성공하는 <strong>첫</strong> 라우트를 골라
     * {@link RouteMatch}(handler + 추출 경로변수)로 반환한다.
     *
     * <p><strong>경계(핵심)</strong>: 경로는 매칭되나 메서드가 하나도 안 맞으면 {@link MethodNotAllowedException}(경로 존재를
     * 증명한 뒤 던짐), 경로 자체가 없으면 {@link NotFoundException}. 이 "던지는" 경계가 "Router는 status를 모른다"를 박는다.
     *
     * <p>힌트(ch09 {@code RouteScanner.dispatch} 회수): 두 단계로 — (1) method+path 둘 다 맞는 첫 라우트를 찾고, 있으면
     * 그 핸들러+pathVars 반환. (2) 없으면, path만 맞는 라우트가 하나라도 있었는지 보고 있으면 405, 없으면 404.
     */
    public RouteMatch<Req, Res> match(String method, String path) {
        throw new UnsupportedOperationException(
                "TODO: method+path 맞는 첫 라우트→RouteMatch(handler,pathVars). 없으면 path만 맞는 게 있었나로 405(MethodNotAllowed) vs 404(NotFound)");
    }

    /**
     * 라우터 전체를 <strong>미들웨어로 감싸인 단일 {@link Handler}</strong>로 닫는다 — 프레임워크가 ch17
     * {@code HttpHandler} 자리에 꽂힐 최종 형태({@code http.HttpAdapter}가 이걸 쓴다).
     *
     * <p>힌트(정답 코드 없이): 먼저 엔드포인트 핸들러를 만든다 — {@code req}에서 {@code req.method()}/{@code req.path()}를 뽑아
     * {@link #match}로 {@link RouteMatch}를 얻고, {@code req.withPathVars(match.pathVars())}로 경로변수를 주입한 요청을 만들어
     * {@code match.handler().handle(...)}을 호출. 그 엔드포인트에 {@code middleware.apply(...)}를 적용해 반환.
     *
     * <p><strong>함정</strong>: 경로변수 주입 시점 — 매칭 <strong>후</strong>, 핸들러 호출 <strong>전</strong>.
     */
    public Handler<Req, Res> asHandler(Middleware<Req, Res> middleware) {
        throw new UnsupportedOperationException(
                "TODO: 엔드포인트 = req -> { m=match(req.method(),req.path()); bound=req.withPathVars(m.pathVars()); return m.handler().handle(bound); }. middleware.apply(엔드포인트) 반환");
    }
}
