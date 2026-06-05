# Chapter 19 — 동시성 HTTP 서버 (keep-alive 연결 루프 × 교체 가능한 Executor · DoS 방어 · 4 스레딩 모델)

> **선행 단원**: Chapter 18(http-protocol — `HttpParser.parse`/`parseResponse`, `HttpMessageWriter`, `Headers`, `BodyFraming`, `HttpProtocolException`, `record` 메시지 모델), Chapter 17(tcp-socket — `ServerSocket(:0)`·`ConnectionHandler` 경계·half-close·4규율), Chapter 15(가상 스레드), Chapter 13(스레드풀), Chapter 11(연결당 스레드), Chapter 08(`volatile` 가시성). **Phase 3의 세 번째 단원** — ch18이 만든 **1왕복 핸들러**를 `accept` 루프 + keep-alive 연결 루프 + 교체 가능한 동시성에 꽂는다.

> **공식 문서**: [RFC 9112 (HTTP/1.1) §9 Connection Management](https://www.rfc-editor.org/rfc/rfc9112#section-9) · [RFC 9112 §6.3 Message Body Length](https://www.rfc-editor.org/rfc/rfc9112#section-6.3) · [`java.util.concurrent.Executor`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Executor.html)

> ⚠️ **이 단원은 Gradle 의존을 갖는 유일한 단원**이다 — `build.gradle`에 `implementation project(':chapter18-http-protocol')`. ch18의 파서/직렬화기를 **복사하지 않고 실제 의존으로 끌어온다.** 그래서 **`main` 브랜치에선 ch18·ch19 스텁이 둘 다라 ch19 테스트가 전부 빨간불**이다(정상). `solve`에서 ch18까지 푼 뒤에야 초록불이 된다.

---

## 이 단원의 큰 그림 — "동시성 서버 = (연결 루프) × (주입하는 Executor)"

> 동시성 서버는 곱이 아니라 **두 직교축의 분리**다: **연결을 어떻게 무는가**(keep-alive 루프, 결정적)와 **연결들을 어떻게 동시에 돌리는가**(Executor, 비결정적). 서버 본체는 한 벌이고, 진화하는 건 **주입 한 줄**이다.

```
              [축 1] 연결 루프  (프로토콜 로직 · in-memory · 100% 결정적 · flaky 0)
                 ConnectionLoop.serve(in, out, handler, limits)
                 한 소켓에 요청 N개:  parseOrNull → handle → 협상 → writeResponse  (반복)
                          │
                          │  executor.execute(() -> handleConnection(socket))   ← 직교점(한 줄)
                          ▼
              [축 2] 동시성   (스레딩 모델 · 진짜 소켓·스레드 · 소수 불변식만)
                 ThreadingStrategy:  callerRuns → threadPerConnection → fixedPool → virtual
```

`HttpServer`는 **스레딩 모델을 모른다**(연결을 `executor.execute`에 넘기는 한 줄뿐). `ConnectionLoop`는 **소켓을 모른다**(`InputStream`/`OutputStream`만 안다 — ch17 `ConnectionHandler` 정신). 이 두 "모름"이 직교성의 물리적 구현이다.

```
[Layer A] 연결 루프 + 정책 + 파서 (Socket 모름)   → BAIS/BAOS, 100% 결정적 (sleep 0·스레드 0·flaky 0)
   BoundedHttpParser · ConnectionClose · ConnectionPolicy · ConnectionLoop · Responses
        │
        ▼
[Layer B] accept 엔진 + 스레딩 (진짜 소켓·스레드)  → loopback + @Timeout + CountDownLatch (소수 불변식)
   HttpServer · ThreadingStrategy
```

> **한 줄 슬로건**: 동시성 서버 = (keep-alive 연결 루프) × (교체 가능한 Executor) — 서버는 한 벌, 진화하는 건 주입 한 줄.

**다루지 않는 것(경계)**: 라우팅(@Route 리플렉션)·미들웨어 체인·JSON 직렬화 → **ch20**. backpressure/큐 한계·graceful drain의 정밀 제어·HTTP/2 멀티플렉싱·TLS·WebSocket·진짜 부하 벤치마크 → 이론 박스로만.

---

## keep-alive 연결 루프 — 이 단원의 무게중심 (`ConnectionLoop.serve`)

ch18은 한 소켓에 **1왕복**만 했다(요청 1 → 응답 1 → close). keep-alive는 **한 소켓에 요청 N개**를 문다:

```
loop:
  req = parseOrNull(in, limits)          ← 조용한 EOF면 null → break (정상 종료)
  if req == null: break
  res = handler.handle(req)              ← "무엇을 응답할지"는 주입받은 핸들러가 (서버는 모름)
  keep = shouldKeepAlive(req, count, limits)
  res = applyConnectionHeader(res, keep) ← Connection: keep-alive / close 헤더 박기
  writeResponse(out, res)                ← ch18 직렬화기 그대로
  count++
  if !keep: break                        ← 협상 결과 종료면 그 요청 후 끝
```

`serve`는 **처리한 요청 수를 반환**한다(테스트 훅). 이 한 정수 덕에 "keep-alive 3요청 → `serve()==3`", "`Connection: close` → `serve()==1`"을 소켓·스레드 없이 결정적으로 검증한다.

> 📦 **EOF 계약 차이 — ch18 `parse`는 예외, ch19 `parseOrNull`은 `null`**
> ch18 `HttpParser.parse`는 즉시 EOF에서 **예외**를 던졌다("요청이 와야 한다" — 1왕복 계약). 그러나 keep-alive 루프는 정반대가 필요하다: 요청 사이의 **조용한 EOF는 정상 종료**(클라가 더 안 보냄)다. 그래서 `BoundedHttpParser.parseOrNull`은 요청 전 EOF에서 **`null`**을 반환한다. **ch18 `parse`를 keep-alive 루프에 그대로 쓰면 안 된다** — idle 종료마다 예외가 터진다. 이 계약 한 줄 차이가 N왕복 서버의 핵심이다.

> 📦 **에러는 accept 루프 밖으로 새지 않는다**
> malformed/한계초과는 `serve` 안에서 **1회 응답으로 변환**(400/413/431)하고 연결을 종료한다. 상위 `handleConnection`이 `catch(IOException ignored)`로 삼키는 건 "이미 응답한 뒤의 전송 실패/소켓 타임아웃"뿐 — **프로토콜 에러를 상위에서 삼키면 클라가 응답을 못 받는다**.

---

## connection-close(EOF까지) 프레이밍 — 응답 측에만 존재하는 모순 (`ConnectionClose`)

ch18에서 떡밥만 깔았던 **세 번째(가 아니라 0번째) 프레이밍**: `Content-Length`도 `chunked`도 없으면 **연결을 끊는 것이 곧 메시지 끝**(EOF = 프레임 경계)이다.

> 📦 **왜 응답 측에만 있나 (요청엔 못 쓴다)**
> 클라이언트가 요청 바디를 "EOF까지"로 보내려면 소켓을 닫아야 하는데, 닫으면 **응답을 받을 길이 사라진다**(half-close 없이는). 그래서 connection-close 프레이밍은 **응답 전용**이다 — 서버는 응답을 다 쓰고 소켓을 닫아 끝을 알린다. 이게 keep-alive와 정면충돌하는 이유: **길이를 못 정하는 응답은 반드시 `Connection: close`여야 한다**(`responseMustClose`). keep-alive는 "다음 응답이 어디서 시작하는지" 길이로 알아야 성립하므로.

`readUntilEof`(전부 누적)와 `frameResponseBody`(Content-Length 있으면 그 길이만, 없으면 EOF까지)가 이 규칙을 박는다. 그래서 `Responses` 빌더가 **모든 응답에 `Content-Length`를 항상 확정**한다 — 길이 프레이밍이 keep-alive의 전제다.

---

## DoS 방어 — "다 읽고 검사"가 아니라 "누적 중 즉시 차단" (`BoundedHttpParser` · `ServerLimits`)

> 📦 **slowloris / 거대 헤더 — 이미 다 읽었으면 늦다**
> 공격자는 헤더를 1바이트씩 영원히 보내거나(slowloris), 8GB짜리 요청라인 한 줄을 보낸다. **다 읽은 다음 길이를 검사하면 이미 OOM**이다. 그래서 `readLineBounded`는 **누적 바이트가 `maxBytes`를 넘는 순간 즉시** `RequestLimitException`을 던진다(끝까지 안 읽음). idle 소켓은 `soTimeout`(ch17 회수)으로 끊는다.

상태코드 매핑(`errorStatusFor`): 요청라인/헤더가 너무 큼 → **431**(Request Header Fields Too Large), 바디가 `maxBodyBytes` 초과 → **413**(Content Too Large), 그 외 문법 오류 → **400**. `ServerLimits`는 **`maxBodyBytes`를 반드시 포함**한다 — 라인·헤더만 막고 바디를 무제한 두면 거대한 `Content-Length` POST 한 방에 메모리가 터지는 반쪽 방어다.

---

## 4가지 스레딩 모델 — 서버 코드는 한 줄도 안 바뀐다 (`ThreadingStrategy`)

`HttpServer`가 `executor.execute(() -> handleConnection(s))` 한 줄뿐이라, **주입하는 `Executor`만 갈아끼우면** 네 모델이 서버 코드를 안 건드리고 갈린다. 동시성 도구는 ch11~13에서 **이미 직접 만들었다** — 여기선 다시 만들지 않고 **선택**을 배운다(각 팩토리는 1~3줄 표준 API 위임).

| 모델 | 팩토리 | 회수 | 강점 | 무너지는 지점 |
|---|---|---|---|---|
| **caller-runs** | `command -> command.run()` | — | 진짜 직렬(동시성 0) → **단위 테스트가 accept 루프를 결정적으로 검증** | accept 스레드가 처리에 묶여 다음 연결을 못 받음(실서비스 불가) |
| **연결당 새 스레드** | `new Thread(r).start()` | ch11 | 단순·즉시 병렬 | 연결 폭증 시 **스레드 고갈(C10K)** — 스레드는 비싸다 |
| **고정 스레드풀** | `Executors.newFixedThreadPool(n)` | ch13 | 워커 재사용·동시성 상한 | 블로킹 IO에서 모든 워커가 keep-alive에 묶이면 **풀 고갈**(큐만 쌓임) |
| **연결당 가상 스레드** | `newVirtualThreadPerTaskExecutor()` | ch15 | IO 바운드에 최적·풀 고갈 없음 | (CPU 바운드엔 이득 없음) |

> **진화의 결론 — 가상 스레드는 "연결당 스레드"의 부활이다.** C10K가 연결당 스레드를 죽였고(스레드가 비싸서), 그 회피책이 풀(고갈 문제 도입)·이벤트 루프(콜백 지옥)였다. 가상 스레드는 스레드를 **다시 싸게** 만들어, 가장 단순했던 "연결당 하나" 모델을 블로킹 코드 그대로 부활시킨다 — 풀링 **금지**가 핵심(가상 스레드를 풀에 넣으면 이점이 사라진다).

> ⚠️ `caller-runs`는 README의 "단일스레드 *서버 모델*"(별도 워커 1개 = `newSingleThreadExecutor`, accept와 처리가 분리)과 **다르다**. caller-runs는 accept 자체가 막힌다.

---

## 결정성 — 비결정성을 곱이 아니라 분리된 합으로

> 📦 **두 종류의 테스트를 곱하지 않고 더한다**
> 프로토콜 로직(연결 루프·정책·파서)은 **곱하면** "동시성 × 프레이밍"의 조합 폭발 + flaky가 된다. 그래서 **분리**한다:
> - **`ConnectionLoopTest`**(in-memory): `ByteArrayInputStream`/`ByteArrayOutputStream`으로 keep-alive·close·malformed·한계를 **소켓·스레드·sleep 없이** 결정적으로. `serve()` 반환값(처리 수)이 핵심 훅.
> - **`HttpServerIntegrationTest`**(진짜 소켓): 동시성에 대해 **소수의 강한 불변식만** — N개 동시 클라가 `CountDownLatch` 출발 배리어로 한꺼번에 출발 → **응답 바디 집합 == 보낸 target 집합**(뒤섞임·유실 0). 순서·타이밍은 안 본다. `ServerSocket(:0)` + `@Timeout` + 데몬 스레드(ch17 4규율).
>
> 비결정성을 "태그로 숨기지" 않고 기본 `test`에 포함하되, 그 **표면적을 불변식 하나로 압축**한다.

---

## HTTP 서버 실무 함정 (가장 많이 틀리는 곳)

1. **정지 플래그는 `volatile`** — accept 스레드와 `close` 스레드가 다른 스레드라 가시성 없이는 `closed` 변경이 안 보여 영원히 accept(ch08 회수). 닫힌 `ServerSocket`의 `accept()`는 `SocketException`으로 깨어나므로 그것을 종료 신호로(ch17 회수).
2. **한 연결의 예외는 그 연결만 격리** — `handleConnection`이 던지면 소켓만 닫고 다음 연결·서버 전체엔 영향 0(ch13 "작업 하나 실패가 풀을 안 죽임").
3. **소켓 close는 누구 책임인가** — `ConnectionLoop`는 **안 닫는다**(스트림만 안다). 닫는 건 `handleConnection`의 `try (s) { ... }`(LIFO). 계약을 한 곳에 모은다.
4. **길이 미확정 응답 + keep-alive = 파이프라인 붕괴** — `Content-Length` 없는 응답을 keep-alive로 보내면 클라가 다음 응답 시작점을 못 찾는다. `responseMustClose`가 강제 `close`로 막는다.
5. **`close`는 멱등 + Executor 정리 분기** — `executor instanceof ExecutorService`면 `shutdown()`+`awaitTermination`, `AutoCloseable`이면 `close()`, caller-runs는 정리할 게 없어 어느 분기도 안 탐. 이 `instanceof` 분기가 4모델을 모두 안전하게 정리하는 열쇠.
6. **`maxRequestsPerConnection`로 연결 자원 회수** — keep-alive를 무한 허용하면 한 클라가 연결을 영원히 점유한다. 한도 도달 시 협상에서 `close`로 끊어 자원을 회수.

---

## 연습 문제

> 권장 순서: **ConnectionPolicy → BoundedHttpParser → ConnectionClose → ConnectionLoop → ThreadingStrategy → HttpServer.**
> (순수 협상 함수 → 한계 강제 파서 → EOF 프레이밍 → 루프 무게중심 → Executor 선택 → 진짜 소켓 엔진. **앞 5개를 다 풀면 `ConnectionLoopTest`가 초록불**, 마지막 `HttpServer`까지 풀면 통합 테스트도 초록불.)

### 완성 제공(채우지 않음)
`HttpHandler`(@FunctionalInterface 경계), `RequestLimitException`(ch18 예외 상속·431/413), `ServerLimits`(record·`defaults()`), `Responses`(ok/text/error·`Content-Length` 자동 확정). 테스트 픽스처 `ChunkedInputStream`(ch17 복사), `KeepAliveClient`(half-close 없는 연속 exchange).

### 채우는 클래스

| 클래스 | 스텁 | 주제 |
|---|---|---|
| `ConnectionPolicy` | 4 | `clientWantsKeepAlive`(버전 기본값+헤더) / `shouldKeepAlive`(max-requests 합산) / `applyConnectionHeader` / `responseMustClose`(길이 미확정→강제 close) |
| `BoundedHttpParser` | 3 | `readLineBounded`(누적 중 즉시 431) / `readHeadersBounded` / `parseOrNull`(요청 전 EOF=`null`) |
| `ConnectionClose` | 2 | `readUntilEof` / `frameResponseBody`(Content-Length 있으면 그 길이만, 없으면 EOF까지) |
| `ConnectionLoop` | 3 | `serve`(keep-alive 루프 무게중심·처리 수 반환) / `errorStatusFor`(431/413/400) / `errorResponse` |
| `ThreadingStrategy` | 4 | `callerRuns` / `threadPerConnection` / `fixedPool` / `virtualThreadPerConnection` |
| `HttpServer` | 5 | 생성자(`ServerSocket(:0)`·`volatile`) / `port` / `start`(데몬 accept 루프) / `handleConnection`(격리) / `close`(멱등·Executor 분기) |

---

## 실행

```sh
# 전체 테스트 — main에선 ch18·ch19 스텁이 둘 다라 전부 빨간불(정상). solve에서 ch18+ch19을 풀면 초록불.
./gradlew :chapter19-concurrent-http-server:test

# 무게중심만(in-memory, 결정적)
./gradlew :chapter19-concurrent-http-server:test --tests "study.chapter19.ConnectionLoopTest"

# 진짜 소켓 + 동시성 불변식
./gradlew :chapter19-concurrent-http-server:test --tests "study.chapter19.HttpServerIntegrationTest"
```

---

## 다음 단원(ch20 mini-web-framework)으로

이 단원이 **주입받는 자리**(`HttpHandler`)에, ch20은 **@Route 리플렉션 라우터**(ch10 회수)·**미들웨어 체인**·**JSON 직렬화**를 채운다. ch19은 라우팅을 모른다 — 테스트는 echo/fixed 람다를 주입했지만, ch20에선 그 자리에 진짜 디스패처가 들어간다. `HttpServer`·`ConnectionLoop`는 한 줄도 안 바뀌고 핸들러만 똑똑해진다 — 그게 이 단원이 그은 경계의 값어치다.
