package study.chapter17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import study.chapter16.Headers;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ConnectionClose — connection-close(EOF) 프레이밍 (응답 측)")
class ConnectionCloseTest {

    private static final java.nio.charset.Charset UTF_8 = StandardCharsets.UTF_8;

    @Test
    void readUntilEof_전부_누적() throws Exception {
        byte[] data = "hello world".getBytes(UTF_8);
        assertArrayEquals(data, ConnectionClose.readUntilEof(new ByteArrayInputStream(data)));
    }

    @Test
    void 빈_스트림은_빈_배열() throws Exception {
        assertArrayEquals(new byte[0], ConnectionClose.readUntilEof(new ByteArrayInputStream(new byte[0])));
    }

    @Test
    void 단편화돼도_전부_누적() throws Exception {
        byte[] data = "abcdefghij".getBytes(UTF_8);
        assertArrayEquals(data, ConnectionClose.readUntilEof(new ChunkedInputStream(data, 1)));
    }

    @Test
    void frameResponseBody_Content_Length_있으면_그_길이만() throws Exception {
        // Content-Length 있으면 EOF까지가 아니라 정확히 그 길이만 읽는다(뒤에 쓰레기가 와도)
        Headers h = new Headers().with("Content-Length", "5");
        byte[] body = ConnectionClose.frameResponseBody(h, new ByteArrayInputStream("hello EXTRA".getBytes(UTF_8)));
        assertEquals("hello", new String(body, UTF_8));
    }

    @Test
    void frameResponseBody_길이_없으면_EOF까지_connection_close() throws Exception {
        // Content-Length도 chunked도 없는 응답 → connection-close 프레이밍(EOF까지)
        Headers h = new Headers();
        byte[] body = ConnectionClose.frameResponseBody(h, new ByteArrayInputStream("until eof".getBytes(UTF_8)));
        assertEquals("until eof", new String(body, UTF_8));
    }
}
