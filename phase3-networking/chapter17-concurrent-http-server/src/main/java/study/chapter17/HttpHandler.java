package study.chapter17;

import study.chapter16.HttpRequest;
import study.chapter16.HttpResponse;

/**
 * 요청 하나를 응답 하나로 바꾸는 <strong>주입받는 핸들러</strong> — 이 단원의 가장 중요한 경계.
 *
 * <p>ch15 {@code ConnectionHandler}(스트림 단발 대화)의 HTTP판이다. 서버 엔진({@link HttpServer}·
 * {@link ConnectionLoop})은 이 함수를 <strong>받아서 구동만</strong> 한다 — "무엇을 응답할지"는 모른다.
 *
 * <p><strong>ch18의 봉합선</strong>: ch18 mini-web-framework가 이 자리에 @Route 리플렉션 라우터·미들웨어 체인·
 * JSON 직렬화를 채운다. ch17은 라우팅을 모른다 — 테스트는 echo/fixed 같은 람다를 주입한다.
 *
 * <p>완성 제공(채우지 않는다). 핸들러는 부수효과 없는 순수 함수가 이상적이며, 공유 가변 상태를 쓰면
 * <strong>스레드 안전</strong>해야 한다(동시 연결이 같은 핸들러를 호출하므로 — ch07/ch10 회수).
 */
@FunctionalInterface
public interface HttpHandler {

    /**
     * @param request 파싱된 요청(불변)
     * @return 보낼 응답. 길이가 확정된 응답({@link Responses} 사용 권장 — keep-alive의 전제).
     */
    HttpResponse handle(HttpRequest request);
}
