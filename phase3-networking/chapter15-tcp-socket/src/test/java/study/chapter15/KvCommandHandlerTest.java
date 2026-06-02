package study.chapter15;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KvCommandHandler — 라인 기반 명령 프로토콜 (SET/GET/DEL, +/$/- 프리픽스)")
class KvCommandHandlerTest {

    private static final java.nio.charset.Charset UTF_8 = StandardCharsets.UTF_8;

    @Nested
    @DisplayName("reply — 순수 함수 (소켓·스트림 무관, 완전 결정적)")
    class Reply {

        @Test
        void SET은_OK() {
            assertEquals("+OK", new KvCommandHandler().reply("SET foo bar"));
        }

        @Test
        void SET_후_GET은_값을_달러로() {
            KvCommandHandler h = new KvCommandHandler();
            h.reply("SET foo bar");
            assertEquals("$bar", h.reply("GET foo"));
        }

        @Test
        void GET_없는_키는_에러() {
            assertEquals("-ERR no such key", new KvCommandHandler().reply("GET missing"));
        }

        @Test
        void DEL_있는_키는_OK_없는_키는_에러() {
            KvCommandHandler h = new KvCommandHandler();
            h.reply("SET k v");
            assertEquals("+OK", h.reply("DEL k"));
            assertEquals("-ERR no such key", h.reply("DEL k"));
        }

        @Test
        void 알_수_없는_명령은_에러() {
            assertEquals("-ERR unknown command", new KvCommandHandler().reply("PING"));
        }

        @Test
        void value에_공백을_허용한다() {
            KvCommandHandler h = new KvCommandHandler();
            h.reply("SET k a b c");          // value = "a b c"
            assertEquals("$a b c", h.reply("GET k"));
        }
    }

    @Nested
    @DisplayName("상태 접근자 — get / size")
    class State {

        @Test
        void SET으로_늘고_DEL로_준다() {
            KvCommandHandler h = new KvCommandHandler();
            h.reply("SET a 1");
            h.reply("SET b 2");
            assertEquals(2, h.size());
            assertEquals("1", h.get("a").orElseThrow());
            h.reply("DEL a");
            assertEquals(1, h.size());
            assertTrue(h.get("a").isEmpty());
        }
    }

    @Nested
    @DisplayName("handle — 스트림 위 통합 (BAIS/BAOS, 소켓 없이 결정적)")
    class Handle {

        @Test
        void 요청_여러_줄을_차례로_응답() throws Exception {
            String wire = "SET foo bar\r\nGET foo\r\nGET nope\r\n";
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new KvCommandHandler().handle(new ByteArrayInputStream(wire.getBytes(UTF_8)), out);
            assertEquals("+OK\r\n$bar\r\n-ERR no such key\r\n", out.toString(UTF_8));
        }
    }
}
