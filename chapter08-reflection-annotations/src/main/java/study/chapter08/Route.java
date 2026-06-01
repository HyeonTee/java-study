package study.chapter08;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드를 HTTP 경로에 매핑하는 애너테이션 (인프라 — 완성 코드). {@link RouteScanner}가 스캔한다.
 *
 * <p><strong>{@code @Retention(RUNTIME)}가 핵심이다.</strong> 이게 없으면 기본값
 * {@code CLASS}라서 {@code .class} 파일엔 남지만 JVM이 런타임에 로드하지 않아
 * <strong>리플렉션으로 읽을 수 없다</strong>. 스캐너용 애너테이션이 반드시 {@code RUNTIME}이어야
 * 하는 이유다(이 단원의 대표 함정).
 *
 * <p>{@code @Target(METHOD)}은 이 애너테이션을 메서드에만 붙일 수 있게 제한한다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Route {
    /** 매핑할 경로. 예: {@code @Route("/hello")}. */
    String value();
}
