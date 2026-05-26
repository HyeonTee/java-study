# Java Study

자바를 단원별로 직접 구현하며 학습하는 저장소. 표준 라이브러리에 의존하기 전에 그 안쪽을 먼저 짜본 뒤, 동시성 → 네트워크 → 프레임워크 순서로 추상화 계층을 올라간다.

## 커리큘럼 (13단원, 3 Phase)

### Phase 1 — 자료구조 / 제네릭 / 함수형 / 현대 문법

| 단원 | 주제 |
|---|---|
| `chapter01-generics-collections` | 제네릭, `MyArrayList`, `MyLinkedList` 직접 구현 |
| `chapter02-hashmap-lru` | `HashMap` 직접 구현, LRU 캐시 |
| `chapter03-stream-optional` | Stream API, `Optional` |
| `chapter04-functional-interface` | 함수형 인터페이스 (`Function`, `Predicate`, `Consumer`, `Supplier` 등) |
| `chapter05-modern-java-syntax` | `record`, `sealed`, switch expression, pattern matching |

### Phase 2 — JVM / 동시성

| 단원 | 주제 |
|---|---|
| `chapter06-jvm-memory-model` | JMM, happens-before, 메모리 가시성, GC 기초, 캐시 일관성 |
| `chapter07-thread-basics` | `Thread`, `synchronized`, `volatile`, Atomic, 경쟁조건 |
| `chapter08-executor-blocking-queue` | `ExecutorService`, ThreadPool, `BlockingQueue`, Producer-Consumer |
| `chapter09-completable-future` | `CompletableFuture`, 비동기 파이프라인, 예외 전파 |
| `chapter10-virtual-thread` | Project Loom, Virtual Thread, structured concurrency (**Java 21+ 필요**) |

### Phase 3 — 네트워크 / 웹

| 단원 | 주제 |
|---|---|
| `chapter11-tcp-http` | Socket TCP 서버 + HTTP 1.1 파싱 직접 구현 |
| `chapter12-mini-web-framework` | 라우터, 핸들러 체인, JSON 직렬화 |
| `chapter13-spring-boot-rest` | Spring Boot, JPA, 통합 테스트 |

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

- **Java 17 이상** (chapter01~07, chapter09~11)
- **Java 21 이상** (chapter08 Virtual Thread — Gradle toolchain이 자동 처리)
- Gradle 9.3.1 (`./gradlew`로 자동 사용, 별도 설치 불필요)
