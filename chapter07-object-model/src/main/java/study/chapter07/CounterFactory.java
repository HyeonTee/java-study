package study.chapter07;

/**
 * 카운터를 찍어내는 팩토리 — 중첩/내부 클래스(또는 람다)가 <strong>상태를 캡처</strong>한다는 것의 증명.
 *
 * <p>{@link #makeCounter()}가 반환하는 {@link Counter}는 두 가지를 "캡처"해야 한다:
 * <ul>
 *   <li>자기만의 <strong>독립된 카운트 상태</strong> — 메서드가 끝나도 살아남아, 호출할 때마다
 *       증가한다. (이것이 클로저의 정체다: 지역/필드 상태가 객체에 갇혀 메서드 수명을 넘긴다.)</li>
 *   <li>자신을 만든 <strong>바깥 {@code CounterFactory} 인스턴스</strong>의 {@code factoryId} —
 *       비-static 내부 클래스는 바깥 인스턴스({@code CounterFactory.this})를 암묵적으로 붙잡는다.</li>
 * </ul>
 *
 * <p>그래서 서로 다른 {@link #makeCounter()} 호출이 만든 카운터는 <strong>서로 독립</strong>이고,
 * 같은 팩토리에서 나온 카운터들은 모두 같은 {@code factoryId}를 보고한다.
 */
public final class CounterFactory {

    private final int factoryId;

    public CounterFactory(int factoryId) {
        this.factoryId = factoryId;
    }

    /** 호출할 때마다 다음 정수를 돌려주는 카운터. */
    public interface Counter {
        /** 0부터 시작해, 호출할 때마다 다음 정수를 반환한다(첫 호출 0, 다음 1, 2, ...). */
        int next();

        /** 이 카운터를 만든 {@code CounterFactory}의 {@code factoryId}. */
        int factoryId();
    }

    /**
     * 새 {@link Counter}를 만든다. 각 카운터는 0부터 세는 <strong>독립된</strong> 상태를 가지며,
     * {@code factoryId()}로 이 팩토리의 id를 노출한다.
     *
     * <p>힌트: 비-static 내부 클래스(또는 익명 클래스/람다)로 구현하면 바깥 {@code factoryId}에
     * 자연스럽게 접근할 수 있다. 카운트 상태는 카운터마다 따로여야 한다(서로 영향 없음).
     */
    public Counter makeCounter() {
        throw new UnsupportedOperationException(
                "TODO: 독립된 카운트 상태를 캡처하고, 바깥 factoryId를 노출하는 Counter를 반환하라");
    }
}
