package study.chapter18.routing;

import study.chapter18.core.Routable;

import java.util.Map;

/**
 * 라우팅 단위 테스트용 최소 {@link Routable} — ch16/http 무관하게 {@link Router}/{@link PathPattern}만 검증하기 위한
 * 픽스처(메서드·경로·경로변수만 든 경량 컨텍스트). record 자동 접근자가 {@code method()}/{@code path()}를 제공한다.
 */
record TestRoute(String method, String path, Map<String, String> pathVars) implements Routable<TestRoute> {

    static TestRoute of(String method, String path) {
        return new TestRoute(method, path, Map.of());
    }

    @Override
    public TestRoute withPathVars(Map<String, String> pathVars) {
        return new TestRoute(method, path, pathVars);
    }

    String pathVar(String name) {
        return pathVars.get(name);
    }
}
