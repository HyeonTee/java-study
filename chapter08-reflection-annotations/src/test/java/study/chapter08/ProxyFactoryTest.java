package study.chapter08;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProxyFactory — 동적 프록시로 호출 가로채기")
class ProxyFactoryTest {

    @Nested
    @DisplayName("위임과 가로채기")
    class DelegateAndIntercept {

        @Test
        void 호출을_위임해_같은_결과를_낸다() {
            CallCounter counter = new CallCounter();
            Greeter g = ProxyFactory.counting(Greeter.class, new GreetingService(), counter);
            assertEquals("Hello, Bob", g.greet("Bob"));
            assertEquals(42, g.count());
        }

        @Test
        void 호출마다_카운터가_증가한다() {
            CallCounter counter = new CallCounter();
            Greeter g = ProxyFactory.counting(Greeter.class, new GreetingService(), counter);
            g.greet("a");
            g.greet("b");
            g.count();
            assertEquals(2, counter.countOf("greet"));
            assertEquals(1, counter.countOf("count"));
            assertEquals(3, counter.total());
        }

        @Test
        void 반환된_객체는_진짜_동적_프록시다() {
            Greeter g = ProxyFactory.counting(Greeter.class, new GreetingService(), new CallCounter());
            assertTrue(g instanceof Greeter);
            assertTrue(Proxy.isProxyClass(g.getClass()), "수동 래퍼가 아니라 java.lang.reflect.Proxy여야 한다");
        }
    }

    @Nested
    @DisplayName("예외 투명성과 제약")
    class Constraints {

        @Test
        @DisplayName("대상이 던진 예외는 원래 타입으로 전파된다")
        void 대상_예외가_원래_타입으로_전파된다() {
            // Greeter에는 boom이 없으니, 예외 전파 검증은 별도 인터페이스로.
            Runnable r = ProxyFactory.counting(Runnable.class, () -> {
                throw new IllegalStateException("boom");
            }, new CallCounter());
            assertThrows(IllegalStateException.class, r::run);
        }
    }
}
