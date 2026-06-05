package study.chapter19;

import study.chapter18.HttpRequest;
import study.chapter18.HttpResponse;

/**
 * keep-alive <strong>Connection 협상</strong>을 순수 함수로 모은 곳 — 이 단원의 지적 정점.
 *
 * <p>소켓·스레드·스트림이 전혀 없다 → {@code HttpRequest}/{@code HttpResponse}만 입력받아 boolean/값을 반환하는
 * 순수 함수라 <strong>진리표 단위 테스트로 100% 결정적</strong>(flaky 0)으로 채점된다.
 *
 * <p>HTTP/1.1 keep-alive는 여러 신호의 합산이다: 클라이언트 의사 ∧ 서버의 max-requests 한도 ∧ 응답이 길이를 확정했는지.
 */
public final class ConnectionPolicy {

    private ConnectionPolicy() {
    }

    /**
     * 클라이언트가 keep-alive를 원하는가.
     *
     * <p>규칙: {@code Connection} 헤더에 {@code "close"}(대소문자 무시)가 있으면 {@code false}. 없으면 버전 기본값 —
     * <strong>HTTP/1.1은 기본 keep-alive({@code true})</strong>, HTTP/1.0은 {@code Connection: keep-alive}를 명시해야 {@code true}.
     *
     * <p>힌트: {@code req.headers().get("connection")}(ch18 Headers 회수)을 소문자화해 판정. 버전은 {@code req.line().version()}.
     */
    public static boolean clientWantsKeepAlive(HttpRequest req) {
        throw new UnsupportedOperationException(
                "TODO: connection 헤더에 \"close\"면 false. 없으면 HTTP/1.1=true, HTTP/1.0=keep-alive 명시해야 true");
    }

    /**
     * 이 요청을 처리한 <strong>뒤</strong> 연결을 유지할지 최종 판정.
     *
     * <p>규칙: {@link #clientWantsKeepAlive} ∧ ({@code requestsServedSoFar + 1 < maxRequestsPerConnection}).
     * max-requests에 도달하면 종료한다(자원 회수).
     *
     * <p>힌트: 두 조건의 AND. 둘 다 만족해야 keep-alive.
     */
    public static boolean shouldKeepAlive(HttpRequest req, int requestsServedSoFar, ServerLimits limits) {
        throw new UnsupportedOperationException(
                "TODO: clientWantsKeepAlive(req) && (requestsServedSoFar+1 < limits.maxRequestsPerConnection())");
    }

    /**
     * 서버가 응답에 박을 {@code Connection} 헤더를 적용한 <strong>새</strong> {@code HttpResponse}를 반환한다(불변).
     *
     * <p>힌트: {@code res.headers().with("Connection", keepAlive ? "keep-alive" : "close")}(ch18 {@code Headers.with}
     * 불변 회수)로 새 헤더를 만들고, 같은 line/body로 새 {@code HttpResponse}를 만든다.
     */
    public static HttpResponse applyConnectionHeader(HttpResponse res, boolean keepAlive) {
        throw new UnsupportedOperationException(
                "TODO: res.headers().with(\"Connection\", keepAlive?\"keep-alive\":\"close\")로 새 HttpResponse(line/body 동일)");
    }

    /**
     * 응답이 <strong>길이를 확정하지 못했으면</strong>(바디가 있는데 {@code Content-Length}도 chunked도 없음) keep-alive
     * 불가 → 반드시 close여야 한다. README 모순 박스의 코드화("길이 못 정함 → close").
     *
     * <p>힌트: {@code res.body().length > 0 && res.headers().get("Content-Length").isEmpty() && !res.headers().isChunked()}.
     * <strong>길이의 '존재 여부'만 보면 되므로 {@code get("Content-Length").isEmpty()}로 판정한다</strong> — ch18
     * {@code Headers.contentLength()}는 값을 파싱하느라 검사 예외({@code HttpProtocolException})를 던지는데, 이 메서드엔
     * {@code throws} 절이 없어 그대로 쓰면 컴파일이 안 된다(값이 아니라 헤더 유무만 필요). ({@link Responses} 빌더로 만든
     * 응답이면 항상 길이가 확정돼 {@code false}여야 정상.)
     */
    public static boolean responseMustClose(HttpResponse res) {
        throw new UnsupportedOperationException(
                "TODO: 바디>0인데 Content-Length 헤더 없고(get(\"Content-Length\").isEmpty()) chunked도 아니면 true(길이 미확정→close). 아니면 false");
    }
}
