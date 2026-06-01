package study.chapter14;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 바이트 스트림 복사·소진 유틸 — IO의 "hello world". {@code read()}의 EOF(-1) 계약과 {@code byte[]}
 * 버퍼 루프를 손으로 다룬다.
 *
 * <p>모든 입력은 {@link java.io.ByteArrayInputStream}, 출력은 {@link java.io.ByteArrayOutputStream}으로
 * 다룬다(실제 파일/소켓 없음 → 완전 결정적). 여기서 만드는 복사 루프가 ch15에서 소켓→소켓 릴레이,
 * ch16에서 본문 전달의 기본형이 된다.
 */
public final class StreamUtils {

    private StreamUtils() {
    }

    static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * {@code in}을 끝(EOF)까지 읽어 {@code out}에 그대로 복사하고, 복사한 총 바이트 수를 반환한다.
     * 스트림을 닫거나 flush하지 않는다(호출자 책임).
     *
     * <p>힌트: {@code byte[] buf}를 두고 {@code while ((n = in.read(buf)) != -1) out.write(buf, 0, n);}.
     * <strong>함정</strong>: 반드시 {@code write(buf, 0, n)} — 읽은 만큼만 쓴다({@code write(buf)}로 통째
     * 쓰면 마지막 회전에서 버퍼 뒤쪽 쓰레기/0이 섞인다). 비교는 {@code != -1}({@code read}가 0을 반환할 수도
     * 있는데 그건 EOF가 아니다).
     *
     * @return 복사한 총 바이트 수 (누적이 int를 넘을 수 있어 long)
     * @throws NullPointerException {@code in} 또는 {@code out}이 null이면
     * @throws IOException          읽기/쓰기 중 I/O 오류
     */
    public static long copy(InputStream in, OutputStream out) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: byte[] 버퍼 루프로 read != -1 동안 write(buf,0,n) 복사, 누적 바이트 수(long) 반환");
    }

    /**
     * {@code in}을 끝까지 읽어 전체 내용을 새 {@code byte[]}로 반환한다({@code InputStream.readAllBytes}의
     * 축소판). 입력이 비어 있으면 길이 0 배열(null 아님).
     *
     * <p>힌트: {@link java.io.ByteArrayOutputStream}에 {@link #copy}하듯 모은 뒤 {@code toByteArray()}.
     *
     * @throws IOException I/O 오류
     */
    public static byte[] readAllBytes(InputStream in) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: 동적 버퍼(ByteArrayOutputStream)에 EOF까지 모아 byte[] 반환 (빈 입력은 길이 0)");
    }
}
