# Chapter 01 — 제네릭 & ArrayList / LinkedList 직접 구현

> 목표: 표준 라이브러리를 사용하지 않고 `ArrayList`와 `LinkedList`를 직접 구현하면서,
> 제네릭과 두 자료구조의 시간복잡도를 몸으로 익힙니다.

## 1. 제네릭이 왜 있나

```java
// 제네릭이 없으면 — Object로 저장하고 매번 캐스팅
List rawList = new ArrayList();
rawList.add("hello");
String s = (String) rawList.get(0); // 런타임에 캐스팅 실패할 수도 있음

// 제네릭이 있으면 — 컴파일 타임에 타입 안전성 보장
List<String> safeList = new ArrayList<>();
safeList.add("hello");
String s2 = safeList.get(0); // 캐스팅 불필요
// safeList.add(42); // 컴파일 에러
```

핵심: **컴파일 타임에 타입을 검사**해 런타임 에러를 미리 잡는다.

## 2. 타입 소거 (Type Erasure)

Java 제네릭은 **컴파일 시점에만** 존재합니다. 바이트코드로 변환되면 `T`는 `Object`로 바뀝니다.

```java
public class Box<T> {
    private T value;          // 바이트코드에서는 Object value
    public T get() { ... }    // 바이트코드에서는 Object get()
}
```

그래서 `new T[10]`은 불가능합니다(런타임에 T가 뭔지 모르므로). 대신:

```java
@SuppressWarnings("unchecked")
T[] arr = (T[]) new Object[10];
```

## 3. 와일드카드 — PECS

`? extends T`는 **읽기 전용**(Producer), `? super T`는 **쓰기 전용**(Consumer).

```java
// 숫자를 더한다 — List 안에서 꺼내 읽기만 함 → extends
double sum(List<? extends Number> list) { ... }

// 정수를 넣는다 — List에 쓰기만 함 → super
void fillIntegers(List<? super Integer> list) { ... }
```

**P**roducer **E**xtends, **C**onsumer **S**uper.

## 4. ArrayList 내부 구조

- 내부에 `Object[] elements` 배열을 가진다.
- `size`는 실제 원소 수, `elements.length`는 용량(capacity).
- 용량이 부족하면 새 배열을 만들어 복사 (보통 1.5배 ~ 2배 증가).
- 인덱스 접근: **O(1)**, 중간 삽입/삭제: **O(n)** (뒤쪽 원소 이동).

## 5. LinkedList 내부 구조

- 각 원소를 `Node`로 감싸고 prev/next 포인터로 연결 (이중 연결 리스트).
- `head`와 `tail`을 따로 들고 양 끝 추가는 **O(1)**.
- 인덱스 접근: **O(n)** (head/tail부터 순회).
- 양 끝 삽입/삭제: **O(1)**, 중간 삽입/삭제: 해당 노드만 찾으면 **O(1)** (찾는 게 O(n)).

## 6. 이 단원에서 만들 것

- `MyList<T>` — 공통 인터페이스
- `MyArrayList<T>` — 배열 기반 동적 리스트
- `MyLinkedList<T>` — 이중 연결 리스트

## 7. 진행 순서 (추천)

1. `MyArrayList`의 `size`, `isEmpty`, `add`, `get`부터 통과시킨다.
2. `set`, `remove(int)`, `indexOf`로 넘어간다.
3. 용량 확장 테스트를 통과시킨다 (`ensureCapacity`).
4. `MyLinkedList`도 같은 순서로. (단, `addFirst`/`addLast` O(1)이 핵심)
5. 마지막으로 `iterator()` 테스트.

## 8. 테스트 돌리는 법

```sh
# 단원 전체
./gradlew :chapter01-generics-collections:test

# 특정 클래스만
./gradlew :chapter01-generics-collections:test --tests "study.chapter01.MyArrayListTest"

# 특정 테스트 메서드만
./gradlew :chapter01-generics-collections:test --tests "study.chapter01.MyArrayListTest.add는_size를_1_증가시킨다"
```

## 9. 생각해볼 거리 (테스트 다 통과한 뒤)

- ArrayList의 용량 증가 전략을 1.5배 → 2배로 바꾸면 무엇이 달라지나?
- LinkedList에서 `get(n/2)`를 더 빠르게 하려면? (힌트: 어느 쪽에서 출발할지)
- `MyArrayList<Integer>`에 1억 개 원소를 넣으면 메모리는 얼마나 쓰나? (`Integer` 박싱)
