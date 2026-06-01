package study.chapter09;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 데모(채점 아님) — 동기화 없는 카운터가 경쟁조건으로 값을 잃는 것을 <strong>관측되면</strong> 보인다.
 *
 * <p>경쟁조건은 비결정적이라 "값을 잃는다"를 무조건 단언하면 그 테스트 자체가 flaky해진다
 * (운 좋게 안 겹치면 손실이 0일 수 있음). 그래서 ch06의 GC 데모처럼, 손실이 실제로
 * <strong>관측된 경우에만</strong> 그 성질을 단언하고, 아니면 skip한다.
 *
 * <p>이 데모는 {@link UnsafeCounter}(완성된 틀린 코드)를 대상으로 하며, {@link SafeCounter}를
 * 왜 만들어야 하는지에 대한 동기 부여다.
 */
@DisplayName("UnsafeCounter (데모) — 경쟁조건이 관측되면 값을 잃는다")
class UnsafeCounterDemoTest {

    @Test
    @Timeout(10)
    @DisplayName("비동기화 카운터는 레이스가 관측되면 N×M보다 작은 값을 낸다 (아니면 skip)")
    void 비동기화_카운터는_레이스가_관측되면_값을_잃는다() {
        int threads = 16;
        int iters = 100_000;
        UnsafeCounter counter = new UnsafeCounter();

        Concurrently.run(threads, () -> {
            for (int i = 0; i < iters; i++) {
                counter.increment();
            }
        });

        long expected = (long) threads * iters;
        long actual = counter.get();
        long lost = expected - actual;

        assumeTrue(lost > 0, "이 실행에서는 경쟁조건이 관측되지 않음 — skip (flaky 방지)");
        assertTrue(actual < expected,
                "레이스가 관측되었으면 최종값이 N×M(" + expected + ")보다 작아야 한다: " + actual);
    }
}
