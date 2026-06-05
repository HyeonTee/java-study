package study.chapter10;

/**
 * 리플렉션 호출에서 발생하는 검사 예외를 감싸 다시 던지는 비검사 예외 (인프라 — 완성 코드).
 *
 * <p>{@code java.lang.reflect} API는 {@code ClassNotFoundException},
 * {@code NoSuchMethodException}, {@code IllegalAccessException},
 * {@code InvocationTargetException} 등 검사 예외를 잔뜩 던진다. 학습용 유틸({@link Reflect})은
 * 호출부를 깔끔하게 유지하려고 이것들을 하나의 비검사 예외로 감싼다 — ch04의
 * wrap-and-rethrow 패턴을 리플렉션 도메인에 적용한 것이다.
 *
 * <p><strong>주의</strong>: {@code Method.invoke}가 던지는 {@code InvocationTargetException}은
 * "대상 메서드 내부에서 던진 진짜 예외"를 {@code getCause()}에 담고 있다. 그 경우엔 보통
 * 이 예외로 감싸지 말고 <strong>원래 예외(cause)를 그대로 다시 던지는</strong> 것이 옳다
 * (그래야 호출자가 원래 예외 타입을 본다). {@link Reflect}의 Javadoc 참고.
 */
public class ReflectionException extends RuntimeException {

    public ReflectionException(String message) {
        super(message);
    }

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
