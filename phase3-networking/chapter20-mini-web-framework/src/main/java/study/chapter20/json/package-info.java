/**
 * <strong>JSON 직렬화 — 독립({@code core}와도 무관)</strong>. sealed {@link study.chapter20.json.JsonValue} 모델 +
 * {@link study.chapter20.json.JsonWriter}(switch 망라 직렬화) + {@link study.chapter20.json.JsonReflect}(record 접근자
 * 리플렉션, ch10 회수).
 *
 * <p>직렬화기'만'(파서는 경계 밖). 이 패키지는 프레임워크의 다른 부분({@code core}/{@code routing})도, HTTP도 모른다 —
 * 순수 "객체 → JSON 문자열" 변환기다. {@code http} 패키지가 응답 바디를 만들 때 이걸 쓴다.
 */
package study.chapter20.json;
