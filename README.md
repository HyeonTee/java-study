# Java Study

자바를 단원별로 직접 구현하며 학습하는 저장소. 표준 라이브러리에 의존하기 전에 그 내부 동작을 먼저 직접 구현한 뒤, 동시성 → 네트워크 순서로 추상화 계층을 올라간다.

**대상**: Java 기초를 마치고 자료구조·동시성·네트워크를 깊이 파고들려는 개발자.

> **이 저장소가 기르는 것**은 `HashMap`·`Stream`·동적 디스패치·HTTP 서버를 *직접 구현*해본, **Java 내부와 런타임을 깊이 이해하는 백엔드 엔지니어**다. 반대로 Spring·빌드 도구·ORM·관측성 같은 *기존 생태계 활용*은 의도적으로 제외했다 — 내부를 알면 그 위는 빠르게 배운다. 즉 "Java 전문가"라는 말의 넓이보다 **"라이브러리·런타임 내부"의 깊이**를 목표로 한다.

## 학습 방식

각 단원은 **이론 `README.md` + 빈 구현(인터페이스 + 스텁) + 실패하는 테스트** 형태로 제공된다. 테스트가 통과하도록 직접 구현하며 학습한다. 정답 코드는 제공하지 않는다.

## 시작하기

1. 이 저장소를 **Use this template** 또는 **Fork**로 복사한다.
2. `./gradlew :chapter01-generics-collections:test` 실행 — 빨간불(실패)을 확인한다.
3. 단원별 `README.md`에 이론 설명이 있으니 먼저 읽는다.
4. `src/main/java` 안의 빈 구현을 채워서 테스트를 초록불로 만든다.
5. 순서대로 진행하되, 관심 있는 Phase부터 시작해도 무방하다.

## 커리큘럼 (18단원, 3 Phase)

> **규모** 열의 `—`는 아직 빌드되지 않은 단원(계획)이다. 진행하면서 추가된다. 현재 ch01~16이 빌드되어 있다. (Phase 1·2 블록 완결, Phase 3는 ch15 tcp-socket·ch16 http-protocol 빌드됨 — 다음은 ch17 concurrent-http-server)

> **저장소 구조**: 단원은 Phase별 폴더(`phase1-foundations/`, `phase2-jvm-concurrency/`, `phase3-networking/`)로 묶여 있다. Gradle 프로젝트 이름은 평면이라 테스트 명령은 폴더와 무관하게 `./gradlew :chapterNN-주제:test` 그대로다.

### Phase 1 — 자료구조 / 제네릭 / 함수형 / 현대 문법

Java의 기본기를 다진다. 표준 라이브러리의 내부를 직접 구현해보고, Stream/Optional/함수형으로 선언적 코드 작성법을 익힌다.

| 단원 | 주제 | 규모 |
|---|---|---|
| `chapter01-generics-collections` | 제네릭, `MyArrayList`, `MyLinkedList` 직접 구현 | 2 클래스 · 86 tests |
| `chapter02-hashmap-lru` | `HashMap` 직접 구현, LRU 캐시, `equals`/`hashCode` 계약 | 14 문제 · 38 tests |
| `chapter03-sorting-trees` | `Comparator` 합성, 이진탐색트리, `TreeMap`(정렬 맵), 최소 힙 직접 구현 | 28 문제 · 42 tests |
| `chapter04-functional-interface` | 함수형 인터페이스 (`Function`, `Predicate`, `Consumer`, `Supplier`), 검사 예외 래핑 | 18 문제 · 61 tests |
| `chapter05-stream-optional` | Stream API, `Optional`, `Collector` 직접 구현 | 23 문제 · 71 tests |
| `chapter06-modern-java-syntax` | `record`, `sealed`, switch expression, pattern matching | 12 문제 · 44 tests |

### Phase 2로 넘어가기 전 — 선수 개념 점검


| 선수 개념 | 왜 필요한가 | 어디서 다지나 |
|---|---|---|
| `equals`/`hashCode` 계약 | 값 객체를 맵/셋의 키로 쓰는 모든 곳 | ch02 HashMap 키 계약, ch08에서 계약 자체를 작성 |
| 검사 예외(checked) + wrap-and-rethrow | ch09 리플렉션 예외 래핑, ch12 예외 전파, ch14 `IOException`, ch15 소켓 | ch04 `ThrowingFunction` 문제 |
| try-with-resources / `AutoCloseable` | ch14 IO, ch15 소켓 수명주기 | ch14에서 본격적으로 |

### Phase 2 — JVM / 동시성

JVM이 메모리를 어떻게 관리하는지(ch07) 이해하고, 객체·클래스가 런타임에 실제로 무엇인지(ch08 객체 모델 → ch09 리플렉션) 해부한 뒤, 멀티스레드 프로그래밍을 저수준(`Thread`)부터 고수준(`CompletableFuture`, Virtual Thread)까지 단계적으로 학습한다.

| 단원 | 주제 | 규모 |
|---|---|---|
| `chapter07-jvm-memory-model` | 도달성 분석(mark-sweep)·세대별 GC 시뮬레이션 직접 구현, 약한 참조 캐시, JMM/GC 이론 | 12 문제 · 23 tests |
| `chapter08-object-model` | 동적 디스패치(vtable) 직접 구현, 정체성 vs 동등성, 방어적 복사/캡슐화, 내부 클래스 캡처 | 13 문제 · 35 tests |
| `chapter09-reflection-annotations` | `Class`/`Method`/`Field` 리플렉션, 커스텀 애너테이션 스캔, 객체↔맵 매퍼, 동적 프록시 직접 구현 | 11 문제 · 25 tests |
| `chapter10-thread-basics` | `Thread`/`join`, `synchronized`, `volatile`, Atomic/CAS, 경쟁조건 | 14 문제 · 58 tests |
| `chapter11-executor-blocking-queue` | `BlockingQueue`(wait/notify·Condition 2방식)·미니 스레드풀 직접 구현, Producer-Consumer | 17 문제 · 36 tests |
| `chapter12-completable-future` | 미니 promise(콜백·합성) 직접 구현, `CompletableFuture` 파이프라인·예외 전파 | 18 문제 · 23 tests |
| `chapter13-virtual-thread` | 가상 스레드(thread-per-task), structured concurrency 미니 스코프 직접 구현, 순서 보존 fan-out | 8 문제 · 18 tests |
| `chapter14-io-basics` | 스트림 데코레이터·라인/길이 프레이밍·NIO `ByteBuffer`·try-with-resources(LIFO·suppressed) 직접 구현 | 18 문제 · 36 tests |

### Phase 3 — 네트워크 / 웹

이 저장소의 **캡스톤**. TCP 소켓의 바이트 입출력에서 시작해 HTTP를 직접 파싱하고, 동시성 모델을 진화시키고, 마지막에 라우터·핸들러 체인을 갖춘 미니 웹 프레임워크를 만든다. 각 단원은 앞선 Phase의 기술을 실제 워크로드에서 다시 꺼내 쓴다 — 네트워크 지식과 Java 숙련도를 동시에 쌓는 것이 목표다.

설계 원칙: **표준 라이브러리가 감추는 로직(파서·상태 기계·직렬화기·서버 아키텍처)을 직접 손으로 구현**한다. 단순히 고수준 API를 설정만 하는 주제(TLS/HTTPS, WebSocket, HTTP/2, UDP)는 의도적으로 제외했다 — 그것은 프로토콜 지식이지 Java 내부 구현 학습이 아니기 때문이다.

| 단원 | 주제 | 복습하는 이전 Phase | 규모 |
|---|---|---|---|
| `chapter15-tcp-socket` | `ServerSocket`/`Socket` 생명주기, 블로킹 I/O, TCP echo **서버 + 클라이언트**, 스트림 위 프로토콜·라인/길이 프레이밍·half-close(`shutdownOutput`) | ch14 `LineReader`/`FrameCodec`/`closeAll`, try-with-resources | 19 문제 · 33 tests |
| `chapter16-http-protocol` | HTTP/1.1 요청·응답 직접 파싱·직렬화, 메시지 프레이밍(`Content-Length` + chunked 디코더), `sealed`+`record`로 메시지 모델링, case-insensitive 헤더 맵, 미니 HTTP 클라이언트(실소켓 1왕복) | ch15 `LineProtocol.readLine`·`readFully`/Content-Length·CRLF·half-close, ch06 sealed/record/pattern matching, ch05 Optional, ch02 HashMap(헤더 맵), ch04 검사 예외 | 18 문제 · 47 tests |
| `chapter17-concurrent-http-server` | 동일 서버를 단일스레드 → 스레드/연결 → 스레드풀 → 가상스레드/연결로 진화, keep-alive 연결 루프, 부하 테스트 | ch10 Thread, ch11 Executor/`BlockingQueue`, ch13 Virtual Thread, ch07 가시성 | — |
| `chapter18-mini-web-framework` | 제네릭 `Handler<Req,Res>`, 라우트 테이블, 핸들러 체인(미들웨어), JSON 직렬화 직접 구현 | ch09 리플렉션·애너테이션(`@Route` 스캔 라우팅), ch01 제네릭, ch04 함수형 인터페이스, ch02 맵, ch06 record | — |

## 실행

```sh
# 전체 테스트
./gradlew test

# 특정 단원
./gradlew :chapter01-generics-collections:test

# 특정 테스트 클래스
./gradlew :chapter01-generics-collections:test --tests "study.chapter01.MyArrayListTest"

# 특정 테스트 메서드
./gradlew :chapter01-generics-collections:test --tests "study.chapter01.MyArrayListTest.add_하면_size가_1증가한다"
```

## 요구 환경

- **Java 21 이상** (Gradle toolchain이 자동 다운로드하므로 별도 설치 불필요)
- Gradle 9.3.1 (`./gradlew`로 자동 사용)
