# Chapter 07 — 예외 모델 (검사/비검사 설계, 커스텀 계층, cause 체이닝, try-with-resources, 람다 어댑팅)

> **선행 단원**: Chapter 04(함수형 인터페이스·람다 합성), Chapter 06(record·sealed·switch). ch04는 "함수를 값으로 다루고 합성하는 법"을, ch06은 "sealed로 타입 계층을 닫는 법"을 다뤘다. 이 단원은 그 둘을 **예외**라는 한 가지 주제 위에서 합친다 — 예외 계층을 sealed로 닫고(ch06), 검사예외를 람다로 감싸 비검사로 어댑팅한다(ch04).

> **공식 문서**: [`Throwable`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Throwable.html) · [`RuntimeException`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/RuntimeException.html) · [`AutoCloseable`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/AutoCloseable.html) · [try-with-resources (JLS 14.20.3)](https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.20.3) · [`Throwable.getSuppressed`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Throwable.html#getSuppressed())

---

## 왜 이 단원이 생겼나

지금까지 예외는 커리큘럼 곳곳에 **파편**으로만 등장했다. ch04에서 "검사예외를 람다 안에서 던질 수 없어 RuntimeException으로 감싸야 한다"는 문제를 잠깐 만났고, 뒤의 ch15(io-basics)에서는 try-with-resources를 파일 입출력의 부속물로 다룰 것이다. 정작 **"예외 모델 그 자체"** — 어떤 예외를 검사로 둘지, 계층을 어떻게 닫을지, 원인을 어떻게 보존할지 — 를 정면으로 가르친 적이 없었다. 이 단원이 그 공백을 메운다.

그래서 이 단원은 phase1의 **토대 마무리**다. 뒤에서 예외가 다시 등장할 때 — ch10(reflection-annotations), ch15(io-basics, try-with-resources), ch17(http-protocol), ch18(concurrent-http-server), ch19(mini-web-framework)가 전부 **커스텀 예외를 정의·전파**한다 — 그때 "왜 이렇게 설계하나"를 여기서 한 번 정립해 두면 매번 다시 고민하지 않아도 된다.

---

## 이 단원의 큰 그림 — "예외를 다루는 5축"

```
1. 검사 vs 비검사 설계 결정     — 이 예외, 호출자가 복구할 수 있나? 그게 분기 기준
        ↓
2. 커스텀 예외 계층 (sealed)    — 닫힌 계층으로 "가능한 실패의 집합"을 타입에 새긴다 (ch06)
        ↓
3. cause 체이닝                — 추상화 경계를 넘을 때 원인을 잃지 않는다 (super(msg, cause))
        ↓
4. try-with-resources          — AutoCloseable 계약, close 역순, suppressed 예외
        ↓
5. 람다 + 검사 예외             — 함수형 시그니처는 검사예외를 못 던진다 → 감싸서 어댑팅 (ch04)
```

다루지 않는 것(경계): **예외 성능 튜닝**(`fillInStackTrace` 생략 등 핫패스 최적화), **로깅 프레임워크**(SLF4J/Logback), **`Error` 계열**(`OutOfMemoryError`처럼 잡지 말아야 할 것), **다국어 메시지**. 이 단원은 "예외를 설계·전파·소비하는 원리"로 한정한다.

---

## 1. 검사 vs 비검사 — 어디에 선을 긋나

Java 예외는 둘로 갈린다:

- **검사 예외(checked)** — `Exception`의 하위지만 `RuntimeException`은 **아닌** 것. 컴파일러가 `throws` 선언이나 `try/catch`를 **강제**한다.
- **비검사 예외(unchecked)** — `RuntimeException` 및 그 하위. 컴파일러가 강제하지 않는다.

선을 긋는 기준은 단 하나: **"이 실패를 호출자가 합리적으로 복구할 수 있는가?"**

| | 검사 예외 | 비검사 예외 |
|---|---|---|
| 의미 | "복구 가능한 외부 조건" | "프로그래밍 오류 / 복구 불가" |
| 예 | 파일 없음, 네트워크 타임아웃 | 잘못된 인자, 깨진 불변식, 설정 누락 |
| 호출자 부담 | 매번 처리 강제 | 처리 선택적 |

현대 Java(및 Spring 같은 프레임워크)가 **대부분 비검사 쪽으로 설계**하는 이유: 검사 예외는 호출 스택을 따라 `throws`가 전염되고, 람다·스트림 시그니처와 충돌하며(→ 5축), 호출자가 정말 복구하는 경우는 생각보다 드물다. "복구할 게 아니면 검사로 강제하지 마라"가 실무 합의에 가깝다.

> **함정 — `catch (Exception e) {}`**: 빈 catch, 또는 너무 넓은 catch는 예외를 **삼켜** 버그를 침묵시킨다. 잡았으면 (a) 복구하든가, (b) 변환해 다시 던지든가, (c) 최소한 원인을 남겨라. "잡고 아무것도 안 하기"가 가장 나쁘다.

> **함정 — 흐름 제어용 예외**: 정상적인 분기(예: "키가 없으면 기본값")를 예외로 처리하면 비싸고 읽기 어렵다. 예외는 *예외적인* 경우에만.

이 단원의 `ConfigException` 계층은 **비검사**(`RuntimeException` 상속)다. 설정 누락·형식 오류는 "프로그램이 잘못 구성된" 상황이므로 호출자에게 매 호출 `throws`를 강요하는 대신 비검사로 둔다 — 이 결정 자체가 1축의 실습이다.

---

## 2. 커스텀 예외 계층 — sealed로 "실패의 집합"을 닫다

새 예외 타입은 **언제** 만드나? "호출자가 이 실패를 *다른 실패와 구분해* 다뤄야 할 때". 구분할 필요가 없으면 기존 예외(`IllegalArgumentException` 등)를 재사용하는 게 낫다.

ch06에서 배운 `sealed`를 여기 적용한다. `ConfigException`을 sealed abstract로 두고 허용 하위를 명시하면, "설정에서 날 수 있는 예외는 정확히 이 둘"이라는 사실이 **타입에 박힌다**:

```java
// 형태만 — 본문은 GIVEN으로 제공된다
public sealed abstract class ConfigException extends RuntimeException
        permits MissingKeyException, MalformedValueException {
    // (String) 와 (String, Throwable) 두 생성자를 가진다
}
```

이렇게 닫으면 switch 패턴 매칭이 `default` 없이도 망라성을 보장받고(ch06), 새 실패 종류를 추가하려면 `permits`를 고쳐야 하므로 "조용히 늘어나는" 일이 없다.

하위 예외의 **필드 설계**도 중요하다 — 메시지 문자열에 정보를 묻어 두지 말고 구조화해 노출하라:

- `MissingKeyException` → `String key` 필드 + `key()` 접근자. "어떤 키가 없었나"를 호출자가 코드로 꺼낼 수 있다.
- `MalformedValueException` → `key`·`rawValue` 필드 + 접근자. "어떤 키의 어떤 원본 값이 깨졌나"를 보존한다.

> **함정 — 메시지에만 의존**: `throw new RuntimeException("key 'port' not found")`는 호출자가 `port`를 다시 쓰려면 문자열을 파싱해야 한다. 필요한 정보는 **필드**로 노출하라.

---

## 3. cause 체이닝 — 원인을 잃지 않기

저수준 예외(`NumberFormatException`)를 잡아 도메인 예외(`MalformedValueException`)로 바꿔 던지는 건 좋은 추상화다. 단, **원인을 버리면** 안 된다:

```java
// 나쁨 — NumberFormatException의 스택트레이스가 증발한다
catch (NumberFormatException e) {
    throw new MalformedValueException(key, raw, null); // cause 끊김!
}

// 좋은 형태 — cause를 넘긴다 (super(msg, cause)가 내부에서 연결)
catch (NumberFormatException e) {
    throw new MalformedValueException(key, raw, e);    // cause 보존
}
```

체인을 끊으면 로그에 "왜 깨졌는지"의 진짜 뿌리(`NumberFormatException: For input string ...`)가 사라져 디버깅이 죽는다. `Throwable`은 `getCause()`로 한 단계씩 원인을 따라갈 수 있고, **루트 원인**은 그 체인을 끝까지 따라간 마지막 예외다.

> **함정 — self-cause 무한 루프**: 드물게 `getCause()`가 자기 자신(또는 순환)을 가리키면 루트 원인 탐색이 무한 루프에 빠진다. `Exceptions.rootCause`를 구현할 때 "다음 cause가 현재와 같거나 null이면 멈춘다"는 방어가 필요하다.

`Exceptions.rootCause(Throwable)` 연습이 정확히 이 언랩과 self-cause 방어를 다룬다.

---

## 4. try-with-resources — close 계약과 suppressed 예외

`AutoCloseable`을 구현한 자원을 `try (...)` 헤더에 선언하면, 블록을 어떻게 빠져나가든(정상 종료/예외/return) **자동으로 `close()`가 불린다**. 핵심 규칙:

- **역순 close**: `try (A a = ...; B b = ...)`에서 닫는 순서는 **B → A** (열린 역순). 나중에 연 것을 먼저 닫는다.
- **suppressed 예외**: 본문에서 예외가 나고 *그 와중에* `close()`도 예외를 던지면, **본문 예외가 primary**가 되고 `close()` 예외는 거기에 **suppressed로 붙는다**. primary는 사라지지 않는다 — `primary.getSuppressed()`로 close 예외를 꺼낼 수 있다.

```
try 본문에서 RuntimeException("primary") 발생
   → close()도 IllegalStateException 던짐
   → 호출자가 받는 건 primary, 그 .getSuppressed()[0] 이 close 예외
```

이는 옛날 수동 `finally { close(); }` 방식과 정반대다. 수동 finally에서 close가 던지면 **그게 본문 예외를 덮어써** 진짜 원인을 잃었다. try-with-resources는 이 함정을 언어 차원에서 고친다.

> **함정 — `finally`의 `return`**: `finally` 블록 안에서 `return`하면 try 본문에서 던지던 예외가 **조용히 삼켜진다**. 같은 원리로 `finally`에서 새 예외를 던지면 본문 예외를 덮는다. 자원 정리는 `finally` 대신 try-with-resources에 맡겨라.

`TrackedResource`(open/use/close를 로그 리스트에 기록)와 `ResourceScenarios`가 이 축을 눈으로 확인시킨다 — 로그 순서로 역순 close를, 반환된 예외의 `getSuppressed()`로 억제 관계를 관찰한다.

---

## 5. 람다 + 검사 예외 — 어댑팅으로 경계 넘기

함수형 인터페이스(`Supplier<T>`, `Function<T,R>` 등)의 추상 메서드 시그니처에는 `throws`가 없다. 그래서 **검사예외를 던지는 코드를 람다 안에 그대로 넣을 수 없다**(ch04에서 만난 바로 그 벽):

```java
// 컴파일 안 됨 — get()은 검사예외를 던질 수 없다
Supplier<String> s = () -> Files.readString(path); // IOException 검사예외!
```

해법: 검사예외를 던지는 **자기만의 함수형 인터페이스**(`ThrowingSupplier`, `ThrowingFunction`)를 정의하고, 이를 표준 `Supplier`/`Function`으로 **어댑팅**하는 고차 함수를 둔다. 어댑터 안에서 검사예외를 잡아 비검사(`RuntimeException`)로 **감싸** 던지면, 바깥 세계는 표준 시그니처만 본다.

```java
// 형태 — GIVEN으로 제공되는 인터페이스
@FunctionalInterface interface ThrowingSupplier<T> { T get() throws Exception; }
@FunctionalInterface interface ThrowingFunction<T,R> { R apply(T t) throws Exception; }
```

여기서 1축(검사→비검사 변환)·3축(원인 보존)·ch04(고차 함수로 함수를 감싸 반환)가 한 점에서 만난다. `uncheck`/`unchecked`가 이 어댑터들이다 — **비검사 예외는 그대로 통과**시키고 **검사 예외만 감싸는** 게 정확한 계약임에 주의하라(이미 RuntimeException인 걸 또 감싸면 체인이 한 겹 더 깊어져 읽기 나빠진다).

---

## 이 단원에서 만들 것 (스텁) 과 각 축 매핑

> **GIVEN(완성 제공, 읽기만)**: 함수형 인터페이스 `ThrowingSupplier<T>`·`ThrowingFunction<T,R>`, 그리고 예외 계층 `ConfigException`(sealed) · `MissingKeyException` · `MalformedValueException`. 이들은 **읽고 계약을 파악**하는 대상이다 — 직접 구현하지 않는다.

패키지 `study.chapter07`. 학습자가 채울 STUB은 넷:

| 클래스 | 연습하는 축 | 핵심 |
|---|---|---|
| `Config` | 1·2·3 | `require(key)`→없으면 `MissingKeyException`. `getInt(key)`→`require` 후 `Integer.parseInt`, 실패 시 `NumberFormatException`을 **cause로 보존**해 `MalformedValueException`으로 변환 |
| `Exceptions` (static util) | 3·5 | `rootCause`(체인 끝까지 + self-cause 방어), `uncheck(ThrowingSupplier)`(검사예외만 래핑), `unchecked(ThrowingFunction)`→`Function` 어댑터 |
| `TrackedResource` (`AutoCloseable`) | 4 | `use()`→`"use:name"` 로그, `close()`→`"close:name"` 로그 + `failOnClose`면 `IllegalStateException`. (생성자·open 로그는 제공) |
| `ResourceScenarios` (static) | 4 | `openTwoAndUse`→try-with-resources로 A·B 열고 use()(로그로 역순 close 관찰). `captureSuppressed`→본문 `RuntimeException("primary")`를 catch해 반환(suppressed 관찰용) |

---

## 진행 순서 (권장)

1. **GIVEN 읽기** — `ConfigException` 계층(sealed의 `permits`)과 `Throwing*` 인터페이스 시그니처를 먼저 눈으로 파악. "이 단원이 다룰 실패의 집합"이 여기 박혀 있다.
2. **`Config`** (1·2·3축) — `require` → `getInt` 순서로. `getInt`에서 cause 보존이 핵심.
3. **`Exceptions`** (3·5축) — `rootCause`(self-cause 방어부터) → `uncheck` → `unchecked`. "검사만 감싸고 비검사는 통과"를 정확히.
4. **`TrackedResource` + `ResourceScenarios`** (4축) — close 로그·역순·suppressed를 로그와 `getSuppressed()`로 확인.

---

## 실행

```sh
# 전체 테스트 (스텁이 비어 있어 빨간불 — 구현하며 초록불로)
./gradlew :chapter07-exception-model:test

# 특정 클래스
./gradlew :chapter07-exception-model:test --tests "study.chapter07.ConfigTest"
```

---

## 생각해볼 거리

1. **검사로 둘까, 비검사로 둘까?** 이 단원은 `ConfigException`을 비검사로 설계했다. 만약 "설정 파일을 디스크에서 읽다 실패"라면(호출자가 다른 경로로 재시도할 수 있다면) 검사가 나을까? "복구 가능성"이라는 기준을 실제 사례에 대 보라.

2. **`uncheck`가 비검사 예외까지 또 감싸면** 무슨 일이 생기나? 호출자가 원래 `IllegalArgumentException`을 잡으려 했는데 `RuntimeException`으로 한 겹 덮이면 catch가 빗나간다. "이미 비검사면 그대로 통과"가 왜 계약의 일부인지 설명해 보라.

3. **루트 원인 vs 전체 체인** — `rootCause`는 맨 끝 하나만 준다. 로그에는 보통 *전체* 체인(`Caused by: ...`)을 찍는다. 둘은 언제 각각 유용한가?

4. **try-with-resources의 역순 close가 왜 옳은가?** B가 A에 의존해 열렸다면(A 위에 B를 쌓았다면), 닫을 때 B를 먼저 닫아야 A가 아직 살아 있는 상태에서 정리된다. 자원 의존 그래프로 생각해 보라.

5. **suppressed 예외를 무시하면?** primary만 보고 close 실패를 흘리면, 예컨대 "파일은 썼지만 flush/close에 실패해 데이터가 안 남은" 사고를 놓친다. close 예외가 *때로는* 더 중요한 경우는 언제인가?

6. **앞으로의 연결** — ch15(try-with-resources로 파일·스트림), ch17~ch19(HTTP 계층의 커스텀 예외)에서 이 5축이 어떻게 재등장할지 미리 그려 보라. 특히 ch19 미니 웹 프레임워크의 예외→HTTP 상태 코드 매핑은 2축(닫힌 계층)이 빛나는 자리다.
