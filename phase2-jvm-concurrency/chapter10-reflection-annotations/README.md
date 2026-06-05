# Chapter 10 — 리플렉션 · 애너테이션 · 동적 프록시

> **선행 단원**: Chapter 09(객체 모델). ch09은 클래스 계층과 동적 디스패치를 `ClassNode`라는 **손으로 만든 데이터로 흉내** 냈다(`MethodTable`). 이 단원은 JVM이 **실제로 들고 있는** `Class`/`Method`/`Field` 객체를 직접 만진다 — "내가 만든 메타데이터 모델 → JVM의 실물 메타데이터 API"로의 도약. ch09이 정적·개념이었다면 이 단원은 그 **런타임 거울**이다.

> **공식 문서**: [`java.lang.reflect`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/reflect/package-summary.html) · [`java.lang.annotation`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/annotation/package-summary.html) · [JLS 9.6 — Annotation Types](https://docs.oracle.com/javase/specs/jls/se21/html/jls-9.html#jls-9.6) · [`Proxy`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/reflect/Proxy.html)

---

## 이 단원의 큰 그림 — "프레임워크의 마법"의 정체

Spring의 `@GetMapping`, Jackson의 직렬화, JPA의 엔티티 매핑, JUnit의 `@Test`, Mockito의 목(mock) — 이 모든 "마법"의 공통 메커니즘이 **리플렉션**이다. 프레임워크는 당신의 클래스를 **런타임에 들여다보고**, 애너테이션을 읽고, 메서드를 찾아 부르고, 인터페이스만으로 구현체를 만들어낸다.

ch20 미니 웹 프레임워크에서 라우트를 `map.put("/users", handler)`로 **손배선**하게 되는데, 실무 프레임워크는 그렇게 하지 않는다 — `@Route("/users")`를 붙여두면 **스캐너가 알아서 찾아 등록**한다. 이 단원은 그 스캐너를 직접 만든다.

```
ch09 객체 모델     — 클래스 계층·디스패치를 데이터(ClassNode)로 "흉내"
        ↓
ch10 리플렉션      — 진짜 Class/Method/Field API로 스캔→생성→호출  ← 지금 여기
        ↓
ch20 프레임워크    — 이 메커니즘으로 @Route 라우트 테이블을 자동 구성
```

이 단원에서 다루지 않는 것(경계):
- **바이트코드 생성**(ASM/CGLIB/ByteBuddy), **`invokedynamic`/`MethodHandle` 내부**, **java agent/instrumentation**, **클래스로더 심층**, **컴파일 타임 애너테이션 처리(APT)**. 모두 별도 주제거나 외부 라이브러리 영역.

---

## 객체는 자기 타입을 안다 — 그 타입을 손에 쥐기

ch08/ch09에서 봤듯, 모든 객체는 자신의 클래스 메타데이터를 가리키는 포인터(klass pointer)를 들고 있다. 리플렉션은 그 메타데이터를 **읽는 API**다. `Class<?>` 객체를 얻는 세 가지 길:

```java
Class<?> a = "hi".getClass();              // 인스턴스로부터 (런타임 타입)
Class<?> b = String.class;                 // 클래스 리터럴 (컴파일 타임)
Class<?> c = Class.forName("java.lang.String");  // 이름(FQCN)으로 — 런타임에 로드
```

`Class` 객체에서 `getDeclaredMethods()`, `getDeclaredFields()`, `getConstructors()`, `getAnnotations()`로 클래스의 모든 구조를 꺼낼 수 있다. `Reflect` 연습이 이 기초 위에 안전한 헬퍼를 만든다.

---

## `Method.invoke`의 예외 모델 — 이 단원의 대표 함정

`Method.invoke`로 메서드를 부르면, **대상 메서드가 던진 예외는 그대로 나오지 않고 `InvocationTargetException`으로 감싸여** 나온다.

```java
Method m = calc.getClass().getDeclaredMethod("divide", int.class, int.class);
m.invoke(calc, 1, 0);
// divide가 ArithmeticException을 던지지만, invoke는 그것을
// InvocationTargetException으로 감싸 던진다. 원래 예외는 getCause()에 있다.
```

| 상황 | 던지는 예외 |
|---|---|
| 대상 메서드 본문이 던진 예외(checked/unchecked 모두) | `InvocationTargetException` (원본은 `getCause()`) |
| `private`인데 `setAccessible(true)` 안 함 | `IllegalAccessException` |
| 인자 개수/타입 불일치 | `IllegalArgumentException` |

> **핵심**: `invoke`를 감쌀 때 `InvocationTargetException`을 그대로 다시 감싸면 호출자가 진짜 원인을 못 본다. **`getCause()`로 원래 예외를 꺼내 다시 던져야** 한다. (`Reflect.invoke`와 `ProxyFactory`가 똑같이 이 함정을 다룬다 — ch04 wrap-and-rethrow의 리플렉션판.)

---

## 애너테이션 — 코드에 붙이는 메타데이터

애너테이션은 **그 자체로 아무 동작도 하지 않는다.** 그저 코드에 붙은 메타데이터(데이터)일 뿐이고, "동작"은 그것을 **리플렉션으로 읽어 해석하는 코드**(스캐너/프록시)가 부여한다. `@Route`가 라우팅하는 게 아니라, 스캐너가 `@Route`를 보고 라우팅한다.

### `@Retention` — 가장 흔한 함정

```java
@Retention(RetentionPolicy.RUNTIME)   // 이게 없으면 런타임에 안 보인다!
@Target(ElementType.METHOD)
public @interface Route { String value(); }
```

| 보존 정책 | 의미 | 리플렉션으로 읽힘? |
|---|---|---|
| `SOURCE` | 컴파일 시 버려짐 (예: `@Override`) | ✗ |
| `CLASS` (**기본값**) | `.class`엔 있지만 JVM이 런타임에 로드 안 함 | ✗ |
| `RUNTIME` | 런타임까지 살아남음 | ✅ |

> `@Retention`을 **안 붙이면 기본이 `CLASS`**라 리플렉션으로 안 읽힌다. 스캐너용 애너테이션엔 반드시 `@Retention(RUNTIME)`. (이걸 빠뜨려 "왜 내 애너테이션이 스캔에 안 잡히지?"가 입문자 1위 버그다.)

- `@Target(ElementType.METHOD)`: 붙일 수 있는 위치를 제한(메서드에만).
- `@Inherited`: **클래스 상속 체인에만** 적용된다. 인터페이스 구현·메서드 오버라이드에는 **무효**다(흔한 오해).

---

## 캡슐화 우회 — `setAccessible`과 모듈 시스템

`field.setAccessible(true)`는 접근 검사를 끄고 `private` 멤버를 읽고 쓰게 한다. ch09이 "`private`은 불변식의 수호"라고 했는데, 리플렉션은 그 수호를 **합법적으로 우회**한다(직렬화·ORM이 생성자 없이 필드를 채우는 법).

단, **Java 9+ 모듈 시스템**에서는 무제한이 아니다: 모듈이 `opens` 하지 않은 패키지의 비공개 멤버에 `setAccessible`을 호출하면 `InaccessibleObjectException`이 난다(strong encapsulation). JDK 내부(`java.lang`의 일부 등)가 이 보호를 받는다.

> 흔한 오해 둘 다 틀렸다: "Java 9부터 `setAccessible`은 무조건 막힌다" ✗(모듈/`opens` 설정에 달림), "내 클래스 private도 못 읽는다" ✗. **이 저장소는 classpath 빌드(모듈 미사용)라 자기 클래스의 `private`은 문제없이 접근**된다 — 그래서 이 단원의 테스트는 모두 결정적이다. (남의/JDK 내부 클래스에 `setAccessible`을 시도할 때만 환경 의존이 생긴다.)

---

## 제네릭 타입 소거와 리플렉션 (참고)

`new ArrayList<String>().getClass()`는 그냥 `ArrayList`다 — **인스턴스에선 타입 인자가 지워진다**(소거). 하지만 **선언부**(필드·메서드 시그니처·상위 타입)의 제네릭 정보는 메타데이터로 남아 `Field.getGenericType()` / `Method.getGenericReturnType()`로 읽을 수 있다. "제네릭은 런타임에 완전히 사라진다"는 절반만 맞다.

---

## 동적 프록시 — 인터페이스만으로 구현체 만들기

`java.lang.reflect.Proxy`는 런타임에 인터페이스를 구현하는 클래스를 **만들어내고**, 그 모든 메서드 호출을 하나의 `InvocationHandler`로 보낸다. AOP, `@Transactional`, Mockito, MyBatis 매퍼가 작동하는 원리다.

```java
Greeter proxy = (Greeter) Proxy.newProxyInstance(
    iface.getClassLoader(),
    new Class<?>[]{ Greeter.class },
    (p, method, args) -> { /* 모든 호출이 여기로 */ ... });
```

> **제약**: JDK 동적 프록시는 **인터페이스만** 가능하다. 구체 클래스는 불가 — 그래서 CGLIB/ByteBuddy(서브클래싱)가 존재하고, Spring AOP가 인터페이스 없는 빈엔 CGLIB로 폴백한다. `InvocationHandler`는 함수형 인터페이스라 **람다로 쓸 수 있다**(ch04).

---

## 연습 문제

> 권장 순서: **Reflect(기초) → RouteScanner(간판) → ObjectMapper → ProxyFactory.** Reflect가 나머지의 헬퍼라 먼저 푼다(ch09에서 `ClassNode`가 `MethodTable`을 떠받친 것과 같은 의존 방향).

### Reflect (5문제) — 리플렉션 기초와 예외 모델

날것의 리플렉션 API를 검사 예외 없는 안전한 헬퍼로 감싼다. 나머지 연습이 이걸 쓴다.

| 메서드 | 핵심 |
|---|---|
| `load(fqcn)` | `Class.forName` + 검사 예외 래핑 |
| `newInstance(type)` | 기본 생성자로 인스턴스 생성 |
| `invoke(target, name, paramTypes, args)` | 메서드 호출, **`InvocationTargetException.getCause()`를 풀어 다시 던짐** |
| `getField` / `setField` | `setAccessible`로 `private` 필드 읽기/쓰기 |

### RouteScanner (3문제) — 애너테이션 스캔 · 간판

`@Route` 붙은 메서드를 스캔해 "경로 → Method" 테이블을 만들고 디스패치한다. **ch20 프레임워크 라우팅의 정체.**

| 메서드 | 핵심 |
|---|---|
| `scan(target)` | `@Route` 메서드 수집 → 경로→Method 맵 (없으면 제외, 중복은 예외) |
| `dispatch(target, path, args)` | 경로로 메서드 호출, 없으면 `Optional.empty()` |
| `routes(target)` | 등록된 경로 집합 |

### ObjectMapper (2문제) — 필드 리플렉션으로 객체↔맵

`@Column`/`@Ignore`를 읽어 객체를 `Map`으로, 다시 객체로. Jackson/JPA의 골격.

| 메서드 | 핵심 |
|---|---|
| `toMap(obj)` | `@Ignore`·`static` 제외, `@Column`으로 키 매핑 |
| `fromMap(type, source)` | 새 인스턴스에 맵 값을 필드로 주입(역연산) |

### ProxyFactory (1문제) — 동적 프록시

인터페이스를 감싸 모든 호출을 카운팅·위임하는 프록시. AOP/Mockito의 원리.

| 메서드 | 핵심 |
|---|---|
| `counting(iface, target, counter)` | `Proxy.newProxyInstance` + `InvocationHandler`로 가로채기·위임 (인터페이스만, 예외 투명성) |

> 인프라(완성 제공): `ReflectionException`, `@Route`/`@Column`/`@Ignore`, `CallCounter`. 테스트 픽스처는 `src/test`의 `Fixtures.java`.

---

## 실행

```sh
# 전체 테스트 (스텁이 비어 있어 빨간불 — 구현하며 초록불로)
./gradlew :chapter10-reflection-annotations:test

# 특정 클래스
./gradlew :chapter10-reflection-annotations:test --tests "study.chapter10.RouteScannerTest"
```
