# Chapter 02 — HashMap 직접 구현 + LRU 캐시

> **공식 문서**: [HashMap (Java 21)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html) · [LinkedHashMap (Java 21)](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/LinkedHashMap.html)

## HashMap

### 해시 테이블이란?

**key → value** 매핑을 평균 O(1)에 처리하는 자료구조. 핵심 아이디어는 key의 해시값으로 배열 인덱스를 결정하여 바로 접근하는 것이다.

```
put("apple", 1)
  → hash("apple") = 3456789
  → index = 3456789 % 16 = 5
  → table[5]에 저장
```

### 해시 함수와 인덱스 계산

```java
int hash = key.hashCode();
int index = hash % table.length;
```

좋은 해시 함수는 키를 버킷에 **균등하게 분산**시킨다. 편향되면 한 버킷에 몰려 O(n)으로 퇴화한다.

### 해시 충돌 (Collision)

서로 다른 key가 같은 인덱스에 매핑되는 것. 배열 크기가 유한하므로 반드시 발생한다.

**해결 방법 — Separate Chaining (이 구현에서 사용)**

같은 버킷에 들어온 Entry들을 **연결 리스트**로 묶는다.

```
table[5] → [apple:1] → [grape:3] → null
```

조회 시: index로 버킷을 찾고, 리스트를 순회하며 `key.equals()`로 일치하는 Entry를 찾는다.

| 다른 방법 | 설명 |
|---|---|
| Open Addressing | 충돌 시 다음 빈 슬롯을 탐색 (linear probing 등) |
| Red-Black Tree | Java 8+ HashMap은 한 버킷에 8개 이상 쌓이면 리스트 → 트리로 전환 |

### 내부 구조

```
MyHashMap
├── Entry<K,V>[] table    (버킷 배열, 기본 크기 16)
├── int size              (저장된 entry 수)
└── int threshold         (= capacity * loadFactor, 이 넘으면 resize)

Entry<K,V>
├── int hash     (캐싱된 해시값)
├── K key
├── V value
└── Entry next   (같은 버킷의 다음 Entry)
```

### 시간 복잡도

| 연산 | 평균 | 최악 (모든 키가 한 버킷) |
|---|---|---|
| `put` | O(1) | O(n) |
| `get` | O(1) | O(n) |
| `remove` | O(1) | O(n) |
| `containsKey` | O(1) | O(n) |

### Resize (재해싱)

`size > capacity * loadFactor`가 되면:
1. 새 배열을 **2배 크기**로 생성
2. 모든 Entry를 새 배열에 **다시 해시**하여 분배
3. 이전 배열 버림

resize는 O(n)이지만 발생 빈도가 낮아 amortized O(1)을 유지한다. `loadFactor`(기본 0.75)는 공간 vs 성능의 트레이드오프.

### 핵심 연산 흐름

**put(key, value)**
```
1. hash = key.hashCode()  (null key면 hash=0)
2. index = hash % table.length
3. table[index]의 연결 리스트를 순회
   - key가 같은 Entry 발견 → value 교체, 이전 값 반환
   - 못 찾음 → 새 Entry를 리스트 앞에 삽입, size++
4. size > threshold면 resize
```

**get(key)**
```
1. hash → index 계산
2. table[index] 리스트 순회, key.equals()로 매칭
3. 찾으면 value, 못 찾으면 null
```

**remove(key)**
```
1. hash → index 계산
2. table[index] 리스트 순회, 일치하는 Entry 발견 시
   - 이전 노드의 next를 다음 노드로 연결 (unlink)
   - size--
3. 제거된 value 반환, 없었으면 null
```

---

## LRU Cache

### LRU란?

**Least Recently Used**. 고정 크기 캐시에서 공간이 부족할 때, **가장 오랫동안 사용되지 않은 항목을 제거**하는 교체 정책.

"최근에 쓴 건 곧 다시 쓸 가능성이 높다" (temporal locality)라는 가정에 기반한다.

### 자료구조: HashMap + Doubly Linked List

get/put 모두 O(1)을 달성하려면 두 자료구조의 조합이 필요하다.

```
HashMap (O(1) 조회)          Doubly Linked List (O(1) 삽입/삭제, 순서 유지)
┌──────────────┐
│ "a" → Node─┐ │            head                              tail
│ "b" → Node─┤ │            ↓                                  ↓
│ "c" → Node─┤ │           [c] ⇄ [a] ⇄ [b]
└──────────────┘          최근 사용 ←──────────→ 오래된 것
```

| 역할 | 자료구조 | 단독 사용 시 문제 |
|---|---|---|
| key로 노드 O(1) 조회 | HashMap | 사용 순서를 모름 |
| 사용 순서 유지 + O(1) 이동/삭제 | Doubly Linked List | key 검색이 O(n) |

### 연산 흐름

**get(key)**
```
1. HashMap에서 Node 조회
2. 없으면 → null
3. 있으면 → Node를 DLL head로 이동 (최근 사용 갱신)
4. value 반환
```

**put(key, value)**
```
1. HashMap에서 key 조회
2. 이미 있으면 → value 갱신 + head로 이동
3. 없으면:
   a. 새 Node 생성, head에 추가, HashMap에 등록, size++
   b. size > capacity면 → tail(가장 오래된) 제거, HashMap에서도 삭제, size--
```

### 구현 시 주의 포인트

- **head/tail이 null** — 빈 캐시에 첫 삽입 시 head = tail = 새 노드
- **노드가 하나뿐일 때 unlink** — head == tail인 경우 둘 다 null로
- **capacity 1** — 매 put마다 기존 항목이 밀려남
- **removeTail의 key 반환** — HashMap에서도 제거해야 하므로 제거된 노드의 key가 필요
- **이미 head인 노드에 moveToHead** — early return 필요 (아니면 NPE)

---

## 연습 문제

### MyHashMap (7문제)

| # | 메서드 | 핵심 |
|---|---|---|
| 1 | `size` / `isEmpty` | 기본 상태 |
| 2 | `put` | 해시 → 인덱스 → 체이닝 삽입 |
| 3 | `get` | 해시 → 인덱스 → 리스트 탐색 |
| 4 | `remove` | 리스트에서 unlink |
| 5 | `containsKey` | get과 유사 |
| 6 | `clear` | 배열 초기화 |
| 7 | `resize` | 2배 확장 + 재해시 |

### LRUCache (7문제)

| # | 메서드 | 핵심 |
|---|---|---|
| 1 | `size` / `containsKey` | HashMap 위임 |
| 2 | `addToHead` | DLL head 삽입 |
| 3 | `unlink` | DLL에서 노드 분리 |
| 4 | `moveToHead` | unlink + addToHead |
| 5 | `removeTail` | unlink(tail) + 반환 |
| 6 | `get` | 조회 + 순서 갱신 |
| 7 | `put` | 삽입/갱신 + eviction |
