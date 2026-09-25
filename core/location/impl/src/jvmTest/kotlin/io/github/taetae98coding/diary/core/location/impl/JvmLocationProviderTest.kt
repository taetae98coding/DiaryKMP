package io.github.taetae98coding.diary.core.location.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull

class JvmLocationProviderTest :
    FunSpec({
        test("TC-CURRENT-LOCATION-DOMAIN-002 디바이스 위치 확인을 제공하지 않는 데스크톱에서는 디바이스 위치를 확인하지 못한 것으로 다룬다") {
            JvmLocationProvider().getCurrentLocation().shouldBeNull()
        }
    })
