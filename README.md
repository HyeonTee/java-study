# Java Study

자바를 단원별로 직접 구현하며 학습하는 저장소. 표준 라이브러리에 의존하기 전에 그 내부 동작을 먼저 직접 구현한 뒤, 동시성 → 네트워크 순서로 추상화 계층을 올라간다.

**대상**: Java 기초를 마치고 자료구조·동시성·네트워크를 깊이 파고들려는 개발자.

## 학습 방식

각 단원은 **이론 `README.md` + 빈 구현(인터페이스 + 스텁) + 실패하는 테스트** 형태로 제공된다. 테스트가 통과하도록 직접 구현하며 학습한다. 정답 코드는 제공하지 않는다.

## 시작하기

1. 이 저장소를 **Use this template** 또는 **Fork**로 복사한다.
2. `./gradlew :chapter01-generics-collections:test` 실행 — 빨간불(실패)을 확인한다.
3. 단원별 `README.md`에 이론 설명이 있으니 먼저 읽는다.
4. `src/main/java` 안의 빈 구현을 채워서 테스트를 초록불로 만든다.
5. 순서대로 진행하되, 관심 있는 Phase부터 시작해도 무방하다.

## 커리큘럼 (15단원, 3 Phase)

> **규모** 열의 `—`는 아직 빌드되지 않은 단원(계획)이다. 진행하면서 추가된다. 현재 ch01~06이 빌드되어 있다.

### Phase 1 — 자료구조 / 제네릭 / 함수형 / 현대 문법

Java의 기본기를 다진다. 표준 라이브러리의 내부를 직접 구현해보고, Stream/Optional/함수형으로 선언적 코드 작성법을 익힌다.

| 단원 | 주제 | 규모 |
|---|---|---|
| `chapter01-generics-collections` | 제네릭, `MyArrayList`, `MyLinkedList` 직접 구현 | 2 클래스 · 86 tests |
| `chapter02-hashmap-lru` | `HashMap` 직접 구현, LRU 캐시, `equals`/`hashCode` 계약 | 16 문제 · 51 tests |
| `chapter03-functional-interface` | 함수형 인터페이스 (`Function`, `Predicate`, `Consumer`, `Supplier`), 검사 예외 래핑 | 18 문제 · 61 tests |
| `chapter04-stream-optional` | Stream API, `Optional` | 20 문제 · 62 tests |
| `chapter05-modern-java-syntax` | `record`, `sealed`, switch expression, pattern matching | 12 문제 · 44 tests |

### Phase 2로 넘어가기 전 — 선수 개념 점검


| 선수 개념 | 왜 필요한가 | 어디서 다지나 |
|---|---|---|
| `equals`/`hashCode` 계약 | 값 객체를 맵/셋의 키로 쓰는 모든 곳 | ch02 `Money` 문제 |
| 검사 예외(checked) + wrap-and-rethrow | ch09 예외 전파, ch11 `IOException`, ch12 소켓 | ch03 `ThrowingFunction` 문제 |
| try-with-resources / `AutoCloseable` | ch11 IO, ch12 소켓 수명주기 | ch11에서 본격적으로 |

### Phase 2 — JVM / 동시성

JVM이 메모리를 어떻게 관리하는지 이해한 뒤, 멀티스레드 프로그래밍을 저수준(`Thread`)부터 고수준(`CompletableFuture`, Virtual Thread)까지 단계적으로 학습한다.

| 단원 | 주제 | 규모 |
|---|---|---|
| `chapter06-jvm-memory-model` | 도달성 분석(mark-sweep) 직접 구현, 약한 참조 캐시, JMM/GC 이론 | 6 문제 · 17 tests |
| `chapter07-thread-basics` | `Thread`, `synchronized`, `volatile`, Atomic, 경쟁조건 | — |
| `chapter08-executor-blocking-queue` | `ExecutorService`, ThreadPool, `BlockingQueue`, Producer-Consumer | — |
| `chapter09-completable-future` | `CompletableFuture`, 비동기 파이프라인, 예외 전파 | — |
| `chapter10-virtual-thread` | Project Loom, Virtual Thread, structured concurrency | — |
| `chapter11-io-basics` | `InputStream`/`OutputStream`, `Reader`/`Writer`, NIO `ByteBuffer`/`Channel`, try-with-resources | — |

### Phase 3 — 네트워크 / 웹

이 저장소의 **캡스톤**. TCP 소켓의 바이트 입출력에서 시작해 HTTP를 직접 파싱하고, 동시성 모델을 진화시키고, 마지막에 라우터·핸들러 체인을 갖춘 미니 웹 프레임워크를 만든다. 각 단원은 앞선 Phase의 기술을 실제 워크로드에서 다시 꺼내 쓴다 — 네트워크 지식과 Java 숙련도를 동시에 쌓는 것이 목표다.

설계 원칙: **표준 라이브러리가 감추는 로직(파서·상태 기계·직렬화기·서버 아키텍처)을 직접 손으로 구현**한다. 단순히 고수준 API를 설정만 하는 주제(TLS/HTTPS, WebSocket, HTTP/2, UDP)는 의도적으로 제외했다 — 그것은 프로토콜 지식이지 Java 내부 구현 학습이 아니기 때문이다.

| 단원 | 주제 | 복습하는 이전 Phase | 규모 |
|---|---|---|---|
| `chapter12-tcp-socket-basics` | `ServerSocket`/`Socket` 생명주기, 블로킹 I/O, TCP echo **서버 + 클라이언트**, 라인 단위 프레이밍 | ch11 스트림/`Channel`, try-with-resources | — |
| `chapter13-http-protocol` | HTTP/1.1 요청·응답 직접 파싱·직렬화, 메시지 프레이밍(`Content-Length` vs chunked), `sealed`+`record`로 메시지 모델링, 미니 HTTP 클라이언트로 검증 | ch05 sealed/record/pattern matching, ch04 Stream/Optional, ch02 HashMap(헤더 맵) | — |
| `chapter14-concurrent-http-server` | 동일 서버를 단일스레드 → 스레드/연결 → 스레드풀 → 가상스레드/연결로 진화, keep-alive 연결 루프, 부하 테스트 | ch07 Thread, ch08 Executor/`BlockingQueue`, ch10 Virtual Thread, ch06 가시성 | — |
| `chapter15-mini-web-framework` | 제네릭 `Handler<Req,Res>`, 라우트 테이블, 핸들러 체인(미들웨어), JSON 직렬화 직접 구현 | ch01 제네릭, ch03 함수형 인터페이스, ch02 맵, ch05 record | — |

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
