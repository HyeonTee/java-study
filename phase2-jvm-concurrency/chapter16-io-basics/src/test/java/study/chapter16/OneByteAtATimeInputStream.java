package study.chapter16;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 테스트 픽스처 — 한 번에 최대 1바이트만 돌려주는 InputStream. {@code read(byte[])}가 버퍼를 한 번에
 * 채우지 않는(부분 읽기) 상황을 결정적으로 강제해, copy 루프·카운팅의 부분 읽기 처리를 검증한다.
 */
final class OneByteAtATimeInputStream extends InputStream {

    private final ByteArrayInputStream delegate;

    OneByteAtATimeInputStream(byte[] data) {
        this.delegate = new ByteArrayInputStream(data);
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
        int one = delegate.read();   // 최대 1바이트만
        if (one == -1) {
            return -1;
        }
        b[off] = (byte) one;
        return 1;
    }
}
