package study.chapter18.http;

import study.chapter18.json.JsonValue;
import study.chapter18.json.JsonWriter;
import study.chapter16.Headers;
import study.chapter16.HttpResponse;
import study.chapter16.StatusLine;
import study.chapter17.Responses;

import java.nio.charset.StandardCharsets;

/**
 * ch16 {@link HttpResponse} 빌더 — 프레임워크가 응답을 만드는 자리. <strong>완성 제공</strong>.
 *
 * <p><strong>이전 단원 재사용</strong>: 텍스트·에러 응답은 ch17 {@link Responses}(ok/text/error, {@code Content-Length}
 * 자동 확정)에 그대로 위임한다. JSON 응답만 ch18이 더한다({@code application/json} + {@link JsonWriter}). 즉 ch16
 * {@link Headers}/{@link StatusLine}·ch17 {@link Responses}를 회수해, ch18은 "JSON 한 겹"만 얹는다.
 *
 * <p>ch16을 풀기 전(main)에는 ch17 {@code Responses}·ch16 {@code Headers.with}가 스텁이라 이 메서드들도
 * {@code UnsupportedOperationException}을 던진다 — 의존 단원 스텁 때문. 챕터 순서대로 ch16까지 풀면 동작한다.
 */
public final class HttpResponses {

    private HttpResponses() {
    }

    /** 200 OK + {@code application/json} + {@link JsonWriter} 직렬화 바디 + {@code Content-Length} 자동. */
    public static HttpResponse json(JsonValue body) {
        return json(200, "OK", body);
    }

    /** 임의 상태코드 + {@code application/json} 바디. */
    public static HttpResponse json(int status, String reason, JsonValue body) {
        byte[] bytes = JsonWriter.write(body).getBytes(StandardCharsets.UTF_8);
        Headers headers = new Headers()
                .with("Content-Type", "application/json; charset=utf-8")
                .with("Content-Length", String.valueOf(bytes.length));
        return new HttpResponse(new StatusLine("HTTP/1.1", status, reason), headers, bytes);
    }

    /** 텍스트 응답 — ch17 {@link Responses#text}에 위임(재사용). */
    public static HttpResponse text(int status, String reason, String body) {
        return Responses.text(status, reason, body);
    }

    /** 404 — ch17 {@link Responses#error}에 위임(Connection: close + 길이 0). */
    public static HttpResponse notFound() {
        return Responses.error(404, "Not Found");
    }

    /** 405 — ch17 {@link Responses#error}에 위임. */
    public static HttpResponse methodNotAllowed() {
        return Responses.error(405, "Method Not Allowed");
    }

    /** 500 — ch17 {@link Responses#error}에 위임. */
    public static HttpResponse serverError() {
        return Responses.error(500, "Internal Server Error");
    }
}
