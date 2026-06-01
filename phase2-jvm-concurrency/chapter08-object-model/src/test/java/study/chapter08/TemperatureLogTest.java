package study.chapter08;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TemperatureLog — 캡슐화와 방어적 복사")
class TemperatureLogTest {

    @Nested
    @DisplayName("기본 계약")
    class Contract {

        @Test
        void count와_max() {
            TemperatureLog log = new TemperatureLog(List.of(20.0, 25.5, 18.0));
            assertEquals(3, log.count());
            assertEquals(25.5, log.max());
        }

        @Test
        void readings는_기록된_값을_담는다() {
            TemperatureLog log = new TemperatureLog(List.of(1.0, 2.0));
            assertEquals(List.of(1.0, 2.0), log.readings());
        }

        @Test
        void 비었으면_max는_NoSuchElementException() {
            TemperatureLog log = new TemperatureLog(List.of());
            assertThrows(NoSuchElementException.class, log::max);
        }
    }

    @Nested
    @DisplayName("방어적 복사 — 내부가 외부로 새지 않는다")
    class DefensiveCopy {

        @Test
        void 생성자에_넘긴_리스트를_나중에_바꿔도_내부는_불변() {
            List<Double> input = new ArrayList<>(List.of(20.0, 21.0));
            TemperatureLog log = new TemperatureLog(input);
            input.add(999.0);                 // 외부에서 원본 변형
            assertEquals(2, log.count());      // 내부에 새지 않아야 한다
        }

        @Test
        void readings가_반환한_리스트를_변형해도_내부는_불변() {
            TemperatureLog log = new TemperatureLog(List.of(20.0, 21.0));
            // 내부 참조를 그대로 반환하면 외부 변경이 샌다 → 복사본/수정불가여야 한다.
            assertThrows(UnsupportedOperationException.class, () -> log.readings().add(999.0));
            assertEquals(2, log.count());
        }
    }

    @Nested
    @DisplayName("불변 객체 — withReading은 새 인스턴스를 만든다")
    class Immutable {

        @Test
        void withReading은_원본을_바꾸지_않고_새_로그를_반환() {
            TemperatureLog original = new TemperatureLog(List.of(20.0));
            TemperatureLog updated = original.withReading(30.0);

            assertEquals(1, original.count());   // 원본 불변
            assertEquals(2, updated.count());     // 새 인스턴스
            assertEquals(30.0, updated.max());
        }
    }
}
