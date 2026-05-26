package study.chapter02;

/**
 * 고정 capacity LRU(Least Recently Used) 캐시.
 *
 * <p>
 * 내부는 <b>HashMap + Doubly Linked List</b> 조합:
 * <ul>
 * <li>HashMap: key → Node 즉시 조회 (O(1))</li>
 * <li>DLL: 사용 순서(최근 ↔ 오래됨) 유지. head = 가장 최근, tail = 가장 오래된 것.</li>
 * </ul>
 *
 * <p>
 * 둘을 결합해 get/put 모두 평균 O(1). 캐시가 가득 찼을 때 새 put이 들어오면 tail을 제거한다.
 *
 * <p>
 * 학습 목적상 capacity가 양수여야 한다. null key는 허용하지 않는다 (필요하면 확장).
 */
public class LRUCache<K, V> {

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final MyHashMap<K, Node<K, V>> index;
    private Node<K, V> head;
    private Node<K, V> tail;
    private int size;

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity <= 0: " + capacity);
        }
        this.capacity = capacity;
        this.index = new MyHashMap<>();
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    public boolean containsKey(K key) {
        return index.containsKey(key);
    }

    /**
     * key에 해당하는 value를 반환한다. 있다면 그 노드를 DLL의 head로 이동시켜 "최근 사용"으로 갱신한다. 없으면 null.
     */
    public V get(K key) {

        Node<K, V> node = index.get(key);

        if (node == null) {
            return null;
        }

        moveToHead(node);

        return node.value;

    }

    /**
     * key/value를 캐시에 넣는다.
     * <ul>
     * <li>이미 있는 key면: value 갱신 + DLL head로 이동.</li>
     * <li>새 key면: 새 노드를 head에 추가, size++.</li>
     * <li>새 key 추가 후 size > capacity면: tail(=가장 오래된 노드) 제거.</li>
     * </ul>
     */
    public void put(K key, V value) {

        Node<K, V> node = index.get(key);

        if (node != null) {
            node.value = value;
            moveToHead(node);
        } else {
            if (size >= capacity) {
                Node<K, V> removedTail = removeTail();
                index.remove(removedTail.key);
                size--;
            }
            Node<K, V> newNode = new Node<>(key, value);

            addToHead(newNode);
            index.put(key, newNode);
            size++;
        }

    }

    /** 노드를 현재 위치에서 떼어내 head 앞에 다시 붙인다. */
    private void moveToHead(Node<K, V> node) {

        if (head == node) {
            return;
        }

        unlink(node);
        addToHead(node);

    }

    /** 새 노드를 head 앞에 붙인다. */
    private void addToHead(Node<K, V> node) {

        if (head == null) {
            head = node;
            tail = node;
        } else {
            head.prev = node;
            node.next = head;

            head = node;
        }

    }

    /** 노드를 DLL에서 떼어낸다 (인덱스에서는 별도로 지워야 함). */
    private void unlink(Node<K, V> node) {

        Node<K, V> prev = node.prev;
        Node<K, V> next = node.next;

        if (prev == null && next == null) {
            head = null;
            tail = null;
        } else if (prev == null) {
            head = next;
            next.prev = null;
            node.next = null;
        } else if (next == null) {
            tail = prev;
            prev.next = null;
            node.prev = null;
        } else {
            prev.next = next;
            next.prev = prev;

            node.prev = null;
            node.next = null;
        }

    }

    /** tail 노드를 제거하고 그 노드를 반환한다 (호출자가 인덱스에서 지울 수 있도록). */
    private Node<K, V> removeTail() {

        if (tail == null) {
            throw new IllegalStateException();
        }

        Node<K, V> node = tail;
        unlink(node);

        return node;

    }
}
