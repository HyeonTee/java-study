package study.chapter01;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

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
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void add(T value) {
        addLast(value);
    }

    private Node<T> getNode(int index) {
        Node<T> curr;
        if (index <= (size >> 1)) {
            curr = head;
            for (int i = 0; i < index; i++) {
                curr = curr.next;
            }
        } else {
            curr = tail;
            for (int i = 0; i < size - 1 - index; i++) {
                curr = curr.prev;
            }
        }

        return curr;
    }

    @Override
    public void add(int index, T value) {
        if (index < 0 || size < index) {
            throw new IndexOutOfBoundsException();
        }

        if (index == 0) {
            addFirst(value);
        } else if (index == size) {
            addLast(value);
        } else {
            Node<T> newNode = new Node<>(value);

            Node<T> next = getNode(index);
            Node<T> prev = next.prev;

            prev.next = newNode;
            newNode.prev = prev;
            newNode.next = next;
            next.prev = newNode;

            size++;
        }
    }

    /** head에 추가 (O(1)) — 따로 만들어두면 add(0, v)에서 재사용 가능. */
    public void addFirst(T value) {
        Node<T> newNode = new Node<>(value);

        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            head.prev = newNode;
            newNode.next = head;
            head = newNode;
        }

        size++;
    }

    /** tail에 추가 (O(1)). */
    public void addLast(T value) {
        Node<T> newNode = new Node<>(value);

        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }

        size++;
    }

    @Override
    public T get(int index) {
        if (index < 0 || size <= index) {
            throw new IndexOutOfBoundsException();
        }

        Node<T> node = getNode(index);

        return node.value;
    }

    @Override
    public T set(int index, T value) {
        if (index < 0 || size <= index) {
            throw new IndexOutOfBoundsException();
        }

        Node<T> node = getNode(index);
        T oldValue = node.value;

        node.value = value;

        return oldValue;
    }

    @Override
    public T remove(int index) {
        if (index < 0 || size <= index) {
            throw new IndexOutOfBoundsException();
        }

        Node<T> node = getNode(index);
        T value = node.value;

        if (node.prev == null) {
            head = node.next;
        } else {
            node.prev.next = node.next;
        }

        if (node.next == null) {
            tail = node.prev;
        } else {
            node.next.prev = node.prev;
        }

        node.prev = null;
        node.next = null;
        node.value = null;

        size--;

        return value;
    }

    @Override
    public int indexOf(T value) {
        Node<T> curr = head;
        for (int i = 0; i < size; i++) {
            if (Objects.equals(curr.value, value)) {
                return i;
            }
            curr = curr.next;
        }

        return -1;
    }

    @Override
    public boolean contains(T value) {
        return indexOf(value) >= 0;
    }

    @Override
    public void clear() {
        Node<T> curr = head;
        while (curr != null) {
            Node<T> next = curr.next;
            curr.prev = null;
            curr.next = null;
            curr.value = null;
            curr = next;
        }
        head = null;
        tail = null;
        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new MyIterator();
    }

    private class MyIterator implements Iterator<T> {
        Node<T> cursor = head;

        public boolean hasNext() {
            return cursor != null;
        }

        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            T value = cursor.value;
            cursor = cursor.next;

            return value;
        }
    }

}
