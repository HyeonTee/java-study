package study.chapter09;

/**
 * {@link Point2D}에 색(color)을 더한 값 객체 — 상속 계층에서 {@code equals} 대칭성 함정을 직면한다.
 *
 * <p>"좌표가 같고 색도 같아야 동등하다"로 {@code equals}를 오버라이드하려고 하면, 상위
 * {@link Point2D}의 {@code equals}와 어떻게 맞물리느냐에 따라 <strong>대칭성</strong>이 깨질 수 있다.
 * 고전적인 함정:
 *
 * <pre>{@code
 * Point2D   p  = new Point2D(1, 2);
 * ColorPoint cp = new ColorPoint(1, 2, "RED");
 * // 만약 Point2D.equals가 instanceof 기반이라면:
 * //   p.equals(cp)  → true  (cp도 좌표 (1,2)인 Point2D니까)
 * //   cp.equals(p)  → false (p에는 색이 없으니까)
 * //   → 비대칭! 계약 위반.
 * }</pre>
 *
 * <p>이 단원의 테스트는 <strong>대칭성 자체를 단언</strong>한다 — 즉
 * {@code p.equals(cp) == cp.equals(p)}여야 통과한다. 그래서 {@code instanceof}로는 통과할 수 없고,
 * "서로 같은 클래스일 때만 동등"하게 보는 전략({@code getClass} 비교)으로 가야 한다.
 * 그 트레이드오프(리스코프 치환 원칙과의 충돌)는 README에서 설명한다.
 */
public class ColorPoint extends Point2D {

    private final String color;

    public ColorPoint(int x, int y, String color) {
        super(x, y);
        this.color = color;
    }

    public String color() {
        return color;
    }

    /**
     * 좌표와 색이 모두 같을 때 동등. 단, {@link Point2D}와의 <strong>대칭성</strong>을 깨지 않게
     * 하라(테스트가 {@code p.equals(cp) == cp.equals(p)}를 단언한다).
     */
    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException(
                "TODO: 좌표+색 동등성을 구현하되 Point2D와의 대칭성을 깨지 마라");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException(
                "TODO: 좌표와 색을 모두 반영한, equals와 일관된 hashCode");
    }
}
