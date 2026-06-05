package study.chapter17;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 테스트 픽스처(완성 제공 — 채우지 않는다) — {@code read(b, off, len)} 호출마다 <strong>최대 {@code chunkSize}
 * 바이트만</strong> 돌려줘 부분 읽기(short read)를 결정적으로 강제한다.
 *
 * <p>ch16 {@code OneByteAtATimeInputStream}의 일반화다(1바이트는 {@code chunkSize == 1}의 특수 케이스).
 * 이걸로 "한 줄/한 프레임이 여러 read에 쪼개져 와도, 여러 줄이 한 read에 뭉쳐 와도 프레이밍이 정확히 복원함"을
 * <strong>소켓 없이 결정적으로</strong> 증명한다 — ch16→ch17의 진짜 다리이자 "왜 프레이밍이 필요한가"의 실증 도구.
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
