package io.github.taetae98coding.diary.core.integrity.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull

class UnsupportedPlayIntegrityTokenProviderTest :
    FunSpec({
        test("TC-0ad09bab-6b59-4f35-9e63-0478f4bc6a48 Play Integrity를 제공하지 않는 플랫폼은 토큰이 없다") {
            UnsupportedPlayIntegrityTokenProvider().getToken(requestHash = "hash").shouldBeNull()
        }
    })
