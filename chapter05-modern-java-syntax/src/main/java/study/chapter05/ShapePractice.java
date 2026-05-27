package study.chapter05;

import java.util.List;
import java.util.Optional;

/**
 * Sealed interface + Record + Pattern matching switch 연습.
 *
 * <p>모든 메서드는 {@code switch (shape) { case Circle c -> ... }} 형태의
 * 패턴 매칭 switch expression으로 구현한다.
 */
public class ShapePractice {

    private ShapePractice() {
    }

    /**
     * 도형의 넓이를 반환한다.
     * <ul>
     *   <li>Circle: π × r²</li>
     *   <li>Rectangle: width × height</li>
     *   <li>Triangle: 0.5 × base × height</li>
     * </ul>
     */
    public static double area(Shape shape) {
        throw new UnsupportedOperationException(
                "TODO: switch (shape) { case Circle c -> ... case Rectangle r -> ... case Triangle t -> ... }");
    }

    /**
     * 도형을 설명하는 문자열을 반환한다.
     * <ul>
     *   <li>Circle: "Circle: radius=5.0"</li>
     *   <li>Rectangle: "Rectangle: 3.0 x 4.0"</li>
     *   <li>Triangle: "Triangle: base=3.0, height=4.0"</li>
     * </ul>
     */
    public static String describe(Shape shape) {
        throw new UnsupportedOperationException(
                "TODO: switch expression — 각 case에서 record 컴포넌트를 꺼내 문자열 조합");
    }

    /**
     * 도형을 factor배 확대/축소한 새 도형을 반환한다.
     * <ul>
     *   <li>Circle: radius × factor</li>
     *   <li>Rectangle: width × factor, height × factor</li>
     *   <li>Triangle: base × factor, height × factor</li>
     * </ul>
     */
    public static Shape scale(Shape shape, double factor) {
        throw new UnsupportedOperationException(
                "TODO: switch expression — 각 case에서 새 record 인스턴스를 생성하여 반환");
    }

    /**
     * 도형 리스트에서 넓이가 가장 큰 도형을 반환한다. 빈 리스트면 empty.
     *
     * <p>힌트: Stream + Comparator.comparingDouble + area() 메서드 활용.
     */
    public static Optional<Shape> largestArea(List<Shape> shapes) {
        throw new UnsupportedOperationException(
                "TODO: shapes.stream().max(Comparator.comparingDouble(ShapePractice::area))");
    }
}
