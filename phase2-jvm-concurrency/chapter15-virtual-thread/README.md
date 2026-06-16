# Chapter 15 — Virtual Thread · Structured Concurrency

> **선행 단원**: Chapter 11(Thread)·13(스레드풀/BlockingQueue)·14(CompletableFuture). Phase 2(동시성)의 마지막 단원. ch13이 "스레드는 비싸니 풀로 재사용"이라는 전제 위에 풀을 쌓았다면, 이 단원은 그 전제를 **무너뜨린다**(가상 스레드는 싸다 → 풀링하지 마라). 다음 Phase 3의 **ch19(concurrent-http-server)이 "가상 스레드/연결" 모델을 재사용**한다.

> **공식 문서**: [JEP 444 — Virtual Threads](https://openjdk.org/jeps/444) · [`Thread.ofVirtual`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html) · [JEP 453 — Structured Concurrency (Preview)](https://openjdk.org/jeps/453)

---

## 이 단원의 큰 그림 — 전제를 뒤집고, 흩어진 것을 다시 묶는다

```
ch11  Thread = OS 스레드 1:1, 비싸다 (암묵 전제)
ch13  "비싸니 풀로 재사용" — MyThreadPool: 고정 워커 N + 작업 큐
        ↓  전제를 뒤집는다
ch15  가상 스레드: 스레드가 싸다 → 풀 버리고 작업마다 새로 만든다 (thread-per-task)
        ↓  흩어진 작업의 수명을 다시 묶는다
      structured concurrency: fork → 전부 join → 첫 실패 시 형제 취소·전파
        ↓
ch19  concurrent-http-server: 연결마다 가상 스레드
```

API는 ch11의 `Thread`/`join`과 **똑같다** — 달라진 건 **생성 방식과 비용 모델**뿐이다.

---

## 가상 스레드 — 무엇이 달라졌나

- **경량**: OS 스레드가 아니라 JVM이 스케줄링하는 스레드. 스택은 힙의 작은 객체라 **수백만 개**도 가능.
- **마운트/언마운트**: 가상 스레드는 실행할 때만 **캐리어(플랫폼) 스레드**에 마운트되고, 블로킹 호출(I/O, `sleep`, `BlockingQueue.take`)을 만나면 **언마운트**되어 캐리어를 반납한다 → 그래서 **블로킹이 싸다**. 캐리어는 소수(기본 = CPU 코어 수), 가상 스레드는 다수(M:N).
- **풀링하지 마라**: 생성이 공짜이므로 재사용할 이유가 없다. `Executors.newVirtualThreadPerTaskExecutor()`는 **작업마다 새 스레드**를 만든다(ch13 고정 풀과 정반대). 동시성 제한이 필요하면 풀이 아니라 `Semaphore`.

> **흔한 오해**:
> - ❌ "가상 스레드는 더 빠르다" — 개별 작업의 **지연(latency)**은 안 빨라진다. 같은 자원으로 더 많은 동시 작업을 처리하는 **처리량(throughput)**용이다.
> - ❌ "CPU 바운드에 유리" — CPU 바운드는 코어 수가 한계라 도움 안 된다. **블로킹·I/O 바운드**(수많은 대기) 워크로드용.
> - ❌ "성능 위해 풀링" — 풀에 가두면 이점이 사라진다.

> **pinning 함정**: 가상 스레드가 `synchronized` 블록 안에서 **블로킹**하면 캐리어에 **고정(pinned)**되어 언마운트 못 한다 → 경량성 붕괴. 블로킹 구간을 감싸는 `synchronized`는 ch13의 `ReentrantLock`으로 바꾼다. (Java 21 기준 — 짧은 비블로킹 임계구역은 무해.)

---

## Structured Concurrency — 직접 구현한다

여러 작업을 가상 스레드로 흩뿌리면(fire-and-forget) **고아 작업·누수**가 생긴다. 구조적 동시성은 **자식 작업의 수명을 한 블록에 묶는다**: 부모가 `join()`까지 전부 기다리고, 블록을 벗어나기 전 모두 끝남을 보장한다.

> 표준 `java.util.concurrent.StructuredTaskScope`는 Java 21 **프리뷰**다 — `--enable-preview`가 필요하고 버전마다 시그니처가 바뀐다(빌드 취약). 그래서 이 단원은 그것을 쓰지 않고, **핵심 의미론을 가상 스레드 위에 `MyTaskScope`로 직접 구현**한다(ch13 `MyThreadPool`, ch14 `MyPromise`와 같은 "내부 직접 구현" 방식). 실무에선 표준 API를 쓰되 그 내부가 이렇게 생겼음을 안다.

**shutdown-on-failure 의미론**: fork한 작업 중 **하나라도 실패**하면 → 나머지 형제를 `interrupt()`로 **취소**하고 → `throwIfFailed()`가 **첫 예외**를 전파한다. `AutoCloseable`이라 try-with-resources로 쓰며, `close()`가 누수를 막는다.

ch14 `MyPromise`(값 완료 | 예외 완료, 단 한 번)의 모델을 작업 단위로, ch14 `allOf`(전부 대기)에 **취소·수명 묶기**를 더한 것이다.

---

## 채점 — 가상 스레드를 결정적으로 (ch11~12 원칙 계승)

비결정적인 건 단언하지 않는다.
- **결정적 채점**: `isVirtual()==true`, "N개 전부 완료(카운터==N)", 결과 리스트 순서·값, 실패 전파(`ExecutionException.getCause()`), 취소를 **`CountDownLatch`로 관측**(느린 형제가 인터럽트로 끝났는지).
- **채점 안 함(데모/이론)**: 실행 속도·처리량·"가상이 더 빠름"·동시 마운트 수·캐리어 수 — 전부 환경 의존(flaky). 경량성은 "1만 개가 OOM 없이 전부 완료"로만.
- 취소가 깨진 구현은 느린 형제가 안 끝나 `@Timeout`으로 잡힌다.

---

## 연습 문제

> 권장 순서: **VirtualThreads → MyTaskScope → ParallelFanOut.**

### VirtualThreads (4문제) — 가상 스레드 기초

| 메서드 | 핵심 |
|---|---|
| `runAll(tasks)` | 작업마다 가상 스레드 + 전부 join (InterruptedException 래핑) |
| `runManyVirtual(count, body)` | N개 띄워 전부 완료 (경량성 — 카운터==N) |
| `runningOnVirtualThread()` | `Thread.currentThread().isVirtual()` |
| `newNamedVirtual(name, task)` | `ofVirtual().name().unstarted()` (미시작) |

### MyTaskScope (간판) — structured concurrency 미니 스코프

| 메서드 | 핵심 |
|---|---|
| `fork(Callable)` | 가상 스레드로 시작 → `Subtask` 반환, 실패 시 형제 취소 |
| `join()` | 모든 하위 작업 종료까지 대기 |
| `throwIfFailed()` | 첫 예외를 `ExecutionException`으로 전파 |
| `close()` | try-with-resources 정리(누수 방지, idempotent) |
| `Subtask.state/get/exception` | 완료 상태·결과·예외 |

### ParallelFanOut (1문제) — 순서 보존 병렬 map

| 메서드 | 핵심 |
|---|---|
| `parallelMap(inputs, mapper)` | `MyTaskScope`로 fan-out, 입력 순서 보존, all-or-nothing |

---

## 실행

```sh
# 전체 테스트 (스텁이 비어 있어 빨간불 — 구현하며 초록불로)
./gradlew :chapter15-virtual-thread:test

# 특정 클래스
./gradlew :chapter15-virtual-thread:test --tests "study.chapter15.MyTaskScopeTest"
```
