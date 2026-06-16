package study.chapter16;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("LineReader — 라인 프레이밍 (ch17 소켓 직결)")
class LineReaderTest {

    private static LineReader of(String s) {
        InputStream in = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        return new LineReader(in);
    }

    @Nested
    @DisplayName("기본")
    class Basic {

        @Test
        void LF로_구분된_줄들() throws Exception {
            assertEquals(List.of("a", "b", "c"), of("a\nb\nc\n").readLines());
        }

        @Test
        void 마지막_줄에_개행_없어도_읽는다() throws Exception {
            assertEquals(List.of("a", "b", "c"), of("a\nb\nc").readLines());
        }

        @Test
        void 빈_입력은_빈_리스트() throws Exception {
            assertEquals(List.of(), of("").readLines());
        }

        @Test
        void 줄이_하나뿐() throws Exception {
            assertEquals(List.of("solo"), of("solo").readLines());
        }
    }

    @Nested
    @DisplayName("빈 줄 보존 / CRLF")
    class BlankAndCrlf {

        @Test
        void 중간_빈줄_보존() throws Exception {
            assertEquals(List.of("a", "", "b"), of("a\n\nb").readLines());
        }

        @Test
        void 개행만_있으면_빈줄_하나() throws Exception {
            assertEquals(List.of(""), of("\n").readLines());
        }

        @Test
        void CRLF_구분에서_CR을_제거() throws Exception {
            assertEquals(List.of("a", "b"), of("a\r\nb\r\n").readLines());
        }

        @Test
        void CRLF와_LF_혼재() throws Exception {
            assertEquals(List.of("a", "b", "c"), of("a\r\nb\nc").readLines());
        }

        @Test
        void 단독_CR은_구분자도_제거대상도_아니다() throws Exception {
            // 구 Mac 스타일 단독 '\r'은 줄 구분자가 아니다. \n 바로 앞도 아니라 제거되지도 않는다 → 그대로 보존.
            assertEquals(List.of("a\rb"), of("a\rb\n").readLines());
        }

        @Test
        void UTF8_한글_줄() throws Exception {
            assertEquals(List.of("가나", "다라"), of("가나\n다라").readLines());
        }
    }

    @Nested
    @DisplayName("readLine 직접 — null 종료")
    class ReadLineContract {

        @Test
        void 한_줄씩_읽고_끝에서_null() throws Exception {
            LineReader r = of("x\ny");
            assertEquals("x", r.readLine());
            assertEquals("y", r.readLine());
            assertNull(r.readLine(), "더 읽을 줄이 없으면 null");
        }
    }
}
