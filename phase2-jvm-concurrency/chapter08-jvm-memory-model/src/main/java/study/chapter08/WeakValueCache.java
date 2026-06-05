package study.chapter08;

import java.util.Optional;

/**
 * 값을 {@link java.lang.ref.WeakReference}로 보관하는 캐시.
 *
 * <p>일반 {@code Map<K, V>}는 값을 <strong>강한 참조(strong reference)</strong>로 들고 있어,
 * 캐시에 남아 있는 한 GC가 값을 절대 수거하지 못한다 — 흔한 메모리 누수 원인이다.
 * 이 캐시는 값을 <strong>약한 참조</strong>로 들고 있어, 다른 곳에서 그 값을 더 이상
 * 강하게 참조하지 않으면 GC가 값을 회수하고 캐시에서도 사라진다.
 *
 * <p>핵심 개념:
 * <ul>
 *   <li>약한 참조는 referent의 생존을 보장하지 않는다 — {@code WeakReference.get()}이
 *       어느 순간 {@code null}을 반환할 수 있다 (GC가 수거한 경우).</li>
 *   <li>강한 참조가 살아 있는 동안에는 약한 참조도 값을 정상 반환한다.</li>
 * </ul>
 *
 * <p>구현 힌트: 내부에 {@code Map<K, WeakReference<V>>}를 두고, 조회 시
 * {@code ref.get()}이 null이면 "없음"으로 취급한다. (null 키/값은 다루지 않는다고 가정.)
 *
 * @param <K> 키 타입
 * @param <V> 값 타입
 */
public class WeakValueCache<K, V> {

    /**
     * key에 value를 저장한다. value는 약한 참조로 보관한다.
     */
    public void put(K key, V value) {
        throw new UnsupportedOperationException(
                "TODO: value를 WeakReference로 감싸 내부 맵에 저장하라");
    }

    /**
     * key에 해당하는 값을 반환한다. 저장된 적이 없거나 이미 GC로 수거되었으면 빈 Optional.
     *
     * <p>힌트: 맵에서 WeakReference를 꺼내 {@code get()}한 결과가 null이면 빈 Optional.
     */
    public Optional<V> get(K key) {
        throw new UnsupportedOperationException(
                "TODO: WeakReference.get()이 살아 있으면 그 값을, 아니면 Optional.empty()를 반환하라");
    }

    /**
     * 아직 수거되지 않고 살아 있는 항목의 수를 반환한다.
     * 이미 referent가 GC된 항목은 세지 않는다.
     *
     * <p>힌트: 각 WeakReference의 get()이 null이 아닌 것만 센다.
     */
    public int size() {
        throw new UnsupportedOperationException(
                "TODO: referent가 아직 살아 있는(get() != null) 항목 수를 반환하라");
    }
}
