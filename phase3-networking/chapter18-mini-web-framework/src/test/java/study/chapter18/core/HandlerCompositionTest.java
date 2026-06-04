package study.chapter18.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Handler — 결과 후처리 합성(andThen), before 가로채기는 못 함")
class HandlerCompositionTest {

    @Test
    void andThen이_결과를_후처리한다() {
        Handler<String, Integer> length = String::length;
        assertEquals(4, length.andThen(n -> n + 1).handle("abc"));
    }

    @Test
    void andThen_2단_체이닝() {
        Handler<String, Integer> length = String::length;
        Handler<String, String> labeled = length.andThen(n -> n + 1).andThen(n -> "n=" + n);
        assertEquals("n=4", labeled.handle("abc"));
    }

    @Test
    void 원본_핸들러는_변하지_않는다() {
        Handler<String, Integer> length = String::length;
        length.andThen(n -> n + 100);
        assertEquals(3, length.handle("abc"), "andThen은 새 핸들러를 만들 뿐 원본을 바꾸지 않는다");
    }

    @Test
    void andThen은_before를_가로채지_못한다() {
        // andThen은 '결과만' 후처리한다 — 내부 핸들러는 원래 요청을 그대로 받는다(Middleware와의 결정적 차이).
        List<String> seen = new ArrayList<>();
        Handler<String, String> spy = req -> {
            seen.add(req);
            return req;
        };
        String out = spy.andThen(String::toUpperCase).handle("hi");
        assertEquals("HI", out, "결과는 후처리됨");
        assertEquals(List.of("hi"), seen, "내부 핸들러는 가공되지 않은 원래 요청을 본다(before 불가)");
    }
}
