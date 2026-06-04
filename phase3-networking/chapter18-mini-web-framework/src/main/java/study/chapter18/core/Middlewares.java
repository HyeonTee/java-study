package study.chapter18.core;

import java.util.List;
import java.util.function.Function;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 실전 {@link Middleware} 표본 — 양파(onion) 모델을 구체 사례로 못박는 자리(ch04 합성 회수의 응용).
 *
 * <p><strong>결정성 규율</strong>: 시계({@code System.currentTimeMillis})·랜덤 금지. 미들웨어가 관측하는 것은 <strong>주입된
 * 수집기</strong>(로그 리스트·카운터)뿐이라, 호출 순서·횟수가 100% 결정적이다(flaky 0).
 *
 * <p>이 표본들은 모두 제네릭이라 {@code core}에 산다 — HTTP를 모른다(라우팅 예외 처리 같은 HTTP 관심사는 {@code http}
 * 패키지의 어댑터가 맡는다).
 */
public final class Middlewares {

    private Middlewares() {
    }

    /**
     * 호출 전/후를 {@code sink}에 기록하는 미들웨어 — 양파의 before/after를 눈으로 보는 도구. {@code wrap(next)}는
     * "{@code sink.add(label+"-before")} → {@code next.handle(req)} → {@code sink.add(label+"-after")} → 결과 반환"하는
     * 핸들러를 돌려준다.
     *
     * <p><strong>함정</strong>: {@code after}는 반드시 {@code next.handle} <strong>후</strong>에 기록(양파의 바깥쪽).
     * 시계 금지 — 타이밍이 아니라 <strong>호출 순서</strong>만 남긴다(결정성). {@code MiddlewareOnionTest}가 여러 logging을
     * 합성한 sink 시퀀스를 명세한다.
     *
     * <p>힌트(정답 코드 없이): {@code next -> (req -> { sink.add(...before); Res r = next.handle(req); sink.add(...after); return r; })}.
     */
    public static <Req, Res> Middleware<Req, Res> logging(String label, List<String> sink) {
        throw new UnsupportedOperationException(
                "TODO: next -> req -> { sink.add(label+\"-before\"); r=next.handle(req); sink.add(label+\"-after\"); return r; }. after는 호출 후(시계 금지)");
    }

    /**
     * 엔드포인트가 던진 {@code RuntimeException}을 잡아 {@code onError}로 폴백 응답을 만드는 양파 —
     * "{@code try/catch}를 핸들러 <strong>바깥</strong>에 한 겹". {@code wrap(next)}는
     * {@code try { next.handle(req) } catch (RuntimeException e) { onError.apply(e) }}.
     *
     * <p><strong>함정(경계)</strong>: 라우팅 예외({@code routing.NotFoundException}/{@code MethodNotAllowedException})도
     * {@code RuntimeException}이라, 여기서 무조건 삼키면 라우팅 404/405가 사라진다. 그래서 {@code onError}가 예외 타입을
     * 구분하거나(라우팅 예외는 다시 던짐), {@code http.HttpAdapter}가 라우팅 예외를 먼저 처리하도록 경계를 둔다(README 박스).
     * {@code MiddlewareErrorTest}가 검증.
     *
     * <p>힌트: {@code next -> (req -> { try return next.handle(req); catch(RuntimeException e) return onError.apply(e); })}.
     */
    public static <Req, Res> Middleware<Req, Res> errorBoundary(Function<RuntimeException, Res> onError) {
        throw new UnsupportedOperationException(
                "TODO: next -> req -> try{ next.handle(req) } catch(RuntimeException e){ onError.apply(e) }. onError가 라우팅 예외를 구분할 수 있게(삼키면 404/405 사라짐)");
    }

    /**
     * 엔드포인트가 호출된 <strong>횟수</strong>를 {@code calls}에 누적하는 미들웨어 — 시계 없는 결정적 "계측" 표본(주입된
     * 카운터만 관측). {@code wrap(next)}는 매 요청마다 {@code calls.incrementAndGet()} 후 {@code next.handle(req)}.
     *
     * <p>힌트: {@code next -> (req -> { calls.incrementAndGet(); return next.handle(req); })}. 시계·랜덤 금지(호출 수만).
     */
    public static <Req, Res> Middleware<Req, Res> counting(AtomicInteger calls) {
        throw new UnsupportedOperationException(
                "TODO: next -> req -> { calls.incrementAndGet(); return next.handle(req); }. 시계 대신 호출 횟수만(결정적)");
    }
}
