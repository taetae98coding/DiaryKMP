package io.github.taetae98coding.diary.feature.qr.ui.home

import android.Manifest
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QrHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-QR-HOME-FEATURE-003 뒤로가기를 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setQrHomeScreen(
            registry = respondingRegistry(isGranted = false),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-FEATURE-006 카메라 권한이 이미 허용되어 있으면 QrScan 화면으로 이동한다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA)
        val registry = respondingRegistry(isGranted = true)
        var navigateToScanCount = 0
        setQrHomeScreen(
            registry = registry,
            navigateToScan = { navigateToScanCount += 1 },
        )

        composeRule.onNodeWithContentDescription(KOREAN_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        registry.verifyLaunchCount(count = 0)
        navigateToScanCount shouldBe 1
        composeRule.onNodeWithText(KOREAN_PERMISSION_DENIED_MESSAGE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-FEATURE-006 카메라 권한 요청을 허용하면 QrScan 화면으로 이동한다`() {
        val registry = respondingRegistry(isGranted = true)
        var navigateToScanCount = 0
        setQrHomeScreen(
            registry = registry,
            navigateToScan = { navigateToScanCount += 1 },
        )

        composeRule.onNodeWithContentDescription(KOREAN_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        registry.verifyLaunchCount(count = 1)
        navigateToScanCount shouldBe 1
        composeRule.onNodeWithText(KOREAN_PERMISSION_DENIED_MESSAGE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-FEATURE-007 카메라 권한이 허용되지 않으면 화면에 머무르고 권한 안내를 표시한다`() {
        var navigateToScanCount = 0
        setQrHomeScreen(
            registry = respondingRegistry(isGranted = false),
            navigateToScan = { navigateToScanCount += 1 },
        )

        composeRule.onNodeWithContentDescription(KOREAN_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateToScanCount shouldBe 0
        composeRule.onNodeWithText(KOREAN_PERMISSION_DENIED_MESSAGE).assertExists()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-008 권한 요청에 응답하기 전에는 다시 선택해도 새 요청을 시작하지 않는다`() {
        val requestCode = slot<Int>()
        val registry = pendingRegistry(requestCode = requestCode)
        var navigateToScanCount = 0
        setQrHomeScreen(
            registry = registry,
            navigateToScan = { navigateToScanCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle { registry.respond(requestCode = requestCode.captured, isGranted = true) }
        composeRule.waitForIdle()

        registry.verifyLaunchCount(count = 1)
        navigateToScanCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-FEATURE-009 권한 요청에 응답한 뒤에는 다시 선택할 수 있다`() {
        val registry = respondingRegistry(isGranted = false)
        setQrHomeScreen(registry = registry)

        composeRule.onNodeWithContentDescription(KOREAN_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(KOREAN_PERMISSION_DENIED_MESSAGE).assertExists()
        composeRule.mainClock.advanceTimeBy(SNACKBAR_DISMISS_WAIT_MILLIS)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(KOREAN_PERMISSION_DENIED_MESSAGE).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(KOREAN_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        registry.verifyLaunchCount(count = 2)
        composeRule.onNodeWithText(KOREAN_PERMISSION_DENIED_MESSAGE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-DOMAIN-002 화면이 재생성되면 표시 중이던 권한 안내를 다시 표시하지 않는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            QrHomeScreenContent(registry = respondingRegistry(isGranted = false))
        }

        composeRule.onNodeWithContentDescription(KOREAN_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(KOREAN_PERMISSION_DENIED_MESSAGE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_PERMISSION_DENIED_MESSAGE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-DOMAIN-003 권한 요청을 기다리는 동안 화면이 재생성되면 허용 응답을 반영하지 않는다`() {
        assertResponseIgnoredAfterRestore(isGranted = true)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-DOMAIN-004 권한 요청을 기다리는 동안 화면이 재생성되면 거부 응답을 반영하지 않는다`() {
        assertResponseIgnoredAfterRestore(isGranted = false)
    }

    @Test
    fun `TC-CAMERA-PERMISSION-FEATURE-001 카메라 권한이 허용되어 있지 않으면 QR 스캔 시작 시 시스템 카메라 권한 요청이 시작된다`() {
        val input = slot<Any>()
        val registry = spyk<ActivityResultRegistry>()
        every { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), capture(input), any()) } answers {
            registry.respond(requestCode = firstArg(), isGranted = false)
        }
        setQrHomeScreen(registry = registry)

        composeRule.onNodeWithContentDescription(DEFAULT_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        registry.verifyLaunchCount(count = 1)
        (input.captured as Array<*>).toList() shouldContainExactly listOf(Manifest.permission.CAMERA)
    }

    @Test
    fun `TC-CAMERA-PERMISSION-FEATURE-002 카메라 권한이 이미 허용되어 있으면 요청이 시작되지 않는다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA)
        val registry = respondingRegistry(isGranted = true)
        setQrHomeScreen(registry = registry)

        composeRule.onNodeWithContentDescription(DEFAULT_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        registry.verifyLaunchCount(count = 0)
    }

    @Test
    fun `TC-CAMERA-PERMISSION-DOMAIN-001 QR 스캔 시작을 선택할 때마다 요청 조건을 다시 확인한다`() {
        val registry = respondingRegistry(isGranted = false)
        setQrHomeScreen(registry = registry)

        composeRule.onNodeWithContentDescription(DEFAULT_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        registry.verifyLaunchCount(count = 2)
    }

    @Test
    fun `TC-CAMERA-PERMISSION-DOMAIN-002 QrHome 화면이 표시되는 것만으로는 요청하지 않는다`() {
        val registry = respondingRegistry(isGranted = false)
        setQrHomeScreen(registry = registry)
        composeRule.waitForIdle()

        registry.verifyLaunchCount(count = 0)
    }

    private fun assertResponseIgnoredAfterRestore(isGranted: Boolean) {
        val requestCode = slot<Int>()
        val registry = pendingRegistry(requestCode = requestCode)
        var navigateToScanCount = 0
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            QrHomeScreenContent(
                registry = registry,
                navigateToScan = { navigateToScanCount += 1 },
            )
        }

        composeRule.onNodeWithContentDescription(KOREAN_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        composeRule.runOnIdle { registry.respond(requestCode = requestCode.captured, isGranted = isGranted) }
        composeRule.waitForIdle()

        navigateToScanCount shouldBe 0
        composeRule.onNodeWithText(KOREAN_PERMISSION_DENIED_MESSAGE).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(KOREAN_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        if (isGranted) {
            registry.verifyLaunchCount(count = 1)
            navigateToScanCount shouldBe 1
        } else {
            registry.verifyLaunchCount(count = 2)
            navigateToScanCount shouldBe 0
        }
    }

    private fun setQrHomeScreen(
        registry: ActivityResultRegistry,
        navigateUp: () -> Unit = {},
        navigateToScan: () -> Unit = {},
    ) {
        composeRule.setContent {
            QrHomeScreenContent(
                registry = registry,
                navigateUp = navigateUp,
                navigateToScan = navigateToScan,
            )
        }
    }

    @Composable
    private fun QrHomeScreenContent(
        registry: ActivityResultRegistry,
        navigateUp: () -> Unit = {},
        navigateToScan: () -> Unit = {},
    ) {
        val registryOwner =
            object : ActivityResultRegistryOwner {
                override val activityResultRegistry: ActivityResultRegistry = registry
            }

        DiaryTheme {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
                QrHomeScreen(
                    navigateUp = navigateUp,
                    navigateToScan = navigateToScan,
                    permissionManager = rememberPermissionManager(),
                )
            }
        }
    }

    private fun respondingRegistry(isGranted: Boolean): ActivityResultRegistry {
        val registry = spyk<ActivityResultRegistry>()
        every { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) } answers {
            registry.respond(requestCode = firstArg(), isGranted = isGranted)
        }

        return registry
    }

    private fun pendingRegistry(requestCode: CapturingSlot<Int>): ActivityResultRegistry {
        val registry = spyk<ActivityResultRegistry>()
        every { registry.onLaunch(capture(requestCode), any<ActivityResultContract<Any?, Any?>>(), any(), any()) } just runs

        return registry
    }

    private fun ActivityResultRegistry.verifyLaunchCount(count: Int) {
        verify(exactly = count) { onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) }
    }

    // 실제 시스템은 허용 응답과 함께 권한 상태도 바꾸므로 조회 결과가 응답을 따라가게 한다.
    private fun ActivityResultRegistry.respond(
        requestCode: Int,
        isGranted: Boolean,
    ) {
        if (isGranted) {
            shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA)
        }

        dispatchResult(requestCode, mapOf(Manifest.permission.CAMERA to isGranted))
    }

    public companion object {
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_SCAN_DESCRIPTION = "Scan QR code"
        private const val KOREAN_SCAN_DESCRIPTION = "QR 스캔"
        private const val SNACKBAR_DISMISS_WAIT_MILLIS = 10_000L
        private const val KOREAN_PERMISSION_DENIED_MESSAGE = "카메라 권한을 허용해야 QR을 스캔할 수 있습니다."
    }
}
