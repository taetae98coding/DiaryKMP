package io.github.taetae98coding.diary.feature.qr.ui.scan

import android.hardware.camera2.CameraManager
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.spyk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QrScanScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `TC-54221e2e-5131-46fa-a6c9-c19f89f30399 한국어 환경에서 상단 바에 제목을 표시한다`() {
        setQrScanScreen()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
    }

    @Test
    fun `TC-54221e2e-5131-46fa-a6c9-c19f89f30399 기본 환경에서 상단 바에 제목을 표시한다`() {
        setQrScanScreen()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-f755b080-8888-49ef-a8cb-56ac64438dda 제목 외의 정보와 뒤로가기 외의 동작을 두지 않는다`() {
        setQrScanScreen()
        composeRule.waitForIdle()

        visibleTextList() shouldBe listOf(KOREAN_TITLE)
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-f868d081-61f3-4ee0-b902-551b84aaea24 뒤로가기를 선택하면 값을 넘기지 않고 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        val valueList = mutableListOf<String>()
        setQrScanScreen(
            navigateUp = { navigateUpCount += 1 },
            navigateUpWithValue = { value -> valueList += value },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        valueList.shouldBeEmpty()
    }

    @Test
    fun `TC-2a394c2d-fe2c-4497-a268-ad7dcf159e14 QR을 읽기 전에는 화면에 머무른다`() {
        var navigateUpCount = 0
        val valueList = mutableListOf<String>()
        setQrScanScreen(
            navigateUp = { navigateUpCount += 1 },
            navigateUpWithValue = { value -> valueList += value },
        )

        composeRule.mainClock.advanceTimeBy(SCAN_WAIT_MILLIS)
        composeRule.waitForIdle()

        navigateUpCount shouldBe 0
        valueList.shouldBeEmpty()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-e58e6c08-4531-40fb-b4da-c59e4c7cea91 카메라를 시작할 수 없어도 안내 없이 뒤로가기로 돌아갈 수 있다`() {
        val cameraManager = RuntimeEnvironment.getApplication().getSystemService(CameraManager::class.java)
        cameraManager.cameraIdList.shouldBeEmpty()
        var navigateUpCount = 0
        setQrScanScreen(navigateUp = { navigateUpCount += 1 })
        composeRule.waitForIdle()

        visibleTextList() shouldBe listOf(KOREAN_TITLE)

        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-e6b4c026-fd1b-415d-845c-31ce485cd49c QrScan 화면이 표시되는 것만으로는 요청하지 않는다`() {
        val registry = spyk<ActivityResultRegistry>()
        every { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) } just runs
        val registryOwner =
            object : ActivityResultRegistryOwner {
                override val activityResultRegistry: ActivityResultRegistry = registry
            }

        composeRule.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
                DiaryTheme {
                    QrScanScreen(
                        navigateUp = {},
                        navigateUpWithValue = {},
                    )
                }
            }
        }
        composeRule.waitForIdle()

        verify(exactly = 0) { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) }
    }

    private fun setQrScanScreen(
        navigateUp: () -> Unit = {},
        navigateUpWithValue: (String) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                QrScanScreen(
                    navigateUp = navigateUp,
                    navigateUpWithValue = navigateUpWithValue,
                )
            }
        }
    }

    private fun visibleTextList(): List<String> =
        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .flatMap { node -> node.config[SemanticsProperties.Text] }
            .map { text -> text.text }

    public companion object {
        private const val KOREAN_TITLE = "QR 스캔"
        private const val DEFAULT_TITLE = "Scan QR code"
        private const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val SCAN_WAIT_MILLIS = 10_000L
    }
}
