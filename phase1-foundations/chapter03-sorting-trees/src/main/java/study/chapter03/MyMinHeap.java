package study.chapter03;

import java.util.NoSuchElementException;

/**
 * 배열로 표현한 이진 최소 힙(우선순위 큐) — "순서가 덜 필요할 때". 루트는 항상 최솟값이다.
 *
 * <p>
 * BST가 <strong>전순서</strong>(완전 정렬)를 유지한다면, 힙은 <strong>부분 순서</strong>만 유지한다
 * — 부모 ≤ 자식이라는 heap property만 보장하고 형제 간 순서는 없다. "전부 정렬할 필요 없이 최솟값만
 * 빠르게"가 필요할 때 쓴다(작업 스케줄러, 다익스트라 등).
 *
 * <p>
 * 완전 이진 트리를 ch01 {@code MyArrayList}처럼 <strong>배열로 표현</strong>한다: 인덱스
 * {@code i}의
 * 부모는 {@code (i-1)/2}, 자식은 {@code 2i+1}·{@code 2i+2}(0-기반). offer/poll은 O(log
 * n)(siftUp/siftDown),
 * peek는 O(1).
 *
 * <p>
 * 주의: 힙 배열 자체는 <strong>정렬되어 있지 않다</strong>(부모-자식 관계만 보장). 다만 poll을
 * 반복하면 오름차순으로 나온다(= 힙 정렬). null 원소는 허용하지 않는다.
 *
 * @param <T> 정렬 가능한 원소 타입
 */
public class MyMinHeap<T extends Comparable<? super T>> {

    static final int DEFAULT_CAPACITY = 16;

    private Object[] heap = new Object[DEFAULT_CAPACITY];
    private int size;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 원소를 추가한다. 배열 끝에 넣고 heap property가 회복될 때까지 위로 끌어올린다(siftUp).
     * 가득 차면 용량을 2배로 늘린다.
     *
     * <p>
     * 힌트: 부모는 {@code (i-1)/2}. 부모보다 작으면 swap하고 부모 위치로 이동, 반복.
     *
     * @throws NullPointerException {@code value}가 null이면
     */
    @SuppressWarnings("unchecked")
    public void offer(T value) {
        if (value == null) {
            throw new NullPointerException();
        }

        if (size == heap.length) {
            Object[] newHeap = new Object[size * 2];
            for (int i = 0; i < size; i++) {
                newHeap[i] = heap[i];
            }

            heap = newHeap;
        }

        int i = size++;
        heap[i] = value;
        while (0 < i && ((T) heap[i]).compareTo((T) heap[(i - 1) / 2]) < 0) {
            Object temp = heap[i];
            heap[i] = heap[(i - 1) / 2];
            heap[(i - 1) / 2] = temp;
            i = (i - 1) / 2;
        }
    }

    /**
     * 최솟값을 제거하고 반환한다. 마지막 원소를 루트로 옮긴 뒤 아래로 내린다(siftDown).
     *
     * <p>
     * 힌트: 두 자식({@code 2i+1}, {@code 2i+2}) 중 작은 쪽과 비교해 내려간다.
     *
     * @throws java.util.NoSuchElementException 비어 있으면
     */
    @SuppressWarnings("unchecked")
    public T poll() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        T root = (T) heap[0];
        heap[0] = heap[--size];
        heap[size] = null;

        int i = 0;
        while (i * 2 + 1 < size) {
            T leftChild = (T) heap[i * 2 + 1];
            T rightChild = i * 2 + 2 < size ? (T) heap[i * 2 + 2] : null;

            T smallerChild;
            if (rightChild == null) {
                smallerChild = leftChild;
            } else {
                smallerChild = leftChild.compareTo(rightChild) > 0 ? rightChild : leftChild;
            }
            if (smallerChild.compareTo((T) heap[i]) >= 0) {
                break;
            }

            if (smallerChild == leftChild) {
                heap[i * 2 + 1] = heap[i];
                heap[i] = smallerChild;
                i = i * 2 + 1;
            } else {
                heap[i * 2 + 2] = heap[i];
                heap[i] = smallerChild;
                i = i * 2 + 2;
            }
        }

        return root;
    }

    /**
     * 최솟값을 제거하지 않고 반환한다.
     *
     * @throws java.util.NoSuchElementException 비어 있으면
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        return (T) heap[0];
    }
}
