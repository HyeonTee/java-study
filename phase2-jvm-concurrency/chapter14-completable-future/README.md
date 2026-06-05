# Chapter 14 — CompletableFuture · 비동기 파이프라인 · 예외 전파

> **선행 단원**: Chapter 13(executor-blocking-queue). ch13은 `Future`의 **블로킹 `get()`(pull)**까지 다루고 "결과를 조합·변환하는 건 ch14"라 미뤘다. 이 단원은 그 위에 **콜백(push)**을 얹는다 — 결과를 기다리지 않고 "오면 할 일"을 미리 등록해 파이프라인을 조립한다. Chapter 04(함수 합성)·Chapter 05(Optional/Stream 모나드)의 **비동기 버전**이기도 하다.

> **공식 문서**: [`CompletableFuture`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/CompletableFuture.html) · [`CompletionStage`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/CompletionStage.html)

---

## 이 단원의 큰 그림 — pull에서 push로

ch13의 `take()`/`get()`은 결과가 올 때까지 **블록**했다(pull). 변환하려면 `get()`→계산→또 제출을 손으로 엮어야 했다. CompletableFuture는 반대다: 결과를 **기다리지 않고** "결과가 오면 이걸 해라"를 **미리 등록**(push)해 둔다. 나중에 완료되면 등록된 콜백이 도미노처럼 발화한다.

```
ch13 Future:  result = future.get();  // 블록(pull). 합성 불가 — 손으로 엮어야.
        ↓
ch14 push:    future.thenApply(f).thenCompose(g).exceptionally(h)  // 파이프라인을 미리 조립
```

ch04/05와의 대응 (**CompletableFuture는 비동기 모나드**):

| 동기 (ch04/05) | 비동기 (ch14) |
|---|---|
| `Optional.map` / `Function.andThen` | `thenApply` |
| `Optional.flatMap` / `Stream.flatMap` | `thenCompose` |
| `Optional.orElse` | `exceptionally` |
| `BiFunction` zip | `thenCombine` |

이 단원에서 다루지 않는 것(경계): **Virtual Thread·structured concurrency → ch15**, reactive streams(`Flow`/`Publisher`), `orTimeout`/`completeOnTimeout`(시간 의존 → flaky), 취소(`cancel`).

---

## 직접 구현 — `MyPromise` (간판)

`CompletableFuture`의 정수는 **콜백 등록 + 단 한 번의 완료**다. 200줄 안에 직접 만들 수 있다:

- **상태**: 미완료 → (값 완료 | 예외 완료). 완료는 **단 한 번**(두 번째 `complete`는 무시, `false` 반환).
- **콜백 발화**: 완료 *전* 등록분은 모아 뒀다 완료 시 발화, 완료 *후* 등록분은 즉시 발화. 어느 경우든 정확히 한 번.
- **합성**: `thenApply`는 새 promise를 만들고 "값이 오면 `fn` 적용해 새 promise 완료" 콜백을 건다. `thenCompose`는 `fn`이 반환한 inner promise에 **다시 콜백을 걸어 평탄화**한다.

수동 `complete`만으로 완전히 동작하므로 **스레드 없이 결정적**이다. `get()`만은 ch13의 `wait`/`notifyAll`로 완료까지 블록한다.

---

## 사용 — 실제 CompletableFuture로 조립

`MyPromise`에서 손으로 만든 것이 표준 API에선 어떤 모양인지 대응시킨다.

### `thenApply` vs `thenCompose` (map vs flatMap)

```java
cf.thenApply(n -> n + 1)                       // T → U          (값 변환)
cf.thenApply(n -> asyncLookup(n))              // T → CF<U>  →  CF<CF<U>>  중첩! (버그)
cf.thenCompose(n -> asyncLookup(n))            // T → CF<U>  →  CF<U>      평탄화 (정답)
```

### `thenApply` vs `thenApplyAsync`

`thenApply`(비-Async)는 **완료를 일으킨 스레드** 또는 (이미 완료됐으면) **호출 스레드**에서 실행될 수 있다 — "항상 별도 스레드"가 **아니다**(흔한 오해). `thenApplyAsync`는 executor(기본 `ForkJoinPool.commonPool()`)에서 실행한다. ch13에서 만든 스레드풀을 넘길 수도 있다.

### 예외 전파와 복구

비동기 단계가 던진 예외는 future를 예외 완료시키고 **하류 변환은 건너뛴다**. 복구 도구:

| 메서드 | 시그니처 | 동작 |
|---|---|---|
| `exceptionally` | `Throwable → T` | **예외일 때만** 호출, 대체값. 성공이면 통과 |
| `handle` | `(T, Throwable) → U` | **항상** 호출(성공·실패 모두), 변환·복구 동시 |
| `whenComplete` | `(T, Throwable) → void` | **관찰만**(값 변환 X), 예외는 그대로 하류 전파 |

> **래핑 함정**: 단계에서 던진 예외는 `join()`엔 `CompletionException`(비검사), `get()`엔 `ExecutionException`(검사)으로 **감싸여** 나온다 — 원래 예외는 `getCause()`로 꺼낸다. `exceptionally`/`handle`이 받는 `Throwable`도 보통 `CompletionException` 래퍼이므로 언래핑이 필요하다.

> **`allOf`는 `CompletableFuture<Void>`** — 결과를 모아주지 않고 완료만 알린다. 결과 수집은 `allOf(...).thenApply(v -> ...)` 안에서 각 future를 `join()`(이미 완료라 즉시)해 직접 모은다.

---

## 채점 — 비동기를 결정적으로 (ch11/11 원칙 계승)

- **`MyPromise`(간판)**: 수동 `complete`라 모든 발화가 호출 스레드에서 동기 실행 → **스레드 자체가 불필요**, 100% 결정적. 콜백 등록/평탄화/예외 전파의 *논리*만 단언한다. (`get()` 블로킹은 latch+`@Timeout` 한 건으로만.)
- **실제 CF**: `join()`으로 **결과**만 단언(타이밍·스레드·실행 순서는 절대 단언 금지 — flaky). `@Timeout`으로 데드락 방지. 실패는 `failedFuture`/예외 던지는 supplier로 명시 주입 → 결정적.

---

## 연습 문제

> 권장 순서: **MyPromise → CompletableFuturePractice.**

### MyPromise (10문제) — 미니 promise 직접 구현 · 간판

| 메서드 | 핵심 |
|---|---|
| 생성자 / `complete` / `completeExceptionally` | 멱등 완료(1회만) + 콜백 발화 |
| `isDone` | 완료 여부 |
| `whenComplete` | (값, 예외) 콜백 등록 (발화 규칙의 원형) |
| `thenApply` | map — 새 promise + 콜백, 예외 전파, fn 예외 처리 |
| `thenCompose` | flatMap — inner promise에 다시 콜백 걸어 평탄화 |
| `get` | 완료까지 블록(wait/notify), 예외는 `ExecutionException` 래핑 |
| `completed` / `failed` | 즉시 완료 팩토리 |

### CompletableFuturePractice (8문제) — 표준 CF 파이프라인·복구

| 메서드 | 핵심 |
|---|---|
| `runAsync` | `supplyAsync(supplier, executor)` |
| `mapResult` / `flatMapResult` / `combine` | thenApply / thenCompose / thenCombine |
| `allOfResults` | `allOf`(Void!) 후 각 `join()`으로 순서대로 수집 |
| `recover` / `handleResult` | exceptionally / handle |
| `describe` | handle + `CompletionException` 언래핑(원인 메시지) |

---

## 실행

```sh
# 전체 테스트 (스텁이 비어 있어 빨간불 — 구현하며 초록불로)
./gradlew :chapter14-completable-future:test

# 특정 클래스
./gradlew :chapter14-completable-future:test --tests "study.chapter14.MyPromiseTest"
```
