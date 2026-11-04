package io.github.taetae98coding.diary.feature.qr.ui.add

import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.core.permission.PermissionResult
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

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

        test("권한 요청에 응답하기 전에 다시 시작하면 새 요청을 시작하지 않는다") {
            runTest {
                val response = CompletableDeferred<PermissionResult>()
                val permissionManager = mockk<PermissionManager>()
                coEvery { permissionManager.request(Permission.CAMERA) } coAnswers { response.await() }
                val starter = QrScanStarter(permissionManager = permissionManager)
                var grantedCount = 0

                launch { starter.startIfIdle(onGranted = { grantedCount += 1 }, onDenied = {}) }
                runCurrent()
                launch { starter.startIfIdle(onGranted = { grantedCount += 1 }, onDenied = {}) }
                runCurrent()
                response.complete(PermissionResult.GRANTED)
                runCurrent()

                coVerify(exactly = 1) { permissionManager.request(Permission.CAMERA) }
                grantedCount shouldBe 1
            }
        }

        test("권한 요청이 끝난 뒤에는 다시 시작한다") {
            runTest {
                val permissionManager = mockk<PermissionManager>()
                coEvery { permissionManager.request(Permission.CAMERA) } returns PermissionResult.DENIED
                val starter = QrScanStarter(permissionManager = permissionManager)
                var deniedCount = 0

                starter.startIfIdle(onGranted = {}, onDenied = { deniedCount += 1 })
                starter.startIfIdle(onGranted = {}, onDenied = { deniedCount += 1 })

                coVerify(exactly = 2) { permissionManager.request(Permission.CAMERA) }
                deniedCount shouldBe 2
            }
        }

        test("권한 요청이 취소되면 다시 시작할 수 있다") {
            runTest {
                val response = CompletableDeferred<PermissionResult>()
                val permissionManager = mockk<PermissionManager>()
                coEvery { permissionManager.request(Permission.CAMERA) } coAnswers { response.await() }
                val starter = QrScanStarter(permissionManager = permissionManager)
                var grantedCount = 0

                val job = launch { starter.startIfIdle(onGranted = { grantedCount += 1 }, onDenied = {}) }
                runCurrent()
                job.cancel()
                runCurrent()
                coEvery { permissionManager.request(Permission.CAMERA) } returns PermissionResult.GRANTED
                starter.startIfIdle(onGranted = { grantedCount += 1 }, onDenied = {})

                coVerify(exactly = 2) { permissionManager.request(Permission.CAMERA) }
                grantedCount shouldBe 1
            }
        }
    })
