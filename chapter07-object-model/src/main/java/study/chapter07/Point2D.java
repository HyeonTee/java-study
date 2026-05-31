package study.chapter07;

/**
 * 2D 좌표 값 객체 — 정체성(identity)과 동등성(equality)의 구분, 그리고 {@code equals}/{@code hashCode}
 * 계약을 직접 작성한다.
 *
 * <p>{@code ==}는 <strong>정체성</strong>(같은 객체 = 같은 참조)을 비교하고, {@code equals}는
 * <strong>논리적 동등성</strong>(같은 값)을 비교한다. {@code new Point2D(1,2)}와
 * {@code new Point2D(1,2)}는 서로 다른 객체({@code == }는 {@code false})지만, 값으로는 같아야
 * 한다({@code equals}는 {@code true}).
 *
 * <p>{@code equals}는 다음 <strong>계약</strong>을 지켜야 한다(이걸 어기면 {@code HashMap}/
 * {@code HashSet}이 오동작한다):
 * <ul>
 *   <li><strong>반사성</strong>: {@code x.equals(x)}는 {@code true}.</li>
 *   <li><strong>대칭성</strong>: {@code x.equals(y)}가 {@code true}면 {@code y.equals(x)}도 {@code true}.</li>
 *   <li><strong>추이성</strong>: {@code x≈y}, {@code y≈z}면 {@code x≈z}.</li>
 *   <li><strong>일관성</strong>: 값이 안 바뀌면 몇 번 호출해도 같은 결과.</li>
 *   <li><strong>null</strong>: {@code x.equals(null)}은 {@code false}(예외를 던지면 안 된다).</li>
 *   <li>그리고 {@code equals}가 같다고 본 두 객체는 <strong>{@code hashCode}도 같아야 한다.</strong></li>
 * </ul>
 *
 * <p>주의(이 단원의 함정 — {@link ColorPoint}에서 직면): 이 클래스를 상속해 값 컴포넌트(색)를
 * 추가할 때, {@code equals}를 {@code instanceof}로 짜면 <strong>대칭성이 깨진다</strong>.
 * 계층 전체에서 대칭성을 지키는 방법을 README에서 다룬다.
 */
public class Point2D {

    private final int x;
    private final int y;

    public Point2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    /**
     * 같은 좌표면 동등하다고 본다. 위의 5개 계약을 모두 지켜라 — 특히 {@link ColorPoint}와의
     * <strong>대칭성</strong>을 생각하라(같은 타입일 때만 동등하게 볼지 결정해야 한다).
     */
    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException(
                "TODO: equals 계약(반사/대칭/추이/일관/null)을 지키며 좌표 동등성을 구현하라");
    }

    /**
     * {@code equals}와 일관되게 — 동등한 두 객체는 같은 해시를 내야 한다.
     *
     * <p>힌트: {@code java.util.Objects.hash(...)}를 써도 된다.
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException(
                "TODO: equals와 일관된 hashCode를 구현하라 (동등하면 해시도 같게)");
    }
}
