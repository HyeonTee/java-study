/**
 * <strong>라우팅 — {@code core} 의존</strong>. 메서드+경로패턴으로 핸들러를 고르는 디스패처.
 * {@link study.chapter20.routing.Router}(간판)·{@link study.chapter20.routing.PathPattern}(경로변수 추출)·
 * {@link study.chapter20.routing.RouteMatch}, 그리고 <strong>프레임워크-중립 예외</strong>
 * {@link study.chapter20.routing.NotFoundException}/{@link study.chapter20.routing.MethodNotAllowedException}.
 *
 * <p><strong>HTTP를 모른다</strong>: 매칭 실패를 404/405 같은 숫자가 아니라 예외로 던진다(숫자 번역은 {@code http} 패키지).
 * ch10 {@code RouteScanner}가 {@code @Route}로 만든 {@code 경로→Method} 맵을, 여기선 제네릭 {@code Handler} 값으로 일급화한다.
 */
package study.chapter20.routing;
