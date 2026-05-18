package study.chapter01;

import java.util.Iterator;

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
        throw new UnsupportedOperationException("TODO: implement size()");
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("TODO: implement isEmpty()");
    }

    @Override
    public void add(T value) {
        throw new UnsupportedOperationException("TODO: implement add(value) — 끝에 추가, 가득 차면 ensureCapacity 호출");
    }

    @Override
    public void add(int index, T value) {
        throw new UnsupportedOperationException("TODO: implement add(index, value) — index 위치에 삽입, 뒤쪽 원소를 한 칸씩 밀어라");
    }

    @Override
    public T get(int index) {
        throw new UnsupportedOperationException("TODO: implement get(index) — 범위 체크 후 (T) 캐스팅");
    }

    @Override
    public T set(int index, T value) {
        throw new UnsupportedOperationException("TODO: implement set(index, value) — 이전 값을 반환");
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException("TODO: implement remove(index) — 뒤쪽 원소를 한 칸씩 당기고 마지막 슬롯은 null로");
    }

    @Override
    public int indexOf(T value) {
        throw new UnsupportedOperationException("TODO: implement indexOf(value) — null 안전한 비교 사용");
    }

    @Override
    public boolean contains(T value) {
        throw new UnsupportedOperationException("TODO: implement contains(value)");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("TODO: implement clear() — 참조를 null로 끊고 size=0");
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException("TODO: implement iterator() — 내부 클래스로 cursor 추적");
    }

    /**
     * 내부 배열이 minCapacity를 담을 수 있도록 확장한다. 현재 용량의 1.5배와 minCapacity 중 큰 값을 새 용량으로 한다.
     */
    private void ensureCapacity(int minCapacity) {
        throw new UnsupportedOperationException("TODO: implement ensureCapacity(minCapacity) — 새 배열 만들고 복사");
    }
}
