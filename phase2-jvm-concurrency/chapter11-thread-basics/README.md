# Chapter 11 — 스레드 기초 (Thread, synchronized, volatile, Atomic)

> **선행 단원**: Chapter 08(JVM 메모리 모델). ch08은 **왜** 멀티스레드에서 값이 안 보이고 순서가 뒤집히는지(가시성·재배치·happens-before)를 **이론과 데모로** 설명했다. 이 단원은 그 약속을 이행한다 — ch08이 "데모로만 보여주고 미뤄둔" **올바른 게시(publication)와 동기화를 직접 *구현*하고, 결정적으로 *검증*한다.**

> **공식 문서**: [The Java Tutorials — Concurrency](https://docs.oracle.com/javase/tutorial/essential/concurrency/) · [`java.util.concurrent.atomic` 패키지](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/package-summary.html) · [JLS 17.4 — Memory Model](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html#jls-17.4)

---

## 이 단원의 큰 그림

ch08은 "보장이 없으면 위험하다"는 **이유**에서 끝났다. ch11은 그 위에서 다음 흐름을 따른다:

```
체험: 보호 없이 짜면 실제로 깨진다   (UnsafeCounter — 값을 잃는다)
  │
진단: 왜 깨지나?                    원자성(atomicity) + 가시성(visibility)
  │
해결: 도구로 고친다
  ├─ synchronized  → 상호배제 + 가시성 (SafeCounter)
  ├─ volatile      → 가시성만 (StoppableWorker)
  └─ Atomic / CAS  → 락 없는 원자적 갱신 (AtomicAccount)
```

이 단원에서 다루지 않는 것(경계):
- **고수준 도구** — `ExecutorService`, 스레드풀, `BlockingQueue`, Producer-Consumer, `wait`/`notify`, `ReentrantLock`은 **ch13**에서. ch11은 날것의 `Thread`로 먼저 근육을 만든다.
- **`Thread.start()`/`join()`의 happens-before 이론** — ch08에서 이미 다뤘다. 여기서는 그 규칙에 *의존하는 코드를 짠다*.

---

## 두 개의 다른 문제: 원자성 vs 가시성

초보자가 가장 자주 섞는 두 개념이다. **서로 다른 문제**이고, **서로 다른 도구**로 푼다.

### 1. 원자성(atomicity) — "연산이 쪼개진다"

`count++`는 한 줄이지만 실제로는 세 단계다:

```
1. read   : 메모리에서 count 값을 읽는다     (예: 41)
2. modify : 1을 더한다                        (42)
3. write  : 결과를 메모리에 쓴다              (count = 42)
```

두 스레드가 이 세 단계를 겹쳐 실행하면:

```
Thread A: read(41) ──────────────── write(42)
Thread B:        read(41) ── write(42)
                                          → 두 번 증가했는데 결과는 42 (한 번이 사라짐!)
```

이것이 **잃어버린 갱신(lost update)**이다. N개 스레드가 각각 M번 증가시켜도 최종값이 N×M보다 **작게** 나온다.

> 해결: read-modify-write 세 단계를 **하나의 분리 불가능한 단위**로 묶는다 → `synchronized` 또는 `Atomic`.

### 2. 가시성(visibility) — "변경이 안 보인다"

한 스레드가 변수를 바꿔도, 다른 스레드는 자기 CPU 캐시에 든 **옛 값**을 계속 볼 수 있다(ch08 "Working Memory" 그림). 대표 증상은 **멈추지 않는 루프**다:

```java
boolean stop = false;          // volatile 아님

// Worker 스레드
while (!stop) { /* 일한다 */ }   // stop이 true가 돼도 영영 못 볼 수 있다 → 무한 루프

// Main 스레드
stop = true;                    // 변경이 Worker에게 전파된다는 보장이 없다
```

> 해결: 변경을 **즉시 보이게** 만든다 → `volatile`(또는 `synchronized`).

**핵심**: `synchronized`는 원자성과 가시성을 *둘 다* 준다. `volatile`은 **가시성만** 준다 — 그래서 `volatile`만으로는 `count++`를 고칠 수 없다(아래에서 다시 강조).

---

## 도구 1 — `synchronized`

한 객체의 **모니터(monitor)** 락을 잡아, 임계영역(critical section)에 **한 번에 한 스레드만** 들어가게 한다(상호배제, mutual exclusion).

```java
// (a) synchronized 메서드 — 락 객체는 this
public synchronized void increment() {
    count++;
}

// (b) synchronized 블록 — 락 객체를 명시
public void increment() {
    synchronized (this) {     // 위 (a)와 똑같이 this 모니터
        count++;
    }
}
```

- 같은 모니터에 대해 **unlock → lock**은 happens-before 관계다(ch08 규칙 2). 그래서 블록을 나갈 때 쓴 값이 다음에 들어오는 스레드에게 **보장되어 보인다** → 가시성도 해결.
- **읽기도 보호해야 한다.** 쓰기만 `synchronized`로 감싸고 `get()`을 보호하지 않으면, 읽는 쪽이 옛 값을 볼 수 있다.

### 흔한 함정: 서로 다른 락

상호배제는 **같은 모니터**를 잡을 때만 성립한다. 두 코드가 다른 락 객체를 잡으면 서로를 전혀 막지 못한다.

```java
synchronized (lockA) { count++; }   // 이 둘은
synchronized (lockB) { count++; }   // 서로 배타적이지 않다 → 여전히 레이스!
```

> 인스턴스 메서드에 붙인 `synchronized`의 락은 항상 `this`다. 메서드 방식과 `synchronized(this)` 블록 방식이 서로 배타적인 이유가 이것이다 — 둘 다 `this` 모니터를 공유한다.

---

## 도구 2 — `volatile`

변수를 `volatile`로 선언하면:

1. **가시성 보장** — 한 스레드의 쓰기가 다른 스레드에 즉시 보인다(항상 메인 메모리에서 읽고 쓴다).
2. **재배치 제한** — `volatile` 쓰기 이전의 명령이 그 뒤로 재배치되지 않는다(ch08 규칙 1).

```java
private volatile boolean stop = false;

public void requestStop() { stop = true; }     // 다른 스레드가 즉시 본다
public void run() { while (!stop) { /* ... */ } }  // 이제 반드시 멈춘다
```

### `volatile`의 결정적 한계

`volatile`은 **가시성만** 준다. **복합 연산의 원자성은 주지 않는다.**

```java
private volatile int count;
count++;     // 여전히 read-modify-write 3단계 → volatile이어도 갱신 손실 발생!
```

이것이 이 단원의 정점 교훈이다: **정지 플래그(단순 쓰기)는 `volatile`로 충분하지만, 카운터(read-modify-write)는 `volatile`로 못 고친다.** 후자는 `synchronized`나 `Atomic`이 필요하다.

---

## 도구 3 — `Atomic`과 CAS

`java.util.concurrent.atomic`의 `AtomicInteger`/`AtomicLong`/`AtomicReference`는 **락 없이(lock-free)** 원자적 갱신을 제공한다. 바탕은 하드웨어의 **CAS(Compare-And-Swap)** 명령이다:

> "메모리의 값이 내가 방금 읽은 값(expected)과 같으면, 새 값(next)으로 바꾸고 성공을 알려라. 그 사이 누군가 바꿨으면(달라졌으면) 아무것도 안 하고 실패를 알려라."

단순 누적은 한 줄이다:

```java
AtomicLong count = new AtomicLong();
count.incrementAndGet();      // 원자적 +1, 내부적으로 CAS 재시도
count.addAndGet(5);
```

**조건부 갱신**(예: "잔액이 충분할 때만 출금")은 직접 **CAS 재시도 루프**를 짜야 한다:

```java
while (true) {
    long current = balance.get();        // 1. 읽고
    if (current < amount) return false;  // 2. 조건 확인
    long next = current - amount;        // 3. 새 값 계산
    if (balance.compareAndSet(current, next)) return true;  // 4. "그대로면 바꿔라"
    // 실패 = 그 사이 다른 스레드가 balance를 바꿈 → 루프 처음으로, 다시 읽고 재시도
}
```

`get()` 후 `set()`으로 나누면 그 틈에 다른 스레드가 끼어드는 **check-then-act 경쟁조건**이 생긴다(잔액이 음수로 내려갈 수 있다). CAS는 "내가 읽은 값이 그대로일 때만" 갱신하므로 이 틈을 닫는다.

`synchronized` 카운터와 `Atomic` 카운터는 **같은 문제를 다른 방식으로** 푼다 — 전자는 락, 후자는 낙관적 재시도. 경합이 심하지 않을 때 `Atomic`이 보통 더 빠르다.

> **ABA 문제**: CAS는 "값이 **그대로**면 성공"이라 판단한다. 그런데 값이 `A → B → A`로 바뀌었다 돌아오면 CAS는 "안 바뀌었다"고 착각해 성공해버린다. 숫자 카운터처럼 값 자체만 의미 있을 땐 무해하지만, **참조(노드 포인터 등)**를 CAS할 때는 "그 사이 다른 일이 있었다"는 사실을 놓쳐 버그가 된다(lock-free 스택의 고전적 함정). 해법은 값에 **버전(스탬프)**을 붙여 같이 비교하는 `AtomicStampedReference`(또는 `AtomicMarkableReference`) — "값도 같고 스탬프도 같을 때만" 성공.

---

## 왜 동시성 테스트가 까다로운가 — 그리고 이 단원의 채점 원칙

ch08의 원칙을 그대로 따른다: **잘못된 구현이 초록불이 되거나, 올바른 구현이 빨간불이 되는(flaky) 테스트는 학습에 해롭다.** 동시성은 비결정적이라 이게 진짜 어렵다. 핵심 통찰:

> 동시성 테스트는 "레이스를 재현"하는 게 아니라 **"올바른 구현의 결과가 수렴하는 불변식(invariant)을 단언"**하는 것이다.

- **카운터/계좌(채점)**: 올바른 동기화면 N×M 증가의 결과는 인터리빙과 무관하게 **항상 N×M 하나**다. 그래서 고경합(스레드 16 × 반복 1만) + 출발 배리어(`CountDownLatch`) + 반복 실행(`@RepeatedTest`)으로 단언하면, **올바른 구현은 항상 통과**하고 틀린 구현은 높은 확률로 깨진다. (틀린 구현을 *가끔* 놓치는 건 받아들인다 — 학습자는 올바르게 구현하려 하므로.)
- **정지 플래그(채점은 "멈춘다"만)**: `@Timeout`으로 "정지 신호를 받으면 종료한다"는 **기능**을 채점한다. "`volatile`이 *없으면* 안 멈춘다"는 x86에서 재현이 보장되지 않으므로 채점하지 않고, ch08의 `VisibilityDemo`(실행 관찰)로 미룬다.
- **레이스 *재현*(데모)**: "비동기화 카운터가 값을 잃는다"는 비결정적이라 단언하면 그 자체가 flaky다. 그래서 ch08의 GC 패턴처럼 **손실이 관측된 경우에만 단언하고, 아니면 `assumeTrue`로 skip**한다(`UnsafeCounterDemoTest`).

---

## 연습 문제

> 권장 순서: `UnsafeCounter`를 먼저 실행해 "왜 깨지는지" 본 뒤 → `SafeCounter`(synchronized) → `StoppableWorker`(volatile) → `AtomicAccount`(CAS). 가시성만 주는 `volatile`을 먼저, 원자적 갱신까지 가는 CAS를 마지막에 두어 난이도가 단조 증가한다(위 "큰 그림" 다이어그램 순서와 동일).

### ParallelSum (1문제) — Thread / join 입문 · 결정적

배열을 여러 스레드로 나눠 합산한다. **공유 가변 상태가 없어서** 동기화가 필요 없다 — "공유하지 않으면 안전하다". `Thread` 생성과 `join()`(결과 취합 + 가시성)을 익히는 워밍업.

| 메서드 | 핵심 |
|---|---|
| `sum(numbers, threadCount)` | 구간 분할 → 스레드마다 부분합 → 모두 join 후 합산 (각 스레드가 자기 칸에만 쓰면 충돌 없음) |

### SafeCounter (4문제) — synchronized 메서드 & 블록 · 채점

`count++`의 잃어버린 갱신을 `synchronized`로 막는다. 메서드 한정자 방식과 `synchronized(this)` 블록 방식을 모두 연습한다.

| 메서드 | 핵심 |
|---|---|
| `increment()` | `synchronized` **메서드**로 보호 |
| `add(delta)` | `synchronized(this)` **블록**으로 보호 (음수 delta 허용) |
| `get()` | 쓰기와 **같은 모니터**로 보호해야 최신값 가시 |
| `reset()` | 0으로 (역시 보호) |
| — 고경합 테스트 | 16스레드 × 1만회 → 정확히 160000 (`@RepeatedTest`) |

### StoppableWorker (4문제) — volatile 정지 플래그 · 채점("멈춘다")

다른 스레드가 보낸 정지 신호를 받고 협력적으로 멈추는 작업 루프. 정지 플래그는 **반드시 `volatile`**이어야 한다(아니면 가시성 문제로 무한 루프 — ch08 `VisibilityDemo` 참고). `volatile`은 **가시성만** 주므로 단순 쓰기(플래그)에는 충분하다 — 다음 `AtomicAccount`에서 "가시성만으로는 부족한" 원자성 문제로 넘어간다.

| 메서드 | 핵심 |
|---|---|
| `run()` | 정지 요청 전까지 `unitOfWork` 반복, 사이클 수 증가 |
| `requestStop()` | 정지 플래그를 `true`로 (다른 스레드가 호출 — 가시성 핵심) |
| `isRunning()` | 루프 진행 중 여부 |
| `completedCycles()` | 완료 사이클 수 |
| — 종료 테스트 | `@Timeout` — 신호 후 `join()`이 끝나야 통과(무한 루프면 타임아웃 실패) |

### AtomicAccount (5문제) — Atomic & CAS 루프 · 채점

락 없이 원자적으로 갱신되는 계좌. 단순 누적(`deposit`)은 `addAndGet`으로, 조건부 갱신(`withdraw`)은 **CAS 재시도 루프**로 직접 구현한다. (`volatile`은 가시성만 주지 read-modify-write의 원자성은 못 준다 — 그래서 CAS가 필요하다.)

| 메서드 | 핵심 |
|---|---|
| `AtomicAccount(initial)` | 잔액/횟수를 원자적 타입으로 보관 (`initial<0`이면 예외) |
| `balance()` | 현재 잔액 |
| `deposit(amount)` | 원자적 누적 + 새 잔액 반환 (`amount<=0`이면 예외) |
| `withdraw(amount)` | **CAS 루프** — 충분할 때만 차감, 부족하면 `false` |
| `depositCount()` | 성공한 입금 횟수 |
| — 보존 테스트 | 입금/출금 혼합 후 `잔액 == 초기 + 입금합 − 출금합`, 잔액 ≥ 0 |

### UnsafeCounter (데모 — 채점 안 함)

일부러 동기화하지 않은 카운터(완성된 틀린 코드). `SafeCounter`를 만들기 전에 경쟁조건으로 값을 잃는 것을 직접 본다. 테스트는 손실이 **관측된 경우에만** 단언(아니면 skip).

---

## 실행

```sh
# 전체 테스트 (스텁이 비어 있어 빨간불 — 구현하며 초록불로)
./gradlew :chapter11-thread-basics:test

# 특정 클래스
./gradlew :chapter11-thread-basics:test --tests "study.chapter11.SafeCounterTest"
```
