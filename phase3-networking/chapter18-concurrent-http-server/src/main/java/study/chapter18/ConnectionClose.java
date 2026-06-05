package study.chapter18;

import study.chapter17.Headers;

import java.io.IOException;
import java.io.InputStream;

/**
 * <strong>connection-close(EOF까지) 프레이밍</strong> — ch17 {@code parseResponse}가 명시적으로 ch18로 미룬 네 번째 모드.
 *
 * <p><strong>중요(방향 정정)</strong>: 이 프레이밍은 <strong>응답 측에만</strong> 적용된다. 서버가 {@code Connection: close}
 * + {@code Content-Length} 없이 응답을 보내고 소켓을 닫으면, 클라이언트는 <strong>EOF가 곧 바디 끝</strong>이다(HTTP/1.0
 * 스타일). <strong>요청 측엔 적용 안 된다</strong> — RFC 9112 §6.3상 길이 헤더 없는 요청 바디는 길이 0이다(서버가 EOF까지
 * 읽으면 다음 요청을 영원히 못 받는다).
 *
 * <p><strong>모순(README 박스)</strong>: EOF 프레이밍 ⟂ keep-alive. EOF까지 읽으면 다음 응답 경계가 사라지므로,
 * 길이를 못 정한 응답은 정의상 그 연결의 마지막이다("길이 못 정함 → close").
 */
public final class ConnectionClose {

    private ConnectionClose() {
    }

    /**
     * {@code in}을 EOF까지 전부 읽어 누적한 바이트를 반환한다. 빈 스트림이면 빈 배열.
     *
     * <p>힌트: {@code read(buf)} 블록 읽기 루프로 {@code -1}까지 누적({@code ByteArrayOutputStream}). 여기엔 줄 경계가
     * 없으므로(라인 프레이밍과 달리) 블록 읽기를 써도 안전하다.
     *
     * @throws IOException I/O 오류
     */
    public static byte[] readUntilEof(InputStream in) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: read(buf) 루프로 EOF(-1)까지 ByteArrayOutputStream에 누적해 toByteArray()");
    }

    /**
     * <strong>응답</strong> 바디의 프레이밍을 헤더로 결정해 읽는다 — ch17 세 모드 + connection-close 네 번째 모드의 합류점.
     *
     * <p>힌트: ch17 {@code BodyFraming.from(headers)}를 {@code switch}(pattern matching)로 분기 —
     * {@code ContentLength(len)}이면 ch17 {@code HttpParser.readFully}, {@code Chunked}면 ch17
     * {@code ChunkedBody.decode}, {@code None}이면 <strong>여기서만</strong> {@link #readUntilEof}(connection-close).
     * {@code None}에서 EOF까지 읽는 건 <strong>응답을 읽을 때만</strong>이다(요청 파싱에선 None=빈 바디 — 위 클래스 주석 참조).
     *
     * @throws IOException I/O 오류
     */
    public static byte[] frameResponseBody(Headers headers, InputStream in) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: BodyFraming.from(headers) switch — ContentLength=readFully, Chunked=ChunkedBody.decode, None이면 readUntilEof(응답 측 connection-close)");
    }
}
