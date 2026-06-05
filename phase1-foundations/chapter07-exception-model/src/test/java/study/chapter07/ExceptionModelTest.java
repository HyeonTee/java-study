package study.chapter07;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("chapter07 — 예외 모델 / 자원 정리")
class ExceptionModelTest {

    @Nested
    @DisplayName("Config — require / getInt 와 도메인 예외")
    class ConfigTest {

        @Test
        @DisplayName("require: 존재하는 키는 값을 반환한다")
        void require_존재하는_키는_값_반환() {
            Config config = new Config(Map.of("host", "localhost"));
            assertEquals("localhost", config.require("host"));
        }

        @Test
        @DisplayName("require: 없는 키는 MissingKeyException 을 던지고 key() 가 일치한다")
        void require_없는_키는_MissingKeyException() {
            Config config = new Config(Map.of("host", "localhost"));
            MissingKeyException ex =
                    assertThrows(MissingKeyException.class, () -> config.require("port"));
            assertEquals("port", ex.key());
        }

        @Test
        @DisplayName("getInt: 숫자 문자열은 정수로 변환한다")
        void getInt_숫자_문자열은_정수_반환() {
            Config config = new Config(Map.of("port", "8080"));
            assertEquals(8080, config.getInt("port"));
        }

        @Test
        @DisplayName("getInt: 숫자가 아닌 값은 MalformedValueException — key/rawValue/cause 검증")
        void getInt_숫자_아니면_MalformedValueException() {
            Config config = new Config(Map.of("port", "abc"));
            MalformedValueException ex =
                    assertThrows(MalformedValueException.class, () -> config.getInt("port"));
            assertEquals("port", ex.key());
            assertEquals("abc", ex.rawValue());
            assertInstanceOf(NumberFormatException.class, ex.getCause());
        }

        @Test
        @DisplayName("getInt: 없는 키는 MissingKeyException 을 던진다")
        void getInt_없는_키는_MissingKeyException() {
            Config config = new Config(Map.of("host", "localhost"));
            MissingKeyException ex =
                    assertThrows(MissingKeyException.class, () -> config.getInt("port"));
            assertEquals("port", ex.key());
        }

        @Test
        @DisplayName("도메인 예외는 ConfigException 의 하위 타입이다")
        void 도메인_예외는_ConfigException_하위타입() {
            Config config = new Config(Map.of("port", "abc"));
            ConfigException missing =
                    assertThrows(ConfigException.class, () -> config.require("nope"));
            assertInstanceOf(MissingKeyException.class, missing);
            ConfigException malformed =
                    assertThrows(ConfigException.class, () -> config.getInt("port"));
            assertInstanceOf(MalformedValueException.class, malformed);
        }
    }

    @Nested
    @DisplayName("Exceptions — rootCause / uncheck / unchecked")
    class ExceptionsTest {

        @Test
        @DisplayName("rootCause: 가장 깊은 원인 예외를 반환한다")
        void rootCause_가장_깊은_원인_반환() {
            IOException root = new IOException("root");
            Throwable top = new RuntimeException(new IllegalStateException(root));
            assertSame(root, Exceptions.rootCause(top));
        }

        @Test
        @DisplayName("rootCause: cause 가 없으면 자기 자신을 반환한다")
        void rootCause_cause_없으면_자기자신() {
            RuntimeException only = new RuntimeException("only");
            assertSame(only, Exceptions.rootCause(only));
        }

        @Test
        @DisplayName("uncheck: 정상 supplier 는 값을 그대로 반환한다")
        void uncheck_정상_supplier는_값_반환() {
            assertEquals(42, Exceptions.uncheck(() -> 42));
        }

        @Test
        @DisplayName("uncheck: 검사 예외는 RuntimeException 으로 감싸고 cause 를 보존한다")
        void uncheck_검사예외는_RuntimeException으로_래핑() {
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> Exceptions.uncheck(() -> {
                        throw new IOException("io");
                    }));
            assertInstanceOf(IOException.class, ex.getCause());
            assertEquals("io", ex.getCause().getMessage());
        }

        @Test
        @DisplayName("uncheck: RuntimeException 은 래핑 없이 그대로 전파한다")
        void uncheck_RuntimeException은_그대로_전파() {
            IllegalArgumentException original = new IllegalArgumentException("boom");
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> Exceptions.uncheck(() -> {
                        throw original;
                    }));
            assertSame(original, thrown);
            assertEquals("boom", thrown.getMessage());
        }

        @Test
        @DisplayName("unchecked: 정상 입력에 대해 값을 계산하는 Function 을 만든다")
        void unchecked_정상_입력은_값_계산() {
            Function<String, Integer> parse =
                    Exceptions.unchecked(s -> Integer.parseInt(s));
            assertEquals(123, parse.apply("123"));
        }

        @Test
        @DisplayName("unchecked: 검사 예외를 던지는 함수는 RuntimeException 으로 바뀐다")
        void unchecked_검사예외는_RuntimeException으로_래핑() {
            Function<String, String> readFails =
                    Exceptions.unchecked(s -> {
                        throw new IOException(s);
                    });
            RuntimeException ex =
                    assertThrows(RuntimeException.class, () -> readFails.apply("io"));
            assertInstanceOf(IOException.class, ex.getCause());
            assertEquals("io", ex.getCause().getMessage());
        }
    }

    @Nested
    @DisplayName("Resource — try-with-resources 순서 / suppressed")
    class ResourceTest {

        @Test
        @DisplayName("openTwoAndUse: open 순서대로, close 는 역순으로 기록된다")
        void openTwoAndUse_close는_역순() {
            List<String> log = new ArrayList<>();
            ResourceScenarios.openTwoAndUse(log);
            assertEquals(
                    List.of("open:A", "open:B", "use:A", "use:B", "close:B", "close:A"),
                    log);
        }

        @Test
        @DisplayName("captureSuppressed: primary 예외에 close 실패가 suppressed 로 붙는다")
        void captureSuppressed_close실패는_suppressed() {
            List<String> log = new ArrayList<>();
            RuntimeException ex = ResourceScenarios.captureSuppressed(log);
            assertEquals("primary", ex.getMessage());
            assertEquals(1, ex.getSuppressed().length);
            assertInstanceOf(IllegalStateException.class, ex.getSuppressed()[0]);
        }

        @Test
        @DisplayName("TrackedResource: failOnClose 면 close 가 IllegalStateException 을 던지되 로그는 먼저 남긴다")
        void trackedResource_failOnClose면_close가_예외() {
            List<String> log = new ArrayList<>();
            TrackedResource resource = new TrackedResource("X", log, true);
            assertThrows(IllegalStateException.class, resource::close);
            assertTrue(log.contains("close:X"));
        }

        @Test
        @DisplayName("TrackedResource: 정상 흐름은 open/use/close 를 순서대로 기록한다")
        void trackedResource_정상_흐름() {
            List<String> log = new ArrayList<>();
            try (TrackedResource resource = new TrackedResource("X", log, false)) {
                resource.use();
            }
            assertEquals(List.of("open:X", "use:X", "close:X"), log);
        }
    }
}
