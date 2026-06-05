package study.chapter16;

import java.io.IOException;
import java.io.InputStream;

/**
 * 위임 대상 스트림에서 읽은 누적 바이트 수를 세는 데코레이터 — 이 단원의 척추(java.io 데코레이터 패턴).
 *
 * <p>{@code new BufferedReader(new InputStreamReader(new FileInputStream(...)))}처럼 java.io가 "양파"인 이유가
 * 데코레이터다: 다른 스트림을 <strong>감싸</strong> 동작을 더한다. {@code FilterInputStream}이 그 베이스이고,
 * Buffered/GZIP 등도 전부 이 패턴이다. 여기서는 {@link InputStream}을 직접 상속해 손으로 만든다.
 *
 * <p>핵심: 모든 읽기 경로는 결국 {@code delegate}로 위임되고, <strong>실제로 읽힌 만큼만</strong> 카운터를
 * 올린다. EOF(-1)는 0바이트 — 카운트하지 않는다(여기서 -1을 그대로 더하면 카운트가 음수로 망가진다).
 */
public class CountingInputStream extends InputStream {

    private final InputStream delegate;
    private long count;

    /**
     * @throws NullPointerException {@code delegate}가 null이면
     */
    public CountingInputStream(InputStream delegate) {
        throw new UnsupportedOperationException("TODO: null 검증 후 delegate 보관, count=0");
    }

    /**
     * 한 바이트를 0~255로 반환, 끝이면 -1.
     *
     * <p>힌트: {@code delegate.read()} 결과가 {@code >= 0}일 때만 {@code count++}. -1이면 그대로 반환.
     */
    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException(
                "TODO: delegate.read() 위임, >=0이면 count 1 증가 후 반환, -1이면 그대로");
    }

    /**
     * {@code b[off..off+len)}에 최대 {@code len}바이트를 읽어 실제 읽은 개수 반환, 끝이면 -1.
     *
     * <p>힌트: {@code delegate.read(b, off, len)} 결과 {@code n}이 {@code > 0}이면 {@code count += n}, -1이면
     * 그대로 반환. (이 오버라이드를 빼면 상위 기본 구현이 {@code read()}를 len번 불러 동작은 맞지만 데코레이터의
     * 위임 의미가 흐려진다 — 반드시 위임 오버라이드하라. 상위의 {@code read(byte[])}는 이걸 호출하므로 공짜로 동작.)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: delegate.read(b,off,len) 위임, n>0이면 count += n, -1이면 그대로 반환");
    }

    /** 위임 대상을 닫는다. */
    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("TODO: delegate.close() 위임");
    }

    /** 지금까지 읽은 누적 바이트 수. */
    public long getCount() {
        throw new UnsupportedOperationException("TODO: 누적 카운트 반환");
    }
}
