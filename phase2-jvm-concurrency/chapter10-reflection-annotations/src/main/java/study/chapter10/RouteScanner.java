package study.chapter10;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 객체를 스캔해 {@link Route} 붙은 메서드를 찾아 라우트 테이블을 만들고 경로로 디스패치한다 —
 * 이 단원의 간판. <strong>ch20 미니 웹 프레임워크가 부팅할 때 하는 바로 그 일이다.</strong>
 *
 * <p>ch09 {@code MethodTable.buildVTable}이 "이름 → body 문자열" 맵을 손으로 만들었다면, 여기서는
 * <strong>실제 애너테이션을 읽어</strong> "경로 → 진짜 {@link Method}" 맵을 만들고, 그 {@code Method}를
 * 실제로 호출한다. {@code map.put("/path", handler)}로 라우트를 손배선하는 대신, {@code @Route}를
 * 붙여두면 스캐너가 알아서 찾아 등록하는 — 프레임워크의 "마법"의 정체다.
 *
 * <p>핵심: 애너테이션은 그 자체로 라우팅하지 않는다. {@code @Route}는 <strong>메타데이터</strong>일
 * 뿐이고, "라우팅"이라는 동작은 그것을 읽는 이 스캐너가 부여한다.
 */
public final class RouteScanner {

    private RouteScanner() {
    }

    /**
     * {@code target}의 {@link Route} 붙은 메서드를 모아 "경로 → {@link Method}" 테이블을 만든다.
     *
     * <ul>
     *   <li>{@code @Route}가 없는 메서드는 제외한다.</li>
     *   <li>같은 경로가 둘 이상이면 {@link ReflectionException}(라우트 충돌).</li>
     *   <li>등록 순서를 유지하라(예: {@code LinkedHashMap}).</li>
     * </ul>
     *
     * <p>힌트: {@code target.getClass().getDeclaredMethods()}를 순회하며
     * {@code m.getAnnotation(Route.class)}가 {@code null}이 아닌 것만 모은다. 경로는
     * 애너테이션의 {@code value()}.
     */
    public static Map<String, Method> scan(Object target) {
        throw new UnsupportedOperationException(
                "TODO: @Route 메서드를 모아 경로→Method 맵을 만들어라 (없으면 제외, 중복 경로는 예외)");
    }

    /**
     * {@link #scan(Object)} 테이블에서 {@code path}에 매핑된 메서드를 {@code target} 위에서 호출하고
     * 결과를 {@link Optional}로 반환한다. 매핑이 없으면 {@code Optional.empty()}.
     *
     * <p>힌트: {@code scan}으로 {@code Method}를 얻고, 호출은 {@link Reflect#invoke} 또는
     * {@code Method.invoke}를 활용하라({@code InvocationTargetException} 주의).
     */
    public static Optional<Object> dispatch(Object target, String path, Object... args) {
        throw new UnsupportedOperationException(
                "TODO: 경로에 매핑된 메서드를 target 위에서 호출하고 결과를 Optional로 반환하라");
    }

    /**
     * 등록된 모든 경로(등록 순서).
     */
    public static Set<String> routes(Object target) {
        throw new UnsupportedOperationException(
                "TODO: scan 결과의 경로 집합을 반환하라");
    }
}
