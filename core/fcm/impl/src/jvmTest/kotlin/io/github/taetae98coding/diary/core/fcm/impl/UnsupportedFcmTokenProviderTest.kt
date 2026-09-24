package io.github.taetae98coding.diary.core.fcm.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull

class UnsupportedFcmTokenProviderTest :
    FunSpec({
        test("TC-FCM-TOKEN-DOMAIN-001 FCM을 지원하지 않는 환경은 토큰이 없다") {
            UnsupportedFcmTokenProvider().getToken().shouldBeNull()
        }
    })
