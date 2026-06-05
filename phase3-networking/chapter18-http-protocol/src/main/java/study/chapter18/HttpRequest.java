package study.chapter18;

import java.util.Objects;

/**
 * HTTP 요청 메시지 — 요청라인 + 헤더 + 바디. <strong>완성 제공</strong>(채우지 않는다).
 *
 * <p><strong>byte[] body와 record 불변성의 함정</strong>(ch09 객체 모델 회수): record의 자동 접근자 {@code body()}는
 * 내부 배열의 <strong>참조를 그대로 흘린다</strong>(외부에서 변조 가능). 또한 record의 자동 {@code equals}는 배열을
 * <strong>참조로 비교</strong>한다 — 내용이 같아도 다른 배열이면 {@code false}. 따라서
 * <strong>round-trip 테스트는 {@code req.equals(parsed)}가 아니라 {@code Arrays.equals(req.body(), parsed.body())}</strong>로
 * 바디를 비교해야 한다. (진짜 불변을 원하면 compact 생성자/접근자에서 {@code clone()} — 이 단원은 내부 전용이라 생략하고
 * 한계를 명시적으로 가르친다.)
 */
public record HttpRequest(RequestLine line, Headers headers, byte[] body) {

    public HttpRequest {
        Objects.requireNonNull(line, "line");
        Objects.requireNonNull(headers, "headers");
        if (body == null) {
            body = new byte[0];   // null 바디는 빈 바디로 정규화
        }
    }
}
