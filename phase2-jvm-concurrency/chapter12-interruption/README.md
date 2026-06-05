# Chapter 12 — 인터럽트와 협력적 취소 (interrupt, InterruptedException, 상태 비트)

> **선행 단원**: Chapter 11(스레드 기초). ch11은 `volatile` 정지 플래그(`StoppableWorker`)로 협력적 종료를 다뤘다 — 다른 스레드가 `boolean`을 `true`로 바꾸면 작업 루프가 그것을 보고 멈췄다. 이 단원은 그 직접 만든 플래그를 **JVM이 모든 스레드에 *내장*해 둔 신호 메커니즘인 인터럽트로 승격**한다. `volatile` 플래그가 못 하던 일 — `sleep`/`wait`/`join`/`BlockingQueue.take`처럼 **블로킹된 스레드를 깨우는** 일 — 을 인터럽트가 한다.

> **공식 문서**: [`Thread.interrupt()`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html#interrupt()) · [`Thread.isInterrupted()`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html#isInterrupted()) · [`Thread.interrupted()`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html#interrupted()) · [`InterruptedException`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/InterruptedException.html)

---

## 이 단원의 큰 그림 — 정지 플래그를 JVM에 위임한다

ch11에서 정지는 이렇게 생겼었다:

```java
private volatile boolean stop = false;
public void requestStop() { stop = true; }
public void run() { while (!stop) { /* 일한다 */ } }
```

이게 동작하긴 한다. 하지만 두 가지 한계가 있다.

1. **내가 직접 플래그를 들고 다녀야 한다.** 클래스마다 `volatile boolean`을 선언하고, 게터/세터를 만들고, 게시(publication)를 신경 써야 한다.
2. **블로킹된 스레드를 못 깨운다.** 워커가 `while (!stop)`을 *폴링*하는 게 아니라 `sleep(60_000)`이나 `queue.take()`에서 **잠들어 있으면**, `stop = true`를 봐 줄 코드 자체가 안 돈다. 플래그는 영영 안 읽힌다.

JVM은 이 둘을 한 번에 해결할 장치를 **모든 `Thread`에 이미 박아 두었다** — **인터럽트 상태 비트(interrupt status)** 한 개와, 그 비트를 다루는 메서드들이다.

```
ch11 volatile 플래그            →  ch12 인터럽트
─────────────────────────────────────────────────────────────
내가 선언한 boolean             →  모든 Thread에 내장된 비트 한 개
stop = true                     →  thread.interrupt()
while (!stop) (폴링만)          →  while (!isInterrupted()) (폴링) + 블로킹 호출도 깨어남
(블로킹 호출은 못 깨움)          →  sleep/wait/join/take가 InterruptedException으로 즉시 깨어남
```

> **핵심 한 줄**: 인터럽트는 ch11의 정지 플래그와 **똑같이 협력적**이다(상대가 응답해야 멈춘다). 차이는 단 하나 — **블로킹 호출까지 깨운다**는 것. 강제 종료가 아니다.

이 단원에서 다루지 않는 것(경계):

- **고수준 종료 도구** — `ExecutorService.shutdownNow()`(인터럽트를 워커에 뿌린다), `Future.cancel(true)`는 **ch13/ch14**에서. 이 단원은 날것의 인터럽트 비트로 먼저 근육을 만든다.
- **인터럽트로 못 깨우는 블로킹** — 소켓 `read()`, `InputStream.read()`는 인터럽트에 반응하지 않는다(아래 함정 박스 참고). 그 우회책(`soTimeout`, 채널 닫기)은 **ch16(io-basics)** 에서.

---

## 인터럽트 상태 비트: 세 메서드의 정확한 구분

모든 스레드는 **`boolean` 한 개**(인터럽트 상태 비트)를 갖는다. 처음엔 `false`. 헷갈리는 모든 버그는 "어느 메서드가 이 비트를 *읽기만* 하고 어느 것이 *지우는지*"를 안 외워서 생긴다.

| 메서드 | 종류 | 비트를 세우나? | 비트를 읽나? | **비트를 지우나?** |
|---|---|---|---|---|
| `t.interrupt()` | 인스턴스 | **예 (true로)** | — | 아니오 |
| `t.isInterrupted()` | 인스턴스 | 아니오 | 예 | **아니오 (그대로 둔다)** |
| `Thread.interrupted()` | **static** | 아니오 | 예 | **예 (읽고 false로 지운다)** |

세 줄로 외운다:

```java
thread.interrupt();              // "신호를 보낸다" — 대상 스레드의 비트를 true로 세운다
thread.isInterrupted();          // "엿본다"       — 비트를 읽되 건드리지 않는다
Thread.interrupted();            // "읽고 비운다"  — 현재 스레드의 비트를 읽고 false로 만든다
```

> **함정 1 — `interrupted()`는 static이고, 호출하면 비트가 사라진다.** `Thread.interrupted()`는 *항상 현재 실행 중인 스레드*를 대상으로 하며, 호출 한 번으로 비트를 **소비(읽고 지움)** 한다. 그래서 `if (Thread.interrupted()) { ... }`를 무심코 두 번 부르면 두 번째는 무조건 `false`다. "확인만" 하고 싶으면 `isInterrupted()`(인스턴스, 안 지움)를 써라. "확인하고 처리하면서 비트는 비우고 싶을" 때만 `Thread.interrupted()`(static, 지움)다.

```java
// 틀린 형태: 같은 비트를 두 번 본다고 생각하지만, 첫 호출이 이미 지운다
if (Thread.interrupted()) log("인터럽트됨");
if (Thread.interrupted()) cleanup();   // 여기선 항상 false — cleanup 영영 안 됨

// 옳은 형태: 한 번 소비해서 변수에 담거나, 안 지우는 isInterrupted()로 엿본다
boolean wasInterrupted = Thread.interrupted();   // 한 번만 소비
// 또는
if (Thread.currentThread().isInterrupted()) { ... }   // 몇 번을 봐도 비트는 그대로
```

---

## `InterruptedException`은 비트를 끈다 — 그래서 "삼킬 거면 복원"

이게 이 단원의 정점 교훈이고, 동시성 단원 곳곳에 흩어진 `Thread.currentThread().interrupt()` 한 줄의 정체다.

`sleep`/`wait`/`join`/`BlockingQueue.take` 같은 **블로킹 메서드는, 자기가 블록된 동안 인터럽트 비트가 세워지면**:

1. 즉시 깨어나서 `InterruptedException`을 던지고,
2. **그러면서 인터럽트 비트를 다시 `false`로 지운다.**

2번이 직관에 반한다. "방금 인터럽트됐는데, catch 블록에 들어와 비트를 보면 `false`"인 것이다.

```java
try {
    Thread.sleep(10_000);
} catch (InterruptedException e) {
    // 여기서 Thread.currentThread().isInterrupted() 는 false 다!
    //   ← sleep이 예외를 던지면서 비트를 이미 꺼 버렸다
}
```

왜 끄는가? 예외 *자체*가 이미 "인터럽트됐다"는 정보를 한 번 전달했기 때문이다. 비트와 예외로 두 번 알리지 않는다는 설계다. 문제는 — **이 예외를 잡아서 그냥 삼키면, "나는 인터럽트됐다"는 사실이 비트에서도 예외에서도 사라진다.** 그러면 이 스레드를 호출한 바깥쪽(상위 루프, executor, `join`으로 기다리는 부모)은 취소가 요청됐다는 걸 알 길이 없다.

그래서 **인터럽트를 그 자리에서 끝까지 처리하지 않고 삼킬 거라면, 비트를 다시 세워서 정보를 보존**해야 한다:

```java
try {
    blockingCall();
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();   // 복원: 비트를 다시 true로 → 바깥 루프가 알 수 있다
    // (필요하면 정리 후 return — 스레드를 자연스럽게 끝낸다)
}
```

두 가지 올바른 대응만 기억하면 된다:

- **(A) 전파**: `InterruptedException`을 `throws`로 그대로 위로 던진다 — 호출자가 결정하게 한다(가장 정직).
- **(B) 복원 후 종료**: 못 던지는 자리(`Runnable.run()`은 checked 예외를 못 던진다)에선 잡되, **`Thread.currentThread().interrupt()`로 비트를 복원**하고 루프를 빠져나간다.

> **함정 2 — 인터럽트를 *조용히 삼키지* 마라.** `catch (InterruptedException e) {}` (빈 블록)이나 `e.printStackTrace()`만 하고 넘어가는 코드는, 취소 신호를 먹어 치워 스레드가 멈추지 않게 만드는 동시성 버그의 단골이다. 삼킬 거면 복원이 원칙이다.

---

## 협력적 취소: 인터럽트는 강제가 아니라 *신호*

`thread.interrupt()`는 대상 스레드를 **죽이지 않는다.** 비트 하나를 세울 뿐이다. 대상이 멈추려면 둘 중 하나여야 한다:

1. **인터럽트 가능 지점에서 블로킹 중**이다 → `sleep`/`wait`/`join`/`take`가 `InterruptedException`을 던지며 깨어난다. (코드가 이 예외를 받아 종료해야 한다.)
2. **블로킹 없이 도는 폴링 루프**다 → 코드가 직접 `isInterrupted()`를 **확인해서** 빠져나가야 한다.

```java
// 폴링 루프: CPU를 계속 쓰며 도는 작업은, 내가 직접 비트를 봐야 멈춘다
public void run() {
    while (!Thread.currentThread().isInterrupted()) {
        doOneStep();      // 블로킹 없음 → 인터럽트는 예외를 던질 기회가 없다
    }
    // 비트가 true가 되면 루프 탈출 → run 끝 → 스레드 종료
}
```

폴링 루프에서 비트를 **한 번도 안 보면**, 아무리 `interrupt()`를 불러도 영영 안 멈춘다. 인터럽트가 "신호일 뿐"이라는 말의 의미가 이것이다 — **응답할 코드가 있어야 협력이 성립**한다(ch11의 `while (!stop)`과 정확히 같은 책임).

> **함정 3 — 인터럽트로 못 깨우는 블로킹이 있다.** `Socket.getInputStream().read()`, `InputStream.read()`, `synchronized` 락 획득 대기는 **인터럽트에 반응하지 않는다.** 비트는 세워지지만 `read()`는 계속 블록된 채다. 이걸 깨우려면 다른 수단 — 소켓 `setSoTimeout()`으로 주기적 타임아웃을 주거나, 다른 스레드에서 소켓/채널을 `close()`하는 것 — 이 필요하다. 이 우회책은 **ch16(io-basics)** 에서 다룬다. 여기서는 "`sleep`/`wait`/`join`/`take`는 깨어나지만, 모든 블로킹이 그런 건 아니다"만 기억하면 된다.

---

## 취소 전파: 왜 `Thread.stop()`이 아니라 `interrupt()`인가

부모(또는 그룹/풀)가 여러 자식 워커를 한꺼번에 멈추고 싶을 때, 자식마다 `worker.interrupt()`를 호출해 비트를 세우고, 각 워커가 자기 루프에서 협력적으로 빠져나오게 한다. 이것이 ch13의 `shutdownNow()`, ch15의 형제 취소, ch19의 서버 종료가 전부 딛고 서는 토대다.

옛 API에는 `Thread.stop()`이 있었지만 **폐기(deprecated)** 됐다. 이유는 그것이 *강제 종료*이기 때문이다:

- `stop()`은 대상 스레드를 **임의의 지점에서 즉시 죽인다.** 객체를 절반만 갱신한 상태, 락을 잡은 채 임계영역 한복판일 수 있다.
- 그러면 잡고 있던 **모니터 락이 그대로 풀려** 다른 스레드가 깨진(불변식이 무너진) 객체를 보게 된다 → 복구 불가능한 데이터 손상.

`interrupt()`는 반대로 **신호만 보내고, *언제 어떻게* 멈출지는 대상이 자기 안전한 지점에서 결정**한다. 워커는 임계영역을 빠져나오고, 정리하고, 일관된 상태로 종료할 수 있다. "협력적"이라는 제약이 곧 **안전성**이다.

---

## `volatile` 플래그(ch11) vs 인터럽트(ch12) — 언제 무엇을

둘 다 **협력적**이다(상대가 응답해야 멈춘다). 둘 다 비트/플래그를 폴링 루프에서 봐야 한다. 결정적 차이는 하나다.

| | ch11 `volatile boolean stop` | ch12 인터럽트 |
|---|---|---|
| 신호 보관 | 내가 선언한 필드 | 모든 `Thread`에 내장된 비트 |
| 신호 보내기 | `stop = true` | `t.interrupt()` |
| 폴링 루프 종료 | `while (!stop)` | `while (!isInterrupted())` |
| **블로킹 호출 깨우기** | **못 한다** (잠든 채 방치) | **한다** (`sleep`/`wait`/`join`/`take` → `InterruptedException`) |
| 표준성 | 클래스마다 제각각 | JVM·라이브러리 공통 관용구 |

> 정리: **블로킹 호출이 전혀 없는 순수 CPU 폴링 루프**라면 `volatile` 플래그로도 충분하다(ch11이 옳았다). 하지만 작업이 어디선가 **블록될 수 있다면**(거의 모든 실전 코드) 인터럽트가 정답이다 — 잠든 스레드까지 깨우는 건 인터럽트뿐이고, 게다가 표준 관용구라 라이브러리(`Executor`, `Future`, `BlockingQueue`)와 결이 맞는다.

---

## 연습 문제

> 권장 순서: `InterruptStatus`(비트 3메서드의 차이를 손으로 확인) → `InterruptibleCounter`(블로킹 없는 폴링 루프) → `InterruptibleBlocker`(블로킹 + `InterruptedException`이 비트를 끄는 것·복원 관용구) → `CancellableGroup`(여러 워커 취소 전파). 비트 자체 → 폴링 → 블로킹/복원 → 전파 순으로 난이도가 단조 증가한다(위 다이어그램 흐름과 동일).

패키지 `study.chapter12`. **생성자·접근자는 완성 제공**, 핵심 메서드만 빈 구현이다.

### InterruptStatus (static 메서드) — 비트 3메서드의 정확한 의미 · 채점

세 메서드의 "읽기/지우기" 차이를 코드로 박제한다. 가장 작지만 가장 헷갈리는 단원이다.

| 메서드 | 핵심 |
|---|---|
| `drain()` | `Thread.interrupted()` 위임 — 현재 스레드 비트를 **읽고 지운다**(static, 소비형) |
| `isSet()` | `Thread.currentThread().isInterrupted()` 위임 — 비트를 **읽되 지우지 않는다** |
| — 테스트 | 현재 스레드를 `interrupt()`한 뒤: `isSet()`은 여러 번 불러도 계속 `true`, `drain()`은 첫 호출 `true`·둘째 호출부터 `false` |

### InterruptibleCounter (implements Runnable) — 블로킹 없는 협력 폴링 · 채점

블로킹 호출이 전혀 없는 작업 루프. **순수 폴링**이라 `isInterrupted()`를 직접 봐야만 멈춘다(인터럽트가 던질 예외가 없다).

| 멤버 | 핵심 |
|---|---|
| `run()` | `while (!Thread.currentThread().isInterrupted())` 동안 `count++` |
| (종료 시) | 루프를 빠져나오면 `exitedByInterrupt = true`로 표시 |
| `count` / `exitedByInterrupt` (접근자) | 증가 횟수 / 인터럽트로 끝났는지 |
| — 테스트 | 스레드 시작 → 잠깐 돌게 둠 → `interrupt()` → `join()` 후 종료했고 `exitedByInterrupt`가 `true` |

### InterruptibleBlocker (implements Runnable) — 블로킹 + 예외가 비트를 끈다 + 복원 · 채점

블로킹 호출(`sleep`)이 인터럽트로 깨어나고, 그 과정에서 **비트가 꺼지는 것**과 **복원 관용구**를 한 메서드 안에서 전부 관측한다. 이 단원의 정점.

| 멤버 | 핵심 |
|---|---|
| `run()` | `ready.countDown()`으로 "잠들 준비됨"을 알린 뒤 `Thread.sleep(MAX)` |
| (catch에서) | `InterruptedException`을 잡으면 `threwInterrupted = true` |
| | `flagClearedInCatch = isInterrupted()` 기록 — **예외가 껐으므로 `false`** 가 관측돼야 함 |
| | 그 다음 `Thread.currentThread().interrupt()`로 **복원** |
| | `flagAfterRestore = isInterrupted()` 기록 — 복원했으므로 `true` |
| `ready`(CountDownLatch) | 테스트가 "확실히 sleep에 들어간 뒤" interrupt하도록 동기화 |
| — 테스트 | `ready.await()`로 대기 진입 보장 → `interrupt()` → `join()` 후: `threwInterrupted==true`, `flagClearedInCatch==false`, `flagAfterRestore==true` |

> 왜 `ready` 래치가 필요한가? interrupt를 sleep에 *들어가기 전에* 쏘면, sleep이 진입하자마자 예외를 던지긴 하지만 타이밍이 제각각이라 테스트가 흔들린다(flaky). 래치로 "워커가 sleep 직전까지 왔다"를 보장한 뒤 신호를 보내면 결정적이 된다. (ch11 `CountDownLatch` 출발 배리어와 같은 발상.)

### CancellableGroup — 취소 전파 · 채점

부모가 여러 협력 워커를 한꺼번에 인터럽트로 취소한다. ch13 `shutdownNow`의 축소판.

| 메서드 | 핵심 |
|---|---|
| `startWorkers(n, allReady)` | `n`개 워커 스레드 시작. 각 워커는 `allReady.countDown()` 후 `while (!isInterrupted())` 폴링 루프, 빠져나오면 `stopped++` |
| `cancelAll()` | 시작한 워커 **전원에게 `interrupt()`** |
| `joinAll(ms)` | 전원을 `join(ms)` |
| `stoppedCount()` (접근자) | 협력적으로 종료한 워커 수 |
| — 테스트 | `n`개 시작 → `allReady.await()`로 전원 진입 보장 → `cancelAll()` → `joinAll()` 후 `stoppedCount() == n` |

> `stopped++`는 여러 워커 스레드가 동시에 건드린다 — ch11에서 배운 대로 단순 `int++`는 잃어버린 갱신이 날 수 있으니, 스텁의 카운터 타입(원자적 타입)을 그대로 쓰면 된다. 이 단원의 채점 포인트는 **취소 전파**이지 카운팅 자체가 아니다.

---

## 실행

```sh
# 전체 테스트 (스텁이 비어 있어 빨간불 — 구현하며 초록불로)
./gradlew :chapter12-interruption:test

# 특정 클래스
./gradlew :chapter12-interruption:test --tests "study.chapter12.InterruptibleBlockerTest"
```

---

## 생각해볼 거리

1. **catch에서 비트가 `false`인 이유.** `Thread.sleep`이 `InterruptedException`을 던졌다면, catch 블록 첫 줄에서 `Thread.currentThread().isInterrupted()`는 `true`일까 `false`일까? 왜 그렇게 설계됐을까? (힌트: 같은 정보를 비트와 예외로 *두 번* 알리지 않는다.)

2. **`interrupted()` vs `isInterrupted()`를 바꿔 쓰면.** `InterruptStatus.isSet()`을 `Thread.interrupted()`로 잘못 구현하면 어떤 테스트가, 왜 깨질까? "두 번째 호출부터 `false`"가 그 증상이다.

3. **삼킨 인터럽트의 대가.** `catch (InterruptedException e) {}` (빈 블록)으로 인터럽트를 먹어 버린 워커를, 그 워커를 `join()`으로 기다리던 부모 입장에서 어떻게 관측될까? 복원했을 때와 무엇이 달라지나?

4. **폴링 루프가 비트를 안 보면.** `InterruptibleCounter.run()`에서 `isInterrupted()` 확인을 빼고 `while (true)`로 두면, `interrupt()`를 아무리 보내도 왜 안 멈출까? `InterruptibleBlocker`(sleep으로 블록)와 무엇이 다른가?

5. **인터럽트로 못 깨우는 블로킹.** 워커가 `sleep` 대신 `socket.getInputStream().read()`에서 블록돼 있다면 `interrupt()`로 깨울 수 있을까? 못 깨운다면 그 스레드를 어떻게 멈출 수 있을까? (ch16 `soTimeout`/채널 `close` 예고.)

6. **`Thread.stop()`이 폐기된 이유.** 워커가 `synchronized` 블록 한복판(불변식이 잠깐 깨진 상태)에서 `stop()`으로 강제 종료되면 무슨 일이 벌어지나? `interrupt()`가 "협력적"이라는 제약이 왜 *안전성*인가?

7. **ch11과의 연결.** ch11 `StoppableWorker`의 `volatile boolean stop`을 인터럽트로 바꿔 쓰면 무엇이 좋아지고(블로킹 작업도 깰 수 있음), 순수 폴링 루프에선 무엇이 똑같은가? 언제까지는 `volatile` 플래그로 충분한가?
