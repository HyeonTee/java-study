package study.chapter20.http;

import study.chapter20.core.Handler;
import study.chapter20.core.Middleware;
import study.chapter20.routing.MethodNotAllowedException;
import study.chapter20.routing.NotFoundException;
import study.chapter20.routing.Router;
import study.chapter18.HttpRequest;
import study.chapter18.HttpResponse;
import study.chapter19.HttpHandler;

/**
 * ★ <strong>봉합선</strong> — ch20 제네릭 코어를 ch19 {@link HttpHandler} 자리에 꽂는 얇은 어댑터. <strong>완성 제공</strong>
 * (채우지 않는다).
 *
 * <p>이게 ch19이 코드로 한 약속("ch20이 이 자리를 채운다")을 <strong>진짜 타입으로</strong> 갚는 지점이다. ch19
 * {@code HttpHandler}({@code HttpResponse handle(HttpRequest)})를 직접 {@code implements}하므로, 코어가 완성되면 이
 * 어댑터를 그대로 ch19 {@code HttpServer}·{@code ConnectionLoop}에 주입할 수 있다(서버 코드는 한 줄도 안 바뀐다 —
 * ch19 "핸들러만 갈아끼운다" 회수).
 *
 * <p><strong>코어가 status 숫자를 모르는 대가를 여기서 갚는다</strong>: {@link Router}가 던진 {@link NotFoundException}→404,
 * {@link MethodNotAllowedException}→405, 그 외 핸들러 {@code RuntimeException}→500으로 번역한다(번역 = HTTP 경계의 일).
 *
 * <p><strong>왜 이 단원이 ch19을 진짜 의존하는가</strong>: ch18/ch19을 챕터 순서대로 풀어왔으니, 봉합선을 문서가 아니라
 * 실제 ch19 {@code HttpHandler}·ch18 {@code HttpRequest}/{@code HttpResponse}로 증명한다(이전 단원 구현 재사용). 그 대가로
 * main에선 ch18·ch19 스텁 때문에 봉합 테스트가 빨간불이지만, 순서대로 풀면 초록불이 된다.
 */
public final class HttpAdapter implements HttpHandler {

    private final Handler<HttpContext, HttpResponse> core;

    /** 라우터 + 미들웨어를 단일 핸들러로 닫아 보관한다({@link Router#asHandler}). */
    public HttpAdapter(Router<HttpContext, HttpResponse> router, Middleware<HttpContext, HttpResponse> middleware) {
        this.core = router.asHandler(middleware);
    }

    /** 미들웨어 없이(항등) 라우터만 꽂는 편의 생성자. */
    public HttpAdapter(Router<HttpContext, HttpResponse> router) {
        this(router, Middleware.identity());
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        try {
            return core.handle(HttpContext.of(request));
        } catch (NotFoundException e) {
            return HttpResponses.notFound();           // 404
        } catch (MethodNotAllowedException e) {
            return HttpResponses.methodNotAllowed();   // 405
        } catch (RuntimeException e) {
            return HttpResponses.serverError();        // 500 — 핸들러 내부 오류 격리
        }
    }
}
