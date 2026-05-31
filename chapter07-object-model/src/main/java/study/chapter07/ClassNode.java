package study.chapter07;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 클래스 계층을 <strong>데이터로</strong> 표현하는 인프라 — {@link MethodTable} 문제에서 사용한다.
 *
 * <p>이 클래스는 연습 문제가 <strong>아니다</strong>(이미 완성되어 있다). 실제 JVM의 클래스 구조를
 * 흉내 낸 모델일 뿐이다: 각 {@code ClassNode}는 이름, 상위 클래스(없으면 {@code null}), 그리고
 * 이 클래스가 <strong>직접 선언한</strong> 메서드 집합을 가진다.
 *
 * <p>핵심: {@link #declaredBody(String)}는 <strong>이 클래스가 직접 선언한</strong> 메서드만 본다 —
 * 상속은 따라가지 않는다. 상속/오버라이드를 따라 "실제로 어떤 구현이 실행되는가"를 푸는 것이
 * 바로 {@link MethodTable}이 할 일이다.
 */
public final class ClassNode {

    private final String name;
    private final ClassNode superClass;
    private final Map<String, String> declared = new LinkedHashMap<>();

    /**
     * @param name       클래스 이름
     * @param superClass 상위 클래스. 최상위(Object에 해당)면 {@code null}.
     */
    public ClassNode(String name, ClassNode superClass) {
        this.name = name;
        this.superClass = superClass;
    }

    public String name() {
        return name;
    }

    /** 상위 클래스. 없으면 {@code null}. */
    public ClassNode superClass() {
        return superClass;
    }

    /**
     * 이 클래스가 {@code methodName}을 직접 선언(정의/오버라이드)한다고 등록한다.
     *
     * @param body 어떤 구현이 실행되는지 식별하는 표식 문자열 (예: {@code "Dog.speak"})
     */
    public void declareMethod(String methodName, String body) {
        declared.put(methodName, body);
    }

    /**
     * 이 클래스가 <strong>직접 선언한</strong> {@code methodName}의 body. 직접 선언하지 않았으면
     * (상속만 받았으면) {@code null}. — 상속은 보지 않는다는 점이 핵심이다.
     */
    public String declaredBody(String methodName) {
        return declared.get(methodName);
    }

    /**
     * 이 클래스가 직접 선언한 메서드 이름들(상속 제외). {@link MethodTable#buildVTable}에서
     * 계층을 훑어 vtable을 구성할 때 쓴다.
     */
    public Set<String> declaredMethodNames() {
        return Collections.unmodifiableSet(declared.keySet());
    }
}
