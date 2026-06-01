package study.chapter03;

import java.util.List;

/**
 * 키가 항상 정렬된 순서로 유지되는 맵 (BST 기반) — ch02 {@code MyHashMap}과의 정면 대비.
 *
 * <p>ch02 해시 맵은 평균 O(1)이지만 <strong>순서 보장이 없다</strong>. 이 맵은 put/get O(log n)이지만
 * <strong>키를 정렬 순서로 순회</strong>할 수 있고, {@link #floorKey}/{@link #ceilingKey} 같은
 * <strong>범위 질의</strong>를 지원한다(JDK {@link java.util.TreeMap}/{@code NavigableMap}에 대응).
 * "순서가 필요하면 트리, 아니면 해시"라는 선택 기준을 코드로 증명한다.
 *
 * <p>여기서는 ch02가 이미 다룬 기본 맵 동작(해시·체이닝)을 반복하지 않는다. 이 단원의 초점은
 * <strong>(1) compareTo로 위치 결정, (2) 항해 연산(floor/ceiling/first), (3) 정렬 순회</strong>다.
 * 회전은 하지 않으므로 치우친 입력에서 O(n)으로 퇴화할 수 있다. null 키는 허용하지 않고(비교 불가),
 * null 값은 허용한다.
 *
 * @param <K> 정렬 가능한 키 타입
 * @param <V> 값 타입
 */
public class MyTreeMap<K extends Comparable<? super K>, V> {

    private static class Node<K, V> {
        final K key;
        V value;
        Node<K, V> left;
        Node<K, V> right;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node<K, V> root;
    private int size;

    public int size() {
        throw new UnsupportedOperationException("TODO: size 반환");
    }

    /**
     * 키-값을 저장한다. 같은 키가 있으면 값을 교체하고 <strong>이전 값</strong>을 반환, 새 키면 null
     * (ch02 {@code MyMap.put}과 동일한 반환 규약).
     *
     * @throws NullPointerException {@code key}가 null이면
     */
    public V put(K key, V value) {
        throw new UnsupportedOperationException(
                "TODO: compareTo로 위치를 찾아 저장. 같은 키면 값 교체 후 이전 값 반환, 새 키면 null");
    }

    /** 키에 대응하는 값. 없으면 null. (null 값과 미존재 구분은 {@link #containsKey}로.) */
    public V get(K key) {
        throw new UnsupportedOperationException("TODO: compareTo 부호로 탐색해 값 반환");
    }

    public boolean containsKey(K key) {
        throw new UnsupportedOperationException("TODO: 키 존재 여부 반환");
    }

    /**
     * 키를 제거하고 그 값을 반환한다. 없었으면 null. (삭제 로직은 BST의 3경우와 동일.)
     */
    public V remove(K key) {
        throw new UnsupportedOperationException("TODO: BST 삭제 3경우. 제거된 값 반환, 없으면 null");
    }

    /**
     * 가장 작은 키.
     *
     * @throws java.util.NoSuchElementException 비어 있으면
     */
    public K firstKey() {
        throw new UnsupportedOperationException("TODO: 최좌단 키 (비었으면 NoSuchElementException)");
    }

    /**
     * {@code key} <strong>이하</strong>(≤)인 키 중 가장 큰 키. 그런 키가 없으면 null.
     *
     * <p>예: 키 {10,20,30}에서 {@code floorKey(25)==20}, {@code floorKey(10)==10}, {@code floorKey(5)==null}.
     *
     * <p>힌트: 내려가며 "key 이하인 마지막으로 본 키"를 후보로 기억한다. {@code compareTo>0}이면 현재 키를
     * 후보로 두고 오른쪽으로.
     */
    public K floorKey(K key) {
        throw new UnsupportedOperationException("TODO: key 이하 중 최대 키 (없으면 null)");
    }

    /**
     * {@code key} <strong>이상</strong>(≥)인 키 중 가장 작은 키. 없으면 null.
     *
     * <p>예: 키 {10,20,30}에서 {@code ceilingKey(25)==30}, {@code ceilingKey(30)==30}, {@code ceilingKey(35)==null}.
     */
    public K ceilingKey(K key) {
        throw new UnsupportedOperationException("TODO: key 이상 중 최소 키 (없으면 null)");
    }

    /**
     * 모든 키를 <strong>오름차순</strong>으로 담은 리스트(중위순회). 빈 맵이면 빈 리스트.
     * (JDK keySet과 달리 뷰가 아닌 스냅샷 리스트를 반환한다 — 학습 단순화.)
     */
    public List<K> keySet() {
        throw new UnsupportedOperationException("TODO: 중위순회로 키를 오름차순 수집");
    }
}
