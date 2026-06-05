package study.chapter17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("HttpParser — HTTP = 라인 프레이밍 ∘ 길이 프레이밍 (간판)")
class HttpParserTest {

    private static final java.nio.charset.Charset UTF_8 = StandardCharsets.UTF_8;

    private static InputStream wire(String s) {
        return new ByteArrayInputStream(s.getBytes(UTF_8));
    }

    @Nested
    @DisplayName("원시 빌딩블록 — readLine / readFully (ch16 회수)")
    class Primitives {

        @Test
        void readLine_CRLF와_LF_모두_흡수() throws Exception {
            InputStream in = wire("abc\r\ndef\n");
            assertEquals("abc", HttpParser.readLine(in));
            assertEquals("def", HttpParser.readLine(in));
            assertNull(HttpParser.readLine(in));
        }

        @Test
        void readLine_빈_줄과_끝_구분() throws Exception {
            InputStream in = wire("\r\n");
            assertEquals("", HttpParser.readLine(in));
            assertNull(HttpParser.readLine(in));
        }

        @Test
        void readFully_정확히_n바이트() throws Exception {
            assertArrayEquals("abcdef".getBytes(UTF_8), HttpParser.readFully(wire("abcdef"), 6));
        }

        @Test
        void readFully_즉시_EOF면_null() throws Exception {
            assertNull(HttpParser.readFully(wire(""), 4));
        }

        @Test
        void readFully_도중_EOF면_EOFException() {
            assertThrows(EOFException.class, () -> HttpParser.readFully(wire("abc"), 4));
        }
    }

    @Nested
    @DisplayName("시작줄 파싱")
    class StartLine {

        @Test
        void 요청라인_3토큰() throws Exception {
            RequestLine rl = HttpParser.parseRequestLine("GET /index.html HTTP/1.1");
            assertEquals("GET", rl.method());
            assertEquals("/index.html", rl.target());
            assertEquals("HTTP/1.1", rl.version());
        }

        @Test
        void 미지_메서드도_보존() throws Exception {
            assertEquals("PROPFIND", HttpParser.parseRequestLine("PROPFIND /x HTTP/1.1").method());
        }

        @Test
        void 요청라인_토큰수_틀리면_예외() {
            assertThrows(HttpProtocolException.class, () -> HttpParser.parseRequestLine("GET /onlytwo"));
        }

        @Test
        void 상태줄_정상() throws Exception {
            StatusLine sl = HttpParser.parseStatusLine("HTTP/1.1 200 OK");
            assertEquals(200, sl.code());
            assertEquals("OK", sl.reason());
        }

        @Test
        void 상태줄_reason에_공백() throws Exception {
            assertEquals("Not Found", HttpParser.parseStatusLine("HTTP/1.1 404 Not Found").reason());
        }

        @Test
        void 상태줄_빈_reason_허용() throws Exception {
            // "HTTP/1.1 204 " (trailing space) 와 "HTTP/1.1 204" 둘 다 reason=""로 보정
            assertEquals("", HttpParser.parseStatusLine("HTTP/1.1 204 ").reason());
            assertEquals("", HttpParser.parseStatusLine("HTTP/1.1 204").reason());
        }

        @Test
        void 상태줄_비정수_코드는_예외() {
            assertThrows(HttpProtocolException.class, () -> HttpParser.parseStatusLine("HTTP/1.1 XX OK"));
        }
    }

    @Nested
    @DisplayName("요청 파싱 — 세 가지 바디 프레이밍")
    class ParseRequest {

        @Test
        void 바디_없는_GET() throws Exception {
            HttpRequest req = HttpParser.parse(wire("GET /index.html HTTP/1.1\r\nHost: example.com\r\n\r\n"));
            assertEquals("GET", req.line().method());
            assertEquals("example.com", req.headers().get("Host").orElseThrow());
            assertArrayEquals(new byte[0], req.body());
        }

        @Test
        void Content_Length_바디() throws Exception {
            HttpRequest req = HttpParser.parse(wire("POST /x HTTP/1.1\r\nContent-Length: 5\r\n\r\nhello"));
            assertArrayEquals("hello".getBytes(UTF_8), req.body());
        }

        @Test
        void chunked_바디() throws Exception {
            HttpRequest req = HttpParser.parse(
                    wire("POST /x HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n5\r\nhello\r\n0\r\n\r\n"));
            assertArrayEquals("hello".getBytes(UTF_8), req.body());
        }

        @Test
        void 단편화돼도_정확히_파싱() throws Exception {
            // 한 바이트씩 쪼개 와도(readLine을 read()로 구현해야 통과) 결과 동일.
            byte[] bytes = "POST /x HTTP/1.1\r\nContent-Length: 5\r\n\r\nhello".getBytes(UTF_8);
            HttpRequest req = HttpParser.parse(new ChunkedInputStream(bytes, 1));
            assertEquals("/x", req.line().target());
            assertArrayEquals("hello".getBytes(UTF_8), req.body());
        }
    }

    @Nested
    @DisplayName("응답 파싱")
    class ParseResponse {

        @Test
        void Content_Length_응답() throws Exception {
            HttpResponse res = HttpParser.parseResponse(wire("HTTP/1.1 200 OK\r\nContent-Length: 2\r\n\r\nhi"));
            assertEquals(200, res.line().code());
            assertEquals("OK", res.line().reason());
            assertArrayEquals("hi".getBytes(UTF_8), res.body());
        }
    }

    @Nested
    @DisplayName("malformed → HttpProtocolException (검사 예외 일원화)")
    class Malformed {

        @Test
        void 즉시_EOF() {
            assertThrows(HttpProtocolException.class, () -> HttpParser.parse(wire("")));
        }

        @Test
        void 헤더가_빈_줄_없이_끝남() {
            assertThrows(HttpProtocolException.class,
                    () -> HttpParser.parse(wire("GET / HTTP/1.1\r\nHost: a\r\n")));
        }

        @Test
        void 바디가_Content_Length에_못_미침() {
            assertThrows(HttpProtocolException.class,
                    () -> HttpParser.parse(wire("POST /x HTTP/1.1\r\nContent-Length: 10\r\n\r\nshort")));
        }
    }
}
