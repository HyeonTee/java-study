package study.chapter02;

/**
 * 직접 구현할 맵의 공통 인터페이스.
 *
 * <p>
 * key 비교는 {@code equals}로 한다 (참조 비교 X). null key/value 허용 여부는 구현체가 결정한다 — 본 학습에서는
 * JDK {@link java.util.HashMap}과 동일하게 null key 1개, null value 다수를 허용한다.
 */
public interface MyMap<K, V> {

    int size();

    boolean isEmpty();

    /**
     * key에 대응하는 value를 저장한다. 이미 같은 key가 있으면 value를 교체하고 **이전 값**을 반환한다. 새 key였다면
     * null을 반환한다.
     */
    V put(K key, V value);

    /** key에 대응하는 value를 반환한다. 없으면 null. */
    V get(K key);

    /** key를 제거하고 그 value를 반환한다. 없었다면 null. */
    V remove(K key);

    boolean containsKey(K key);

    void clear();
}
