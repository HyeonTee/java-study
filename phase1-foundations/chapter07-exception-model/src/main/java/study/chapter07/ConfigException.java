package study.chapter07;

/**
 * 설정(Config) 처리에서 발생하는 예외들의 봉인된(sealed) 공통 상위 타입 — 지원 코드.
 *
 * <p>비검사 예외({@link RuntimeException})로 둔 이유: 설정 오류는 호출자가 매번 catch로 복구하기보다
 * "프로그램 설정이 잘못됐다"는 신호에 가깝다. {@code sealed}로 허용 하위 타입을 두 개
 * ({@link MissingKeyException}, {@link MalformedValueException})로 못박아, switch 패턴 매칭 시
 * 컴파일러가 모든 경우를 검증할 수 있게 한다.
 */
public sealed abstract class ConfigException extends RuntimeException
        permits MissingKeyException, MalformedValueException {

    protected ConfigException(String message) {
        super(message);
    }

    protected ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
