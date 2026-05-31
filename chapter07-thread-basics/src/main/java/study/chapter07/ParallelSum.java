package study.chapter07;

/**
 * 배열을 여러 스레드로 나눠 병렬 합산한다 — {@code Thread} 생성과 {@code join()}의 입문.
 *
 * <p>이 단원의 첫 문제이자 가장 쉬운 문제다. <strong>공유 가변 상태가 없는</strong> 병렬화라서
 * 동기화가 전혀 필요 없다 — "공유하지 않으면 안전하다(no sharing, no problem)". 각 스레드는
 * 자기 구간만 더해 <strong>자기 칸</strong>에 결과를 쓰고, 메인 스레드는 모든 스레드를
 * {@code join()}으로 기다린 뒤 부분합을 합친다.
 *
 * <p>여기서 {@code join()}이 하는 일은 두 가지다:
 * <ul>
 *   <li>스레드가 끝날 때까지 <strong>기다린다</strong>.</li>
 *   <li>그 스레드가 쓴 값이 메인 스레드에 <strong>보이게</strong> 한다
 *       (ch06 README의 happens-before 규칙 4: run() 종료 → join() 리턴). 그래서 부분합을
 *       읽을 때 별도의 {@code volatile}/{@code synchronized} 없이도 최신값이 보장된다.</li>
 * </ul>
 *
 * <p>다음 문제부터는 <strong>공유 상태를 함께 갱신</strong>하기 때문에 이 무료 점심이 사라진다
 * — 그때 {@code synchronized}/{@code Atomic}이 필요해진다 (ch07의 나머지 문제들).
 */
public final class ParallelSum {

    private ParallelSum() {
    }

    /**
     * {@code numbers}를 {@code threadCount}개의 (거의) 균등한 구간으로 나눠, 각 구간을 별도
     * 스레드가 합산하게 한 뒤, 모든 부분합을 더한 전체 합을 반환한다.
     *
     * <ul>
     *   <li>{@code numbers}가 비어 있으면 0.</li>
     *   <li>{@code threadCount}가 {@code numbers.length}보다 커도 된다(빈 구간을 맡는 스레드가
     *       생겨도 무방하다).</li>
     *   <li>반환값은 {@code long} — {@code int} 합산의 오버플로를 피하기 위함이다.</li>
     * </ul>
     *
     * <p>힌트:
     * <ol>
     *   <li>{@code long[] partial = new long[threadCount]}를 두고, i번째 스레드가
     *       {@code partial[i]}에만 쓰게 하면 스레드 간 쓰기 충돌이 없다(서로 다른 칸).</li>
     *   <li>모든 스레드를 먼저 {@code start()}한 뒤, 그 다음에 모두 {@code join()}하라
     *       (start 따로 / join 따로). 하나 start하고 바로 join하면 순차 실행이 된다.</li>
     *   <li>{@code join()}은 {@code InterruptedException}을 던진다 — 메서드 시그니처가 검사 예외를
     *       선언하지 않으므로, 내부에서 잡아 {@code RuntimeException}으로 감싸고
     *       {@code Thread.currentThread().interrupt()}로 인터럽트 상태를 복원하라
     *       (ch03 wrap-and-rethrow 복습).</li>
     * </ol>
     *
     * @throws IllegalArgumentException {@code threadCount < 1}
     */
    public static long sum(int[] numbers, int threadCount) {
        throw new UnsupportedOperationException(
                "TODO: numbers를 threadCount개 구간으로 나눠 각 스레드가 부분합을 구하고, "
                        + "모두 join한 뒤 부분합을 합쳐 반환하라");
    }
}
