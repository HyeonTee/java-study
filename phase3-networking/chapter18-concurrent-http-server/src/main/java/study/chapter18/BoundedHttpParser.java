package study.chapter18;

import study.chapter17.HttpProtocolException;
import study.chapter17.HttpRequest;

import java.io.IOException;
import java.io.InputStream;

/**
 * ch17 파서를 <strong>한계 강제(DoS 방어) + keep-alive 종료 신호</strong> 버전으로 감싸는 데코레이션.
 * (ch17 {@code HttpParser}는 손대지 않는다 — 그 위에 한 겹.)
 *
 * <p><strong>이 단원의 선행-연결 핵심</strong>: ch17 {@code HttpParser.parse}는 즉시 EOF면 {@link HttpProtocolException}을
 * 던진다(1왕복 계약 — "요청이 와야 한다"). 그러나 keep-alive 루프는 정반대가 필요하다: 요청 사이의 <strong>조용한 EOF는
 * 정상 종료</strong>(클라가 더 안 보냄)다. 그래서 {@link #parseOrNull}은 요청 전 EOF에서 <strong>예외가 아니라
 * {@code null}</strong>을 반환한다. <strong>ch17 {@code parse}를 keep-alive 루프에 그대로 쓰면 안 된다</strong>(idle
 * 종료마다 예외가 터진다). 이 계약 차이가 N왕복 서버의 핵심이다.
 *
 * <p>DoS 방어: 한계는 "다 읽고 검사"가 아니라 <strong>누적 중 즉시 차단</strong>한다(이미 다 읽었으면 OOM이라 늦다).
 */
public final class BoundedHttpParser {

    private BoundedHttpParser() {
    }

    /**
     * ch17 {@code HttpParser.readLine}과 같은 계약(한 바이트씩 {@code '\n'}까지, 끝 {@code '\r'} 제거, UTF-8, 즉시
     * EOF면 {@code null}, 빈 줄 {@code ""})이되, <strong>누적 바이트가 {@code maxBytes}를 넘는 순간 즉시</strong>
     * {@link RequestLimitException}(431)을 던진다(끝까지 안 읽음 — slowloris/거대 헤더 방어).
     *
     * <p>힌트: ch17 readLine 로직(풀이 재사용 가능)에 바이트 카운터를 더해, 카운터가 {@code maxBytes}를 넘으면
     * 즉시 {@code throw new RequestLimitException(431, ...)}.
     *
     * @throws IOException I/O 오류 또는 {@link RequestLimitException}
     */
    public static String readLineBounded(InputStream in, int maxBytes) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: read() 한 바이트씩 누적(ch17 readLine 회수) + 카운터. maxBytes 초과 순간 즉시 RequestLimitException(431). EOF→null, 빈 줄 \"\"");
    }

    /**
     * {@link #readLineBounded}를 빈 줄({@code ""})이 나올 때까지 반복해 {@code study.chapter17.Headers}를 모은다.
     * 헤더 줄 수가 {@code limits.maxHeaderCount()}를 넘으면 {@link RequestLimitException}(431). 빈 줄 없이 EOF면
     * {@link HttpProtocolException}(잘린 헤더).
     *
     * <p>힌트: 각 줄 {@code split(":", 2)}로 name/value(value trim)는 ch17 {@code readHeaders} 로직 회수.
     * {@code maxHeaderLineBytes}를 {@code readLineBounded}에 넘기고, 줄 수 카운트가 한도를 넘으면 431.
     *
     * @throws IOException I/O 오류 / {@link RequestLimitException} / {@link HttpProtocolException}
     */
    public static study.chapter17.Headers readHeadersBounded(InputStream in, ServerLimits limits) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: readLineBounded(maxHeaderLineBytes) 반복, \"\"이면 종료. split(\":\",2)→name/value. 줄 수>maxHeaderCount면 431, null로 끝나면 HttpProtocolException");
    }

    /**
     * 요청 한 개를 한계 안에서 파싱한다(ch17 {@code parse}의 bounded 버전).
     *
     * <p><strong>핵심 차이</strong>: 요청을 읽기 전 <strong>즉시 EOF면 {@code null}</strong>을 반환한다(keep-alive
     * 루프의 정상 종료 신호 — "다음 요청 없음"). ch17 {@code parse}는 여기서 예외를 던졌다(N왕복 계약이라 다르다).
     *
     * <p>힌트: 첫 {@code readLineBounded(maxRequestLineBytes)}가 {@code null}이면 곧장 {@code null} 반환. 아니면
     * ch17 {@code parseRequestLine} → {@code readHeadersBounded} → {@code BodyFraming.from} 분기로 바디. 바디 길이
     * ({@code Content-Length})가 {@code maxBodyBytes}를 넘으면 {@link RequestLimitException}(413). 바디는 ch17
     * {@code readFully}/{@code ChunkedBody.decode} 회수.
     *
     * @return 파싱된 요청, 또는 요청 전 조용한 EOF면 {@code null}
     * @throws IOException I/O 오류 / {@link RequestLimitException} / {@link HttpProtocolException}
     */
    public static HttpRequest parseOrNull(InputStream in, ServerLimits limits) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: 첫 줄이 null(요청 전 EOF)이면 null 반환(ch17 parse는 여기서 예외 — N왕복 계약). 아니면 parseRequestLine→readHeadersBounded→BodyFraming 분기. Content-Length>maxBodyBytes면 413");
    }
}
