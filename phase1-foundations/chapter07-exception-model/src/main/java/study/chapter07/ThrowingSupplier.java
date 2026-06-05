package study.chapter07;

/**
 * 검사 예외(checked exception)를 던질 수 있는 {@link java.util.function.Supplier} 변형 — 지원 코드.
 *
 * <p>JDK의 {@code Supplier.get()}은 검사 예외를 던질 수 없어 람다 안에서 try-catch를 강제당한다.
 * 이 인터페이스는 {@code throws Exception}을 허용해, {@link Exceptions#uncheck}가 그 경계를
 * "검사 예외 → 비검사 예외"로 옮기는 어댑터를 만들 수 있게 한다.
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {

    T get() throws Exception;
}
