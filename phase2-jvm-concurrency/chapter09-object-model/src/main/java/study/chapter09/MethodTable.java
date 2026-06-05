package study.chapter09;

import java.util.Map;
import java.util.Optional;

/**
 * 동적 디스패치를 손으로 구현한다 — JVM이 {@code invokevirtual}에서 하는 일.
 *
 * <p>이 단원의 간판 문제. Java에서 {@code animal.speak()}를 호출하면, 컴파일러가 미리 어떤
 * 메서드를 부를지 정하는 게 <strong>아니다</strong>. 실행 시점에 <strong>수신 객체의 실제
 * 런타임 타입</strong>에서 출발해 상위 클래스로 올라가며 그 메서드를 <strong>직접 선언한 첫
 * 클래스</strong>를 찾는다(메서드 해석, method resolution). 이 알고리즘을 {@link ClassNode}
 * 그래프 위에서 직접 구현한다.
 *
 * <p>이 문제로 증명하는 것:
 * <ul>
 *   <li>호출되는 구현은 <strong>정적(선언) 타입</strong>이 아니라 <strong>런타임 타입</strong>이
 *       결정한다 — 이것이 다형성(polymorphism)의 메커니즘이다.</li>
 *   <li>오버라이드 = 하위 클래스의 선언이 상위의 선언을 <strong>가린다(override)</strong>.</li>
 *   <li>오버라이드하지 않고 상속만 받은 메서드는 <strong>상위 구현</strong>으로 해석된다.</li>
 *   <li>{@code super.m()}은 런타임 타입을 무시하고 <strong>호출자의 상위부터</strong> 해석한다.</li>
 * </ul>
 */
public final class MethodTable {

    private MethodTable() {
    }

    /**
     * {@code invokevirtual} 해석: {@code receiverType}(수신 객체의 실제 타입)에서 시작해
     * {@code superClass()} 방향으로 올라가며 {@code methodName}을 직접 선언한 첫 클래스의 body를
     * 반환한다. 어디에도 없으면 빈 {@link Optional}({@code AbstractMethodError}에 해당).
     *
     * <p>힌트: 항상 <strong>아래(수신 타입)에서 위(상위)로</strong>. {@link ClassNode#declaredBody}가
     * {@code null}이 아닌 첫 클래스에서 멈춘다.
     */
    public static Optional<String> resolve(ClassNode receiverType, String methodName) {
        throw new UnsupportedOperationException(
                "TODO: receiverType에서 상위로 올라가며 methodName을 직접 선언한 첫 body를 찾아라");
    }

    /**
     * {@code super.methodName()} 해석: {@code callerClass} 자신이 아니라
     * {@code callerClass.superClass()}에서 시작해 위로 올라간다. 수신 객체의 런타임 타입은
     * 관여하지 않는다 — {@code super} 호출은 <strong>정적</strong>이다.
     *
     * <p>힌트: "나를 선언한 클래스의 부모부터 다시 해석"하는 것과 같다.
     */
    public static Optional<String> resolveSuper(ClassNode callerClass, String methodName) {
        throw new UnsupportedOperationException(
                "TODO: callerClass의 상위 클래스부터 위로 해석하라 (런타임 수신 타입 무시)");
    }

    /**
     * {@code receiverType}의 "유효 vtable"을 만든다: 이 타입의 인스턴스에서 호출 가능한 모든
     * 메서드 이름 → 실제로 실행될 body. 계층 전체를 훑어 오버라이드를 반영한다.
     *
     * <p>힌트: 계층을 최상위까지 거슬러 올라간 뒤, <strong>상위(부모)부터 아래로</strong> 내려오며
     * 각 클래스의 {@link ClassNode#declaredMethodNames()}를 vtable에 넣어라. 하위 클래스의 선언이
     * 상위를 <strong>덮어쓰는</strong> 것이 곧 오버라이드의 의미다.
     */
    public static Map<String, String> buildVTable(ClassNode receiverType) {
        throw new UnsupportedOperationException(
                "TODO: 상위→하위 순으로 채워 하위 선언이 상위를 가리는 vtable을 구성하라");
    }
}
