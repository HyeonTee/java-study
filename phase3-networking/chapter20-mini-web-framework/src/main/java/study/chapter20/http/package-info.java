/**
 * ★ <strong>봉합선 — {@code core}·{@code routing}·{@code json} + ch18·ch19 의존</strong>. <strong>이 패키지만 HTTP를 안다.</strong>
 *
 * <p>{@link study.chapter20.http.HttpAdapter}가 진짜 {@code study.chapter19.HttpHandler}를 구현해, 제네릭 코어를 ch19
 * 서버 자리에 꽂는다. {@link study.chapter20.http.HttpContext}는 ch18 {@code HttpRequest}를 감싸고,
 * {@link study.chapter20.http.HttpResponses}는 ch19 {@code Responses}를 재사용해 응답을 만든다.
 *
 * <p><strong>패키지 import 그래프가 단원 서사를 증명한다</strong>: {@code study.chapter18}/{@code study.chapter19} import는
 * 오직 이 패키지에만 있다. {@code core}/{@code json}은 ch18·ch19을 모르고, {@code routing}은 {@code core}만 안다. 그래서
 * "프레임워크 코어는 HTTP와 직교, 봉합선만 의존"이 컴파일 단위로 강제된다.
 */
package study.chapter20.http;
