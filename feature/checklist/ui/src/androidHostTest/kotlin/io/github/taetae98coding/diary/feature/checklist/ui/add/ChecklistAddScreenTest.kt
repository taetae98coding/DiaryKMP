package io.github.taetae98coding.diary.feature.checklist.ui.add

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.domain.checklist.exception.ChecklistTitleBlankException
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistUseCase
import io.github.taetae98coding.diary.feature.checklist.ui.actionNameList
import io.github.taetae98coding.diary.feature.checklist.ui.checklistTestFixtureMonkey
import io.github.taetae98coding.diary.feature.checklist.ui.editableText
import io.github.taetae98coding.diary.feature.checklist.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.checklist.ui.visibleTextList
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class ChecklistAddScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CHECKLIST-ADD-FEATURE-001 한국어 환경에서 상단 바에 화면 제목을 표시한다`() {
        setScreen()

        composeRule.onNodeWithText(KOREAN_TITLE, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-001 기본 환경에서 상단 바에 화면 제목을 표시한다`() {
        setScreen()

        composeRule.onNodeWithText(DEFAULT_TITLE, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-002 처음 들어오면 입력이 비어 있고 넣어 둔 항목이 없으며 제목 입력에 초점이 있다`() {
        setScreen()

        titleInput().editableText() shouldBe ""
        descriptionInput().editableText() shouldBe ""
        itemInput().editableText() shouldBe ""
        composeRule.onNode(removeButton(position = 1)).assertDoesNotExist()
        titleInput().assertIsFocused()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CHECKLIST-ADD-FEATURE-003 화면에는 정해진 입력과 동작만 둔다`() {
        setScreen()
        addItem(PASSPORT)

        composeRule.actionNameList() shouldContainExactlyInAnyOrder
            listOf(
                KOREAN_NAVIGATE_UP,
                KOREAN_TITLE_LABEL,
                KOREAN_DESCRIPTION_LABEL,
                "$KOREAN_ITEM_REMOVE 1",
                KOREAN_ITEM_LABEL,
                KOREAN_ITEM_ADD,
                KOREAN_ADD,
            ) + KOREAN_DESCRIPTION_TAB_LIST
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-004 항목 넣기를 선택하면 목록 맨 끝에 놓이고 항목 입력이 비워진다`() {
        setScreen()
        addItem(PASSPORT)
        itemInput().performTextInput(CHARGER)

        composeRule.onNodeWithContentDescription(DEFAULT_ITEM_ADD).performScrollTo().performClick()
        composeRule.waitForIdle()

        itemTextList() shouldBe listOf(PASSPORT, CHARGER)
        itemInput().editableText() shouldBe ""
        itemInput().assertIsFocused()
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-004 입력을 마치는 키를 누르면 목록 맨 끝에 놓이고 항목 입력이 비워진다`() {
        setScreen()
        addItem(PASSPORT)
        itemInput().performTextInput(CHARGER)

        itemInput().performImeAction()
        composeRule.waitForIdle()

        itemTextList() shouldBe listOf(PASSPORT, CHARGER)
        itemInput().editableText() shouldBe ""
        itemInput().assertIsFocused()
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-005 항목 입력이 비어 있거나 공백뿐이면 항목을 넣지 않는다`() {
        setScreen()

        listOf("", "   ").forEach { value ->
            itemInput().performTextReplacement(value)
            composeRule.onNodeWithContentDescription(DEFAULT_ITEM_ADD).assertIsNotEnabled()
            itemInput().performImeAction()
            composeRule.waitForIdle()

            composeRule.onNode(removeButton(position = 1)).assertDoesNotExist()
        }
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-006 같은 내용의 항목도 새 항목으로 넣는다`() {
        setScreen()

        addItem(PASSPORT)
        addItem(PASSPORT)

        itemTextList() shouldBe listOf(PASSPORT, PASSPORT)
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-007 넣어 둔 항목을 빼면 사라지고 남은 항목의 순서는 그대로다`() {
        setScreen()
        addItem(PASSPORT)
        addItem(CHARGER)
        addItem(UMBRELLA)

        composeRule.onNode(removeButton(position = 2)).performScrollTo().performClick()
        composeRule.waitForIdle()

        itemTextList() shouldBe listOf(PASSPORT, UMBRELLA)
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-008 넣어 둔 항목은 체크할 수 없다`() {
        setScreen()

        addItem(PASSPORT)

        composeRule.onNode(isToggleable()).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-009 넣어 둔 항목이 없으면 안내 없이 목록 자리를 비워 둔다`() {
        setScreen()

        composeRule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.TestTag, DIARY_EMPTY_BOX_TEST_TAG)).assertDoesNotExist()
        composeRule.onNode(removeButtonMatcher()).assertDoesNotExist()
        composeRule.visibleTextList() shouldContainExactlyInAnyOrder
            listOf(DEFAULT_TITLE, DEFAULT_TITLE_LABEL, DEFAULT_DESCRIPTION_LABEL, DEFAULT_ITEMS_LABEL, DEFAULT_ITEM_LABEL)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CHECKLIST-ADD-FEATURE-010 추가에 성공하면 화면에 머문 채 입력과 넣어 둔 항목을 비우고 성공 피드백을 제공한다`() {
        val useCase = mockk<AddChecklistUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.success(checklistTestFixtureMonkey.giveMeOne<Uuid>())
        var navigateUpCount = 0
        setScreen(viewModel = ChecklistAddViewModel(addChecklistUseCase = useCase), navigateUp = { navigateUpCount += 1 })
        fillInput()

        clickAdd()

        coVerify(exactly = 1) {
            useCase(
                parameter =
                    AddChecklistUseCase.Parameter(
                        detail = ChecklistDetail(title = TRIP, description = WEEKEND),
                        itemContentList = listOf(PASSPORT, UMBRELLA),
                    ),
            )
        }
        titleInput().editableText() shouldBe ""
        descriptionInput().editableText() shouldBe ""
        itemInput().editableText() shouldBe ""
        composeRule.onNode(removeButton(position = 1)).assertDoesNotExist()
        titleInput().assertIsFocused()
        composeRule.onNodeWithText(KOREAN_ADD_SUCCEEDED).assertExists()
        navigateUpCount shouldBe 0
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-010 기본 환경에서 추가 성공 피드백을 제공한다`() {
        setScreen(viewModel = effectViewModel(effect = ChecklistAddEffect.AddSucceeded))

        clickAdd()

        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-011 추가를 처리하는 동안 진행 상태를 표시한다`() {
        val gate = CompletableDeferred<Unit>()
        val useCase = mockk<AddChecklistUseCase>()
        coEvery { useCase(parameter = any()) } coAnswers {
            gate.await()
            Result.success(checklistTestFixtureMonkey.giveMeOne<Uuid>())
        }
        setScreen(viewModel = ChecklistAddViewModel(addChecklistUseCase = useCase))
        titleInput().performTextInput(TRIP)

        clickAdd()

        composeRule.onNode(progressIndicator(), useUnmergedTree = true).assertExists()
        gate.complete(Unit)
        composeRule.waitForIdle()
        composeRule.onNode(progressIndicator(), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-012 저장에 실패하면 안내 없이 작성 내용을 유지하고 진행 상태만 해제한다`() {
        val useCase = mockk<AddChecklistUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.failure(IllegalStateException(checklistTestFixtureMonkey.giveMeOne<String>()))
        setScreen(viewModel = ChecklistAddViewModel(addChecklistUseCase = useCase))
        fillInput()

        clickAdd()

        titleInput().editableText() shouldBe TRIP
        descriptionInput().editableText() shouldBe WEEKEND
        itemTextList() shouldBe listOf(PASSPORT)
        itemInput().editableText() shouldBe UMBRELLA
        composeRule.onNode(progressIndicator(), useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CHECKLIST-ADD-FEATURE-013 제목이 비어 있으면 제목 입력을 알리고 초점을 옮긴다`() {
        assertTitleBlank(title = "")
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CHECKLIST-ADD-FEATURE-013 제목이 공백뿐이면 제목 입력을 알리고 초점을 옮긴다`() {
        assertTitleBlank(title = "   ")
    }

    private fun assertTitleBlank(title: String) {
        val useCase = mockk<AddChecklistUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.failure(ChecklistTitleBlankException())
        setScreen(viewModel = ChecklistAddViewModel(addChecklistUseCase = useCase))
        fillInput(title = title)
        itemInput().performClick()

        clickAdd()

        composeRule.onNodeWithText(KOREAN_TITLE_BLANK).assertExists()
        titleInput().assertIsFocused()
        descriptionInput().editableText() shouldBe WEEKEND
        itemTextList() shouldBe listOf(PASSPORT)
        itemInput().editableText() shouldBe UMBRELLA
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-013 기본 환경에서 공백 제목으로 추가하면 제목 입력을 알린다`() {
        val useCase = mockk<AddChecklistUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.failure(ChecklistTitleBlankException())
        setScreen(viewModel = ChecklistAddViewModel(addChecklistUseCase = useCase))
        titleInput().performTextInput("   ")

        clickAdd()

        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK).assertExists()
        titleInput().assertIsFocused()
    }

    @Test
    fun `TC-CHECKLIST-ADD-FEATURE-014 뒤로가기를 선택하면 이전 화면으로 돌아가고 추가를 요청하지 않는다`() {
        val viewModel = relaxedViewModel()
        var navigateUpCount = 0
        setScreen(viewModel = viewModel, navigateUp = { navigateUpCount += 1 })
        titleInput().performTextInput(TRIP)
        addItem(PASSPORT)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        verify(exactly = 0) { viewModel.add(detail = any(), itemContentList = any()) }
    }

    @Test
    fun `TC-CHECKLIST-ADD-DOMAIN-001 화면이 재생성되거나 시스템이 되살려도 입력과 넣어 둔 항목을 유지한다`() {
        val stateRestorationTester = StateRestorationTester(composeRule)
        val viewModel = relaxedViewModel()
        stateRestorationTester.setContent { DiaryTheme { ChecklistAddScreen(navigateUp = {}, componentVisibleProvider = { ChecklistAddScaffoldComponentVisible() }, viewModel = viewModel) } }
        composeRule.waitForIdle()
        titleInput().performTextInput(TRIP)
        descriptionInput().performTextInput(WEEKEND)
        addItem(PASSPORT)
        addItem(CHARGER)
        itemInput().performTextInput(UMBRELLA)
        composeRule.waitForIdle()

        stateRestorationTester.emulateSavedInstanceStateRestore()

        titleInput().editableText() shouldBe TRIP
        descriptionInput().editableText() shouldBe WEEKEND
        itemTextList() shouldBe listOf(PASSPORT, CHARGER)
        itemInput().editableText() shouldBe UMBRELLA
    }

    @Test
    fun `TC-CHECKLIST-ADD-DOMAIN-002 화면이 재생성되면 표시 중이던 제목 미입력 피드백을 다시 표시하지 않는다`() {
        assertFeedbackNotRestored(effect = ChecklistAddEffect.TitleBlank, message = DEFAULT_TITLE_BLANK)
    }

    @Test
    fun `TC-CHECKLIST-ADD-DOMAIN-002 화면이 재생성되면 표시 중이던 성공 피드백을 다시 표시하지 않는다`() {
        assertFeedbackNotRestored(effect = ChecklistAddEffect.AddSucceeded, message = DEFAULT_ADD_SUCCEEDED)
    }

    private fun assertFeedbackNotRestored(
        effect: ChecklistAddEffect,
        message: String,
    ) {
        val stateRestorationTester = StateRestorationTester(composeRule)
        val viewModel = effectViewModel(effect = effect)
        stateRestorationTester.setContent { DiaryTheme { ChecklistAddScreen(navigateUp = {}, componentVisibleProvider = { ChecklistAddScaffoldComponentVisible() }, viewModel = viewModel) } }
        composeRule.waitForIdle()
        clickAdd()
        composeRule.onNodeWithText(message).assertExists()

        stateRestorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(message).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-ADD-DOMAIN-009 항목 입력에 남은 내용은 맨 끝 항목으로 함께 추가를 요청한다`() {
        assertLeftoverInput(input = UMBRELLA, expected = listOf(PASSPORT, UMBRELLA))
    }

    @Test
    fun `TC-CHECKLIST-ADD-DOMAIN-009 항목 입력이 비어 있으면 넣어 둔 항목만 추가를 요청한다`() {
        assertLeftoverInput(input = "", expected = listOf(PASSPORT))
    }

    @Test
    fun `TC-CHECKLIST-ADD-DOMAIN-009 항목 입력이 공백뿐이면 넣어 둔 항목만 추가를 요청한다`() {
        assertLeftoverInput(input = "   ", expected = listOf(PASSPORT))
    }

    private fun assertLeftoverInput(
        input: String,
        expected: List<String>,
    ) {
        val viewModel = relaxedViewModel()
        setScreen(viewModel = viewModel)
        titleInput().performTextInput(TRIP)
        addItem(PASSPORT)
        itemInput().performTextReplacement(input)

        clickAdd()

        verify(exactly = 1) { viewModel.add(detail = any(), itemContentList = expected) }
    }

    @Test
    fun `Cmd Enter 단축키를 누르면 체크리스트 추가를 요청한다`() {
        val viewModel = relaxedViewModel()
        setScreen(viewModel = viewModel)
        titleInput().performTextInput(TRIP)

        composeRule.onRoot().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.add(detail = ChecklistDetail(title = TRIP, description = ""), itemContentList = emptyList()) }
    }

    private fun setScreen(
        viewModel: ChecklistAddViewModel = relaxedViewModel(),
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                ChecklistAddScreen(
                    navigateUp = navigateUp,
                    componentVisibleProvider = { ChecklistAddScaffoldComponentVisible() },
                    viewModel = viewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun fillInput(title: String = TRIP) {
        titleInput().performTextInput(title)
        descriptionInput().performTextInput(WEEKEND)
        addItem(PASSPORT)
        itemInput().performTextInput(UMBRELLA)
        composeRule.waitForIdle()
    }

    private fun addItem(content: String) {
        itemInput().performTextReplacement(content)
        itemInput().performImeAction()
        composeRule.waitForIdle()
    }

    private fun clickAdd() {
        composeRule.onNode(hasContentDescription(DEFAULT_ADD) or hasContentDescription(KOREAN_ADD)).performClick()
        composeRule.waitForIdle()
    }

    private fun titleInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[TITLE_INPUT_INDEX]

    private fun descriptionInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INPUT_INDEX]

    private fun itemInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[ITEM_INPUT_INDEX]

    private fun itemTextList(): List<String> =
        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .map { node -> node.config[SemanticsProperties.Text].joinToString { text -> text.text } }
            .filter { text -> text in ITEM_CANDIDATE_LIST }

    private fun relaxedViewModel(): ChecklistAddViewModel =
        mockk<ChecklistAddViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(ChecklistAddUiState())
            every { effect } returns Channel<ChecklistAddEffect>().receiveAsFlow()
        }

    private fun effectViewModel(effect: ChecklistAddEffect): ChecklistAddViewModel {
        val channel = Channel<ChecklistAddEffect>(Channel.BUFFERED)

        return mockk<ChecklistAddViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(ChecklistAddUiState())
            every { this@apply.effect } returns channel.receiveAsFlow()
            every { add(detail = any(), itemContentList = any()) } answers { channel.trySend(effect) }
        }
    }

    private companion object {
        const val TITLE_INPUT_INDEX = 0
        const val DESCRIPTION_INPUT_INDEX = 1
        const val ITEM_INPUT_INDEX = 2
        const val KOREAN_TITLE = "체크리스트 추가"
        const val DEFAULT_TITLE = "Add checklist"
        const val KOREAN_ADD = "체크리스트 추가"
        const val DEFAULT_ADD = "Add checklist"
        const val KOREAN_NAVIGATE_UP = "뒤로가기"
        const val DEFAULT_NAVIGATE_UP = "Navigate up"
        const val KOREAN_ITEM_LABEL = "새 항목"
        const val DEFAULT_ITEM_LABEL = "New item"
        const val DEFAULT_ITEMS_LABEL = "Items"
        const val KOREAN_ITEM_ADD = "항목 넣기"
        const val DEFAULT_ITEM_ADD = "Add item"
        const val KOREAN_ITEM_REMOVE = "항목 빼기"
        const val DEFAULT_ITEM_REMOVE = "Remove item"
        const val DEFAULT_TITLE_LABEL = "Title"
        const val DEFAULT_DESCRIPTION_LABEL = "Description"
        const val KOREAN_TITLE_LABEL = "제목"
        const val KOREAN_DESCRIPTION_LABEL = "설명"
        const val KOREAN_ADD_SUCCEEDED = "체크리스트가 추가되었습니다."
        const val DEFAULT_ADD_SUCCEEDED = "Checklist added."
        const val KOREAN_TITLE_BLANK = "제목을 입력해 주세요."
        const val DEFAULT_TITLE_BLANK = "Please enter a title."
        const val TRIP = "여행 준비"
        const val WEEKEND = "주말"
        const val PASSPORT = "여권"
        const val CHARGER = "충전기"
        const val UMBRELLA = "우산"
        val KOREAN_DESCRIPTION_TAB_LIST = listOf("입력", "미리보기")
        val ITEM_CANDIDATE_LIST = listOf(PASSPORT, CHARGER, UMBRELLA)

        fun removeButton(position: Int): SemanticsMatcher = hasContentDescription("$DEFAULT_ITEM_REMOVE $position") or hasContentDescription("$KOREAN_ITEM_REMOVE $position")

        fun removeButtonMatcher(): SemanticsMatcher = hasContentDescription(DEFAULT_ITEM_REMOVE, substring = true) or hasContentDescription(KOREAN_ITEM_REMOVE, substring = true)

        fun progressIndicator(): SemanticsMatcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
    }
}
