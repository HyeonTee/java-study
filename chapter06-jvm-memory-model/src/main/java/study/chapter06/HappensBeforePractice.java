package study.chapter06;

/**
 * Java Memory Model의 happens-before 관계 연습.
 *
 * <p>happens-before 관계는 한 스레드의 작업 결과가 다른 스레드에
 * 반드시 보이도록 보장하는 JMM(Java Memory Model)의 핵심 규칙이다.
 *
 * <p>각 메서드는 특정 동기화 메커니즘이 happens-before 관계를 형성하는지를 묻는다.
 */
public class HappensBeforePractice {

    /**
     * volatile 변수에 대한 쓰기가 그 변수의 후속 읽기에 대해 happens-before 관계를 형성하는가?
     *
     * <p>힌트: volatile 쓰기는 메인 메모리에 즉시 반영되고,
     * volatile 읽기는 메인 메모리에서 최신 값을 가져온다.
     *
     * @return happens-before 관계가 성립하면 true
     */
    public boolean doesVolatileWriteHappenBefore() {
        throw new UnsupportedOperationException(
                "TODO: volatile 쓰기의 happens-before 보장 여부를 반환하라");
    }

    /**
     * synchronized 블록의 unlock이 같은 모니터의 후속 lock에 대해 happens-before 관계를 형성하는가?
     *
     * <p>힌트: synchronized 블록을 나갈 때(unlock) 변경 사항이 플러시되고,
     * 진입할 때(lock) 최신 값을 읽어온다.
     *
     * @return happens-before 관계가 성립하면 true
     */
    public boolean doesSynchronizedBlockHappenBefore() {
        throw new UnsupportedOperationException(
                "TODO: synchronized의 happens-before 보장 여부를 반환하라");
    }

    /**
     * Thread.start() 호출이 해당 스레드의 run() 메서드 시작에 대해 happens-before 관계를 형성하는가?
     *
     * <p>힌트: start()를 호출하기 전에 설정한 값이
     * 새 스레드의 run() 안에서 보이도록 보장되는지 생각해 보라.
     *
     * @return happens-before 관계가 성립하면 true
     */
    public boolean doesThreadStartHappenBefore() {
        throw new UnsupportedOperationException(
                "TODO: Thread.start()의 happens-before 보장 여부를 반환하라");
    }

    /**
     * Thread.join()이 반환된 후, 해당 스레드에서 수행한 모든 작업에 대해 happens-before 관계가 성립하는가?
     *
     * <p>힌트: join()이 반환되면, 종료된 스레드에서 수행한 모든 쓰기가
     * join()을 호출한 스레드에서 보이도록 보장되는지 생각해 보라.
     *
     * @return happens-before 관계가 성립하면 true
     */
    public boolean doesThreadJoinHappenBefore() {
        throw new UnsupportedOperationException(
                "TODO: Thread.join()의 happens-before 보장 여부를 반환하라");
    }
}
