package study.chapter15;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("FrameCodec — 길이 프리픽스 프레이밍 (ch17 HTTP 직결)")
class FrameCodecTest {

    @Nested
    @DisplayName("encode")
    class Encode {

        @Test
        void 길이_프리픽스가_빅엔디안_4바이트() {
            byte[] frame = FrameCodec.encode(new byte[]{7, 8});   // 길이 2
            assertArrayEquals(new byte[]{0, 0, 0, 2, 7, 8}, frame);
        }

        @Test
        void 길이_256은_빅엔디안에서_상위바이트() {
            byte[] frame = FrameCodec.encode(new byte[256]);
            // 256 = 0x00000100 → 빅엔디안 {0,0,1,0} (리틀엔디안이면 {0,1,0,0})
            assertEquals(0, frame[0]);
            assertEquals(0, frame[1]);
            assertEquals(1, frame[2]);
            assertEquals(0, frame[3]);
        }

        @Test
        void 빈_페이로드는_길이0_프리픽스만() {
            assertArrayEquals(new byte[]{0, 0, 0, 0}, FrameCodec.encode(new byte[0]));
        }
    }

    @Nested
    @DisplayName("decode 라운드트립")
    class RoundTrip {

        @Test
        void encode_후_decode하면_원본() {
            byte[] payload = "hello frame".getBytes(StandardCharsets.UTF_8);
            byte[] decoded = FrameCodec.decode(ByteBuffer.wrap(FrameCodec.encode(payload)));
            assertArrayEquals(payload, decoded);
        }

        @Test
        void 빈_페이로드_라운드트립() {
            assertArrayEquals(new byte[0], FrameCodec.decode(ByteBuffer.wrap(FrameCodec.encode(new byte[0]))));
        }

        @Test
        @DisplayName("한 버퍼에 연속된 두 프레임을 이어서 디코딩")
        void 연속_프레임() {
            byte[] a = FrameCodec.encode("AAA".getBytes(StandardCharsets.UTF_8));
            byte[] b = FrameCodec.encode("BB".getBytes(StandardCharsets.UTF_8));
            ByteBuffer buf = ByteBuffer.allocate(a.length + b.length);
            buf.put(a).put(b).flip();
            assertArrayEquals("AAA".getBytes(StandardCharsets.UTF_8), FrameCodec.decode(buf));
            assertArrayEquals("BB".getBytes(StandardCharsets.UTF_8), FrameCodec.decode(buf));
            assertEquals(0, buf.remaining(), "두 프레임을 모두 소비");
        }
    }

    @Nested
    @DisplayName("경계 / 방어")
    class Edge {

        @Test
        void 불완전_프레임은_BufferUnderflow() {
            // 길이는 5라고 했는데 페이로드가 3바이트뿐
            ByteBuffer buf = ByteBuffer.allocate(4 + 3);
            buf.putInt(5).put(new byte[3]).flip();
            assertThrows(BufferUnderflowException.class, () -> FrameCodec.decode(buf));
        }

        @Test
        void 음수_길이는_IllegalArgument() {
            ByteBuffer buf = ByteBuffer.allocate(4);
            buf.putInt(-1).flip();
            assertThrows(IllegalArgumentException.class, () -> FrameCodec.decode(buf));
        }
    }
}
