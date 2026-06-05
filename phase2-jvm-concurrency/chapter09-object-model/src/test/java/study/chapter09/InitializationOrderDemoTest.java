package study.chapter09;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 데모(채점 아님) — {@link InitializationOrderDemo}가 보여주는 초기화 순서와 생성자-오버라이드
 * 함정을 잠가둔다. 학습자가 구현하는 코드가 아니라, 관찰 대상이 정말 그렇게 동작하는지 확인하는
 * 테스트다.
 */
@DisplayName("InitializationOrderDemo (데모) — 초기화 순서와 생성자-오버라이드 함정")
class InitializationOrderDemoTest {

    @Test
    @DisplayName("상위 생성자가 먼저 끝나고, 그 다음 하위 초기화/생성자가 실행된다")
    void 상위가_하위보다_먼저() {
        List<String> log = InitializationOrderDemo.run();
        int baseStart = indexOfContains(log, "Base 생성자 시작");
        int childBody = indexOfContains(log, "Child 생성자 본문");
        assertTrue(baseStart >= 0 && childBody >= 0);
        assertTrue(baseStart < childBody, "Base 생성자가 Child 생성자 본문보다 먼저여야 한다: " + log);
    }

    @Test
    @DisplayName("함정: 상위 생성자가 부른 오버라이드 메서드는 하위 필드를 아직 0으로 본다")
    void 생성자_오버라이드_함정() {
        List<String> log = InitializationOrderDemo.run();
        // Base 생성자 안에서 실행된 Child.overridable이 본 childField는 42가 아니라 0.
        assertTrue(log.stream().anyMatch(s -> s.contains("childField=0")),
                "상위 생성자 시점에 하위 필드는 기본값(0)으로 보여야 한다: " + log);
        // 생성이 끝난 뒤(Child 생성자 본문)에는 42로 초기화되어 있다.
        assertTrue(log.stream().anyMatch(s -> s.contains("Child 생성자 본문") && s.contains("childField=42")),
                "생성 완료 후에는 childField=42 여야 한다: " + log);
    }

    private static int indexOfContains(List<String> log, String needle) {
        for (int i = 0; i < log.size(); i++) {
            if (log.get(i).contains(needle)) {
                return i;
            }
        }
        return -1;
    }
}
