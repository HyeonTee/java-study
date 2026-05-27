package study.chapter05;

/**
 * 도형을 나타내는 sealed 인터페이스.
 *
 * <p>허용된 하위 타입: {@link Circle}, {@link Rectangle}, {@link Triangle}.
 * switch 패턴 매칭으로 모든 케이스를 다룰 수 있다.
 */
public sealed interface Shape permits Circle, Rectangle, Triangle {}
