# Chapter 07 — JVM 메모리 모델

> **선행 단원**: Phase 1(Chapter 01~05)의 Java 문법과 표준 API를 익힌 상태에서 진행한다. 특히 Chapter 01에서 다룬 `new`를 통한 객체 생성, Chapter 06의 `record`가 힙에 어떻게 저장되는지를 이 단원에서 JVM 관점으로 다시 본다.

> **공식 문서**: [JVM Specification Ch.2 — Run-Time Data Areas](https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-2.html#jvms-2.5) · [Java Memory Model (JLS 17.4)](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html#jls-17.4) · [HotSpot GC Tuning Guide](https://docs.oracle.com/en/java/javase/21/gctuning/)

---

## JVM 메모리 구조

### 왜 알아야 하는가?

Java 개발자는 `new`로 객체를 만들고, 더 이상 쓰지 않으면 GC가 알아서 회수해 준다고 배운다. 대부분의 경우 이것으로 충분하다. 그런데 **왜 메모리 누수가 발생할까?** 왜 `OutOfMemoryError`가 터질까? 왜 멀티스레드에서 값이 안 보일까?

이 질문들에 답하려면 JVM이 메모리를 어떻게 나누고, 객체를 어디에 두며, 스레드 간 데이터를 어떻게 공유하는지 이해해야 한다.

### 런타임 데이터 영역 전체 그림

```
┌─────────────────────────────────────────────────────────────┐
│                           JVM                               │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                 Shared across all threads             │  │
│  │                                                       │  │
│  │  ┌─────────────────┐  ┌─────────────────────────────┐ │  │
│  │  │   Method Area   │  │            Heap             │ │  │
│  │  │  (class meta,   │  │  (object instances, arrays) │ │  │
│  │  │   constant pool,│  │                             │ │  │
│  │  │   static fields)│  │  Young ──┬── Eden           │ │  │
│  │  │                 │  │          ├── Survivor 0     │ │  │
│  │  │                 │  │          └── Survivor 1     │ │  │
│  │  │                 │  │  Old ────── Tenured         │ │  │
│  │  └─────────────────┘  └─────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                 Per-thread (independent)              │  │
│  │                                                       │  │
│  │  Thread 1           Thread 2           Thread N       │  │
│  │  ┌──────────┐      ┌──────────┐      ┌──────────┐     │  │
│  │  │ PC Reg   │      │ PC Reg   │      │ PC Reg   │     │  │
│  │  │ JVM Stack│      │ JVM Stack│      │ JVM Stack│     │  │
│  │  │ Native   │      │ Native   │      │ Native   │     │  │
│  │  │  Stack   │      │  Stack   │      │  Stack   │     │  │
│  │  └──────────┘      └──────────┘      └──────────┘     │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 1. Heap (힙)

`new`로 생성된 **모든 객체와 배열**이 저장되는 공간. **모든 스레드가 공유**한다.

```java
String s = new String("hello");   // "hello" 객체 → 힙
int[] arr = new int[10];          // 배열 → 힙
Point p = new Point(1, 2);       // Point 인스턴스 → 힙
```

- GC(Garbage Collector)가 관리하는 영역이다.
- `-Xms`(초기 크기), `-Xmx`(최대 크기)로 조절한다.
- 공간이 부족하면 `java.lang.OutOfMemoryError: Java heap space`가 발생한다.

### 2. JVM Stack (스택)

각 스레드가 생성될 때 **전용 스택**을 하나씩 받는다. 메서드 호출마다 **스택 프레임(Stack Frame)**이 하나씩 push되고, 메서드가 리턴하면 pop된다.

스택 프레임에는 다음이 들어있다:
- **지역 변수 배열** (Local Variable Array) — 메서드의 매개변수와 지역 변수
- **오퍼랜드 스택** (Operand Stack) — 연산의 중간 결과
- **프레임 데이터** — 상수 풀 참조, 예외 처리 정보 등

```java
void foo() {
    int x = 10;         // x → foo()의 스택 프레임 (지역 변수 배열)
    int y = 20;         // y → foo()의 스택 프레임
    bar(x + y);         // bar() 호출 → 새 스택 프레임 push
}                       // foo() 리턴 → 스택 프레임 pop (x, y 소멸)

void bar(int sum) {
    // sum → bar()의 스택 프레임
}                       // bar() 리턴 → 스택 프레임 pop
```

**핵심**: primitive 지역 변수의 **값 자체**가 스택에 저장된다. 참조형 지역 변수는 **참조(주소)**만 스택에 있고, 실제 객체는 힙에 있다.

```java
void example() {
    int age = 30;                  // 30이라는 값 자체가 스택에
    String name = new String("A"); // name(참조)은 스택, "A" 객체는 힙
}
```

- 스택 깊이를 초과하면 `StackOverflowError`가 발생한다 (무한 재귀가 대표 원인).
- `-Xss`로 스택 크기를 조절한다.

### 3. Method Area (메서드 영역)

클래스 로더가 `.class` 파일을 읽어 올릴 때, 그 **클래스의 메타데이터**를 저장하는 공간. **모든 스레드가 공유**한다.

저장되는 것:
- 클래스/인터페이스의 구조 정보 (필드 목록, 메서드 목록, 접근 제어자 등)
- **Runtime Constant Pool** — 리터럴 상수, 심볼릭 참조
- `static` 필드의 값
- 메서드의 바이트코드

```java
public class Counter {
    static int count = 0;          // count → Method Area (static 필드)
    
    public void increment() {      // increment()의 바이트코드 → Method Area
        count++;
    }
}
```

> **Metaspace**: Java 8 이후, Method Area의 구현이 PermGen에서 Metaspace(네이티브 메모리)로 변경되었다. 클래스를 과도하게 동적 생성하면 `OutOfMemoryError: Metaspace`가 발생할 수 있다.

### 4. PC Register (Program Counter)

각 스레드가 현재 **실행 중인 바이트코드 명령어의 주소**를 저장한다. CPU의 Program Counter와 같은 개념. 스레드가 컨텍스트 스위칭 후 돌아올 때 이전 실행 위치를 복원하는 데 사용된다.

### 5. Native Method Stack

`native` 키워드로 선언된 메서드(C/C++로 작성된 JNI 코드)를 실행할 때 사용하는 스택. JVM Stack과 별개다.

### 정리: "이 값은 어디에 있는가?"

| 코드 | 저장 위치 |
|---|---|
| `int x = 10;` (지역 변수) | JVM Stack — 스택 프레임의 지역 변수 배열 |
| `new Object()` | Heap |
| `static int count` | Method Area |
| 클래스의 메서드 바이트코드 | Method Area |
| 지역 변수 `Object obj = new Object();`의 `obj` | 참조는 Stack, 객체는 Heap |

---

## 객체의 생명주기

### 1단계: 생성 (new)

`new` 키워드가 실행되면 JVM이 수행하는 일:

1. **클래스 로딩 확인** — 해당 클래스가 Method Area에 로드되어 있는지 확인. 없으면 ClassLoader가 로드.
2. **메모리 할당** — Heap의 Eden 영역에서 객체 크기만큼 메모리를 확보.
3. **기본값 초기화** — 모든 필드를 기본값으로 초기화 (`int`→0, `boolean`→false, 참조→null).
4. **생성자 실행** — 개발자가 작성한 초기화 코드 실행.

```java
Person p = new Person("Kim", 25);
// 1. Person.class 로딩 확인
// 2. 힙에 Person 크기만큼 할당
// 3. name=null, age=0으로 초기화
// 4. Person("Kim", 25) 생성자 실행 → name="Kim", age=25
```

### 2단계: 사용 (참조)

객체는 **참조 변수**를 통해 접근한다. 하나의 객체를 여러 변수가 참조할 수 있다.

```java
Person p1 = new Person("Kim", 25);
Person p2 = p1;   // p1과 p2가 같은 객체를 참조
```

### 3단계: 도달 가능성 (Reachability)

GC는 객체를 회수할지 말지를 **도달 가능성**으로 판단한다.

**GC Root**에서 참조 체인을 따라 도달할 수 있는 객체는 살아 있고, 도달할 수 없으면 GC 대상이다.

GC Root가 되는 것:
- 실행 중인 스레드의 **스택 프레임에 있는 지역 변수**
- **static 필드**
- **활성 스레드** 자체
- JNI 참조

```java
void method() {
    Person a = new Person("A");  // Person("A") → a가 참조 → 도달 가능
    Person b = new Person("B");  // Person("B") → b가 참조 → 도달 가능
    
    a = null;                    // Person("A") → 아무도 참조 안 함 → GC 대상
    b = new Person("C");        // Person("B") → 아무도 참조 안 함 → GC 대상
                                 // Person("C") → b가 참조 → 도달 가능
}
// method() 리턴 → 스택 프레임 pop → Person("C")도 GC 대상
```

### 4단계: 소멸

GC가 도달 불가능한 객체의 메모리를 회수한다. 개발자가 소멸 시점을 제어할 수 없다. `System.gc()`는 GC를 **요청**할 뿐 보장하지 않는다.

### 순환 참조와 GC

Java의 GC는 참조 카운팅이 아니라 **도달 가능성 분석(reachability analysis)**을 사용한다. 따라서 **순환 참조만 남은 객체도 GC 대상**이다.

```java
class Node {
    Node next;
}

void test() {
    Node a = new Node();
    Node b = new Node();
    a.next = b;    // a → b
    b.next = a;    // b → a  (순환 참조)
    
    a = null;
    b = null;
    // a, b 지역 변수가 더 이상 참조하지 않음
    // GC Root에서 두 Node에 도달할 수 없음 → 둘 다 GC 대상
    // (순환 참조가 있어도 회수된다)
}
```

---

## GC 기초

### 왜 GC가 필요한가?

C/C++에서는 개발자가 `malloc`/`free`로 직접 메모리를 관리한다. 이 방식의 문제:
- **해제 누락** (memory leak) — free를 안 하면 메모리가 계속 쌓인다.
- **이중 해제** (double free) — 이미 해제된 메모리를 또 해제하면 프로그램이 망가진다.
- **댕글링 포인터** (dangling pointer) — 해제된 메모리를 참조하면 예측 불가한 동작.

Java의 GC는 이 문제들을 **자동으로 해결**한다. 대신 GC가 동작하는 동안 성능 비용이 발생한다.

### Mark-and-Sweep

가장 기본적인 GC 알고리즘. 두 단계로 동작한다:

**1단계: Mark (표시)**
GC Root부터 참조 체인을 따라가며 도달 가능한 객체에 "살아 있음" 표시를 한다.

**2단계: Sweep (청소)**
힙을 순회하면서 표시가 없는 객체의 메모리를 회수한다.

```
Mark 전:
GC Root → [A] → [B] → [C]
          [D] (참조 없음)
          [E] → [F]  (참조 없음)

Mark 후:    ✓A → ✓B → ✓C     (도달 가능 → 표시)
           D, E, F           (도달 불가 → 표시 없음)

Sweep 후:  [A] → [B] → [C]   (유지)
           D, E, F 메모리 회수
```

### Generational GC (세대별 GC)

대부분의 객체는 **금방 죽는다** (Weak Generational Hypothesis). 임시 변수, 반복문 안의 객체 등은 생성 직후 곧 불필요해진다.

이 관찰에 기반하여 힙을 **세대(Generation)**로 나눈다:

```
Heap
├── Young Generation (새로 생성된 객체)
│   ├── Eden          ← new 객체가 처음 할당되는 곳
│   ├── Survivor 0    ← Minor GC 생존자가 이동하는 곳
│   └── Survivor 1    ← S0과 S1은 번갈아 사용
│
└── Old Generation (오래 살아남은 객체)
    └── Tenured       ← 여러 번 Minor GC를 살아남으면 승격(promotion)
```

### Minor GC vs Major GC

| | Minor GC | Major GC (Full GC) |
|---|---|---|
| 대상 | Young Generation | 전체 힙 (Young + Old) |
| 발생 시점 | Eden이 가득 찼을 때 | Old가 가득 찼을 때 |
| 속도 | 빠름 (대부분 죽은 객체) | 느림 (전체 스캔) |
| STW 시간 | 짧음 | 김 |

### 객체의 세대 이동 흐름

```
1. new Object()  →  Eden에 할당

2. Eden 꽉 참  →  Minor GC 발생
   - 살아남은 객체 → Survivor 영역으로 이동 (age = 1)
   - 죽은 객체 → 회수

3. 다음 Minor GC
   - Eden + 현재 Survivor의 생존 객체 → 다른 Survivor로 이동 (age++)
   - 죽은 객체 → 회수

4. age가 임계값(기본 15) 도달
   - 해당 객체 → Old Generation으로 승격 (promotion)

5. Old Generation 꽉 참  →  Major GC (Full GC) 발생
```

### Stop-the-World (STW)

GC가 동작하는 동안 **모든 애플리케이션 스레드가 일시 정지**하는 현상. GC가 객체 그래프를 분석하는 동안 참조 관계가 바뀌면 안 되기 때문이다.

- Minor GC의 STW는 보통 수 밀리초.
- Full GC의 STW는 수십 밀리초에서 수 초까지 걸릴 수 있다.
- 실시간 응답이 중요한 서비스에서는 STW 시간을 최소화하는 GC 알고리즘(G1, ZGC, Shenandoah 등)을 선택한다.

### GC 로그 읽기 (기초)

JVM 옵션으로 GC 로그를 켤 수 있다:

```sh
java -Xlog:gc* -jar app.jar
```

로그 예시 (G1 GC):

```
[0.015s][info][gc] Using G1
[1.234s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
                   24M->8M(256M) 5.432ms
```

| 항목 | 의미 |
|---|---|
| `GC(0)` | 0번째 GC 이벤트 |
| `Pause Young` | Young Generation 대상 GC |
| `24M->8M` | GC 전 24MB 사용 → GC 후 8MB 사용 |
| `(256M)` | 전체 힙 크기 |
| `5.432ms` | STW 일시정지 시간 |

### 주요 GC 알고리즘 (참고)

| GC | 특징 | 적합한 경우 |
|---|---|---|
| **Serial GC** | 단일 스레드. 가장 단순 | 클라이언트, 작은 힙 |
| **Parallel GC** | 여러 스레드로 병렬 처리. 높은 처리량 | 배치 처리, 높은 처리량 우선 |
| **G1 GC** | 힙을 Region으로 분할. 예측 가능한 STW | 범용 (Java 9+ 기본) |
| **ZGC** | 초저지연. STW < 1ms 목표 | 지연 민감한 서비스 |

이 단원에서는 GC 알고리즘의 세부 튜닝보다는 **세대별 GC의 기본 원리**와 **객체가 어떻게 이동하고 회수되는지** 이해하는 데 집중한다.

---

## JMM (Java Memory Model) 기초

### 왜 필요한가?

단일 스레드에서는 코드를 위에서 아래로 읽으면 동작을 예측할 수 있다. 그런데 **멀티스레드**에서는 그렇지 않다.

```java
// Thread 1
x = 1;
ready = true;

// Thread 2
if (ready) {
    System.out.println(x);  // 1이 출력될까? 0이 출력될 수도 있다!
}
```

직관적으로는 `ready`가 true이면 `x`는 반드시 1이어야 할 것 같다. 하지만 **실제로는 0이 출력될 수 있다**. 왜?

### Main Memory vs Working Memory

JMM은 하드웨어의 복잡한 캐시 구조를 추상화한 모델이다:

```
                  ┌─────────────────┐
                  │   Main Memory   │  (heap — shared by all threads)
                  │    x = ?        │
                  │    ready = ?    │
                  └────────┬────────┘
                           │
            ┌──────────────┼──────────────┐
            │              │              │
    ┌───────┴───────┐ ┌────┴────────┐ ┌───┴─────────┐
    │  Working      │ │  Working    │ │  Working    │
    │  Memory 1     │ │  Memory 2   │ │  Memory 3   │
    │  (CPU cache)  │ │ (CPU cache) │ │ (CPU cache) │
    └───────┬───────┘ └────┬────────┘ └───┬─────────┘
            │              │              │
        Thread 1       Thread 2       Thread 3
```

각 스레드는 공유 변수를 **자신의 Working Memory(CPU 캐시)**에 복사해서 작업한다. Working Memory의 변경이 Main Memory에 **언제** 반영되는지는 보장되지 않는다.

### 가시성 (Visibility) 문제

한 스레드가 변수를 수정해도, 다른 스레드에게 그 변경이 **보이지 않을 수 있다**.

```java
public class VisibilityProblem {
    private boolean running = true;  // 공유 변수

    public void stop() {
        running = false;  // Thread 1이 변경
    }

    public void run() {
        while (running) {  // Thread 2가 읽음 — 변경이 안 보일 수 있음!
            // 무한 루프에 빠질 수 있다
        }
    }
}
```

Thread 1이 `running = false`를 설정해도, 그 값이 Thread 1의 Working Memory에만 머물러 있을 수 있다. Thread 2는 자신의 Working Memory에 캐시된 `running = true`를 계속 읽는다.

### 재배치 (Reordering) 문제

컴파일러와 CPU는 성능 최적화를 위해 **명령어 순서를 바꿀 수 있다**. 단일 스레드에서는 결과가 동일하지만, 멀티스레드에서는 예상치 못한 결과를 만든다.

```java
// 코드 작성 순서
x = 1;        // (1)
ready = true;  // (2)

// 컴파일러/CPU가 재배치할 수 있음:
ready = true;  // (2) 먼저!
x = 1;        // (1) 나중에
```

단일 스레드에서는 (1)→(2)든 (2)→(1)이든 최종 결과가 같다. 하지만 다른 스레드가 `ready`를 보고 `x`를 읽으면, `x`가 아직 0일 수 있다.

### 가시성과 재배치 문제의 해결

JMM은 `volatile`, `synchronized`, `final` 등의 키워드로 **가시성을 보장하고 재배치를 제한하는 규칙**을 정의한다. 이것이 다음 섹션에서 다루는 **happens-before 관계**다.

---

## happens-before 관계

### 핵심 개념

**happens-before**는 JMM이 정의하는 순서 보장 규칙이다.

> "A happens-before B"이면, **A의 결과가 B에게 반드시 보인다.**

happens-before는 실제 시간 순서가 아니라 **메모리 가시성의 보장**이다. A가 B보다 먼저 실행되는 것이 아니라, A에서 쓴 값이 B에서 읽을 때 반드시 보인다는 뜻이다.

### 규칙 1: volatile 쓰기 → 읽기

`volatile` 변수에 대한 **쓰기**는 그 변수의 **읽기**에 happens-before이다.

```java
private volatile boolean ready = false;
private int x = 0;

// Thread 1
x = 42;              // (1)
ready = true;         // (2) volatile 쓰기

// Thread 2
if (ready) {          // (3) volatile 읽기 — (2) happens-before (3)
    System.out.println(x);  // (4) 반드시 42가 보인다
}
```

volatile 쓰기가 happens-before 읽기를 보장하므로, volatile 쓰기 **이전의 모든 쓰기**(여기서는 `x = 42`)도 volatile 읽기 이후에 보인다. 이것을 **메모리 가시성의 전이(transitivity)**라고 한다.

volatile의 두 가지 효과:
1. **가시성 보장** — volatile 변수의 변경이 다른 스레드에 즉시 보인다.
2. **재배치 방지** — volatile 쓰기 이전의 명령어가 volatile 쓰기 이후로 재배치되지 않는다.

### 규칙 2: synchronized unlock → lock

같은 모니터(락)에 대해, **unlock**은 이후의 **lock**에 happens-before이다.

```java
private int count = 0;
private final Object lock = new Object();

// Thread 1
synchronized (lock) {     // lock 획득
    count = 10;            // (1)
}                          // (2) unlock — happens-before →

// Thread 2
synchronized (lock) {     // (3) lock 획득 — (2) happens-before (3)
    System.out.println(count);  // (4) 반드시 10이 보인다
}
```

Thread 1이 `synchronized` 블록을 빠져나오면(unlock), 블록 안에서 수정한 모든 변수가 Main Memory에 플러시된다. Thread 2가 같은 `lock`으로 `synchronized`에 진입하면(lock), Main Memory에서 최신 값을 읽어온다.

### 규칙 3: Thread.start() → run()

`thread.start()`를 호출한 코드의 모든 변경은 새 스레드의 `run()` 메서드에 보인다.

```java
int value = 0;

value = 99;               // (1)
Thread t = new Thread(() -> {
    System.out.println(value);  // (3) 반드시 99가 보인다
});
t.start();                // (2) start() — (1) happens-before (3)
```

### 규칙 4: run() 종료 → Thread.join()

스레드의 `run()` 메서드에서 수행한 모든 변경은 `join()` 리턴 이후에 보인다.

```java
int[] result = new int[1];

Thread t = new Thread(() -> {
    result[0] = 42;        // (1) run() 안에서 변경
});                        // (2) run() 종료

t.start();
t.join();                  // (3) join() 리턴 — (2) happens-before (3)
System.out.println(result[0]);  // (4) 반드시 42가 보인다
```

### happens-before 규칙 요약

| 규칙 | A (먼저) | B (나중) | 보장 |
|---|---|---|---|
| volatile | volatile 쓰기 | 같은 변수 volatile 읽기 | A의 모든 쓰기가 B에 보임 |
| synchronized | unlock (블록 나감) | 같은 모니터 lock (블록 진입) | A의 모든 쓰기가 B에 보임 |
| Thread.start() | start() 호출 전 코드 | 새 스레드의 run() | start() 전 쓰기가 run()에 보임 |
| Thread.join() | run() 안의 코드 | join() 리턴 후 코드 | run() 안 쓰기가 join() 후에 보임 |
| 프로그램 순서 | 같은 스레드의 앞 코드 | 같은 스레드의 뒷 코드 | 단일 스레드 내에서는 항상 순서대로 |

### happens-before가 없으면?

```java
// 잘못된 코드 — happens-before 보장 없음
private int x = 0;        // volatile 아님
private boolean done = false; // volatile 아님

// Thread 1
x = 42;
done = true;

// Thread 2
if (done) {
    // x가 42일 수도, 0일 수도 있다!
    // done이 true로 보여도 x = 42가 보인다는 보장이 없다.
}
```

`done`이 `volatile`이 아니므로 happens-before 관계가 성립하지 않는다. Thread 2에서 `done`이 true로 보이더라도 `x`의 값은 보장되지 않는다.

---

## 참조의 종류 — strong / soft / weak / phantom

지금까지 본 참조는 전부 **강한 참조(strong reference)**다. `Object o = new Object();`의 `o`처럼, 강한 참조가 하나라도 살아 있으면 GC는 그 객체를 절대 수거하지 않는다. `java.lang.ref` 패키지는 GC와의 관계가 더 약한 참조들을 제공한다.

| 참조 종류 | 수거 시점 | 용도 |
|---|---|---|
| Strong (일반) | 도달 불가능해질 때까지 절대 수거 안 됨 | 보통의 참조 |
| `SoftReference` | 메모리가 부족할 때 수거 | 메모리 민감 캐시 |
| `WeakReference` | 다음 GC에서 (강한 참조가 없으면) 수거 | 약한 캐시, 누수 방지 |
| `PhantomReference` | 수거 직전, `ReferenceQueue`로만 관찰 | 자원 정리(`finalize` 대체) |

```java
WeakReference<byte[]> ref = new WeakReference<>(new byte[1024]);
ref.get();   // 살아 있으면 배열, GC가 수거했으면 null
```

**핵심**: `WeakReference.get()`은 어느 순간 `null`을 반환할 수 있다. 강한 참조가 사라진 객체를 약한 참조만으로는 살릴 수 없기 때문이다. 이 성질을 이용하면 "값을 들고 있되, 아무도 안 쓰면 알아서 비워지는" 캐시를 만들 수 있다 — 강한 참조 맵이 일으키는 메모리 누수를 막는다.

> `System.gc()`는 GC를 **요청**할 뿐 보장하지 않는다. 그래서 "약한 참조가 언제 비워지는가"는 테스트로 단정할 수 없고, **검증되면 단언하고 아니면 skip**하는 식으로만 다룬다 (아래 `WeakValueCache` 테스트 참고).

---

## 왜 가시성/happens-before는 "문제"가 아니라 "데모"인가

이 단원의 가시성·재배치·happens-before는 **단위 테스트로 채점하지 않는다.** 데이터 레이스는 **비결정적**이기 때문이다 — `volatile`을 빠뜨린 잘못된 코드도 x86에서는 우연히 잘 동작할 때가 많아서, "레이스가 있다"를 테스트로 재현할 수 없다. 잘못된 구현이 초록불이 되는 테스트는 학습에 해롭다.

대신 `VisibilityDemo`를 **직접 실행해보며 관찰**한다. 동시성을 제대로 *구현*하고 검증하는 일(올바른 게시, 동기화)은 Phase 2의 ch10(thread-basics) 이후에서 적절한 도구로 다룬다. 이 단원은 "왜 그런 보장이 필요한가"라는 **메모리 모델의 이유**에 집중한다.

---

## 연습 문제

### ReachabilityAnalyzer (3문제) — GC의 mark 단계 직접 구현

`HeapObject` 그래프 위에서 **도달 가능성 분석**을 손으로 구현한다. 실제 GC가 GC Root에서 도달 가능한 객체를 표시(mark)하고 나머지를 수거(sweep)하는 그 알고리즘이다. 퀴즈로 "순환 참조도 수거되나요?"를 묻는 대신, **순환 참조가 수거 대상이 되는 것을 코드로 증명**한다.

| # | 메서드 | 핵심 |
|---|---|---|
| 1 | `reachable(roots)` | root에서 도달 가능한 객체 집합 (BFS/DFS, 사이클 안전) |
| 2 | `isReachable(roots, target)` | 특정 객체의 도달 가능 여부 |
| 3 | `collectable(all, roots)` | 도달 불가능한 = 수거 대상 집합 (전체 − 도달가능) |
| — | 순환 참조 테스트 | root에서 끊긴 사이클은 둘 다 수거 대상임을 검증 |

### WeakValueCache (3문제) — 약한 참조 캐시 구현

`java.lang.ref.WeakReference`로 값을 보관하는 캐시를 직접 만든다. 강한 참조가 살아 있는 동안의 동작은 **결정적으로** 테스트하고, "GC되면 사라진다"는 `System.gc()`가 실제로 수거했을 때만 검증(아니면 skip)한다.

| # | 메서드 | 핵심 |
|---|---|---|
| 1 | `put(k, v)` | 값을 `WeakReference`로 감싸 저장 |
| 2 | `get(k)` | 살아 있으면 값, 수거됐으면 `Optional.empty()` |
| 3 | `size()` | 아직 살아 있는 항목 수 |

### GenerationalHeap (6문제) — Minor GC·객체 승격 시뮬레이션

세대별 GC의 핵심(Eden→Survivor→Old 승격)을 **결정적 모형**으로 구현한다. `System.gc()`에 의존하지 않으므로 채점 가능하다 — 위에서 이론으로만 본 세대·승격·age를 코드로 증명한다. `ReachabilityAnalyzer.reachable`을 재사용해 도달성을 판단한다(그 문제를 먼저 푼다).

| # | 메서드 | 핵심 |
|---|---|---|
| 1 | `allocate(obj)` | young 세대에 age 0으로 할당 |
| 2 | `youngCount` / `oldCount` / `isInOldGen` | 세대 상태 조회 |
| 3 | `ageOf(obj)` | 생존한 Minor GC 횟수 (young에 없으면 −1) |
| 4 | `minorGc(roots, threshold)` | 도달 가능 young은 age++/임계값 이상이면 승격, 불가능은 수거. old는 보존 |

### VisibilityDemo (데모 — 채점 안 함)

`volatile` 유무에 따른 가시성 차이를 직접 실행해 관찰하는 데모 클래스. 위 "왜 데모인가" 절 참고.
