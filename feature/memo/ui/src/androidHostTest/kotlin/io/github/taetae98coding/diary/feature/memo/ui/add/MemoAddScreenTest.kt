package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoAddScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-ADD-FEATURE-052 진입하면 제목 입력 칸에 자동으로 초점이 맞춰진다`() {
        setMemoAddScreen(screenTestViewModel())

        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assertIsFocused()
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-005 뒤로가기 버튼을 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setMemoAddScreen(
            viewModels = screenTestViewModel(),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-006 추가 버튼으로 성공하면 입력 칸을 비우고 다시 초점을 맞춘다`() {
        assertSuccessClearsInput {
            composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        }
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-006 단축키로 성공하면 입력 칸을 비우고 다시 초점을 맞춘다`() {
        assertSuccessClearsInput {
            composeRule.onAllNodes(hasSetTextAction()).onFirst().performKeyInput {
                keyDown(Key.MetaLeft)
                keyDown(Key.Enter)
                keyUp(Key.Enter)
                keyUp(Key.MetaLeft)
            }
        }
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-009 제목이 비어 있으면 입력 칸을 비운 채 다시 초점을 맞춘다`() {
        assertTitleBlankRetainsInput(initialInput = "")
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-009 공백만 입력하면 입력 내용을 유지한 채 다시 초점을 맞춘다`() {
        assertTitleBlankRetainsInput(initialInput = WHITESPACE_TITLE)
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-053 추가에 성공하면 제목 입력으로 초점을 옮긴다`() {
        assertFocusMovesToTitle(effect = MemoAddEffect.AddSucceeded)
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-054 제목 미입력으로 추가하면 제목 입력으로 초점을 옮긴다`() {
        assertFocusMovesToTitle(effect = MemoAddEffect.TitleBlank)
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-011 진입하면 날짜·시간 입력이 스위치가 꺼진 상태로 표시된다`() {
        setMemoAddScreen(screenTestViewModel())

        composeRule.waitForIdle()

        composeRule.onNode(hasRole(Role.Switch)).assertIsOff()
        composeRule.onNode(hasRole(Role.Checkbox)).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_START_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_END_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-013 추가에 성공해도 날짜·시간 입력의 선택 내용이 유지된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toDefaultDisplayText()
        val effect = Channel<MemoAddEffect>(capacity = Channel.BUFFERED)
        val viewModels = screenTestViewModel(effect = effect.receiveAsFlow())
        every { viewModels.viewModel.add(detail = any(), tagSelection = any(), webIdSet = any(), contactIdSet = any(), placeIdSet = any()) } answers { effect.trySend(MemoAddEffect.AddSucceeded).getOrThrow() }
        setMemoAddScreen(viewModels)

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.onNode(hasRole(Role.Switch)).performScrollTo().performClick()
        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNode(hasRole(Role.Checkbox)).assertIsOn()
        composeRule.onAllNodesWithText(today).assertCountEquals(2)
    }

    private fun assertFocusMovesToTitle(effect: MemoAddEffect) {
        val effectChannel = Channel<MemoAddEffect>(capacity = Channel.BUFFERED)
        val viewModels = screenTestViewModel(effect = effectChannel.receiveAsFlow())
        every { viewModels.viewModel.add(detail = any(), tagSelection = any(), webIdSet = any(), contactIdSet = any(), placeIdSet = any()) } answers { effectChannel.trySend(effect).getOrThrow() }
        setMemoAddScreen(viewModels)

        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput(TYPED_DESCRIPTION)
        composeRule.onAllNodes(hasSetTextAction())[1].assertIsFocused()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assertIsFocused()
        composeRule.onAllNodes(hasSetTextAction())[1].assertIsNotFocused()
    }

    private fun assertSuccessClearsInput(triggerAdd: () -> Unit) {
        var navigateUpCount = 0
        val effect = Channel<MemoAddEffect>(capacity = Channel.BUFFERED)
        val viewModels = screenTestViewModel(effect = effect.receiveAsFlow())
        every { viewModels.viewModel.add(detail = any(), tagSelection = any(), webIdSet = any(), contactIdSet = any(), placeIdSet = any()) } answers { effect.trySend(MemoAddEffect.AddSucceeded).getOrThrow() }
        setMemoAddScreen(
            viewModels = viewModels,
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput(TYPED_DESCRIPTION)
        composeRule.onNodeWithText(TYPED_TITLE).assertExists()
        composeRule.onAllNodes(hasSetTextAction())[1].assert(hasText(TYPED_DESCRIPTION))

        triggerAdd()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModels.viewModel.add(detail = any(), tagSelection = any(), webIdSet = any(), contactIdSet = any(), placeIdSet = any()) }
        composeRule.onNodeWithText(TYPED_TITLE).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction())[1].assert(hasText(""))
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assertIsFocused()
        navigateUpCount shouldBe 0
    }

    private fun assertTitleBlankRetainsInput(initialInput: String) {
        var navigateUpCount = 0
        val effect = Channel<MemoAddEffect>(capacity = Channel.BUFFERED)
        val viewModels = screenTestViewModel(effect = effect.receiveAsFlow())
        every { viewModels.viewModel.add(detail = any(), tagSelection = any(), webIdSet = any(), contactIdSet = any(), placeIdSet = any()) } answers { effect.trySend(MemoAddEffect.TitleBlank).getOrThrow() }
        setMemoAddScreen(
            viewModels = viewModels,
            navigateUp = { navigateUpCount += 1 },
        )

        if (initialInput.isNotEmpty()) {
            composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(initialInput)
        }

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModels.viewModel.add(detail = any(), tagSelection = any(), webIdSet = any(), contactIdSet = any(), placeIdSet = any()) }
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assertIsFocused()
        if (initialInput.isNotEmpty()) {
            composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(initialInput))
        }
        navigateUpCount shouldBe 0
    }

    private fun setMemoAddScreen(
        viewModels: MemoAddScreenViewModels,
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            MemoAddScreenTestTheme {
                MemoAddScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    addViewModel = viewModels.viewModel,
                    tagViewModel = viewModels.tagViewModel,
                    webViewModel = viewModels.webViewModel,
                    contactViewModel = viewModels.contactViewModel,
                    placeViewModel = viewModels.placeViewModel,
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = navigateUp,
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    initialDateRange = null,
                    componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
    }

    public companion object {
        private const val TYPED_TITLE = "MemoTitleInput"
        private const val TYPED_DESCRIPTION = "MemoDescriptionInput"
        private const val WHITESPACE_TITLE = "   "
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add memo"
        private const val DEFAULT_START_LABEL = "Start"
        private const val DEFAULT_END_LABEL = "End"

        private fun hasRole(role: Role): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoAddScreenInitialDateTimeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-ADD-FEATURE-047 캘린더 홈에서 기간을 선택해 진입하면 그 기간이 종일 기간으로 입력되어 있다`() {
        val start = LocalDate(year = 2026, month = Month.JULY, day = 14)
        val endInclusive = LocalDate(year = 2026, month = Month.JULY, day = 17)
        composeRule.setContent {
            MemoAddScreenTestTheme {
                val viewModels = screenTestViewModel()

                MemoAddScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    addViewModel = viewModels.viewModel,
                    tagViewModel = viewModels.tagViewModel,
                    webViewModel = viewModels.webViewModel,
                    contactViewModel = viewModels.contactViewModel,
                    placeViewModel = viewModels.placeViewModel,
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    initialDateRange = MemoAddNavKey.InitialDateRange(start = start, endInclusive = endInclusive),
                    componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNode(hasRole(Role.Checkbox)).assertIsOn()
        composeRule.onNodeWithText(start.toDefaultDisplayText()).assertExists()
        composeRule.onNodeWithText(endInclusive.toDefaultDisplayText()).assertExists()
    }

    public companion object {
        private fun hasRole(role: Role): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoAddScreenMessageTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-ADD-FEATURE-007 기본 환경 추가 성공 안내`() {
        assertMessage(effect = MemoAddEffect.AddSucceeded, expectedMessage = DEFAULT_ADD_SUCCEEDED_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-ADD-FEATURE-007 한국어 추가 성공 안내`() {
        assertMessage(effect = MemoAddEffect.AddSucceeded, expectedMessage = KOREAN_ADD_SUCCEEDED_MESSAGE)
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-010 기본 환경 제목 미입력 안내`() {
        assertMessage(effect = MemoAddEffect.TitleBlank, expectedMessage = DEFAULT_TITLE_BLANK_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-ADD-FEATURE-010 한국어 제목 미입력 안내`() {
        assertMessage(effect = MemoAddEffect.TitleBlank, expectedMessage = KOREAN_TITLE_BLANK_MESSAGE)
    }

    private fun assertMessage(
        effect: MemoAddEffect,
        expectedMessage: String,
    ) {
        val effectChannel = Channel<MemoAddEffect>(capacity = Channel.BUFFERED)
        val viewModels = screenTestViewModel(effect = effectChannel.receiveAsFlow())
        every { viewModels.viewModel.add(detail = any(), tagSelection = any(), webIdSet = any(), contactIdSet = any(), placeIdSet = any()) } answers { effectChannel.trySend(effect).getOrThrow() }
        composeRule.setContent {
            MemoAddScreenTestTheme {
                MemoAddScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    addViewModel = viewModels.viewModel,
                    tagViewModel = viewModels.tagViewModel,
                    webViewModel = viewModels.webViewModel,
                    contactViewModel = viewModels.contactViewModel,
                    placeViewModel = viewModels.placeViewModel,
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    initialDateRange = null,
                    componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
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
        private const val TYPED_TITLE = "MemoTitleInput"
        private const val DEFAULT_ADD_SUCCEEDED_MESSAGE = "Memo added."
        private const val KOREAN_ADD_SUCCEEDED_MESSAGE = "메모가 추가되었습니다."
        private const val DEFAULT_TITLE_BLANK_MESSAGE = "Please enter a title."
        private const val KOREAN_TITLE_BLANK_MESSAGE = "제목을 입력해 주세요."
    }
}

private val DEFAULT_MONTH_NAMES =
    listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private fun LocalDate.toDefaultDisplayText(): String = "${DEFAULT_MONTH_NAMES[month.number - 1]} $day, $year"
