package study.chapter19.json;

/**
 * 테스트용 샘플 도메인 record — {@link JsonReflect#toJson} 검증용. <strong>완성 제공 픽스처</strong>(http 봉합 테스트도 재사용).
 *
 * <p>{@code id}가 {@code long}인 게 포인트: JSON으로 {@code 1.0}이 아니라 {@code 1}로 직렬화돼야 한다
 * ({@link JsonValue#of(Number)}가 정수/실수를 구분).
 */
public record User(long id, String name) {
}
