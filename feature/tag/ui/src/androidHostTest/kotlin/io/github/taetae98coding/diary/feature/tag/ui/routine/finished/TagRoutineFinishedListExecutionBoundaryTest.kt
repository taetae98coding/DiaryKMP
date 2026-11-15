package io.github.taetae98coding.diary.feature.tag.ui.routine.finished

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedTagRoutineUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FindTagUseCase
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagRoutineFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.ui.memo.finished.screenTestSyncViewModel
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagRoutine
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 화면을 떠났다 돌아올 때의 상태는 전환 이력의 항목마다 보관되므로, 앱과 같은 방식으로 항목을 그리는 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagRoutineFinishedListExecutionBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val tagId = Arb.kotlinUuid().next()
    private val finishedListKey = TagRoutineFinishedListNavKey(tagId = tagId)
    private val backStack = NavBackStack<ScreenNavKey>(TagDetailNavKey(id = tagId))
    private val syncViewModel = screenTestSyncViewModel()
    private val finishedRoutineListFlow = MutableStateFlow<List<RoutineSummary>>(emptyList())

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-92271eee-7353-4c02-868f-6220d5607516 화면에 들어오거나 다른 화면에서 돌아오는 것만으로는 새로고침하지 않는다`() {
        val routineList = routineList()
        setFinishedListNavDisplay(routineList = routineList)

        openFinishedList(routineList = routineList)
        composeRule.runOnIdle { backStack.add(RoutineDetailNavKey(id = Arb.kotlinUuid().next())) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        verify(exactly = 0) { syncViewModel.refresh() }
    }

    @Test
    fun `TC-545c5675-a1fd-432c-935c-300ad7b9f45b 루틴 상세에서 돌아오면 목록에서 보던 자리가 그대로다`() {
        val routineList = routineList()
        setFinishedListNavDisplay(routineList = routineList)
        openFinishedList(routineList = routineList)
        scrollList(routineList = routineList)

        composeRule.onNodeWithText(routineList[SCROLL_INDEX].routine.detail.title).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        assertScrolledPosition(routineList = routineList)
    }

    // 상세에서 다시 시작하거나 삭제하거나 대상 태그와의 연결을 해제한 루틴은 완료된 루틴 조회 결과에서 빠진다.
    @Test
    fun `TC-99e368c4-809d-4985-86e4-dceeb98f85e7 루틴 상세에서 상태나 연결을 바꾼 루틴이 조회 결과에서 빠지면 돌아온 목록에 표시되지 않는다`() {
        val routineList = routineList()
        val selected = routineList.first()
        setFinishedListNavDisplay(routineList = routineList)
        openFinishedList(routineList = routineList)

        composeRule.onNodeWithText(selected.routine.detail.title).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DETAIL_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle { finishedRoutineListFlow.value = routineList - selected }
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(selected.routine.detail.title).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithText(routineList[1].routine.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-7e689571-24b6-4b4d-afbc-dd9ef172507e TagDetail 화면으로 돌아갔다가 다시 진입하면 목록이 맨 위부터 보인다`() {
        val routineList = routineList()
        setFinishedListNavDisplay(routineList = routineList)
        openFinishedList(routineList = routineList)
        scrollList(routineList = routineList)

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(TAG_DETAIL_CONTENT).assertIsDisplayed()
        openFinishedList(routineList = routineList)

        composeRule
            .onNodeWithText(
                routineList
                    .first()
                    .routine.detail.title,
            ).assertIsDisplayed()
    }

    @Test
    fun `TC-d21ea977-c027-4835-adab-d345b1d5a5fa 앱이 백그라운드에 다녀와도 보던 자리가 그대로다`() {
        val routineList = routineList()
        setFinishedListNavDisplay(routineList = routineList)
        openFinishedList(routineList = routineList)
        scrollList(routineList = routineList)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        assertScrolledPosition(routineList = routineList)
    }

    private fun openFinishedList(routineList: List<RoutineSummary>) {
        composeRule.runOnIdle { backStack.add(finishedListKey) }
        composeRule.waitForIdle()
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule
                .onAllNodesWithText(
                    routineList
                        .first()
                        .routine.detail.title,
                ).fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun scrollList(routineList: List<RoutineSummary>) {
        composeRule.refreshableList().performScrollToIndex(SCROLL_INDEX)
        composeRule.waitForIdle()
        assertScrolledPosition(routineList = routineList)
    }

    private fun assertScrolledPosition(routineList: List<RoutineSummary>) {
        composeRule.onNodeWithText(routineList[SCROLL_INDEX].routine.detail.title).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                routineList
                    .first()
                    .routine.detail.title,
            ).assertDoesNotExist()
    }

    private fun setFinishedListNavDisplay(routineList: List<RoutineSummary>) {
        finishedRoutineListFlow.value = routineList
        val pageFinishedTagRoutineUseCase = mockk<PageFinishedTagRoutineUseCase>()
        every { pageFinishedTagRoutineUseCase(parameter = any()) } answers { finishedRoutineListFlow.map { itemList -> Result.success(PagingData.from(itemList)) } }
        val findTagUseCase = mockk<FindTagUseCase>()
        every { findTagUseCase(parameter = tagId) } returns MutableStateFlow(Result.success(null))

        composeRule.setContent {
            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators =
                        listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                    entryProvider =
                        entryProvider {
                            entry<TagDetailNavKey> { Text(text = TAG_DETAIL_CONTENT) }
                            entry<TagRoutineFinishedListNavKey> { key ->
                                TagRoutineFinishedListScreen(
                                    navigateUp = { backStack.removeLastOrNull() },
                                    navigateToRoutineDetail = { id -> backStack.add(RoutineDetailNavKey(id = id)) },
                                    routineViewModel =
                                        viewModel {
                                            TagRoutineFinishedListViewModel(
                                                tagId = key.tagId,
                                                pageFinishedTagRoutineUseCase = pageFinishedTagRoutineUseCase,
                                                findTagUseCase = findTagUseCase,
                                                finishRoutineUseCase = mockk(),
                                                restartRoutineUseCase = mockk(),
                                                deleteRoutineUseCase = mockk(),
                                                restoreRoutineUseCase = mockk(),
                                            )
                                        },
                                    syncViewModel = syncViewModel,
                                )
                            }
                            entry<RoutineDetailNavKey> { Text(text = DETAIL_CONTENT) }
                        },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun routineList(): List<RoutineSummary> {
        val titlePrefix = Arb.alphanumericString(prefix = "FinishedRoutine").next()

        return List(ROUTINE_COUNT) { index -> tagRoutine(title = "${titlePrefix}Index$index", isFinished = true) }
    }

    private companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val ROUTINE_COUNT = 40
        private const val SCROLL_INDEX = 30
        private const val TAG_DETAIL_CONTENT = "TagDetailContent"
        private const val DETAIL_CONTENT = "RoutineDetailContent"
    }
}
