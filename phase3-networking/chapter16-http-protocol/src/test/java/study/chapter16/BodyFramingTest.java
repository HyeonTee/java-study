package study.chapter16;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("BodyFraming.from — 헤더가 바디 프레이밍을 결정 (RFC 9112 우선순위)")
class BodyFramingTest {

    @Test
    void Content_Length가_있으면_ContentLength() {
        Headers h = new Headers().with("Content-Length", "10");
        BodyFraming f = BodyFraming.from(h);
        BodyFraming.ContentLength cl = assertInstanceOf(BodyFraming.ContentLength.class, f);
        assertEquals(10L, cl.length());
    }

    @Test
    void chunked면_Chunked() {
        Headers h = new Headers().with("Transfer-Encoding", "chunked");
        assertInstanceOf(BodyFraming.Chunked.class, BodyFraming.from(h));
    }

    @Test
    void 둘_다_없으면_None() {
        assertInstanceOf(BodyFraming.None.class, BodyFraming.from(new Headers()));
    }

    @Test
    void chunked가_Content_Length를_이긴다() {
        // RFC 9112 §6.1: Transfer-Encoding이 있으면 Content-Length는 무시된다.
        Headers h = new Headers().with("Content-Length", "10").with("Transfer-Encoding", "chunked");
        assertInstanceOf(BodyFraming.Chunked.class, BodyFraming.from(h));
    }
}
