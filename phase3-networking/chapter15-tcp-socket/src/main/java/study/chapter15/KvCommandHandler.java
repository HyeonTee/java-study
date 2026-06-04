package study.chapter15;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

/**
 * 라인 기반 텍스트 <strong>명령 프로토콜</strong>(SET/GET/DEL) — Redis RESP·SMTP 풍의 미니 와이어 프로토콜.
 * 이 단원의 두 번째 간판이자 <strong>ch16 HTTP 상태줄·헤더의 예고편</strong>.
 *
 * <p>{@link LineProtocol} 위에 얹는 순수 프로토콜이라 소켓을 모른다 — 인메모리 {@code Map} 상태로 완전 결정적.
 *
 * <p><strong>와이어 문법</strong>(요청·응답 모두 한 줄, 종결자 CRLF는 {@link LineProtocol}이 붙인다):
 * <pre>
 *   SET &lt;key&gt; &lt;value&gt;   →  +OK
 *   GET &lt;key&gt;           →  $&lt;value&gt;            (없으면)  -ERR no such key
 *   DEL &lt;key&gt;           →  +OK                   (없으면)  -ERR no such key
 *   (알 수 없는 명령)         →  -ERR unknown command
 * </pre>
 * 응답 <strong>프리픽스로 종류를 구분</strong>한다: {@code +}=상태, {@code $}=값, {@code -}=에러 (RESP 차용).
 * 이 "프리픽스로 메시지 종류 구분"이 ch16 {@code HTTP/1.1 200 OK} 상태줄로 이어진다.
 *
 * <p><strong>파싱 함정</strong>: 명령을 {@code split(" ", 3)}로 3조각(명령/키/값)까지만 나눠라 — value에 공백을
 * 허용해야 {@code SET k a b c}의 값이 {@code "a b c"}가 된다. 무한정 split하면 value의 공백에서 깨진다.
 */
public final class KvCommandHandler implements ConnectionHandler {

    /**
     * 한 줄 명령을 받아 한 줄 응답을 돌려주는 <strong>순수 함수</strong>(소켓·스트림 무관 → 결정적 단위 테스트의 핵심).
     * 인메모리 상태를 변경한다. 반환 문자열에 줄 종결자(CRLF)는 붙이지 않는다({@link LineProtocol#writeLine}이 붙인다).
     *
     * <p>힌트: {@code split(" ", 3)}로 분리. SET이면 저장 후 {@code "+OK"}, GET이면 있으면 {@code "$"+값} 없으면
     * {@code "-ERR no such key"}, DEL이면 지웠으면 {@code "+OK"} 없으면 {@code "-ERR no such key"}, 그 외
     * {@code "-ERR unknown command"}.
     */
    public String reply(String command) {
        throw new UnsupportedOperationException(
                "TODO: split(\" \", 3)로 명령/키/값 분리(value의 공백 허용). +/$/- 프리픽스로 응답. 종결자는 붙이지 않는다");
    }

    /**
     * 한 연결의 전체 대화를 처리한다 — {@code in}의 EOF(상대 half-close)까지 한 줄씩 읽어 {@link #reply(String)}
     * 결과를 한 줄로 응답한다.
     *
     * <p>힌트: {@code new LineProtocol(in, out).serve(this::reply)} 한 줄이면 된다 — 로직은 {@code reply}에,
     * {@code handle}은 얇은 위임 루프.
     */
    @Override
    public void handle(InputStream in, OutputStream out) throws IOException {
        throw new UnsupportedOperationException(
                "TODO: LineProtocol으로 in/out을 감싸 serve(this::reply). 로직은 reply에, handle은 얇은 위임");
    }

    /**
     * 현재 인메모리 상태에서 {@code key}의 값을 조회한다(테스트가 상태를 직접 검증하는 접근자). 없으면 빈 Optional.
     */
    public Optional<String> get(String key) {
        throw new UnsupportedOperationException("TODO: 내부 Map에서 조회해 Optional로 반환");
    }

    /**
     * 현재 저장된 키 개수(SET으로 늘고 DEL로 준다 — 테스트가 효과를 결정적으로 확인하는 용도).
     */
    public int size() {
        throw new UnsupportedOperationException("TODO: 내부 Map의 크기 반환");
    }
}
