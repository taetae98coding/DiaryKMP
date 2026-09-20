package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoGeminiDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-004 프롬프트가 비어 있어도 생성을 실행할 수 있다`() {
        val promptList = mutableListOf<String>()
        setDialog(
            uiState = MemoGeminiUiState(step = MemoGeminiStep.PROMPT),
            onEvent = { event -> if (event is MemoGeminiDialogEvent.ClickGenerate) promptList += event.prompt },
        )

        composeRule.onNodeWithText(DEFAULT_GENERATE_ACTION).performClick()
        composeRule.waitForIdle()

        promptList shouldBe listOf("")
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-005 생성하는 동안 진행 표시와 취소를 보여 준다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.GENERATING))

        composeRule.onAllNodesWithContentDescription(DEFAULT_GENERATING_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_CANCEL_ACTION).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_GENERATE_ACTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_PROMPT_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-005 생성 중에 취소를 알린다`() {
        var cancelCount = 0
        setDialog(
            uiState = MemoGeminiUiState(step = MemoGeminiStep.GENERATING),
            onEvent = { event -> if (event is MemoGeminiDialogEvent.ClickCancel) cancelCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_CANCEL_ACTION).performClick()
        composeRule.waitForIdle()

        cancelCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-007 생성된 제목, 설명, 날짜·시간을 각각 반영할 수 있다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = DRAFT))

        composeRule.onNodeWithText(DRAFT.title).assertExists()
        composeRule.onNodeWithText(DRAFT.description).assertExists()
        composeRule.onNodeWithText(ALL_DAY_PERIOD_TEXT).assertExists()

        listOf(DEFAULT_TITLE_LABEL, DEFAULT_DESCRIPTION_LABEL, DEFAULT_DATE_TIME_LABEL).forEach { label ->
            composeRule.onNodeWithContentDescription("Apply $label").assert(hasClickAction())
        }
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-011 반영하면 어느 결과를 반영했는지 알린다`() {
        val fieldList = mutableListOf<MemoGeminiField>()
        setDialog(
            uiState = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = DRAFT),
            onEvent = { event -> if (event is MemoGeminiDialogEvent.ClickApply) fieldList += event.field },
        )

        composeRule.onNodeWithContentDescription("Apply $DEFAULT_DESCRIPTION_LABEL").performClick()
        composeRule.waitForIdle()

        fieldList shouldBe listOf(MemoGeminiField.DESCRIPTION)
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-014 반영한 결과에 반영됨을 표시하고 반영 동작을 유지한다`() {
        setDialog(
            uiState =
                MemoGeminiUiState(
                    step = MemoGeminiStep.RESULT,
                    draft = DRAFT,
                    appliedFieldSet = setOf(MemoGeminiField.TITLE),
                ),
        )

        composeRule.onAllNodesWithContentDescription(DEFAULT_APPLIED_DESCRIPTION).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithContentDescription("Apply $DEFAULT_TITLE_LABEL").assert(hasClickAction())
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-016 비어 있는 결과는 반영할 수 없다`() {
        setDialog(
            uiState =
                MemoGeminiUiState(
                    step = MemoGeminiStep.RESULT,
                    draft = MemoDraft(title = "제목만 있는 결과", description = "", dateTime = null),
                ),
        )

        composeRule.onNodeWithContentDescription("Apply $DEFAULT_TITLE_LABEL").assert(hasClickAction())
        composeRule.onNodeWithContentDescription("Apply $DEFAULT_DESCRIPTION_LABEL").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Apply $DEFAULT_DATE_TIME_LABEL").assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-017 생성된 내용이 없으면 안내만 표시한다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = MemoDraft.EMPTY))

        composeRule.onNodeWithText(DEFAULT_EMPTY_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_APPLY_ACTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_CLOSE_ACTION).assert(hasClickAction())
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-018 결과 확인 상태에서는 다시 생성할 수 없다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = DRAFT))

        composeRule.onNodeWithText(DEFAULT_GENERATE_ACTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_PROMPT_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-019 결과 확인 상태에서 닫기를 제공한다`() {
        var dismissCount = 0
        setDialog(
            uiState = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = DRAFT),
            onDismissRequest = { dismissCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_CLOSE_ACTION).performClick()
        composeRule.waitForIdle()

        dismissCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-010 생성 실패의 원인을 구분해 안내한다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.PROMPT, failure = MemoGeminiFailure.INVALID_API_KEY))

        composeRule.onNodeWithText(DEFAULT_INVALID_API_KEY_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_GENERATE_ACTION).assert(hasClickAction())
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-010 그 밖의 실패는 생성 실패를 안내한다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.PROMPT, failure = MemoGeminiFailure.UNKNOWN))

        composeRule.onNodeWithText(DEFAULT_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_INVALID_API_KEY_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `실패 안내가 없으면 표시하지 않는다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.PROMPT))

        composeRule.onNodeWithText(DEFAULT_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_INVALID_API_KEY_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-022 화면이 재생성되어도 적어 둔 프롬프트를 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                MemoGeminiDialogHost(
                    onEvent = {},
                    onDismissRequest = {},
                    uiStateProvider = { MemoGeminiUiState(step = MemoGeminiStep.PROMPT) },
                )
            }
        }

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("회고를 써 줘")
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText("회고를 써 줘").assertExists()
    }

    @Test
    fun `시간까지 지정한 기간은 시각까지 표시한다`() {
        val draft =
            DRAFT.copy(
                dateTime =
                    MemoDateTime.DateTime(
                        start = LocalDateTime(2026, 9, 21, 9, 30),
                        endInclusive = LocalDateTime(2026, 9, 21, 10, 30),
                    ),
            )
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = draft))

        composeRule.onNodeWithText("Sep 21, 2026 9:30 AM – Sep 21, 2026 10:30 AM").assertExists()
    }

    private fun setDialog(
        uiState: MemoGeminiUiState,
        onEvent: (MemoGeminiDialogEvent) -> Unit = {},
        onDismissRequest: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                MemoGeminiDialogHost(
                    onEvent = onEvent,
                    onDismissRequest = onDismissRequest,
                    uiStateProvider = { uiState },
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_PROMPT_LABEL = "Prompt"
        private const val DEFAULT_GENERATE_ACTION = "Generate"
        private const val DEFAULT_CANCEL_ACTION = "Cancel"
        private const val DEFAULT_CLOSE_ACTION = "Close"
        private const val DEFAULT_APPLY_ACTION = "Apply"
        private const val DEFAULT_GENERATING_MESSAGE = "Generating"
        private const val DEFAULT_INVALID_API_KEY_MESSAGE = "Check your API key"
        private const val DEFAULT_FAILED_MESSAGE = "Couldn't generate"
        private const val DEFAULT_EMPTY_MESSAGE = "Nothing was generated"
        private const val DEFAULT_APPLIED_DESCRIPTION = "Applied"
        private const val DEFAULT_TITLE_LABEL = "Title"
        private const val DEFAULT_DESCRIPTION_LABEL = "Description"
        private const val DEFAULT_DATE_TIME_LABEL = "Date & time"
        private const val ALL_DAY_PERIOD_TEXT = "All day Sep 21, 2026 – Sep 22, 2026"

        private val DRAFT =
            MemoDraft(
                title = "주간 회고 정리",
                description = "이번 주 회고",
                dateTime = MemoDateTime.AllDay(dateRange = LocalDate(2026, 9, 21)..LocalDate(2026, 9, 22)),
            )
    }
}
