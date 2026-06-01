package study.chapter08;

/**
 * 인터페이스를 감싸는 <strong>동적 프록시</strong>를 만들어 모든 메서드 호출을 가로채 카운팅하고
 * 실제 대상에 위임한다 — AOP, {@code @Transactional}, Mockito가 작동하는 바로 그 메커니즘.
 *
 * <p>ch07 동적 디스패치 + ch03 "함수를 다루기" + 캡처가 합쳐지는 정점이다. 런타임에 인터페이스를
 * 구현하는 클래스가 <strong>없는데도</strong> 구현체가 생긴다 — {@code java.lang.reflect.Proxy}가
 * 런타임에 그 클래스를 만들어내고, 모든 호출을 하나의 {@code InvocationHandler}로 보낸다.
 *
 * <p><strong>제약</strong>: JDK 동적 프록시는 <strong>인터페이스만</strong> 프록시할 수 있다
 * (구체 클래스는 불가 — 그래서 CGLIB/ByteBuddy가 존재하고, Spring AOP가 인터페이스 없는 빈엔
 * CGLIB로 폴백한다). 이 범위 밖.
 */
public final class ProxyFactory {

    private ProxyFactory() {
    }

    /**
     * {@code target}을 감싸, 모든 메서드 호출을 {@code counter}에 기록한 뒤 {@code target}에
     * 위임하는 프록시를 만들어 반환한다.
     *
     * <ul>
     *   <li>{@code iface}는 반드시 인터페이스여야 한다. 아니면 {@code Proxy.newProxyInstance}가
     *       {@code IllegalArgumentException}을 던진다(그대로 전파해도 좋다).</li>
     *   <li>호출 가로채기: {@code InvocationHandler.invoke} 안에서
     *       {@code counter.record(method.getName())} 후 {@code method.invoke(target, args)}로 위임하고
     *       그 반환값을 돌려준다.</li>
     *   <li>대상이 던진 예외 투명성: {@code method.invoke}의 {@code InvocationTargetException}은
     *       {@code getCause()}를 꺼내 다시 던져야 호출자가 원래 예외를 본다({@link Reflect#invoke}와
     *       같은 함정).</li>
     * </ul>
     *
     * <p>힌트: {@code Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[]{iface}, handler)}.
     * {@code handler}는 람다로 쓸 수 있다(ch03 — {@code InvocationHandler}는 함수형 인터페이스다).
     */
    public static <T> T counting(Class<T> iface, T target, CallCounter counter) {
        throw new UnsupportedOperationException(
                "TODO: Proxy.newProxyInstance로 호출을 가로채 카운팅 후 위임하는 프록시를 반환하라");
    }
}
