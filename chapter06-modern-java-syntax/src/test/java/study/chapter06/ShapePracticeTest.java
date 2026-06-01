package study.chapter06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShapePractice")
class ShapePracticeTest {

    @Nested
    @DisplayName("area")
    class Area {

        @Test
        void 원의_넓이() {
            assertEquals(Math.PI * 25, ShapePractice.area(new Circle(5)), 0.001);
        }

        @Test
        void 직사각형의_넓이() {
            assertEquals(12.0, ShapePractice.area(new Rectangle(3, 4)), 0.001);
        }

        @Test
        void 삼각형의_넓이() {
            assertEquals(6.0, ShapePractice.area(new Triangle(3, 4)), 0.001);
        }
    }

    @Nested
    @DisplayName("describe")
    class Describe {

        @Test
        void 원() {
            assertEquals("Circle: radius=5.0", ShapePractice.describe(new Circle(5)));
        }

        @Test
        void 직사각형() {
            assertEquals("Rectangle: 3.0 x 4.0", ShapePractice.describe(new Rectangle(3, 4)));
        }

        @Test
        void 삼각형() {
            assertEquals("Triangle: base=3.0, height=4.0",
                    ShapePractice.describe(new Triangle(3, 4)));
        }
    }

    @Nested
    @DisplayName("scale")
    class Scale {

        @Test
        void 원_두배() {
            Shape scaled = ShapePractice.scale(new Circle(5), 2);
            assertInstanceOf(Circle.class, scaled);
            assertEquals(10.0, ((Circle) scaled).radius(), 0.001);
        }

        @Test
        void 직사각형_절반() {
            Shape scaled = ShapePractice.scale(new Rectangle(4, 6), 0.5);
            assertInstanceOf(Rectangle.class, scaled);
            assertEquals(2.0, ((Rectangle) scaled).width(), 0.001);
            assertEquals(3.0, ((Rectangle) scaled).height(), 0.001);
        }

        @Test
        void 삼각형_세배() {
            Shape scaled = ShapePractice.scale(new Triangle(3, 4), 3);
            assertInstanceOf(Triangle.class, scaled);
            assertEquals(9.0, ((Triangle) scaled).base(), 0.001);
            assertEquals(12.0, ((Triangle) scaled).height(), 0.001);
        }
    }

    @Nested
    @DisplayName("largestArea")
    class LargestArea {

        @Test
        void 가장_큰_넓이의_도형을_반환한다() {
            List<Shape> shapes = List.of(
                    new Circle(1),
                    new Rectangle(10, 10),
                    new Triangle(3, 4));
            Optional<Shape> largest = ShapePractice.largestArea(shapes);
            assertTrue(largest.isPresent());
            assertInstanceOf(Rectangle.class, largest.get());
        }

        @Test
        void 빈_리스트면_empty() {
            assertEquals(Optional.empty(), ShapePractice.largestArea(List.of()));
        }

        @Test
        void 하나뿐이면_그것이_최대() {
            Optional<Shape> largest = ShapePractice.largestArea(List.of(new Circle(3)));
            assertTrue(largest.isPresent());
            assertInstanceOf(Circle.class, largest.get());
        }
    }
}
