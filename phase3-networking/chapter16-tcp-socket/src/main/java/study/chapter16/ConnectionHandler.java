package study.chapter16;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 한 연결의 <strong>대화 전체</strong>를 처리하는 경계 인터페이스 — 이 단원에서 가장 중요한 추상화.
 *
 * <p>이 시그니처가 단원을 두 층으로 가른다:
 * <ul>
 *   <li><strong>Layer A — 프로토콜</strong>: {@code InputStream}/{@code OutputStream}만 알고 {@code java.net.Socket}은
 *       <strong>모른다</strong>. 그래서 ch15처럼 {@code ByteArrayInputStream}으로 100% 결정적으로 테스트된다.</li>
 *   <li><strong>Layer B — 소켓 배선</strong>: 진짜 소켓에서 얻은 스트림을 이 핸들러에 <strong>꽂기만</strong> 한다.</li>
 * </ul>
 *
 * <p>계약: {@code in}의 EOF(상대의 half-close)까지 읽고 {@code out}으로 응답한다. 스트림·소켓의 {@code close}는
 * <strong>호출자(소켓 측)의 책임</strong>이다(핸들러는 닫지 않는다).
 *
 * <p>이 인터페이스는 <strong>계약 그 자체</strong>라 완성본으로 제공된다(채울 스텁이 아니다). ch15
 * {@code Resources.closeAll}이 자원 수명의 경계였듯, 여기서는 이 인터페이스가 "프로토콜 ↔ 전송"의 경계다.
 */
@FunctionalInterface
public interface ConnectionHandler {

    /**
     * 한 연결의 입력 스트림을 EOF까지 읽으며 출력 스트림으로 응답한다.
     *
     * @param in  연결의 입력(상대가 보낸 바이트). EOF(-1)는 상대의 half-close/close를 뜻한다.
     * @param out 연결의 출력(상대에게 보낼 바이트). 응답마다 flush를 잊지 말 것.
     * @throws IOException I/O 오류
     */
    void handle(InputStream in, OutputStream out) throws IOException;
}
