package io.github.taetae98coding.diary.core.permission

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class JvmPermissionManagerTest :
    FunSpec({
        test("TC-NOTIFICATION-PERMISSION-DOMAIN-001 알림 권한 요청을 제공하지 않는 환경에서는 요청 없이 거부로 처리한다") {
            JvmPermissionManager().request(Permission.NOTIFICATION) shouldBe PermissionResult.DENIED
        }

        test("TC-LOCATION-PERMISSION-DOMAIN-001 위치 권한 요청을 제공하지 않는 환경에서는 요청 없이 거부로 처리한다") {
            JvmPermissionManager().request(Permission.LOCATION) shouldBe PermissionResult.DENIED
        }

        test("권한 개념이 없는 환경에서는 모든 권한의 허용 여부가 허용되지 않음이다") {
            val permissionManager = JvmPermissionManager()

            Permission.entries.forEach { permission ->
                permissionManager.refresh(permission)
                permissionManager.isGranted(permission).first() shouldBe false
            }
        }

        test("요청이 거부로 끝나도 허용 여부는 허용되지 않음으로 남는다") {
            val permissionManager = JvmPermissionManager()

            permissionManager.request(Permission.LOCATION)

            permissionManager.isGranted(Permission.LOCATION).first() shouldBe false
        }
    })
