package study.chapter17;

import study.chapter16.Headers;
import study.chapter16.HttpResponse;
import study.chapter16.StatusLine;

import java.nio.charset.StandardCharsets;

/**
 * {@link HttpResponse} 빌더 — <strong>"길이 프레이밍 확정"을 보장하는 단일 책임 지점</strong>. <strong>완성 제공</strong>.
 *
 * <p>왜 필요한가: ch16 {@code HttpMessageWriter}는 {@code Content-Length}를 <strong>자동으로 안 붙인다</strong>(호출자
 * 책임). 그런데 keep-alive에서 응답에 길이 헤더가 없으면 클라이언트는 <strong>다음 응답이 어디서 시작하는지 모른다</strong>
 * → 파이프라인이 깨진다. 그래서 모든 응답을 이 빌더로 만들어 {@code Content-Length}를 항상 확정한다(길이 프레이밍이
 * keep-alive의 전제).
 *
 * <p>완성 제공이지만, 내부적으로 ch16 {@link Headers#with}를 호출하므로 ch16을 풀기 전(main)에는 이 메서드들도
 * {@code UnsupportedOperationException}을 던진다 — Gradle 의존(ch16 스텁) 때문. ch16 풀이 후 동작한다.
 */
public final class Responses {

    private Responses() {
    }

    /** 200 OK + {@code Content-Length} 자동. */
    public static HttpResponse ok(byte[] body) {
        Headers headers = new Headers().with("Content-Length", String.valueOf(body.length));
        return new HttpResponse(new StatusLine("HTTP/1.1", 200, "OK"), headers, body);
    }

    /** 임의 상태코드 + UTF-8 텍스트 바디 + {@code Content-Length} 자동. */
    public static HttpResponse text(int code, String reason, String body) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Headers headers = new Headers()
                .with("Content-Type", "text/plain; charset=utf-8")
                .with("Content-Length", String.valueOf(bytes.length));
        return new HttpResponse(new StatusLine("HTTP/1.1", code, reason), headers, bytes);
    }

    /** 에러 응답(400/413/431 등) — 빈 바디 + {@code Content-Length: 0} + {@code Connection: close}. */
    public static HttpResponse error(int code, String reason) {
        Headers headers = new Headers()
                .with("Content-Length", "0")
                .with("Connection", "close");
        return new HttpResponse(new StatusLine("HTTP/1.1", code, reason), headers, new byte[0]);
    }
}
