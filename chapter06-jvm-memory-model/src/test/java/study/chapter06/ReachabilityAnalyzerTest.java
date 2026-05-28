package study.chapter06;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReachabilityAnalyzer — mark 단계(도달 가능성 분석) 직접 구현")
class ReachabilityAnalyzerTest {

    @Nested
    @DisplayName("reachable")
    class Reachable {

        @Test
        void root_자신도_도달_가능에_포함된다() {
            HeapObject a = new HeapObject("a");
            assertEquals(Set.of(a), ReachabilityAnalyzer.reachable(List.of(a)));
        }

        @Test
        void 선형_체인은_모두_도달_가능() {
            HeapObject a = new HeapObject("a");
            HeapObject b = new HeapObject("b");
            HeapObject c = new HeapObject("c");
            a.pointTo(b);
            b.pointTo(c);

            assertEquals(Set.of(a, b, c), ReachabilityAnalyzer.reachable(List.of(a)));
        }

        @Test
        void 참조되지_않은_객체는_도달_불가() {
            HeapObject a = new HeapObject("a");
            HeapObject b = new HeapObject("b");
            HeapObject orphan = new HeapObject("orphan");
            a.pointTo(b);

            Set<HeapObject> reachable = ReachabilityAnalyzer.reachable(List.of(a));
            assertTrue(reachable.contains(b));
            assertFalse(reachable.contains(orphan));
        }

        @Test
        void 순환_참조여도_무한루프에_빠지지_않고_종료한다() {
            HeapObject a = new HeapObject("a");
            HeapObject b = new HeapObject("b");
            a.pointTo(b);
            b.pointTo(a);   // 사이클

            assertEquals(Set.of(a, b), ReachabilityAnalyzer.reachable(List.of(a)));
        }

        @Test
        void 자기_자신을_참조하는_노드도_종료한다() {
            HeapObject a = new HeapObject("a");
            a.pointTo(a);   // self-loop
            assertEquals(Set.of(a), ReachabilityAnalyzer.reachable(List.of(a)));
        }

        @Test
        void 여러_root에서_도달_가능한_것을_합친다() {
            HeapObject r1 = new HeapObject("r1");
            HeapObject r2 = new HeapObject("r2");
            HeapObject x = new HeapObject("x");
            HeapObject y = new HeapObject("y");
            r1.pointTo(x);
            r2.pointTo(y);

            assertEquals(Set.of(r1, r2, x, y), ReachabilityAnalyzer.reachable(List.of(r1, r2)));
        }
    }

    @Nested
    @DisplayName("isReachable")
    class IsReachable {

        @Test
        void 도달_가능하면_true() {
            HeapObject a = new HeapObject("a");
            HeapObject b = new HeapObject("b");
            a.pointTo(b);
            assertTrue(ReachabilityAnalyzer.isReachable(List.of(a), b));
        }

        @Test
        void 도달_불가능하면_false() {
            HeapObject a = new HeapObject("a");
            HeapObject lonely = new HeapObject("lonely");
            assertFalse(ReachabilityAnalyzer.isReachable(List.of(a), lonely));
        }
    }

    @Nested
    @DisplayName("collectable (sweep 대상)")
    class Collectable {

        @Test
        void 도달_불가능한_객체가_수거_대상이다() {
            HeapObject a = new HeapObject("a");
            HeapObject b = new HeapObject("b");
            HeapObject c = new HeapObject("c");
            HeapObject d = new HeapObject("d");
            a.pointTo(b);

            Set<HeapObject> garbage =
                    ReachabilityAnalyzer.collectable(List.of(a, b, c, d), List.of(a));
            assertEquals(Set.of(c, d), garbage);
        }

        @Test
        void 참조가_끊긴_객체는_수거_대상이_된다() {
            // a = new; b = new; (어디서도 b를 참조하지 않음)
            HeapObject a = new HeapObject("a");
            HeapObject b = new HeapObject("b");

            Set<HeapObject> garbage =
                    ReachabilityAnalyzer.collectable(List.of(a, b), List.of(a));
            assertEquals(Set.of(b), garbage);
        }

        @Test
        void 순환_참조만_남으면_둘_다_수거_대상이다() {
            // 이것이 핵심: 참조 카운팅이라면 서로를 가리켜 카운트가 0이 안 되지만,
            // 도달 가능성 분석은 root에서 도달 불가능한 사이클을 정상 수거한다.
            HeapObject root = new HeapObject("root");
            HeapObject x = new HeapObject("x");
            HeapObject y = new HeapObject("y");
            x.pointTo(y);
            y.pointTo(x);   // x ↔ y 순환, 단 root에서 도달 불가

            Set<HeapObject> garbage =
                    ReachabilityAnalyzer.collectable(List.of(root, x, y), List.of(root));
            assertEquals(Set.of(x, y), garbage);
        }

        @Test
        void 순환이어도_root에서_도달하면_수거_대상이_아니다() {
            HeapObject root = new HeapObject("root");
            HeapObject x = new HeapObject("x");
            HeapObject y = new HeapObject("y");
            root.pointTo(x);
            x.pointTo(y);
            y.pointTo(x);   // 순환이지만 root → x 로 도달 가능

            Set<HeapObject> garbage =
                    ReachabilityAnalyzer.collectable(List.of(root, x, y), List.of(root));
            assertTrue(garbage.isEmpty());
        }
    }
}
