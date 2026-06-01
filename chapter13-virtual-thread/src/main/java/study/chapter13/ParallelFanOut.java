package study.chapter13;

import java.util.List;
import java.util.function.Function;

/**
 * 가상 스레드 fan-out — 여러 입력을 동시에 변환하되 <strong>입력 순서를 보존</strong>해 결과를 모은다.
 *
 * <p>{@link MyTaskScope}를 재사용한다: 입력마다 {@code fork}하고 {@code join} 후 입력 순서대로 결과를
 * 회수한다. 완료 순서는 제각각이어도(비결정적) 반환 리스트 순서는 입력과 1:1로 같아야 한다(결정적). 풀 없이
 * 작업마다 가상 스레드로 I/O 바운드 작업(여러 URL 동시 fetch 등)을 처리하는 전형적 패턴이다.
 */
public final class ParallelFanOut {

    private ParallelFanOut() {
    }

    /**
     * {@code inputs}의 각 원소에 {@code mapper}를 <strong>동시에</strong> 적용하고, 결과를 <strong>입력
     * 순서대로</strong> 담은 리스트로 반환한다(= 병렬판 {@code stream().map(...).toList()}).
     *
     * <p>하나라도 {@code mapper}가 예외를 던지면 나머지를 취소하고 그 예외를 {@link RuntimeException}으로
     * 감싸 던진다(부분 결과를 반환하지 않는다 — all-or-nothing).
     *
     * <p>힌트: {@link MyTaskScope}로 입력마다 {@code fork(() -> mapper.apply(원소))}한 Subtask들을 순서대로
     * 모으고, {@code join()} + {@code throwIfFailed()} 후 인덱스 순서대로 {@code get()}해 수집하라.
     * {@code throwIfFailed}의 {@code ExecutionException}은 원인을 꺼내 {@code RuntimeException}으로 감싼다.
     *
     * @return 입력과 같은 크기·같은 순서의 결과 리스트
     * @throws NullPointerException {@code inputs}/{@code mapper}가 null이면
     */
    public static <T, R> List<R> parallelMap(List<T> inputs, Function<? super T, ? extends R> mapper) {
        throw new UnsupportedOperationException(
                "TODO: MyTaskScope로 입력마다 fork → join → throwIfFailed → 인덱스 순서대로 get() 수집 (all-or-nothing)");
    }
}
