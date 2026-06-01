package study.chapter08;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("MethodTable — 동적 디스패치 직접 구현")
class MethodTableTest {

    // 계층:  Animal ─ speak="Animal.speak", sleep="Animal.sleep"
    //          └ Dog   ─ speak="Dog.speak"            (sleep은 상속)
    //              └ Puppy (아무것도 오버라이드 안 함 — 전부 상속)
    //          └ Cat   (아무것도 선언 안 함 — 전부 Animal 상속)
    ClassNode animal, dog, puppy, cat;

    @BeforeEach
    void setUp() {
        animal = new ClassNode("Animal", null);
        animal.declareMethod("speak", "Animal.speak");
        animal.declareMethod("sleep", "Animal.sleep");

        dog = new ClassNode("Dog", animal);
        dog.declareMethod("speak", "Dog.speak");

        puppy = new ClassNode("Puppy", dog);

        cat = new ClassNode("Cat", animal);
    }

    @Nested
    @DisplayName("resolve — 수신 타입에서 위로 올라가며 첫 선언을 찾는다")
    class Resolve {

        @Test
        void 직접_선언한_메서드를_해석한다() {
            assertEquals(Optional.of("Dog.speak"), MethodTable.resolve(dog, "speak"));
        }

        @Test
        void 선언이_없으면_상위에서_상속받아_해석한다() {
            assertEquals(Optional.of("Animal.sleep"), MethodTable.resolve(dog, "sleep"));
        }

        @Test
        void 오버라이드하지_않은_하위타입은_상위_구현을_쓴다() {
            assertEquals(Optional.of("Animal.speak"), MethodTable.resolve(cat, "speak"));
        }

        @Test
        void 여러_단계_상속도_따라_올라간다() {
            assertEquals(Optional.of("Dog.speak"), MethodTable.resolve(puppy, "speak"));  // Dog에서
            assertEquals(Optional.of("Animal.sleep"), MethodTable.resolve(puppy, "sleep")); // Animal까지
        }

        @Test
        void 어디에도_없으면_빈_Optional() {
            assertEquals(Optional.empty(), MethodTable.resolve(dog, "fly"));
        }
    }

    @Nested
    @DisplayName("resolveSuper — 호출자의 상위부터 해석 (런타임 타입 무시)")
    class ResolveSuper {

        @Test
        void super는_자신을_건너뛰고_상위부터_찾는다() {
            // Dog 안에서 super.speak() → Dog의 Dog.speak를 건너뛰고 Animal.speak
            assertEquals(Optional.of("Animal.speak"), MethodTable.resolveSuper(dog, "speak"));
        }

        @Test
        void super가_상속메서드를_찾을_때도_상위부터() {
            assertEquals(Optional.of("Animal.sleep"), MethodTable.resolveSuper(dog, "sleep"));
        }

        @Test
        void 최상위에서_super는_빈_Optional() {
            assertEquals(Optional.empty(), MethodTable.resolveSuper(animal, "speak"));
        }
    }

    @Nested
    @DisplayName("buildVTable — 상위를 채우고 하위 선언으로 덮어쓴다")
    class BuildVTable {

        @Test
        void 오버라이드가_상위를_가린다() {
            Map<String, String> vtable = MethodTable.buildVTable(dog);
            assertEquals("Dog.speak", vtable.get("speak"));   // 오버라이드
            assertEquals("Animal.sleep", vtable.get("sleep")); // 상속
            assertEquals(2, vtable.size());
        }

        @Test
        void 최상위_클래스의_vtable은_자기_선언_그대로() {
            Map<String, String> vtable = MethodTable.buildVTable(animal);
            assertEquals("Animal.speak", vtable.get("speak"));
            assertEquals("Animal.sleep", vtable.get("sleep"));
            assertEquals(2, vtable.size());
        }

        @Test
        void 오버라이드없이_상속만_받은_타입은_상위와_같은_vtable() {
            Map<String, String> vtable = MethodTable.buildVTable(puppy);
            assertEquals("Dog.speak", vtable.get("speak"));
            assertEquals("Animal.sleep", vtable.get("sleep"));
        }
    }
}
