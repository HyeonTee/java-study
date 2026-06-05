# Chapter 09 — 객체 모델 (클래스가 실제로 무엇인가)

> **선행 단원**: Chapter 08(JVM 메모리 모델). ch08은 객체가 **어디**에 사는지(힙/스택/메서드 영역), `new`가 무엇을 하는지, 참조와 도달성을 다뤘다. 이 단원은 그 위에서 **그 객체가 *무엇*인가** — 메서드 호출은 어떻게 결정되고, 객체는 자기 타입을 어떻게 알며, 캡슐화와 초기화가 실제로 어떻게 동작하는가 — 를 해부한다.

> **공식 문서**: [JLS 8 — Classes](https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html) · [JLS 12.4/12.5 — 초기화·인스턴스 생성](https://docs.oracle.com/javase/specs/jls/se21/html/jls-12.html) · [JVMS 6.5 — `invokevirtual`](https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-6.html#jvms-6.5.invokevirtual) · *Effective Java* Item 10(equals)·11(hashCode)·17(불변)·19(상속·생성자)·50(방어적 복사)

---

## 이 단원의 큰 그림

당신은 Phase 1 내내 클래스를 **써왔다** — 제네릭 컬렉션을 짜고, record를 만들고, 람다를 넘겼다. 그런데:

- `animal.speak()`를 부르면 JVM은 *정확히* 무엇을 해서 올바른 구현을 고르는가?
- 객체는 자기가 무슨 타입인지 런타임에 어떻게 **아는가**?
- 생성자 안에서 메서드를 부르면 무슨 일이 일어나는가?
- `private`을 붙였는데 왜 캡슐화가 새어나가는가?

이 단원은 이미 쓰던 것을 **메커니즘으로 해부**한다. ch08이 `new`를 "힙 할당 + 기본값 + 생성자"로 분해했듯, 여기서는 **메서드 호출을 `invokevirtual`로, 다형성을 vtable 룩업으로** 분해한다. 이것이 우리가 "Java의 핵심은 클래스라는 그릇이 아니라 **타입·다형성·캡슐화**"라고 말하는 이유를 코드로 증명하는 자리다.

```
ch08 메모리 모델   — 객체가 "어디"에 사는가
        ↓
ch09 객체 모델     — 그 객체가 "무엇"인가  ← 지금 여기
        ↓
ch10 리플렉션      — 그 객체/클래스를 런타임에 "들여다보고 조작"한다
        ↓
ch11 thread-basics — 그 객체를 여럿이 "어떻게 공유"하는가
```

---

## 객체는 자기 타입을 안다 — klass pointer

힙에 있는 객체는 우리가 선언한 필드만 들고 있는 게 아니다. 모든 객체는 **객체 헤더**를 가지며, 그 안에 자신의 **클래스 메타데이터(Method Area, ch08)를 가리키는 포인터**(흔히 *klass pointer*라 부른다)가 있다.

```
힙의 Dog 인스턴스
┌──────────────────────────┐
│ [헤더: mark word]         │
│ [klass pointer] ─────────┼──► Method Area의 Dog 클래스 메타데이터
│ name = "바둑이"           │        (메서드 테이블 = vtable 포함)
└──────────────────────────┘
```

이 포인터 덕분에 객체는 **런타임에 자신의 실제 타입을 안다**. `getClass()`가 동작하는 것도, 다형성이 동작하는 것도 전부 여기서 출발한다. 다음 절의 동적 디스패치가 바로 이 klass pointer를 따라간다.

---

## [척추] 동적 디스패치 — `invokevirtual`

이 단원의 심장이다. `Animal a = new Dog(); a.speak();`에서 `Animal.speak`가 아니라 `Dog.speak`가 불린다. **변수의 선언 타입(`Animal`)이 아니라 객체의 런타임 타입(`Dog`)이 호출 대상을 결정**한다 — 이것이 다형성(polymorphism)이다.

JVM은 이렇게 한다:
1. 수신 객체의 klass pointer를 따라 실제 클래스(`Dog`)의 메서드 테이블(vtable)로 간다.
2. 거기서 `speak` 슬롯이 가리키는 구현을 찾아 점프한다.
3. `Dog`이 오버라이드하지 않았다면, 그 슬롯은 상위(`Animal`)의 구현을 가리킨다.

즉 **"수신 타입에서 출발해 위로 올라가며 그 메서드를 직접 선언한 첫 클래스"**를 찾는 것이다. 이 해석(method resolution) 알고리즘을 `MethodTable`에서 직접 구현한다.

### 모든 호출이 동적은 아니다 — 정적 바인딩의 비대칭

다형적으로 디스패치되는 것과 **선언(정적) 타입으로 고정**되는 것을 구분하는 게 핵심이다:

| 대상 | 바인딩 | 바이트코드 |
|---|---|---|
| 보통의 인스턴스 메서드(오버라이드 가능) | **동적**(런타임 타입) | `invokevirtual` |
| 인터페이스 메서드 | **동적** | `invokeinterface` |
| `static` 메서드 | **정적**(선언 타입) | `invokestatic` |
| `private` 메서드 · 생성자 · `super.m()` | **정적** | `invokespecial` |
| **필드 접근** | **정적**(선언 타입!) | `getfield` |

> 함정: `static` 메서드와 **필드**는 오버라이드되지 않고 **가려진다(hiding)** — 변수의 선언 타입으로 정해진다. `Animal a = new Dog(); a.name`은 `Animal`의 `name`을 본다. 메서드는 자식, 필드는 부모가 나오는 이 비대칭이 흔한 혼란의 원천이다. (**오버로딩**도 런타임이 아니라 컴파일 타임 정적 타입으로 결정된다 — 오버라이딩과 반대.)

---

## 초기화 순서 — 디스패치가 생성자 안에서도 동작한다

디스패치를 이해했으니 그 직접적 귀결을 본다. 객체가 만들어지는 정확한 순서(JLS 12.4/12.5):

**클래스 최초 사용 시 (1회):** 상위 클래스 static 초기화 → 그 클래스 static 초기화(static 필드/블록을 소스 순서로). **클래스당 정확히 한 번.**

**`new`로 인스턴스 생성 시마다:**
1. 필드를 **기본값**(0/null/false)으로 세팅
2. 생성자 진입 → **`super(...)`가 본문보다 먼저** 실행 (최상위까지 거슬러 올라감)
3. `super` 반환 후, **그 클래스의 인스턴스 필드 초기화자 + 초기화 블록**을 소스 순서로 실행
4. 그 다음 **생성자 본문 나머지** 실행

결과: **상위가 완전히 끝난 뒤 하위가 초기화된다.**

### 함정: 생성자에서 오버라이드 가능한 메서드를 부르지 마라 (EJ Item 19)

상위 생성자가 오버라이드된 메서드를 부르면, **동적 디스패치 때문에 하위 버전이 실행된다**. 그런데 그 시점은 위 3·4단계 전 — **하위 필드가 아직 기본값**이다.

```java
class Base { Base() { overridable(); } void overridable() {} }
class Derived extends Base {
    int x = 42;
    @Override void overridable() { System.out.println(x); }  // 0 출력! (42 아님)
}
new Derived();   // Base() → overridable() → Derived 버전 실행, 그러나 x는 아직 0
```

`InitializationOrderDemo`를 실행해(`run()`) 이 순서와 함정을 **이벤트 로그로 직접 관찰**한다. (비결정성이 없으므로 채점이 아니라 관찰·확인용 데모다.)

---

## 정체성 vs 동등성 — 그리고 상속이 깨뜨리는 대칭성

`==`는 **정체성**(같은 객체 = 같은 참조)을, `equals`는 **논리적 동등성**(같은 값)을 비교한다. `new Point2D(1,2) == new Point2D(1,2)`는 `false`지만, 값으로는 같아야 한다.

### `equals` 5계약 (Effective Java Item 10)

반사성 · **대칭성** · 추이성 · 일관성 · `x.equals(null)==false`. 그리고 Item 11: **`equals`가 같다고 본 두 객체는 `hashCode`도 같아야 한다**(아니면 `HashMap`/`HashSet`이 오동작). 

> ch02와의 선: ch02는 *주어진* 키를 맵에 넣어 **계약을 지키면 잘 찾힌다**를 다뤘다(단일 클래스). 여기서는 학습자가 **계약 자체를 작성**하고, **상속이 끼면 계약을 지켜도 깨진다**는 한 층 위를 다룬다.

### 상속 함정: `getClass` vs `instanceof`

`Point2D`를 상속한 `ColorPoint`에 색을 더하면서 `equals`를 짜면 대칭성이 위태롭다:

```java
Point2D   p  = new Point2D(1, 2);
ColorPoint cp = new ColorPoint(1, 2, "RED");
// instanceof 기반이라면:  p.equals(cp)=true 인데  cp.equals(p)=false  → 비대칭! 계약 위반
```

- **`instanceof` 기반**: 상위↔하위를 비교 가능하게 열어두면 **대칭성·추이성이 깨지기 쉽다**.
- **`getClass` 기반**: `getClass()`가 같을 때만 동등 → 대칭은 지키지만 **리스코프 치환 원칙(LSP)과 충돌**(하위를 상위로서 동등 취급해야 하는 경우 깨짐).

값을 더하는 상속에서 `equals` 5계약을 *모두* 지키는 깔끔한 방법은 없다 — Effective Java의 권고는 **상속 대신 컴포지션**이다. 이 단원의 테스트는 **대칭성 자체**(`p.equals(cp) == cp.equals(p)`)를 단언하므로, `getClass` 전략으로 가야 통과한다.

### `==`의 또 다른 함정 (참고 — 채점 안 함)

- **Integer 캐시**: `Integer.valueOf(int)`는 **−128 ~ 127**을 캐시해 같은 참조를 준다(`==` true). 이 범위는 명세 보장이지만 상한은 `-XX:AutoBoxCacheMax`로 늘릴 수 있어, "128부터 무조건 다른 참조"라고 단정하면 안 된다. `new Integer(...)`는 항상 새 객체.
- **문자열 인터닝**: 리터럴과 **컴파일 타임 상수식**(`"a"+"b"`)은 풀에 인터닝돼 같은 참조. **런타임 연결**이나 `new String(...)`은 새 객체.

---

## 캡슐화 = 불변식의 수호 (방어적 복사)

`private`은 단순한 "숨김"이 아니라 **클래스가 자기 불변식(invariant)을 외부 변경으로부터 지키는 계약**이다. 그런데 가변 객체(컬렉션·배열·`Date`)를 경계에서 **복사하지 않으면** 캡슐화가 새어나간다(에일리어싱 누수 — ch08의 "참조는 공유된다"의 어두운 면):

```java
// 누수 버전
TemperatureLog(List<Double> r) { this.readings = r; }   // 호출자가 r을 나중에 바꾸면 내부가 변함
List<Double> readings() { return readings; }            // 외부가 내부 리스트를 직접 변경

// 방어 버전
TemperatureLog(List<Double> r) { this.readings = new ArrayList<>(r); }  // 입력 복사
List<Double> readings() { return List.copyOf(readings); }              // 출력 복사(수정 불가)
```

> ch06와의 선: `record`는 `equals`/불변을 **공짜로** 주지만, 그 불변은 **얕다(shallow)** — record가 가변 `List`/배열을 품으면 여전히 새어나간다. 방어적 복사는 record라도 직접 해야 한다.

`TemperatureLog`는 **불변 객체**다 — 값 추가는 상태를 바꾸는 대신 `withReading`이 **새 인스턴스**를 만든다(Item 17).

---

## 중첩/내부 클래스와 캡처된 상태 — ch11로 가는 다리

- **비-static 내부 클래스**는 **숨은 바깥 인스턴스 참조(`Outer.this`)**를 들고 있다. 그래서 바깥 필드에 접근할 수 있다 — 그리고 바깥 객체를 도달 가능 상태로 **묶어 누수**시킬 수도 있다(ch08 도달성: 리스너 누수의 정체).
- **static 중첩 클래스**는 그 참조가 없다(명시적으로 넘긴 것만 본다). ch01 `MyLinkedList`의 `private static class Node`가 그 예다.
- **람다·익명 클래스**는 **effectively final 지역 변수를 캡처**한다. 메서드가 끝나도 캡처된 상태는 객체에 갇혀 살아남는다 — 이것이 클로저다.

`CounterFactory`에서 이를 직접 만든다: 각 카운터는 자기만의 독립된 카운트 상태를 캡처하고(메서드 수명을 넘겨 살아남고), 바깥 팩토리의 `factoryId`를 본다(바깥 인스턴스 캡처). 

> ch11과의 선: "캡처한 가변 상태를 **여러 스레드**가 만지면?"은 ch11(안전 게시·동기화)로 미룬다. 여기서는 단일 스레드 의미론까지.

---

## 이 단원에서 다루지 않는 것 (경계)

이 선이 흐려지면 "OOP 입문서 재탕"이 된다. 의도적으로 다음을 **제외/위임**한다:

- **`hashCode`의 버킷/해시 분포** → ch02 소관. 여기서는 "equals와 일관" 계약만.
- **GC 알고리즘·세대·`WeakReference`** → ch08. 내부 클래스 누수는 "강한 참조 한 줄이 바깥을 살린다"까지만.
- **`record`/`sealed` 문법** → ch06. 여기서는 record 불변의 *한계*만 반증.
- **동시성 안전 게시(safe publication)** → ch11.
- **리플렉션 / `MethodHandle` / `invokedynamic`**, **클래스로더 상세**, **`clone()`/`Cloneable`**, **`finalize()`** → 제외(디스패치는 vtable 개념 모델까지, clone 대신 방어적 복사·복사 생성자).

---

## 연습 문제

> 권장 순서: **MethodTable(척추) → InitializationOrderDemo 관찰 → Point2D/ColorPoint → TemperatureLog → CounterFactory.** 디스패치를 먼저 익히면 초기화 함정과 equals 대칭성이 그 언어로 설명된다.

### MethodTable (3문제) — 동적 디스패치 직접 구현 · 간판

`ClassNode`(완성 제공) 그래프 위에서 JVM의 메서드 해석을 손으로 구현한다. "받는 타입이 같아도 실제 타입에 따라 다른 구현으로 간다"를 코드로 증명한다.

| 메서드 | 핵심 |
|---|---|
| `resolve(receiverType, m)` | 수신 타입에서 위로 올라가며 직접 선언한 첫 body (오버라이드/상속) |
| `resolveSuper(callerClass, m)` | `super.m()` — 호출자의 상위부터 해석(런타임 타입 무시) |
| `buildVTable(receiverType)` | 상위→하위로 채워 오버라이드가 상위를 가리는 유효 vtable |

### Point2D / ColorPoint (4문제) — 정체성 vs 동등성, 상속 대칭성

`equals`/`hashCode` 계약을 직접 작성하고, 상속 계층에서 대칭성이 깨지는 함정을 직면한다. 테스트가 **대칭성 자체를 단언**하므로 `getClass` 전략으로 가야 한다.

| 메서드 | 핵심 |
|---|---|
| `Point2D.equals` / `hashCode` | 5계약 준수, 정체성(`==`)과 구별 |
| `ColorPoint.equals` / `hashCode` | 색까지 비교하되 `Point2D`와 **대칭성** 유지 |

### TemperatureLog (5문제) — 캡슐화 · 방어적 복사 · 불변 객체

가변 컬렉션을 품은 불변 객체. 경계(생성자 입력 / getter 출력)에서 방어적 복사로 불변식을 지킨다.

| 메서드 | 핵심 |
|---|---|
| `TemperatureLog(List)` | 입력 방어적 복사 |
| `readings()` | 출력 방어(복사본/수정 불가) |
| `withReading(d)` | `this` 불변, 새 인스턴스 반환 |
| `count()` / `max()` | 개수 / 최고값(비었으면 `NoSuchElementException`) |

### CounterFactory (1문제) — 내부 클래스가 캡처한 상태

`makeCounter()`가 독립된 카운트 상태를 캡처하고 바깥 `factoryId`를 노출하는 `Counter`를 반환. 클로저와 바깥 인스턴스 캡처를 직접 만든다.

### InitializationOrderDemo (데모 — 채점 안 함)

초기화 순서와 "생성자가 오버라이드 메서드를 호출하는 함정"을 `run()`으로 관찰한다(완성 코드). 구현이 곧 정답 노출이라 ch08 `VisibilityDemo`처럼 관찰용으로 둔다.

---

## 실행

```sh
# 전체 테스트 (스텁이 비어 있어 빨간불 — 구현하며 초록불로)
./gradlew :chapter09-object-model:test

# 특정 클래스
./gradlew :chapter09-object-model:test --tests "study.chapter09.MethodTableTest"
```
