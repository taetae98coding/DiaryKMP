package io.github.taetae98coding.diary.feature.qr.ui.add

import android.Manifest
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextRange
import androidx.navigation3.runtime.result.ResultEventBus
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScannedResult
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
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class QrAddScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-QR-ADD-FEATURE-016 뒤로가기를 선택하면 QrHome 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setQrAddScreen(
            registry = respondingRegistry(isGranted = false),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-008 카메라 권한이 이미 허용되어 있으면 QrScan 화면으로 이동한다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA)
        val registry = respondingRegistry(isGranted = true)
        var navigateToScanCount = 0
        setQrAddScreen(
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
    fun `TC-QR-ADD-FEATURE-008 카메라 권한 요청을 허용하면 QrScan 화면으로 이동한다`() {
        val registry = respondingRegistry(isGranted = true)
        var navigateToScanCount = 0
        setQrAddScreen(
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
    fun `TC-QR-ADD-FEATURE-009 카메라 권한이 허용되지 않으면 화면에 머무르고 권한 안내를 표시한다`() {
        var navigateToScanCount = 0
        setQrAddScreen(
            registry = respondingRegistry(isGranted = false),
            navigateToScan = { navigateToScanCount += 1 },
        )
        val detail = qrTestFixtureMonkey.qrDetail(description = "description-${qrTestFixtureMonkey.giveMeOne<String>()}")
        composeRule.onTitleInput().performTextInput(detail.title)
        composeRule.onDescriptionInput().performTextInput(detail.description)
        composeRule.onQrValueInput().performTextInput(detail.value)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(KOREAN_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateToScanCount shouldBe 0
        composeRule.onNodeWithText(KOREAN_PERMISSION_DENIED_MESSAGE).assertExists()
        composeRule.titleInputText() shouldBe detail.title
        composeRule.descriptionInputText() shouldBe detail.description
        composeRule.qrValueInputText() shouldBe detail.value
    }

    @Test
    @Config(qualifiers = "ko")
    fun `권한 안내가 보이는 동안 추가 결과가 나오면 즉시 새 피드백으로 바꾼다`() {
        composeRule.setContent {
            QrAddScreenContent(
                registry = respondingRegistry(isGranted = false),
                viewModel = remember { effectViewModel(effect = QrAddEffect.AddSucceeded) },
            )
        }
        composeRule.onNodeWithContentDescription(KOREAN_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(KOREAN_PERMISSION_DENIED_MESSAGE).assertExists()

        composeRule.onNodeWithContentDescription(KOREAN_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_ADD_SUCCEEDED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_PERMISSION_DENIED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-QR-ADD-FEATURE-010 권한 요청에 응답하기 전에는 다시 선택해도 새 요청을 시작하지 않는다`() {
        val requestCode = slot<Int>()
        val registry = pendingRegistry(requestCode = requestCode)
        var navigateToScanCount = 0
        setQrAddScreen(
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
    fun `TC-QR-ADD-FEATURE-011 권한 요청에 응답한 뒤에는 다시 선택할 수 있다`() {
        val registry = respondingRegistry(isGranted = false)
        setQrAddScreen(registry = registry)

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
    fun `TC-QR-ADD-DOMAIN-004 화면이 재생성되면 표시 중이던 권한 안내를 다시 표시하지 않는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            QrAddScreenContent(registry = respondingRegistry(isGranted = false))
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
    fun `TC-QR-ADD-DOMAIN-005 권한 요청을 기다리는 동안 화면이 재생성되면 허용 응답을 반영하지 않는다`() {
        assertResponseIgnoredAfterRestore(isGranted = true)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-DOMAIN-006 권한 요청을 기다리는 동안 화면이 재생성되면 거부 응답을 반영하지 않는다`() {
        assertResponseIgnoredAfterRestore(isGranted = false)
    }

    @Test
    fun `TC-CAMERA-PERMISSION-FEATURE-001 카메라 권한이 허용되어 있지 않으면 QR 스캔 시작 시 시스템 카메라 권한 요청이 시작된다`() {
        val input = slot<Any>()
        val registry = spyk<ActivityResultRegistry>()
        every { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), capture(input), any()) } answers {
            registry.respond(requestCode = firstArg(), isGranted = false)
        }
        setQrAddScreen(registry = registry)

        composeRule.onNodeWithContentDescription(DEFAULT_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        registry.verifyLaunchCount(count = 1)
        (input.captured as Array<*>).toList() shouldContainExactly listOf(Manifest.permission.CAMERA)
    }

    @Test
    fun `TC-CAMERA-PERMISSION-FEATURE-002 카메라 권한이 이미 허용되어 있으면 요청이 시작되지 않는다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA)
        val registry = respondingRegistry(isGranted = true)
        setQrAddScreen(registry = registry)

        composeRule.onNodeWithContentDescription(DEFAULT_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        registry.verifyLaunchCount(count = 0)
    }

    @Test
    fun `TC-CAMERA-PERMISSION-DOMAIN-001 QR 스캔 시작을 선택할 때마다 요청 조건을 다시 확인한다`() {
        val registry = respondingRegistry(isGranted = false)
        setQrAddScreen(registry = registry)

        composeRule.onNodeWithContentDescription(DEFAULT_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        registry.verifyLaunchCount(count = 2)
    }

    @Test
    fun `TC-CAMERA-PERMISSION-DOMAIN-002 QrAdd 화면이 표시되는 것만으로는 요청하지 않는다`() {
        val registry = respondingRegistry(isGranted = false)
        setQrAddScreen(registry = registry)
        composeRule.waitForIdle()

        registry.verifyLaunchCount(count = 0)
    }

    @Test
    fun `스캔 결과로 값을 바꾸면 입력 커서를 값의 끝에 둔다`() {
        val value = qrTestFixtureMonkey.qrDetail().value
        val resultEventBus = ResultEventBus()
        setQrAddScreen(
            registry = respondingRegistry(isGranted = true),
            resultEventBus = resultEventBus,
        )

        composeRule.runOnIdle { resultEventBus.sendResult(result = QrScannedResult(value = value)) }
        composeRule.waitForIdle()

        composeRule
            .onQrValueInput()
            .fetchSemanticsNode()
            .config[SemanticsProperties.TextSelectionRange] shouldBe TextRange(value.length)
    }

    private fun assertResponseIgnoredAfterRestore(isGranted: Boolean) {
        val requestCode = slot<Int>()
        val registry = pendingRegistry(requestCode = requestCode)
        var navigateToScanCount = 0
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            QrAddScreenContent(
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

    private fun setQrAddScreen(
        registry: ActivityResultRegistry,
        navigateUp: () -> Unit = {},
        navigateToScan: () -> Unit = {},
        resultEventBus: ResultEventBus = ResultEventBus(),
    ) {
        composeRule.setContent {
            QrAddScreenContent(
                registry = registry,
                navigateUp = navigateUp,
                navigateToScan = navigateToScan,
                resultEventBus = resultEventBus,
            )
        }
    }

    @Composable
    private fun QrAddScreenContent(
        registry: ActivityResultRegistry,
        navigateUp: () -> Unit = {},
        navigateToScan: () -> Unit = {},
        resultEventBus: ResultEventBus = remember { ResultEventBus() },
        viewModel: QrAddViewModel = remember { screenTestViewModel() },
    ) {
        val registryOwner =
            object : ActivityResultRegistryOwner {
                override val activityResultRegistry: ActivityResultRegistry = registry
            }

        DiaryTheme {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
                QrAddScreen(
                    navigateUp = navigateUp,
                    navigateToScan = navigateToScan,
                    permissionManager = rememberPermissionManager(),
                    resultEventBus = resultEventBus,
                    viewModel = viewModel,
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
        private const val KOREAN_ADD_DESCRIPTION = "QR 추가"
        private const val KOREAN_ADD_SUCCEEDED_MESSAGE = "QR이 추가되었습니다."
        private const val SNACKBAR_DISMISS_WAIT_MILLIS = 10_000L
        private const val KOREAN_PERMISSION_DENIED_MESSAGE = "카메라 권한을 허용해야 QR을 스캔할 수 있습니다."
    }
}
