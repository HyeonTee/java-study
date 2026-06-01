package study.chapter10;

/**
 * 일부러 동기화하지 않은 카운터 — 경쟁조건(race condition)을 <strong>관찰</strong>하는 데모.
 *
 * <p>이 클래스는 연습 문제가 <strong>아니다</strong>. 이미 "완성"되어 있지만 의도적으로 틀렸다.
 * {@link SafeCounter}를 구현하기 전에, 보호 없는 {@code count++}가 어떻게 값을 잃는지 직접
 * 눈으로 보기 위한 교재다.
 *
 * <p>{@code count++}는 한 줄이지만 바이트코드로는 <strong>읽기 → +1 → 쓰기</strong> 세 단계다.
 * 여러 스레드가 이 세 단계를 겹쳐 실행하면, 두 스레드가 같은 값을 읽고 각자 +1 한 뒤 같은 값을
 * 써서 <strong>증가 한 번이 통째로 사라진다</strong>. 그래서 N개 스레드가 각각 M번 증가시켜도
 * 최종값이 N×M보다 <strong>작게</strong> 나올 수 있다.
 *
 * <p>단, 경쟁조건은 <strong>비결정적</strong>이다 — 운 좋게 안 겹치면 N×M이 정확히 나오기도 한다.
 * 그래서 이 데모를 검증하는 테스트({@code UnsafeCounterDemoTest})는 "값을 잃었다"를 단언하지
 * 않고, <strong>실제로 손실이 관측된 경우에만</strong> 그 사실을 단언한다(아니면 skip).
 * 이는 ch07이 GC 수거를 다룬 방식과 같은 철학이다.
 *
 * @see SafeCounter 같은 문제를 synchronized로 올바르게 푼 버전
 */
public final class UnsafeCounter {

    private long count = 0;

    /** 보호 없는 증가 — 비원자적 read-modify-write라 갱신이 손실될 수 있다. */
    public void increment() {
        count++;
    }

    /** 현재 값(역시 보호 없음). */
    public long get() {
        return count;
    }
}
