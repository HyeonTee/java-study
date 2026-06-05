package study.chapter07;

import java.util.List;

/**
 * 자신의 생명주기(open/use/close)를 로그에 기록하는 {@link AutoCloseable} — try-with-resources 관찰용.
 *
 * <p>여러 리소스의 close가 <strong>역순</strong>으로 일어나는지, 본문 예외와 close 예외가 충돌할 때
 * close 예외가 {@code getSuppressed()}로 붙는지를 로그로 확인하기 위한 도구다.
 * 생성 즉시 {@code "open:이름"}을 로그에 남긴다.
 */
public final class TrackedResource implements AutoCloseable {

    private final String name;
    private final List<String> log;
    private final boolean failOnClose;

    public TrackedResource(String name, List<String> log, boolean failOnClose) {
        this.name = name;
        this.log = log;
        this.failOnClose = failOnClose;
        log.add("open:" + name);
    }

    /**
     * 리소스를 "사용"한다 — 로그에 {@code "use:이름"}을 추가한다.
     *
     * <p>힌트: {@code log.add("use:" + name);}.
     */
    public void use() {
        throw new UnsupportedOperationException(
                "TODO: log에 \"use:\" + name 을 추가하라");
    }

    /**
     * 리소스를 닫는다 — 로그에 {@code "close:이름"}을 추가하고, {@code failOnClose}면 예외를 던진다.
     *
     * <p>힌트: 먼저 {@code log.add("close:" + name);} 한 뒤, {@code failOnClose}가 참이면
     * {@code throw new IllegalStateException("close failed: " + name);}.
     */
    @Override
    public void close() {
        throw new UnsupportedOperationException(
                "TODO: log에 \"close:\" + name 추가 후 failOnClose면 IllegalStateException을 던져라");
    }
}
