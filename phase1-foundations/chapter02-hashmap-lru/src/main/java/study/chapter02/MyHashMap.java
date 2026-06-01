package study.chapter02;

import java.util.Objects;

/**
 * Separate chaining 방식의 해시 맵.
 *
 * <p>
 * 내부는 {@code Entry} 배열 (= 버킷). 같은 버킷에 떨어진 키들은 단방향 연결 리스트로 묶인다. 원소 수가
 * {@code capacity * LOAD_FACTOR}를 넘으면 capacity를 2배로 키우고 모든 entry를 재해시한다 (=
 * resize).
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
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public V put(K key, V value) {

        int hash = hash(key);
        int index = indexFor(hash, table.length);

        Entry<K, V> prev = null;
        Entry<K, V> curr = table[index];

        while (curr != null) {
            if (Objects.equals(curr.key, key)) {
                V oldValue = curr.value;
                curr.value = value;
                return oldValue;
            }

            prev = curr;
            curr = curr.next;
        }

        Entry<K, V> newEntry = new Entry<>(hash, key, value, null);
        if (prev == null) {
            table[index] = newEntry;
        } else {
            prev.next = newEntry;
        }

        size++;
        if (size > threshold) {
            resize();
        }

        return null;
    }

    @Override
    public V get(K key) {
        int hash = hash(key);
        int index = indexFor(hash, table.length);
        Entry<K, V> entry = table[index];

        while (entry != null) {
            if (Objects.equals(entry.key, key)) {
                return entry.value;
            }

            entry = entry.next;
        }

        return null;
    }

    @Override
    public V remove(K key) {
        int hash = hash(key);
        int index = indexFor(hash, table.length);

        Entry<K, V> prev = null;
        Entry<K, V> curr = table[index];
        while (curr != null) {
            if (Objects.equals(curr.key, key)) {
                V oldValue = curr.value;

                if (prev == null) {
                    table[index] = curr.next;
                } else {
                    prev.next = curr.next;
                }

                size--;
                return oldValue;
            }

            prev = curr;
            curr = curr.next;
        }

        return null;
    }

    @Override
    public boolean containsKey(K key) {
        int hash = hash(key);
        int index = indexFor(hash, table.length);
        Entry<K, V> entry = table[index];

        while (entry != null) {
            if (Objects.equals(entry.key, key)) {
                return true;
            }

            entry = entry.next;
        }

        return false;
    }

    @Override
    public void clear() {
        for (int i = 0; i < table.length; i++) {
            table[i] = null;
        }

        size = 0;
    }

    /**
     * Object.hashCode()를 그대로 쓰면 상위 비트가 인덱스 계산에 안 쓰여 충돌이 늘어난다. JDK처럼 상위 16비트를 XOR로
     * 섞어주면 분포가 좋아진다.
     */
    private static int hash(Object key) {
        if (key == null) {
            return 0;
        }

        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    /** hash → 버킷 인덱스. capacity가 2의 거듭제곱이면 (capacity - 1)과 AND 하는 게 mod보다 빠르다. */
    private static int indexFor(int hash, int capacity) {
        return (capacity - 1) & hash;
    }

    /** capacity를 2배로 키우고 모든 entry를 새 table에 재해시한다. */
    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity = 2 * table.length;
        Entry<K, V>[] newTable = (Entry<K, V>[]) new Entry[newCapacity];
        for (int i = 0; i < table.length; i++) {
            Entry<K, V> curr = table[i];
            while (curr != null) {
                Entry<K, V> next = curr.next;
                int newIndex = indexFor(curr.hash, newCapacity);

                curr.next = newTable[newIndex];
                newTable[newIndex] = curr;

                curr = next;
            }

        }
        table = newTable;
        threshold = (int) (newCapacity * LOAD_FACTOR);
    }
}
