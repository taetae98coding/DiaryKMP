package io.github.taetae98coding.diary.feature.qr.ui.add

import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.core.permission.PermissionResult
import io.github.taetae98coding.diary.feature.qr.ui.scan.isQrScanSupported
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class QrScanStartTest :
    FunSpec({
        test("TC-QR-ADD-FEATURE-012 카메라를 사용할 수 없는 환경에서는 권한을 요청하지 않고 이동도 안내도 하지 않는다") {
            val permissionManager = mockk<PermissionManager>()
            var grantedCount = 0
            var deniedCount = 0

            startQrScan(
                permissionManager = permissionManager,
                isSupported = isQrScanSupported,
                onGranted = { grantedCount += 1 },
                onDenied = { deniedCount += 1 },
            )

            coVerify(exactly = 0) { permissionManager.request(any()) }
            grantedCount shouldBe 0
            deniedCount shouldBe 0
        }

        test("카메라를 쓸 수 있는 요청 결과는 허용으로 한 번 처리한다") {
            listOf(PermissionResult.ALREADY_GRANTED, PermissionResult.GRANTED).forEach { result ->
                val permissionManager = mockk<PermissionManager>()
                coEvery { permissionManager.request(Permission.CAMERA) } returns result
                var grantedCount = 0
                var deniedCount = 0

                startQrScan(
                    permissionManager = permissionManager,
                    isSupported = true,
                    onGranted = { grantedCount += 1 },
                    onDenied = { deniedCount += 1 },
                )

                grantedCount shouldBe 1
                deniedCount shouldBe 0
            }
        }

        test("카메라 권한이 거부되면 거부로 한 번 처리한다") {
            val permissionManager = mockk<PermissionManager>()
            coEvery { permissionManager.request(Permission.CAMERA) } returns PermissionResult.DENIED
            var grantedCount = 0
            var deniedCount = 0

            startQrScan(
                permissionManager = permissionManager,
                isSupported = true,
                onGranted = { grantedCount += 1 },
                onDenied = { deniedCount += 1 },
            )

            grantedCount shouldBe 0
            deniedCount shouldBe 1
        }
    })
