package study.chapter03;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Stack;

/**
 * 비균형 이진탐색트리(BST) — 이 단원의 간판. 모든 노드가 "왼쪽 &lt; 자신 &lt; 오른쪽" 불변식을 만족한다.
 *
 * <p>
 * ch01의 노드·포인터 조작(MyLinkedList의 next/prev)을 2차원으로 확장하고, ch02의 "키로 값 찾기"를
 * <strong>순서 기반</strong>으로 다시 한다. 핵심 통찰: <strong>중위순회(in-order)가 곧 정렬된
 * 순서</strong>다.
 *
 * <p>
 * 제네릭 바운드가 {@code <T extends Comparable<? super T>>}인 이유:
 * {@code Dog extends Animal}이고
 * {@code Animal implements Comparable<Animal>}이면 {@code Dog}은
 * {@code Comparable<Dog>}가 아니라
 * {@code Comparable<Animal>}을 (상속으로) 만족한다. {@code <T extends Comparable<T>>}였다면
 * {@code MyBinarySearchTree<Dog>}가 컴파일되지 않는다. JDK의
 * {@code Collections.sort}/{@code TreeMap}이
 * 모두 {@code ? super T}를 쓰는 이유다.
 *
 * <p>
 * 주의: 평균 O(log n)이지만 <strong>정렬된 입력을 넣으면 한쪽으로 치우쳐 O(n)으로 퇴화</strong>한다
 * (균형 트리 AVL/Red-Black이 회전으로 이를 해결 — README 이론 참고. 본 구현은 회전하지 않는다).
 * 중복 키는 허용하지 않으며({@code insert}가 false), null 원소도 허용하지 않는다.
 *
 * @param <T> 스스로(또는 상위 타입으로) 비교 가능한 원소 타입
 */
public class MyBinarySearchTree<T extends Comparable<? super T>> {

    private static class Node<T> {
        T value;
        Node<T> left;
        Node<T> right;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> root;
    private int size;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 값을 삽입한다. 이미 같은 값({@code compareTo==0})이 있으면 변경 없이 {@code false}.
     *
     * <p>
     * 힌트: 루트부터 {@code compareTo} 부호로 좌/우로 내려가다 빈 자리에 새 노드.
     *
     * @return 실제로 추가되면 {@code true}
     * @throws NullPointerException {@code value}가 null이면
     */
    public boolean insert(T value) {
        if (value == null) {
            throw new NullPointerException();
        }
        if (root == null) {
            root = new Node<T>(value);
            size++;
            return true;
        }

        Node<T> curr = root;
        Node<T> parent = null;

        while (curr != null) {
            int cmp = curr.value.compareTo(value);
            if (cmp == 0) {
                return false;
            }

            parent = curr;
            if (cmp > 0) {
                curr = curr.left;
            } else {
                curr = curr.right;
            }
        }

        Node<T> newNode = new Node<>(value);
        if (parent.value.compareTo(value) > 0) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }
        size++;

        return true;
    }

    /** {@code compareTo==0}인 값이 있으면 {@code true}. */
    public boolean contains(T value) {
        Node<T> curr = root;

        while (curr != null) {
            int cmp = curr.value.compareTo(value);
            if (cmp == 0) {
                return true;
            } else if (cmp > 0) {
                curr = curr.left;
            } else {
                curr = curr.right;
            }
        }

        return false;
    }

    /**
     * 값을 삭제한다. <strong>3가지 경우</strong>를 모두 다뤄라:
     * <ol>
     * <li>리프 → 그냥 떼낸다.</li>
     * <li>자식 1개 → 그 자식을 자기 자리로 올린다.</li>
     * <li>자식 2개 → 오른쪽 서브트리의 최소값(중위 후속자)으로 값을 대체하고, 그 후속자를 삭제한다.</li>
     * </ol>
     *
     * @return 실제로 삭제되면 {@code true}, 없던 값이면 {@code false}
     */
    public boolean delete(T value) {
        Node<T> curr = root;
        Node<T> parent = null;

        while (curr != null) {
            int cmp = curr.value.compareTo(value);

            if (cmp > 0) {
                parent = curr;
                curr = curr.left;
            } else if (cmp < 0) {
                parent = curr;
                curr = curr.right;
            } else {
                boolean isLeaf = curr.left == null && curr.right == null;
                boolean onlyRightChild = curr.left == null && curr.right != null;
                boolean onlyLeftChild = curr.left != null && curr.right == null;
                if (parent == null) {
                    if (isLeaf) {
                        root = null;
                    } else if (onlyRightChild) {
                        root = curr.right;
                    } else if (onlyLeftChild) {
                        root = curr.left;
                    } else {
                        root.value = extractMinNode(curr.right, curr);
                    }
                } else {
                    boolean isLeft = parent.left == curr;
                    if (isLeaf) {
                        if (isLeft) {
                            parent.left = null;
                        } else {
                            parent.right = null;
                        }
                    } else if (onlyRightChild) {
                        if (isLeft) {
                            parent.left = curr.right;
                        } else {
                            parent.right = curr.right;
                        }
                    } else if (onlyLeftChild) {
                        if (isLeft) {
                            parent.left = curr.left;
                        } else {
                            parent.right = curr.left;
                        }
                    } else {
                        if (isLeft) {
                            parent.left.value = extractMinNode(curr.right, curr);
                        } else {
                            parent.right.value = extractMinNode(curr.right, curr);
                        }
                    }
                }

                size--;
                return true;
            }
        }

        return false;
    }

    private T extractMinNode(Node<T> node, Node<T> parent) {
        Node<T> substitute = null;

        boolean isLeft = false;
        while (node.left != null) {
            parent = node;
            node = node.left;
            isLeft = true;
        }

        substitute = node;
        if (isLeft) {
            parent.left = node.right;
        } else {
            parent.right = node.right;
        }

        return substitute.value;
    }

    /**
     * 가장 작은 값.
     *
     * @throws java.util.NoSuchElementException 비어 있으면
     */
    public T min() {
        if (root == null) {
            throw new NoSuchElementException();
        }

        Node<T> curr = root;

        while (curr.left != null) {
            curr = curr.left;
        }

        return curr.value;
    }

    /**
     * 가장 큰 값.
     *
     * @throws java.util.NoSuchElementException 비어 있으면
     */
    public T max() {
        if (root == null) {
            throw new NoSuchElementException();
        }

        Node<T> curr = root;

        while (curr.right != null) {
            curr = curr.right;
        }

        return curr.value;
    }

    /**
     * 중위순회 결과를 <strong>오름차순</strong> 리스트로 반환한다(빈 트리면 빈 리스트).
     *
     * <p>
     * 힌트: 왼쪽 → 자신 → 오른쪽 재귀로 append.
     */
    public List<T> inorder() {
        List<T> result = new ArrayList<>();

        Node<T> curr = root;
        Stack<Node<T>> stack = new Stack<>();
        while (curr != null || !stack.isEmpty()) {
            while (curr != null) {
                stack.push(curr);
                curr = curr.left;
            }

            curr = stack.pop();

            result.add(curr.value);

            curr = curr.right;
        }

        return result;
    }

    /**
     * 트리의 높이 = 루트에서 가장 먼 리프까지의 <strong>간선 수</strong>. 빈 트리는 {@code -1},
     * 노드 1개는 {@code 0}.
     *
     * <p>
     * 이 메서드로 편향을 관찰한다: 정렬된 입력을 넣으면 높이가 {@code size-1}(연결 리스트로 퇴화)이
     * 되는 것을 테스트가 보여준다 — "왜 균형이 필요한가"의 증거.
     */
    public int height() {
        if (root == null) {
            return -1;
        }

        int height = -1;

        Queue<Node<T>> queue = new ArrayDeque<>();

        queue.add(root);
        while (!queue.isEmpty()) {
            height++;

            for (int i = 0; i < queue.size(); i++) {
                Node<T> node = queue.poll();

                if (node.left != null) {
                    queue.add(node.left);
                }

                if (node.right != null) {
                    queue.add(node.right);
                }
            }
        }

        return height;
    }
}
