package study.chapter09;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CounterFactory — 내부 클래스가 캡처한 상태")
class CounterFactoryTest {

    @Test
    @DisplayName("카운터는 0부터 호출마다 1씩 증가한다 (캡처된 상태가 살아남는다)")
    void 카운터는_호출마다_증가한다() {
        CounterFactory factory = new CounterFactory(1);
        CounterFactory.Counter c = factory.makeCounter();
        assertEquals(0, c.next());
        assertEquals(1, c.next());
        assertEquals(2, c.next());
    }

    @Test
    @DisplayName("같은 팩토리의 서로 다른 카운터는 독립된 상태를 가진다")
    void 카운터들은_서로_독립이다() {
        CounterFactory factory = new CounterFactory(1);
        CounterFactory.Counter a = factory.makeCounter();
        CounterFactory.Counter b = factory.makeCounter();

        a.next();   // a만 진행
        a.next();
        assertEquals(0, b.next());   // b는 영향받지 않음
        assertEquals(2, a.next());   // a는 자기 상태 유지
    }

    @Test
    @DisplayName("카운터는 자신을 만든 바깥 팩토리의 factoryId를 노출한다 (바깥 인스턴스 캡처)")
    void 카운터는_바깥_factoryId를_본다() {
        CounterFactory f1 = new CounterFactory(7);
        CounterFactory f2 = new CounterFactory(99);

        assertEquals(7, f1.makeCounter().factoryId());
        assertEquals(99, f2.makeCounter().factoryId());
    }
}
