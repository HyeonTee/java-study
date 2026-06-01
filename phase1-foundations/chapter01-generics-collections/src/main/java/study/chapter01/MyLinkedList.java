package study.chapter01;

import java.util.Iterator;

/**
 * 이중 연결 리스트.
 *
 * head와 tail을 모두 보관해 양 끝 추가/삭제가 O(1)이다.
 * 인덱스 접근은 O(n)이지만, 인덱스가 size/2보다 크면 tail에서 거꾸로 가는 식으로 최적화할 수 있다.
 */
public class MyLinkedList<T> implements MyList<T> {

    private static class Node<T> {
        T value;
        Node<T> prev;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    @Override
    public int size() {
        throw new UnsupportedOperationException("TODO: implement size()");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("TODO: implement isEmpty()");
    }

    @Override
    public void add(T value) {
        throw new UnsupportedOperationException("TODO: implement add(value) — tail에 붙여라 (O(1))");
    }

    @Override
    public void add(int index, T value) {
        throw new UnsupportedOperationException("TODO: implement add(index, value) — index==size면 끝, 0이면 head 앞에 붙이기");
    }

    /** head에 추가 (O(1)) — 따로 만들어두면 add(0, v)에서 재사용 가능. */
    public void addFirst(T value) {
        throw new UnsupportedOperationException("TODO: implement addFirst(value)");
    }

    /** tail에 추가 (O(1)). */
    public void addLast(T value) {
        throw new UnsupportedOperationException("TODO: implement addLast(value)");
    }

    @Override
    public T get(int index) {
        throw new UnsupportedOperationException("TODO: implement get(index) — index가 size/2 이상이면 tail에서 시작하면 빠름");
    }

    @Override
    public T set(int index, T value) {
        throw new UnsupportedOperationException("TODO: implement set(index, value)");
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException("TODO: implement remove(index) — prev.next와 next.prev를 다시 연결하라");
    }

    @Override
    public int indexOf(T value) {
        throw new UnsupportedOperationException("TODO: implement indexOf(value)");
    }

    @Override
    public boolean contains(T value) {
        throw new UnsupportedOperationException("TODO: implement contains(value)");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("TODO: implement clear() — head/tail을 null로, size=0");
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException("TODO: implement iterator() — head부터 next로 순회");
    }
}
