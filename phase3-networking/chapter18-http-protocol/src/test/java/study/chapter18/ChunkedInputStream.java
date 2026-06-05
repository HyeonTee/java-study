package study.chapter18;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 테스트 픽스처(완성 제공 — 채우지 않는다) — {@code read(b, off, len)} 호출마다 <strong>최대 {@code chunkSize}
 * 바이트만</strong> 돌려줘 부분 읽기(short read)를 결정적으로 강제한다. ch17에서 그대로 가져온 단편화 픽스처.
 *
 * <p>이걸로 "헤더 한 줄이 여러 read로 쪼개져 와도", "chunk 길이 프리픽스가 read 경계에 걸쳐도", "Content-Length 바디
 * 경계가 read에 걸쳐도" 파서가 정확히 복원함을 <strong>소켓 없이 결정적으로</strong> 증명한다. 특히 {@code readLine}을
 * 한 바이트씩({@code read()}) 구현하지 않고 버퍼로 over-read하면 이 픽스처에서 깨진다.
 */
final class ChunkedInputStream extends InputStream {

    private final ByteArrayInputStream delegate;
    private final int chunkSize;

    ChunkedInputStream(byte[] data, int chunkSize) {
        if (chunkSize < 1) {
            throw new IllegalArgumentException("chunkSize >= 1");
        }
        this.delegate = new ByteArrayInputStream(data);
        this.chunkSize = chunkSize;
    }

    @Override
    public int read() {
        return delegate.read();
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (len == 0) {
            return 0;
        }
        return delegate.read(b, off, Math.min(len, chunkSize));   // 최대 chunkSize 바이트만
    }
}
