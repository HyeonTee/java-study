package study.chapter03;

// 테스트 픽스처 모음 (학습자가 구현하지 않음). 한 파일에 여러 package-private 타입.

/** Comparator 합성 테스트용 값 객체. */
record Person(String name, int age) {
}

/**
 * BST/힙의 {@code <T extends Comparable<? super T>>} 바운드가 상속에서 동작함을 보이는 픽스처.
 * {@code Dog}은 {@code compareTo}를 상속하므로 {@code Comparable<Animal>}이지 {@code Comparable<Dog>}가
 * 아니다 — 바운드가 {@code <T extends Comparable<T>>}였다면 {@code MyBinarySearchTree<Dog>}는 컴파일되지 않는다.
 */
class Animal implements Comparable<Animal> {
    final int weight;

    Animal(int weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(Animal o) {
        return Integer.compare(weight, o.weight);
    }
}

class Dog extends Animal {
    Dog(int weight) {
        super(weight);
    }
}
