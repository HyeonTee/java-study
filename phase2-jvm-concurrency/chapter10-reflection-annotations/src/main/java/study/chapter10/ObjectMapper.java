package study.chapter10;

import java.util.Map;

/**
 * 임의 객체를 <strong>필드 리플렉션</strong>으로 {@code Map}으로 바꾸고 되돌린다 — Jackson/JPA가
 * 하는 일의 골격.
 *
 * <p>{@link Column}으로 키 이름을 바꾸고, {@link Ignore}로 필드를 제외한다. ch09이 "캡슐화 =
 * 불변식 수호"를 가르쳤다면, 여기서는 리플렉션이 {@code setAccessible}로 그 캡슐화를 <strong>합법적으로
 * 우회</strong>해 {@code private} 필드를 직접 읽고 쓰는 정반대 면을 본다(직렬화·ORM이 생성자
 * 없이 필드를 채우는 메커니즘).
 */
public final class ObjectMapper {

    /**
     * 객체의 (선언된) 인스턴스 필드를 {@code Map<String, Object>}으로 변환한다.
     *
     * <ul>
     *   <li>{@link Ignore} 붙은 필드는 제외한다.</li>
     *   <li>{@link Column}이 있고 값이 비어 있지 않으면 그 값을 키로, 아니면 필드명을 키로 쓴다.</li>
     *   <li>{@code static} 필드는 제외한다(힌트: {@code Modifier.isStatic(field.getModifiers())}).</li>
     *   <li>필드 선언 순서를 유지하라({@code LinkedHashMap}).</li>
     * </ul>
     *
     * <p>힌트: {@code obj.getClass().getDeclaredFields()} 순회 + {@code setAccessible(true)} +
     * {@code field.get(obj)}.
     */
    public Map<String, Object> toMap(Object obj) {
        throw new UnsupportedOperationException(
                "TODO: @Ignore 제외, @Column으로 키 매핑, static 제외하며 필드값을 Map으로 모아라");
    }

    /**
     * {@link #toMap(Object)}의 역연산: {@code type}의 새 인스턴스를 만들고 {@code source}의 값을
     * 해당 필드에 채워 넣는다.
     *
     * <ul>
     *   <li>인스턴스 생성은 인자 없는 생성자를 쓴다({@link Reflect#newInstance} 활용 가능).</li>
     *   <li>키 매핑은 {@code toMap}과 동일한 규칙({@link Column})을 역으로 적용한다.</li>
     *   <li>{@link Ignore} 필드와 {@code static} 필드는 건드리지 않는다.</li>
     * </ul>
     */
    public <T> T fromMap(Class<T> type, Map<String, Object> source) {
        throw new UnsupportedOperationException(
                "TODO: 새 인스턴스를 만들고 source의 값을 (Column 키 매핑을 역적용해) 필드에 주입하라");
    }
}
