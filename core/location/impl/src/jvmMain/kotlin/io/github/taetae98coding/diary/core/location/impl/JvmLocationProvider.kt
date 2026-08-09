package io.github.taetae98coding.diary.core.location.impl

import io.github.taetae98coding.diary.core.location.api.Location
import io.github.taetae98coding.diary.core.location.api.LocationProvider
import org.koin.core.annotation.Factory

@Factory
internal class JvmLocationProvider : LocationProvider {
    // JVM 데스크톱 디바이스 위치 확인은 아직 지원하지 않아 계약상 위치 확인 불가로 null을 반환한다.
    override suspend fun getCurrentLocation(): Location? = null
}
