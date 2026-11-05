package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.core.model.memo.MemoDraftProgress
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
    fun `TC-634c4200-23c0-46b4-93ed-0627f341002f 프롬프트가 비어 있어도 생성을 실행할 수 있다`() {
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
    fun `TC-52cc9cbb-b5c0-4819-94ef-2f5838293b74 줄바꿈이 여러 번 들어 있는 긴 프롬프트도 그대로 생성을 실행한다`() {
        val longPrompt = List(LONG_PROMPT_LINE_COUNT) { index -> "$index 번째 줄 ${"가".repeat(LONG_PROMPT_LINE_LENGTH)}" }.joinToString(separator = "\n")
        val promptList = mutableListOf<String>()
        setDialog(
            uiState = MemoGeminiUiState(step = MemoGeminiStep.PROMPT),
            onEvent = { event -> if (event is MemoGeminiDialogEvent.ClickGenerate) promptList += event.prompt },
        )

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(longPrompt)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_GENERATE_ACTION).performClick()
        composeRule.waitForIdle()

        promptList shouldBe listOf(longPrompt)
    }

    @Test
    fun `TC-b1c391bd-de35-495f-8346-0a07ef6f7fe6 생성하는 동안 진행 표시와 취소를 보여 준다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.GENERATING))

        composeRule.onAllNodesWithContentDescription(DEFAULT_GENERATING_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_CANCEL_ACTION).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_GENERATE_ACTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_PROMPT_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-b1c391bd-de35-495f-8346-0a07ef6f7fe6 생성 중에 취소를 알린다`() {
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
    fun `TC-7e318c4b-ead9-4441-9f6e-fab7d4ed4ae2 생성된 제목, 설명, 날짜·시간을 각각 반영할 수 있다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = DRAFT))
        awaitText(DRAFT.description)

        composeRule.onNodeWithText(DRAFT.title).assertExists()
        composeRule.onNodeWithText(DRAFT.description).assertExists()
        composeRule.onNodeWithText(ALL_DAY_PERIOD_TEXT).assertExists()

        listOf(DEFAULT_TITLE_LABEL, DEFAULT_DESCRIPTION_LABEL, DEFAULT_DATE_TIME_LABEL).forEach { label ->
            composeRule.onNodeWithContentDescription(applyDescription(label)).assert(hasClickAction())
        }
    }

    @Test
    fun `TC-765ebb0c-9c5c-40c2-8688-08385b765be1 반영하면 어느 결과를 반영했는지 알린다`() {
        val fieldList = mutableListOf<MemoGeminiField>()
        setDialog(
            uiState = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = DRAFT),
            onEvent = { event -> if (event is MemoGeminiDialogEvent.ClickApply) fieldList += event.field },
        )

        composeRule.onNodeWithContentDescription(applyDescription(DEFAULT_DESCRIPTION_LABEL)).performClick()
        composeRule.waitForIdle()

        fieldList shouldBe listOf(MemoGeminiField.DESCRIPTION)
    }

    @Test
    fun `TC-ea461a30-9ffb-45e4-80d6-2bef834efda8 반영한 결과에만 반영했다는 표시를 보인다`() {
        setDialog(
            uiState =
                MemoGeminiUiState(
                    step = MemoGeminiStep.RESULT,
                    draft = DRAFT,
                    appliedFieldSet = setOf(MemoGeminiField.TITLE),
                ),
        )

        composeRule.onAllNodesWithContentDescription(DEFAULT_APPLIED_DESCRIPTION).fetchSemanticsNodes().size shouldBe 1
        composeRule
            .onNode(hasContentDescription(DEFAULT_APPLIED_DESCRIPTION) and hasAnySibling(hasText(DEFAULT_TITLE_LABEL)))
            .assertExists()
    }

    @Test
    fun `TC-048257f1-7281-4c7f-8ef3-20c08483899e 반영한 뒤에도 결과와 모든 반영 동작을 유지한다`() {
        setDialog(
            uiState =
                MemoGeminiUiState(
                    step = MemoGeminiStep.RESULT,
                    draft = DRAFT,
                    appliedFieldSet = setOf(MemoGeminiField.TITLE),
                ),
        )
        awaitText(DRAFT.description)

        composeRule.onNodeWithText(DRAFT.title).assertExists()
        composeRule.onNodeWithText(ALL_DAY_PERIOD_TEXT).assertExists()
        listOf(DEFAULT_TITLE_LABEL, DEFAULT_DESCRIPTION_LABEL, DEFAULT_DATE_TIME_LABEL).forEach { label ->
            composeRule.onNodeWithContentDescription(applyDescription(label)).assert(hasClickAction())
        }
    }

    @Test
    fun `TC-3f6d23cc-4f42-41fd-8e46-754b028658fd 화면이 재생성되어도 진행 중인 생성 표시를 유지한다`() {
        setRestorableDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.GENERATING)).emulateSavedInstanceStateRestore()

        composeRule.onAllNodesWithContentDescription(DEFAULT_GENERATING_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_CANCEL_ACTION).assert(hasClickAction())
    }

    @Test
    fun `TC-3f6d23cc-4f42-41fd-8e46-754b028658fd 화면이 재생성되어도 생성 중에 받은 내용을 유지한다`() {
        val progress = MemoDraftProgress(title = DRAFT.title, description = DRAFT.description)
        setRestorableDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.GENERATING, progress = progress)).emulateSavedInstanceStateRestore()
        awaitText(progress.description)

        composeRule.onNodeWithText(DEFAULT_GENERATING_MESSAGE).assertExists()
        composeRule.onNodeWithText(progress.title).assertExists()
        composeRule.onNodeWithText(DEFAULT_CANCEL_ACTION).assert(hasClickAction())
    }

    @Test
    fun `TC-3f6d23cc-4f42-41fd-8e46-754b028658fd 화면이 재생성되어도 받아 둔 결과를 유지한다`() {
        setRestorableDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = DRAFT)).emulateSavedInstanceStateRestore()
        awaitText(DRAFT.description)

        composeRule.onNodeWithText(DRAFT.title).assertExists()
        composeRule.onNodeWithText(ALL_DAY_PERIOD_TEXT).assertExists()
        listOf(DEFAULT_TITLE_LABEL, DEFAULT_DESCRIPTION_LABEL, DEFAULT_DATE_TIME_LABEL).forEach { label ->
            composeRule.onNodeWithContentDescription(applyDescription(label)).assert(hasClickAction())
        }
    }

    @Test
    fun `TC-6c6f219a-1cbd-404d-8cbb-f0e6d438c90a 비어 있는 결과는 반영할 수 없고 채워진 결과만 반영할 수 있다`() {
        val dateTime = MemoDateTime.AllDay(dateRange = LocalDate(2026, 9, 21)..LocalDate(2026, 9, 22))
        val uiState = mutableStateOf(MemoGeminiUiState(step = MemoGeminiStep.RESULT))
        composeRule.setContent {
            DiaryTheme {
                MemoGeminiDialogHost(
                    onEvent = {},
                    onDismissRequest = {},
                    uiStateProvider = { uiState.value },
                )
            }
        }

        listOf(
            MemoDraft(title = "제목만 있는 결과", description = "", dateTime = null) to DEFAULT_TITLE_LABEL,
            MemoDraft(title = "", description = "설명만 있는 결과", dateTime = null) to DEFAULT_DESCRIPTION_LABEL,
            MemoDraft(title = "", description = "", dateTime = dateTime) to DEFAULT_DATE_TIME_LABEL,
        ).forEach { (draft, filledLabel) ->
            composeRule.runOnIdle { uiState.value = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = draft) }
            composeRule.waitForIdle()
            if (draft.description.isNotEmpty()) awaitText(draft.description)

            listOf(DEFAULT_TITLE_LABEL, DEFAULT_DESCRIPTION_LABEL, DEFAULT_DATE_TIME_LABEL).forEach { label ->
                if (label == filledLabel) {
                    composeRule.onNodeWithContentDescription(applyDescription(label)).assert(hasClickAction())
                } else {
                    composeRule.onNodeWithContentDescription(applyDescription(label)).assertDoesNotExist()
                }
            }
        }
    }

    @Test
    fun `TC-75b0f674-af63-4ec0-b280-668fdbca6b45 생성된 내용이 없으면 안내만 표시한다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = MemoDraft.EMPTY))

        composeRule.onNodeWithText(DEFAULT_EMPTY_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_APPLY_ACTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_CLOSE_ACTION).assert(hasClickAction())
    }

    @Test
    fun `TC-b74c11e5-3a4a-4927-b652-39ea209f6bfc 결과 확인 상태에서는 다시 생성할 수 없다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = DRAFT))

        composeRule.onNodeWithText(DEFAULT_GENERATE_ACTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_PROMPT_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-0725149c-6215-42e7-b736-99bcc3c1b1fb 결과 확인 상태에서 닫기를 제공한다`() {
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
    fun `프롬프트 입력과 생성 중에는 상태의 동작과 함께 닫기 버튼으로 닫는다`() {
        var dismissCount = 0
        val events = mutableListOf<MemoGeminiDialogEvent>()
        val uiState = mutableStateOf(MemoGeminiUiState(step = MemoGeminiStep.PROMPT))
        composeRule.setContent {
            DiaryTheme {
                MemoGeminiDialogHost(
                    onEvent = events::add,
                    onDismissRequest = { dismissCount += 1 },
                    uiStateProvider = { uiState.value },
                )
            }
        }

        listOf(
            MemoGeminiStep.PROMPT to DEFAULT_GENERATE_ACTION,
            MemoGeminiStep.GENERATING to DEFAULT_CANCEL_ACTION,
        ).forEachIndexed { index, (step, stepAction) ->
            composeRule.runOnIdle { uiState.value = MemoGeminiUiState(step = step) }
            composeRule.waitForIdle()

            composeRule.onNodeWithText(stepAction).assertExists()
            composeRule.onNodeWithText(DEFAULT_CLOSE_ACTION).performClick()
            composeRule.waitForIdle()

            dismissCount shouldBe index + 1
            events shouldBe emptyList()
        }
    }

    @Test
    fun `결과 확인 상태에는 닫기 버튼을 하나만 둔다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = DRAFT))

        composeRule.onAllNodesWithText(DEFAULT_CLOSE_ACTION).assertCountEquals(1)
    }

    @Test
    fun `TC-37fa34eb-5306-4a2d-af56-d0e3f60b7c7c 기본 환경에서 실패 이유마다 안내를 구분해 보여 준다`() {
        assertFailureMessageList(messageMap = DEFAULT_FAILURE_MESSAGE_MAP)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-37fa34eb-5306-4a2d-af56-d0e3f60b7c7c 한국어 환경에서 실패 이유마다 안내를 구분해 보여 준다`() {
        assertFailureMessageList(messageMap = KOREAN_FAILURE_MESSAGE_MAP)
    }

    @Test
    fun `TC-da9bb546-e1b1-4d67-a91e-42cf5e0ae8c3 그 밖의 실패에서 Gemini의 설명을 안내 아래에 그대로 보여 준다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.PROMPT, failure = MemoGeminiFailure.UNKNOWN, failureDescription = GEMINI_DESCRIPTION))

        composeRule.onNodeWithText(DEFAULT_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(GEMINI_DESCRIPTION).assertExists()
    }

    @Test
    fun `이유가 정해진 실패에서는 Gemini의 설명을 보여 주지 않는다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.PROMPT, failure = MemoGeminiFailure.SERVER, failureDescription = GEMINI_DESCRIPTION))

        composeRule.onNodeWithText(GEMINI_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-11c3ed9d-2f48-41c6-8682-ddb76b0b5379 생성하는 동안 받은 제목과 설명을 진행 표시와 함께 보여 주고 늘려 간다`() {
        val uiState = mutableStateOf(MemoGeminiUiState(step = MemoGeminiStep.GENERATING))
        composeRule.setContent {
            DiaryTheme {
                MemoGeminiDialogHost(
                    onEvent = {},
                    onDismissRequest = {},
                    uiStateProvider = { uiState.value },
                )
            }
        }

        listOf(
            MemoDraftProgress(title = "주간", description = ""),
            MemoDraftProgress(title = "주간 회고", description = "이번"),
            MemoDraftProgress(title = "주간 회고", description = "이번 주 정리"),
        ).forEach { progress ->
            composeRule.runOnIdle { uiState.value = MemoGeminiUiState(step = MemoGeminiStep.GENERATING, progress = progress) }

            composeRule.onNodeWithText(DEFAULT_GENERATING_MESSAGE).assertExists()
            composeRule.onNodeWithText(progress.title).assertExists()
            if (progress.description.isNotEmpty()) awaitText(progress.description)
            composeRule.onNodeWithText(DEFAULT_DATE_TIME_LABEL).assertDoesNotExist()
        }
    }

    @Test
    fun `TC-7a48327c-38ad-4375-9ae4-b0da177b8179 생성 중에 보이는 내용은 반영할 수 없다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.GENERATING, progress = MemoDraftProgress(title = DRAFT.title, description = DRAFT.description)))
        awaitText(DRAFT.description)

        composeRule.onAllNodesWithText(DEFAULT_APPLY_ACTION).assertCountEquals(0)
        listOf(DEFAULT_TITLE_LABEL, DEFAULT_DESCRIPTION_LABEL).forEach { label ->
            composeRule.onNodeWithContentDescription(applyDescription(label)).assertDoesNotExist()
        }
        composeRule.onNodeWithText(DEFAULT_CANCEL_ACTION).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_CLOSE_ACTION).assert(hasClickAction())
    }

    @Test
    fun `실패 안내가 없으면 표시하지 않는다`() {
        setDialog(uiState = MemoGeminiUiState(step = MemoGeminiStep.PROMPT))

        composeRule.onNodeWithText(DEFAULT_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_INVALID_API_KEY_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-60a1289f-81d1-4815-84b4-c4ba8fe94210 화면이 재생성되어도 적어 둔 프롬프트를 유지한다`() {
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

    private fun applyDescription(label: String): String = "$DEFAULT_APPLY_ACTION $label"

    // 설명은 마크다운 해석을 거쳐 표시되고 그 해석은 Compose가 대기하지 않는 별도 디스패처에서 끝나므로 표시될 때까지 기다린다.
    private fun awaitText(text: String) {
        composeRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    // 도우미의 단계와 결과는 화면 재생성과 함께 유지되는 곳이 보관하므로, 재생성 뒤에도 같은 상태를 넘긴다.
    private fun setRestorableDialog(uiState: MemoGeminiUiState): StateRestorationTester {
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                MemoGeminiDialogHost(
                    onEvent = {},
                    onDismissRequest = {},
                    uiStateProvider = { uiState },
                )
            }
        }

        return restorationTester
    }

    // 화면이 한 번만 구성되므로, 실패 이유를 바꿔 가며 같은 다이얼로그에서 안내를 확인한다.
    private fun assertFailureMessageList(messageMap: Map<MemoGeminiFailure, String>) {
        messageMap.keys shouldBe MemoGeminiFailure.entries.toSet()
        val uiState = mutableStateOf(MemoGeminiUiState(step = MemoGeminiStep.PROMPT))
        composeRule.setContent {
            DiaryTheme {
                MemoGeminiDialogHost(
                    onEvent = {},
                    onDismissRequest = {},
                    uiStateProvider = { uiState.value },
                )
            }
        }

        messageMap.forEach { (failure, message) ->
            composeRule.runOnIdle { uiState.value = MemoGeminiUiState(step = MemoGeminiStep.PROMPT, failure = failure) }

            composeRule.onNodeWithText(message).assertExists()
            (messageMap.values - message).forEach { other -> composeRule.onNodeWithText(other).assertDoesNotExist() }
        }
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
        private const val WAIT_TIMEOUT_MILLIS = 5_000L
        private const val LONG_PROMPT_LINE_COUNT = 50
        private const val LONG_PROMPT_LINE_LENGTH = 40
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

        private const val GEMINI_DESCRIPTION = "Request contains an invalid argument."

        private val DEFAULT_FAILURE_MESSAGE_MAP: Map<MemoGeminiFailure, String> =
            mapOf(
                MemoGeminiFailure.INVALID_API_KEY to DEFAULT_INVALID_API_KEY_MESSAGE,
                MemoGeminiFailure.QUOTA_EXCEEDED to "Request limit reached. Try again later",
                MemoGeminiFailure.MODEL_NOT_FOUND to "Model not found. Choose a model again in Gemini settings",
                MemoGeminiFailure.BLOCKED to "Blocked by Gemini's safety policy. Edit your prompt",
                MemoGeminiFailure.TRUNCATED to "The response was too long to finish",
                MemoGeminiFailure.TIMEOUT to "No response. Try again later",
                MemoGeminiFailure.NETWORK to "Can't connect to the network",
                MemoGeminiFailure.SERVER to "Gemini is having trouble. Try again later",
                MemoGeminiFailure.UNKNOWN to DEFAULT_FAILED_MESSAGE,
            )

        private val KOREAN_FAILURE_MESSAGE_MAP: Map<MemoGeminiFailure, String> =
            mapOf(
                MemoGeminiFailure.INVALID_API_KEY to "API 키를 확인해 주세요",
                MemoGeminiFailure.QUOTA_EXCEEDED to "요청 한도를 넘었습니다. 잠시 뒤 다시 시도해 주세요",
                MemoGeminiFailure.MODEL_NOT_FOUND to "모델을 찾을 수 없습니다. Gemini 설정에서 모델을 다시 골라 주세요",
                MemoGeminiFailure.BLOCKED to "Gemini 안전 정책에 따라 생성하지 않았습니다. 프롬프트를 고쳐 주세요",
                MemoGeminiFailure.TRUNCATED to "내용이 너무 길어 끝까지 만들지 못했습니다",
                MemoGeminiFailure.TIMEOUT to "응답이 오지 않습니다. 잠시 뒤 다시 시도해 주세요",
                MemoGeminiFailure.NETWORK to "네트워크에 연결할 수 없습니다",
                MemoGeminiFailure.SERVER to "Gemini 서버에 문제가 있습니다. 잠시 뒤 다시 시도해 주세요",
                MemoGeminiFailure.UNKNOWN to "생성하지 못했습니다",
            )

        private val DRAFT =
            MemoDraft(
                title = "주간 회고 정리",
                description = "이번 주 회고",
                dateTime = MemoDateTime.AllDay(dateRange = LocalDate(2026, 9, 21)..LocalDate(2026, 9, 22)),
            )
    }
}
