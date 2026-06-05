# Chapter 12 — ExecutorService · 스레드풀 · BlockingQueue · Producer-Consumer

> **선행 단원**: Chapter 11(스레드 기초). ch11은 날것의 `Thread`로 **원자성·가시성**(synchronized/volatile/CAS)을 다뤘지만, "조건이 맞을 때까지 **기다리는**" 도구가 없었다(busy-wait는 답이 아니다). 이 단원은 ch11이 명시적으로 미뤄둔 고수준 동시성 도구 — `wait`/`notify`, `ReentrantLock`/`Condition`, `BlockingQueue`, 스레드풀 — 를 **직접 구현**한다.

> **공식 문서**: [`Object.wait`/`notify`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html#wait()) · [`ReentrantLock`/`Condition`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/locks/Condition.html) · [`BlockingQueue`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/BlockingQueue.html) · [`ExecutorService`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html)

---

## 이 단원의 큰 그림 — "조건부 대기"가 모든 것을 꿴다

ch11에서 `count++`가 원자성·가시성·CAS를 관통했듯, ch12은 **`take()`/`put()`의 조건 대기** 하나가 wait/notify·Condition·Producer-Consumer·스레드풀을 전부 꿴다.

```
저수준(ch11): synchronized로 안전하게 "바꾼다" — 하지만 "기다리는" 도구가 없다
  │
조건 대기 입문: wait/notify (synchronized 안에서 while(조건) wait → 바꾸고 notifyAll)
  │
[간판] BlockingQueue 직접 구현
  ├─ MyBlockingQueue       : synchronized + wait/notifyAll
  └─ LockedBlockingQueue   : ReentrantLock + Condition 둘(notFull/notEmpty)  ← "왜 synchronized로 부족한가"
  │
[부 간판] MyThreadPool : 위 BlockingQueue를 작업 큐로 재사용 (워커 N개 + graceful shutdown)
  │
종합: Producer-Consumer (생산자·소비자가 큐로 분리, 보존 불변식)
```

이 단원에서 다루지 않는 것(경계): **`CompletableFuture`·비동기 파이프라인 → ch13**, **Virtual Thread → ch14**, `Semaphore`/`CountDownLatch`/`CyclicBarrier`(동기화 보조 도구는 *사용*만, 주제 아님), `ForkJoinPool`/work-stealing, `ConcurrentHashMap` 등 동시 컬렉션. `Future`/`Callable`로 결과를 회수하는 것도 ch13로 미룬다 — 여기 스레드풀은 `Runnable`만 다룬다.

---

## 조건 대기 — `wait` / `notify`

ch11의 `synchronized`는 **상호배제**(누가 들어가나)만 했다 — 들어가면 즉시 일하고 나왔다. 이제 그 안에서 **기다린다**: 조건이 안 맞으면 `wait()`로 **모니터를 놓고 잠들었다가**, 다른 스레드의 `notify`로 깨어 **모니터를 되찾고 조건을 재확인**한다.

```java
synchronized (this) {
    while (조건이_안_맞음) wait();   // 락을 놓고 잠 — 그래서 남이 조건을 바꿔줄 수 있다
    // ... 조건이 맞을 때만 여기 도달 ...
    notifyAll();                     // 상태를 바꿨으니 기다리던 스레드를 깨운다
}
```

### 함정 1 — `if`가 아니라 반드시 `while`

```java
while (비었음) wait();   // O
if    (비었음) wait();   // X — 버그
```
두 가지 이유로 **깨어남 ≠ 조건이 참**이다: (1) **가짜 깨어남**(spurious wakeup) — JLS가 허용, 아무도 안 깨웠는데 깰 수 있다. (2) `notifyAll`로 여러 대기자가 깨면 먼저 깬 스레드가 유일한 자원을 가져가 나중 스레드는 조건이 다시 거짓이다. **깨어남은 "재확인하라"는 신호일 뿐.**

### 함정 2 — `notify`(하나) vs `notifyAll`(전부)

한 모니터에 생산자와 소비자가 **섞여서** 대기하면, `notify`는 아무나 하나를 깨우는데 하필 진행 불가한 쪽(소비자가 자리를 비웠는데 또 소비자)을 깨우면 신호를 잃고 멈춘다(lost wakeup). **단일 모니터에선 `notifyAll`이 안전.** (이 비효율을 다음 절의 Condition 둘이 없앤다.)

> `wait`/`notify`/`notifyAll`은 반드시 그 객체의 **모니터를 쥔 상태**(`synchronized` 안)에서만 호출 — 아니면 `IllegalMonitorStateException`.

---

## `ReentrantLock` / `Condition` — 대기 줄을 나눈다

`synchronized`를 객체로 외부화한 것이 `ReentrantLock`이고, "이 락에 묶인 대기 줄"이 `Condition`이다. 1:1 대응:

| synchronized | ReentrantLock |
|---|---|
| `synchronized(o){ ... }` | `lock.lock(); try { ... } finally { lock.unlock(); }` |
| `wait()` | `condition.await()` |
| `notifyAll()` | `condition.signalAll()` |

**핵심 이득 — 조건을 둘로 나눈다.** 한 락에 `Condition`을 여러 개 걸 수 있다. 자리가 없어 막힌 **생산자는 `notFull`**에서, 원소가 없어 막힌 **소비자는 `notEmpty`**에서 따로 잔다. 원소를 하나 넣은 뒤엔 `notEmpty.signal()`로 **소비자만 콕 집어** 깨운다 — `notifyAll`로 무관한 생산자까지 깨우던 낭비가 사라진다. **이것이 bounded queue에서 `ReentrantLock`을 쓰는 결정적 이유.**

주의: `await()`도 `while` 가드 필수. `unlock()`은 반드시 `finally`(예외 나도 락 해제). `synchronized`에는 없고 `ReentrantLock`에만 있는 능력: `tryLock()`/시한부/`lockInterruptibly()`/공정성/다중 Condition.

---

## 직접 구현 — bounded BlockingQueue (간판)

용량이 찬 `put`은 자리가 날 때까지, 빈 `take`는 원소가 들어올 때까지 **블록**되는 큐. 같은 계약을 **두 방식**으로 구현해 대조한다:

- `MyBlockingQueue` — `synchronized` + `wait`/`notifyAll`
- `LockedBlockingQueue` — `ReentrantLock` + `notFull`/`notEmpty` 두 Condition (+ 시한부 `offer`)

> **표준 `BlockingQueue`의 4쌍 경계** (직접 구현은 블로킹 `put`/`take`에 집중): 가득/빈 상황에서 — 예외(`add`/`remove`) · 특수값(`offer` false / `poll` null) · **블록(`put`/`take`)** · 시한부(`offer(e,t,u)`/`poll(t,u)`). `add`가 가득 차면 예외이고 `put`은 블록인 점을 혼동하지 말 것.

---

## 미니 스레드풀 (부 간판)

고정 워커 N개가 **작업 큐**(위 BlockingQueue 재사용)에서 `Runnable`을 꺼내 실행한다. 스레드를 작업마다 새로 만들지 않고 **재사용**하는 게 본질. 큐가 비면 워커는 `take()`에서 자연스럽게 잠든다(busy-wait 아님).

**graceful shutdown**: `shutdown()`은 새 작업만 거부하고 큐의 잔여 작업은 다 처리한 뒤 종료한다. 기법은 **poison pill**(센티넬 작업)을 워커 수만큼 큐에 넣어 각 워커가 그걸 꺼내면 루프를 빠져나오게 하는 것(FIFO라 진짜 작업이 먼저 소비됨). ch11 `StoppableWorker`의 협력적 취소가 "큐를 통한" 종료로 확장된다.

> 스레드풀 크기: **CPU 바운드**는 코어 수 정도, **IO 바운드**는 그보다 넉넉히(대기 시간만큼).

---

## 왜 동시성 채점이 까다로운가 (ch11 원칙 계승)

비결정적인 것은 채점하지 않는다. **올바른 구현이 수렴하는 불변식**만 단언한다:
- **보존 불변식**: N 생산자가 넣은 원소 멀티셋 == K 소비자가 받은 멀티셋(손실·중복 0). 인터리빙과 무관하게 항상 성립.
- **"블록됐다"의 결정적 관측**: `Thread.sleep`은 flaky다. 대신 `CountDownLatch`로 "스레드가 출발했다"를 확인하고, `latch.await(50ms) == false`로 "아직 안 깼다(=블록 중)"를 단언한다 — 올바른 구현이면 put이 오기 전엔 절대 안 깨므로 이 단언은 **항상 참**(flaky red 불가). `@Timeout`으로 데드락을 실패로 전환.
- **스레드풀 "정확히 한 번"**: 제출 수 == 실행 수(`@RepeatedTest`). 워커 재사용은 "서로 다른 실행 스레드 수 ≤ 워커 수"로 검증(결정적). "진짜 병렬 동시 실행"은 스케줄러 의존이라 **채점하지 않는다**.

---

## 연습 문제

> 권장 순서: **MyBlockingQueue → LockedBlockingQueue → MyThreadPool.**

### MyBlockingQueue (5문제) — synchronized + wait/notifyAll · 간판

| 메서드 | 핵심 |
|---|---|
| 생성자 | capacity 검증 + 버퍼 초기화 |
| `put` | 가득 차면 `while wait`, 넣고 `notifyAll` (null NPE) |
| `take` | 비면 `while wait`, 꺼내고 `notifyAll` |
| `size` / `capacity` | 보호된 스냅샷 / 용량 |

### LockedBlockingQueue (6문제) — ReentrantLock + 두 Condition

| 메서드 | 핵심 |
|---|---|
| 생성자 / `put` / `take` | `lock`/`try-finally` + `notFull`/`notEmpty` await·signal |
| `offer(item, timeout, unit)` | `awaitNanos`로 남은 시간 추적, 시한부 시도 |
| `size` / `capacity` | |

### MyThreadPool (6문제) — 미니 고정 스레드풀 · 부 간판

| 메서드 | 핵심 |
|---|---|
| 생성자 | 작업 큐 + 워커 N개 생성·start (take→run 반복) |
| `submit` | 작업 큐에 넣기 (shutdown 후엔 `RejectedExecutionException`) |
| `shutdown` | graceful — poison pill로 잔여 작업 처리 후 종료 |
| `awaitTermination` | 워커 join 대기 |
| `isShutdown` / `isTerminated` | 종료 요청 / 완전 종료 여부 |

---

## 실행

```sh
# 전체 테스트 (스텁이 비어 있어 빨간불 — 구현하며 초록불로)
./gradlew :chapter12-executor-blocking-queue:test

# 특정 클래스
./gradlew :chapter12-executor-blocking-queue:test --tests "study.chapter12.MyBlockingQueueTest"
```
