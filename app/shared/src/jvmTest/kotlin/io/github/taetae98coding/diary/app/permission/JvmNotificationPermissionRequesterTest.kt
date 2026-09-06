package io.github.taetae98coding.diary.app.permission

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class JvmNotificationPermissionRequesterTest :
    FunSpec({
        test("TC-NOTIFICATION-PERMISSION-DOMAIN-001 알림 권한 요청을 제공하지 않는 환경에서는 요청 없이 거부로 처리한다") {
            val requester = JvmNotificationPermissionRequester()

            requester.request() shouldBe NotificationPermissionRequestResult.DENIED
        }
    })
