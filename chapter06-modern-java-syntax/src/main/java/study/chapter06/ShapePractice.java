package study.chapter06;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Sealed interface + Record + Pattern matching switch 연습.
 *
 * <p>
 * 모든 메서드는 {@code switch (shape) { case Circle c -> ... }} 형태의
 * 패턴 매칭 switch expression으로 구현한다.
 */
public class ShapePractice {

    private ShapePractice() {
    }

    /**
     * 도형의 넓이를 반환한다.
     * <ul>
     * <li>Circle: π × r²</li>
     * <li>Rectangle: width × height</li>
     * <li>Triangle: 0.5 × base × height</li>
     * </ul>
     */
    public static double area(Shape shape) {
        return switch (shape) {
            case Circle c -> Math.PI * c.radius() * c.radius();
            case Rectangle r -> r.width() * r.height();
            case Triangle t -> t.base() * t.height() * 0.5;
        };
    }

    /**
     * 도형을 설명하는 문자열을 반환한다.
     * <ul>
     * <li>Circle: "Circle: radius=5.0"</li>
     * <li>Rectangle: "Rectangle: 3.0 x 4.0"</li>
     * <li>Triangle: "Triangle: base=3.0, height=4.0"</li>
     * </ul>
     */
    public static String describe(Shape shape) {
        return switch (shape) {
            case Circle c -> "Circle: radius=" + c.radius();
            case Rectangle r -> "Rectangle: " + r.width() + " x " + r.height();
            case Triangle t -> "Triangle: base=" + t.base() + ", height=" + t.height();
        };
    }

    /**
     * 도형을 factor배 확대/축소한 새 도형을 반환한다.
     * <ul>
     * <li>Circle: radius × factor</li>
     * <li>Rectangle: width × factor, height × factor</li>
     * <li>Triangle: base × factor, height × factor</li>
     * </ul>
     */
    public static Shape scale(Shape shape, double factor) {
        return switch (shape) {
            case Circle c -> new Circle(c.radius() * factor);
            case Rectangle r -> new Rectangle(r.width() * factor, r.height() * factor);
            case Triangle t -> new Triangle(t.base() * factor, t.height() * factor);
        };
    }

    /**
     * 도형 리스트에서 넓이가 가장 큰 도형을 반환한다. 빈 리스트면 empty.
     *
     * <p>
     * 힌트: Stream + Comparator.comparingDouble + area() 메서드 활용.
     */
    public static Optional<Shape> largestArea(List<Shape> shapes) {
        return shapes.stream().max(Comparator.comparingDouble(shape -> area(shape)));
    }
}
