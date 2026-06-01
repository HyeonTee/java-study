package study.chapter08;

import java.util.ArrayList;
import java.util.List;

/**
 * 객체 초기화 순서와 "생성자가 오버라이드 메서드를 호출하는 함정"을 <strong>관찰</strong>하는 데모.
 *
 * <p>이 클래스는 연습 문제가 <strong>아니다</strong>(이미 완성되어 있다). 초기화 순서는 직접 구현할
 * 거리가 마땅치 않고(스텁이 곧 정답 노출), 규칙을 <strong>눈으로 확인</strong>하는 것이 핵심이라
 * ch07의 {@code VisibilityDemo}처럼 관찰용 데모로 둔다.
 *
 * <p>{@link #run()}을 호출하면 {@code new Child()} 한 번이 일으키는 초기화 이벤트가 순서대로
 * 기록되어 반환된다. 핵심 관찰 두 가지:
 * <ol>
 *   <li><strong>상위 먼저</strong>: {@code Child()}의 암묵적 {@code super()}가 먼저 끝나고
 *       ({@code Base} 생성자 완료), 그 다음 {@code Child}의 필드/인스턴스 초기화, 마지막에
 *       {@code Child} 생성자 본문이 실행된다.</li>
 *   <li><strong>생성자-오버라이드 함정</strong>: {@code Base} 생성자가 부른 {@code overridable()}은
 *       동적 디스패치로 {@code Child}의 오버라이드 버전이 실행되지만, 그 시점엔 {@code Child}의
 *       필드 초기화가 <strong>아직</strong> 안 끝나서 {@code childField}가 {@code 42}가 아니라
 *       <strong>기본값 {@code 0}</strong>으로 보인다. (그래서 생성자에서 오버라이드 가능한
 *       메서드를 부르면 안 된다 — Effective Java Item 19.)</li>
 * </ol>
 */
public final class InitializationOrderDemo {

    private static final List<String> LOG = new ArrayList<>();

    private InitializationOrderDemo() {
    }

    /**
     * {@code new Child()} 한 번이 일으키는 초기화 이벤트를 순서대로 기록해 반환한다.
     */
    public static List<String> run() {
        LOG.clear();
        new Child();
        return new ArrayList<>(LOG);
    }

    static class Base {
        Base() {
            LOG.add("1. Base 생성자 시작");
            overridable();                       // 가상 호출 → Child.overridable 이 실행됨
            LOG.add("3. Base 생성자 끝");
        }

        void overridable() {
            LOG.add("(Base.overridable — 오버라이드되면 안 불림)");
        }
    }

    static class Child extends Base {
        int childField = 42;                     // 이 초기화는 super() 이후에 실행된다

        {
            LOG.add("4. Child 인스턴스 초기화 (childField=" + childField + ")");
        }

        Child() {
            // 암묵적 super() 가 먼저 (위 1~3) → 그 다음 필드/인스턴스 초기화(4) → 그 다음 이 본문(5)
            LOG.add("5. Child 생성자 본문 (childField=" + childField + ")");
        }

        @Override
        void overridable() {
            // Base 생성자에서 불릴 때 childField 는 아직 0 — Child 필드 초기화 전이므로!
            LOG.add("2. Child.overridable 실행 — childField=" + childField + " (아직 0!)");
        }
    }
}
