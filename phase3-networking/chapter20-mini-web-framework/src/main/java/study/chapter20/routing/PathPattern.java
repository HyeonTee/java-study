package study.chapter20.routing;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 경로 패턴 하나({@code /users/{id}})를 컴파일하고 실제 path와 매칭해 <strong>경로변수를 추출</strong>한다 —
 * 라우팅 정교함을 {@link Router}에서 떼어낸 격리된 핵(단위 테스트 가능). ch02 맵 회수(추출된 변수 맵).
 *
 * <p>여기만 정교하게, 나머지는 단순하게 — 분량 통제의 핵심 지렛대다. <strong>와일드카드({@code /files/**})·정규식
 * 제약은 경계 밖</strong>(이론 박스). 세그먼트 기반 {@code {var}} 1단계까지만.
 */
public final class PathPattern {

    private PathPattern() {
        // 컴파일된 세그먼트를 담을 필드는 구현하며 추가하라(예: List<Segment>).
    }

    /**
     * 패턴 문자열을 세그먼트 리스트로 컴파일한다. {@code '/'}로 쪼개고, {@code {name}} 형태 세그먼트는 <strong>변수</strong>
     * (이름 추출), 그 외는 <strong>리터럴</strong>로 분류해 보관한다.
     *
     * <p><strong>함정</strong>: 선행/후행 슬래시를 일관되게 정규화하라({@code /users/{id}}와 {@code /users/{id}/}가 같게,
     * {@code /users//x}의 빈 세그먼트 처리). 루트 {@code "/"}·빈 패턴도 안전하게.
     *
     * <p>힌트(정답 코드 없이): 세그먼트마다 (리터럴 문자열 or 변수명)을 표현하는 내부 구조를 필드에 저장하라.
     */
    public static PathPattern compile(String pattern) {
        throw new UnsupportedOperationException(
                "TODO: '/'로 split, '{name}'은 변수·그 외는 리터럴로 분류해 보관. 선행/후행 슬래시 정규화");
    }

    /**
     * 실제 {@code path}를 이 패턴과 <strong>세그먼트 단위</strong>로 비교한다. 매칭되면 {@code 변수명 → 값} 맵
     * ({@code LinkedHashMap}, 순서 보존)을, 아니면 {@link Optional#empty()}를 반환한다.
     *
     * <p><strong>함정</strong>: 세그먼트 개수가 다르면 즉시 불일치(빠른 탈출). 리터럴 세그먼트는 정확히 같아야 하고, 변수
     * 세그먼트는 무엇이든 매칭하며 값을 추출한다. (정적 {@code /users/me} vs 변수 {@code /users/{id}} 우선순위는 {@link Router}가
     * 결정 — 여기선 단일 패턴 매칭만.)
     *
     * <p>힌트: path도 같은 방식으로 세그먼트화해 길이부터 비교, 그다음 세그먼트별로 리터럴/변수 판정.
     */
    public Optional<Map<String, String>> match(String path) {
        throw new UnsupportedOperationException(
                "TODO: path를 세그먼트화→개수 비교→리터럴 일치/변수 추출. 매칭이면 LinkedHashMap(순서보존), 아니면 empty");
    }

    /**
     * 이 패턴이 선언한 변수 이름들(등장 순서). 예: {@code /users/{uid}/posts/{pid}} → {@code [uid, pid]}.
     *
     * <p>힌트: 컴파일된 세그먼트 중 변수 세그먼트의 이름만 순서대로 모아 반환.
     */
    public List<String> variableNames() {
        throw new UnsupportedOperationException("TODO: 컴파일된 세그먼트 중 변수명만 등장 순서대로 List로 반환");
    }
}
