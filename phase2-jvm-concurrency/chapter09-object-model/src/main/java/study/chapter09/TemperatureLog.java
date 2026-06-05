package study.chapter09;

import java.util.List;

/**
 * 온도 측정 기록 — 캡슐화와 방어적 복사(defensive copy)로 불변식을 수호하는 불변(immutable) 객체.
 *
 * <p>클래스가 가변 컬렉션을 필드로 들고 있을 때, 경계에서 <strong>복사하지 않으면</strong>
 * 캡슐화가 뚫린다:
 * <ul>
 *   <li><strong>생성자 입력</strong>을 그대로 들면, 호출자가 넘긴 리스트를 나중에 바꿔 내부를
 *       오염시킬 수 있다.</li>
 *   <li><strong>getter가 내부 참조</strong>를 그대로 반환하면, 외부에서 내부를 직접 변경할 수 있다.</li>
 * </ul>
 * "측정값은 한 번 기록되면 바뀌지 않는다"는 불변식은 <strong>경계에서 방어적으로 복사</strong>해야만
 * 지켜진다. 캡슐화란 결국 "이 클래스가 자기 불변식의 유일한 수호자"라는 뜻이다.
 *
 * <p>이 클래스는 <strong>불변</strong>이다 — 측정값 추가는 상태를 바꾸는 대신 {@link #withReading(double)}로
 * <strong>새 인스턴스</strong>를 만든다.
 */
public final class TemperatureLog {

    /**
     * {@code readings}를 <strong>방어적으로 복사</strong>해 보관한다.
     *
     * <p>힌트: 인자로 받은 리스트를 그대로 필드에 대입하면, 호출자가 그 리스트를 나중에 변경했을 때
     * 내부도 함께 변한다. 새 리스트로 복사해 들어라.
     */
    public TemperatureLog(List<Double> readings) {
        throw new UnsupportedOperationException(
                "TODO: readings를 방어적으로 복사해 보관하라 (외부 변경이 새지 않게)");
    }

    /**
     * {@code reading}을 추가한 <strong>새</strong> {@code TemperatureLog}를 반환한다.
     * {@code this}는 변하지 않는다(불변 객체).
     */
    public TemperatureLog withReading(double reading) {
        throw new UnsupportedOperationException(
                "TODO: 기존 측정값 + reading을 가진 새 인스턴스를 반환하라 (this는 불변)");
    }

    /**
     * 측정값들을 반환한다. 반환된 리스트를 외부에서 변경해도 이 객체 내부에는 영향이 없어야 한다.
     *
     * <p>힌트: 내부 리스트를 그대로 반환하지 마라. 복사본 또는 수정 불가
     * ({@code List.copyOf} / {@code Collections.unmodifiableList}) 뷰를 반환하라.
     */
    public List<Double> readings() {
        throw new UnsupportedOperationException(
                "TODO: 내부를 노출하지 않는(수정 불가 또는 복사본) 리스트를 반환하라");
    }

    /** 기록된 측정값의 개수. */
    public int count() {
        throw new UnsupportedOperationException("TODO: 측정값 개수 반환");
    }

    /**
     * 최고 온도.
     *
     * @throws java.util.NoSuchElementException 측정값이 하나도 없을 때
     */
    public double max() {
        throw new UnsupportedOperationException("TODO: 최고 온도 반환 (비었으면 NoSuchElementException)");
    }
}
