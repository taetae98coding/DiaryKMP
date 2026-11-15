package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.routine.ui.RoutineTestTheme
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_EMPTY_UNIT
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_DAILY
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_TAG_LABEL
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormAddTagViewModel
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormAmountPerRecordInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormDateButton
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormDescriptionInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormEditableText
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormGoalAmountInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormInputtedUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormSelectedCycle
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormTitleInput
import io.github.taetae98coding.diary.feature.routine.ui.form.today
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
class RoutineAddTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-af2877d1-07f5-4cbe-870f-19b86b5f42a5 RoutineHome에서 진입하면 태그 입력에 연결 대상으로 표시되는 태그가 없다`() {
        val tagList = listOf(Arb.tag(title = TAG_A_TITLE).next(), Arb.tag(title = TAG_B_TITLE).next()).map(::activeTag)
        setScreen(tagViewModel = tagViewModel(initialTagId = null, tagFlow = MutableStateFlow(tagList)))

        composeRule.onNodeWithText(TAG_A_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(TAG_B_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_FORM_DEFAULT_TAG_LABEL).assertExists()
    }

    @Test
    fun `TC-d1c7e3f2-9a4a-443c-8aa8-7677854d8e74 TagDetail 루틴 탭에서 진입하면 그 태그만 고르고 나머지는 처음 상태로 시작한다`() {
        val tagA = activeTag(Arb.tag(title = TAG_A_TITLE).next())
        val tagB = activeTag(Arb.tag(title = TAG_B_TITLE).next())
        setScreen(tagViewModel = tagViewModel(initialTagId = tagA.id, tagFlow = MutableStateFlow(listOf(tagA, tagB))))

        composeRule.onNodeWithText(TAG_A_TITLE).assertExists()
        composeRule.onNodeWithText(TAG_B_TITLE).assertDoesNotExist()
        assertInitialInput()
    }

    @Test
    fun `TC-a8321e69-e09f-442f-b9b0-c0735f920433 추가에 성공해도 고른 태그는 유지한다`() {
        val tagA = activeTag(Arb.tag(title = TAG_A_TITLE).next())
        val viewModel = succeedingViewModel()
        setScreen(viewModel = viewModel, tagViewModel = routineFormAddTagViewModel(tagList = listOf(tagA)))
        composeRule.routineFormTitleInput().performTextInput(DRINK_WATER)
        composeRule.waitForIdle()

        clickAdd()

        verify(exactly = 1) { viewModel.add(input = any(), tagIdSet = setOf(tagA.id)) }
        composeRule.routineFormTitleInput().routineFormEditableText() shouldBe ""
        composeRule.onNodeWithText(TAG_A_TITLE).assertExists()
    }

    @Test
    fun `TC-2b6f7868-c1a1-48a6-88f8-06ebd9013c08 태그 선택 목록에서 태그를 고르면 선택만 바뀌고 루틴 추가는 요청하지 않는다`() {
        val tagA = activeTag(Arb.tag(title = TAG_A_TITLE).next())
        val tagB = activeTag(Arb.tag(title = TAG_B_TITLE).next())
        val viewModel = relaxedViewModel()
        // 고른 태그는 화면이 들고 있는 선택에만 반영되고, 루틴이 저장되기 전이라 연결을 저장하는 경로가 없다.
        val tagViewModel = tagViewModel(initialTagId = null, tagFlow = MutableStateFlow(listOf(tagA, tagB)))
        setScreen(viewModel = viewModel, tagViewModel = tagViewModel)

        composeRule.onNodeWithText(ROUTINE_FORM_DEFAULT_TAG_LABEL).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        awaitPickerRow(title = TAG_A_TITLE)
        composeRule.onNodeWithText(TAG_A_TITLE).performClick()
        composeRule.waitForIdle()

        tagViewModel.selectionUiState.value.tagIdSet shouldBe setOf(tagA.id)
        verify(exactly = 0) { viewModel.add(input = any(), tagIdSet = any()) }
    }

    private fun assertInitialInput() {
        composeRule.routineFormTitleInput().routineFormEditableText() shouldBe ""
        composeRule.routineFormDescriptionInput().routineFormEditableText() shouldBe ""
        composeRule.routineFormDateButton(today()).assertExists()
        composeRule.routineFormSelectedCycle() shouldBe ROUTINE_FORM_DEFAULT_CYCLE_DAILY
        composeRule.routineFormInputtedUnit() shouldBe ROUTINE_EMPTY_UNIT
        composeRule.routineFormGoalAmountInput().routineFormEditableText() shouldBe ""
        composeRule.routineFormAmountPerRecordInput().routineFormEditableText() shouldBe "1"
    }

    // 선택 목록은 페이지 단위로 준비되므로 항목이 나타날 때까지 프레임과 실제 시간을 함께 진행시킨다.
    private fun awaitPickerRow(title: String) {
        repeat(PICKER_WAIT_ATTEMPT_COUNT) {
            composeRule.waitForIdle()
            if (composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()) return
            composeRule.mainClock.advanceTimeByFrame()
            @Suppress("ForbiddenMethodCall")
            Thread.sleep(PICKER_WAIT_INTERVAL_MILLIS)
        }

        error("태그 선택 목록에 $title 이 나타나지 않았다")
    }

    private fun setScreen(
        viewModel: RoutineAddViewModel = relaxedViewModel(),
        tagViewModel: EntityAddTagViewModel = routineFormAddTagViewModel(),
    ) {
        val tagAddRequestKey = Arb.kotlinUuid().next()
        composeRule.setContent {
            RoutineTestTheme {
                RoutineAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = tagAddRequestKey,
                    componentVisibleProvider = { RoutineAddScaffoldComponentVisible() },
                    addViewModel = viewModel,
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun clickAdd() {
        composeRule.onNodeWithContentDescription(DEFAULT_ADD).performClick()
        composeRule.waitForIdle()
    }

    private fun relaxedViewModel(): RoutineAddViewModel =
        mockk<RoutineAddViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(RoutineAddUiState())
            every { effect } returns Channel<RoutineAddEffect>().receiveAsFlow()
        }

    private fun succeedingViewModel(): RoutineAddViewModel {
        val channel = Channel<RoutineAddEffect>(capacity = Channel.BUFFERED)
        return mockk<RoutineAddViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(RoutineAddUiState())
            every { effect } returns channel.receiveAsFlow()
            every { add(input = any(), tagIdSet = any()) } answers { channel.trySend(RoutineAddEffect.AddSucceeded) }
        }
    }

    // 고른 태그 조회는 저장된 태그 중 고른 것만, 완료되거나 삭제된 태그는 빼고 돌려준다.
    private fun tagViewModel(
        initialTagId: Uuid?,
        tagFlow: MutableStateFlow<List<Tag>>,
    ): EntityAddTagViewModel {
        val pageTagUseCase = mockk<PageTagUseCase>()
        every { pageTagUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(tagFlow.value)))
        val getSelectedTagUseCase = mockk<GetSelectedTagUseCase>()
        every { getSelectedTagUseCase(parameter = any()) } answers {
            val idSet = firstArg<Set<Uuid>>()
            tagFlow.map { tagList -> Result.success(tagList.filter { tag -> tag.id in idSet && !tag.isFinished && !tag.isDeleted }) }
        }

        return EntityAddTagViewModel(
            initialTagId = initialTagId,
            pageTagUseCase = pageTagUseCase,
            getSelectedTagUseCase = getSelectedTagUseCase,
        )
    }

    private companion object {
        const val DEFAULT_ADD = "Add routine"
        const val DRINK_WATER = "물 마시기"
        const val TAG_A_TITLE = "RoutineAddTagA"
        const val TAG_B_TITLE = "RoutineAddTagB"
        const val PICKER_WAIT_ATTEMPT_COUNT = 500
        const val PICKER_WAIT_INTERVAL_MILLIS = 10L

        fun activeTag(tag: Tag): Tag = tag.copy(isFinished = false, isDeleted = false)
    }
}
