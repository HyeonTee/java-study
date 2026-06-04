package study.chapter18.routing;

/**
 * 매칭되는 라우트가 없을 때 {@link Router}가 던지는 <strong>프레임워크-중립</strong> 예외. <strong>완성 제공</strong>.
 *
 * <p>"라우터는 HTTP 숫자(404)를 모른다"는 경계를 <strong>타입으로</strong> 박는다 — {@link Router#match}는 이걸 던지고,
 * {@code http.HttpAdapter}가 404로 번역한다(코어는 status 숫자를 들지 않는다).
 */
public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
