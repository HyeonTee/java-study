package study.chapter08;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GenerationalHeap — Minor GC와 객체 승격 시뮬레이션")
class GenerationalHeapTest {

    @Nested
    @DisplayName("할당")
    class Allocate {

        @Test
        void 새_객체는_young에_age0() {
            GenerationalHeap heap = new GenerationalHeap();
            HeapObject a = new HeapObject("a");
            heap.allocate(a);
            assertEquals(1, heap.youngCount());
            assertEquals(0, heap.oldCount());
            assertEquals(0, heap.ageOf(a));
        }
    }

    @Nested
    @DisplayName("Minor GC — 생존과 수거")
    class MinorGc {

        @Test
        void 도달가능한_young은_살아남아_age가_증가() {
            GenerationalHeap heap = new GenerationalHeap();
            HeapObject a = new HeapObject("a");
            heap.allocate(a);
            Set<HeapObject> collected = heap.minorGc(List.of(a), 15);
            assertTrue(collected.isEmpty());
            assertEquals(1, heap.ageOf(a));
            assertEquals(1, heap.youngCount());
        }

        @Test
        void 도달불가능한_young은_수거된다() {
            GenerationalHeap heap = new GenerationalHeap();
            HeapObject root = new HeapObject("root");
            HeapObject garbage = new HeapObject("garbage");
            heap.allocate(root);
            heap.allocate(garbage);   // 아무도 참조 안 함
            Set<HeapObject> collected = heap.minorGc(List.of(root), 15);
            assertEquals(Set.of(garbage), collected);
            assertEquals(1, heap.youngCount());     // root만 남음
            assertEquals(-1, heap.ageOf(garbage));   // 사라짐
        }

        @Test
        void 참조로_이어진_객체도_생존() {
            GenerationalHeap heap = new GenerationalHeap();
            HeapObject root = new HeapObject("root");
            HeapObject child = new HeapObject("child");
            root.pointTo(child);     // root → child
            heap.allocate(root);
            heap.allocate(child);
            Set<HeapObject> collected = heap.minorGc(List.of(root), 15);
            assertTrue(collected.isEmpty());
            assertEquals(2, heap.youngCount());
        }
    }

    @Nested
    @DisplayName("승격(promotion)과 old 세대")
    class Promotion {

        @Test
        void age가_임계값에_도달하면_old로_승격() {
            GenerationalHeap heap = new GenerationalHeap();
            HeapObject a = new HeapObject("a");
            heap.allocate(a);
            // tenuringThreshold=3: 3번째 생존에서 age 3 → 승격
            heap.minorGc(List.of(a), 3);   // age 1
            heap.minorGc(List.of(a), 3);   // age 2
            assertEquals(2, heap.ageOf(a));
            assertEquals(0, heap.oldCount());

            heap.minorGc(List.of(a), 3);   // age 3 → 승격
            assertTrue(heap.isInOldGen(a));
            assertEquals(1, heap.oldCount());
            assertEquals(0, heap.youngCount());
            assertEquals(-1, heap.ageOf(a));   // 더 이상 young 아님
        }

        @Test
        void old_세대는_Minor_GC에_수거되지_않는다() {
            GenerationalHeap heap = new GenerationalHeap();
            HeapObject a = new HeapObject("a");
            heap.allocate(a);
            heap.minorGc(List.of(a), 1);   // age 1 ≥ 1 → 즉시 승격
            assertTrue(heap.isInOldGen(a));

            // roots가 비어도(young에서 도달 불가) old의 a는 그대로 생존
            Set<HeapObject> collected = heap.minorGc(List.of(), 1);
            assertTrue(collected.isEmpty());
            assertEquals(1, heap.oldCount());
        }
    }
}
