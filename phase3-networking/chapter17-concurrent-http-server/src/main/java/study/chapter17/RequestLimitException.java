package study.chapter17;

import study.chapter16.HttpProtocolException;

/**
 * 요청이 서버 한계({@link ServerLimits})를 초과했을 때 던지는 예외 — DoS 방어. <strong>완성 제공</strong>.
 *
 * <p>ch16 {@link HttpProtocolException}(malformed)를 상속해 같은 검사 예외 사슬에 얹는다. 단, "문법 오류(400)"와
 * 구분되는 "너무 큼"이라 별도 상태코드를 싣는다:
 * <ul>
 *   <li><strong>431</strong> Request Header Fields Too Large — 요청라인/헤더 줄이 너무 길거나 헤더가 너무 많음.</li>
 *   <li><strong>413</strong> Content Too Large — 바디가 {@code maxBodyBytes} 초과.</li>
 * </ul>
 *
 * <p>{@link ConnectionLoop#errorStatusFor}가 이 {@link #statusCode()}를 읽어 응답 코드로 매핑한다.
 */
public class RequestLimitException extends HttpProtocolException {

    private static final long serialVersionUID = 1L;

    private final int statusCode;

    public RequestLimitException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /** 이 한계 위반에 대응하는 HTTP 상태코드(431 또는 413). */
    public int statusCode() {
        return statusCode;
    }
}
