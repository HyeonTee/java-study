package study.chapter19.core;

import java.util.Map;

/**
 * {@code routing.Router}가 디스패치할 수 있는 요청의 계약 — <strong>메서드·경로를 알려주고, 추출된 경로변수를 주입받을 수
 * 있는</strong> 요청. <strong>완성 제공</strong>.
 *
 * <p><strong>왜 자기참조 제네릭({@code Self})인가</strong>(ch01·ch03 바운드 제네릭의 정점): {@link #withPathVars}가
 * <strong>자기 타입</strong>을 그대로 돌려줘야({@code HttpContext.withPathVars}가 {@code HttpContext}를) {@code Handler<Req,Res>}의
 * {@code Req}가 흐트러지지 않는다. 그래서 {@code Comparable<T>}·{@code Enum<E>}처럼 {@code Routable<Self extends Routable<Self>>}로
 * 자기 자신을 바운드한다(F-bounded polymorphism). 이 덕에 경로변수가 {@code Req} 안에 올라타 {@code Handler<Req,Res>}의
 * 단일 인자 모양이 유지된다(별도 컨텍스트 인자 불필요).
 *
 * @param <Self> 구현 타입 자신(예: {@code HttpContext implements Routable<HttpContext>})
 */
public interface Routable<Self extends Routable<Self>> {

    /** 요청 메서드(GET/POST 등). */
    String method();

    /** 요청 경로(예: {@code /users/42}). */
    String path();

    /** 추출된 경로변수를 주입한 <strong>같은 타입</strong>의 새 요청을 반환한다(불변). */
    Self withPathVars(Map<String, String> pathVars);
}
