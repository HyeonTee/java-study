package study.chapter02;

/**
 * equals/hashCode 계약을 직접 작성해보기 위한 값 객체(value object).
 *
 * <p>같은 통화·같은 금액이면 "같은 돈"으로 취급되어야 한다. 즉 <strong>값이 같으면
 * equals가 true</strong>여야 하고, 이 객체를 {@code HashMap}/{@code HashSet}의
 * 키로 쓰려면 <strong>equals가 같을 때 hashCode도 같아야</strong> 한다.
 *
 * <p>지금은 {@link #equals(Object)}와 {@link #hashCode()}를 오버라이드하지 않아
 * {@link Object}의 <em>동일성(identity)</em> 기준이 적용된다 — 그래서 값이 같아도
 * 서로 다른 객체로 취급되고, HashMap 키로 쓰면 찾지 못한다. 이 두 메서드를 직접
 * 구현해 값 기반 동등성을 완성하라.
 *
 * <p><strong>equals 계약 5조항</strong> (반드시 만족해야 함):
 * <ul>
 *   <li>반사성(reflexive): {@code x.equals(x)}는 true</li>
 *   <li>대칭성(symmetric): {@code x.equals(y)}이면 {@code y.equals(x)}</li>
 *   <li>추이성(transitive): {@code x.equals(y)}, {@code y.equals(z)}이면 {@code x.equals(z)}</li>
 *   <li>일관성(consistent): 값이 안 바뀌면 몇 번 호출해도 결과 동일</li>
 *   <li>null 비교: {@code x.equals(null)}은 항상 false</li>
 * </ul>
 *
 * <p>그리고 <strong>equals가 true인 두 객체는 hashCode도 반드시 같아야 한다</strong>
 * (역은 성립하지 않아도 됨 — 해시 충돌은 허용).
 */
public final class Money {

    private final String currency;
    private final long amount;

    public Money(String currency, long amount) {
        if (currency == null) {
            throw new IllegalArgumentException("currency must not be null");
        }
        this.currency = currency;
        this.amount = amount;
    }

    public String currency() {
        return currency;
    }

    public long amount() {
        return amount;
    }

    /**
     * 같은 통화이고 같은 금액이면 true.
     *
     * <p>힌트: null 검사 → 타입 검사({@code getClass} 또는 {@code instanceof}) →
     * 필드 비교 순서. {@code currency}는 {@code Objects.equals}, {@code amount}는 {@code ==}.
     */
    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException(
                "TODO: currency와 amount가 모두 같을 때 true를 반환하도록 equals 계약을 지켜 구현하라");
    }

    /**
     * equals와 일관된 hashCode.
     *
     * <p>힌트: equals에서 사용한 필드(currency, amount)로 {@code Objects.hash(...)}를 만든다.
     * equals가 보는 필드와 hashCode가 보는 필드는 반드시 일치해야 한다.
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException(
                "TODO: equals와 동일한 필드(currency, amount)로 hashCode를 구현하라");
    }

    @Override
    public String toString() {
        return currency + " " + amount;
    }
}
