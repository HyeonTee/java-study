package study.chapter18.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 미들웨어 <strong>양파(onion)</strong> — 합성 방향을 sink 로그 시퀀스로 명세한다(이 단원의 무게중심). 100% 인메모리·결정적.
 */
@DisplayName("Middleware — 양파 합성 방향(before 정순 / after 역순)")
class MiddlewareOnionTest {

    /** "handler"를 기록하는 엔드포인트(미들웨어의 before/after 사이에 끼는 중심). */
    private static Handler<String, String> endpoint(List<String> sink) {
        return req -> {
            sink.add("handler");
            return "ok";
        };
    }

    @Test
    void 단일_미들웨어는_before_handler_after() {
        List<String> sink = new ArrayList<>();
        Middleware<String, String> a = Middlewares.logging("A", sink);
        a.apply(endpoint(sink)).handle("req");
        assertEquals(List.of("A-before", "handler", "A-after"), sink);
    }

    @Test
    void andThen은_this가_바깥_양파() {
        // A.andThen(B): A가 바깥 → before는 A,B / after는 B,A
        List<String> sink = new ArrayList<>();
        Middleware<String, String> a = Middlewares.logging("A", sink);
        Middleware<String, String> b = Middlewares.logging("B", sink);
        a.andThen(b).apply(endpoint(sink)).handle("req");
        assertEquals(List.of("A-before", "B-before", "handler", "B-after", "A-after"), sink);
    }

    @Test
    void chain은_첫_원소가_가장_바깥() {
        List<String> sink = new ArrayList<>();
        Middleware<String, String> a = Middlewares.logging("A", sink);
        Middleware<String, String> b = Middlewares.logging("B", sink);
        Middleware<String, String> c = Middlewares.logging("C", sink);
        Middleware.chain(List.of(a, b, c)).apply(endpoint(sink)).handle("req");
        assertEquals(
                List.of("A-before", "B-before", "C-before", "handler", "C-after", "B-after", "A-after"),
                sink);
    }

    @Test
    void 빈_체인은_항등() {
        List<String> sink = new ArrayList<>();
        Middleware.<String, String>chain(List.of()).apply(endpoint(sink)).handle("req");
        assertEquals(List.of("handler"), sink, "빈 체인은 엔드포인트만 실행");
    }

    @Test
    void identity는_아무것도_감싸지_않는다() {
        List<String> sink = new ArrayList<>();
        Middleware.<String, String>identity().apply(endpoint(sink)).handle("req");
        assertEquals(List.of("handler"), sink);
    }
}
