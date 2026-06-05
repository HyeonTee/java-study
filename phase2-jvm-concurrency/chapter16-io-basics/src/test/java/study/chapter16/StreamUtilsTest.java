package study.chapter16;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("StreamUtils — copy / readAllBytes")
class StreamUtilsTest {

    private static byte[] pattern(int n) {
        byte[] b = new byte[n];
        for (int i = 0; i < n; i++) {
            b[i] = (byte) (i % 256);
        }
        return b;
    }

    @Nested
    @DisplayName("copy")
    class Copy {

        @Test
        void 입력을_그대로_복사하고_바이트수를_반환() throws Exception {
            byte[] in = "hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            long n = StreamUtils.copy(new ByteArrayInputStream(in), out);
            assertArrayEquals(in, out.toByteArray());
            assertEquals(5L, n);
        }

        @Test
        void 빈_입력은_0바이트() throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            assertEquals(0L, StreamUtils.copy(new ByteArrayInputStream(new byte[0]), out));
            assertEquals(0, out.toByteArray().length);
        }

        @Test
        @DisplayName("버퍼 크기를 넘는 큰 입력도 전부 복사 (다회 read)")
        void 큰_입력도_전부() throws Exception {
            byte[] big = pattern(70_000);   // 내부 버퍼(수 KB)보다 충분히 큼
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            assertEquals(big.length, StreamUtils.copy(new ByteArrayInputStream(big), out));
            assertArrayEquals(big, out.toByteArray());
        }

        @Test
        @DisplayName("한 번에 1바이트만 주는 스트림도 정확히 복사 (부분 읽기)")
        void 부분_읽기도_정확() throws Exception {
            byte[] in = pattern(100);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            assertEquals(100L, StreamUtils.copy(new OneByteAtATimeInputStream(in), out));
            assertArrayEquals(in, out.toByteArray());
        }
    }

    @Nested
    @DisplayName("readAllBytes")
    class ReadAll {

        @Test
        void 전체를_바이트배열로() throws Exception {
            byte[] in = pattern(5000);
            assertArrayEquals(in, StreamUtils.readAllBytes(new ByteArrayInputStream(in)));
        }

        @Test
        void 빈_입력은_길이0() throws Exception {
            assertEquals(0, StreamUtils.readAllBytes(new ByteArrayInputStream(new byte[0])).length);
        }
    }
}
