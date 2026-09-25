package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QrHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-FEATURE-001 한국어 환경에서 상단 바에 제목을 표시한다`() {
        setQrHomeScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
    }

    @Test
    fun `TC-QR-HOME-FEATURE-001 기본 환경에서 상단 바에 제목을 표시한다`() {
        setQrHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-FEATURE-004 제목 외의 정보와 준비 중 안내를 표시하지 않는다`() {
        setQrHomeScaffold()

        val texts =
            composeRule
                .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .flatMap { node -> node.config[SemanticsProperties.Text] }
                .map { text -> text.text }

        texts shouldBe listOf(KOREAN_TITLE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-HOME-FEATURE-005 한국어 환경에서 선택할 수 있는 동작은 뒤로가기와 QR 스캔 시작뿐이다`() {
        setQrHomeScaffold()

        clickableContentDescriptionList() shouldContainExactly listOf(KOREAN_NAVIGATE_UP_DESCRIPTION, KOREAN_SCAN_DESCRIPTION)
    }

    @Test
    fun `TC-QR-HOME-FEATURE-005 기본 환경에서 선택할 수 있는 동작은 뒤로가기와 QR 스캔 시작뿐이다`() {
        setQrHomeScaffold()

        clickableContentDescriptionList() shouldContainExactly listOf(DEFAULT_NAVIGATE_UP_DESCRIPTION, DEFAULT_SCAN_DESCRIPTION)
    }

    @Test
    fun `뒤로가기를 누르면 뒤로가기 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<QrHomeScaffoldEvent>()
        setQrHomeScaffold(onEvent = { event -> eventList += event })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(QrHomeScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `QR 스캔 버튼을 누르면 QR 스캔 시작 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<QrHomeScaffoldEvent>()
        setQrHomeScaffold(onEvent = { event -> eventList += event })

        composeRule.onNodeWithContentDescription(DEFAULT_SCAN_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(QrHomeScaffoldEvent.ClickScan)
    }

    private fun setQrHomeScaffold(onEvent: (QrHomeScaffoldEvent) -> Unit = {}) {
        composeRule.setContent {
            DiaryTheme {
                QrHomeScaffold(onEvent = onEvent)
            }
        }
    }

    private fun clickableContentDescriptionList(): List<String> =
        composeRule
            .onAllNodes(hasClickAction())
            .fetchSemanticsNodes()
            .flatMap { node -> node.config[SemanticsProperties.ContentDescription] }

    public companion object {
        private const val KOREAN_TITLE = "QR"
        private const val DEFAULT_TITLE = "QR"
        private const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val KOREAN_SCAN_DESCRIPTION = "QR 스캔"
        private const val DEFAULT_SCAN_DESCRIPTION = "Scan QR code"
    }
}
