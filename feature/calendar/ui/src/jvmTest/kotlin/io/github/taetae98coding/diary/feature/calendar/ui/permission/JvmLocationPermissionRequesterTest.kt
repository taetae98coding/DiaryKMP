package io.github.taetae98coding.diary.feature.calendar.ui.permission

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class JvmLocationPermissionRequesterTest :
    FunSpec({
        test("TC-CALENDAR-HOME-DOMAIN-002 위치 권한 요청을 제공하지 않는 환경에서는 시스템 요청 없이 거부로 처리한다") {
            val requester = JvmLocationPermissionRequester()

            requester.request() shouldBe LocationPermissionRequestResult.DENIED
        }
    })
