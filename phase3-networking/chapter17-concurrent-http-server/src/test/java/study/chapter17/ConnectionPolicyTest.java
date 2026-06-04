package study.chapter17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import study.chapter16.Headers;
import study.chapter16.HttpRequest;
import study.chapter16.HttpResponse;
import study.chapter16.RequestLine;
import study.chapter16.StatusLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ConnectionPolicy — keep-alive 협상 진리표 (순수 함수, 결정적)")
class ConnectionPolicyTest {

    private static HttpRequest request(String version, String connectionHeader) {
        Headers h = new Headers();
        if (connectionHeader != null) {
            h = h.with("Connection", connectionHeader);
        }
        return new HttpRequest(new RequestLine("GET", "/", version), h, null);
    }

    private static final ServerLimits LIMITS = ServerLimits.defaults();

    @Nested
    @DisplayName("clientWantsKeepAlive — 버전 기본값 + Connection 헤더")
    class ClientWants {

        @Test
        void HTTP_1_1은_기본_keep_alive() {
            assertTrue(ConnectionPolicy.clientWantsKeepAlive(request("HTTP/1.1", null)));
        }

        @Test
        void HTTP_1_1에_Connection_close면_종료() {
            assertFalse(ConnectionPolicy.clientWantsKeepAlive(request("HTTP/1.1", "close")));
        }

        @Test
        void close는_대소문자_무시() {
            assertFalse(ConnectionPolicy.clientWantsKeepAlive(request("HTTP/1.1", "Close")));
        }

        @Test
        void HTTP_1_0은_기본_종료_명시해야_유지() {
            assertFalse(ConnectionPolicy.clientWantsKeepAlive(request("HTTP/1.0", null)));
            assertTrue(ConnectionPolicy.clientWantsKeepAlive(request("HTTP/1.0", "keep-alive")));
        }
    }

    @Nested
    @DisplayName("shouldKeepAlive — max-requests 한도 합산")
    class ShouldKeepAlive {

        @Test
        void 클라가_원하고_한도_안이면_유지() {
            assertTrue(ConnectionPolicy.shouldKeepAlive(request("HTTP/1.1", null), 0, LIMITS));
        }

        @Test
        void max_requests_도달_직전이면_종료() {
            // maxRequestsPerConnection=100이면, 이미 99개 처리(다음이 100번째)면 종료
            assertFalse(ConnectionPolicy.shouldKeepAlive(request("HTTP/1.1", null), 99, LIMITS));
        }

        @Test
        void 클라가_close면_한도_안이라도_종료() {
            assertFalse(ConnectionPolicy.shouldKeepAlive(request("HTTP/1.1", "close"), 0, LIMITS));
        }
    }

    @Nested
    @DisplayName("applyConnectionHeader / responseMustClose")
    class ResponseSide {

        @Test
        void keepAlive면_keep_alive_헤더() {
            HttpResponse res = ConnectionPolicy.applyConnectionHeader(Responses.ok(new byte[0]), true);
            assertEquals("keep-alive", res.headers().get("Connection").orElseThrow());
        }

        @Test
        void close면_close_헤더() {
            HttpResponse res = ConnectionPolicy.applyConnectionHeader(Responses.ok(new byte[0]), false);
            assertEquals("close", res.headers().get("Connection").orElseThrow());
        }

        @Test
        void 길이_확정된_응답은_close_강제_안_함() {
            assertFalse(ConnectionPolicy.responseMustClose(Responses.ok("hi".getBytes())));
        }

        @Test
        void 길이_미확정_바디는_close_강제() {
            // Content-Length도 chunked도 없는데 바디가 있는 응답 → 길이 못 정함 → 반드시 close
            HttpResponse noLen = new HttpResponse(
                    new StatusLine("HTTP/1.1", 200, "OK"), new Headers(), "body".getBytes());
            assertTrue(ConnectionPolicy.responseMustClose(noLen));
        }
    }
}
