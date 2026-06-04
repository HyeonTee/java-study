package study.chapter16;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * chunked transfer-encoding <strong>디코더</strong> — 프레이밍의 세 번째 모드. ch15 길이 프레이밍({@code readFully})의
 * 텍스트 변주(16진수 길이 + CRLF)다.
 *
 * <p><strong>왜 chunked인가</strong>: 응답을 만들기 시작할 때 전체 길이를 아직 모를 때(스트리밍 생성), Content-Length를
 * 못 쓴다. 대신 본문을 "청크"로 쪼개 각 청크 앞에 그 길이를 16진수로 붙인다.
 *
 * <p><strong>와이어 포맷</strong>:
 * <pre>
 *   &lt;hex-size&gt;[;chunk-ext]CRLF      (청크 크기, 16진수)
 *   &lt;size 바이트&gt;CRLF                (청크 데이터 + 뒤따르는 CRLF — 이 CRLF는 크기에 미포함!)
 *   ...
 *   0CRLF                            (last-chunk: 크기 0)
 *   [trailer-field CRLF]*            (선택적 트레일러 헤더들)
 *   CRLF                             (빈 줄로 종료)
 * </pre>
 *
 * <p>이 단원은 <strong>디코더만</strong> 다룬다. 인코더·trailer 생성·chunk-extension 처리는 이론(README 박스)으로만.
 */
public final class ChunkedBody {

    private ChunkedBody() {
    }

    /**
     * chunked 본문 전체를 읽어 디코드된 바이트를 반환한다.
     *
     * <p>힌트(반복 루프, {@link HttpParser#readLine}/{@link HttpParser#readFully} 재사용):
     * <ol>
     *   <li>크기 줄을 {@code readLine}으로 읽고 {@code ';'}로 split해 <strong>앞부분만</strong> 취해 {@code trim}
     *       (chunk-extension 버림), {@code Long.parseLong(size, 16)}로 파싱. <strong>{@code Integer.parseInt} 금지</strong>
     *       — {@code "80000000"} 이상에서 {@code NumberFormatException}. 16진수는 대소문자 무관.</li>
     *   <li>크기가 {@code 0}이면 종료 — 단, {@code readLine}이 <strong>빈 줄({@code ""})을 반환할 때까지</strong> 트레일러
     *       줄들을 흡수하라(단일 CRLF 하나만 읽으면 트레일러 있는 스트림에서 깨진다).</li>
     *   <li>아니면 그 크기만큼 {@code readFully}로 청크 데이터를 읽어 누적({@code ByteArrayOutputStream} 감각),
     *       <strong>뒤따르는 CRLF를 {@code readLine}으로 흡수</strong>한 뒤 1번으로.</li>
     * </ol>
     * 크기가 hex가 아니거나(→{@code NumberFormatException}) 청크가 선언 크기에 못 미친 채 EOF면(→{@code readFully}의
     * {@link EOFException}) {@link HttpProtocolException}으로 감싼다(malformed = 문법 오류).
     *
     * @throws IOException I/O 오류 또는 {@link HttpProtocolException}(malformed)
     */
    public static byte[] decode(InputStream in) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: [hex크기줄(';'앞 trim, Long.parseLong base16) → 0이면 빈 줄까지 트레일러 흡수 후 종료 / 아니면 readFully(크기)로 누적+뒤 CRLF readLine 흡수] 반복. NumberFormatException·EOFException은 HttpProtocolException으로 wrap");
    }
}
