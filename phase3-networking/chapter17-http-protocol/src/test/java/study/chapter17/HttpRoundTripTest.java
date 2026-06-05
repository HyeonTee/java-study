package study.chapter17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("round-trip — write↔parse 항등 (클라이언트↔서버 대칭)")
class HttpRoundTripTest {

    private static final java.nio.charset.Charset UTF_8 = StandardCharsets.UTF_8;

    @Nested
    @DisplayName("in-memory (BAIS/BAOS, 결정적)")
    class InMemory {

        @Test
        void 요청_write후_parse하면_원본과_동일() throws Exception {
            HttpRequest original = new HttpRequest(
                    new RequestLine("POST", "/submit", "HTTP/1.1"),
                    new Headers().with("Host", "example.com").with("Content-Length", "5"),
                    "hello".getBytes(UTF_8));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HttpMessageWriter.writeRequest(out, original);
            HttpRequest parsed = HttpParser.parse(new ByteArrayInputStream(out.toByteArray()));

            assertEquals(original.line(), parsed.line());
            assertEquals(original.headers().get("content-length"), parsed.headers().get("Content-Length"));
            // byte[]는 record equals가 참조 비교라 Arrays.equals로 검증(README 함정 박스)
            assertArrayEquals(original.body(), parsed.body());
        }

        @Test
        void 응답_write후_parse하면_원본과_동일() throws Exception {
            HttpResponse original = new HttpResponse(
                    new StatusLine("HTTP/1.1", 404, "Not Found"),
                    new Headers().with("Content-Length", "3"),
                    "no!".getBytes(UTF_8));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HttpMessageWriter.writeResponse(out, original);
            HttpResponse parsed = HttpParser.parseResponse(new ByteArrayInputStream(out.toByteArray()));

            assertEquals(original.line(), parsed.line());
            assertArrayEquals(original.body(), parsed.body());
        }
    }

    @Nested
    @DisplayName("진짜 소켓 — loopback 1왕복 (ch16 4규율: :0 + @Timeout + half-close)")
    class RealSocket {

        @Test
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void 미니_클라이언트가_loopback_서버와_1왕복() throws Exception {
            try (ServerSocket server = new ServerSocket()) {
                server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
                int port = server.getLocalPort();

                // 서버: 요청 파싱 → "echo:<target>" 응답 직렬화 (단일 연결)
                Thread serverThread = new Thread(() -> {
                    try (Socket s = server.accept()) {
                        HttpRequest req = HttpParser.parse(s.getInputStream());
                        byte[] body = ("echo:" + req.line().target()).getBytes(UTF_8);
                        HttpResponse res = new HttpResponse(
                                new StatusLine("HTTP/1.1", 200, "OK"),
                                new Headers().with("Content-Length", String.valueOf(body.length)),
                                body);
                        HttpMessageWriter.writeResponse(s.getOutputStream(), res);
                    } catch (IOException ignored) {
                        // 연결 종료/소켓 닫힘은 테스트 흐름상 정상
                    }
                }, "http-server");
                serverThread.setDaemon(true);
                serverThread.start();

                MiniHttpClient client =
                        new MiniHttpClient(InetAddress.getLoopbackAddress().getHostAddress(), port);
                HttpResponse res = client.exchange(new HttpRequest(
                        new RequestLine("GET", "/hi", "HTTP/1.1"),
                        new Headers().with("Host", "localhost"),
                        null));

                assertEquals(200, res.line().code());
                assertArrayEquals("echo:/hi".getBytes(UTF_8), res.body());
                serverThread.join(5000);
            }
        }
    }
}
