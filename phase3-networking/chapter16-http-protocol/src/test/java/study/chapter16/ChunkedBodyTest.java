package study.chapter16;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ChunkedBody.decode — chunked transfer-encoding 디코더 (세 번째 프레이밍)")
class ChunkedBodyTest {

    private static final java.nio.charset.Charset UTF_8 = StandardCharsets.UTF_8;

    private static InputStream wire(String s) {
        return new ByteArrayInputStream(s.getBytes(UTF_8));
    }

    private static String decode(InputStream in) throws Exception {
        return new String(ChunkedBody.decode(in), UTF_8);
    }

    @Nested
    @DisplayName("정상 디코딩")
    class Ok {

        @Test
        void 단일_청크() throws Exception {
            assertEquals("hello", decode(wire("5\r\nhello\r\n0\r\n\r\n")));
        }

        @Test
        void 다중_청크_이어붙이기() throws Exception {
            assertEquals("hello world", decode(wire("5\r\nhello\r\n6\r\n world\r\n0\r\n\r\n")));
        }

        @Test
        void 빈_본문은_즉시_0청크() throws Exception {
            assertEquals("", decode(wire("0\r\n\r\n")));
        }

        @Test
        void 대문자_hex_크기() throws Exception {
            // 0x1A = 26바이트. 16진수는 대소문자 무관(Integer.parseInt가 아니라 Long.parseLong base16).
            String payload = "abcdefghijklmnopqrstuvwxyz";   // 26
            assertEquals(payload, decode(wire("1A\r\n" + payload + "\r\n0\r\n\r\n")));
        }

        @Test
        void 트레일러_헤더는_무시하고_빈_줄까지_흡수() throws Exception {
            assertEquals("hi", decode(wire("2\r\nhi\r\n0\r\nX-Checksum: abc\r\nX-Trace: 1\r\n\r\n")));
        }

        @Test
        void 청크가_한_바이트씩_쪼개_와도_복원() throws Exception {
            // 길이 프리픽스가 read 경계에 걸쳐도 readLine/readFully가 정확히 복원해야 한다.
            byte[] bytes = "5\r\nhello\r\n6\r\n world\r\n0\r\n\r\n".getBytes(UTF_8);
            assertEquals("hello world", decode(new ChunkedInputStream(bytes, 1)));
        }
    }

    @Nested
    @DisplayName("malformed → HttpProtocolException")
    class Malformed {

        @Test
        void hex가_아닌_크기() {
            assertThrows(HttpProtocolException.class, () -> ChunkedBody.decode(wire("xyz\r\nhello\r\n0\r\n\r\n")));
        }

        @Test
        void 청크가_선언_크기에_못_미친_채_EOF() {
            // 크기 5라고 했으나 데이터가 3바이트뿐 → 잘림(readFully EOFException) → HttpProtocolException
            assertThrows(HttpProtocolException.class, () -> ChunkedBody.decode(wire("5\r\nhel")));
        }
    }
}
