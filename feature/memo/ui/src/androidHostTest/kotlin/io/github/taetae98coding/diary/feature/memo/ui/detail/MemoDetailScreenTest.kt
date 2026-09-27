package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.contact.screenTestContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.web.screenTestWebViewModel
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-001 첫진입 시 조회한 제목이 상단 바와 입력 칸에 채워진다`() {
        val uiState = MutableStateFlow<MemoDetailUiState>(MemoDetailUiState.Loading)
        setMemoDetailScreen(screenTestViewModel(uiState = uiState))
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()

        uiState.value = memoDetailUiState(detail = memoDetail(MEMO_TITLE))

        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE))
        composeRule.onAllNodesWithText(MEMO_TITLE).assertCountEquals(2)
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-029 조회할 수 없으면 로딩 상태를 유지한다`() {
        setMemoDetailScreen(screenTestViewModel())

        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithText(REMOVED_DEFAULT_TITLE).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_COPY_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-005 TC-CONTACT-DETAIL-MEMO-FEATURE-021 TC-PLACE-DETAIL-MEMO-FEATURE-021 TC-WEB-DETAIL-MEMO-FEATURE-021 뒤로가기 버튼을 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setMemoDetailScreen(
            viewModel = screenTestViewModel(),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-006 입력 칸을 수정해도 상단 바 제목은 유지된다`() {
        setMemoDetailScreen(titleLoadedViewModel())
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE + EDIT_SUFFIX))
        composeRule.onAllNodesWithText(MEMO_TITLE).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-007 화면 재생성 후에도 수정 중이던 내용이 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val viewModel = titleLoadedViewModel()
        restorationTester.setContent {
            MemoDetailScreenTestTheme {
                MemoDetailScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = viewModel,
                    tagViewModel = screenTestTagViewModel(),
                    webViewModel = screenTestWebViewModel(),
                    contactViewModel = screenTestContactViewModel(),
                    placeViewModel = screenTestPlaceViewModel(),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToCopiedMemo = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE + EDIT_SUFFIX))
        composeRule.onAllNodesWithText(MEMO_TITLE).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-022 화면이 재생성되어도 MemoDetail이 도우미를 닫지 않는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val geminiViewModel = screenTestGeminiViewModel()
        restorationTester.setContent {
            MemoDetailScreenTestTheme {
                MemoDetailScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = titleLoadedViewModel(),
                    tagViewModel = screenTestTagViewModel(),
                    webViewModel = screenTestWebViewModel(),
                    contactViewModel = screenTestContactViewModel(),
                    placeViewModel = screenTestPlaceViewModel(),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = geminiViewModel,
                    navigateUp = {},
                    navigateToCopiedMemo = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { verify(exactly = 0) { geminiViewModel.close() } }
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-023 MemoDetail의 상세 대상이 다른 메모로 바뀌면 도우미를 닫는다`() {
        val uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE)))
        val geminiViewModel = screenTestGeminiViewModel()
        setMemoDetailScreen(screenTestViewModel(uiState), geminiViewModel = geminiViewModel)
        composeRule.waitForIdle()

        uiState.value = memoDetailUiState(id = SECOND_MEMO_ID, detail = memoDetail(SECOND_MEMO_TITLE))
        composeRule.waitForIdle()

        verify(exactly = 1) { geminiViewModel.close() }
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-009 다른 메모를 선택하면 새 메모 내용으로 바뀐다`() {
        val uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE)))
        setMemoDetailScreen(screenTestViewModel(uiState))
        composeRule.waitForIdle()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)

        uiState.value = memoDetailUiState(id = SECOND_MEMO_ID, detail = memoDetail(SECOND_MEMO_TITLE).copy(dateTime = allDayDateTime()))
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(SECOND_MEMO_TITLE))
        composeRule.onAllNodesWithText(SECOND_MEMO_TITLE).assertCountEquals(2)
        composeRule.onNodeWithText(MEMO_TITLE + EDIT_SUFFIX).assertDoesNotExist()
        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNodeWithText(ALL_DAY_START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(ALL_DAY_END_DATE_TEXT).assertExists()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-010 같은 메모 제목 변경은 상단 바에 반영하고 입력값은 유지한다`() {
        val uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE)))
        setMemoDetailScreen(screenTestViewModel(uiState))
        composeRule.waitForIdle()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)

        uiState.value = memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(CHANGED_MEMO_TITLE))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CHANGED_MEMO_TITLE).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-011 수정을 실행해도 수정 중이던 내용이 유지된다`() {
        val viewModel = titleLoadedViewModel()
        every { viewModel.update(any()) } just Runs
        setMemoDetailScreen(viewModel)
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)
        composeRule.onNode(hasRole(Role.Switch)).performScrollTo().performClick()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE + EDIT_SUFFIX))
        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-018 수정 버튼은 입력이 저장 내용과 다를 때만 나타난다`() {
        setMemoDetailScreen(titleLoadedViewModel())
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-013 수정을 처리하는 동안 수정 버튼에 진행 표시가 나타난다`() {
        val uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE), isInProgress = true))
        setMemoDetailScreen(screenTestViewModel(uiState))
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()
    }

    private fun setMemoDetailScreen(
        viewModel: MemoDetailViewModel,
        navigateUp: () -> Unit = {},
        geminiViewModel: MemoGeminiViewModel = screenTestGeminiViewModel(),
    ) {
        composeRule.setContent {
            MemoDetailScreenTestTheme {
                MemoDetailScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = viewModel,
                    tagViewModel = screenTestTagViewModel(),
                    webViewModel = screenTestWebViewModel(),
                    contactViewModel = screenTestContactViewModel(),
                    placeViewModel = screenTestPlaceViewModel(),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = geminiViewModel,
                    navigateUp = navigateUp,
                    navigateToCopiedMemo = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
    }

    public companion object {
        private const val REMOVED_DEFAULT_TITLE = "Memo"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val SECOND_MEMO_TITLE = "SecondMemoDetailTitle"
        private const val CHANGED_MEMO_TITLE = "ChangedMemoDetailTitle"
        private const val ALL_DAY_END_DATE_TEXT = "Jul 28, 2026"
        private val SECOND_MEMO_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000002")

        private fun titleLoadedViewModel(): MemoDetailViewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE))))

        private fun allDayDateTime(): MemoDateTime.AllDay = MemoDateTime.AllDay(dateRange = LocalDate(year = 2026, month = 7, day = 19)..LocalDate(year = 2026, month = 7, day = 28))

        private fun hasRole(role: Role): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScreenDateTimeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-019 종일 기간이 있으면 스위치가 켜진 상태로 종일 기간이 표시된다`() {
        setMemoDetailScreen(loadedViewModel(dateTime = allDayDateTime()))

        composeRule.waitForIdle()

        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNode(isAllDayCheckbox()).assertIsOn()
        composeRule.onNodeWithText(ALL_DAY_START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(ALL_DAY_END_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(MIDNIGHT_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-019 날짜·시간 기간이 있으면 스위치가 켜진 상태로 날짜와 시간이 표시된다`() {
        setMemoDetailScreen(loadedViewModel(dateTime = dateTimeDateTime()))

        composeRule.waitForIdle()

        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNode(isAllDayCheckbox()).assertIsOff()
        composeRule.onNodeWithText(DATE_TIME_START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(START_TIME_TEXT).assertExists()
        composeRule.onNodeWithText(DATE_TIME_END_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(END_TIME_TEXT).assertExists()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-019 기간이 없으면 스위치가 꺼진 상태로 표시된다`() {
        setMemoDetailScreen(loadedViewModel(dateTime = null))

        composeRule.waitForIdle()

        composeRule.onNode(hasRole(Role.Switch)).assertIsOff()
        composeRule.onNode(isAllDayCheckbox()).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_START_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_END_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-020 스위치를 전환해 기간 유무를 바꾸면 수정 버튼이 나타난다`() {
        setMemoDetailScreen(loadedViewModel(dateTime = null))
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()

        composeRule.onNode(hasRole(Role.Switch)).performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-020 종일 여부를 바꾸면 수정 버튼이 나타난다`() {
        setMemoDetailScreen(loadedViewModel(dateTime = allDayDateTime()))
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()

        composeRule.onNode(isAllDayCheckbox()).performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-020 시작 날짜를 바꾸면 수정 버튼이 나타난다`() {
        setMemoDetailScreen(loadedViewModel(dateTime = allDayDateTime()))
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()

        composeRule.onNodeWithText(ALL_DAY_START_DATE_TEXT).performScrollTo().performClick()
        composeRule.onNode(hasText(text = PICK_DAY_CELL_TEXT, substring = true)).performClick()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(PICKED_START_DATE_TEXT).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()
    }

    private fun setMemoDetailScreen(viewModel: MemoDetailViewModel) {
        composeRule.setContent {
            MemoDetailScreenTestTheme {
                MemoDetailScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = viewModel,
                    tagViewModel = screenTestTagViewModel(),
                    webViewModel = screenTestWebViewModel(),
                    contactViewModel = screenTestContactViewModel(),
                    placeViewModel = screenTestPlaceViewModel(),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToCopiedMemo = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
    }

    public companion object {
        private const val DEFAULT_START_LABEL = "Start"
        private const val DEFAULT_END_LABEL = "End"
        private const val DEFAULT_CONFIRM = "OK"
        private const val ALL_DAY_END_DATE_TEXT = "Jul 28, 2026"
        private const val DATE_TIME_START_DATE_TEXT = ALL_DAY_START_DATE_TEXT
        private const val DATE_TIME_END_DATE_TEXT = "Jul 20, 2026"
        private const val START_TIME_TEXT = "1:30 PM"
        private const val END_TIME_TEXT = "9:00 AM"
        private const val MIDNIGHT_TEXT = "12:00 AM"
        private const val PICK_DAY_CELL_TEXT = "July 25, 2026"
        private const val PICKED_START_DATE_TEXT = "Jul 25, 2026"

        private fun loadedViewModel(dateTime: MemoDateTime?): MemoDetailViewModel =
            screenTestViewModel(
                uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE).copy(dateTime = dateTime))),
            )

        private fun allDayDateTime(): MemoDateTime.AllDay = MemoDateTime.AllDay(dateRange = LocalDate(year = 2026, month = 7, day = 19)..LocalDate(year = 2026, month = 7, day = 28))

        private fun isAllDayCheckbox(): SemanticsMatcher = hasRole(Role.Checkbox) and SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription)

        private fun dateTimeDateTime(): MemoDateTime.DateTime =
            MemoDateTime.DateTime(
                start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 13, minute = 30),
                endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 9, minute = 0),
            )

        private fun hasRole(role: Role): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScreenActionTest {
    @get:Rule
    val composeRule = createComposeRule()

    // 스낵바는 닫기 동작을 제공하므로, 닫기 동작을 가진 노드와 알려진 안내 문구가 없으면 안내가 표시되지 않은 것이다.
    private fun assertNoFeedbackShown() {
        composeRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss)).assertCountEquals(0)
        FEEDBACK_MESSAGE_LIST.forEach { message -> composeRule.onNodeWithText(message).assertDoesNotExist() }
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-023 완료되지 않은 메모를 완료하면 안내 없이 완료 버튼이 다시 시작 동작으로 바뀐다`() {
        val uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE), isFinished = false))
        val viewModel = screenTestViewModel(uiState)
        every { viewModel.finish() } answers { uiState.value = uiState.value.copy(isFinished = true) }
        setMemoDetailScreen(viewModel)
        composeRule.waitForIdle()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)

        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE + EDIT_SUFFIX))
        assertNoFeedbackShown()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-024 완료된 메모를 다시 시작하면 안내 없이 완료 버튼이 완료 동작으로 바뀐다`() {
        val uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE), isFinished = true))
        val viewModel = screenTestViewModel(uiState)
        every { viewModel.restart() } answers { uiState.value = uiState.value.copy(isFinished = false) }
        setMemoDetailScreen(viewModel)
        composeRule.waitForIdle()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)

        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE + EDIT_SUFFIX))
        assertNoFeedbackShown()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-073 같은 메모가 다른 경로로 완료되거나 다시 시작되면 완료 동작이 저장된 완료 여부에 맞게 바뀐다`() {
        val uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE), isFinished = false))
        setMemoDetailScreen(screenTestViewModel(uiState))
        composeRule.waitForIdle()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)

        uiState.value = uiState.value.copy(isFinished = true)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertDoesNotExist()

        uiState.value = uiState.value.copy(isFinished = false)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(MEMO_TITLE + EDIT_SUFFIX))
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-026 메모를 삭제하면 뒤로가기와 같은 동작으로 화면에서 빠져나간다`() {
        var navigateUpCount = 0
        val effectChannel = Channel<MemoDetailEffect>(capacity = Channel.BUFFERED)
        val viewModel =
            screenTestViewModel(
                uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE), isFinished = false)),
                effect = effectChannel.receiveAsFlow(),
            )
        every { viewModel.delete() } answers { effectChannel.trySend(MemoDetailEffect.DeleteSucceeded).getOrThrow() }
        setMemoDetailScreen(
            viewModel = viewModel,
            navigateUp = { navigateUpCount += 1 },
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-044 메모를 복사하면 복사본 메모의 상세로 한 번만 전환을 요청한다`() {
        val copiedIdList = mutableListOf<Uuid>()
        val effectChannel = Channel<MemoDetailEffect>(capacity = Channel.BUFFERED)
        val viewModel =
            screenTestViewModel(
                uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE))),
                effect = effectChannel.receiveAsFlow(),
            )
        every { viewModel.copy() } answers { effectChannel.trySend(MemoDetailEffect.CopySucceeded(id = COPIED_MEMO_ID)).getOrThrow() }
        setMemoDetailScreen(
            viewModel = viewModel,
            navigateToCopiedMemo = { id -> copiedIdList.add(id) },
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_COPY_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        copiedIdList shouldBe listOf(COPIED_MEMO_ID)
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-045 메모를 복사하면 복사본 메모의 상세가 받을 수 있게 복사 성공을 전달한다`() {
        val copiedResultList = mutableListOf<MemoDetailCopiedResult>()
        val effectChannel = Channel<MemoDetailEffect>(capacity = Channel.BUFFERED)
        val viewModel =
            screenTestViewModel(
                uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE))),
                effect = effectChannel.receiveAsFlow(),
            )
        every { viewModel.copy() } answers { effectChannel.trySend(MemoDetailEffect.CopySucceeded(id = COPIED_MEMO_ID)).getOrThrow() }
        composeRule.setContent {
            MemoDetailScreenTestTheme {
                MemoDetailScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = viewModel,
                    tagViewModel = screenTestTagViewModel(),
                    webViewModel = screenTestWebViewModel(),
                    contactViewModel = screenTestContactViewModel(),
                    placeViewModel = screenTestPlaceViewModel(),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToCopiedMemo = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                    isStandalone = true,
                )
                ResultEffect<MemoDetailCopiedResult>(
                    resultKey = memoDetailCopiedResultKey(id = COPIED_MEMO_ID),
                ) { result ->
                    copiedResultList.add(result)
                }
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_COPY_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        copiedResultList shouldBe listOf(MemoDetailCopiedResult)
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-046 복사에 실패하면 상세 전환을 요청하지 않고 복사 성공 안내도 표시하지 않는다`() {
        var copiedCount = 0
        val viewModel =
            screenTestViewModel(
                uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE))),
            )
        every { viewModel.copy() } just Runs
        setMemoDetailScreen(
            viewModel = viewModel,
            navigateToCopiedMemo = { copiedCount += 1 },
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_COPY_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        copiedCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_COPY_SUCCEEDED_MESSAGE).assertDoesNotExist()
        composeRule.onAllNodesWithText(MEMO_TITLE).assertCountEquals(2)
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-028 복사를 처리하는 동안 복사 버튼이 이름을 유지한 채 진행 표시로 바뀐다`() {
        val uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE), isCopyInProgress = true))
        setMemoDetailScreen(screenTestViewModel(uiState))

        composeRule.waitForIdle()

        composeRule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate).and(hasAnyAncestor(hasContentDescription(DEFAULT_COPY_BUTTON_DESCRIPTION))),
                useUnmergedTree = true,
            ).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertExists()
    }

    private fun setMemoDetailScreen(
        viewModel: MemoDetailViewModel,
        navigateUp: () -> Unit = {},
        navigateToCopiedMemo: (Uuid) -> Unit = {},
    ) {
        composeRule.setContent {
            MemoDetailScreenTestTheme {
                MemoDetailScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = viewModel,
                    tagViewModel = screenTestTagViewModel(),
                    webViewModel = screenTestWebViewModel(),
                    contactViewModel = screenTestContactViewModel(),
                    placeViewModel = screenTestPlaceViewModel(),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = navigateUp,
                    navigateToCopiedMemo = navigateToCopiedMemo,
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
    }

    public companion object {
        private val COPIED_MEMO_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000003")

        // 이 화면이 보이는 안내와, 목록 화면에서 완료·다시 시작·삭제 때 보이는 안내를 함께 둔다.
        private val FEEDBACK_MESSAGE_LIST: List<String> =
            listOf("Memo updated.", DEFAULT_COPY_SUCCEEDED_MESSAGE, "Memo finished.", "Memo restarted.", "Memo deleted.", "Undo")
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScreenCopiedResultTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-045 기본 환경 복사 성공 안내`() {
        assertCopySucceededMessage(DEFAULT_COPY_SUCCEEDED_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-DETAIL-FEATURE-045 한국어 복사 성공 안내`() {
        assertCopySucceededMessage(KOREAN_COPY_SUCCEEDED_MESSAGE)
    }

    @Test
    fun `복사본이 아닌 메모의 상세에서는 복사 성공 안내를 표시하지 않는다`() {
        val resultEventBus = ResultEventBus()
        resultEventBus.sendResult(
            resultKey = memoDetailCopiedResultKey(id = OTHER_MEMO_ID),
            result = MemoDetailCopiedResult,
        )

        setMemoDetailScreen(resultEventBus = resultEventBus)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_COPY_SUCCEEDED_MESSAGE).assertDoesNotExist()
    }

    private fun assertCopySucceededMessage(expectedMessage: String) {
        val resultEventBus = ResultEventBus()
        resultEventBus.sendResult(
            resultKey = memoDetailCopiedResultKey(id = FIRST_MEMO_ID),
            result = MemoDetailCopiedResult,
        )

        setMemoDetailScreen(resultEventBus = resultEventBus)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(expectedMessage).assertExists()
    }

    private fun setMemoDetailScreen(resultEventBus: ResultEventBus) {
        composeRule.setContent {
            MemoDetailScreenTestTheme(resultEventBus = resultEventBus) {
                MemoDetailScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE)))),
                    tagViewModel = screenTestTagViewModel(),
                    webViewModel = screenTestWebViewModel(),
                    contactViewModel = screenTestContactViewModel(),
                    placeViewModel = screenTestPlaceViewModel(),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToCopiedMemo = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
    }

    public companion object {
        private const val KOREAN_COPY_SUCCEEDED_MESSAGE = "메모가 복사되었습니다."
        private val OTHER_MEMO_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000004")
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScreenMessageTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `기본 환경 수정 버튼 접근성 이름`() {
        assertUpdateButtonDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 수정 버튼 접근성 이름`() {
        assertUpdateButtonDescription(KOREAN_UPDATE_BUTTON_DESCRIPTION)
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-012 기본 환경 수정 성공 안내`() {
        assertMessage(effect = MemoDetailEffect.UpdateSucceeded, expectedMessage = DEFAULT_UPDATE_SUCCEEDED_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-DETAIL-FEATURE-012 한국어 수정 성공 안내`() {
        assertMessage(effect = MemoDetailEffect.UpdateSucceeded, expectedMessage = KOREAN_UPDATE_SUCCEEDED_MESSAGE)
    }

    @Test
    fun `기본 환경 복사 버튼 접근성 이름`() {
        assertCopyButtonDescription(DEFAULT_COPY_BUTTON_DESCRIPTION)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 복사 버튼 접근성 이름`() {
        assertCopyButtonDescription(KOREAN_COPY_BUTTON_DESCRIPTION)
    }

    private fun assertUpdateButtonDescription(expectedDescription: String) {
        setMemoDetailScreen()
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(expectedDescription).assertExists()
    }

    private fun assertCopyButtonDescription(expectedDescription: String) {
        setMemoDetailScreen()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(expectedDescription).assertExists()
    }

    private fun setMemoDetailScreen() {
        composeRule.setContent {
            MemoDetailScreenTestTheme {
                MemoDetailScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = titleLoadedViewModel(),
                    tagViewModel = screenTestTagViewModel(),
                    webViewModel = screenTestWebViewModel(),
                    contactViewModel = screenTestContactViewModel(),
                    placeViewModel = screenTestPlaceViewModel(),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToCopiedMemo = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
    }

    private fun assertMessage(
        effect: MemoDetailEffect,
        expectedMessage: String,
    ) {
        val effectChannel = Channel<MemoDetailEffect>(capacity = Channel.BUFFERED)
        val viewModel =
            screenTestViewModel(
                uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE))),
                effect = effectChannel.receiveAsFlow(),
            )
        every { viewModel.update(any()) } answers { effectChannel.trySend(effect).getOrThrow() }
        composeRule.setContent {
            MemoDetailScreenTestTheme {
                MemoDetailScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = viewModel,
                    tagViewModel = screenTestTagViewModel(),
                    webViewModel = screenTestWebViewModel(),
                    contactViewModel = screenTestContactViewModel(),
                    placeViewModel = screenTestPlaceViewModel(),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToCopiedMemo = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(EDIT_SUFFIX)
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(expectedMessage).assertExists()
    }

    public companion object {
        private const val KOREAN_UPDATE_BUTTON_DESCRIPTION = "메모 수정"
        private const val KOREAN_COPY_BUTTON_DESCRIPTION = "메모 복사"
        private const val DEFAULT_UPDATE_SUCCEEDED_MESSAGE = "Memo updated."
        private const val KOREAN_UPDATE_SUCCEEDED_MESSAGE = "메모가 수정되었습니다."

        private fun titleLoadedViewModel(): MemoDetailViewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE))))
    }
}
