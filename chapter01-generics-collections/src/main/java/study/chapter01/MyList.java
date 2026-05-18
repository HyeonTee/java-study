package study.chapter01;

import java.util.Iterator;

/**
 * 직접 구현할 리스트의 공통 인터페이스.
 * MyArrayList와 MyLinkedList가 모두 이 계약을 따른다.
 */
public interface MyList<T> extends Iterable<T> {

    int size();

    boolean isEmpty();

    /** 끝에 원소를 추가한다. */
    void add(T value);

    /** 지정한 인덱스에 원소를 삽입한다. 기존 원소들은 뒤로 밀린다. */
    void add(int index, T value);

    /** 인덱스 위치의 원소를 반환한다. */
    T get(int index);

    /** 인덱스 위치의 원소를 새 값으로 교체하고 이전 값을 반환한다. */
    T set(int index, T value);

    /** 인덱스 위치의 원소를 제거하고 반환한다. */
    T remove(int index);

    /** value와 같은(equals) 첫 원소의 인덱스를 반환한다. 없으면 -1. */
    int indexOf(T value);

    boolean contains(T value);

    void clear();

    @Override
    Iterator<T> iterator();
}
