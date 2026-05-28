package study.chapter06;

/**
 * JVM 메모리 영역을 나타내는 열거형.
 *
 * <ul>
 *   <li>{@link #STACK} - 스레드별 스택 영역 (지역 변수, 메서드 호출 프레임)</li>
 *   <li>{@link #HEAP} - 객체 인스턴스가 생성되는 공유 영역</li>
 *   <li>{@link #METHOD_AREA} - 클래스 메타데이터, static 필드, 메서드 정보가 저장되는 영역</li>
 * </ul>
 */
public enum MemoryArea {
    STACK,
    HEAP,
    METHOD_AREA
}
