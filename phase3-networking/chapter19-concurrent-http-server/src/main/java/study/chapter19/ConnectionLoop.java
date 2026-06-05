package study.chapter19;

import study.chapter18.HttpMessageWriter;
import study.chapter18.HttpProtocolException;
import study.chapter18.HttpRequest;
import study.chapter18.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 이 단원의 무게중심 — <strong>keep-alive 연결 루프 본체</strong>. 한 소켓에 요청 N개를
 * {@code parse → handle → write}로 반복 처리한다.
 *
 * <p>{@code java.net.Socket}을 <strong>모른다</strong> — {@link InputStream}/{@link OutputStream}만 안다(ch17
 * {@code ConnectionHandler}의 "스트림만 안다" 정신 계승). 그래서 {@code ByteArrayInputStream}/{@code ByteArrayOutputStream}으로
 * 100% 결정적으로 테스트된다(소켓 0·스레드 0·sleep 0·flaky 0). 단, ch17 {@code ConnectionHandler}를
 * <strong>implements 하지는 않는다</strong> — 그 인터페이스는 "EOF까지 단발 대화" 계약인데, keep-alive는 EOF 전에
 * (협상·max-requests로) 끝날 수 있어 계약이 다르기 때문이다.
 *
 * <p>{@link BoundedHttpParser}·{@link ConnectionPolicy}·{@link Responses}를 조립한다. <strong>소켓 close는 하지
 * 않는다</strong>(호출자 책임 — ch17 계약 회수).
 */
public final class ConnectionLoop {

    private ConnectionLoop() {
    }

    /**
     * 한 연결의 전체 대화를 처리하고, 처리한 요청 수를 반환한다(테스트 훅).
     *
     * <p>루프: {@code parseOrNull}이 {@code null}(조용한 EOF)이면 정상 종료. 아니면 {@code handler.handle}로 응답을
     * 만들고, {@link ConnectionPolicy#shouldKeepAlive}로 유지 여부를 정해 {@link ConnectionPolicy#applyConnectionHeader}로
     * {@code Connection} 헤더를 박은 뒤 ch18 {@link HttpMessageWriter#writeResponse}로 직렬화한다. keep-alive가 아니면
     * 그 요청 후 종료.
     *
     * <p>에러는 한 번 응답하고 연결 종료: {@link RequestLimitException}→{@link #errorStatusFor}로 431/413,
     * {@link HttpProtocolException}→400. <strong>에러를 accept 루프 밖으로 새어 나가게 두지 말 것</strong> — 여기서
     * 응답으로 변환한다(상위에서 {@code ignored}로 삼키면 클라가 응답을 못 받는다).
     *
     * @return 정상 처리한 요청 수
     * @throws IOException 응답 전송 자체가 실패한 경우(진짜 I/O 오류)
     */
    public static int serve(InputStream in, OutputStream out, HttpHandler handler, ServerLimits limits)
            throws IOException {
        throw new UnsupportedOperationException(
                "TODO: 루프 — parseOrNull==null이면 break(정상 종료). handler.handle→shouldKeepAlive→applyConnectionHeader→writeResponse(ch18). keepAlive 아니면 break. 처리 수 반환. 프로토콜 예외는 errorResponse로 1회 응답 후 종료(소켓 close는 호출자가)");
    }

    /**
     * 한계 초과/문법 오류를 상태코드로 매핑하는 <strong>순수 함수</strong>(분리해 단위 테스트 가능).
     *
     * <p>힌트: {@code e instanceof RequestLimitException rle}면 {@code rle.statusCode()}(431/413), 그 외
     * {@link HttpProtocolException}이면 400.
     */
    public static int errorStatusFor(IOException e) {
        throw new UnsupportedOperationException(
                "TODO: RequestLimitException이면 statusCode()(431/413), 그 외 HttpProtocolException이면 400");
    }

    /**
     * 에러 응답을 만든다 — 반드시 {@code Connection: close} + {@code Content-Length} 확정({@link Responses#error} 사용).
     * 에러는 항상 연결을 종료한다.
     *
     * <p>힌트: {@code Responses.error(code, reason)} 반환(이미 close + 길이 0 포함).
     */
    public static HttpResponse errorResponse(int code, String reason) {
        throw new UnsupportedOperationException("TODO: Responses.error(code, reason) 반환(Connection: close + 길이 확정)");
    }
}
