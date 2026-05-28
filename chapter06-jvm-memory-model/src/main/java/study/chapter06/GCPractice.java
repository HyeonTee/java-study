package study.chapter06;

/**
 * Garbage Collection 개념 연습.
 *
 * <p>각 메서드는 특정 시나리오에서 GC 대상 여부 또는 GC 세대를 묻는다.
 * Java의 GC는 tracing 방식(root에서 도달 가능한지 추적)을 사용한다는 점을 기억하라.
 */
public class GCPractice {

    /**
     * 참조 변수가 null이 된 객체는 GC 대상인가?
     *
     * <p>시나리오: {@code Object obj = new Object(); obj = null;}
     *
     * @return GC 대상이면 true
     */
    public boolean isEligibleForGC_unreferenced() {
        throw new UnsupportedOperationException(
                "TODO: 참조가 null이 된 객체의 GC 대상 여부를 반환하라");
    }

    /**
     * 참조 변수가 다른 객체를 가리키도록 재할당된 경우, 원래 객체는 GC 대상인가?
     *
     * <p>시나리오: {@code Object obj = new Object(); obj = new Object();} — 첫 번째 객체
     *
     * @return GC 대상이면 true
     */
    public boolean isEligibleForGC_reassigned() {
        throw new UnsupportedOperationException(
                "TODO: 참조가 재할당되어 도달 불가능한 객체의 GC 대상 여부를 반환하라");
    }

    /**
     * 두 객체가 서로만 참조하고 외부에서 도달할 수 없는 경우 (순환 참조), GC 대상인가?
     *
     * <p>시나리오: A → B, B → A이지만 GC root에서 A, B 모두 도달 불가능.
     *
     * <p>힌트: Java는 reference counting이 아닌 tracing GC를 사용한다.
     *
     * @return GC 대상이면 true
     */
    public boolean isEligibleForGC_circularReference() {
        throw new UnsupportedOperationException(
                "TODO: 순환 참조 객체의 GC 대상 여부를 반환하라 — Java GC 방식을 고려하라");
    }

    /**
     * 새로 생성된 객체는 어느 GC 세대에 할당되는가?
     *
     * <p>힌트: 대부분의 객체는 금방 소멸한다는 '세대 가설(Generational Hypothesis)'을 떠올려라.
     *
     * @return 해당 GC 세대
     */
    public GCGeneration whichGCGeneration_newObject() {
        throw new UnsupportedOperationException(
                "TODO: 새 객체가 할당되는 GC 세대를 반환하라");
    }

    /**
     * 여러 번의 Minor GC를 살아남은 객체는 어느 세대로 승격되는가?
     *
     * <p>힌트: age threshold를 넘기면 승격(promotion)된다.
     *
     * @return 해당 GC 세대
     */
    public GCGeneration whichGCGeneration_survivedObject() {
        throw new UnsupportedOperationException(
                "TODO: 오래 살아남은 객체가 승격되는 GC 세대를 반환하라");
    }
}
