package study.chapter16;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 바이트 스트림을 줄 단위로 끊어 읽는 라인 프레이머 — 이 단원의 간판이자 <strong>ch17 소켓 프로토콜의 핵심</strong>.
 *
 * <p>스트림에는 경계가 없다(바이트가 줄줄 흐를 뿐). <strong>프레이밍</strong>이 경계를 만든다 — 여기서는
 * 줄 종결자 {@code '\n'}을 기준으로 자른다. TCP도 바이트 스트림이라 경계가 없으므로, ch17 echo 서버가
 * "라인 단위"로 도는 메커니즘이 바로 이것이다.
 *
 * <p><strong>계약</strong>(README에 고정):
 * <ul>
 *   <li>줄 종결자({@code '\n'})는 반환 문자열에 <strong>포함하지 않는다</strong>. 바로 앞의 {@code '\r'}도
 *       제거한다(LF·CRLF 모두 지원). <strong>단독 {@code '\r'}(구 Mac)은 줄 구분자로 보지 않는다.</strong></li>
 *   <li>마지막 줄에 종결자가 없어도(EOF로 끝) 그 줄을 반환한다.</li>
 *   <li>빈 줄("")과 "더 읽을 줄 없음"(null)을 구분한다 — {@code "\n"}은 빈 줄 하나([""])이고, ""(빈 입력)는 줄 없음([]).</li>
 *   <li>줄 내용은 UTF-8로 디코딩한다.</li>
 * </ul>
 */
public class LineReader {

    private final InputStream in;

    /**
     * @throws NullPointerException {@code in}이 null이면
     */
    public LineReader(InputStream in) {
        throw new UnsupportedOperationException("TODO: null 검증 후 delegate 보관");
    }

    /**
     * 다음 한 줄을 읽어 반환한다(종결자 제외). 더 읽을 줄이 없으면(즉시 EOF) {@code null}.
     *
     * <p>힌트: 한 바이트씩 읽으며 누적하다 {@code '\n'}을 만나면 줄 완성(누적 끝의 {@code '\r'}은 버린다).
     * 누적 중 EOF를 만나면: 모은 게 있으면 그걸 마지막 줄로, 한 바이트도 못 읽었으면 {@code null}.
     * 모은 바이트는 {@code StandardCharsets.UTF_8}로 디코딩하라. ({@code "\n"}만 있으면 ""를 반환 — null 아님.)
     *
     * @throws IOException I/O 오류
     */
    public String readLine() throws IOException {
        throw new UnsupportedOperationException(
                "TODO: \\n까지 누적, 끝 \\r 제거, UTF-8 디코딩. 즉시 EOF면 null, 빈 줄은 \"\"");
    }

    /**
     * {@code in}을 끝까지 읽어 모든 줄을 순서대로 담은 리스트를 반환한다(각 줄 종결자 제외). 빈 입력이면 빈 리스트.
     *
     * <p>힌트: {@link #readLine()}이 {@code null}을 줄 때까지 모은다.
     *
     * @throws IOException I/O 오류
     */
    public List<String> readLines() throws IOException {
        throw new UnsupportedOperationException("TODO: readLine()이 null일 때까지 누적해 List 반환");
    }
}
