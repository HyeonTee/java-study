package study.chapter17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Headers — 대소문자 무시 헤더 맵 (ch02 함정 회수 + ch05 Optional)")
class HeadersTest {

    @Nested
    @DisplayName("case-insensitive 조회 / 불변 with")
    class CaseInsensitive {

        @Test
        void 대소문자_달라도_같은_헤더() {
            Headers h = new Headers().with("Content-Length", "42");
            assertEquals("42", h.get("content-length").orElseThrow());
            assertEquals("42", h.get("CONTENT-LENGTH").orElseThrow());
        }

        @Test
        void 없는_헤더는_빈_Optional() {
            assertTrue(new Headers().get("X-Nope").isEmpty());
        }

        @Test
        void with는_원본을_바꾸지_않는다() {
            Headers base = new Headers();
            Headers derived = base.with("Host", "example.com");
            assertTrue(base.get("Host").isEmpty(), "원본은 불변이어야 한다");
            assertEquals("example.com", derived.get("host").orElseThrow());
        }

        @Test
        void 같은_이름_재추가는_덮어쓴다() {
            Headers h = new Headers().with("X-A", "1").with("x-a", "2");
            assertEquals("2", h.get("X-A").orElseThrow());
        }
    }

    @Nested
    @DisplayName("contentLength / isChunked")
    class Framing {

        @Test
        void Content_Length_파싱() throws Exception {
            assertEquals(5L, new Headers().with("Content-Length", "5").contentLength().orElseThrow());
        }

        @Test
        void Content_Length_없으면_empty() throws Exception {
            assertTrue(new Headers().contentLength().isEmpty());
        }

        @Test
        void 음수_Content_Length는_예외() {
            Headers h = new Headers().with("Content-Length", "-1");
            assertThrows(HttpProtocolException.class, h::contentLength);
        }

        @Test
        void 비정수_Content_Length는_예외() {
            Headers h = new Headers().with("Content-Length", "abc");
            assertThrows(HttpProtocolException.class, h::contentLength);
        }

        @Test
        void Transfer_Encoding_chunked_감지() {
            assertTrue(new Headers().with("Transfer-Encoding", "chunked").isChunked());
            assertFalse(new Headers().with("Transfer-Encoding", "gzip").isChunked());
            assertFalse(new Headers().isChunked());
        }
    }

    @Nested
    @DisplayName("writeTo — CRLF 직렬화")
    class Serialize {

        @Test
        void 각_헤더를_CRLF로_쓴다() throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new Headers().with("Host", "a").with("Content-Length", "0").writeTo(out);
            // 정규화 키(lowercase)로 직렬화, 삽입 순서 보존, 빈 줄은 쓰지 않음
            assertEquals("host: a\r\ncontent-length: 0\r\n", out.toString(StandardCharsets.UTF_8));
        }
    }
}
