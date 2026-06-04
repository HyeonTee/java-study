# Chapter 15 — TCP 소켓 (스트림 위 프로토콜 · 라인/길이 프레이밍 · half-close · 소켓 수명주기)

> **선행 단원**: Chapter 14(io-basics — `LineReader` 라인 프레이밍, `FrameCodec` 길이 프레이밍, `Resources.closeAll`·try-with-resources), Chapter 04(검사 예외 — `IOException`). **Phase 3(네트워크)의 첫 단원**: ch14에서 인메모리로 깎은 바이트 파이프·프레이밍·자원 수명이 여기서 **진짜 소켓** 위에 올라선다. 라인 프레이밍은 **ch16 HTTP 요청라인/헤더**로, 길이 프레이밍은 **ch16 `Content-Length`**로 이어진다.

> **공식 문서**: [`Socket`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/Socket.html) · [`ServerSocket`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/ServerSocket.html) · [`InetSocketAddress`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/InetSocketAddress.html) · [`setSoTimeout`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/Socket.html#setSoTimeout(int))

---

## 이 단원의 큰 그림 — "경계 없는 바이트 스트림 한 쌍 + 수명주기"

> TCP는 **양방향 바이트 스트림 한 쌍 + 수명주기**다. ch14의 바이트 파이프가 네트워크 너머로 연결될 뿐, 다루는 스트림은 ch14와 같다.

ch14에서 `ByteArrayInputStream`으로 깎은 **라인/길이 프레이밍·`closeAll`**이 이제 진짜 소켓의 `getInputStream()`/`getOutputStream()` 위에서 그대로 돈다. 그래서 이 단원은 둘로 쪼갠다.

```
[Layer A — 프로토콜]  Socket을 모름. InputStream/OutputStream만 안다.        ← 인메모리, 100% 결정적 (flaky 0)
   LineProtocol · KvCommandHandler · LengthFramedTransport                    ch14처럼 BAIS/BAOS + ChunkedInputStream
        │  (ConnectionHandler 경계)
        ▼
[Layer B — 소켓 배선]  진짜 ServerSocket/Socket. 프로토콜을 소켓에 "꽂기만".    ← loopback :0 + @Timeout + half-close로 길들임
   EchoServer · EchoClient                                                    → ch16 HTTP
```

> **한 줄 슬로건**: 프로토콜은 결정적으로 테스트하고, 소켓은 loopback으로 길들인다 — **분리가 곧 테스트 가능성**이다.

다루지 않는 것(경계): NIO `Selector`·논블로킹·`SocketChannel`·짧은 쓰기(short write)가 보이는 영역(→ ch17), TLS/`SSLSocket`, UDP, 실제 멀티클라이언트 동시성(Phase 2에서 다룸 — 아래 박스 노트로만).

---

## 결정성의 경계 (이 단원의 철학 — 정직한 재정의)

ch14는 "전부 인메모리라 완전 결정적, flaky 없음"을 자랑했다. ch15부터는 **진짜 OS 소켓**이라 본질적으로 타이밍·환경에 의존한다. 우리는 그 비결정성을 **죽이지도, 태그로 숨기지도 않는다 — 길들인다.**

- **프로토콜·프레이밍 로직(Layer A)은 소켓을 import하지 않는다.** `InputStream`/`OutputStream`만 받아 ch14처럼 인메모리로 100% 결정적으로 검증한다(전체 문제의 약 80%).
- **진짜 소켓(Layer B)은 4규율로 길들인다**:
  1. **ephemeral port** — `new ServerSocket()` + `bind(:0)` → OS가 빈 포트 할당(`getLocalPort()`로 회수). 하드코딩 금지 = 포트 충돌 0.
  2. **loopback only** — `InetAddress.getLoopbackAddress()`. 외부 네트워크·DNS 0.
  3. **`@Timeout` + `setSoTimeout` 이중 가드** — 데드락 시 영원히 매달리는 대신 실패한다.
  4. **half-close로 확정 종료** — 클라가 `shutdownOutput()`으로 EOF를 보내 서버 루프를 끝낸다. **임의 `sleep` 절대 금지.**
- `@Tag`/커스텀 Gradle 태스크 같은 별도 인프라는 **두지 않는다**(레포 컨벤션 유지). 소켓 테스트도 기본 `./gradlew :chapter15-tcp-socket:test`에서 함께 초록불이 뜬다.

---

## 소켓의 계약 (가장 많이 틀리는 곳)

- **`accept()`는 블로킹**이다. 닫힌 `ServerSocket`의 블로킹된 `accept()`는 `SocketException`으로 **깨어난다** — 이게 블로킹 서버를 멈추는 정석(인터럽트·플래그 폴링보다).
- **`getInputStream().read()`의 `-1`(EOF)은 "상대가 (출력 방향을) 닫았다"**는 뜻이다. ch14 `LineReader`의 `null`(EOF) 계약이 여기서 half-close로 회수된다.
- **`SocketTimeoutException` ≠ EOF.** `setSoTimeout` 후 read가 타임아웃이면 `SocketTimeoutException`(소켓은 **살아있음**, 데이터가 또 올 수 있어 재시도 가능)이고, EOF는 `-1`(상대가 닫음, 끝)이다. **이 둘을 같게 다루는 게 실무 최대 버그.**
- 닫힌 소켓은 재사용 불가. `close()`는 **멱등**으로 만든다(ch14 `TrackedResource` 계약 재현).

---

## half-close — 이 단원의 가장 중요한 개념

`shutdownOutput()`은 소켓의 **출력 방향만** 닫아 상대에게 **FIN**(="나는 더 안 보냄, 너에겐 EOF")을 통지한다. 소켓 전체는 아직 살아 있어 **이미 보낸 요청의 응답은 받을 수 있다**(half = 반만 닫음).

왜 1급 개념인가:

```
클라:  send("hello") → send("world") → shutdownOutput()      [더 안 보냄 = FIN]
서버:  readLine()="hello" → readLine()="world" → readLine()=null   [EOF! serve 루프 종료]
```

`shutdownOutput()`이 **없으면** 서버의 `readLine()`은 "더 올 줄" 알고 영원히 블로킹한다 — **request/response의 고전 데드락**. half-close가 서버 루프를 **임의 `sleep` 없이 확정적으로** 끝내므로, 데드락의 해법이자 동시에 테스트 flaky 제거 장치다. (`SocketIntegrationTest`가 "half-close가 서버를 종료시킨다"를 `@Timeout`으로 실증한다.)

---

## 두 프레이밍 패러다임 회수 (ch14 → ch15 → ch16)

스트림엔 경계가 없다. TCP도 마찬가지라 `send("a\r\nb")` 한 번이 상대에 `"a\r\n"` + `"b"` 두 read로 쪼개져 오거나, 두 번 보낸 게 한 read로 뭉쳐 올 수 있다. **프레이밍**이 경계를 복원한다.

- **라인 프레이밍**(`LineProtocol` ← ch14 `LineReader`): `\n`까지 모아 한 줄(끝 `\r` 제거, LF·CRLF 흡수). 쓰기 종결자는 **CRLF**(HTTP·SMTP·RESP 표준). `KvCommandHandler`가 이 위에 `SET/GET/DEL` 텍스트 명령을 얹는다(응답 프리픽스 `+`/`$`/`-` = RESP 차용 → ch16 `HTTP/1.1 200 OK` 상태줄의 예고편).
- **길이 프레이밍**(`LengthFramedTransport` ← ch14 `FrameCodec`): `[4바이트 BE 길이][페이로드]`. **ch14와의 차이**: `FrameCodec`은 `ByteBuffer`(완성된 버퍼)를 가정했지만, 실제 소켓엔 완성 버퍼가 없다 — **"정확히 N바이트가 모일 때까지 채워 읽는 루프"**(`readFully`)가 ch15의 새 살이다.

> **박스 노트 — 짧은 쓰기(short write)**: 블로킹 `OutputStream.write(byte[])`는 다 써줄 때까지 블록하므로 "짧은 쓰기"가 안 보인다. 하지만 NIO `SocketChannel.write`에선 실제로 일부만 써진다(→ ch17). 부분 **읽기**(short read)는 `ChunkedInputStream` 픽스처로 소켓 없이 결정적으로 재현한다.

---

## 프로토콜을 전송에서 분리하기 (패턴)

`ConnectionHandler.handle(InputStream, OutputStream)`(완성 제공) 한 줄이 **Layer A ↔ Layer B의 경계**다. `LineProtocol`·`KvCommandHandler`는 `Socket`을 절대 import하지 않으므로, echo·명령 로직을 `ByteArrayInputStream`/`ByteArrayOutputStream`으로 완전히 결정적으로 테스트한다. `ChunkedInputStream`(ch14 `OneByteAtATimeInputStream`의 일반화)으로 TCP 단편화까지 소켓 없이 재현한다.

---

## 수명주기 & 자원 — ch14 `closeAll` 회수

`Socket`·`ServerSocket`·스트림은 모두 `AutoCloseable`이다. ch14에서 만든 LIFO 닫기(`Resources.closeAll`: 역순 close + suppressed)가 "accept한 클라이언트 소켓 → 스트림 → 서버 소켓"을 역순으로 닫는 데 그대로 쓰인다. `EchoServer`/`EchoClient`는 try-with-resources로 쓰고, `close()`는 **멱등**으로 구현한다.

---

## 연습 문제 (Layer A는 결정적 · Layer B는 길들인 통합)

> 권장 순서: **LineProtocol → KvCommandHandler → LengthFramedTransport → EchoServer → EchoClient.**
> (Layer A를 먼저 다 초록불로 만든 뒤, 마지막에 진짜 소켓에 "꽂는다".)

### Layer A — 프로토콜 (소켓 無, 완전 결정적)

| 클래스 | 문제 | 주제 |
|---|---|---|
| `LineProtocol` | 4 | 생성자 / `readLine()`(LF·CRLF·빈 줄·EOF=null) / `writeLine()`(CRLF+flush) / `serve()`(EOF까지 루프) |
| `KvCommandHandler` | 4 | `reply()`(순수 함수 SET/GET/DEL, `+`/`$`/`-`) / `handle()`(LineProtocol 위임) / `get()` / `size()` |
| `LengthFramedTransport` | 3 | `writeFrame()` / `readFully()`(부분읽기 채움, 즉시EOF=null·도중EOF=`EOFException`) / `readFrame()`(음수→`IllegalArgument`) |

### Layer B — 진짜 소켓 (loopback :0 · @Timeout · half-close로 길들임)

| 클래스 | 문제 | 주제 |
|---|---|---|
| `EchoServer` | 4 | 생성자(`ServerSocket`·`:0`·`setReuseAddress`) / `port()` / `serveOne()`(accept·soTimeout·LIFO close) / `close()`(멱등) |
| `EchoClient` | 4 | 생성자(connect+타임아웃) / `send()` / `finish()`(**half-close**) / `close()`(멱등) |

**완성 제공(채우지 않음)**: `ConnectionHandler`(경계 인터페이스), `ChunkedInputStream`(단편화 픽스처).

> **박스 노트 — 멀티클라이언트는?** "연결마다 스레드/가상 스레드"의 동시성 서버는 ch11(Executor)·ch13(가상 스레드)에서 이미 다뤘다. accept 루프 + `pool.submit`은 그 둘을 소켓 위에서 재결합하는 것뿐 — 본 단원은 **프로토콜·수명주기**에 집중하고 동시성 서버는 다루지 않는다(다중 연결 경합은 flaky 온상).

---

## 실행

```sh
# 전체 테스트 (스텁이 비어 있어 빨간불 — 구현하며 초록불로). 소켓 테스트도 함께 포함된다.
./gradlew :chapter15-tcp-socket:test

# 특정 클래스
./gradlew :chapter15-tcp-socket:test --tests "study.chapter15.LineProtocolTest"
./gradlew :chapter15-tcp-socket:test --tests "study.chapter15.SocketIntegrationTest"
```

---

## 다음 단원(ch16 HTTP)으로

HTTP는 **ch15 두 프레이밍의 합성**이다 — 요청 라인과 헤더는 **라인 프레이밍**(`LineProtocol`/`LineReader`, "`\r\n`로 끝나는 헤더 줄 + 빈 줄로 헤더 종료"), 바디는 **길이 프레이밍**(`LengthFramedTransport`/`FrameCodec` = `Content-Length`). `KvCommandHandler`의 응답 프리픽스(`+`/`$`/`-`)와 `reply()` 순수함수 분리가 HTTP 상태줄·헤더 파싱의 토대다. request/response 1왕복과 half-close·소켓 수명주기가 ch16 persistent connection/keep-alive의 씨앗이 된다.
