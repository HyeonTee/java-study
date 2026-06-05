package study.chapter20.core;

import java.util.function.Function;

/**
 * 프레임워크의 <strong>원자</strong> — 요청 하나를 응답 하나로 바꾸는 제네릭 함수형 인터페이스. 라우터·미들웨어·핸들러가
 * 전부 이 한 타입으로 합성되는 <strong>전 계층의 공통 화폐</strong>다(ch01 제네릭 + ch04 함수형 인터페이스 회수).
 *
 * <p><strong>ch19 봉합선</strong>: ch19 {@code study.chapter19.HttpHandler}({@code HttpResponse handle(HttpRequest)})는
 * 사실 이 {@code Handler}의 특수화 — 개념상 {@code Handler<HttpRequest, HttpResponse>}다. ch19은 그 자리에 echo
 * 람다를 꽂아 테스트했고, ch20은 진짜 라우터/미들웨어/직렬화를 그 자리에 채운다({@code http.HttpAdapter} 참조).
 *
 * <p>추상 메서드 {@link #handle}은 완성 제공(시그니처). 채우는 것은 합성 default {@link #andThen} 하나뿐이다.
 *
 * @param <Req> 요청 타입
 * @param <Res> 응답 타입
 */
@FunctionalInterface
public interface Handler<Req, Res> {

    /** 요청을 받아 응답을 만든다. 핸들러는 도메인 객체(또는 응답)를 반환할 뿐, 직렬화는 응답 경계의 어댑터가 한다. */
    Res handle(Req request);

    /**
     * 이 핸들러의 <strong>결과(Res)</strong>를 {@code after}로 후처리해 새 {@code Handler<Req, Out>}를 만든다 —
     * ch04 {@code Function.andThen}의 핸들러판.
     *
     * <p><strong>이 단원의 핵심 대비</strong>: {@code andThen}은 <em>결과만</em> 후처리한다 — 요청 전(before) 가로채기는
     * <strong>못 한다</strong>(그건 {@link Middleware}의 일이다). 이 둘의 차이가 "양파(onion) vs 단순 후처리"를 가른다.
     *
     * <p>힌트(정답 코드 없이): 람다 하나를 돌려주되, 그 람다는 {@code req}를 받아 {@code this.handle(req)}의 결과에
     * {@code after}를 적용한다.
     */
    default <Out> Handler<Req, Out> andThen(Function<Res, Out> after) {
        throw new UnsupportedOperationException(
                "TODO: req -> after.apply(this.handle(req)) 형태의 Handler<Req,Out> 반환(결과만 후처리, before 가로채기는 못 함)");
    }
}
