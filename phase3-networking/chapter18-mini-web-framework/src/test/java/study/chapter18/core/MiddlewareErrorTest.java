package study.chapter18.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import study.chapter18.routing.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Middlewares — errorBoundary(예외 경계) · counting(결정적 계측)")
class MiddlewareErrorTest {

    @Test
    void errorBoundary가_엔드포인트_예외를_폴백으로() {
        Handler<String, String> boom = req -> {
            throw new IllegalStateException("kaboom");
        };
        Middleware<String, String> guard = Middlewares.errorBoundary(e -> "fallback:" + e.getMessage());
        assertEquals("fallback:kaboom", guard.apply(boom).handle("r"));
    }

    @Test
    void 정상흐름에선_onError_미호출() {
        List<String> errs = new ArrayList<>();
        Middleware<String, String> guard = Middlewares.errorBoundary(e -> {
            errs.add("err");
            return "fb";
        });
        assertEquals("ok", guard.apply(req -> "ok").handle("r"));
        assertTrue(errs.isEmpty(), "예외가 없으면 onError는 호출되지 않는다");
    }

    @Test
    void onError가_라우팅예외를_다시_던질_수_있다() {
        // errorBoundary가 NotFound까지 삼키면 404가 사라진다 — onError가 타입을 구분해 재던지는 경계 검증.
        Middleware<String, String> guard = Middlewares.errorBoundary(e -> {
            if (e instanceof NotFoundException) {
                throw e;
            }
            return "fb";
        });
        Handler<String, String> notFound = req -> {
            throw new NotFoundException("no route");
        };
        assertThrows(NotFoundException.class, () -> guard.apply(notFound).handle("r"));
    }

    @Test
    void counting은_호출_횟수만_센다() {
        AtomicInteger calls = new AtomicInteger();
        Handler<String, String> counted = Middlewares.<String, String>counting(calls).apply(req -> "ok");
        counted.handle("a");
        counted.handle("b");
        assertEquals(2, calls.get());
    }
}
