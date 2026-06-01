package study.chapter07;

import java.util.Collection;
import java.util.Set;

/**
 * 세대별 GC(generational GC)의 핵심 — <strong>Minor GC와 객체 승격(promotion)</strong> — 을 직접
 * 시뮬레이션한다.
 *
 * <p>README의 "Weak Generational Hypothesis(대부분의 객체는 금방 죽는다)"와 Eden→Survivor→Old 승격
 * 이론을, 비결정적인 실제 GC 대신 <strong>결정적인 모형</strong>으로 구현해 검증한다. {@code System.gc()}에
 * 의존하지 않으므로 결과가 항상 같다(채점 가능). {@link ReachabilityAnalyzer#reachable}을 재사용해
 * 도달성을 판단한다 — 그 문제를 먼저 풀어야 한다.
 *
 * <p>모델(단순화):
 * <ul>
 *   <li>새 객체는 <strong>young 세대</strong>에 age 0으로 할당된다.</li>
 *   <li><strong>Minor GC</strong>: young 객체 중 roots에서 도달 가능한 것은 살아남아 age가 1 증가하고,
 *       도달 불가능한 것은 <strong>수거</strong>된다.</li>
 *   <li>살아남아 age가 <strong>tenuringThreshold 이상</strong>이 되면 <strong>old 세대로 승격</strong>된다.</li>
 *   <li>Minor GC는 <strong>old 세대를 건드리지 않는다</strong>(old는 그대로 생존).</li>
 * </ul>
 */
public final class GenerationalHeap {

    /**
     * 새 객체를 young 세대에 age 0으로 할당한다.
     */
    public void allocate(HeapObject obj) {
        throw new UnsupportedOperationException("TODO: obj를 young 세대에 age 0으로 추가");
    }

    /** 현재 young 세대 객체 수. */
    public int youngCount() {
        throw new UnsupportedOperationException("TODO: young 세대 크기");
    }

    /** 현재 old 세대 객체 수. */
    public int oldCount() {
        throw new UnsupportedOperationException("TODO: old 세대 크기");
    }

    /** {@code obj}가 old 세대에 있으면 {@code true}. */
    public boolean isInOldGen(HeapObject obj) {
        throw new UnsupportedOperationException("TODO: old 세대 포함 여부");
    }

    /**
     * young 세대 객체가 살아남은 Minor GC 횟수(age). young에 없으면(수거됐거나 승격됐거나 할당된 적 없음)
     * {@code -1}.
     */
    public int ageOf(HeapObject obj) {
        throw new UnsupportedOperationException("TODO: young 세대 객체의 age, 없으면 -1");
    }

    /**
     * Minor GC를 1회 수행한다. young 세대 객체 중 {@code roots}에서 도달 가능한 것은 age를 1 늘리고
     * (age가 {@code tenuringThreshold} 이상이면 old로 승격), 도달 불가능한 것은 수거한다. old 세대는
     * 건드리지 않는다.
     *
     * <p>힌트: {@link ReachabilityAnalyzer#reachable}로 도달 가능 집합을 구한 뒤, young 스냅샷을 순회하며
     * 생존/승격/수거를 처리하라(순회 중 컬렉션 수정 주의).
     *
     * @return 이번 Minor GC에서 <strong>수거된</strong>(도달 불가능했던) young 객체들의 집합
     */
    public Set<HeapObject> minorGc(Collection<HeapObject> roots, int tenuringThreshold) {
        throw new UnsupportedOperationException(
                "TODO: 도달 가능한 young은 age++/승격, 도달 불가능한 young은 수거해 반환 (old는 보존)");
    }
}
