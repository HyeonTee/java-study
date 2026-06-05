package study.chapter18;

import java.io.IOException;

/**
 * HTTP 메시지의 <strong>문법 오류(malformed)</strong>를 나타내는 단일 예외. <strong>완성 제공</strong>.
 *
 * <p><strong>{@code extends IOException}이 핵심</strong>: (1) 파서 메서드의 {@code throws IOException} 시그니처와
 * 호환되고, (2) ch17 {@code LengthFramedTransport.readFully}가 잘린 바디에서 던지는 {@link java.io.EOFException}
 * (이것도 {@code IOException})을 자연스럽게 이 타입으로 감쌀 수 있으며, (3) 호출자가 "진짜 I/O 오류"와 "프로토콜
 * 문법 오류"를 구분해 후자를 {@code 400 Bad Request}로 매핑할 수 있다(ch04 검사 예외 wrap 회수).
 */
public class HttpProtocolException extends IOException {

    private static final long serialVersionUID = 1L;

    public HttpProtocolException(String message) {
        super(message);
    }

    public HttpProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
