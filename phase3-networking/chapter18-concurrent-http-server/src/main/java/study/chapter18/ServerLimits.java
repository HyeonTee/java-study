package study.chapter18;

/**
 * 서버의 DoS 방어 한계치 묶음. <strong>완성 제공</strong>(채우지 않는다) — ch17 record 완성 제공 철학 계승.
 *
 * <p><strong>{@code maxBodyBytes}를 반드시 포함</strong>한다 — 라인·헤더만 막고 바디를 무제한 두면 거대한
 * {@code Content-Length} POST 한 방에 메모리가 터지는 반쪽 방어가 된다.
 *
 * @param maxRequestLineBytes    요청라인 한 줄 최대 바이트(초과 시 431)
 * @param maxHeaderLineBytes     헤더 한 줄 최대 바이트(초과 시 431)
 * @param maxHeaderCount         헤더 줄 최대 개수(초과 시 431)
 * @param maxBodyBytes           바디 최대 바이트(Content-Length 초과 시 413)
 * @param maxRequestsPerConnection 한 연결(keep-alive)에서 처리할 최대 요청 수(도달 시 연결 종료 — 자원 회수)
 * @param soTimeoutMillis        accept된 소켓의 읽기 타임아웃(idle/slowloris 가드, ch16 회수)
 */
public record ServerLimits(
        int maxRequestLineBytes,
        int maxHeaderLineBytes,
        int maxHeaderCount,
        int maxBodyBytes,
        int maxRequestsPerConnection,
        int soTimeoutMillis) {

    public ServerLimits {
        if (maxRequestLineBytes <= 0 || maxHeaderLineBytes <= 0 || maxHeaderCount <= 0
                || maxBodyBytes <= 0 || maxRequestsPerConnection <= 0 || soTimeoutMillis <= 0) {
            throw new IllegalArgumentException("모든 한계치는 양수여야 한다: " + this);
        }
    }

    /** 테스트·데모용 합리적 기본값. */
    public static ServerLimits defaults() {
        return new ServerLimits(8192, 8192, 100, 1 << 20, 100, 5000);
    }
}
