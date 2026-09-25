package io.github.taetae98coding.diary.core.integrity.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull

class UnsupportedPlayIntegrityTokenProviderTest :
    FunSpec({
        test("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-007 Play Integrity를 제공하지 않는 플랫폼은 토큰이 없다") {
            UnsupportedPlayIntegrityTokenProvider().getToken(requestHash = "hash").shouldBeNull()
        }
    })
