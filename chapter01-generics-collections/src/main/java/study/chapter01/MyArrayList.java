package study.chapter01;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * 배열 기반 동적 리스트.
 *
 * <p>
 * 내부 배열의 초기 용량은 {@link #DEFAULT_CAPACITY}이고, 가득 차면 1.5배로 확장한다. 인덱스 접근은 O(1), 중간
 * 삽입/삭제는 O(n).
 */
public class MyArrayList<T> implements MyList<T> {

    static final int DEFAULT_CAPACITY = 10;

    private Object[] elements;
    private int size;

    public MyArrayList() {
        this.elements = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public MyArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity < 0: " + initialCapacity);
        }
        this.elements = new Object[initialCapacity];
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
    public void add(T value) {
        ensureCapacity(size + 1);

        elements[size++] = value;
    }

    @Override
    public void add(int index, T value) {
        if (index < 0 || size < index) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(size + 1);

        for (int i = size; index < i; i--) {
            elements[i] = elements[i - 1];
        }

        elements[index] = value;
        size++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || size <= index) {
            throw new IndexOutOfBoundsException();
        }
        return (T) elements[index];
    }

    @Override
    @SuppressWarnings("unchecked")
    public T set(int index, T value) {
        if (index < 0 || size <= index) {
            throw new IndexOutOfBoundsException();
        }

        T oldValue = (T) elements[index];
        elements[index] = value;

        return oldValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T remove(int index) {
        if (index < 0 || size <= index) {
            throw new IndexOutOfBoundsException();
        }

        T oldValue = (T) elements[index];
        for (int i = index; i < size; i++) {
            elements[i] = elements[i + 1];
        }

        elements[size--] = null;

        return oldValue;
    }

    @Override
    public int indexOf(T value) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(value, elements[i])) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public boolean contains(T value) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(value, elements[i])) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }

        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new MyIterator();
    }

    private class MyIterator implements Iterator<T> {
        int cursor = 0;

        public boolean hasNext() {
            return cursor != size;
        }

        @SuppressWarnings("unchecked")
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            T element = (T) elements[cursor++];

            return element;
        }
    }

    /**
     * 내부 배열이 minCapacity를 담을 수 있도록 확장한다. 현재 용량의 1.5배와 minCapacity 중 큰 값을 새 용량으로 한다.
     */
    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= elements.length) {
            return;
        }

        int oldCapacity = elements.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }

        Object[] newElements = new Object[newCapacity];
        for (int i = 0; i < size; i++) {
            newElements[i] = elements[i];
        }
        this.elements = newElements;
    }
}
