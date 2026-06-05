package study.chapter16;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * "4바이트 int 길이 프리픽스 + 페이로드" 프레임을 {@link ByteBuffer}로 인코딩/디코딩한다 — 간판이자
 * <strong>ch18 HTTP {@code Content-Length} 프레이밍의 원형</strong>. 라인 프레이밍(구분자 기반)과 대조되는
 * 두 번째 프레이밍 패러다임(길이 기반)이다.
 *
 * <p>와이어 포맷(big-endian, 네트워크 바이트 순서): <pre>[ 4바이트 int 길이 N ][ N바이트 페이로드 ]</pre>
 *
 * <p><strong>flip 함정</strong>: {@code put}/{@code putInt} 후 버퍼의 position은 데이터 끝에 있다. 내용을
 * 꺼내거나 읽으려면 반드시 {@code flip()}으로 (limit=position, position=0) 뒤집어야 한다 — flip을 빼먹으면
 * 빈/쓰레기를 읽는다. ByteBuffer 불변식: {@code 0 ≤ position ≤ limit ≤ capacity}. 기본 바이트 순서는 BIG_ENDIAN.
 */
public final class FrameCodec {

    private FrameCodec() {
    }

    /**
     * {@code payload}를 {@code [길이][페이로드]} 프레임 바이트 배열로 인코딩한다.
     *
     * <p>힌트: {@code ByteBuffer buf = ByteBuffer.allocate(4 + payload.length);} →
     * {@code buf.putInt(payload.length);} → {@code buf.put(payload);} → {@code buf.flip();} 후 남은 바이트를
     * {@code byte[]}로 꺼낸다(기본 big-endian이라 {@code putInt}면 길이가 자동으로 BE).
     *
     * @throws NullPointerException {@code payload}가 null이면
     */
    public static byte[] encode(byte[] payload) {
        throw new UnsupportedOperationException(
                "TODO: allocate(4+len) → putInt(len) → put(payload) → flip → 남은 바이트를 byte[]로");
    }

    /**
     * 읽기 모드(flip/wrap된) 버퍼에서 한 프레임을 디코딩해 <strong>페이로드만</strong> 반환한다. 호출 후
     * {@code buffer}의 position은 이 프레임의 끝(다음 프레임 시작)으로 전진한다 — 같은 버퍼에서 연속 프레임을
     * 이어 읽을 수 있다.
     *
     * <p>힌트: {@code int len = buffer.getInt();} (음수면 {@link IllegalArgumentException}) →
     * {@code byte[] p = new byte[len];} → {@code buffer.get(p);} ({@code get}이 남은 바이트보다 많이 요구하면
     * {@link BufferUnderflowException}을 자연히 던진다 = 불완전 프레임).
     *
     * @throws IllegalArgumentException 길이가 음수이면
     * @throws BufferUnderflowException 남은 바이트가 길이/페이로드에 못 미치면(불완전 프레임)
     */
    public static byte[] decode(ByteBuffer buffer) {
        throw new UnsupportedOperationException(
                "TODO: getInt로 길이 읽고 음수 검증, 그만큼 get해서 페이로드 byte[] 반환");
    }
}
