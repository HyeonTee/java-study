# Chapter 03 — Stream API + Optional

> **공식 문서**: [Stream (Java 21)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html) · [Optional (Java 21)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Optional.html)

## Stream API

### Stream이란?

Stream은 **데이터 처리 파이프라인**이다. 컬렉션의 요소를 하나씩 꺼내서 변환, 필터, 집계하는 선언적 방식을 제공한다.

```
소스(Collection, 배열 등) → 중간 연산들 → 최종 연산 → 결과
```

### 핵심 특성

| 특성 | 설명 |
|---|---|
| **지연 평가 (Lazy)** | 중간 연산은 호출 시점에 실행되지 않는다. 최종 연산이 호출될 때 비로소 파이프라인 전체가 실행된다. |
| **일회성** | Stream은 한 번 소비하면 재사용할 수 없다. 다시 쓰려면 새 Stream을 만들어야 한다. |
| **원본 불변** | Stream 연산은 원본 컬렉션을 변경하지 않는다. 항상 새로운 결과를 만들어낸다. |

### 중간 연산 (Intermediate Operations)

파이프라인을 구성한다. 반환 타입이 `Stream`이므로 체이닝 가능. **Lazy** — 최종 연산 전까지 실행되지 않음.

| 메서드 | 동작 |
|---|---|
| `filter(Predicate)` | 조건에 맞는 요소만 통과 |
| `map(Function)` | 각 요소를 변환 |
| `flatMap(Function)` | 각 요소를 Stream으로 변환 후 하나로 합침 |
| `distinct()` | 중복 제거 (`equals` 기준) |
| `sorted()` / `sorted(Comparator)` | 정렬 |
| `limit(n)` | 앞에서 n개만 |
| `skip(n)` | 앞에서 n개 건너뜀 |
| `peek(Consumer)` | 디버깅용 — 각 요소를 들여다보되 변환하지 않음 |

### 최종 연산 (Terminal Operations)

파이프라인을 실행하고 결과를 만든다. 호출 즉시 전체 파이프라인이 동작(Eager).

| 메서드 | 동작 |
|---|---|
| `collect(Collector)` | 결과를 컬렉션/문자열/Map 등으로 모은다 |
| `reduce(identity, BinaryOperator)` | 요소를 하나로 합친다 |
| `forEach(Consumer)` | 각 요소에 대해 액션 수행 (반환값 없음) |
| `count()` | 요소 개수 |
| `findFirst()` / `findAny()` | 첫 번째 / 아무 요소 하나 (Optional 반환) |
| `anyMatch` / `allMatch` / `noneMatch` | 조건 만족 여부 (boolean) |
| `min(Comparator)` / `max(Comparator)` | 최솟값 / 최댓값 (Optional 반환) |
| `toList()` | (Java 16+) `collect(Collectors.toList())`의 축약형 |

### 주요 Collectors

| Collector | 동작 |
|---|---|
| `toList()` | List로 수집 |
| `toSet()` | Set으로 수집 |
| `toMap(keyMapper, valueMapper)` | Map으로 수집 |
| `groupingBy(classifier)` | 키 기준으로 그룹핑 → `Map<K, List<V>>` |
| `partitioningBy(predicate)` | true/false 두 그룹으로 분리 → `Map<Boolean, List<V>>` |
| `joining(delimiter)` | 문자열 연결 |
| `counting()` | 그룹 내 개수 |
| `averagingInt/Long/Double` | 평균 |

### 기본형 특화 Stream

`mapToInt`, `mapToLong`, `mapToDouble`로 기본형 Stream을 얻으면 **박싱 오버헤드 없이** `sum()`, `average()`, `max()` 등을 바로 쓸 수 있다.

```java
int total = numbers.stream().mapToInt(Integer::intValue).sum();
```

---

## Optional

### 왜 Optional인가?

`null`을 반환하면 호출자가 null 체크를 잊기 쉽고, NPE가 런타임에야 터진다. `Optional<T>`는 **"값이 있을 수도 없을 수도 있다"를 타입으로 표현**하여 컴파일 타임에 의도를 전달한다.

### 생성

| 메서드 | 동작 |
|---|---|
| `Optional.of(value)` | null이면 NPE. 확실히 non-null일 때 |
| `Optional.ofNullable(value)` | null이면 empty, 아니면 of |
| `Optional.empty()` | 빈 Optional |

### 값 꺼내기

| 메서드 | 동작 |
|---|---|
| `orElse(default)` | 없으면 default 반환. **default가 항상 평가됨** |
| `orElseGet(supplier)` | 없으면 supplier 호출. **필요할 때만 평가** |
| `orElseThrow()` | 없으면 `NoSuchElementException` |
| `orElseThrow(exSupplier)` | 없으면 지정한 예외 |

### 변환 (핵심 — map / flatMap / filter)

Optional의 진짜 가치는 **체이닝**에 있다. null 체크 if문 대신 파이프라인으로 표현.

| 메서드 | 동작 |
|---|---|
| `map(Function)` | 값이 있으면 변환, 없으면 empty 유지 |
| `flatMap(Function)` | 변환 결과가 Optional일 때 — 중첩 방지 |
| `filter(Predicate)` | 조건 불만족 시 empty로 |

```java
// if-null 지옥
String city = null;
if (user != null) {
    Address addr = user.getAddress();
    if (addr != null) {
        city = addr.getCity();
    }
}

// Optional 체이닝
String city = Optional.ofNullable(user)
    .map(User::getAddress)
    .map(Address::getCity)
    .orElse("unknown");
```

### orElse vs orElseGet

```java
opt.orElse(expensiveCall());       // opt에 값이 있어도 expensiveCall() 실행됨
opt.orElseGet(() -> expensiveCall()); // opt에 값이 없을 때만 실행
```

부작용이 있거나 비용이 큰 기본값은 반드시 `orElseGet`을 써야 한다.

---

## 연습 문제

### StreamPractice (12문제)

쉬운 단일 연산부터 다단계 파이프라인까지 점진적으로 배치.

| # | 메서드 | 핵심 연산 |
|---|---|---|
| 1 | `filterEven` | filter |
| 2 | `mapToUpperCase` | map |
| 3 | `sumAll` | mapToInt + sum (또는 reduce) |
| 4 | `findMax` | max |
| 5 | `flattenAndSort` | flatMap + sorted |
| 6 | `uniqueElements` | distinct |
| 7 | `topN` | sorted + limit |
| 8 | `groupByLength` | Collectors.groupingBy |
| 9 | `partitionByPositive` | Collectors.partitioningBy |
| 10 | `joinStrings` | Collectors.joining |
| 11 | `average` | mapToInt + average |
| 12 | `wordFrequency` | flatMap + groupingBy + counting |

### OptionalPractice (8문제)

| # | 메서드 | 핵심 연산 |
|---|---|---|
| 1 | `findFirstMatch` | stream + filter + findFirst |
| 2 | `getOrDefault` | orElse |
| 3 | `getOrCompute` | orElseGet |
| 4 | `getOrThrow` | orElseThrow |
| 5 | `safeParseInt` | try-catch + Optional.of / empty |
| 6 | `toUpperIfPresent` | map |
| 7 | `firstChar` | flatMap |
| 8 | `filterByLength` | filter |
