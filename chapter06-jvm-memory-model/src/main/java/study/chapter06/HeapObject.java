package study.chapter06;

import java.util.ArrayList;
import java.util.List;

/**
 * 힙에 있는 하나의 객체를 모델링한 노드. 다른 {@code HeapObject}들을 참조(reference)할 수 있다.
 *
 * <p>이 클래스는 연습 문제가 아니라 {@link ReachabilityAnalyzer}가 다룰 <strong>객체 그래프</strong>를
 * 표현하기 위한 인프라다. 실제 JVM 힙의 객체와 참조 관계를 단순화한 모형이라고 생각하면 된다.
 *
 * <pre>{@code
 * HeapObject a = new HeapObject("a");
 * HeapObject b = new HeapObject("b");
 * a.pointTo(b);   // a가 b를 참조 (a.field = b 와 같은 의미)
 * }</pre>
 */
public final class HeapObject {

    private final String id;
    private final List<HeapObject> references = new ArrayList<>();

    public HeapObject(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    /** 이 객체가 {@code other}를 참조하도록 한다 (참조 엣지 추가). */
    public void pointTo(HeapObject other) {
        references.add(other);
    }

    /** 이 객체가 참조하는 객체들 (나가는 엣지). */
    public List<HeapObject> references() {
        return references;
    }

    @Override
    public String toString() {
        return "HeapObject(" + id + ")";
    }
}
