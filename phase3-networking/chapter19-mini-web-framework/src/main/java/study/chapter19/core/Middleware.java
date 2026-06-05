package study.chapter19.core;

import java.util.List;

/**
 * 핸들러를 <strong>감싸는</strong> 함수형 인터페이스 — 미들웨어 파이프라인의 본질이자 <strong>이 단원의 무게중심</strong>.
 * "{@code Handler → Handler} 변환자"라서, 합성하면 호출 전(before)·후(after)를 모두 감싸는 <strong>양파(onion)</strong>가
 * 된다(ch04 함수 합성 회수).
 *
 * <p><strong>양파 vs 단순 후처리</strong>: {@link Handler#andThen}은 결과만 후처리해 before를 못 가로챈다. 미들웨어는
 * {@code next}를 받아 <em>그 호출 자체를</em> 감싸므로 before도 after도 가능하다 — 로깅·인증·에러경계가 전부 이 모양이다.
 *
 * <p>추상 메서드 {@link #wrap}은 완성 제공(시그니처). 채우는 것은 합성 메서드들({@link #andThen}/{@link #chain}/
 * {@link #apply}/{@link #identity})이다.
 *
 * @param <Req> 요청 타입
 * @param <Res> 응답 타입
 */
@FunctionalInterface
public interface Middleware<Req, Res> {

    /** {@code next} 핸들러를 감싼 새 핸들러를 반환한다(전/후 가로채기의 자리). */
    Handler<Req, Res> wrap(Handler<Req, Res> next);

    /**
     * 두 미들웨어를 하나로 합성한다 — <strong>this가 바깥 양파</strong>가 되도록.
     *
     * <p><strong>학습자 최대 오답: 합성 방향.</strong> 결과 미들웨어의 {@code wrap(h)}는 {@code this.wrap(next.wrap(h))}여야
     * 한다. 그러면 {@code this}의 before가 가장 먼저, after가 가장 나중에 실행된다(양파의 바깥). {@code MiddlewareOnionTest}의
     * sink 로그 기대 시퀀스가 이 방향을 사실상 명세한다(정답 노출 없이 순서로 강제).
     *
     * <p>힌트(정답 코드 없이): {@code h -> this.wrap(next.wrap(h))} 형태의 {@code Middleware} 반환.
     */
    default Middleware<Req, Res> andThen(Middleware<Req, Res> next) {
        throw new UnsupportedOperationException(
                "TODO: h -> this.wrap(next.wrap(h)) 반환(this가 바깥 양파 — before는 this 먼저, after는 this 나중)");
    }

    /**
     * 미들웨어를 최종 엔드포인트 핸들러에 적용해 <strong>구동 가능한 단일 핸들러</strong>를 만든다({@link #wrap}의 의미 있는
     * 별칭이자 체인 적용의 종착). {@code routing.Router.asHandler}가 이걸로 라우터+미들웨어를 한 점에 모은다.
     *
     * <p>힌트: 추상 {@code wrap(endpoint)}을 그대로 반환.
     */
    default Handler<Req, Res> apply(Handler<Req, Res> endpoint) {
        throw new UnsupportedOperationException("TODO: wrap(endpoint) 반환(미들웨어를 엔드포인트에 적용한 단일 핸들러)");
    }

    /**
     * 리스트를 단일 미들웨어로 <strong>접는다</strong>(fold). {@code chain([A,B,C])}의 before 순서는 A,B,C / after 순서는
     * C,B,A(첫 원소가 가장 바깥 양파).
     *
     * <p><strong>함정</strong>: 빈 리스트는 <strong>항등 미들웨어</strong>({@code wrap(h)==h})여야 한다 — 합성의 항등원.
     *
     * <p>힌트: {@link #identity}에서 시작해 {@link #andThen}으로 누적(reduce/접기). 정답 코드는 쓰지 말 것.
     */
    static <Req, Res> Middleware<Req, Res> chain(List<Middleware<Req, Res>> middlewares) {
        throw new UnsupportedOperationException(
                "TODO: identity()에서 시작해 andThen으로 누적(빈 리스트는 항등, 첫 원소가 가장 바깥). reduce/for 접기");
    }

    /**
     * <strong>항등 미들웨어</strong> — 아무것도 안 감싸고 {@code next}를 그대로 돌려준다({@code wrap(h)==h}). 합성의 항등원
     * (빈 체인·기본 미들웨어 자리).
     *
     * <p>힌트: {@code h -> h} 형태의 {@code Middleware} 반환.
     */
    static <Req, Res> Middleware<Req, Res> identity() {
        throw new UnsupportedOperationException("TODO: h -> h 반환(항등 미들웨어, 합성 항등원)");
    }
}
