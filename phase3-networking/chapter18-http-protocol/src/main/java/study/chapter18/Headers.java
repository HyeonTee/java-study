package study.chapter18;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * <strong>대소문자 무시</strong> HTTP 헤더 맵 — 이 단원의 핵심 교육 페이로드(ch02 HashMap 함정 회수 + ch05 Optional).
 *
 * <p><strong>가장 흔한 실무 버그</strong>: HTTP 헤더 이름은 case-insensitive다. {@code Content-Length}와
 * {@code content-length}는 <strong>같은 헤더</strong>다. "그냥 {@code HashMap<String,String>}에 넣으면 끝"이라는
 * ch02 직관이 여기서 깨진다 — 키를 {@code toLowerCase(Locale.ROOT)}로 <strong>정규화</strong>해 저장·조회해야 한다.
 * ({@code Locale.ROOT}가 중요 — 기본 로케일이 터키어면 {@code "I".toLowerCase()}가 점 없는 ı가 되어 깨진다.)
 *
 * <p><strong>불변</strong>: {@link #with}는 원본을 바꾸지 않고 새 인스턴스를 반환한다(빌더 감각).
 *
 * <p>설계 메모(경계): 같은 이름 헤더의 <strong>반복(멀티값)</strong>·<strong>원본 대소문자 표기 보존</strong>은 실무에선
 * 필요하지만(예: 여러 {@code Set-Cookie}), 이 단원의 1왕복 서사엔 실사용 지점이 없어 단일값으로 단순화한다(직렬화도
 * 정규화 키로). 멀티값은 ch19 이후의 박스 주제.
 *
 * <p>내부 저장 맵(정규화 키 → 값)과 생성자는 뼈대로 제공된다. 채우는 것은 {@code with}/{@code get}/
 * {@code contentLength}/{@code isChunked}/{@code writeTo}의 로직이다.
 */
public final class Headers {

    /** 정규화된 키(lowercase) → 값. 직렬화 순서를 위해 삽입 순서 보존. */
    private final Map<String, String> values;

    /** 빈 헤더로 시작한다. */
    public Headers() {
        this(new LinkedHashMap<>());
    }

    private Headers(Map<String, String> values) {
        this.values = values;
    }

    /**
     * 이름/값을 추가(같은 이름은 덮어쓰기)한 <strong>새 Headers</strong>를 반환한다 — 원본은 그대로(불변).
     *
     * <p>힌트: 원본 {@code values}를 복사한 새 맵에 {@code name}을 {@code toLowerCase(Locale.ROOT)}로 정규화해
     * 넣고, 그 맵으로 {@code new Headers(...)}를 만들어 반환하라(private 생성자 사용). 원본 맵을 변형하지 말 것.
     *
     * @throws NullPointerException name/value가 null이면
     */
    public Headers with(String name, String value) {
        throw new UnsupportedOperationException(
                "TODO: 원본 복사한 새 맵에 정규화 키로 put 후 new Headers(map) 반환(불변). 키는 toLowerCase(Locale.ROOT)");
    }

    /**
     * 대소문자 무시 조회. 없으면 {@link Optional#empty()}("없음"과 "빈 값"은 다르다, ch05 회수).
     *
     * <p>힌트: {@code name}을 정규화해 맵에서 찾는다.
     */
    public Optional<String> get(String name) {
        throw new UnsupportedOperationException(
                "TODO: name을 toLowerCase(Locale.ROOT)로 정규화해 조회, Optional.ofNullable로 반환");
    }

    /**
     * {@code Content-Length}를 {@code long}으로 파싱. 없으면 {@link OptionalLong#empty()}.
     *
     * <p>힌트: {@code get("content-length")} 후 값을 {@code trim()}하고 {@code Long.parseLong}. 없으면 empty,
     * 음수이거나 숫자가 아니면 {@link HttpProtocolException}. {@code int}가 아니라 {@code long}인 이유는 바디가 2GB를
     * 넘을 수 있어서다(실제 {@code readFully(int)} 호출 시 범위 검증은 {@link HttpParser} 책임).
     *
     * @throws HttpProtocolException 값이 음수이거나 정수가 아니면
     */
    public OptionalLong contentLength() throws HttpProtocolException {
        throw new UnsupportedOperationException(
                "TODO: get(\"content-length\")→trim→Long.parseLong. 없으면 empty, 음수/비정수면 HttpProtocolException");
    }

    /**
     * {@code Transfer-Encoding} 값에 {@code chunked}가 (대소문자 무시) 포함되면 {@code true}.
     *
     * <p>힌트: {@code get("transfer-encoding")} 후 소문자로 보고 {@code "chunked"} 포함 여부.
     */
    public boolean isChunked() {
        throw new UnsupportedOperationException(
                "TODO: get(\"transfer-encoding\")를 소문자화해 \"chunked\" 포함 판정(없으면 false)");
    }

    /**
     * 각 헤더를 {@code name: value\r\n}(<strong>CRLF</strong> = ch17 종결자)로 삽입 순서대로 직렬화한다.
     * 헤더 종료를 알리는 빈 줄은 여기서 쓰지 않는다({@link HttpMessageWriter} 책임) — <strong>헤더 줄만</strong>.
     *
     * <p>힌트: 맵 엔트리마다 {@code key + ": " + value + "\r\n"}을 UTF-8로 write. (키는 정규화된 표기 그대로.)
     *
     * @throws IOException I/O 오류
     */
    public void writeTo(OutputStream out) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: 각 엔트리를 \"name: value\\r\\n\"(CRLF)로 UTF-8 write. 빈 줄은 쓰지 않는다(Writer 책임)");
    }
}
