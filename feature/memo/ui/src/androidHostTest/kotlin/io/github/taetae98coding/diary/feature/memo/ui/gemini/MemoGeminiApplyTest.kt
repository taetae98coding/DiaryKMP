package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputValue
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.domain.memo.usecase.FetchMemoDraftUseCase
import io.github.taetae98coding.diary.feature.memo.ui.form.MemoFormState
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoAddFormState
import io.kotest.matchers.shouldBe
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoGeminiApplyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-011 제목을 반영하면 제목 입력만 바뀐다`() {
        val state = createFilledFormState()
        val viewModel = createViewModel()

        applyField(field = MemoGeminiField.TITLE, viewModel = viewModel, state = state)

        composeRule.runOnIdle {
            state.titleState.text.toString() shouldBe DRAFT.title
            state.descriptionState.text.toString() shouldBe INITIAL_DESCRIPTION
            state.dateTimeState.value shouldBe null
        }
        verify(exactly = 1) { viewModel.markApplied(MemoGeminiField.TITLE) }
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-011 설명을 반영하면 설명 입력만 바뀐다`() {
        val state = createFilledFormState()
        val viewModel = createViewModel()

        applyField(field = MemoGeminiField.DESCRIPTION, viewModel = viewModel, state = state)

        composeRule.runOnIdle {
            state.descriptionState.text.toString() shouldBe DRAFT.description
            state.titleState.text.toString() shouldBe INITIAL_TITLE
            state.dateTimeState.value shouldBe null
        }
        verify(exactly = 1) { viewModel.markApplied(MemoGeminiField.DESCRIPTION) }
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-013 기간을 반영하면 기간을 사용하는 상태가 된다`() {
        val state = createFilledFormState()
        val viewModel = createViewModel()

        applyField(field = MemoGeminiField.DATE_TIME, viewModel = viewModel, state = state)

        composeRule.runOnIdle {
            state.dateTimeState.hasDateTime shouldBe true
            state.dateTimeState.value shouldBe DiaryDateTimeInputValue.AllDay(dateRange = LocalDate(2026, 9, 21)..LocalDate(2026, 9, 22))
            state.titleState.text.toString() shouldBe INITIAL_TITLE
            state.descriptionState.text.toString() shouldBe INITIAL_DESCRIPTION
        }
        verify(exactly = 1) { viewModel.markApplied(MemoGeminiField.DATE_TIME) }
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-013 시간까지 지정한 기간을 그대로 반영한다`() {
        val dateTime =
            MemoDateTime.DateTime(
                start = LocalDateTime(2026, 9, 21, 9, 30),
                endInclusive = LocalDateTime(2026, 9, 22, 10, 30),
            )
        val state = createFilledFormState()
        val viewModel = createViewModel(draft = DRAFT.copy(dateTime = dateTime))

        applyField(field = MemoGeminiField.DATE_TIME, viewModel = viewModel, state = state)

        composeRule.runOnIdle {
            state.dateTimeState.value shouldBe
                DiaryDateTimeInputValue.DateTime(
                    start = dateTime.start,
                    endInclusive = dateTime.endInclusive,
                )
        }
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-012 반영은 기존 입력을 덮어쓴다`() {
        val state = createFilledFormState()
        val viewModel = createViewModel()

        applyField(field = MemoGeminiField.TITLE, viewModel = viewModel, state = state)
        applyField(field = MemoGeminiField.DESCRIPTION, viewModel = viewModel, state = state)

        composeRule.runOnIdle {
            state.titleState.text.toString() shouldBe DRAFT.title
            state.descriptionState.text.toString() shouldBe DRAFT.description
        }
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-015 입력을 고친 뒤 다시 반영하면 생성된 내용으로 되돌아간다`() {
        val state = createFilledFormState()
        val viewModel = createViewModel()

        applyField(field = MemoGeminiField.TITLE, viewModel = viewModel, state = state)
        composeRule.runOnIdle { state.titleState.setText("사용자가 고친 제목") }
        applyField(field = MemoGeminiField.TITLE, viewModel = viewModel, state = state)

        composeRule.runOnIdle { state.titleState.text.toString() shouldBe DRAFT.title }
        verify(exactly = 2) { viewModel.markApplied(MemoGeminiField.TITLE) }
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-021 반영한 내용은 도우미를 닫아도 남는다`() {
        val state = createFilledFormState()
        val uiState = MutableStateFlow(MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = DRAFT))
        val viewModel = screenTestGeminiViewModel(uiState = uiState)

        applyField(field = MemoGeminiField.TITLE, viewModel = viewModel, state = state)
        composeRule.runOnIdle { uiState.value = MemoGeminiUiState() }

        composeRule.runOnIdle { state.titleState.text.toString() shouldBe DRAFT.title }
    }

    @Test
    fun `TC-MEMO-GEMINI-DOMAIN-004 반영은 컬러를 바꾸지 않는다`() {
        val state = createFilledFormState()
        val viewModel = createViewModel()
        val color = composeRule.runOnIdle { state.colorState.color }

        memoGeminiFieldList.forEach { field -> applyField(field = field, viewModel = viewModel, state = state) }

        composeRule.runOnIdle { state.colorState.color shouldBe color }
    }

    @Test
    fun `생성을 실행하면 지금 입력 중인 내용을 함께 전달한다`() {
        val state = createFilledFormState()
        val viewModel = createViewModel()

        composeRule.runOnIdle {
            handleMemoGeminiEvent(
                event = MemoGeminiDialogEvent.ClickGenerate(prompt = "회고를 써 줘"),
                geminiViewModel = viewModel,
                state = state,
            )
        }

        verify(exactly = 1) {
            viewModel.generate(
                FetchMemoDraftUseCase.Parameter(
                    prompt = "회고를 써 줘",
                    title = INITIAL_TITLE,
                    description = INITIAL_DESCRIPTION,
                    dateTime = null,
                ),
            )
        }
    }

    private fun applyField(
        field: MemoGeminiField,
        viewModel: MemoGeminiViewModel,
        state: MemoFormState,
    ) {
        composeRule.runOnIdle {
            handleMemoGeminiEvent(
                event = MemoGeminiDialogEvent.ClickApply(field = field),
                geminiViewModel = viewModel,
                state = state,
            )
        }
    }

    private fun createFilledFormState(): MemoFormState {
        lateinit var state: MemoFormState

        composeRule.setContent {
            DiaryTheme {
                state = rememberMemoAddFormState()
            }
        }
        composeRule.runOnIdle {
            state.titleState.setText(INITIAL_TITLE)
            state.descriptionState.setText(INITIAL_DESCRIPTION)
        }

        return state
    }

    private companion object {
        private const val INITIAL_TITLE = "기존 제목"
        private const val INITIAL_DESCRIPTION = "기존 설명"

        private val DRAFT =
            MemoDraft(
                title = "주간 회고 정리",
                description = "## 이번 주",
                dateTime = MemoDateTime.AllDay(dateRange = LocalDate(2026, 9, 21)..LocalDate(2026, 9, 22)),
            )

        private fun createViewModel(draft: MemoDraft = DRAFT): MemoGeminiViewModel =
            screenTestGeminiViewModel(
                uiState = MutableStateFlow(MemoGeminiUiState(step = MemoGeminiStep.RESULT, draft = draft)),
            )
    }
}
