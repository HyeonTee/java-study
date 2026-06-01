package study.chapter09;

/**
 * 날것의 리플렉션 API를 검사 예외 없는 안전한 헬퍼로 감싼다 — 이 단원의 기초 도구.
 *
 * <p>ch08 {@code MethodTable}은 클래스 계층을 {@code ClassNode}라는 <strong>데이터로 흉내</strong>
 * 내 디스패치를 풀었다. 이 단원은 JVM이 <strong>실제로 들고 있는</strong> {@link Class}/
 * {@code Method}/{@code Field} 객체를 직접 만진다 — "내가 만든 모델 → JVM의 실물 API"로의 도약.
 * 그래서 {@code invoke}는 {@code MethodTable.resolve}의 <strong>실물 버전</strong>이다(같은 vtable
 * 해석을 JVM이 한다).
 *
 * <p>나머지 연습({@link RouteScanner}, {@link ObjectMapper}, {@link ProxyFactory})이 이 헬퍼를
 * 쓰므로 먼저 구현한다. 모든 검사 예외는 {@link ReflectionException}으로 감싼다(ch04 wrap-and-rethrow).
 */
public final class Reflect {

    private Reflect() {
    }

    /**
     * 완전한 클래스 이름(FQCN)으로 {@link Class} 객체를 로드한다.
     *
     * <p>힌트: {@code Class.forName(name)}. {@code ClassNotFoundException}을
     * {@link ReflectionException}으로 감싸라.
     */
    public static Class<?> load(String fullyQualifiedName) {
        throw new UnsupportedOperationException(
                "TODO: Class.forName으로 클래스를 로드하고 검사 예외를 ReflectionException으로 감싸라");
    }

    /**
     * 인자 없는 생성자로 새 인스턴스를 만든다.
     *
     * <p>힌트: {@code type.getDeclaredConstructor().newInstance()}. 생성자가 없거나 접근 불가/추상
     * 클래스면 검사 예외가 나는데, 모두 {@link ReflectionException}으로 감싸라.
     */
    public static <T> T newInstance(Class<T> type) {
        throw new UnsupportedOperationException(
                "TODO: 기본 생성자로 인스턴스를 생성하고 검사 예외를 감싸라");
    }

    /**
     * {@code target}에서 이름과 파라미터 타입이 맞는 메서드를 찾아 호출하고 결과를 반환한다.
     * {@code private} 메서드도 호출할 수 있어야 한다.
     *
     * <p>힌트: {@code target.getClass().getDeclaredMethod(name, paramTypes)} →
     * {@code setAccessible(true)} → {@code invoke(target, args)}.
     *
     * <p><strong>예외 모델(핵심)</strong>: 대상 메서드가 내부에서 예외를 던지면 그것은
     * {@code InvocationTargetException}으로 <strong>감싸여</strong> 나온다. 이 경우 그 예외로 다시
     * 감싸지 말고 <strong>{@code getCause()}로 원래 예외를 꺼내 그대로 다시 던져라</strong>(원래가
     * 비검사 예외라면 그대로, 검사 예외라면 {@link ReflectionException}으로). 그래야 호출자가
     * 원래 예외 타입을 본다. 메서드를 못 찾는 등 그 외 검사 예외는 {@link ReflectionException}으로.
     */
    public static Object invoke(Object target, String name, Class<?>[] paramTypes, Object[] args) {
        throw new UnsupportedOperationException(
                "TODO: 메서드를 찾아 invoke하라. InvocationTargetException은 getCause()를 풀어 다시 던져라");
    }

    /**
     * {@code target}의 {@code name} 필드 값을 읽는다({@code private} 포함).
     *
     * <p>힌트: {@code getDeclaredField(name)} → {@code setAccessible(true)} → {@code get(target)}.
     */
    public static Object getField(Object target, String name) {
        throw new UnsupportedOperationException(
                "TODO: private 필드도 읽도록 setAccessible 후 값을 반환하라");
    }

    /**
     * {@code target}의 {@code name} 필드에 {@code value}를 쓴다({@code private} 포함).
     */
    public static void setField(Object target, String name, Object value) {
        throw new UnsupportedOperationException(
                "TODO: setAccessible 후 필드에 값을 설정하라");
    }
}
