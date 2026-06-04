package study.chapter18.routing;

import study.chapter18.core.Handler;

import java.util.Map;

/**
 * {@link Router#match}의 결과 묶음 — 매칭된 핸들러 + 추출된 경로변수. <strong>완성 제공</strong>(ch06 record).
 *
 * @param handler  매칭된 핸들러
 * @param pathVars 패턴에서 추출한 경로변수(예: {@code {id=42}}). 변수 없으면 빈 맵.
 */
public record RouteMatch<Req, Res>(Handler<Req, Res> handler, Map<String, String> pathVars) {
}
