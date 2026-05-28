package study.chapter03;

/**
 * 검사 예외(checked exception)를 던질 수 있는 함수형 인터페이스.
 *
 * <p>표준 {@link java.util.function.Function}의 {@code apply}는 검사 예외를 던질 수 없다.
 * 그래서 {@code Files.readString} 같은 검사 예외를 던지는 메서드를 람다/메서드 참조로
 * 바로 넘기면 컴파일 에러가 난다:
 *
 * <pre>{@code
 * // 컴파일 에러 — Function.apply는 IOException을 던질 수 없음
 * Function<Path, String> f = Files::readString;
 * }</pre>
 *
 * <p>이 인터페이스는 {@code throws Exception}을 허용해 그런 메서드를 감쌀 수 있게 한다.
 * 다만 결국 표준 {@link java.util.function.Function}이 필요한 자리(Stream.map 등)에 넘기려면
 * 검사 예외를 비검사 예외로 바꿔주는 어댑터가 필요하다 — 그 변환을
 * {@link ThrowingFunctionPractice}에서 직접 구현한다.
 *
 * @param <T> 입력 타입
 * @param <R> 출력 타입
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> {

    R apply(T t) throws Exception;
}
