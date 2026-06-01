package study.chapter07;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DisplayName("WeakValueCache — 약한 참조 캐시")
class WeakValueCacheTest {

    // ── 계약(contract): 강한 참조를 들고 있는 동안의 결정적 동작 ──────────

    @Nested
    @DisplayName("기본 계약 (강한 참조 유지 중)")
    class Contract {

        @Test
        void put한_값을_get할_수_있다() {
            WeakValueCache<String, String> cache = new WeakValueCache<>();
            String value = "hello";   // 강한 참조 유지
            cache.put("k", value);
            assertEquals(Optional.of("hello"), cache.get("k"));
        }

        @Test
        void 없는_키는_빈_Optional() {
            WeakValueCache<String, String> cache = new WeakValueCache<>();
            assertEquals(Optional.empty(), cache.get("missing"));
        }

        @Test
        void 같은_키에_다시_put하면_새_값으로_덮어쓴다() {
            WeakValueCache<String, String> cache = new WeakValueCache<>();
            String v1 = "first";
            String v2 = "second";
            cache.put("k", v1);
            cache.put("k", v2);
            assertEquals(Optional.of("second"), cache.get("k"));
        }

        @Test
        void size는_살아있는_항목_수를_센다() {
            WeakValueCache<String, String> cache = new WeakValueCache<>();
            String a = "a";
            String b = "b";   // 강한 참조 유지
            cache.put("a", a);
            cache.put("b", b);
            assertEquals(2, cache.size());
        }
    }

    // ── GC 동작: 보장 불가능하므로 "성립하면 검증, 아니면 skip" ───────────

    @Nested
    @DisplayName("GC 동작 (System.gc는 힌트일 뿐 — 검증되면 단언, 아니면 skip)")
    class GcBehavior {

        @Test
        void 강한_참조가_사라지면_값이_수거되어_get이_empty가_된다() {
            WeakValueCache<String, Object> cache = new WeakValueCache<>();

            // 제어용 약한 참조(probe). 이것이 비워졌다는 것은 GC가 실제로 수거했다는 증거다.
            Object value = new Object();
            WeakReference<Object> probe = new WeakReference<>(value);
            cache.put("k", value);

            value = null;   // 유일한 강한 참조 제거

            boolean collected = forceCollect(probe);
            // GC가 정말로 수거했을 때만 캐시도 비어야 한다는 불변식을 검증한다.
            // GC가 끝내 수거하지 않았다면(보장 없음) 이 테스트는 실패가 아니라 skip된다.
            assumeTrue(collected, "이 JVM/실행에서 GC가 referent를 수거하지 않음 — skip");
            assertEquals(Optional.empty(), cache.get("k"),
                    "referent가 GC되었으면 캐시 조회도 빈 결과여야 한다");
        }

        /**
         * System.gc()는 힌트일 뿐이라, 할당 압박을 주며 여러 번 시도하고
         * 제어용 약한 참조가 비워졌는지로 "실제 수거 여부"를 판단한다.
         */
        private static boolean forceCollect(WeakReference<?> probe) {
            for (int i = 0; i < 50 && probe.get() != null; i++) {
                System.gc();
                // 약간의 할당 압박
                byte[] pressure = new byte[1 << 20];
                pressure[0] = 1;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            return probe.get() == null;
        }
    }
}
