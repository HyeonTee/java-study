package study.chapter03;

/**
 * 배열로 표현한 이진 최소 힙(우선순위 큐) — "순서가 덜 필요할 때". 루트는 항상 최솟값이다.
 *
 * <p>BST가 <strong>전순서</strong>(완전 정렬)를 유지한다면, 힙은 <strong>부분 순서</strong>만 유지한다
 * — 부모 ≤ 자식이라는 heap property만 보장하고 형제 간 순서는 없다. "전부 정렬할 필요 없이 최솟값만
 * 빠르게"가 필요할 때 쓴다(작업 스케줄러, 다익스트라 등).
 *
 * <p>완전 이진 트리를 ch01 {@code MyArrayList}처럼 <strong>배열로 표현</strong>한다: 인덱스 {@code i}의
 * 부모는 {@code (i-1)/2}, 자식은 {@code 2i+1}·{@code 2i+2}(0-기반). offer/poll은 O(log n)(siftUp/siftDown),
 * peek는 O(1).
 *
 * <p>주의: 힙 배열 자체는 <strong>정렬되어 있지 않다</strong>(부모-자식 관계만 보장). 다만 poll을
 * 반복하면 오름차순으로 나온다(= 힙 정렬). null 원소는 허용하지 않는다.
 *
 * @param <T> 정렬 가능한 원소 타입
 */
public class MyMinHeap<T extends Comparable<? super T>> {

    static final int DEFAULT_CAPACITY = 16;

    private Object[] heap = new Object[DEFAULT_CAPACITY];
    private int size;

    public int size() {
        throw new UnsupportedOperationException("TODO: size 반환");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("TODO: 비었는지 반환");
    }

    /**
     * 원소를 추가한다. 배열 끝에 넣고 heap property가 회복될 때까지 위로 끌어올린다(siftUp).
     * 가득 차면 용량을 2배로 늘린다.
     *
     * <p>힌트: 부모는 {@code (i-1)/2}. 부모보다 작으면 swap하고 부모 위치로 이동, 반복.
     *
     * @throws NullPointerException {@code value}가 null이면
     */
    public void offer(T value) {
        throw new UnsupportedOperationException("TODO: 끝에 추가 후 siftUp (필요시 용량 2배)");
    }

    /**
     * 최솟값을 제거하고 반환한다. 마지막 원소를 루트로 옮긴 뒤 아래로 내린다(siftDown).
     *
     * <p>힌트: 두 자식({@code 2i+1}, {@code 2i+2}) 중 작은 쪽과 비교해 내려간다.
     *
     * @throws java.util.NoSuchElementException 비어 있으면
     */
    public T poll() {
        throw new UnsupportedOperationException(
                "TODO: 루트 저장 → 마지막을 루트로 → size-- → siftDown(0) (비었으면 NoSuchElementException)");
    }

    /**
     * 최솟값을 제거하지 않고 반환한다.
     *
     * @throws java.util.NoSuchElementException 비어 있으면
     */
    public T peek() {
        throw new UnsupportedOperationException("TODO: heap[0] 반환 (비었으면 NoSuchElementException)");
    }
}
