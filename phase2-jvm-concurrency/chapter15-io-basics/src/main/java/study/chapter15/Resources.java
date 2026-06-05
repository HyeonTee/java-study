package study.chapter15;

/**
 * try-with-resources가 컴파일러 뒤에서 하는 일 — 자원을 <strong>LIFO(역순)</strong>로 닫고, 닫는 중 예외를
 * <strong>suppressed</strong>로 보존하는 것 — 을 손으로 구현한다. 커리큘럼 감사가 "자원 수명·suppressed 예외"를
 * 1급 주제로 격상하라 한 자리. ch16 소켓 수명주기가 이 위에 선다.
 */
public final class Resources {

    private Resources() {
    }

    /**
     * 주어진 자원들을 <strong>역순(LIFO)</strong>으로 닫는다. 어떤 자원의 {@code close}가 예외를 던져도 나머지를
     * 마저 닫는다. 던져진 예외 중 <strong>첫 번째</strong>를 대표로 던지고, 이후 예외들은 그 대표에
     * {@link Throwable#addSuppressed} 로 붙인다. {@code null} 원소는 건너뛴다.
     *
     * <p>힌트: 마지막 인덱스부터 0까지 순회. {@code Exception first = null;} — 잡은 예외가 처음이면 {@code first}에
     * 저장, 아니면 {@code first.addSuppressed(e)}. 루프 끝에서 {@code first}가 있으면 던진다.
     *
     * @throws Exception 닫는 중 발생한 첫 예외(나머지는 suppressed로 부착)
     */
    public static void closeAll(AutoCloseable... resources) throws Exception {
        throw new UnsupportedOperationException(
                "TODO: 역순 순회하며 close, 첫 예외 보관 후 이후는 addSuppressed, 끝에 첫 예외 throw (null은 skip)");
    }

    /**
     * 닫힘 상태를 추적하는 테스트용 자원. {@code close}는 멱등(여러 번 호출해도 안전), 닫힌 뒤 {@link #use}는 예외.
     * 옵션으로 {@code close}가 예외를 던지게 해 suppressed 시나리오를 검증한다.
     */
    public static class TrackedResource implements AutoCloseable {

        private final String name;
        private final boolean failOnClose;
        private boolean closed;

        /**
         * @param name        식별자(닫힌 순서 검증용)
         * @param failOnClose {@code true}면 실제로 닫는 호출에서 {@code close}가 예외를 던진다
         */
        public TrackedResource(String name, boolean failOnClose) {
            throw new UnsupportedOperationException("TODO: 필드 초기화, closed=false");
        }

        public String name() {
            throw new UnsupportedOperationException("TODO: name 반환");
        }

        /**
         * @throws IllegalStateException 이미 닫혔으면
         */
        public void use() {
            throw new UnsupportedOperationException("TODO: closed면 IllegalStateException, 아니면 no-op");
        }

        /** 닫혔으면 {@code true}. */
        public boolean isClosed() {
            throw new UnsupportedOperationException("TODO: closed 플래그 반환");
        }

        /**
         * 자원을 닫는다. 이미 닫혀 있으면 아무 일도 하지 않는다(멱등).
         *
         * @throws Exception {@code failOnClose}가 {@code true}이고 이번 호출이 실제로 닫는 경우
         */
        @Override
        public void close() throws Exception {
            throw new UnsupportedOperationException(
                    "TODO: 이미 닫혔으면 return, 아니면 closed=true 후 failOnClose면 Exception throw");
        }
    }
}
