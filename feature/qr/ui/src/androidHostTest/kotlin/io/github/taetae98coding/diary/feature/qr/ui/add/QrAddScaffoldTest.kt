package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
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
    fun `TC-QR-ADD-FEATURE-027 한국어 환경에서 화면에서 할 수 있는 동작은 정해진 입력과 동작뿐이다`() {
        setQrAddScaffold()

        assertActionList(
            navigateUpDescription = KOREAN_NAVIGATE_UP_DESCRIPTION,
            titleLabel = KOREAN_TITLE_LABEL,
            descriptionLabel = KOREAN_DESCRIPTION_LABEL,
            descriptionTabList = KOREAN_DESCRIPTION_TAB_LIST,
            valueLabel = KOREAN_VALUE_LABEL,
            scanDescription = KOREAN_SCAN_DESCRIPTION,
            addDescription = KOREAN_ADD_DESCRIPTION,
        )
    }

    @Test
    fun `TC-QR-ADD-FEATURE-027 기본 환경에서 화면에서 할 수 있는 동작은 정해진 입력과 동작뿐이다`() {
        setQrAddScaffold()

        assertActionList(
            navigateUpDescription = DEFAULT_NAVIGATE_UP_DESCRIPTION,
            titleLabel = DEFAULT_TITLE_LABEL,
            descriptionLabel = DEFAULT_DESCRIPTION_LABEL,
            descriptionTabList = DEFAULT_DESCRIPTION_TAB_LIST,
            valueLabel = DEFAULT_VALUE_LABEL,
            scanDescription = DEFAULT_SCAN_DESCRIPTION,
            addDescription = DEFAULT_ADD_DESCRIPTION,
        )
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-003 처음 들어오면 값이 비어 있고 QR을 그리지 않는다`() {
        setQrAddScaffold()

        composeRule.qrValueInputText() shouldBe ""
        composeRule.qrCodeValue() shouldBe ""
        composeRule.visibleTextList() shouldContainExactlyInAnyOrder KOREAN_VISIBLE_TEXT_LIST
    }

    @Test
    fun `TC-QR-ADD-FEATURE-004 값을 입력하면 그 값을 담은 QR을 바로 그린다`() {
        setQrAddScaffold()

        listOf(qrTestFixtureMonkey.qrDetail().value, "안녕하세요", "${qrTestFixtureMonkey.qrDetail().value}\n${qrTestFixtureMonkey.qrDetail().value}").forEach { value ->
            composeRule.onQrValueInput().performTextClearance()

            composeRule.onQrValueInput().performTextInput(value)
            composeRule.waitForIdle()

            composeRule.qrValueInputText() shouldBe value
            composeRule.qrCodeValue() shouldBe value
        }
    }

    @Test
    fun `TC-QR-ADD-FEATURE-005 값을 바꾸면 바뀐 값을 담은 QR로 다시 그린다`() {
        val firstValue = qrTestFixtureMonkey.qrDetail().value
        val addedValue = qrTestFixtureMonkey.qrDetail().value
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
        composeRule.onQrValueInput().performTextInput(qrTestFixtureMonkey.qrDetail().value)
        composeRule.waitForIdle()

        composeRule.onQrValueInput().performTextClearance()
        composeRule.waitForIdle()

        composeRule.qrCodeValue() shouldBe ""
        composeRule.visibleTextList() shouldContainExactlyInAnyOrder KOREAN_VISIBLE_TEXT_LIST
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
        composeRule.visibleTextList() shouldContainExactlyInAnyOrder KOREAN_VISIBLE_TEXT_LIST
    }

    @Test
    fun `TC-QR-ADD-FEATURE-020 제목과 설명을 바꿔도 QR 그림은 QR 값만 담는다`() {
        val value = qrTestFixtureMonkey.qrDetail().value
        setQrAddScaffold()
        composeRule.onQrValueInput().performTextInput(value)
        composeRule.waitForIdle()

        composeRule.onTitleInput().performTextInput(qrTestFixtureMonkey.qrDetail().title)
        composeRule.onDescriptionInput().performTextInput(qrTestFixtureMonkey.qrDetail().description)
        composeRule.waitForIdle()

        composeRule.qrCodeValue() shouldBe value
    }

    @Test
    fun `TC-QR-ADD-FEATURE-023 추가를 처리하는 동안 추가 버튼이 이름을 유지한 채 진행 표시로 바뀐다`() {
        setQrAddScaffold(uiState = QrAddUiState(isInProgress = true))

        composeRule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate).and(hasAnyAncestor(hasContentDescription(DEFAULT_ADD_DESCRIPTION))),
                useUnmergedTree = true,
            ).assertExists()
    }

    @Test
    fun `추가를 처리하지 않는 동안에는 진행 표시가 없다`() {
        setQrAddScaffold()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-QR-ADD-DOMAIN-001 줄바꿈과 앞뒤 공백을 그대로 담는다`() {
        setQrAddScaffold()

        listOf("  ${qrTestFixtureMonkey.qrDetail().value}  ", " ", "${qrTestFixtureMonkey.qrDetail().value}\n${qrTestFixtureMonkey.qrDetail().value}").forEach { value ->
            composeRule.onQrValueInput().performTextClearance()

            composeRule.onQrValueInput().performTextInput(value)
            composeRule.waitForIdle()

            composeRule.qrValueInputText() shouldBe value
            composeRule.qrCodeValue() shouldBe value
        }
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

    @Test
    fun `추가 버튼을 누르면 추가 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<QrAddScaffoldEvent>()
        setQrAddScaffold(onEvent = { event -> eventList += event })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(QrAddScaffoldEvent.ClickAdd)
    }

    @Test
    fun `Meta와 Enter를 함께 누르면 추가 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<QrAddScaffoldEvent>()
        setQrAddScaffold(onEvent = { event -> eventList += event })
        composeRule.onTitleInput().performClick()
        composeRule.waitForIdle()

        composeRule.onTitleInput().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        eventList shouldBe listOf(QrAddScaffoldEvent.ClickAdd)
    }

    private fun setQrAddScaffold(
        onEvent: (QrAddScaffoldEvent) -> Unit = {},
        uiState: QrAddUiState = QrAddUiState(),
    ) {
        composeRule.setContent {
            DiaryTheme {
                QrAddScaffold(
                    onEvent = onEvent,
                    uiStateProvider = { uiState },
                )
            }
        }
    }

    private fun assertActionList(
        navigateUpDescription: String,
        titleLabel: String,
        descriptionLabel: String,
        descriptionTabList: List<String>,
        valueLabel: String,
        scanDescription: String,
        addDescription: String,
    ) {
        composeRule.onNode(hasClickAction() and hasContentDescription(navigateUpDescription)).assertExists()
        composeRule.onNode(hasSetTextAction() and hasText(titleLabel)).assertExists()
        composeRule.onNode(hasSetTextAction() and hasText(descriptionLabel)).assertExists()
        composeRule.onNode(hasSetTextAction() and hasText(valueLabel)).assertExists()
        composeRule.onNode(hasClickAction() and hasContentDescription(scanDescription)).assertExists()
        composeRule.onNode(hasClickAction() and hasContentDescription(addDescription)).assertExists()
        composeRule.inputCount() shouldBe INPUT_COUNT
        composeRule.actionNameList() shouldContainExactlyInAnyOrder
            listOf(navigateUpDescription, titleLabel, descriptionLabel, valueLabel, scanDescription, addDescription) + descriptionTabList
    }

    public companion object {
        private const val KOREAN_TITLE = "QR 추가"
        private const val DEFAULT_TITLE = "Add QR code"
        private const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val KOREAN_TITLE_LABEL = "제목"
        private const val DEFAULT_TITLE_LABEL = "Title"
        private const val KOREAN_DESCRIPTION_LABEL = "설명"
        private const val DEFAULT_DESCRIPTION_LABEL = "Description"
        private const val KOREAN_VALUE_LABEL = "QR 값"
        private const val DEFAULT_VALUE_LABEL = "QR value"
        private const val KOREAN_SCAN_DESCRIPTION = "QR 스캔"
        private const val DEFAULT_SCAN_DESCRIPTION = "Scan QR code"
        private const val KOREAN_ADD_DESCRIPTION = "QR 추가"
        private const val DEFAULT_ADD_DESCRIPTION = "Add QR code"
        private val KOREAN_DESCRIPTION_TAB_LIST = listOf("입력", "미리보기")
        private val DEFAULT_DESCRIPTION_TAB_LIST = listOf("Input", "Preview")
        private val KOREAN_VISIBLE_TEXT_LIST = listOf(KOREAN_TITLE, KOREAN_TITLE_LABEL, KOREAN_DESCRIPTION_LABEL, KOREAN_VALUE_LABEL)
    }
}
