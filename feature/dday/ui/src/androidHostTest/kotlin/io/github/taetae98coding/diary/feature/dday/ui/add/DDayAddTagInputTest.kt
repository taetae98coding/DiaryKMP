package io.github.taetae98coding.diary.feature.dday.ui.add

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_OVERFLOW_CLAMP
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_OVERFLOW_SKIP
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_TAG_LABEL
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_UNIT_DAY
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_UNIT_YEAR
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayFormTestTheme
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayAddRepeatOf
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayConfirmDatePicker
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayDateButton
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayDescriptionInput
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayDisplayedColorHex
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayEditableText
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayEndDateNotSetButton
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayFormAddTagViewModel
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayIsCountsBaseDayOn
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayOpenEndDatePickerNotSet
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayOverflowPolicyButton
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayPickDate
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayRepeatList
import io.github.taetae98coding.diary.feature.dday.ui.form.ddaySelectOverflowPolicy
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayTitleInput
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayToggleCountsBaseDay
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayTypePickerDate
import io.github.taetae98coding.diary.feature.dday.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.collections.shouldBeEmpty
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
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class DDayAddTagInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-a568196b-0005-42d2-97ad-0b1a618c0575 DDayHome에서 진입하면 태그 입력에 연결 대상으로 표시되는 태그가 없다`() {
        val tagList = listOf(Arb.tag(title = TAG_A_TITLE).next(), Arb.tag(title = TAG_B_TITLE).next()).map(::activeTag)
        setScreen(tagViewModel = tagViewModel(initialTagId = null, tagFlow = MutableStateFlow(tagList)))

        composeRule.onNodeWithText(TAG_A_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(TAG_B_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_TAG_LABEL).assertExists()
    }

    @Test
    fun `TC-889939c1-ec4d-4cb5-8a21-c8043686613e TagDetail 디데이 탭에서 진입하면 그 태그만 고르고 나머지는 처음 상태로 시작한다`() {
        val tagA = activeTag(Arb.tag(title = TAG_A_TITLE).next())
        val tagB = activeTag(Arb.tag(title = TAG_B_TITLE).next())
        setScreen(tagViewModel = tagViewModel(initialTagId = tagA.id, tagFlow = MutableStateFlow(listOf(tagA, tagB))))

        composeRule.onNodeWithText(TAG_A_TITLE).assertExists()
        composeRule.onNodeWithText(TAG_B_TITLE).assertDoesNotExist()
        assertInitialInput()
    }

    @Test
    fun `TC-961d5298-db4b-4239-94ed-9c714f597575 추가에 성공해도 고른 태그는 유지한다`() {
        val tagA = activeTag(Arb.tag(title = TAG_A_TITLE).next())
        val viewModel = succeedingViewModel()
        setScreen(viewModel = viewModel, tagViewModel = ddayFormAddTagViewModel(tagList = listOf(tagA)))
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        composeRule.waitForIdle()

        clickAdd()

        verify(exactly = 1) { viewModel.add(input = any(), tagIdSet = setOf(tagA.id)) }
        composeRule.ddayTitleInput().ddayEditableText() shouldBe ""
        composeRule.onNodeWithText(TAG_A_TITLE).assertExists()
    }

    @Test
    fun `TC-263c0762-b1c1-4181-9d6f-dda08fd317f0 태그 선택 목록에서 태그를 고르면 선택만 바뀌고 디데이 추가는 요청하지 않는다`() {
        val tagA = activeTag(Arb.tag(title = TAG_A_TITLE).next())
        val tagB = activeTag(Arb.tag(title = TAG_B_TITLE).next())
        val tagViewModel = mockk<EntityAddTagViewModel>(relaxed = true)
        every { tagViewModel.uiState } returns MutableStateFlow(EntityTagInputUiState())
        every { tagViewModel.tagPagingData } returns MutableStateFlow(PagingData.from(listOf(tagA, tagB)))
        every { tagViewModel.selectableTagPagingData } returns MutableStateFlow(PagingData.from(listOf(tagA, tagB)))
        val viewModel = relaxedViewModel()
        setScreen(viewModel = viewModel, tagViewModel = tagViewModel)

        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_TAG_LABEL).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        awaitPickerRow(title = TAG_A_TITLE)
        composeRule.onNodeWithText(TAG_A_TITLE).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { tagViewModel.add(tagId = tagA.id) }
        verify(exactly = 0) { viewModel.add(input = any(), tagIdSet = any()) }
    }

    @Test
    fun `TC-11513348-5a0d-4704-b56e-1a5c20801aea 화면이 재생성되어도 고른 태그를 유지한다`() {
        val tagA = activeTag(Arb.tag(title = TAG_A_TITLE).next())
        val tagViewModel = tagViewModel(initialTagId = null, tagFlow = MutableStateFlow(listOf(tagA)))
        val restorationTester = StateRestorationTester(composeRule)
        val tagAddRequestKey = Arb.kotlinUuid().next()
        restorationTester.setContent {
            DDayFormTestTheme {
                DDayAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = tagAddRequestKey,
                    componentVisibleProvider = { DDayAddScaffoldComponentVisible() },
                    addViewModel = remember { relaxedViewModel() },
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { tagViewModel.add(tagId = tagA.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(TAG_A_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TAG_A_TITLE).assertExists()
        tagViewModel.selectionUiState.value.tagIdSet shouldBe setOf(tagA.id)
    }

    @Test
    fun `TC-11513348-5a0d-4704-b56e-1a5c20801aea 백그라운드에 다녀와도 고른 태그를 유지한다`() {
        val tagA = activeTag(Arb.tag(title = TAG_A_TITLE).next())
        val tagViewModel = tagViewModel(initialTagId = null, tagFlow = MutableStateFlow(listOf(tagA)))
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        val tagAddRequestKey = Arb.kotlinUuid().next()
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DDayFormTestTheme {
                    DDayAddScreen(
                        navigateUp = {},
                        navigateToTagAdd = {},
                        navigateToTagDetail = {},
                        tagAddRequestKey = tagAddRequestKey,
                        componentVisibleProvider = { DDayAddScaffoldComponentVisible() },
                        addViewModel = remember { relaxedViewModel() },
                        tagViewModel = tagViewModel,
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { tagViewModel.add(tagId = tagA.id) }
        composeRule.waitForIdle()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TAG_A_TITLE).assertExists()
    }

    @Test
    fun `TC-3b3b5bc8-2f68-42f7-afe2-c0278ebf3c52 DDayHome에서 진입했으면 메모리 정리 뒤 작성 내용은 복원되고 고른 태그가 없다`() {
        assertMemoryRestore(isTagDetailEntry = false)
    }

    @Test
    fun `TC-3b3b5bc8-2f68-42f7-afe2-c0278ebf3c52 TagDetail 디데이 탭에서 진입했으면 메모리 정리 뒤 작성 내용은 복원되고 태그 A만 고른 상태다`() {
        assertMemoryRestore(isTagDetailEntry = true)
    }

    private fun assertMemoryRestore(isTagDetailEntry: Boolean) {
        val tagA = activeTag(Arb.tag(title = TAG_A_TITLE).next())
        val tagB = activeTag(Arb.tag(title = TAG_B_TITLE).next())
        val tagFlow = MutableStateFlow(listOf(tagA, tagB))
        val initialTagId = if (isTagDetailEntry) tagA.id else null
        val tagViewModelList = mutableListOf<EntityAddTagViewModel>()
        val restorationTester = StateRestorationTester(composeRule)
        val tagAddRequestKey = Arb.kotlinUuid().next()
        restorationTester.setContent {
            // 메모리 정리 뒤에는 태그 선택을 들고 있던 객체도 새로 만들어지므로 복원할 때마다 진입 경로의 초기값으로 새로 만든다.
            val tagViewModel = remember { tagViewModel(initialTagId = initialTagId, tagFlow = tagFlow).also(tagViewModelList::add) }

            DDayFormTestTheme {
                DDayAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = tagAddRequestKey,
                    componentVisibleProvider = { DDayAddScaffoldComponentVisible() },
                    addViewModel = remember { relaxedViewModel() },
                    tagViewModel = tagViewModel,
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        composeRule.ddayDescriptionInput().performTextInput(US)
        val color = composeRule.ddayDisplayedColorHex()
        composeRule.ddayPickDate(from = today(), to = FILLED_BASE_DATE)
        composeRule.ddayToggleCountsBaseDay()
        composeRule.ddayAddRepeatOf(number = "1", unitLabel = DDAY_FORM_DEFAULT_UNIT_YEAR)
        composeRule.ddayAddRepeatOf(number = "100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.ddayOpenEndDatePickerNotSet()
        composeRule.ddayTypePickerDate(FILLED_END_DATE)
        composeRule.ddayConfirmDatePicker()
        composeRule.ddaySelectOverflowPolicy(currentLabel = DDAY_FORM_DEFAULT_OVERFLOW_CLAMP, label = DDAY_FORM_DEFAULT_OVERFLOW_SKIP)
        composeRule.runOnIdle { tagViewModelList.last().add(tagId = tagB.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(TAG_B_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        tagViewModelList.size shouldBe 2
        composeRule.ddayTitleInput().ddayEditableText() shouldBe MARRIAGE
        composeRule.ddayDescriptionInput().ddayEditableText() shouldBe US
        composeRule.ddayDisplayedColorHex() shouldBe color
        composeRule.ddayDateButton(FILLED_BASE_DATE).assertExists()
        composeRule.ddayIsCountsBaseDayOn() shouldBe false
        composeRule.ddayRepeatList() shouldBe listOf("1" to DDAY_FORM_DEFAULT_UNIT_YEAR, "100" to DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.ddayDateButton(FILLED_END_DATE).assertExists()
        composeRule.ddayOverflowPolicyButton(DDAY_FORM_DEFAULT_OVERFLOW_SKIP).assertExists()
        composeRule.onNodeWithText(TAG_B_TITLE).assertDoesNotExist()
        if (isTagDetailEntry) {
            composeRule.onNodeWithText(TAG_A_TITLE).assertExists()
        } else {
            composeRule.onNodeWithText(TAG_A_TITLE).assertDoesNotExist()
        }
        tagViewModelList
            .last()
            .selectionUiState.value.tagIdSet shouldBe setOfNotNull(initialTagId)
    }

    @Test
    fun `TC-383dbc84-bc46-45f9-b1d3-26afa29c6b52 태그를 고르지 않으면 태그 없이 추가를 요청한다`() {
        val viewModel = relaxedViewModel()
        setScreen(viewModel = viewModel, tagViewModel = tagViewModel(initialTagId = null, tagFlow = MutableStateFlow(listOf(activeTag(Arb.tag().next())))))
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)

        clickAdd()

        verify(exactly = 1) { viewModel.add(input = any(), tagIdSet = emptySet()) }
    }

    @Test
    fun `TC-e65ac220-3d79-48ae-afce-c18bbcccced5 고른 뒤 완료된 태그도 연결하도록 추가를 요청한다`() {
        assertChangedTagLinked { tag -> tag.copy(isFinished = true) }
    }

    @Test
    fun `TC-e65ac220-3d79-48ae-afce-c18bbcccced5 고른 뒤 삭제된 태그도 연결하도록 추가를 요청한다`() {
        assertChangedTagLinked { tag -> tag.copy(isDeleted = true) }
    }

    private fun assertChangedTagLinked(change: (Tag) -> Tag) {
        val tagA = activeTag(Arb.tag(title = TAG_A_TITLE).next())
        val tagFlow = MutableStateFlow(listOf(tagA))
        val tagViewModel = tagViewModel(initialTagId = null, tagFlow = tagFlow)
        val viewModel = relaxedViewModel()
        setScreen(viewModel = viewModel, tagViewModel = tagViewModel)
        composeRule.runOnIdle { tagViewModel.add(tagId = tagA.id) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(TAG_A_TITLE).assertExists()
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)

        composeRule.runOnIdle { tagFlow.update { tagList -> tagList.map(change) } }
        composeRule.waitForIdle()
        clickAdd()

        verify(exactly = 1) { viewModel.add(input = any(), tagIdSet = setOf(tagA.id)) }
    }

    @Test
    fun `TC-6999ff06-049d-472c-8d8d-44acf2de0fb1 처음부터 고른 태그가 완료되었으면 보이지 않아도 연결하도록 추가를 요청한다`() {
        val tagA = activeTag(Arb.tag(title = TAG_A_TITLE).next()).copy(isFinished = true)
        val viewModel = relaxedViewModel()
        setScreen(viewModel = viewModel, tagViewModel = tagViewModel(initialTagId = tagA.id, tagFlow = MutableStateFlow(listOf(tagA))))
        composeRule.onNodeWithText(TAG_A_TITLE).assertDoesNotExist()
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)

        clickAdd()

        verify(exactly = 1) { viewModel.add(input = any(), tagIdSet = setOf(tagA.id)) }
    }

    private fun assertInitialInput() {
        composeRule.ddayTitleInput().ddayEditableText() shouldBe ""
        composeRule.ddayDescriptionInput().ddayEditableText() shouldBe ""
        composeRule.ddayDateButton(today()).assertExists()
        composeRule.ddayIsCountsBaseDayOn() shouldBe true
        composeRule.ddayRepeatList().shouldBeEmpty()
        composeRule.ddayEndDateNotSetButton().assertExists()
        composeRule.ddayOverflowPolicyButton(DDAY_FORM_DEFAULT_OVERFLOW_CLAMP).assertExists()
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
        viewModel: DDayAddViewModel = relaxedViewModel(),
        tagViewModel: EntityAddTagViewModel = ddayFormAddTagViewModel(),
    ) {
        val tagAddRequestKey = Arb.kotlinUuid().next()
        composeRule.setContent {
            DDayFormTestTheme {
                DDayAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    tagAddRequestKey = tagAddRequestKey,
                    componentVisibleProvider = { DDayAddScaffoldComponentVisible() },
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

    private fun relaxedViewModel(): DDayAddViewModel =
        mockk<DDayAddViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(DDayAddUiState())
            every { effect } returns Channel<DDayAddEffect>().receiveAsFlow()
        }

    private fun succeedingViewModel(): DDayAddViewModel {
        val channel = Channel<DDayAddEffect>(capacity = Channel.BUFFERED)
        return mockk<DDayAddViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(DDayAddUiState())
            every { effect } returns channel.receiveAsFlow()
            every { add(input = any(), tagIdSet = any()) } answers { channel.trySend(DDayAddEffect.AddSucceeded) }
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
        const val DEFAULT_ADD = "Add D-Day"
        const val MARRIAGE = "결혼"
        const val US = "우리"
        const val TAG_A_TITLE = "DDayAddTagA"
        const val TAG_B_TITLE = "DDayAddTagB"
        const val PICKER_WAIT_ATTEMPT_COUNT = 500
        const val PICKER_WAIT_INTERVAL_MILLIS = 10L
        val FILLED_BASE_DATE = LocalDate(2025, 1, 15)
        val FILLED_END_DATE = LocalDate(2030, 1, 15)

        fun activeTag(tag: Tag): Tag = tag.copy(isFinished = false, isDeleted = false)

        fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    }
}
