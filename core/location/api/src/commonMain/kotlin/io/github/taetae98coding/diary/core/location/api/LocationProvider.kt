package io.github.taetae98coding.diary.core.location.api

public interface LocationProvider {
    /**
     * 디바이스가 위치를 지원하지 않거나 위치 권한이 없는 경우처럼 디바이스 위치를 확인하지 못하면 실패로 알리지 않고 `null`을 돌려준다. 디바이스 위치를 확인하지 못한 것은 현재 위치 확인 실패가 아니라 공인 IP 기준 확인으로 이어지는 경우이기 때문이다.
     */
    public suspend fun getCurrentLocation(): Location?
}
