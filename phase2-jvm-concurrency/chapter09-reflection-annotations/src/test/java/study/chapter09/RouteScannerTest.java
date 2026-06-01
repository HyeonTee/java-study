package study.chapter09;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RouteScanner — @Route 애너테이션 스캔과 디스패치")
class RouteScannerTest {

    @Nested
    @DisplayName("스캔")
    class Scan {

        @Test
        void 애너테이션_붙은_메서드만_등록된다() {
            Set<String> routes = RouteScanner.routes(new GreetingService());
            assertEquals(Set.of("/hello", "/count"), routes);
        }

        @Test
        void 애너테이션_없는_메서드는_제외된다() {
            // secret(), boom()은 @Route가 없으므로 어떤 경로로도 등록되지 않는다.
            assertEquals(2, RouteScanner.routes(new GreetingService()).size());
        }

        @Test
        void 중복_경로는_충돌_예외() {
            assertThrows(ReflectionException.class, () -> RouteScanner.scan(new DuplicateRoutes()));
        }
    }

    @Nested
    @DisplayName("디스패치")
    class Dispatch {

        @Test
        void 경로로_메서드를_호출한다() {
            Optional<Object> r = RouteScanner.dispatch(new GreetingService(), "/hello", "World");
            assertEquals(Optional.of("Hello, World"), r);
        }

        @Test
        void 인자_없는_경로도_호출된다() {
            assertEquals(Optional.of(42), RouteScanner.dispatch(new GreetingService(), "/count"));
        }

        @Test
        void 없는_경로는_빈_Optional() {
            assertEquals(Optional.empty(), RouteScanner.dispatch(new GreetingService(), "/nope"));
        }
    }

    @Nested
    @DisplayName("@Retention 함정 — 런타임에 보이는 애너테이션만 스캔된다")
    class RetentionTrap {

        @Test
        void Route는_RUNTIME_보존이라_읽힌다() {
            Retention r = Route.class.getAnnotation(Retention.class);
            assertEquals(RetentionPolicy.RUNTIME, r.value());
        }

        @Test
        @DisplayName("SOURCE 보존 애너테이션은 런타임에 사라져 리플렉션으로 안 보인다")
        void SOURCE_애너테이션은_런타임에_없다() throws Exception {
            var marked = HasSourceAnnotation.class.getDeclaredMethod("marked");
            // @SourceOnly는 소스에 분명히 붙어 있지만 RetentionPolicy.SOURCE라 .class에 안 남는다.
            assertNull(marked.getAnnotation(SourceOnly.class),
                    "SOURCE 보존 애너테이션은 런타임 리플렉션으로 읽히면 안 된다");
            // 대조: 만약 @Route였다면 읽혔을 것이다.
            assertTrue(new GreetingService().getClass().getDeclaredMethods().length > 0);
            assertFalse(RouteScanner.routes(new HasSourceAnnotation()).contains("marked"));
        }
    }
}
