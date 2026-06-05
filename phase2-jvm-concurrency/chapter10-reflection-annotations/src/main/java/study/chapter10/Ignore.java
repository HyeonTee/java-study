package study.chapter10;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이 필드를 {@link ObjectMapper} 매핑에서 제외하라는 표식 애너테이션 (인프라 — 완성 코드).
 *
 * <p>원소가 없는 <strong>마커(marker) 애너테이션</strong>이다. 애너테이션은 그 자체로 동작하지
 * 않는다 — 그저 메타데이터일 뿐이고, "제외"라는 동작은 그것을 읽는 {@link ObjectMapper}가
 * 부여한다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Ignore {
}
