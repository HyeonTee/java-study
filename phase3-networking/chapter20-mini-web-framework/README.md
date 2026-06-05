# Chapter 20 — 미니 웹 프레임워크 (제네릭 Handler × 라우터 × 미들웨어 양파 × JSON 직렬화 · ch19 봉합선)

> **선행 단원**: Chapter 19(concurrent-http-server — `HttpHandler` 봉합선·`Responses`), Chapter 18(http-protocol — `HttpRequest`/`HttpResponse`/`Headers`), Chapter 10(reflection-annotations — `@Route`·`RouteScanner`·`Reflect.invoke`/`getCause`), Chapter 06(record/sealed/pattern matching), Chapter 04(함수형 인터페이스·합성), Chapter 02(맵), Chapter 01·03(제네릭·바운드). **Phase 3의 마지막 단원이자 저장소의 캡스톤.**

> **공식 문서**: [`java.util.function`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/function/package-summary.html) · [Records & sealed (JLS)](https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html) · [`RecordComponent.getAccessor`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/reflect/RecordComponent.html)

> ⚠️ **이 단원은 ch19·ch18을 Gradle 의존으로 실제 재사용한다**(`build.gradle`에 `implementation project(':chapter19-...')` + `':chapter18-...')`). 봉합선을 문서가 아니라 **진짜 타입**으로 증명한다 — {@code HttpAdapter}가 진짜 `study.chapter19.HttpHandler`를 구현하고 ch18 `HttpRequest`/`HttpResponse`를 다룬다. 그 대가로 **HTTP 봉합 테스트는 `main`에서 빨간불**이다(ch18·ch19 스텁 때문). 챕터를 순서대로(ch18→ch19→ch20) 풀면 초록불이 된다. **단, 코어는 HTTP 무관 제네릭이라 ch20만 풀어도 코어 테스트는 초록불**이 된다(아래 "결정성" 참조).

---

## 이 단원의 큰 그림 — 프레임워크 = (Router ∘ dispatch) ∘ (Middleware 양파) ∘ (JsonValue 직렬화)

> ch10 `RouteScanner`의 Javadoc은 자신이 하는 일을 *"ch19/ch20 미니 웹 프레임워크가 부팅할 때 하는 바로 그 일"*이라 했고, ch19 `HttpHandler`의 Javadoc은 *"ch20이 이 자리에 라우터·미들웨어·JSON을 채운다"*고 약속했다. **ch20은 그 두 약속을 진짜 프레임워크로 갚는 자리다.**

핵심 통찰: 프레임워크가 *진짜로 하는 일*(라우트 테이블·디스패치·합성·직렬화)은 **HTTP와 직교**한다. 그래서 코어를 HTTP 무관 제네릭 `Handler<Req,Res>` 한 벌로 세운다.

```
        [요청]  HttpRequest ──(HttpAdapter)──▶ HttpContext(요청 + 경로변수)
                                                     │
   ┌─────────────────────────  Handler<HttpContext, HttpResponse>  ───────────────────────┐
   │   Middleware 양파:  logging( errorBoundary( ─── 엔드포인트 ─── ) )                       │
   │                                              │                                         │
   │                       Router.match(method, /users/{id})  ──▶  RouteMatch(handler,vars) │
   │                                              │                                         │
   │                          핸들러: 도메인 객체 → JsonReflect.toJson → JsonWriter.write     │
   └──────────────────────────────────────────────┼─────────────────────────────────────────┘
                                                   ▼
        [응답]  HttpResponse  ◀──(HttpAdapter: NotFound→404 / throws→500)──  HttpResponses.json
```

`HttpAdapter`는 이 스택을 ch19 `HttpHandler`({@code HttpResponse handle(HttpRequest)}) 시그니처로 접는 **얇은 한 장**이다 — ch19 `HttpServer`에 그대로 주입된다(서버 코드는 한 줄도 안 바뀐다).

> **한 줄 슬로건**: 서버는 한 줄도 안 바뀌고, 핸들러만 똑똑해진다. **프레임워크는 마법이 아니라 18단원의 합성이다.**

### 패키지 구조 — import 방향이 아키텍처를 강제한다

다른 17개 단원은 단일 패키지(`study.chapterNN`)지만, ch20은 프레임워크라 **레이어를 패키지로 가른다**. 핵심은 **패키지 import 그래프 자체가 "코어는 HTTP를 모른다"를 컴파일 단위로 강제**한다는 것 — `study.chapter18`/`study.chapter19` import는 오직 `http` 패키지에만 존재한다.

```
study.chapter20
├── core/      # 제네릭 엔진 (의존 0 — ch18·17은 물론 routing/json/http도 모름)
│   ├── Handler<Req,Res>      Middleware<Req,Res>
│   ├── Middlewares           Routable<Self>
├── routing/   # → core 의존
│   ├── Router   PathPattern   RouteMatch
│   └── NotFoundException   MethodNotAllowedException   (status 숫자 모름)
├── json/      # 독립 (core와도 무관)
│   ├── JsonValue(sealed)   JsonWriter   JsonReflect
└── http/      # ★봉합선 — core·routing·json + ch18·ch19 의존 (이 패키지만 HTTP를 안다)
    ├── HttpContext(ch18 HttpRequest 감쌈)   HttpResponses(ch19 Responses 재사용)
    └── HttpAdapter implements study.chapter19.HttpHandler
```

각 패키지엔 `package-info.java`로 레이어·의존 방향을 문서화했다. **의존 화살표는 한 방향**(`http → routing → core`, `http → json`)이라 순환이 없다 — `core`를 풀면 `core`만으로 컴파일·테스트되고, `http`만 ch18/ch19 스텁에 묶인다.

**다루지 않는 것(경계)**: JSON 파서(직렬화기만)·DI 컨테이너·완전한 Spring·검증/세션/쿠키·와일드카드/정규식 라우팅·content-negotiation → 이론 박스. 진짜 소켓·동시성 → ch19 소관(여기선 0).

---

## 핵심 박스 1 — `Handler<Req,Res>`는 전 계층의 공통 화폐

라우터·미들웨어·핸들러가 **전부 이 한 타입으로 합성**된다(ch01 제네릭 + ch04 함수형 인터페이스). ch19 `HttpHandler`는 사실 이 `Handler`의 특수화 — 개념상 `Handler<HttpRequest, HttpResponse>`다.

- `Handler.andThen(Function)`은 **결과만** 후처리한다(ch04 `Function.andThen`의 핸들러판).
- 요청 전(before) 가로채기는 못 한다 — 그건 `Middleware`의 일. **이 차이가 단원의 분기점이다.**
- 핸들러는 도메인 객체를 반환하고, JSON 문자열을 손으로 짜지 않는다 — 직렬화는 응답 경계의 어댑터가.

---

## 핵심 박스 2 — 양파(onion) vs 단순 후처리: `andThen`으로는 못 하는 일

`Middleware`는 **`Handler → Handler` 변환자**다(`wrap(next)`). 합성하면 호출 전·후를 모두 감싸는 **양파**가 된다.

```
chain([A, B, C]).apply(endpoint):
   A-before → B-before → C-before → [endpoint] → C-after → B-after → A-after
   (before는 정순 A,B,C / after는 역순 C,B,A — 첫 원소가 가장 바깥 양파)
```

> **학습자 최대 오답: 합성 방향.** `A.andThen(B)`의 결과 `wrap(h)`은 `A.wrap(B.wrap(h))`여야 한다(A가 바깥). `MiddlewareOnionTest`의 `sink` 로그 기대 시퀀스가 이 방향을 **사실상 명세**한다(정답 노출 없이 순서로 강제). 빈 체인은 **항등 미들웨어**(`identity`, `wrap(h)==h`) — 합성의 항등원.

`errorBoundary`는 "`try/catch`를 핸들러 바깥에 한 겹" 두른 양파다. **함정**: `NotFoundException`/`MethodNotAllowedException`도 `RuntimeException`이라, 무조건 삼키면 라우팅 404/405가 사라진다 → `onError`가 예외 타입을 구분(라우팅 예외는 재던짐)하거나 어댑터가 먼저 처리한다.

---

## 핵심 박스 3 — Router는 status 숫자를 모른다 (프레임워크-중립 코어)

`Router.match`는 매칭 실패 시 `NotFoundException`, 메서드 불일치 시 `MethodNotAllowedException`을 **던진다**(404/405 같은 HTTP 숫자를 들지 않는다). 숫자 번역은 `HttpAdapter`의 일이다. ch18처럼 `method`는 `String`("405는 응답 단계의 결정"). 이 "던지는" 경계가 코어를 HTTP 무관 제네릭으로 유지하는 핵심이다.

`match`는 **두 단계**다(ch10 `RouteScanner.dispatch` 회수): ① method+path 둘 다 맞는 첫 라우트를 찾고, ② 없으면 *path만* 맞는 게 있었나로 405 vs 404를 가른다(경로 존재를 증명한 뒤 405를 던져야 클라이언트가 "GET이 아니라 POST를 쓰라"를 안다).

---

## 경로변수와 자기참조 제네릭 — 경로변수가 `Req` 안에 올라탄다

`Router<Req extends Routable<Req>, Res>`. 요청 타입을 `Routable<Self>`로 **자기참조 바운드**(F-bounded, ch03 `Comparable<T>` 정신)하는 이유: `withPathVars`가 **자기 타입**을 그대로 돌려줘야(`HttpContext.withPathVars` → `HttpContext`) `Handler<Req,Res>`의 단일 인자 모양이 유지되기 때문이다. 그 덕에 매칭으로 뽑은 경로변수가 별도 인자 없이 `Req` 안에 올라탄다.

`PathPattern`이 `/users/{id}` 한 패턴의 컴파일·매칭·변수 추출을 격리한다(세그먼트 기반 `{var}` 1단계 — 와일드카드/정규식은 경계 밖).

---

## ch10 회수 박스 — `Router.route` 수동 등록 = `@Route` 스캔이 자동화하는 그것

ch10 `RouteScanner.scan`은 이미 `@Route`를 리플렉션으로 읽어 `경로 → Method` 맵을 만든다(HTTP 무관 `Map<String,Method>`). ch20 `Router.route(method, pattern, handler)`는 같은 일을 **제네릭 `Handler` 값**으로 일급화한다(`Method` 대신 진짜 핸들러 객체). *"`route(...)` 수동 등록 = `@Route` 스캔이 자동화하는 그것"*이다 — 자동 스캔 실물은 ch10가 소유하므로 여기서 다시 만들지 않는다(`AnnotationRouter = RouteScanner + 메서드/경로변수 매칭 + 미들웨어 래핑`은 한 줄 확장, 이론 박스).

**리플렉션 실물 회수는 `JsonReflect.toJson` 한 곳**으로 단일화한다: record 컴포넌트를 `getRecordComponents()` + `component.getAccessor().invoke(obj)`로 읽는다(ch10 `Field.setAccessible` 대신 **record 접근자** — "같은 패턴의 다른 적용"). 접근자가 던진 예외는 `InvocationTargetException`으로 감싸여 나오므로 ch10 `Reflect.invoke` 정신대로 **`getCause()`로 풀어 재던진다**.

---

## 봉합선 박스 — `HttpAdapter`: 진짜 ch19 `HttpHandler`에 꽂힌다

`HttpAdapter implements study.chapter19.HttpHandler`. 코어가 완성되면 이 어댑터를 그대로 ch19 `HttpServer`·`ConnectionLoop`에 주입할 수 있다(ch19 "핸들러만 갈아끼운다" 회수). ch18 `HttpRequest`↔`HttpContext` 매핑(`method`/`path`는 `request.line()`에서)을 하고, 응답은 ch19 `Responses`(text/error) + ch20 `HttpResponses.json`(application/json)으로 만든다 — **이전 단원 구현을 그대로 재사용**.

코어가 status를 모르는 대가를 여기서 갚는다: `NotFoundException`→404, `MethodNotAllowedException`→405, 핸들러 `RuntimeException`→500.

---

## 결정성 — 코어는 자립, 봉합선만 의존

> 📦 **두 종류의 테스트, 두 종류의 빨간불**
> - **코어 테스트**(`HandlerComposition`·`MiddlewareOnion`·`MiddlewareError`·`PathPattern`·`RouterMatch`·`RouterDispatch`·`JsonWriter`·`JsonReflect`): HTTP 무관 제네릭이라 ch18/ch19과 **독립**. `main`에선 ch20 스텁 때문에만 빨간불 → **ch20 코어만 풀면 초록불**(소켓·스레드·sleep 0, 100% 인메모리·결정적). 요청 픽스처도 ch18에 의존하지 않는다 — 경량 `String`·`TestRoute`·`User`만 쓴다(진짜 ch18 `HttpRequest`는 `http` 패키지 봉합 테스트에서만 등장한다).
> - **봉합 테스트**(`SeamContract`·`FrameworkIntegration`): 진짜 ch18 `Headers.with/get`·ch19 `Responses`를 거치므로, ch18까지 풀어야 초록불(B 결합의 의도된 대가). 단 여기서도 **소켓은 없다** — `HttpHandler`는 순수 함수라 `handle(HttpRequest)`를 직접 호출해 검증한다.

비결정성(진짜 소켓·동시성)은 ch19이 이미 다뤘다. ch20은 순수 로직이라 `@Timeout`·`CountDownLatch`가 0개다.

---

## 실무 함정

1. **미들웨어 합성 방향**(before 정순 / after 역순) — 최대 오답. `andThen`은 `this`가 바깥 양파.
2. **`errorBoundary`가 라우팅 예외(404/405)까지 삼키면** 라우팅이 사라진다 — 예외 타입 구분 필요.
3. **`JsonWriter` 콤마** — 배열·객체 마지막 원소 뒤 콤마 금지(`String.join` 또는 인덱스 가드).
4. **JSON 이스케이프는 백슬래시를 가장 먼저** — 안 그러면 이중 이스케이프. 제어문자(`<0x20`)는 `\u00XX`.
5. **`JsonReflect`는 record 접근자**(`getRecordComponents`+`getAccessor().invoke`)를 쓰되, `InvocationTargetException`은 `getCause()`로 풀어 재던짐(ch10).
6. **정수/실수 구분** — `record User(long id,...)`의 `id`는 `1`이지 `1.0`이 아니다(`JsonValue.of(Number)`가 흡수, 완성 제공).
7. **`PathPattern`은 세그먼트 개수부터 비교**(빠른 불일치) + 선행/후행 슬래시 정규화.
8. **시계·랜덤 금지**(결정성) — 미들웨어는 주입된 로그/카운터만 관측.

---

## 연습 문제

> 권장 순서: **Handler.andThen → Middleware(andThen/chain/apply/identity) → PathPattern → Router → JsonWriter → JsonReflect → Middlewares.**
> (가장 작은 원자 → 양파 합성 → 경로 매칭 → 디스패처 → 직렬화 → 리플렉션 → 실전 미들웨어. **앞쪽 코어를 다 풀면 코어 테스트 8개가 초록불**, ch18까지 풀려 있으면 봉합 테스트까지 초록불.)

### 완성 제공(채우지 않음)
`Handler`/`Middleware`(추상 메서드·시그니처), `Routable`(F-bounded 계약), `RouteMatch`·`JsonValue`(+6 하위 record·팩토리), `NotFoundException`/`MethodNotAllowedException`, **봉합선** `HttpContext`(ch18 `HttpRequest` 감쌈)·`HttpResponses`(ch19 `Responses` 재사용)·`HttpAdapter`(ch19 `HttpHandler` 구현). 테스트 픽스처 `User`·`GreetingController`.

### 채우는 클래스 (18 스텁)

| 클래스 | 스텁 | 주제 |
|---|---|---|
| `Handler` | 1 | `andThen`(결과 후처리 — before 불가) |
| `Middleware` | 4 | `andThen`(양파 방향)·`chain`(fold·항등)·`apply`·`identity` |
| `PathPattern` | 3 | `compile`·`match`(경로변수 추출)·`variableNames` |
| `Router` | 3 | `route`(중복 IllegalState)·`match`(404/405 분기)·`asHandler`(라우팅+미들웨어+변수주입) |
| `JsonWriter` | 2 | `escape`(제어문자)·`write`(sealed switch 망라) |
| `JsonReflect` | 2 | `toJson`(record 접근자·getCause)·`fromMap` |
| `Middlewares` | 3 | `logging`(양파 로그)·`errorBoundary`(예외 경계)·`counting`(결정적 계측) |

---

## 실행

```sh
# 전체 테스트 — main에선 ch20 스텁(+봉합 테스트는 ch18/ch19 스텁)으로 빨간불. 챕터 순서대로 풀면 초록불.
./gradlew :chapter20-mini-web-framework:test

# 코어만(ch18/ch19 무관·인메모리 결정적) — 패키지로 통째 지정 가능
./gradlew :chapter20-mini-web-framework:test --tests "study.chapter20.core.*"
./gradlew :chapter20-mini-web-framework:test --tests "study.chapter20.json.*"

# 봉합선(진짜 ch19 HttpHandler·ch18 타입)
./gradlew :chapter20-mini-web-framework:test --tests "study.chapter20.http.SeamContractTest"
```

---

## 저장소 마무리 — 다음 없음: 프레임워크는 18단원의 합성이다

이 단원은 저장소의 끝이다. ch10가 부팅 때 하던 "마법"의 정체를, 우리가 18단원 내내 직접 짠 부품들로 완성했다:

| 단원 | 이 프레임워크의 어느 부품 |
|---|---|
| **ch01** 제네릭 | `Handler<Req,Res>` 공통 화폐 |
| **ch02** 맵 | `Router` 라우트 테이블 |
| **ch03** 바운드 제네릭 | `Routable<Self>` 자기참조 바운드 |
| **ch04** 함수 합성 | `Middleware` 양파 / `Handler.andThen` |
| **ch06** sealed·record·pattern | `JsonValue` 직렬화 |
| **ch10** 리플렉션·애너테이션 | `JsonReflect.toJson` (+ 상호참조 `RouteScanner`) |
| **ch18** HTTP 메시지 | `HttpContext`/`HttpResponses`가 감싼 `HttpRequest`/`HttpResponse` |
| **ch19** HttpHandler 봉합선 | `HttpAdapter`가 구현하는 진짜 `HttpHandler` |

> **프레임워크는 마법이 아니라 합성이다.** Spring의 `@RestController`·`DispatcherServlet`·`HandlerInterceptor`·`HttpMessageConverter`가 각각 이 단원의 `@Route`·`Router`·`Middleware`·`JsonWriter`에 대응한다 — 이제 그 안을 안다. 더 배울 것은 깊이(TLS·HTTP/2·DI 컨테이너·검증)지 새로운 마법이 아니다. **저장소 끝.**
