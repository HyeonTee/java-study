package study.chapter16;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CountingInputStream — 데코레이터")
class CountingInputStreamTest {

    @Test
    void 단일_read_경로가_카운트된다() throws Exception {
        CountingInputStream in = new CountingInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        in.read();
        in.read();
        assertEquals(2, in.getCount());
    }

    @Test
    void byte배열_read_경로가_카운트된다() throws Exception {
        CountingInputStream in = new CountingInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5}));
        byte[] buf = new byte[10];
        int n = in.read(buf, 0, 10);
        assertEquals(5, n);
        assertEquals(5, in.getCount());
    }

    @Test
    void 위임이_정확하다_읽은_값이_원본과_같다() throws Exception {
        byte[] data = "decorator".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        CountingInputStream in = new CountingInputStream(new ByteArrayInputStream(data));
        // extends InputStream이라 상위 readAllBytes()가 우리 read를 호출 → 위임 정확성 검증
        assertArrayEquals(data, in.readAllBytes());
        assertEquals(data.length, in.getCount());
    }

    @Test
    void EOF_이후_read는_카운트를_올리지_않는다() throws Exception {
        CountingInputStream in = new CountingInputStream(new ByteArrayInputStream(new byte[]{9}));
        assertEquals(9, in.read());
        assertEquals(-1, in.read());   // EOF
        assertEquals(-1, in.read());   // EOF 또
        assertEquals(1, in.getCount(), "EOF는 카운트하지 않는다");
    }

    @Test
    void 빈_스트림은_count0이고_즉시_EOF() throws Exception {
        CountingInputStream in = new CountingInputStream(new ByteArrayInputStream(new byte[0]));
        assertEquals(-1, in.read());
        assertEquals(0, in.getCount());
    }
}
