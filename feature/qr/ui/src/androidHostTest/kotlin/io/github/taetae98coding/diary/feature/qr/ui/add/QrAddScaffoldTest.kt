package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QrAddScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-001 한국어 환경에서 상단 바에 제목을 표시한다`() {
        setQrAddScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
    }

    @Test
    fun `TC-QR-ADD-FEATURE-001 기본 환경에서 상단 바에 제목을 표시한다`() {
        setQrAddScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-002 한국어 환경에서 선택할 수 있는 동작은 뒤로가기, QR 값 입력과 QR 스캔 시작뿐이다`() {
        setQrAddScaffold()

        assertActionList(
            navigateUpDescription = KOREAN_NAVIGATE_UP_DESCRIPTION,
            valueLabel = KOREAN_VALUE_LABEL,
            scanDescription = KOREAN_SCAN_DESCRIPTION,
        )
    }

    @Test
    fun `TC-QR-ADD-FEATURE-002 기본 환경에서 선택할 수 있는 동작은 뒤로가기, QR 값 입력과 QR 스캔 시작뿐이다`() {
        setQrAddScaffold()

        assertActionList(
            navigateUpDescription = DEFAULT_NAVIGATE_UP_DESCRIPTION,
            valueLabel = DEFAULT_VALUE_LABEL,
            scanDescription = DEFAULT_SCAN_DESCRIPTION,
        )
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-003 처음 들어오면 값이 비어 있고 QR을 그리지 않는다`() {
        setQrAddScaffold()

        composeRule.qrValueInputText() shouldBe ""
        composeRule.qrCodeValue() shouldBe ""
        composeRule.visibleTextList() shouldContainExactlyInAnyOrder listOf(KOREAN_TITLE, KOREAN_VALUE_LABEL)
    }

    @Test
    fun `TC-QR-ADD-FEATURE-004 값을 입력하면 그 값을 담은 QR을 바로 그린다`() {
        setQrAddScaffold()

        listOf(qrTestFixtureMonkey.qrValue(), "안녕하세요", "첫 줄\n둘째 줄").forEach { value ->
            composeRule.onQrValueInput().performTextClearance()

            composeRule.onQrValueInput().performTextInput(value)
            composeRule.waitForIdle()

            composeRule.qrValueInputText() shouldBe value
            composeRule.qrCodeValue() shouldBe value
        }
    }

    @Test
    fun `TC-QR-ADD-FEATURE-005 값을 바꾸면 바뀐 값을 담은 QR로 다시 그린다`() {
        val firstValue = qrTestFixtureMonkey.qrValue()
        val addedValue = qrTestFixtureMonkey.qrValue()
        setQrAddScaffold()
        composeRule.onQrValueInput().performTextInput(firstValue)
        composeRule.waitForIdle()

        composeRule.onQrValueInput().performTextInput(addedValue)
        composeRule.waitForIdle()

        composeRule.qrCodeValue() shouldBe firstValue + addedValue
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-006 값을 모두 지우면 QR을 그리지 않는다`() {
        setQrAddScaffold()
        composeRule.onQrValueInput().performTextInput(qrTestFixtureMonkey.qrValue())
        composeRule.waitForIdle()

        composeRule.onQrValueInput().performTextClearance()
        composeRule.waitForIdle()

        composeRule.qrCodeValue() shouldBe ""
        composeRule.visibleTextList() shouldContainExactlyInAnyOrder listOf(KOREAN_TITLE, KOREAN_VALUE_LABEL)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-007 QR 하나에 담을 수 없는 입력은 반영하지 않는다`() {
        val longestValue = "1".repeat(MAX_NUMERIC_LENGTH)
        setQrAddScaffold()
        composeRule.onQrValueInput().performTextInput(longestValue)
        composeRule.waitForIdle()

        composeRule.onQrValueInput().performTextInput("1")
        composeRule.waitForIdle()

        composeRule.qrValueInputText() shouldBe longestValue
        composeRule.qrCodeValue() shouldBe longestValue
        composeRule.visibleTextList() shouldContainExactlyInAnyOrder listOf(KOREAN_TITLE, KOREAN_VALUE_LABEL)
    }

    @Test
    fun `TC-QR-ADD-DOMAIN-001 줄바꿈과 앞뒤 공백을 그대로 담는다`() {
        setQrAddScaffold()

        listOf("  abc  ", " ", "a\nb").forEach { value ->
            composeRule.onQrValueInput().performTextClearance()

            composeRule.onQrValueInput().performTextInput(value)
            composeRule.waitForIdle()

            composeRule.qrValueInputText() shouldBe value
            composeRule.qrCodeValue() shouldBe value
        }
    }

    @Test
    fun `TC-QR-ADD-DOMAIN-003 화면이 재생성되거나 시스템이 앱을 되살려도 입력한 값을 유지한다`() {
        val value = qrTestFixtureMonkey.qrValue()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { QrAddScaffoldContent() }
        composeRule.onQrValueInput().performTextInput(value)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.qrValueInputText() shouldBe value
        composeRule.qrCodeValue() shouldBe value
    }

    @Test
    fun `뒤로가기를 누르면 뒤로가기 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<QrAddScaffoldEvent>()
        setQrAddScaffold(onEvent = { event -> eventList += event })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(QrAddScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `QR 스캔 버튼을 누르면 QR 스캔 시작 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<QrAddScaffoldEvent>()
        setQrAddScaffold(onEvent = { event -> eventList += event })

        composeRule.onNodeWithContentDescription(DEFAULT_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(QrAddScaffoldEvent.ClickScan)
    }

    private fun setQrAddScaffold(onEvent: (QrAddScaffoldEvent) -> Unit = {}) {
        composeRule.setContent { QrAddScaffoldContent(onEvent = onEvent) }
    }

    @Composable
    private fun QrAddScaffoldContent(onEvent: (QrAddScaffoldEvent) -> Unit = {}) {
        DiaryTheme {
            QrAddScaffold(onEvent = onEvent)
        }
    }

    private fun assertActionList(
        navigateUpDescription: String,
        valueLabel: String,
        scanDescription: String,
    ) {
        val clickableNodeList = composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes()

        clickableNodeList.size shouldBe 3
        composeRule.onNode(hasClickAction() and hasContentDescription(navigateUpDescription)).assertExists()
        composeRule.onNode(hasClickAction() and hasContentDescription(scanDescription)).assertExists()
        composeRule.onNode(hasSetTextAction() and hasText(valueLabel)).assertExists()
        clickableNodeList.count { node -> node.config.contains(SemanticsProperties.EditableText) } shouldBe 1
    }

    public companion object {
        private const val KOREAN_TITLE = "QR 추가"
        private const val DEFAULT_TITLE = "Add QR code"
        private const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val KOREAN_VALUE_LABEL = "QR 값"
        private const val DEFAULT_VALUE_LABEL = "QR value"
        private const val KOREAN_SCAN_DESCRIPTION = "QR 스캔"
        private const val DEFAULT_SCAN_DESCRIPTION = "Scan QR code"
    }
}
