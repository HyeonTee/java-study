# Java Study

자바를 단원별로 직접 구현하며 학습하는 저장소. 표준 라이브러리에 의존하기 전에 그 안쪽을 먼저 짜본 뒤, 동시성 → 네트워크 → 프레임워크 순서로 추상화 계층을 올라간다.

## 시작하기

1. 이 저장소를 **Use this template** 또는 **Fork**로 복사한다.
2. `./gradlew :chapter01-generics-collections:test` 실행 — 빨간불(실패)을 확인한다.
3. 단원별 `README.md`에 이론 설명이 있으니 먼저 읽는다.
4. `src/main/java` 안의 빈 구현을 채워서 테스트를 초록불로 만든다.
5. 순서대로 진행하되, 관심 있는 Phase부터 시작해도 무방하다.

## 커리큘럼 (13단원, 3 Phase)

### Phase 1 — 자료구조 / 제네릭 / 함수형 / 현대 문법

Java의 기본기를 다진다. 표준 라이브러리의 내부를 직접 구현해보고, Stream/Optional/함수형으로 선언적 코드 작성법을 익힌다.

| 단원 | 주제 | 규모 |
|---|---|---|
| `chapter01-generics-collections` | 제네릭, `MyArrayList`, `MyLinkedList` 직접 구현 | 2 클래스 · 89 tests |
| `chapter02-hashmap-lru` | `HashMap` 직접 구현, LRU 캐시 | 2 클래스 · 31 tests |
| `chapter03-stream-optional` | Stream API, `Optional` | 20 문제 · 62 tests |
| `chapter04-functional-interface` | 함수형 인터페이스 (`Function`, `Predicate`, `Consumer`, `Supplier` 등) | — |
| `chapter05-modern-java-syntax` | `record`, `sealed`, switch expression, pattern matching | — |

### Phase 2 — JVM / 동시성

JVM이 메모리를 어떻게 관리하는지 이해한 뒤, 멀티스레드 프로그래밍을 저수준(`Thread`)부터 고수준(`CompletableFuture`, Virtual Thread)까지 단계적으로 학습한다.

| 단원 | 주제 | 규모 |
|---|---|---|
| `chapter06-jvm-memory-model` | JMM, happens-before, 메모리 가시성, GC 기초, 캐시 일관성 | — |
| `chapter07-thread-basics` | `Thread`, `synchronized`, `volatile`, Atomic, 경쟁조건 | — |
| `chapter08-executor-blocking-queue` | `ExecutorService`, ThreadPool, `BlockingQueue`, Producer-Consumer | — |
| `chapter09-completable-future` | `CompletableFuture`, 비동기 파이프라인, 예외 전파 | — |
| `chapter10-virtual-thread` | Project Loom, Virtual Thread, structured concurrency | — |

### Phase 3 — 네트워크 / 웹

TCP 소켓부터 시작해 HTTP를 직접 파싱하고, 점진적으로 추상화를 올려 최종적으로 Spring Boot REST API를 만든다.

| 단원 | 주제 | 규모 |
|---|---|---|
| `chapter11-tcp-http` | Socket TCP 서버 + HTTP 1.1 파싱 직접 구현 | — |
| `chapter12-mini-web-framework` | 라우터, 핸들러 체인, JSON 직렬화 | — |
| `chapter13-spring-boot-rest` | Spring Boot, JPA, 통합 테스트 | — |

> **규모**의 `—`는 아직 빌드되지 않은 단원. 진행하면서 추가된다.

## 학습 방식

각 단원은 **인터페이스 + 빈 구현 + 실패하는 테스트** 형태로 제공된다. 테스트가 통과하도록 직접 구현하며 학습한다. 정답 코드는 제공하지 않는다.

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
