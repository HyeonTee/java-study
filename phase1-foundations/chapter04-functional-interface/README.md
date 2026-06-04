# Chapter 04 — 함수형 인터페이스

## 함수형 인터페이스란?

**추상 메서드가 정확히 하나**인 인터페이스. 람다식이나 메서드 참조로 인스턴스를 만들 수 있다.

```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);          // 추상 메서드 1개 → 함수형 인터페이스
    // default, static 메서드는 몇 개든 있어도 상관없다
}
```

`@FunctionalInterface` 어노테이션은 선택 사항이지만, 붙이면 컴파일러가 "추상 메서드가 2개 이상이면 에러"를 내준다. 의도를 명시하는 안전장치.

---

## 람다 표현식

함수형 인터페이스의 인스턴스를 간결하게 만드는 문법.

```java
// 익명 클래스
Predicate<String> p = new Predicate<>() {
    @Override
    public boolean test(String s) {
        return s.length() > 3;
    }
};

// 람다
Predicate<String> p = s -> s.length() > 3;

// 블록 본문 (여러 줄)
Predicate<String> p = s -> {
    int len = s.length();
    return len > 3;
};
```

**타입 추론**: 파라미터 타입은 컴파일러가 추론한다. `(String s) -> ...` 대신 `s -> ...`로 충분.

---

## 메서드 참조

람다를 더 간결하게 쓰는 축약 문법. 4가지 형태:

| 형태 | 예시 | 동등 람다 |
|---|---|---|
| 정적 메서드 | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| 인스턴스 메서드 (임의 객체) | `String::toUpperCase` | `s -> s.toUpperCase()` |
| 인스턴스 메서드 (특정 객체) | `System.out::println` | `x -> System.out.println(x)` |
| 생성자 | `ArrayList::new` | `() -> new ArrayList<>()` |

---

## 표준 함수형 인터페이스 (`java.util.function`)

외울 필요 없이 패턴만 이해하면 된다:

| 인터페이스 | 시그니처 | 용도 | 예시 |
|---|---|---|---|
| `Function<T, R>` | `T → R` | 변환 | `Integer::parseInt` |
| `Predicate<T>` | `T → boolean` | 조건 판별 | `s -> s.isEmpty()` |
| `Consumer<T>` | `T → void` | 소비 (부작용) | `System.out::println` |
| `Supplier<T>` | `() → T` | 생성 (지연 평가) | `() -> new Random()` |
| `UnaryOperator<T>` | `T → T` | 같은 타입 변환 | `String::toUpperCase` |
| `BinaryOperator<T>` | `(T, T) → T` | 두 값을 합침 | `Integer::sum` |
| `BiFunction<T, U, R>` | `(T, U) → R` | 두 입력 → 하나 | `(a, b) -> a + b` |
| `BiPredicate<T, U>` | `(T, U) → boolean` | 두 입력 조건 | `String::startsWith` |

`UnaryOperator<T>`는 `Function<T, T>`의 특수 형태이고, `BinaryOperator<T>`는 `BiFunction<T, T, T>`의 특수 형태이다.

---

## 합성 (Composition)

함수형 인터페이스의 진짜 힘. 작은 함수를 조합해 큰 함수를 만든다.

### Function — andThen / compose

```java
Function<String, String> trim = String::trim;
Function<String, String> upper = String::toUpperCase;

// andThen: trim → upper (왼쪽 먼저)
Function<String, String> trimThenUpper = trim.andThen(upper);
trimThenUpper.apply("  hello  ");  // "HELLO"

// compose: upper → trim (오른쪽 먼저) — 수학의 f∘g = f(g(x))
Function<String, String> upperAfterTrim = upper.compose(trim);
upperAfterTrim.apply("  hello  ");  // "HELLO"
```

`andThen`은 읽기 순서대로, `compose`는 수학 순서대로. **실무에서는 `andThen`이 더 직관적**이라 많이 쓴다.

### Predicate — and / or / negate

```java
Predicate<Integer> positive = n -> n > 0;
Predicate<Integer> even = n -> n % 2 == 0;

Predicate<Integer> positiveAndEven = positive.and(even);   // 둘 다 만족
Predicate<Integer> positiveOrEven = positive.or(even);     // 하나라도 만족
Predicate<Integer> notPositive = positive.negate();        // 반전
```

### Consumer — andThen

```java
Consumer<String> log = s -> System.out.println("LOG: " + s);
Consumer<String> save = s -> db.save(s);

Consumer<String> logThenSave = log.andThen(save);  // log 먼저, save 다음
```

---

## 고차 함수 (Higher-Order Function)

**함수를 파라미터로 받거나, 함수를 반환하는 함수.** 함수형 프로그래밍의 핵심.

### 함수를 반환하는 패턴 — 팩토리

```java
// n배 곱셈기를 만드는 팩토리
static Function<Integer, Integer> createMultiplier(int factor) {
    return x -> x * factor;
}

Function<Integer, Integer> triple = createMultiplier(3);
triple.apply(5);  // 15
```

`factor`가 람다 안에 **캡처**된다 (클로저). 이 변수는 effectively final이어야 한다.

### 함수를 받아서 새 함수를 만드는 패턴

```java
// 조건부 적용: 조건을 만족할 때만 변환, 아니면 원본 유지
static <T> UnaryOperator<T> conditionalApply(Predicate<T> condition, UnaryOperator<T> op) {
    return t -> condition.test(t) ? op.apply(t) : t;
}
```

---

## 두 입력을 합치는 함수 — BinaryOperator와 fold

지금까지는 입력이 하나인 함수만 다뤘다. **두 값을 하나로 합치는** 함수가 `BinaryOperator<T>` (`(T, T) → T`)다.

이 함수를 리스트에 반복 적용하면 **누적(fold/reduce)**이 된다. 초기값(identity)에서 시작해 원소를 하나씩 합쳐 나간다:

```java
// [1, 2, 3, 4]를 0에서 시작해 합산
int sum = 0;                          // identity
for (int x : List.of(1, 2, 3, 4)) {
    sum = Integer.sum(sum, x);        // BinaryOperator 적용
}
// sum == 10
```

이것이 Chapter 05에서 만날 `Stream.reduce(identity, combiner)`의 동작 원리다. 여기서 직접 손으로 만들어 보면 Stream의 `reduce`가 낯설지 않다.

**identity의 의미**: 빈 리스트에 fold하면 identity가 그대로 나온다. 합산이면 0, 곱셈이면 1, 문자열 연결이면 `""`이 identity다.

---

## 지연 평가 (Lazy Evaluation) — Supplier

`Supplier<T>` (`() → T`)는 값을 **나중에, 필요할 때** 만들기 위한 인터페이스다.

기본값을 줄 때 값 자체가 아니라 Supplier를 받으면, **그 기본값이 실제로 필요할 때만 계산**할 수 있다:

```java
// orElse: 기본값을 항상 계산한다 (값이 있어도 expensive() 호출됨)
String a = (value != null) ? value : expensive();

// orElseGet 패턴: 기본값을 필요할 때만 계산한다 (값이 있으면 supplier 미호출)
String b = (value != null) ? value : defaultSupplier.get();
```

`Optional`의 `orElse`(값) vs `orElseGet`(Supplier) 차이가 정확히 이것이다. 기본값 계산이 비싸거나 부작용이 있다면 **반드시 지연 평가**해야 한다.

---

## 커스텀 함수형 인터페이스

표준 인터페이스로 부족할 때 직접 만든다. 핵심은 **추상 메서드 1개 + default/static은 자유**.

```java
@FunctionalInterface
public interface Converter<T, R> {
    R convert(T input);

    // default: 합성
    default <V> Converter<T, V> andThen(Converter<R, V> after) {
        return input -> after.convert(this.convert(input));
    }

    // static: 팩토리
    static <T> Converter<T, T> identity() {
        return input -> input;
    }
}
```

---

## 람다와 검사 예외 (Checked Exception)

표준 함수형 인터페이스의 메서드는 **검사 예외(checked exception)를 던질 수 없다**. `Function.apply`, `Supplier.get` 등의 시그니처에 `throws`가 없기 때문이다.

```java
// 컴파일 에러 — Files.readString은 IOException(검사 예외)을 던진다
Function<Path, String> read = Files::readString;
```

이건 Stream 파이프라인에서 자주 부딪히는 벽이다. 해결책은 **검사 예외를 던질 수 있는 커스텀 함수형 인터페이스**로 받은 뒤, **비검사 예외로 감싸 다시 던지는(wrap-and-rethrow)** 어댑터로 표준 `Function` 자리에 끼워 넣는 것이다.

```java
@FunctionalInterface
interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;     // 검사 예외 허용
}

static <T, R> Function<T, R> unchecked(ThrowingFunction<T, R> f) {
    return t -> {
        try {
            return f.apply(t);
        } catch (Exception e) {
            throw new RuntimeException(e);   // 비검사로 감싸 다시 던짐
        }
    };
}
```

이 wrap-and-rethrow 패턴은 Phase 2의 `CompletableFuture` 예외 전파(ch12)에서 다시 만난다. 검사 예외를 비검사로 바꾸면 원래 예외를 `cause`로 보존하는 것이 중요하다.

---

## 연습 문제

### FunctionalPractice (13문제)

| # | 메서드 | 핵심 |
|---|---|---|
| 1 | `chainFunctions` | 함수 리스트를 순서대로 합성 |
| 2 | `allMatch` | Predicate 리스트 AND 조합 |
| 3 | `anyMatch` | Predicate 리스트 OR 조합 |
| 4 | `negatePredicate` | Predicate.negate |
| 5 | `createMultiplier` | 고차 함수 — Function 팩토리 |
| 6 | `createRangeChecker` | 고차 함수 — Predicate 팩토리 |
| 7 | `createFormatter` | 고차 함수 — UnaryOperator 팩토리 |
| 8 | `applyN` | UnaryOperator를 n번 반복 적용 |
| 9 | `conditionalApply` | Predicate + UnaryOperator 조합 |
| 10 | `memoize` | Supplier 캐싱 |
| 11 | `reduce` | BinaryOperator로 fold (Stream.reduce 손구현) |
| 12 | `chainConsumers` | Consumer 합성 — 같은 입력에 부수효과 fan-out |
| 13 | `lazyOrElse` | Supplier 지연 평가 (Optional.orElseGet 패턴) |

### Converter (3문제)

커스텀 `@FunctionalInterface` 직접 완성하기.

| # | 메서드 | 핵심 |
|---|---|---|
| 1 | `convert` | 추상 메서드 (이미 정의됨) |
| 2 | `andThen` | default 메서드 — 합성 |
| 3 | `identity` | static 팩토리 |

### ThrowingFunctionPractice (2문제)

검사 예외를 던지는 함수를 표준 `Function`으로 변환. ch12 예외 전파의 선수 패턴.

| # | 메서드 | 핵심 |
|---|---|---|
| 1 | `unchecked` | 검사 예외 → `RuntimeException` 래핑 (cause 보존) |
| 2 | `withDefault` | 예외 시 기본값 반환 |
