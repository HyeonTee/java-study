package study.chapter18;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 이 단원의 간판 — HTTP 요청/응답을 {@link InputStream}에서 파싱한다. <strong>"HTTP = 라인 프레이밍 ∘ 길이 프레이밍"</strong>을
 * 코드로 증명하는 자리.
 *
 * <p>두 개의 원시 빌딩블록이 ch17에서 그대로 온다:
 * <ul>
 *   <li>{@link #readLine}(InputStream) — ch17 {@code LineProtocol.readLine}과 <strong>같은 계약</strong>(인스턴스→static).
 *       시작줄·헤더의 <strong>라인 프레이밍</strong>.</li>
 *   <li>{@link #readFully}(InputStream,int) — ch17 {@code LengthFramedTransport.readFully}와 같은 계약. 바디의
 *       <strong>길이 프레이밍</strong>(= Content-Length).</li>
 * </ul>
 *
 * <p>{@code Socket}을 import하지 않는다(ch17 {@code ConnectionHandler} 경계 계승) — 그래서 {@code ByteArrayInputStream}으로
 * 100% 결정적으로 테스트된다.
 */
public final class HttpParser {

    private HttpParser() {
    }

    /**
     * ch17 {@code LineProtocol.readLine}과 글자 그대로 같은 계약: 한 바이트씩 {@code '\n'}까지 누적, 끝 {@code '\r'}
     * 제거(LF·CRLF), UTF-8 디코딩, 즉시 EOF면 {@code null}, 빈 줄은 {@code ""}.
     *
     * <p><strong>반드시 한 바이트씩({@code read()})</strong> 읽어라. {@code read(buf)} 블록 읽기로 바꾸면 줄 경계를
     * 넘어 <strong>바디 첫 바이트까지 삼킨다</strong> — HTTP 파서의 고전 버그(헤더 직후 바로 바디가 붙으므로 실재 위험).
     *
     * @throws IOException I/O 오류
     */
    public static String readLine(InputStream in) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: ch17 LineProtocol.readLine과 동일 계약(인스턴스→static). 반드시 read() 한 바이트씩 — read(buf)는 헤더 경계 넘어 바디를 삼킨다");
    }

    /**
     * ch17 {@code LengthFramedTransport.readFully}와 같은 계약: 정확히 {@code n}바이트를 채워 읽어 반환. 부분 읽기를
     * 루프로 채운다. 한 바이트도 못 읽고 즉시 EOF면 {@code null}, 도중에 EOF면 {@link EOFException}(잘림).
     *
     * @throws EOFException 일부만 읽힌 채 EOF면
     * @throws IOException  I/O 오류
     */
    public static byte[] readFully(InputStream in, int n) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: ch17 readFully와 동일 — off가 n이 될 때까지 read(b,off,n-off) 루프. 즉시 EOF=null, 도중 EOF=EOFException");
    }

    /**
     * {@link #readLine}을 빈 줄({@code ""})이 나올 때까지 반복해 {@link Headers}를 모은다.
     *
     * <p>힌트: 각 줄을 첫 {@code ':'} 기준 2분할({@code line.split(":", 2)}), value 앞 공백은 {@code trim}(value 안의
     * {@code ':'}는 허용되므로 limit 2). <strong>반드시 라인 전체를 String으로 만든 뒤 split</strong>하라 — 바이트에서
     * {@code ':'}를 찾아 자르면 멀티바이트 헤더값(한글 등)이 깨진다(콜론은 ASCII라 우연히 안전할 뿐). 빈 줄 없이
     * EOF({@code readLine}이 {@code null})면 {@link HttpProtocolException}(잘린 헤더).
     *
     * @throws IOException I/O 오류
     */
    public static Headers readHeaders(InputStream in) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: readLine 반복, \"\"이면 종료. 각 줄 split(\":\", 2)→name/value(trim). null로 끝나면 HttpProtocolException");
    }

    /**
     * 요청라인 문자열을 {@link RequestLine}으로 파싱한다 — 공백(SP)으로 method/target/version 3토큰.
     *
     * <p>힌트: {@code split(" ")} 후 토큰이 <strong>정확히 3개</strong>가 아니면 {@link HttpProtocolException}. method는
     * 대문자 그대로 보존(enum 변환·검열 금지 — 미지 메서드 허용). target에 공백은 원래 불법이라 3토큰이 정확하다.
     *
     * @throws HttpProtocolException 토큰 수가 3이 아니면
     */
    public static RequestLine parseRequestLine(String line) throws HttpProtocolException {
        throw new UnsupportedOperationException(
                "TODO: split(\" \") 후 정확히 3토큰 검증(아니면 HttpProtocolException). method 보존(enum/검열 금지)");
    }

    /**
     * 상태줄 문자열을 {@link StatusLine}으로 파싱한다 — {@code version SP code SP reason}.
     *
     * <p>힌트: {@code split(" ", 3)}. 토큰이 3 미만이면 <strong>reason을 {@code ""}로 보정</strong>(빈 reason-phrase는
     * 합법, 예: {@code "HTTP/1.1 204 "} — AIOOBE 금지). code는 정수 파싱(범위 밖/비정수면 {@link HttpProtocolException}).
     * reason엔 공백이 들어갈 수 있어 limit 3이 필요하다.
     *
     * @throws HttpProtocolException 코드가 정수가 아니거나 형식이 깨지면
     */
    public static StatusLine parseStatusLine(String line) throws HttpProtocolException {
        throw new UnsupportedOperationException(
                "TODO: split(\" \", 3). 토큰<3이면 reason=\"\". code 정수 파싱(실패/범위밖이면 HttpProtocolException). reason은 공백 포함 가능");
    }

    /**
     * 요청 한 개를 파싱한다: ① {@link #readLine}→{@link #parseRequestLine} ② {@link #readHeaders}
     * ③ {@link BodyFraming#from}으로 바디 분기. 이 한 메서드가 "HTTP = 라인 ∘ 길이 프레이밍"을 증명한다.
     *
     * <p>힌트: 바디 분기를 {@code switch}(pattern matching)로 — {@code ContentLength(len)}이면 {@code readFully}
     * (단 {@code len}이 {@code int} 범위를 넘으면 {@link HttpProtocolException} — {@code readFully}는 {@code int}라
     * silent overflow 금지), {@code Chunked}면 {@link ChunkedBody#decode}, {@code None}이면 빈 바디.
     * 즉시 EOF(요청라인이 {@code null})면 {@link HttpProtocolException}. {@code readFully}의 {@link EOFException}(잘린
     * 바디)도 {@link HttpProtocolException}으로 감싼다.
     *
     * @throws IOException I/O 오류 또는 {@link HttpProtocolException}(malformed)
     */
    public static HttpRequest parse(InputStream in) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: 요청라인(라인) → 헤더(라인, 빈 줄 종료) → BodyFraming switch로 바디(ContentLength=readFully/Chunked=ChunkedBody/None=빈). long→int 범위·EOFException은 HttpProtocolException");
    }

    /**
     * 응답 한 개를 파싱한다: {@link #parseStatusLine} → {@link #readHeaders} → {@link BodyFraming} 분기.
     * {@link #parse}와 데칼코마니.
     *
     * <p>힌트: {@code Content-Length}도 {@code Transfer-Encoding}도 없으면 {@code None}(빈 바디). "Content-Length
     * 없으면 EOF까지 읽기" = connection-close 프레이밍은 <strong>ch19 경계</strong>이므로 여기서 구현하지 않는다.
     *
     * @throws IOException I/O 오류 또는 {@link HttpProtocolException}(malformed)
     */
    public static HttpResponse parseResponse(InputStream in) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: 상태줄 → 헤더 → BodyFraming 분기(ContentLength/Chunked/None). connection-close(EOF까지)는 ch19 — 여기선 None=빈 바디");
    }
}
