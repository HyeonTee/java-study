package study.chapter06;

/**
 * JVM 메모리 영역 퀴즈.
 *
 * <p>각 메서드는 특정 데이터가 JVM의 어느 메모리 영역에 저장되는지를 묻는다.
 * 올바른 {@link MemoryArea} 값 또는 설명 문자열을 반환하도록 구현하라.
 */
public class MemoryAreaQuiz {

    /**
     * 메서드 안에서 선언한 int, double 같은 지역 프리미티브 변수는 어디에 저장되는가?
     *
     * <p>힌트: 프리미티브 타입은 크기가 고정이고, 메서드 호출과 생명주기가 같다.
     *
     * @return 해당 메모리 영역
     */
    public MemoryArea whereIsLocalPrimitive() {
        throw new UnsupportedOperationException(
                "TODO: 지역 프리미티브 변수가 저장되는 메모리 영역을 반환하라");
    }

    /**
     * {@code new} 키워드로 생성한 객체 인스턴스는 어디에 저장되는가?
     *
     * <p>힌트: 객체는 크기가 동적이고, 여러 메서드에서 참조될 수 있다.
     *
     * @return 해당 메모리 영역
     */
    public MemoryArea whereIsObjectInstance() {
        throw new UnsupportedOperationException(
                "TODO: 객체 인스턴스가 저장되는 메모리 영역을 반환하라");
    }

    /**
     * {@code static} 필드는 어디에 저장되는가?
     *
     * <p>힌트: static 필드는 클래스 로딩 시 한 번 할당되며, 클래스 메타데이터와 함께 관리된다.
     * (Java 8 이후 물리적으로는 힙에 있지만, 논리적으로는 클래스 메타데이터 영역에 속한다.)
     *
     * @return 해당 메모리 영역
     */
    public MemoryArea whereIsStaticField() {
        throw new UnsupportedOperationException(
                "TODO: static 필드가 저장되는 메모리 영역을 반환하라");
    }

    /**
     * 메서드의 바이트코드 정보(메서드 시그니처, 코드 등)는 어디에 저장되는가?
     *
     * <p>힌트: 클래스가 로딩될 때 메서드 정보는 클래스 메타데이터의 일부로 저장된다.
     *
     * @return 해당 메모리 영역
     */
    public MemoryArea whereIsMethodInfo() {
        throw new UnsupportedOperationException(
                "TODO: 메서드 바이트코드 정보가 저장되는 메모리 영역을 반환하라");
    }

    /**
     * 메서드가 리턴하면 해당 메서드의 스택 프레임에는 어떤 일이 발생하는가?
     *
     * <p>힌트: 스택 프레임은 메서드 호출 시 생성되고, 리턴 시 소멸한다.
     *
     * @return 동작을 설명하는 문자열: "스택 프레임이 제거되고 지역 변수가 소멸한다"
     */
    public String whatHappensWhenMethodReturns() {
        throw new UnsupportedOperationException(
                "TODO: 메서드 리턴 시 스택 프레임에 일어나는 일을 정확한 문자열로 반환하라");
    }
}
