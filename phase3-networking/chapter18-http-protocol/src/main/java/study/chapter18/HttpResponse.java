package study.chapter18;

import java.util.Objects;

/**
 * HTTP 응답 메시지 — 상태줄 + 헤더 + 바디. <strong>완성 제공</strong>(채우지 않는다). {@link HttpRequest}와 데칼코마니.
 *
 * <p>{@link HttpRequest}와 동일하게 {@code byte[] body}는 record 불변성의 예외다 — round-trip 비교는
 * {@code Arrays.equals(res.body(), parsed.body())}로.
 */
public record HttpResponse(StatusLine line, Headers headers, byte[] body) {

    public HttpResponse {
        Objects.requireNonNull(line, "line");
        Objects.requireNonNull(headers, "headers");
        if (body == null) {
            body = new byte[0];
        }
    }
}
