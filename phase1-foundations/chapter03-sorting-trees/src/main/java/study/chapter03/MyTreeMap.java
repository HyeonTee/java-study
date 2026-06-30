package study.chapter03;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 키가 항상 정렬된 순서로 유지되는 맵 (BST 기반) — ch02 {@code MyHashMap}과의 정면 대비.
 *
 * <p>
 * ch02 해시 맵은 평균 O(1)이지만 <strong>순서 보장이 없다</strong>. 이 맵은 put/get O(log n)이지만
 * <strong>키를 정렬 순서로 순회</strong>할 수 있고, {@link #floorKey}/{@link #ceilingKey} 같은
 * <strong>범위 질의</strong>를 지원한다(JDK
 * {@link java.util.TreeMap}/{@code NavigableMap}에 대응).
 * "순서가 필요하면 트리, 아니면 해시"라는 선택 기준을 코드로 증명한다.
 *
 * <p>
 * 여기서는 ch02가 이미 다룬 기본 맵 동작(해시·체이닝)을 반복하지 않는다. 이 단원의 초점은
 * <strong>(1) compareTo로 위치 결정, (2) 항해 연산(floor/ceiling/first), (3) 정렬
 * 순회</strong>다.
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
        return size;
    }

    /**
     * 키-값을 저장한다. 같은 키가 있으면 값을 교체하고 <strong>이전 값</strong>을 반환, 새 키면 null
     * (ch02 {@code MyMap.put}과 동일한 반환 규약).
     *
     * @throws NullPointerException {@code key}가 null이면
     */
    public V put(K key, V value) {
        if (key == null) {
            throw new NullPointerException();
        }

        if (root == null) {
            root = new Node<>(key, value);
            size++;
            return null;
        }

        Node<K, V> curr = root;
        Node<K, V> parent = null;

        boolean isLeft = false;
        while (curr != null) {
            int cmp = curr.key.compareTo(key);
            if (cmp == 0) {
                V oldValue = curr.value;
                curr.value = value;

                return oldValue;
            }

            parent = curr;
            if (cmp > 0) {
                curr = curr.left;
                isLeft = true;
            } else {
                curr = curr.right;
                isLeft = false;
            }
        }

        if (isLeft) {
            parent.left = new Node<K, V>(key, value);
        } else {
            parent.right = new Node<K, V>(key, value);
        }

        size++;

        return null;
    }

    /** 키에 대응하는 값. 없으면 null. (null 값과 미존재 구분은 {@link #containsKey}로.) */
    public V get(K key) {
        if (key == null) {
            throw new NullPointerException();
        }

        if (root == null) {
            return null;
        }

        Node<K, V> curr = root;

        while (curr != null) {
            int cmp = curr.key.compareTo(key);

            if (cmp == 0) {
                return curr.value;
            }

            if (cmp > 0) {
                curr = curr.left;
            } else {
                curr = curr.right;
            }
        }

        return null;
    }

    public boolean containsKey(K key) {
        if (key == null) {
            throw new NullPointerException();
        }

        if (root == null) {
            return false;
        }

        Node<K, V> curr = root;

        while (curr != null) {
            int cmp = curr.key.compareTo(key);

            if (cmp == 0) {
                return true;
            }

            if (cmp > 0) {
                curr = curr.left;
            } else {
                curr = curr.right;
            }
        }

        return false;
    }

    /**
     * 키를 제거하고 그 값을 반환한다. 없었으면 null. (삭제 로직은 BST의 3경우와 동일.)
     */
    public V remove(K key) {
        if (root == null) {
            return null;
        }

        Node<K, V> curr = root;
        Node<K, V> parent = null;
        boolean isLeft = false;
        while (curr != null) {
            int cmp = curr.key.compareTo(key);

            if (cmp == 0) {
                V val = curr.value;

                if (curr.left == null && curr.right == null) {
                    if (parent == null) {
                        root = null;
                    } else {
                        if (isLeft) {
                            parent.left = null;
                        } else {
                            parent.right = null;
                        }
                    }
                } else if (curr.left == null) {
                    if (parent != null) {
                        if (isLeft) {
                            parent.left = curr.right;
                        } else {
                            parent.right = curr.right;
                        }
                    } else {
                        root = curr.right;
                    }
                } else if (curr.right == null) {
                    if (parent != null) {
                        if (isLeft) {
                            parent.left = curr.left;
                        } else {
                            parent.right = curr.left;
                        }
                    } else {
                        root = curr.left;
                    }
                } else {
                    boolean isFirst = true;
                    Node<K, V> child = curr.right;
                    Node<K, V> leaf = null;
                    while (child.left != null) {
                        leaf = child;
                        child = child.left;
                        isFirst = false;
                    }

                    if (parent != null) {
                        if (isLeft) {
                            parent.left = child;
                        } else {
                            parent.right = child;
                        }
                    } else {
                        root = child;
                    }

                    if (!isFirst) {
                        leaf.left = child.right;
                        child.right = curr.right;
                    }
                    child.left = curr.left;
                }

                size--;

                return val;
            }

            parent = curr;
            if (cmp < 0) {
                isLeft = false;
                curr = curr.right;
            } else {
                isLeft = true;
                curr = curr.left;
            }
        }

        return null;
    }

    /**
     * 가장 작은 키.
     *
     * @throws java.util.NoSuchElementException 비어 있으면
     */
    public K firstKey() {
        if (root == null) {
            throw new NoSuchElementException();
        }
        Node<K, V> curr = root;
        while (curr.left != null) {
            curr = curr.left;
        }

        return curr.key;
    }

    /**
     * {@code key} <strong>이하</strong>(≤)인 키 중 가장 큰 키. 그런 키가 없으면 null.
     *
     * <p>
     * 예: 키 {10,20,30}에서 {@code floorKey(25)==20}, {@code floorKey(10)==10},
     * {@code floorKey(5)==null}.
     *
     * <p>
     * 힌트: 내려가며 "key 이하인 마지막으로 본 키"를 후보로 기억한다. {@code compareTo>0}이면 현재 키를
     * 후보로 두고 오른쪽으로.
     */
    public K floorKey(K key) {
        Node<K, V> curr = root;
        Node<K, V> candidate = null;

        while (curr != null) {
            int cmp = curr.key.compareTo(key);

            if (cmp == 0) {
                return curr.key;
            }

            if (cmp > 0) {
                curr = curr.left;
            } else {
                candidate = curr;
                curr = curr.right;
            }
        }

        return candidate == null ? null : candidate.key;
    }

    /**
     * {@code key} <strong>이상</strong>(≥)인 키 중 가장 작은 키. 없으면 null.
     *
     * <p>
     * 예: 키 {10,20,30}에서 {@code ceilingKey(25)==30}, {@code ceilingKey(30)==30},
     * {@code ceilingKey(35)==null}.
     */
    public K ceilingKey(K key) {
        Node<K, V> curr = root;
        Node<K, V> candidate = null;

        while (curr != null) {
            int cmp = curr.key.compareTo(key);

            if (cmp == 0) {
                return curr.key;
            }

            if (cmp < 0) {
                curr = curr.right;
            } else {
                candidate = curr;
                curr = curr.left;
            }
        }

        return candidate == null ? null : candidate.key;
    }

    /**
     * 모든 키를 <strong>오름차순</strong>으로 담은 리스트(중위순회). 빈 맵이면 빈 리스트.
     * (JDK keySet과 달리 뷰가 아닌 스냅샷 리스트를 반환한다 — 학습 단순화.)
     */
    public List<K> keySet() {
        List<K> result = new ArrayList<>();
        if (root == null) {
            return result;
        }

        recur(root, result);

        return result;
    }

    private void recur(Node<K, V> node, List<K> list) {
        if (node == null) {
            return;
        }

        if (node.left != null) {
            recur(node.left, list);
        }
        list.add(node.key);

        if (node.right != null) {
            recur(node.right, list);
        }
    }
}
