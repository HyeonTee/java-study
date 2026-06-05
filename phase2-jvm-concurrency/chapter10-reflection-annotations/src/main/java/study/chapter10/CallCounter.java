package study.chapter10;

import java.util.HashMap;
import java.util.Map;

/**
 * 메서드 이름별 호출 횟수를 기록하는 카운터 (인프라 — 완성 코드). {@link ProxyFactory}가 만든
 * 프록시가 호출을 가로챌 때마다 여기에 기록한다.
 */
public final class CallCounter {

    private final Map<String, Integer> counts = new HashMap<>();
    private int total = 0;

    /** {@code methodName} 호출을 1회 기록한다. */
    public void record(String methodName) {
        counts.merge(methodName, 1, Integer::sum);
        total++;
    }

    /** 전체 호출 횟수. */
    public int total() {
        return total;
    }

    /** 특정 메서드의 호출 횟수. */
    public int countOf(String methodName) {
        return counts.getOrDefault(methodName, 0);
    }
}
