package study.chapter07;

import java.util.Collection;
import java.util.Set;

/**
 * 도달 가능성 분석(reachability analysis) — GC의 mark 단계를 직접 구현한다.
 *
 * <p>실제 JVM의 GC는 <strong>GC Root</strong>에서 출발해 참조를 따라가며 도달 가능한 객체를
 * "살아 있음"으로 표시(mark)하고, 표시되지 않은 객체를 수거(sweep)한다. 여기서는 그
 * 알고리즘을 {@link HeapObject} 그래프 위에서 직접 구현한다.
 *
 * <p>이 문제로 확인하는 핵심 개념:
 * <ul>
 *   <li>참조 카운팅이 아니라 <strong>root로부터의 도달 가능성</strong>으로 생존을 판단한다.</li>
 *   <li>그래서 <strong>순환 참조</strong>라도 root에서 도달 불가능하면 수거 대상이다.</li>
 *   <li>그래프에 사이클이 있어도 무한 루프에 빠지면 안 된다 (방문 표시 필요).</li>
 * </ul>
 */
public final class ReachabilityAnalyzer {

    private ReachabilityAnalyzer() {
    }

    /**
     * roots에서 시작해 참조를 따라 도달 가능한 모든 객체의 집합을 반환한다 (mark 단계).
     *
     * <p>roots 자신도 도달 가능에 포함된다. 순환 참조가 있어도 종료해야 한다.
     *
     * <p>힌트: BFS/DFS로 그래프를 순회하되, 이미 방문한 노드는 다시 큐/스택에 넣지 않는다.
     */
    public static Set<HeapObject> reachable(Collection<HeapObject> roots) {
        throw new UnsupportedOperationException(
                "TODO: roots에서 참조를 따라 도달 가능한 객체 집합을 구하라 (사이클 주의)");
    }

    /**
     * target이 roots에서 도달 가능한지 여부를 반환한다.
     *
     * <p>힌트: {@link #reachable(Collection)}을 활용할 수 있다.
     */
    public static boolean isReachable(Collection<HeapObject> roots, HeapObject target) {
        throw new UnsupportedOperationException(
                "TODO: target이 roots에서 도달 가능한지 판단하라");
    }

    /**
     * 전체 힙(allObjects) 중 roots에서 <strong>도달 불가능한</strong> 객체들의 집합을 반환한다.
     * 이것이 GC가 수거(collect)할 대상이다 (sweep 단계).
     *
     * <p>예: allObjects = {a, b, c, d}, roots = {a}, a→b 일 때 → 반환 {c, d}
     * (b는 a를 통해 도달 가능하므로 수거 대상이 아니다.)
     *
     * <p>힌트: 전체 집합에서 도달 가능한 집합을 뺀다.
     */
    public static Set<HeapObject> collectable(Collection<HeapObject> allObjects,
                                              Collection<HeapObject> roots) {
        throw new UnsupportedOperationException(
                "TODO: 전체 객체 중 roots에서 도달 불가능한(수거 대상) 객체 집합을 구하라");
    }
}
