package io.github.taetae98coding.diary.core.permission

import io.github.taetae98coding.diary.library.avfoundation.AVAuthorizationStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first

class JvmPermissionManagerTest :
    FunSpec({
        test("TC-NOTIFICATION-PERMISSION-DOMAIN-001 알림 권한 요청을 제공하지 않는 환경에서는 요청 없이 거부로 처리한다") {
            val cameraAuthorization = cameraAuthorization(status = AVAuthorizationStatus.NOT_DETERMINED, response = true)

            JvmPermissionManager(cameraAuthorization = cameraAuthorization).request(Permission.NOTIFICATION) shouldBe PermissionResult.DENIED
            coVerify(exactly = 0) { cameraAuthorization.requestAccess() }
        }

        test("TC-LOCATION-PERMISSION-DOMAIN-001 위치 권한 요청을 제공하지 않는 환경에서는 요청 없이 거부로 처리한다") {
            val cameraAuthorization = cameraAuthorization(status = AVAuthorizationStatus.NOT_DETERMINED, response = true)

            JvmPermissionManager(cameraAuthorization = cameraAuthorization).request(Permission.LOCATION) shouldBe PermissionResult.DENIED
            coVerify(exactly = 0) { cameraAuthorization.requestAccess() }
        }

        test("TC-CAMERA-PERMISSION-DOMAIN-005 데스크톱 앱에서도 시스템 카메라 권한 요청을 시작한다") {
            listOf(
                CameraRequestCase(status = AVAuthorizationStatus.AUTHORIZED, response = null, expectedRequestCount = 0, expectedResult = PermissionResult.ALREADY_GRANTED),
                CameraRequestCase(status = AVAuthorizationStatus.NOT_DETERMINED, response = true, expectedRequestCount = 1, expectedResult = PermissionResult.GRANTED),
                CameraRequestCase(status = AVAuthorizationStatus.NOT_DETERMINED, response = false, expectedRequestCount = 1, expectedResult = PermissionResult.DENIED),
                CameraRequestCase(status = AVAuthorizationStatus.DENIED, response = null, expectedRequestCount = 0, expectedResult = PermissionResult.DENIED),
            ).forEach { case ->
                val cameraAuthorization = cameraAuthorization(status = case.status, response = case.response)

                JvmPermissionManager(cameraAuthorization = cameraAuthorization).request(Permission.CAMERA) shouldBe case.expectedResult
                coVerify(exactly = case.expectedRequestCount) { cameraAuthorization.requestAccess() }
            }
        }

        test("기기 정책으로 카메라가 제한되어 있으면 요청 없이 거부로 처리한다") {
            val cameraAuthorization = cameraAuthorization(status = AVAuthorizationStatus.RESTRICTED, response = null)

            JvmPermissionManager(cameraAuthorization = cameraAuthorization).request(Permission.CAMERA) shouldBe PermissionResult.DENIED
            coVerify(exactly = 0) { cameraAuthorization.requestAccess() }
        }

        test("카메라 권한 요청을 허용하면 허용 여부가 허용됨으로 바뀌고, 거부하면 허용되지 않음으로 남는다") {
            listOf(true to true, false to false).forEach { (response, expectedIsGranted) ->
                val permissionManager =
                    JvmPermissionManager(cameraAuthorization = cameraAuthorization(status = AVAuthorizationStatus.NOT_DETERMINED, response = response))

                permissionManager.request(Permission.CAMERA)

                permissionManager.isGranted(Permission.CAMERA).first() shouldBe expectedIsGranted
            }
        }

        test("카메라 권한은 시스템이 허용됨으로 알려 줄 때만 허용 여부가 허용됨이다") {
            AVAuthorizationStatus.entries.forEach { status ->
                val permissionManager = JvmPermissionManager(cameraAuthorization = cameraAuthorization(status = status, response = null))

                permissionManager.refresh(Permission.CAMERA)

                permissionManager.isGranted(Permission.CAMERA).first() shouldBe (status == AVAuthorizationStatus.AUTHORIZED)
            }
        }

        test("TC-LOCATION-PERMISSION-DOMAIN-011 권한 개념이 없는 환경에서는 허용 여부가 허용되지 않음이다") {
            val permissionManager = JvmPermissionManager(cameraAuthorization = cameraAuthorization(status = AVAuthorizationStatus.AUTHORIZED, response = null))

            listOf(Permission.NOTIFICATION, Permission.LOCATION).forEach { permission ->
                permissionManager.refresh(permission)
                permissionManager.isGranted(permission).first() shouldBe false
            }
        }

        test("요청이 거부로 끝나도 허용 여부는 허용되지 않음으로 남는다") {
            val permissionManager = JvmPermissionManager(cameraAuthorization = cameraAuthorization(status = AVAuthorizationStatus.NOT_DETERMINED, response = true))

            permissionManager.request(Permission.LOCATION)

            permissionManager.isGranted(Permission.LOCATION).first() shouldBe false
        }
    })

private data class CameraRequestCase(
    val status: AVAuthorizationStatus,
    val response: Boolean?,
    val expectedRequestCount: Int,
    val expectedResult: PermissionResult,
)

private fun cameraAuthorization(
    status: AVAuthorizationStatus,
    response: Boolean?,
): JvmCameraAuthorization {
    var currentStatus = status
    val cameraAuthorization = mockk<JvmCameraAuthorization>()
    every { cameraAuthorization.status() } answers { currentStatus }
    coEvery { cameraAuthorization.requestAccess() } answers {
        val isGranted = checkNotNull(response)
        currentStatus = if (isGranted) AVAuthorizationStatus.AUTHORIZED else AVAuthorizationStatus.DENIED
        isGranted
    }

    return cameraAuthorization
}
