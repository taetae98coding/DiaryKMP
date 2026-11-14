package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineFinishedListNavKey
import io.github.taetae98coding.diary.feature.routine.api.navigateToRoutineDetail
import io.github.taetae98coding.diary.feature.routine.ui.RoutineTestTheme
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordEffect
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordUiState
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordViewModel
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListViewModel
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.routine.ui.routineEntry
import io.github.taetae98coding.diary.feature.routine.ui.routineSyncRefreshViewModel
import io.github.taetae98coding.diary.feature.tag.api.TagRoutineFinishedListNavKey
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 제품 routineEntry로 NavDisplay를 구성해 완료된 루틴 목록에서 RoutineDetail 화면에 들어가고 돌아오는 결과를 확인한다. 다른 기능 모듈의 화면은 대역 화면으로 대신한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h2400dp")
class RoutineDetailFinishedNavDisplayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val tagA: Tag = Arb.tag().next().let { tag -> tag.copy(detail = tag.detail.copy(emoji = "", title = TAG_A_TITLE), isFinished = false, isDeleted = false) }
    private val backStack = NavBackStack<ScreenNavKey>()
    private val detailViewModelList = mutableListOf<RoutineDetailViewModel>()

    // 기기에 저장된 루틴, 기록과 태그 연결이다. 완료와 다시 시작은 바로 저장되어 새로 만든 화면도 같은 완료 여부를 보여 준다.
    private val storedRoutine: Routine = Arb.routine(isDeleted = false, detail = routineRecordDetailOf(), isFinished = true).next()
    private val storedUiState = MutableStateFlow<RoutineDetailUiState>(RoutineDetailUiState.Content(id = storedRoutine.id, detail = storedRoutine.detail, isFinished = true))
    private val storedRecord = routineRecordOf(date = MARCH_5, amount = 300, routineId = storedRoutine.id)

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        routineRecordSetToday(TODAY)
    }

    @After
    fun tearDown() {
        routineRecordReleaseToday()
        stopKoin()
    }

    @Test
    fun `TC-0eedfcc7-1173-4a15-97b7-a20dcd5e8ba9 RoutineFinishedList에서 선택한 완료된 루틴도 같은 내용과 입력으로 표시한다`() {
        backStack.add(RoutineFinishedListNavKey)
        setNavDisplay()
        composeRule.routineRecordAwaitText(text = storedRoutine.detail.title)

        composeRule.onNodeWithText(storedRoutine.detail.title).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(RoutineFinishedListNavKey, RoutineDetailNavKey(id = storedRoutine.id))
        assertFinishedRoutineContent()
    }

    @Test
    fun `TC-0eedfcc7-1173-4a15-97b7-a20dcd5e8ba9 TagRoutineFinishedList에서 선택한 완료된 루틴도 같은 내용과 입력으로 표시한다`() {
        val listKey = TagRoutineFinishedListNavKey(tagId = tagA.id)
        backStack.add(listKey)
        setNavDisplay()

        composeRule.runOnIdle { backStack.navigateToRoutineDetail(id = storedRoutine.id) }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(listKey, RoutineDetailNavKey(id = storedRoutine.id))
        assertFinishedRoutineContent()
    }

    private fun assertFinishedRoutineContent() {
        composeRule.onNodeWithContentDescription(ROUTINE_DETAIL_DEFAULT_LOADING).assertDoesNotExist()
        composeRule.routineRecordTitleInput().routineRecordEditableText() shouldBe storedRoutine.detail.title
        composeRule.routineRecordDescriptionInput().routineRecordEditableText() shouldBe storedRoutine.detail.description
        composeRule.routineRecordFormDateButton(storedRoutine.detail.startDate).assertExists()
        composeRule.routineRecordGoalInput().routineRecordEditableText() shouldBe "2000"
        composeRule.routineRecordAmountPerRecordInput().routineRecordEditableText() shouldBe "250"
        composeRule.routineRecordRowNameList() shouldBe listOf(routineRecordRowName(date = MARCH_5, amountText = "300ml"))
        composeRule.onNode(hasText(TAG_A_TITLE) and !hasAnyAncestor(isDialog())).assertExists()
        composeRule.onNode(routineRecordDateButton(TODAY)).assertExists()
        composeRule.routineRecordAmountInput().routineRecordEditableText() shouldBe "250"
        composeRule.onNode(routineRecordAddButton()).assertExists()
        composeRule.onNode(routineRecordRemoveButton(date = MARCH_5, amountText = "300ml")).assertExists()
        composeRule.onNodeWithText(ROUTINE_RECORD_DEFAULT_TAG_LABEL).assertExists()
        composeRule.onNode(routineRecordDeleteButton()).assertExists()
        composeRule.onNode(routineDetailRestartButton()).assertExists()
    }

    @Test
    fun `TC-e9b22ec2-059c-45de-89c0-9853124b018b RoutineFinishedList에서 들어왔으면 뒤로가면 RoutineFinishedList로 돌아가고 수정하지 않는다`() {
        backStack.add(RoutineFinishedListNavKey)
        setNavDisplay()
        composeRule.runOnIdle { backStack.navigateToRoutineDetail(id = storedRoutine.id) }
        composeRule.waitForIdle()
        editTitle()

        clickNavigateUp()

        backStack.toList() shouldBe listOf(RoutineFinishedListNavKey)
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_TITLE).assertIsDisplayed()
        assertNotUpdated()
    }

    @Test
    fun `TC-e9b22ec2-059c-45de-89c0-9853124b018b TagRoutineFinishedList에서 들어왔으면 뒤로가면 그 TagRoutineFinishedList로 돌아가고 수정하지 않는다`() {
        val listKey = TagRoutineFinishedListNavKey(tagId = tagA.id)
        backStack.add(listKey)
        setNavDisplay()
        composeRule.runOnIdle { backStack.navigateToRoutineDetail(id = storedRoutine.id) }
        composeRule.waitForIdle()
        editTitle()

        clickNavigateUp()

        backStack.toList() shouldBe listOf(listKey)
        composeRule.onNodeWithText("$TAG_FINISHED_LIST_CONTENT_PREFIX${tagA.id}").assertIsDisplayed()
        assertNotUpdated()
    }

    @Test
    fun `TC-74105783-b75e-4929-9a0f-1c7874a0618e 다시 시작한 뒤 뒤로갔다가 다시 들어오면 완료를 제공한다`() {
        assertFinishChangeKeptAfterBack(isFinished = true)
    }

    @Test
    fun `TC-74105783-b75e-4929-9a0f-1c7874a0618e 완료한 뒤 뒤로갔다가 다시 들어오면 다시 시작을 제공한다`() {
        assertFinishChangeKeptAfterBack(isFinished = false)
    }

    private fun assertFinishChangeKeptAfterBack(isFinished: Boolean) {
        storedUiState.update { state -> (state as RoutineDetailUiState.Content).copy(isFinished = isFinished) }
        backStack.addAll(listOf(RoutineFinishedListNavKey, RoutineDetailNavKey(id = storedRoutine.id)))
        setNavDisplay()
        composeRule.onNode(if (isFinished) routineDetailRestartButton() else routineDetailFinishButton()).performClick()
        composeRule.waitForIdle()

        clickNavigateUp()
        backStack.toList() shouldBe listOf(RoutineFinishedListNavKey)
        composeRule.runOnIdle { backStack.navigateToRoutineDetail(id = storedRoutine.id) }
        composeRule.waitForIdle()

        composeRule.onNode(if (isFinished) routineDetailFinishButton() else routineDetailRestartButton()).assertExists()
        composeRule.onNode(if (isFinished) routineDetailRestartButton() else routineDetailFinishButton()).assertDoesNotExist()
    }

    private fun editTitle() {
        composeRule.routineRecordTitleInput().performTextReplacement(ROUTINE_RECORD_TITLE_EDITED)
        composeRule.waitForIdle()
        composeRule.onNode(routineRecordUpdateButton()).assertExists()
    }

    private fun assertNotUpdated() {
        detailViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.update(input = any()) } }
    }

    private fun clickNavigateUp() {
        composeRule.onNodeWithContentDescription(ROUTINE_RECORD_DEFAULT_NAVIGATE_UP).performClick()
        composeRule.waitForIdle()
    }

    private fun setNavDisplay() {
        composeRule.setContent {
            NavDisplayTestHost {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            entry<TagRoutineFinishedListNavKey> { key -> Text(text = "$TAG_FINISHED_LIST_CONTENT_PREFIX${key.tagId}") }
                            routineEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                        },
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun NavDisplayTestHost(content: @Composable () -> Unit) {
        val viewModelStoreOwner =
            remember {
                object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore = ViewModelStore()
                }
            }

        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            KoinApplication(configuration = koinConfiguration { modules(navDisplayViewModelModule()) }) {
                RoutineTestTheme(content = content)
            }
        }
    }

    // 화면을 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun navDisplayViewModelModule() =
        module {
            factory { routineSyncRefreshViewModel() }
            factory {
                mockk<RoutineFinishedListViewModel>(relaxed = true).apply {
                    every { routinePagingData } returns flowOf(loadedPagingDataOf(listOf(RoutineSummary(routine = storedRoutine, cycleAmount = 0))))
                    every { effect } returns Channel<RoutineListEffect>().receiveAsFlow()
                }
            }
            factory {
                mockk<RoutineDetailViewModel>(relaxed = true)
                    .apply {
                        every { uiState } returns storedUiState
                        every { effect } returns Channel<RoutineDetailEffect>().receiveAsFlow()
                        every { finish() } answers { storedUiState.update { state -> (state as RoutineDetailUiState.Content).copy(isFinished = true) } }
                        every { restart() } answers { storedUiState.update { state -> (state as RoutineDetailUiState.Content).copy(isFinished = false) } }
                    }.also(detailViewModelList::add)
            }
            factory {
                mockk<RoutineDetailRecordViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(RoutineDetailRecordUiState())
                    every { recordPagingData } returns MutableStateFlow(routineRecordLoadedPagingDataOf(listOf(storedRecord)))
                    every { effect } returns Channel<RoutineDetailRecordEffect>().receiveAsFlow()
                }
            }
            factory {
                mockk<EntityDetailTagViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(EntityTagInputUiState(tagList = listOf(tagA)))
                    every { tagPagingData } returns MutableStateFlow(PagingData.from(listOf(tagA)))
                    every { selectableTagPagingData } returns MutableStateFlow(PagingData.from(listOf(tagA)))
                }
            }
        }

    private companion object {
        val TODAY: LocalDate = LocalDate(2026, 3, 20)
        val MARCH_5: LocalDate = LocalDate(2026, 3, 5)
        const val DEFAULT_FINISHED_LIST_TITLE = "Finished Routines"
        const val TAG_FINISHED_LIST_CONTENT_PREFIX = "TagRoutineFinishedList: "
        const val TAG_A_TITLE = "RoutineDetailFinishedTagA"

        fun loadedPagingDataOf(summaryList: List<RoutineSummary>): PagingData<RoutineSummary> =
            PagingData.from(
                data = summaryList,
                sourceLoadStates =
                    LoadStates(
                        refresh = LoadState.NotLoading(endOfPaginationReached = true),
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.NotLoading(endOfPaginationReached = true),
                    ),
            )
    }
}
