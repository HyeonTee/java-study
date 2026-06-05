package study.chapter16;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.UnaryOperator;

/**
 * 라인 단위 request/response 송수신의 코어 — 이 단원의 간판이자 <strong>ch15 {@code LineReader}의 직계</strong>.
 *
 * <p>이 클래스는 {@code java.net.Socket}을 <strong>절대 import하지 않는다</strong> — {@link InputStream}/
 * {@link OutputStream}만 안다. 이것이 "프로토콜을 전송에서 분리"하는 경계다(→ {@link ConnectionHandler}).
 * 그래서 ch15가 {@code ByteArrayInputStream}으로 라인 프레이밍을 결정적으로 검증했듯, 소켓 위 프로토콜도
 * 똑같이 인메모리로 flaky 없이 검증된다 — 소켓 위 코드와 스트림 위 코드는 <strong>같은 코드</strong>다.
 *
 * <p><strong>왜 프레이밍이 필요한가</strong>: TCP는 경계 없는 바이트 스트림이라 {@code send("a\r\nb")} 한 번이
 * 상대에 {@code "a\r\n"} + {@code "b"} 두 read로 쪼개져 오거나, 두 번 보낸 게 한 read로 뭉쳐 올 수 있다.
 * 라인 프레이밍({@code \n} 기준 절단)이 그 경계를 복원한다.
 *
 * <p><strong>읽기 계약</strong>(ch15 {@code LineReader} 그대로):
 * <ul>
 *   <li>줄 종결자 {@code '\n'}은 반환에 포함하지 않는다. 바로 앞의 {@code '\r'}도 제거한다(LF·CRLF 모두 지원).
 *       단독 {@code '\r'}은 구분자가 아니다.</li>
 *   <li>마지막 줄에 종결자가 없어도(EOF로 끝) 그 줄을 반환한다.</li>
 *   <li>빈 줄("")과 "더 읽을 줄 없음"({@code null}, 상대 half-close=EOF)을 구분한다.</li>
 *   <li>UTF-8로 디코딩한다.</li>
 * </ul>
 *
 * <p><strong>쓰기 계약</strong>: 줄 종결자는 <strong>CRLF({@code "\r\n"})</strong>다 — HTTP·SMTP·Redis RESP가
 * 쓰는 네트워크 표준 종결자이며, ch17 HTTP로 그대로 이어진다. 읽기는 LF·CRLF 모두 흡수하므로 라운드트립이 성립한다.
 */
public final class LineProtocol {

    /** 네트워크 줄 종결자(HTTP/SMTP/RESP 표준). */
    public static final String CRLF = "\r\n";

    private final InputStream in;
    private final OutputStream out;

    /**
     * @throws NullPointerException {@code in} 또는 {@code out}이 null이면
     */
    public LineProtocol(InputStream in, OutputStream out) {
        throw new UnsupportedOperationException(
                "TODO: in/out null 검증 후 보관. 소켓은 모른다 — 스트림만. (ch15 LineReader처럼 in을 라인 프레이밍에 쓴다)");
    }

    /**
     * 다음 한 줄을 읽어 반환한다(종결자 제외). 더 읽을 줄이 없으면(즉시 EOF=상대 half-close) {@code null}.
     *
     * <p>힌트: 한 바이트씩 읽으며 누적하다 {@code '\n'}을 만나면 줄 완성(누적 끝의 {@code '\r'}은 버린다).
     * 누적 중 EOF를 만나면: 모은 게 있으면 그걸 마지막 줄로, 한 바이트도 못 읽었으면 {@code null}.
     * 모은 바이트는 UTF-8로 디코딩하라. (ch15 {@code LineReader.readLine()}과 동일한 계약 — 풀이를 재사용해도 좋다.)
     *
     * @throws IOException I/O 오류
     */
    public String readLine() throws IOException {
        throw new UnsupportedOperationException(
                "TODO: \\n까지 누적, 끝 \\r 제거, UTF-8 디코딩. null=상대가 더 안 보냄(EOF/half-close), \"\"=빈 줄 — 둘은 다르다");
    }

    /**
     * {@code line}을 UTF-8로 인코딩하고 <strong>CRLF</strong>를 붙여 {@code out}에 쓴 뒤 <strong>반드시 flush</strong>한다.
     *
     * <p>힌트: flush를 빠뜨리면 상대가 응답을 영원히 기다린다 — request/response 데드락의 단골이다.
     *
     * @throws IOException I/O 오류
     */
    public void writeLine(String line) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: (line + CRLF)를 UTF-8로 write 후 flush 필수. flush 안 하면 상대가 영원히 기다린다");
    }

    /**
     * {@link #readLine()}이 {@code null}(상대 half-close=EOF)이 될 때까지 루프: 한 줄을 읽어 {@code handler}를
     * 적용한 결과를 {@link #writeLine(String)}으로 응답한다.
     *
     * <p>힌트: {@code while ((line = readLine()) != null) writeLine(handler.apply(line));}. 상대가
     * {@code shutdownOutput()}으로 EOF를 보내면 이 루프가 <strong>임의 sleep 없이 확정적으로</strong> 종료된다 —
     * 이것이 half-close가 서버를 멈추는 메커니즘이다.
     *
     * @throws IOException I/O 오류
     */
    public void serve(UnaryOperator<String> handler) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: readLine()==null까지 while 루프, handler 적용 후 writeLine. null이 루프를 확정 종료시킨다");
    }
}
