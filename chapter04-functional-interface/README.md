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

## 연습 문제

### FunctionalPractice (10문제)

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

### Converter (3문제)

커스텀 `@FunctionalInterface` 직접 완성하기.

| # | 메서드 | 핵심 |
|---|---|---|
| 1 | `convert` | 추상 메서드 (이미 정의됨) |
| 2 | `andThen` | default 메서드 — 합성 |
| 3 | `identity` | static 팩토리 |
