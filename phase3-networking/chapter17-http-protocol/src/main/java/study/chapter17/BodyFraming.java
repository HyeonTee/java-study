package study.chapter17;

/**
 * 바디를 어떻게 프레이밍할지 결정하는 전략 — ch06 <strong>sealed + pattern matching</strong>의 정확한 사용처.
 *
 * <p>HTTP 바디는 세 가지 방식으로 경계가 정해진다:
 * <ul>
 *   <li>{@link ContentLength} — 길이 프레이밍(ch16 {@code readFully}). 바디가 정확히 N바이트.</li>
 *   <li>{@link Chunked} — chunked transfer-encoding(길이를 미리 모를 때, → {@link ChunkedBody}).</li>
 *   <li>{@link None} — 바디 없음(GET 요청, 204 응답 등).</li>
 * </ul>
 *
 * <p>sealed/record 정의는 <strong>완성 제공</strong>이다. 채우는 것은 {@link #from(Headers)} 하나 — 거기서
 * "어느 헤더가 프레이밍을 결정하는가"(RFC 9112 우선순위)를 익힌다. <strong>바디 유무는 메서드가 아니라 헤더가 결정한다.</strong>
 */
public sealed interface BodyFraming permits BodyFraming.ContentLength, BodyFraming.Chunked, BodyFraming.None {

    /** {@code Content-Length: N} — 정확히 {@code length}바이트(ch16 길이 프레이밍). */
    record ContentLength(long length) implements BodyFraming {
        public ContentLength {
            if (length < 0) {
                throw new IllegalArgumentException("Content-Length는 음수일 수 없다: " + length);
            }
        }
    }

    /** {@code Transfer-Encoding: chunked} — 길이를 미리 모를 때(→ {@link ChunkedBody#decode}). */
    record Chunked() implements BodyFraming {
    }

    /** 바디 없음. */
    record None() implements BodyFraming {
    }

    /**
     * 헤더로부터 바디 프레이밍을 결정한다 — <strong>이 단원에서 유일하게 채우는 모델 메서드</strong>.
     *
     * <p><strong>우선순위(RFC 9112 §6.1)</strong>: {@code Transfer-Encoding: chunked}가 있으면 {@link Chunked}
     * ({@code Content-Length}는 <strong>무시</strong>) → 없고 {@code Content-Length}가 있으면 {@link ContentLength}
     * → 둘 다 없으면 {@link None}.
     *
     * <p>힌트: {@code headers.isChunked()}면 {@code new Chunked()}, 아니면 {@code headers.contentLength()}가
     * 있으면 {@code new ContentLength(값)}, 없으면 {@code new None()}. (둘 다 오면 chunked 우선 — 실무는 request
     * smuggling 위험으로 거부하지만, 학습은 스펙대로 chunked 우선. README 박스 참조.)
     */
    static BodyFraming from(Headers headers) {
        throw new UnsupportedOperationException(
                "TODO: isChunked()면 Chunked, 아니면 contentLength() 있으면 ContentLength, 없으면 None. chunked가 Content-Length를 이긴다");
    }
}
