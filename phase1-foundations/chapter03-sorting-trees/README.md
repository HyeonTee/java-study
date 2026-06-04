# Chapter 03 — 정렬·트리 자료구조 (Comparator, 이진탐색트리, 힙)

> **선행 단원**: Chapter 01(제네릭·선형 자료구조), Chapter 02(해시 맵). ch01은 배열·노드 포인터를, ch02는 "키로 값 찾기"를 다뤘다. 이 단원은 그 위에 **순서(order)**를 얹는다 — 정렬, 순서 있는 트리, 우선순위 큐. ch02가 "해시로 O(1)이지만 무순서"였다면, 여기서는 "트리로 O(log n)이지만 정렬·범위 질의 가능"을 직접 구현해 대비한다.

> **공식 문서**: [`Comparable`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Comparable.html) · [`Comparator`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Comparator.html) · [`TreeMap`/`NavigableMap`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/NavigableMap.html) · [`PriorityQueue`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/PriorityQueue.html)

---

## 이 단원의 큰 그림 — "순서를 자료구조에 새기는 4가지 농도"

ch02 README는 "Java 8 HashMap은 버킷이 8개 넘으면 트리로 전환한다"고 적었다. 그런데 정작 트리는 안 만들었다. 이 단원이 그 빈자리를 채운다. 서사는 **"순서란 무엇인가"에서 출발**한다:

```
순서의 정의 (Comparable / Comparator)   — 두 원소 중 누가 먼저인가?
        ↓
순서 있는 저장 (이진탐색트리)            — 비교를 자료구조에 새긴다. 중위순회 = 정렬
        ↓
왜 균형이 필요한가 (편향 → 균형 트리)     — 정렬 입력을 넣으면 O(n)으로 퇴화 (이론)
        ↓
순서가 덜 필요할 때 (힙/우선순위 큐)      — 최솟값만 빠르게. 부분 순서로 충분
        ↓
맵으로서의 트리 (TreeMap vs HashMap)     — 같은 계약, 다른 보장. floor/ceiling
```

다루지 않는 것(경계): **B-트리/B+트리**(디스크·DB 인덱스), **스킵리스트**(동시성 단원에서), **trie**(문자열 특화), **그래프**(BFS/DFS는 별개 단원). 이 단원은 "메모리상의 순서 있는 자료구조"로 한정한다.

---

## 순서의 정의 — `Comparable` vs `Comparator`

트리·힙·정렬 맵은 전부 "두 원소 중 누가 먼저냐"에 의존한다. 그 순서를 정의하는 두 길:

- **자연 순서** — 타입이 `Comparable<T>`를 구현해 `compareTo`로 자기 순서를 선언(예: `Integer`, `String`).
- **주입된 순서** — `Comparator<T>`를 외부에서 건넨다(다중 키 정렬, 역순 등).

### `compareTo` 계약 (ch02 equals/hashCode 계약의 2탄)

ch02가 "`equals`/`hashCode`를 어기면 HashMap이 조용히 깨진다"였다면, 여기서는 "`compareTo`를 어기면 트리가 조용히 깨진다". 계약:

- **전순서 대칭**: `sgn(a.compareTo(b)) == -sgn(b.compareTo(a))`. 부호만 의미 있다 — `compareTo`가 꼭 −1/0/1을 반환할 의무는 없다.
- **추이성**: `a>b && b>c ⇒ a>c`.
- **equals 일관성** (강력 권장, 필수 아님): `a.compareTo(b)==0`이면 `a.equals(b)`이길 권한다.

> **함정 1 — 뺄셈 비교 오버플로**: `return this.x - o.x;`는 `x`가 음수/큰 값일 때 오버플로로 부호가 뒤집힌다. 항상 `Integer.compare(this.x, o.x)`를 써라.

> **함정 2 — `compareTo`/`equals` 불일치**: `TreeMap`/`TreeSet`은 `equals`를 **전혀 보지 않고** `compareTo==0`으로만 키 동일성을 판단한다. 그래서 `new BigDecimal("1.0")`과 `new BigDecimal("1.00")`은 `equals`로는 다르지만(`false`) `compareTo`로는 같아(`0`), **`HashSet`엔 둘 다 들어가도 `TreeSet`엔 하나만** 들어간다. `MyComparators` 연습에서 이 분리를 체감한다.

`MyComparators`에서 `comparing`/`reversed`/`thenComparing`/`nullsFirst`를 **직접 만들어**, 이들이 "기존 비교기를 감싼 새 비교기를 반환하는 고차 함수"임을 확인한다(ch04 함수 합성과 직결).

---

## 이진탐색트리 (BST) — 순서를 자료구조에 새기다

모든 노드가 **왼쪽 < 자신 < 오른쪽** 불변식을 만족하는 트리. ch01의 노드·포인터 조작을 2차원으로 확장한다.

```
        5
       / \
      3   8        ← 중위순회(왼→자신→오른): 1, 3, 4, 5, 8, 9  (정렬됨!)
     / \   \
    1   4   9
```

**핵심 통찰: 중위순회(in-order) = 정렬된 순서.** "트리 안에 정렬이 숨어 있다."

가장 어려운 부분은 **delete의 3가지 경우**:
1. **리프** → 그냥 떼낸다.
2. **자식 1개** → 그 자식을 자기 자리로 올린다.
3. **자식 2개** → 오른쪽 서브트리의 최소값(중위 후속자)으로 값을 대체하고, 그 후속자를 삭제한다.

### 왜 균형이 필요한가 (편향 트리)

BST는 평균 O(log n)이지만, **정렬된 입력을 넣으면 한쪽으로만 자라 연결 리스트로 퇴화**한다(O(n)):

```
1 → 2 → 3 → 4 → 5   (정렬 입력을 넣으면 사실상 ch01의 LinkedList)
```

`height()` 연습이 이를 보여준다: 정렬된 1~7을 넣으면 높이가 6(= size−1). **이것이 자가균형 트리(AVL, Red-Black)가 회전으로 푸는 문제다.** 단 이 단원의 목적은 "순서를 자료구조에 새기는 원리"와 자료구조 선택 안목이지 균형 알고리즘 자체가 아니므로, 이 단원은 **BST까지만 구현하고 균형은 이론으로** 둔다(편향을 *감지*는 하되 *교정*은 하지 않는다 — 회전은 `TreeMap` 같은 라이브러리가 맡는 영역).

> 성능은 테스트로 단언하지 않는다(시간 측정은 비결정적). 편향은 "정확성은 유지되나 높이가 커진다"로만 다룬다.

---

## 힙 / 우선순위 큐 — 부분 순서로 충분할 때

"전부 정렬할 필요 없이 **최솟값만** 빠르게"가 필요할 때(스케줄러, 다익스트라). 힙은 **부분 순서**만 유지한다 — 부모 ≤ 자식이라는 heap property만 보장하고 형제 간 순서는 없다.

완전 이진 트리를 ch01 `MyArrayList`처럼 **배열로 표현**한다(포인터 없이 인덱스 산술):

```
인덱스 i 의 부모 = (i-1)/2,  자식 = 2i+1, 2i+2   (0-기반)

      [3]                배열: [3, 5, 8, 9, 7]
     /   \                      0  1  2  3  4
   [5]   [8]
   / \
 [9] [7]
```

- `offer`: 끝에 넣고 부모보다 작으면 위로(siftUp) — O(log n).
- `poll`: 루트(최솟값) 꺼내고 마지막을 루트로 옮긴 뒤 아래로(siftDown) — O(log n).
- `peek`: 루트 — O(1).

> **핵심**: 힙 배열 자체는 정렬되어 있지 **않다**(부모-자식만 보장). 다만 `poll`을 반복하면 오름차순으로 나온다 — 이것이 힙 정렬이다. "정렬된 시퀀스 ≠ 정렬된 저장구조".

---

## 맵으로서의 트리 — `MyTreeMap` vs ch02 `MyHashMap`

같은 "키→값" 추상이지만 보장이 다르다:

| | `MyHashMap` (ch02) | `MyTreeMap` (이 단원) |
|---|---|---|
| put/get | 평균 O(1) | O(log n) |
| 순서 | **무순서** | **키 정렬 순서** |
| 범위 질의 | 불가 | `floorKey`/`ceilingKey` |
| 기반 | 해시 + 체이닝 | 이진탐색트리 |

`MyTreeMap`은 ch02가 이미 다룬 기본 맵 동작을 반복하지 않는다 — 초점은 **순서 보장**과 **항해 연산**이다:

- `floorKey(k)` = k **이하**(≤) 중 가장 큰 키. 예: 키 {10,20,30}에서 `floorKey(25)==20`, `floorKey(5)==null`.
- `ceilingKey(k)` = k **이상**(≥) 중 가장 작은 키. `ceilingKey(25)==30`.
- `keySet()` = 정렬된 키 순회.

"순서가 필요하면 트리, 아니면 해시"라는 자료구조 선택 기준이 이 대비의 결론이다.

---

## 제네릭 — `<T extends Comparable<? super T>>`

데이터 구조의 타입 바운드가 왜 `Comparable<T>`가 아니라 `Comparable<? super T>`인가?

```java
class Animal implements Comparable<Animal> { ... }
class Dog extends Animal { }   // compareTo를 상속 → Dog는 Comparable<Animal>이지 Comparable<Dog>가 아니다!
```

`<T extends Comparable<T>>`였다면 `MyBinarySearchTree<Dog>`가 **컴파일되지 않는다**(`Dog`이 `Comparable<Dog>`를 만족 못 함). `<T extends Comparable<? super T>>`면 `Dog`이 `Comparable<Animal>`(Animal은 Dog의 상위)로 바운드를 만족한다. **JDK의 `Collections.sort`, `TreeMap`이 전부 `? super T`를 쓰는 이유**다. 테스트의 `Dog` 픽스처가 이 바운드를 검증한다.

---

## 연습 문제

> 권장 순서: **MyComparators(순서 정의) → MyBinarySearchTree(간판) → MyMinHeap → MyTreeMap.**

### MyComparators (5문제) — Comparator 합성

| 메서드 | 핵심 |
|---|---|
| `comparing(keyExtractor)` | 키의 자연 순서로 비교 |
| `reversed(base)` | 방향 뒤집기 (오버플로 함정 — 인자 교환) |
| `thenComparing(first, second)` | 동률이면 2차 기준 |
| `nullsFirst` / `nullsLast` | null 위치 규칙 + non-null은 위임 |

### MyBinarySearchTree (9문제) — 이진탐색트리 · 간판

| 메서드 | 핵심 |
|---|---|
| `insert` / `contains` | compareTo로 좌/우 (중복 false, null NPE) |
| `delete` | **3경우**(리프/자식1/자식2 후속자 교체) |
| `inorder` | 중위순회 = 정렬 리스트 |
| `min` / `max` | 최좌단/최우단 |
| `height` | 편향 관찰(정렬 입력 → size−1) |
| `size` / `isEmpty` | |

### MyMinHeap (5문제) — 배열 기반 우선순위 큐

| 메서드 | 핵심 |
|---|---|
| `offer` | 끝에 추가 후 siftUp (용량 2배 확장) |
| `poll` | 최솟값 제거 + siftDown |
| `peek` | 최솟값 조회(제거 X) |
| `size` / `isEmpty` | |

### MyTreeMap (9문제) — 정렬 맵 (ch02 대비)

| 메서드 | 핵심 |
|---|---|
| `put` / `get` / `containsKey` / `remove` | compareTo로 위치 결정 (put은 이전 값 반환) |
| `firstKey` | 최소 키 |
| `floorKey` / `ceilingKey` | 범위 질의(≤ 중 최대 / ≥ 중 최소) |
| `keySet` | 정렬 순서 순회 |

---

## 실행

```sh
# 전체 테스트 (스텁이 비어 있어 빨간불 — 구현하며 초록불로)
./gradlew :chapter03-sorting-trees:test

# 특정 클래스
./gradlew :chapter03-sorting-trees:test --tests "study.chapter03.MyBinarySearchTreeTest"
```
