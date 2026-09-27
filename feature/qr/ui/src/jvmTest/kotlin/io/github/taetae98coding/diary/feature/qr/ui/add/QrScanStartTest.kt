package io.github.taetae98coding.diary.feature.qr.ui.add

import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.core.permission.PermissionResult
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class QrScanStartTest :
    FunSpec({
        test("카메라를 쓸 수 있는 요청 결과는 허용으로 한 번 처리한다") {
            listOf(PermissionResult.ALREADY_GRANTED, PermissionResult.GRANTED).forEach { result ->
                val permissionManager = mockk<PermissionManager>()
                coEvery { permissionManager.request(Permission.CAMERA) } returns result
                var grantedCount = 0
                var deniedCount = 0

                startQrScan(
                    permissionManager = permissionManager,
                    onGranted = { grantedCount += 1 },
                    onDenied = { deniedCount += 1 },
                )

                grantedCount shouldBe 1
                deniedCount shouldBe 0
                coVerify(exactly = 1) { permissionManager.request(Permission.CAMERA) }
            }
        }

        test("카메라 권한이 거부되면 거부로 한 번 처리한다") {
            val permissionManager = mockk<PermissionManager>()
            coEvery { permissionManager.request(Permission.CAMERA) } returns PermissionResult.DENIED
            var grantedCount = 0
            var deniedCount = 0

            startQrScan(
                permissionManager = permissionManager,
                onGranted = { grantedCount += 1 },
                onDenied = { deniedCount += 1 },
            )

            grantedCount shouldBe 0
            deniedCount shouldBe 1
        }
    })
