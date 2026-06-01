package study.chapter14;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Resources — closeAll(LIFO·suppressed) / TrackedResource")
class ResourcesTest {

    /** 닫힌 순서를 공유 로그에 기록하는 자원. */
    private static AutoCloseable logging(String name, List<String> log, boolean fail) {
        return () -> {
            log.add(name);
            if (fail) {
                throw new RuntimeException("close failed: " + name);
            }
        };
    }

    @Nested
    @DisplayName("closeAll")
    class CloseAll {

        @Test
        void 역순_LIFO로_닫는다() throws Exception {
            List<String> log = new ArrayList<>();
            Resources.closeAll(logging("A", log, false), logging("B", log, false), logging("C", log, false));
            assertEquals(List.of("C", "B", "A"), log);
        }

        @Test
        void 중간_close가_실패해도_나머지를_닫고_첫예외를_던진다() {
            List<String> log = new ArrayList<>();
            // 역순으로 C, B, A 닫음 → B가 실패. 첫 예외는 B, 나머지(A,C)도 닫혀야.
            Exception ex = assertThrows(Exception.class, () ->
                    Resources.closeAll(
                            logging("A", log, false),
                            logging("B", log, true),
                            logging("C", log, false)));
            assertEquals(List.of("C", "B", "A"), log, "실패해도 전부 닫기 시도");
            assertTrue(ex.getMessage().contains("B"), "첫(역순 첫) 실패가 대표 예외");
        }

        @Test
        void 여러_close가_실패하면_나머지는_suppressed() {
            List<String> log = new ArrayList<>();
            Exception ex = assertThrows(Exception.class, () ->
                    Resources.closeAll(
                            logging("A", log, true),
                            logging("B", log, true)));
            // 역순: B 먼저 실패(대표), A 실패는 suppressed
            assertTrue(ex.getMessage().contains("B"));
            assertEquals(1, ex.getSuppressed().length);
            assertTrue(ex.getSuppressed()[0].getMessage().contains("A"));
        }

        @Test
        void null_원소는_건너뛴다() throws Exception {
            List<String> log = new ArrayList<>();
            Resources.closeAll(logging("A", log, false), null, logging("C", log, false));
            assertEquals(List.of("C", "A"), log);
        }
    }

    @Nested
    @DisplayName("TrackedResource")
    class Tracked {

        @Test
        void close는_멱등() throws Exception {
            Resources.TrackedResource r = new Resources.TrackedResource("r", false);
            assertFalse(r.isClosed());
            r.close();
            r.close();   // 두 번 호출해도 안전
            assertTrue(r.isClosed());
        }

        @Test
        void 닫힌_뒤_use는_예외() throws Exception {
            Resources.TrackedResource r = new Resources.TrackedResource("r", false);
            r.use();   // 열려 있을 땐 정상
            r.close();
            assertThrows(IllegalStateException.class, r::use);
        }

        @Test
        void try_with_resources로_쓰면_자동_close() throws Exception {
            Resources.TrackedResource r = new Resources.TrackedResource("r", false);
            try (r) {
                r.use();
            }
            assertTrue(r.isClosed());
        }
    }
}
