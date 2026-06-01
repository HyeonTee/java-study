# Chapter 06 — 현대 Java 문법

> **선행 단원**: `ShapePractice.largestArea()`는 Chapter 05(Stream + Optional)의 `Stream.max()`와 `Optional`을 활용한다.

> **공식 문서**: [Record (Java 21)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Record.html) · [Sealed Classes (JEP 409)](https://openjdk.org/jeps/409) · [Pattern Matching for switch (JEP 441)](https://openjdk.org/jeps/441)

## Record

### Record란?

**불변 데이터 캐리어**를 한 줄로 정의하는 문법. Java 16에서 정식 도입.

```java
public record Point(int x, int y) {}
```

이 한 줄이 아래를 자동 생성한다:
- `private final` 필드 (`x`, `y`)
- 생성자 (`new Point(1, 2)`)
- getter (`point.x()`, `point.y()` — `get` 접두사 없음)
- `equals()`, `hashCode()`, `toString()`

이 보일러플레이트를 한 줄로 줄여주는 것이 record의 핵심이다.

### 컴팩트 생성자 (Compact Constructor)

생성자에서 **유효성 검증**을 할 때 사용한다. 파라미터 목록을 생략하고 바로 본문을 쓴다:

```java
public record Range(int from, int to) {
    public Range {  // 파라미터 목록 없음 — 컴팩트 생성자
        if (from > to) {
            throw new IllegalArgumentException("from > to");
        }
        // 필드 할당은 자동으로 처리됨 — this.from = from 불필요
    }
}
```

### 커스텀 메서드

Record에 메서드를 추가할 수 있다. 다만 필드는 final이므로 값을 변경하는 메서드는 **새 인스턴스를 반환**해야 한다:

```java
public record Range(int from, int to) {
    public int length() {
        return to - from;
    }

    public Range shift(int offset) {
        return new Range(from + offset, to + offset);  // 새 인스턴스
    }
}
```

### Record의 제약

- 필드는 전부 `final` — 불변
- 다른 클래스를 `extends` 불가 (암묵적으로 `java.lang.Record` 상속)
- `sealed interface`는 `implements` 가능

---

## Sealed 클래스/인터페이스

### 왜 필요한가?

"이 타입의 하위 타입은 이것들뿐이다"를 **타입 시스템으로 보장**한다. Java 17에서 정식 도입.

하위 타입을 제한하면, switch에서 모든 경우를 빠짐없이 다룰 수 있고 컴파일러가 이를 검증해준다.

```java
public sealed interface Shape permits Circle, Rectangle, Triangle {}
public record Circle(double radius) implements Shape {}
public record Rectangle(double width, double height) implements Shape {}
public record Triangle(double base, double height) implements Shape {}
```

### permits

`permits` 키워드로 허용할 하위 타입을 나열한다. 목록에 없는 클래스가 `implements Shape`를 시도하면 컴파일 에러.

같은 파일/패키지 안에 있으면 `permits`를 생략할 수도 있다 (컴파일러가 자동 추론).

### sealed가 pattern matching과 만날 때

하위 타입이 확정되어 있으므로, switch에서 **exhaustiveness(완전성) 검사**가 가능하다:

```java
double area(Shape shape) {
    return switch (shape) {
        case Circle c    -> Math.PI * c.radius() * c.radius();
        case Rectangle r -> r.width() * r.height();
        case Triangle t  -> 0.5 * t.base() * t.height();
        // default 불필요 — 컴파일러가 모든 케이스를 다뤘음을 안다
    };
}
```

`default`를 안 써도 컴파일 에러가 나지 않는다. 나중에 `Pentagon`을 추가하면 switch를 수정하지 않은 곳에서 **컴파일 에러**가 나서 누락을 방지한다.

---

## Switch Expression

### 기존 switch vs switch expression

```java
// 기존 (statement) — fall-through 위험, break 필요
String result;
switch (day) {
    case "MONDAY":
    case "TUESDAY":
        result = "WEEKDAY";
        break;
    case "SATURDAY":
        result = "WEEKEND";
        break;
    default:
        result = "UNKNOWN";
}

// switch expression (Java 14+) — 값을 반환, fall-through 없음
String result = switch (day) {
    case "MONDAY", "TUESDAY" -> "WEEKDAY";
    case "SATURDAY"          -> "WEEKEND";
    default                  -> "UNKNOWN";
};
```

### 핵심 차이

| | 기존 switch | switch expression |
|---|---|---|
| 값 반환 | 불가 (statement) | 가능 (expression) |
| fall-through | `break` 빠뜨리면 발생 | 없음 (arrow `->`) |
| 여러 case | 한 줄씩 나열 | 콤마로 묶기 (`case A, B ->`) |
| exhaustiveness | 체크 안 함 | enum/sealed 타입은 체크 |

### yield

블록 안에서 값을 반환할 때 사용:

```java
String desc = switch (score / 10) {
    case 10, 9 -> "A";
    case 8     -> "B";
    default -> {
        String msg = "Grade for score " + score;
        yield msg + ": below B";  // 블록에서 값 반환
    }
};
```

---

## Pattern Matching

### instanceof 패턴 매칭 (Java 16+)

```java
// 기존
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.length());
}

// 패턴 매칭 — 캐스팅 자동
if (obj instanceof String s) {
    System.out.println(s.length());
}
```

### switch 패턴 매칭 (Java 21)

```java
String describe(Object obj) {
    return switch (obj) {
        case Integer i -> "Integer: " + i;
        case String s  -> "String: " + s;
        case null      -> "null";
        default        -> "Unknown: " + obj;
    };
}
```

### Guarded Pattern (when 절)

```java
String format(Object obj) {
    return switch (obj) {
        case Integer i when i > 0  -> "positive: " + i;
        case Integer i             -> "non-positive: " + i;
        case String s when s.isEmpty() -> "empty string";
        case String s              -> "string: " + s;
        default                    -> "other";
    };
}
```

`when` 뒤의 조건이 true일 때만 매칭된다. **순서가 중요** — 더 구체적인 패턴을 위에 써야 한다.

---

## 연습 문제

### Range (4문제)

Record + 컴팩트 생성자 연습.

| # | 메서드 | 핵심 |
|---|---|---|
| 1 | compact constructor | 유효성 검증 (from <= to) |
| 2 | `length()` | to - from |
| 3 | `contains(int)` | 범위 포함 여부 |
| 4 | `overlaps(Range)` | 두 범위 겹침 여부 |

### ShapePractice (4문제)

Sealed interface + Record + Pattern matching switch 조합.

| # | 메서드 | 핵심 |
|---|---|---|
| 1 | `area(Shape)` | sealed 타입 pattern matching |
| 2 | `describe(Shape)` | record 분해 + 문자열 생성 |
| 3 | `scale(Shape, double)` | record → 새 record 반환 |
| 4 | `largestArea(List<Shape>)` | Stream + pattern matching 조합 |

### SwitchPractice (4문제)

Switch expression 문법 연습.

| # | 메서드 | 핵심 |
|---|---|---|
| 1 | `dayType(String)` | 화살표 문법, 여러 case 묶기 |
| 2 | `seasonOf(int)` | 정수 switch expression |
| 3 | `describe(Object)` | 패턴 매칭 switch + null 처리 |
| 4 | `formatNumber(Object)` | guarded pattern (when 절) |
