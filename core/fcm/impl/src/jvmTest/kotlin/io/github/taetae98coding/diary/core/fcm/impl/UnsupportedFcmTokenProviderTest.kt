package io.github.taetae98coding.diary.core.fcm.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull

class UnsupportedFcmTokenProviderTest :
    FunSpec({
        test("TC-10ca5481-2fad-4307-afc7-cde4a831f225 FCM을 지원하지 않는 환경은 토큰이 없다") {
            UnsupportedFcmTokenProvider().getInstallationId().shouldBeNull()
        }
    })
