package study.chapter16;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 길이 프리픽스 프레이밍을 <strong>스트림 위에서</strong> 완성한다 — 라인 프레이밍의 대안이자
 * <strong>ch15 {@code FrameCodec} → ch17 HTTP {@code Content-Length}</strong>의 다리.
 *
 * <p>와이어 포맷(big-endian, 네트워크 바이트 순서): <pre>[ 4바이트 int 길이 N ][ N바이트 페이로드 ]</pre>
 *
 * <p><strong>ch15와의 차이(정직한 가교)</strong>: ch15 {@code FrameCodec}은 {@code ByteBuffer}(이미 완성된 버퍼)를
 * 가정했다. 그러나 실제 소켓엔 "완성된 버퍼"가 없다 — 바이트가 찔끔찔끔 도착한다. 그래서 ch16의 새 살은
 * <strong>"정확히 N바이트가 모일 때까지 채워 읽는 루프"</strong>({@link #readFully})다. 이것이 길이 프레이밍의
 * 본질이자 ch15 copy 루프의 응용이다.
 *
 * <p><strong>부분 읽기(short read)</strong>: {@code read(b, off, len)}은 요청보다 <strong>적게</strong> 줄 수 있다
 * (TCP 단편화). "N바이트 정확히"를 읽으려면 반드시 루프해야 한다.
 */
public final class LengthFramedTransport {

    private LengthFramedTransport() {
    }

    /**
     * {@code payload}를 {@code [4바이트 BE 길이][페이로드]} 프레임으로 {@code out}에 쓰고 <strong>flush</strong>한다.
     *
     * <p>힌트: 길이를 4바이트 빅엔디안으로 먼저 쓰고(예: {@code ByteBuffer.allocate(4).putInt(len)} 또는
     * 시프트 연산) 페이로드를 잇는다. ch15 {@code FrameCodec.encode(payload)}로 바이트를 만든 뒤 그대로 흘려도 된다.
     *
     * @throws NullPointerException {@code payload}가 null이면
     * @throws IOException I/O 오류
     */
    public static void writeFrame(OutputStream out, byte[] payload) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: 4바이트 BE 길이 + 페이로드를 write 후 flush (FrameCodec.encode를 재사용해도 좋다)");
    }

    /**
     * {@code in}에서 <strong>정확히 {@code n}바이트</strong>를 채워 읽어 새 배열로 반환한다.
     *
     * <p>핵심: {@code read(b, off, len)}이 요청보다 적게 줄 수 있으므로 채워질 때까지 루프한다. 종료 규칙:
     * <ul>
     *   <li>한 바이트도 못 읽고 <strong>즉시 EOF</strong>면 {@code null}을 반환한다(= 프레임 경계에서의 깔끔한 스트림 끝).</li>
     *   <li>일부만 읽고 <strong>도중에 EOF</strong>가 오면 {@link EOFException}을 던진다(= truncated, 잘린 데이터).</li>
     * </ul>
     *
     * @throws EOFException 일부만 읽힌 채 EOF에 도달하면(잘림)
     * @throws IOException  I/O 오류
     */
    public static byte[] readFully(InputStream in, int n) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: off가 n이 될 때까지 read(b,off,n-off) 루프. 첫 read가 즉시 -1이면 null, 채우다 -1이면 EOFException");
    }

    /**
     * 한 프레임을 읽어 <strong>페이로드만</strong> 반환한다. 같은 {@code in}에서 연속 프레임을 이어 읽을 수 있다.
     *
     * <p>힌트: 먼저 {@code readFully(in, 4)}로 길이 4바이트를 읽는다 — {@code null}이면(프레임 경계에서 EOF)
     * 깔끔한 스트림 끝이므로 {@code null}을 반환한다. 4바이트를 BE int 길이로 해석하되 <strong>음수면</strong>
     * {@link IllegalArgumentException}. 그 길이만큼 {@code readFully}로 페이로드를 채운다(페이로드 도중 EOF면
     * {@code readFully}가 {@link EOFException}을 던진다 = 불완전 프레임).
     *
     * @return 페이로드, 또는 스트림이 프레임 경계에서 끝났으면 {@code null}
     * @throws IllegalArgumentException 길이가 음수이면
     * @throws EOFException             페이로드가 길이에 못 미친 채 EOF면(잘림)
     * @throws IOException              I/O 오류
     */
    public static byte[] readFrame(InputStream in) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: readFully(in,4)==null이면 null(깔끔한 끝). 4바이트를 BE int 길이로(음수면 예외), 그만큼 readFully로 페이로드");
    }
}
