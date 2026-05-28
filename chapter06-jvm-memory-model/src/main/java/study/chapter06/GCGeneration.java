package study.chapter06;

/**
 * GC 세대(generation)를 나타내는 열거형.
 *
 * <ul>
 *   <li>{@link #YOUNG} - 새로 생성된 객체가 할당되는 Young Generation (Eden + Survivor)</li>
 *   <li>{@link #OLD} - 여러 번의 Minor GC를 살아남은 객체가 승격되는 Old Generation</li>
 * </ul>
 */
public enum GCGeneration {
    YOUNG,
    OLD
}
