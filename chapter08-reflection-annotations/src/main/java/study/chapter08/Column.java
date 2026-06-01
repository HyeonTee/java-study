package study.chapter08;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 필드를 맵의 키 이름에 매핑하는 애너테이션 (인프라 — 완성 코드). {@link ObjectMapper}가 읽는다.
 *
 * <p>{@code value()}가 비어 있으면(기본값 {@code ""}) 필드명을 그대로 키로 쓰고, 값이 있으면
 * 그 값을 키로 쓴다. (Jackson의 {@code @JsonProperty}, JPA의 {@code @Column}의 축소판.)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    /** 매핑할 키 이름. 비우면 필드명을 그대로 사용한다. */
    String value() default "";
}
