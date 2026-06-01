package study.chapter08;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// ── 이 파일은 테스트 픽스처 모음이다 (학습자가 구현하지 않음). ──────────────
// 한 파일에 여러 package-private 최상위 타입을 둔다(공개 타입은 없음).

/** 동적 프록시 / 라우트 스캔 대상 인터페이스. */
interface Greeter {
    String greet(String name);
    int count();
}

/** {@code @Route}가 붙은 핸들러 + 프록시 대상. */
class GreetingService implements Greeter {
    @Route("/hello")
    public String greet(String name) {
        return "Hello, " + name;
    }

    @Route("/count")
    public int count() {
        return 42;
    }

    /** 애너테이션 없음 — 스캔에서 제외되어야 한다. */
    public String secret() {
        return "no-route";
    }

    /** 일부러 예외를 던지는 메서드 — 프록시/Reflect의 예외 투명성 검증용. */
    public String boom() {
        throw new IllegalStateException("boom");
    }
}

/** 같은 경로가 둘 — 라우트 충돌 검증용. */
class DuplicateRoutes {
    @Route("/dup")
    public void a() {
    }

    @Route("/dup")
    public void b() {
    }
}

/** {@link Reflect} 기초 유틸 대상. */
class Calculator {
    private int hidden = 7;

    public Calculator() {
    }

    public int add(int a, int b) {
        return a + b;
    }

    /** b가 0이면 ArithmeticException — invoke의 예외 모델 검증용. */
    public int divide(int a, int b) {
        return a / b;
    }
}

/** {@link ObjectMapper} 대상. */
class Person {
    @Column("user_name")
    private String name;

    private int age;

    @Ignore
    private String temp;

    static int CONSTANT = 1;   // static — 매핑에서 제외되어야 한다

    Person() {
    }

    Person(String name, int age, String temp) {
        this.name = name;
        this.age = age;
        this.temp = temp;
    }
}

/** 런타임에 사라지는 SOURCE 보존 애너테이션 — @Retention 함정 시연용. */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@interface SourceOnly {
}

class HasSourceAnnotation {
    @SourceOnly
    public void marked() {
    }
}
