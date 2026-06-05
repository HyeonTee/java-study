package study.chapter07;

import java.util.List;

/**
 * try-with-resources의 두 가지 핵심 동작을 보여주는 시나리오 모음 — 정적 유틸리티.
 *
 * <p>{@link #openTwoAndUse}는 여러 리소스의 close가 선언 <strong>역순</strong>으로 일어남을,
 * {@link #captureSuppressed}는 본문 예외가 주(primary) 예외가 되고 close 예외가
 * {@code getSuppressed()}로 부착됨을 {@link TrackedResource}의 로그로 드러낸다.
 */
public final class ResourceScenarios {

    private ResourceScenarios() {
    }

    /**
     * 이름 "A", "B" 순서로(둘 다 {@code failOnClose=false}) 두 리소스를 try-with-resources로 열고
     * 각각 {@link TrackedResource#use()}를 호출한다.
     *
     * <p>힌트: {@code try (var a = new TrackedResource("A", log, false);
     * var b = new TrackedResource("B", log, false)) { a.use(); b.use(); }}.
     * close는 B → A 역순으로 일어난다(로그로 확인).
     */
    public static void openTwoAndUse(List<String> log) {
        throw new UnsupportedOperationException(
                "TODO: try-with-resources로 A, B를 열고 각각 use() 하라 (close 역순 관찰)");
    }

    /**
     * {@code failOnClose=true}인 리소스 하나를 try-with-resources로 열고, 본문에서
     * {@code throw new RuntimeException("primary")} 한다. 그 {@link RuntimeException}을 catch해서 반환한다.
     *
     * <p>힌트: {@code try (var r = new TrackedResource("A", log, true)) { throw new
     * RuntimeException("primary"); } catch (RuntimeException e) { return e; }}. 반환된 예외의
     * {@code getSuppressed()}에는 close가 던진 {@link IllegalStateException}이 붙어 있다.
     */
    public static RuntimeException captureSuppressed(List<String> log) {
        throw new UnsupportedOperationException(
                "TODO: failOnClose=true 리소스를 열고 본문에서 RuntimeException을 던진 뒤 catch해서 반환하라");
    }
}
