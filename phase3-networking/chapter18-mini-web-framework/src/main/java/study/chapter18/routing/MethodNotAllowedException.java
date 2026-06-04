package study.chapter18.routing;

/**
 * 경로는 존재하나 <strong>메서드가 맞지 않을</strong> 때 {@link Router}가 던지는 프레임워크-중립 예외. <strong>완성 제공</strong>.
 *
 * <p>{@code http.HttpAdapter}가 405(Method Not Allowed)로 번역한다. {@link NotFoundException}(경로 자체 없음, 404)과
 * 구분되는 별도 타입인 이유: 클라이언트에게 "경로는 맞는데 GET이 아니라 POST를 쓰라"를 알려야 하기 때문(ch16 method=String
 * 정신 — "405는 응답 단계의 결정").
 */
public class MethodNotAllowedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MethodNotAllowedException(String message) {
        super(message);
    }
}
