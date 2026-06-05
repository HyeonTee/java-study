package study.chapter18;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 테스트 픽스처(완성 제공) — {@code read(b, off, len)}마다 최대 {@code chunkSize}바이트만 돌려줘 부분 읽기를 결정적으로
 * 강제한다. ch16/ch17에서 가져온 단편화 픽스처(ch17은 test 소스셋 package-private이라 직접 못 씀 → 복사).
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
        return delegate.read(b, off, Math.min(len, chunkSize));
    }
}
