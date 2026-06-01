package study.chapter03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("MyComparators — Comparator 합성")
class MyComparatorsTest {

    private List<String> names(List<Person> people) {
        return people.stream().map(Person::name).toList();
    }

    @Nested
    @DisplayName("comparing / reversed")
    class ComparingReversed {

        @Test
        void comparing은_키의_자연순서로_정렬() {
            List<Person> people = new ArrayList<>(List.of(
                    new Person("C", 30), new Person("A", 20), new Person("B", 25)));
            people.sort(MyComparators.comparing(Person::age));
            assertEquals(List.of("A", "B", "C"), names(people));
        }

        @Test
        void reversed는_내림차순() {
            Comparator<Integer> nat = MyComparators.comparing(i -> i);
            List<Integer> nums = new ArrayList<>(List.of(3, 1, 2));
            nums.sort(MyComparators.reversed(nat));
            assertEquals(List.of(3, 2, 1), nums);
        }

        @Test
        void reversed_두번이면_원래_순서() {
            Comparator<Integer> nat = MyComparators.comparing(i -> i);
            List<Integer> nums = new ArrayList<>(List.of(3, 1, 2));
            nums.sort(MyComparators.reversed(MyComparators.reversed(nat)));
            assertEquals(List.of(1, 2, 3), nums);
        }
    }

    @Nested
    @DisplayName("thenComparing")
    class ThenComparing {

        @Test
        void 동률이면_2차_기준으로_정렬() {
            Comparator<Person> byAge = MyComparators.comparing(Person::age);
            Comparator<Person> byName = MyComparators.comparing(Person::name);
            List<Person> people = new ArrayList<>(List.of(
                    new Person("B", 20), new Person("A", 20), new Person("C", 10)));
            people.sort(MyComparators.thenComparing(byAge, byName));
            // 나이 오름차순 → 같은 나이는 이름 오름차순: C(10), A(20), B(20)
            assertEquals(List.of("C", "A", "B"), names(people));
        }
    }

    @Nested
    @DisplayName("nullsFirst / nullsLast")
    class Nulls {

        @Test
        void nullsFirst는_null을_앞에() {
            Comparator<Integer> nat = MyComparators.comparing(i -> i);
            List<Integer> nums = new ArrayList<>(Arrays.asList(2, null, 1, null, 3));
            nums.sort(MyComparators.nullsFirst(nat));
            assertEquals(Arrays.asList(null, null, 1, 2, 3), nums);
        }

        @Test
        void nullsLast는_null을_뒤에() {
            Comparator<Integer> nat = MyComparators.comparing(i -> i);
            List<Integer> nums = new ArrayList<>(Arrays.asList(2, null, 1, null, 3));
            nums.sort(MyComparators.nullsLast(nat));
            assertEquals(Arrays.asList(1, 2, 3, null, null), nums);
        }
    }
}
