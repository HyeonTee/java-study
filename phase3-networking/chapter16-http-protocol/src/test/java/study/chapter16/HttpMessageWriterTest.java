package study.chapter16;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("HttpMessageWriter — 직렬화 (파싱의 역함수, CRLF·flush)")
class HttpMessageWriterTest {

    private static final java.nio.charset.Charset UTF_8 = StandardCharsets.UTF_8;

    @Test
    void 요청_직렬화() throws Exception {
        HttpRequest req = new HttpRequest(
                new RequestLine("GET", "/", "HTTP/1.1"),
                new Headers().with("Host", "a"),
                null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpMessageWriter.writeRequest(out, req);
        // 헤더는 정규화 키(lowercase)로 직렬화, 헤더 종료 빈 줄, 바디 없음
        assertEquals("GET / HTTP/1.1\r\nhost: a\r\n\r\n", out.toString(UTF_8));
    }

    @Test
    void 응답_직렬화() throws Exception {
        HttpResponse res = new HttpResponse(
                new StatusLine("HTTP/1.1", 200, "OK"),
                new Headers().with("Content-Length", "2"),
                "hi".getBytes(UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpMessageWriter.writeResponse(out, res);
        assertEquals("HTTP/1.1 200 OK\r\ncontent-length: 2\r\n\r\nhi", out.toString(UTF_8));
    }
}
