package study.chapter16;

import java.util.Objects;

/**
 * HTTP 요청라인 — {@code METHOD SP target SP version} (예: {@code GET /index.html HTTP/1.1}).
 *
 * <p><strong>완성 제공</strong>(채우지 않는다). ch06 record 회수 — 파싱에 집중하도록 모델은 미리 준다.
 *
 * <p><strong>method가 enum이 아니라 String인 이유</strong>: 진짜 파서는 미지의 메서드(PATCH·PROPFIND·커스텀)도
 * 거르지 않고 <strong>그대로 보존</strong>한다. "허용되지 않은 메서드"를 405로 거르는 건 <em>응답 단계</em>의 결정이며
 * ch17의 몫이다. enum + {@code valueOf}로 닫으면 파싱 단계에서 던져버려 파서가 "검열기"가 된다.
 */
public record RequestLine(String method, String target, String version) {

    public RequestLine {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(version, "version");
        if (method.isBlank() || target.isBlank() || version.isBlank()) {
            throw new IllegalArgumentException("요청라인 토큰은 비어 있을 수 없다: " + this);
        }
    }
}
