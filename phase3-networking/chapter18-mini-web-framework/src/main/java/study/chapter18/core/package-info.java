/**
 * <strong>코어 — 제네릭 엔진(의존 0)</strong>. 이 패키지는 HTTP·라우팅·JSON을 <strong>모른다</strong>(ch16/ch17은 물론
 * 같은 단원의 {@code routing}/{@code json}/{@code http}도 import하지 않는다 — {@code java.*}만).
 *
 * <p>프레임워크의 공통 화폐 {@link study.chapter18.core.Handler}와 그 변환자 {@link study.chapter18.core.Middleware}
 * (양파), 라우팅 대상 계약 {@link study.chapter18.core.Routable}, 실전 미들웨어 표본 {@link study.chapter18.core.Middlewares}.
 *
 * <p><strong>패키지 경계가 아키텍처를 강제한다</strong>: 코어가 HTTP를 모른다는 사실이 "동시성·프로토콜은 ch17, 프레임워크
 * 합성은 HTTP와 직교"라는 단원 서사를 <strong>물리적으로</strong> 박는다. ch17 {@code HttpHandler}는 사실
 * {@code Handler<HttpRequest, HttpResponse>}의 특수화일 뿐이다.
 */
package study.chapter18.core;
