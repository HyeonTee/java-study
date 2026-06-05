package study.chapter16;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("LengthFramedTransport — 길이 프레이밍 (ch15 FrameCodec → ch17 Content-Length)")
class LengthFramedTransportTest {

    private static final java.nio.charset.Charset UTF_8 = StandardCharsets.UTF_8;

    private static byte[] bytes(String s) {
        return s.getBytes(UTF_8);
    }

    /** payload 하나를 프레임으로 인코딩한 바이트. */
    private static byte[] framed(String payload) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        LengthFramedTransport.writeFrame(out, bytes(payload));
        return out.toByteArray();
    }

    @Nested
    @DisplayName("round-trip / 연속 프레임")
    class RoundTrip {

        @Test
        void 한_프레임_왕복() throws Exception {
            InputStream in = new ByteArrayInputStream(framed("hello"));
            assertArrayEquals(bytes("hello"), LengthFramedTransport.readFrame(in));
            assertNull(LengthFramedTransport.readFrame(in));   // 경계에서 깔끔한 끝
        }

        @Test
        void 연속_프레임을_이어_읽는다() throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            LengthFramedTransport.writeFrame(out, bytes("one"));
            LengthFramedTransport.writeFrame(out, bytes("two"));
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            assertArrayEquals(bytes("one"), LengthFramedTransport.readFrame(in));
            assertArrayEquals(bytes("two"), LengthFramedTransport.readFrame(in));
            assertNull(LengthFramedTransport.readFrame(in));
        }

        @Test
        void 빈_페이로드() throws Exception {
            InputStream in = new ByteArrayInputStream(framed(""));
            assertArrayEquals(new byte[0], LengthFramedTransport.readFrame(in));
        }
    }

    @Nested
    @DisplayName("readFully — 부분 읽기를 정확히 채운다 (ChunkedInputStream)")
    class Fully {

        @Test
        void 정확히_n바이트를_채운다() throws Exception {
            byte[] data = bytes("abcdef");
            byte[] got = LengthFramedTransport.readFully(new ByteArrayInputStream(data), 6);
            assertArrayEquals(data, got);
        }

        @Test
        void 한_바이트씩_쪼개_와도_채운다() throws Exception {
            // chunkSize 1 → read가 1바이트씩만 줘도 readFully는 정확히 채워야 한다.
            byte[] data = bytes("abcdef");
            byte[] got = LengthFramedTransport.readFully(new ChunkedInputStream(data, 1), 6);
            assertArrayEquals(data, got);
        }

        @Test
        void 즉시_EOF면_null() throws Exception {
            assertNull(LengthFramedTransport.readFully(new ByteArrayInputStream(new byte[0]), 4));
        }

        @Test
        void 도중_EOF면_EOFException() {
            // 3바이트뿐인데 4바이트를 요구 → 잘림
            assertThrows(EOFException.class,
                    () -> LengthFramedTransport.readFully(new ByteArrayInputStream(bytes("abc")), 4));
        }
    }

    @Nested
    @DisplayName("readFrame — truncated / 음수 길이 / 길이 도중 EOF (ChunkedInputStream으로 경계가 길이 한가운데 걸쳐도)")
    class FrameErrors {

        @Test
        void 페이로드가_잘리면_EOFException() throws Exception {
            byte[] full = framed("hello");
            byte[] truncated = Arrays.copyOf(full, full.length - 2);   // 페이로드 끝 2바이트 절단
            assertThrows(EOFException.class,
                    () -> LengthFramedTransport.readFrame(new ByteArrayInputStream(truncated)));
        }

        @Test
        void 길이가_2바이트만_오면_EOFException() {
            assertThrows(EOFException.class,
                    () -> LengthFramedTransport.readFrame(new ByteArrayInputStream(new byte[]{0, 0})));
        }

        @Test
        void 음수_길이는_IllegalArgument() {
            byte[] negative = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};   // -1
            assertThrows(IllegalArgumentException.class,
                    () -> LengthFramedTransport.readFrame(new ByteArrayInputStream(negative)));
        }

        @Test
        void 경계가_쪼개져_와도_정확히_복원() throws Exception {
            // 두 프레임을 1바이트씩 쪼개 줘도(길이 프리픽스 한가운데가 걸려도) 정확히 디코딩.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            LengthFramedTransport.writeFrame(out, bytes("first"));
            LengthFramedTransport.writeFrame(out, bytes("second"));
            InputStream in = new ChunkedInputStream(out.toByteArray(), 1);
            assertEquals("first", new String(LengthFramedTransport.readFrame(in), UTF_8));
            assertEquals("second", new String(LengthFramedTransport.readFrame(in), UTF_8));
            assertNull(LengthFramedTransport.readFrame(in));
        }
    }
}
