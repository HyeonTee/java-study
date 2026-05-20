package study.chapter02;

/**
 * Separate chaining 방식의 해시 맵.
 *
 * <p>
 * 내부는 {@code Entry} 배열 (= 버킷). 같은 버킷에 떨어진 키들은 단방향 연결 리스트로 묶인다. 원소 수가
 * {@code capacity * LOAD_FACTOR}를 넘으면 capacity를 2배로 키우고 모든 entry를 재해시한다 (= resize).
 *
 * <p>
 * 시간 복잡도 (평균): put/get/remove O(1). 최악(모두 한 버킷에 충돌): O(n).
 */
public class MyHashMap<K, V> implements MyMap<K, V> {

    static final int DEFAULT_CAPACITY = 16;
    static final float LOAD_FACTOR = 0.75f;

    private static class Entry<K, V> {
        final int hash;
        final K key;
        V value;
        Entry<K, V> next;

        Entry(int hash, K key, V value, Entry<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Entry<K, V>[] table;
    private int size;
    private int threshold;

    public MyHashMap() {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public MyHashMap(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("initialCapacity <= 0: " + initialCapacity);
        }
        this.table = (Entry<K, V>[]) new Entry[initialCapacity];
        this.threshold = (int) (initialCapacity * LOAD_FACTOR);
        this.size = 0;
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("TODO: implement size()");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("TODO: implement isEmpty()");
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException(
                "TODO: implement put(key, value) — 1) hash 계산 2) 버킷 찾기 3) 같은 key 있으면 값 교체 후 이전 값 반환 4) 없으면 새 Entry를 버킷에 추가, size++ 5) size > threshold면 resize");
    }

    @Override
    public V get(K key) {
        throw new UnsupportedOperationException(
                "TODO: implement get(key) — 1) hash로 버킷 찾기 2) 버킷 LL을 순회하며 key가 같은(equals) entry 찾기");
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException(
                "TODO: implement remove(key) — 단방향 LL 제거: prev.next = curr.next. head 제거 케이스 주의.");
    }

    @Override
    public boolean containsKey(K key) {
        throw new UnsupportedOperationException("TODO: implement containsKey(key)");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("TODO: implement clear() — table 모든 슬롯을 null로, size=0");
    }

    /**
     * Object.hashCode()를 그대로 쓰면 상위 비트가 인덱스 계산에 안 쓰여 충돌이 늘어난다. JDK처럼 상위 16비트를 XOR로
     * 섞어주면 분포가 좋아진다.
     */
    private static int hash(Object key) {
        throw new UnsupportedOperationException(
                "TODO: implement hash(key) — null이면 0. 아니면 h = key.hashCode(); h ^ (h >>> 16) 같은 식으로 spread.");
    }

    /** hash → 버킷 인덱스. capacity가 2의 거듭제곱이면 (capacity - 1)과 AND 하는 게 mod보다 빠르다. */
    private static int indexFor(int hash, int capacity) {
        throw new UnsupportedOperationException("TODO: implement indexFor(hash, capacity) — (capacity - 1) & hash");
    }

    /** capacity를 2배로 키우고 모든 entry를 새 table에 재해시한다. */
    private void resize() {
        throw new UnsupportedOperationException(
                "TODO: implement resize() — 새 capacity = old * 2. 모든 기존 entry를 순회하며 새 table에 다시 넣어라 (hash는 그대로, index는 새 capacity로 다시 계산).");
    }
}
