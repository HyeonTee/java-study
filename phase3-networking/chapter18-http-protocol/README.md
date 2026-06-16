# Chapter 18 — HTTP 프로토콜 (두 프레이밍의 합성 · 메시지 파싱/직렬화 · chunked · 미니 클라이언트)

> **선행 단원**: Chapter 17(tcp-socket — `LineProtocol.readLine` 라인 프레이밍, `LengthFramedTransport.readFully`/Content-Length 길이 프레이밍, `ConnectionHandler` 경계, `ChunkedInputStream` 픽스처, CRLF·half-close), Chapter 16(`LineReader`·`FrameCodec`), Chapter 06(record/sealed/pattern matching), Chapter 05(Optional/OptionalLong), Chapter 02(HashMap의 case-insensitive 함정), Chapter 04(검사 예외 wrap). **Phase 3의 두 번째 단원** — ch17가 깐 "HTTP는 두 프레이밍의 합성" 떡밥을 코드로 회수한다.

> **공식 문서**: [RFC 9110 (HTTP Semantics)](https://www.rfc-editor.org/rfc/rfc9110) · [RFC 9112 (HTTP/1.1)](https://www.rfc-editor.org/rfc/rfc9112) · [`java.net.URI`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/URI.html)

---

## 이 단원의 큰 그림 — "HTTP = 라인 프레이밍 ∘ 길이 프레이밍"

> HTTP/1.1 메시지는 ch17에서 따로 배운 **두 프레이밍의 합성**이다. chunked는 그 세 번째 변주일 뿐이다.

```
요청라인 / 상태줄   ─┐
헤더들             ─┤→  라인 프레이밍   (ch17 LineProtocol.readLine: 한 바이트씩 \n까지, 끝 \r 제거, UTF-8)
빈 줄(헤더 종료)    ─┘    "" 한 줄이 헤더의 끝
─────────────────────────────────────────────────────────
바디               ──→  길이 프레이밍   (ch17 LengthFramedTransport.readFully = Content-Length)
                        또는 chunked    (세 번째 변주: hex 길이 + CRLF, → ChunkedBody)
```

ch17가 만든 `readLine`(라인)과 `readFully`(길이)를 **그대로** 빌딩블록으로 쓴다(import 대신 같은 계약을 ch18에 재서술 — 풀이 복붙 허용). 그 위에:

```
[Layer A] 메시지 파싱/직렬화 (Socket 모름)  → 인메모리 BAIS/BAOS, 100% 결정적 (flaky 0)
   Headers · HttpParser · HttpMessageWriter · ChunkedBody · BodyFraming
        │  (record/sealed 모델은 완성 제공)
        ▼
[Layer B] 미니 클라이언트 (진짜 소켓)        → loopback + @Timeout + half-close (ch17 4규율, 1왕복만)
   MiniHttpClient
```

> **한 줄 슬로건**: HTTP = 라인 프레이밍 ∘ 길이 프레이밍, chunked는 그 세 번째 변주.

**다루지 않는 것(경계)**: 동시성·keep-alive 연결 루프·connection-close(EOF까지) 프레이밍·DoS 제한 enforce → **ch19**. 라우팅(@Route)·미들웨어·JSON 직렬화 → **ch20**. TLS/HTTPS·HTTP/2·WebSocket·쿠키/세션·gzip·chunked 인코더/trailer → 이론 박스로만.

---

## HTTP/1.1 메시지 해부

```
요청:  METHOD SP target SP HTTP-version CRLF      ← 요청라인 (예: GET /index.html HTTP/1.1)
       field-name ":" OWS field-value CRLF        ← 헤더 0개 이상
       ...
       CRLF                                        ← 빈 줄 = 헤더 종료
       [ message-body ]                            ← 바디 (Content-Length 또는 chunked)

응답:  HTTP-version SP status-code SP reason CRLF  ← 상태줄 (예: HTTP/1.1 200 OK)
       ... (헤더, 빈 줄, 바디 동일)
```

메시지 모델은 `record`로 **완성 제공**한다(`RequestLine`·`StatusLine`·`HttpRequest`·`HttpResponse`) — record 보일러플레이트 채우기는 학습 가치가 낮으므로 파싱에 집중. 학습자가 채우는 모델 로직은 `BodyFraming.from` 하나뿐이다.

> **method가 enum이 아니라 String인 이유**: 진짜 파서는 미지의 메서드(`PATCH`·`PROPFIND`·커스텀)도 거르지 않고 **보존**한다. "허용 안 된 메서드 405"는 *응답 단계*(ch19)의 결정이다. enum + `valueOf`로 닫으면 파싱이 "검열기"가 되어 malformed 예외 경로와 충돌한다.

---

## 세(두+한) 가지 바디 프레이밍 — `BodyFraming` (sealed + pattern matching)

바디 유무·길이는 **메서드가 아니라 헤더가 결정한다.** ch06 sealed의 정확한 사용처:

- **`ContentLength(long)`** — 길이 프레이밍. ch17 `readFully`로 정확히 N바이트(이 단원의 핵심 회수).
- **`Chunked`** — 길이를 미리 모를 때(스트리밍 생성). `ChunkedBody.decode`로 디코드.
- **`None`** — 바디 없음(GET, 204 등).

**우선순위(RFC 9112 §6.3)**: `Transfer-Encoding: chunked`가 있으면 `Content-Length`는 **무시**된다(chunked 우선). 둘 다 오면 실무는 **request smuggling** 위험으로 거부하지만, 학습은 스펙대로 chunked 우선. `Content-Length`도 `Transfer-Encoding`도 없는 응답의 "**EOF까지 읽기(connection-close)**" 프레이밍은 **ch19 경계**다.

`BodyFraming.from(Headers)`가 이 규칙을 박는 유일한 스텁이고, `HttpParser.parse`가 그 결과를 `switch`(pattern matching)로 분기한다.

---

## HTTP 실무 함정 (가장 많이 틀리는 곳)

1. **헤더는 case-insensitive** — `Content-Length` = `content-length`. ch02의 "그냥 HashMap" 직관이 깨진다. 키를 `toLowerCase(Locale.ROOT)`로 정규화하라(`Locale.ROOT` 누락 시 터키어 로케일에서 `"I"` → 점 없는 `ı`로 깨짐).
2. **`readLine`은 반드시 한 바이트씩(`read()`)** — `read(buf)` 블록 읽기로 바꾸면 줄 경계를 넘어 **바디 첫 바이트까지 삼킨다**(헤더 직후 바로 바디가 붙으므로 실재 버그). `ChunkedInputStream` 단편화 테스트가 이를 잡는다.
3. **라인은 UTF-8 디코딩 후 String에서 split** — 바이트에서 `:`/공백을 찾아 자르면 멀티바이트 헤더값(한글 등)이 깨질 수 있다(콜론·공백은 ASCII라 우연히 안전할 뿐 — 습관을 바로).
4. **Content-Length는 `long`, `readFully`는 `int`** — long→int 좁힘에서 silent overflow가 난다. int 범위 초과면 명시적으로 `HttpProtocolException`(인메모리 단원은 int 범위만; 스트리밍은 ch19).
5. **빈 reason-phrase는 합법** — `HTTP/1.1 204 `(trailing space). `split(" ", 3)` 후 토큰이 3 미만이면 reason을 `""`로 보정(AIOOBE 금지).
6. **malformed는 `HttpProtocolException`(extends IOException)으로 일원화** — 즉시 EOF·헤더 잘림·바디 잘림·hex 아닌 청크가 전부 한 예외. ch17 `readFully`의 `EOFException`(잘린 바디)을 여기로 wrap(ch04 검사 예외 회수). "진짜 I/O 오류"와 "문법 오류"를 구분해 호출자가 400으로 매핑 가능.
7. **라인 길이/헤더 수 제한(DoS 방어)** — 실제 enforce는 **ch19**. 여기선 박스 노트로만.

---

## chunked transfer-encoding (디코더만)

```
<hex-size>[;ext]CRLF     ← 청크 크기(16진수). ';' 뒤 extension은 버린다
<size 바이트>CRLF         ← 데이터 + 뒤따르는 CRLF(이 CRLF는 크기에 미포함!)
...
0CRLF                    ← last-chunk
[trailer CRLF]*          ← 선택적 트레일러
CRLF                     ← 빈 줄로 종료
```

- 크기는 **`Long.parseLong(s, 16)`** — `Integer.parseInt(s, 16)`은 `"80000000"`(= 0x80000000, `Integer.MAX_VALUE` 초과)에서 `NumberFormatException`(함정). 16진수는 대소문자 무관.
- `0` 청크 뒤엔 **`readLine`이 빈 줄(`""`)을 반환할 때까지** 트레일러를 흡수(단일 CRLF 하나만 읽으면 트레일러 있는 스트림에서 깨짐).
- 인코더·trailer 생성·chunk-extension 파싱은 **다루지 않는다**(이론).

---

## 파싱을 전송에서 분리하기 / 클라이언트↔서버 대칭

`HttpParser`·`HttpMessageWriter`는 `Socket`을 import하지 않는다(ch17 `ConnectionHandler` 경계 계승) — 그래서 `ByteArrayInputStream`/`ByteArrayOutputStream`으로 100% 결정적으로 테스트된다.

**클라이언트와 서버는 같은 코드, 방향만 반대다**(ch17 `EchoClient`↔`EchoServer` 회수):
- 서버 = 요청 **파싱**(`HttpParser.parse`) + 응답 **직렬화**(`HttpMessageWriter.writeResponse`)
- 클라이언트 = 요청 **직렬화**(`writeRequest`) + 응답 **파싱**(`parseResponse`)

`MiniHttpClient.exchange`는 ch17 4규율로 실소켓 **1왕복**만 한다(연결 후 `shutdownOutput` half-close → 응답 파싱 → close). keep-alive 연결 루프는 ch19.

---

## 결정성의 경계

- 파싱/직렬화 전부 **in-memory**(요청 바이트를 문자열 리터럴로 박아 BAIS에 먹임) — flaky 0, ch16·ch17 그대로.
- 단편화는 **`ChunkedInputStream`(ch17 복사)** 로 재현 — 소켓 없이 TCP 단편화 결정적 검증.
- **round-trip이 핵심 무기**: `write → parse` 결과가 원본과 동등(단 `byte[]` 바디는 `Arrays.equals`, 헤더는 정규화 동등성으로 — record 기본 `equals`는 배열을 참조 비교하므로 직접 비교 금지).
- 실소켓은 `HttpRoundTripTest`의 1왕복만(ch17 4규율: `ServerSocket(:0)` + `@Timeout` + 클라 half-close). `@Tag`·커스텀 테스트 태스크 없음(빈 `build.gradle` 상속).

---

## 연습 문제

> 권장 순서: **Headers → BodyFraming.from → ChunkedBody → HttpParser → HttpMessageWriter → MiniHttpClient.**
> (헤더 맵·프레이밍 결정 → chunked 디코더 → 파서 → 직렬화 → 실소켓 클라이언트)

### 완성 제공(채우지 않음)
`RequestLine`·`StatusLine`·`HttpRequest`·`HttpResponse`(record 모델), `BodyFraming`의 sealed/record 정의, `HttpProtocolException`, 테스트 픽스처 `ChunkedInputStream`.

### 채우는 클래스

| 클래스 | 문제 | 주제 |
|---|---|---|
| `Headers` | 5 | `with`(불변·키 정규화) / `get`(case-insensitive·Optional) / `contentLength`(OptionalLong·음수·비정수) / `isChunked` / `writeTo`(CRLF) |
| `BodyFraming.from` | 1 | 헤더 우선순위(chunked > Content-Length > None) |
| `ChunkedBody.decode` | 1 | hex 크기·CRLF 흡수·트레일러·malformed |
| `HttpParser` | 7 | `readLine` / `readFully`(ch17 회수) / `readHeaders` / `parseRequestLine` / `parseStatusLine` / `parse` / `parseResponse` |
| `HttpMessageWriter` | 2 | `writeRequest` / `writeResponse` (CRLF·flush) |
| `MiniHttpClient` | 2 | 생성자 / `exchange`(실소켓 1왕복·half-close) |

---

## 실행

```sh
# 전체 테스트 (스텁이 비어 있어 빨간불 — 구현하며 초록불로). 실소켓 round-trip도 함께 포함된다.
./gradlew :chapter18-http-protocol:test

# 특정 클래스
./gradlew :chapter18-http-protocol:test --tests "study.chapter18.HttpParserTest"
./gradlew :chapter18-http-protocol:test --tests "study.chapter18.HttpRoundTripTest"
```

---

## 다음 단원(ch19 concurrent-http-server)으로

이 단원의 **단일 요청/응답 1왕복 핸들러**(`HttpParser.parse` → `HttpMessageWriter.writeResponse`)를 `accept` 루프 + 연결당 스레드 → 스레드풀(ch13) → 가상스레드(ch15)에 꽂는다. **keep-alive 연결 루프**(한 소켓에 요청 N개), `Connection: close`/`keep-alive` 협상, **connection-close(EOF까지) 프레이밍**, 라인 길이/헤더 수 제한 **enforce**(DoS 방어), 부하테스트가 전부 ch19. **ch20**: 라우팅(@Route 리플렉션 — ch10 회수)·미들웨어·JSON 직렬화를 이 단원의 메시지 모델 위에 얹는다.
