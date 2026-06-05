package study.chapter17;

import java.util.Objects;

/**
 * HTTP 상태줄 — {@code version SP code SP reason} (예: {@code HTTP/1.1 200 OK}).
 *
 * <p><strong>완성 제공</strong>(채우지 않는다).
 *
 * <p><strong>reason은 빈 문자열을 허용한다</strong>: RFC 9112의 {@code reason-phrase = *( ... )}는 0글자도 합법이다
 * (예: {@code HTTP/1.1 204 }, reason 없이 trailing space). 그래서 {@code HttpParser.parseStatusLine}은
 * {@code split(" ", 3)} 후 토큰이 3개 미만이면 reason을 {@code ""}로 보정해야 한다(AIOOBE 금지).
 */
public record StatusLine(String version, int code, String reason) {

    public StatusLine {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(reason, "reason");   // 빈 문자열은 허용, null은 금지
        if (code < 100 || code > 599) {
            throw new IllegalArgumentException("상태 코드는 3자리(100~599): " + code);
        }
    }
}
