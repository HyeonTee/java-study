package study.chapter15;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("LineProtocol — 라인 프레이밍 송수신 (ch14 LineReader 직계, 소켓 無)")
class LineProtocolTest {

    private static final java.nio.charset.Charset UTF_8 = StandardCharsets.UTF_8;

    /** 주어진 문자열을 입력으로, 출력은 버리는 reader. */
    private static LineProtocol reader(String input) {
        return new LineProtocol(new ByteArrayInputStream(input.getBytes(UTF_8)), new ByteArrayOutputStream());
    }

    /** 주어진 InputStream을 입력으로(단편화 픽스처 등), 출력은 버리는 reader. */
    private static LineProtocol reader(InputStream in) {
        return new LineProtocol(in, new ByteArrayOutputStream());
    }

    @Nested
    @DisplayName("readLine — 읽기 계약 (LF·CRLF·빈 줄·EOF)")
    class Read {

        @Test
        void LF로_구분된_줄() throws Exception {
            LineProtocol p = reader("a\nb\n");
            assertEquals("a", p.readLine());
            assertEquals("b", p.readLine());
            assertNull(p.readLine());
        }

        @Test
        void CRLF로_구분된_줄_종결자_제거() throws Exception {
            LineProtocol p = reader("hello\r\nworld\r\n");
            assertEquals("hello", p.readLine());
            assertEquals("world", p.readLine());
            assertNull(p.readLine());
        }

        @Test
        void 마지막_줄에_종결자_없어도_읽는다() throws Exception {
            LineProtocol p = reader("a\nbb");
            assertEquals("a", p.readLine());
            assertEquals("bb", p.readLine());
            assertNull(p.readLine());
        }

        @Test
        void 빈_입력은_바로_null() throws Exception {
            assertNull(reader("").readLine());
        }

        @Test
        void 빈_줄과_끝을_구분한다() throws Exception {
            LineProtocol p = reader("\n");      // 빈 줄 하나
            assertEquals("", p.readLine());      // "" — null 아님
            assertNull(p.readLine());            // 그다음이 끝
        }

        @Test
        void UTF8_한글_디코딩() throws Exception {
            assertEquals("한글", reader("한글\r\n").readLine());
        }
    }

    @Nested
    @DisplayName("writeLine — CRLF 종결 + flush")
    class Write {

        @Test
        void CRLF를_붙여_쓴다() throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            LineProtocol p = new LineProtocol(new ByteArrayInputStream(new byte[0]), out);
            p.writeLine("PING");
            assertArrayEquals("PING\r\n".getBytes(UTF_8), out.toByteArray());
        }

        @Test
        void 여러_줄_연속_쓰기() throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            LineProtocol p = new LineProtocol(new ByteArrayInputStream(new byte[0]), out);
            p.writeLine("+OK");
            p.writeLine("$bar");
            assertEquals("+OK\r\n$bar\r\n", out.toString(UTF_8));
        }
    }

    @Nested
    @DisplayName("serve — EOF까지 루프하며 응답")
    class Serve {

        @Test
        void 받은_줄을_대문자로_되돌린다() throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            LineProtocol p = new LineProtocol(
                    new ByteArrayInputStream("a\r\nbb\r\nccc\r\n".getBytes(UTF_8)), out);
            p.serve(s -> s.toUpperCase());
            assertEquals("A\r\nBB\r\nCCC\r\n", out.toString(UTF_8));
        }

        @Test
        void 입력이_EOF면_아무것도_안_쓴다() throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            LineProtocol p = new LineProtocol(new ByteArrayInputStream(new byte[0]), out);
            p.serve(s -> s);
            assertEquals(0, out.size());
        }
    }

    @Nested
    @DisplayName("프레이밍 — 쪼개져/뭉쳐 와도 결과 동일 (TCP 단편화, 소켓 없이 결정적)")
    class Framing {

        @Test
        void 한_줄이_여러_read로_쪼개져_와도() throws Exception {
            // chunkSize 1 → 1바이트씩만 도착(극단적 단편화). 그래도 줄 경계는 정확히 복원된다.
            LineProtocol p = reader(new ChunkedInputStream("hello\r\nworld\r\n".getBytes(UTF_8), 1));
            assertEquals("hello", p.readLine());
            assertEquals("world", p.readLine());
            assertNull(p.readLine());
        }

        @Test
        void 여러_줄이_한_read에_뭉쳐_와도() throws Exception {
            // 한 배열에 세 줄이 뭉쳐 있어도(=한 번에 도착) 프레이밍이 분리한다.
            LineProtocol p = reader("a\nb\nc\n");
            assertEquals("a", p.readLine());
            assertEquals("b", p.readLine());
            assertEquals("c", p.readLine());
            assertNull(p.readLine());
        }
    }
}
