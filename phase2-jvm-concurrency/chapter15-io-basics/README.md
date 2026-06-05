# Chapter 15 — IO 기초 (스트림 · 데코레이터 · 프레이밍 · NIO ByteBuffer · try-with-resources)

> **선행 단원**: Chapter 04(검사 예외 wrap-and-rethrow — `IOException`), Chapter 01(배열·버퍼 감각). **Phase 2 블록의 마지막 단원**이자 **Phase 3(네트워크)의 토대**: 여기서 깎는 **라인 프레이밍은 ch16 소켓 프로토콜**, **길이 프레이밍은 ch17 HTTP `Content-Length`**, **try-with-resources는 ch16 소켓 수명주기**로 그대로 이어진다.

> **공식 문서**: [`InputStream`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/InputStream.html) · [`ByteBuffer`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/ByteBuffer.html) · [try-with-resources (JLS 14.20.3)](https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.20.3)

---

## 이 단원의 큰 그림 — "데코레이터로 조립한 바이트 파이프"

> java.io/NIO는 **데코레이터로 조립한 바이트 파이프 + try-with-resources로 닫는 자원**이다.

파일·소켓 없이 **인메모리 바이트 배열**(`ByteArrayInputStream`/`ByteArrayOutputStream`)로 그 파이프를 직접 만든다 — 소켓이 없을 뿐 다루는 바이트 파이프라인은 ch16와 동일하다. 전부 인메모리라 **완전 결정적**이다(동시성 단원과 달리 타이밍·flaky 없음).

```
바이트 계약(read()=int 0~255, EOF=-1)
  → copy 루프 → 데코레이터(척추) → 프레이밍(라인/길이) → ByteBuffer → try-with-resources
                                       │                │                     │
                                   ch16 소켓        ch17 HTTP             ch16 소켓 close
```

다루지 않는 것(경계): 실제 파일시스템(`Files`/`Path`), Selector·논블로킹 IO(→ ch16/ch18), 객체 직렬화(`Serializable`), 압축(GZIP), mmap.

---

## 바이트 스트림의 계약 (가장 많이 틀리는 곳)

- **`int read()`**: 다음 한 바이트를 **0~255의 int**로 반환, **EOF는 -1**. `byte`로 캐스팅해 EOF를 비교하면 안 된다 — `0xFF` 바이트(255)가 `-1`로 보여 EOF와 충돌한다.
- **`int read(byte[] b, int off, int len)`**: **실제 읽은 바이트 수**를 반환(요청 `len`보다 적을 수 있다), EOF는 -1. "배열을 끝까지 채워준다"는 오해 — 한 번에 일부만 읽힐 수 있어 **루프 필수**.
- **copy 루프**: `while ((n = in.read(buf)) != -1) out.write(buf, 0, n);` — 반드시 `write(buf, 0, n)`(읽은 만큼만), `write(buf)` 아님.
- **flush/close**: 버퍼드 출력은 `flush()` 전엔 안 나간다. `close()`는 보통 flush를 포함하고, try-with-resources가 보장한다.
- **Reader/Writer vs Stream**: Stream=바이트, Reader/Writer=문자. 변환은 **Charset** — 항상 `StandardCharsets.UTF_8` 명시(기본 인코딩 의존 금지). 한 char ≠ 한 byte(한글은 UTF-8 3바이트).

---

## 데코레이터 패턴 (척추) — java.io의 설계를 손에 넣기

`new BufferedReader(new InputStreamReader(new FileInputStream(...)))`이 "양파"인 이유가 **데코레이터**다 — 다른 스트림을 **감싸** 동작을 더한다(`FilterInputStream`이 베이스, Buffered/GZIP 등 전부 이 패턴). `CountingInputStream`을 `InputStream` 상속으로 직접 만들며 그 구조를 체득한다. 핵심: 모든 읽기 경로를 `delegate`에 위임하고 실제 읽힌 만큼만 처리. `extends InputStream`이라 상위의 `read(byte[])`·`readAllBytes()`가 우리가 오버라이드한 메서드를 호출해 **공짜로** 동작한다(위임의 묘미).

---

## 프레이밍 — 스트림에 경계를 만든다

스트림엔 경계가 없다. 두 가지 프레이밍 패러다임:

- **라인 프레이밍**(`LineReader`, → ch16): 구분자 `\n`(앞의 `\r`은 CRLF로 제거)까지 모아 한 줄. 단독 `\r`은 구분자 아님. 마지막 줄은 종결자 없이 EOF로 끝나도 반환. 빈 줄("")과 끝(null)을 구분.
- **길이 프리픽스 프레이밍**(`FrameCodec`, → ch17): 먼저 **4바이트 빅엔디안 int 길이**를 쓰고 그만큼 페이로드. `Content-Length`가 정확히 이것이다.

---

## NIO ByteBuffer — "흐름"이 아니라 "창(window)"

고정 capacity 버퍼 + 커서 3개. 불변식 **0 ≤ position ≤ limit ≤ capacity**.

- `put`/`putInt`은 position 전진(쓰기).
- **`flip()`**: limit←position, position←0 (쓰기→읽기 전환). **대표 함정**: flip 안 하면 position이 데이터 끝이라 `get`이 빈 영역을 읽는다.
- `clear()`(전체 재사용, 데이터는 안 지움), `compact()`(안 읽은 잔량 앞으로), `remaining()`=limit−position. 기본 바이트 순서 BIG_ENDIAN.

---

## try-with-resources / AutoCloseable (1급 주제 — 감사 격상)

`try (R r = ...) {}`가 컴파일러 뒤에서 하는 일:
- 블록 종료 시 자동 `close()`, 여러 자원은 **선언 역순(LIFO)**으로 닫는다.
- try 본문 예외가 **주 예외**, 그때 `close`가 또 던진 예외는 **suppressed**로 붙는다(`getSuppressed()`). 본문이 정상이면 close 예외가 주 예외.
- `close()`는 **멱등** 권장(두 번 닫아도 안전).

`Resources.closeAll(...)`로 이 LIFO·suppressed 로직을 직접 구현한다. ch16 소켓 수명주기가 이 위에 선다.

> ch04와의 선: ch04는 "검사 예외를 비검사로 wrap하는 어댑터"가 주제. ch15는 "검사 예외를 try-with-resources/suppressed로 다루는 자원 수명"이 주제다.

---

## 연습 문제 (전부 인메모리·결정적)

> 권장 순서: **StreamUtils → CountingInputStream → LineReader → FrameCodec → Resources.**

### StreamUtils (2문제) — copy / readAllBytes
`copy`(버퍼 루프·EOF, 복사 수 반환), `readAllBytes`. 부분 읽기(`OneByteAtATimeInputStream` 픽스처)와 버퍼 경계(70KB)로 검증.

### CountingInputStream (5문제) — 데코레이터 · 척추
생성자/`read()`/`read(byte[],off,len)`/`close()`/`getCount()`. EOF는 카운트 안 함, 위임 정확성.

### LineReader (3문제) — 라인 프레이밍 · ch16 직결
생성자/`readLine()`(null 종료)/`readLines()`. LF·CRLF·빈 줄·UTF-8·마지막 줄.

### FrameCodec (2문제) — 길이 프레이밍 · ch17 직결
`encode`(빅엔디안 길이+페이로드)/`decode`(flip된 버퍼에서 한 프레임, 연속 프레임, 불완전→`BufferUnderflowException`, 음수→`IllegalArgumentException`).

### Resources (6문제) — try-with-resources · 감사 격상
`closeAll`(LIFO + suppressed + null skip), `TrackedResource`(name/use/isClosed/close, 멱등·닫힌 뒤 use 예외).

---

## 실행

```sh
# 전체 테스트 (스텁이 비어 있어 빨간불 — 구현하며 초록불로)
./gradlew :chapter15-io-basics:test

# 특정 클래스
./gradlew :chapter15-io-basics:test --tests "study.chapter15.LineReaderTest"
```
