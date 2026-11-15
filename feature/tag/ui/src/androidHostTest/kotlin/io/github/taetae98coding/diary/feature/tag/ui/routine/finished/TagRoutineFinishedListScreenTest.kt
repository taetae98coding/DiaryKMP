package io.github.taetae98coding.diary.feature.tag.ui.routine.finished

import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.tag.ui.memo.finished.screenTestSyncViewModel
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagRoutine
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagRoutineFinishedListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-03e6e171-84b7-4c94-92e9-af4d89ff5b40 다시 시작하면 카드가 사라지고 기본 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setScreen(viewModel = environment.viewModel)

        swipeCard { swipeRight() }

        verify(exactly = 1) { environment.viewModel.restart(id = environment.item.routine.id) }
        verify(exactly = 0) { environment.viewModel.delete(id = any()) }
        assertCardIsGone()
        composeRule.onNodeWithText(DEFAULT_RESTARTED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-03e6e171-84b7-4c94-92e9-af4d89ff5b40 다시 시작하면 한국어 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setScreen(viewModel = environment.viewModel)

        swipeCard { swipeRight() }

        composeRule.onNodeWithText(KOREAN_RESTARTED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-63a0dbf9-4df1-4c8f-b222-96ede3ccba1f 삭제하면 카드가 사라지고 기본 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setScreen(viewModel = environment.viewModel)

        swipeCard { swipeLeft() }

        verify(exactly = 1) { environment.viewModel.delete(id = environment.item.routine.id) }
        verify(exactly = 0) { environment.viewModel.restart(id = any()) }
        assertCardIsGone()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-63a0dbf9-4df1-4c8f-b222-96ede3ccba1f 삭제하면 한국어 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setScreen(viewModel = environment.viewModel)

        swipeCard { swipeLeft() }

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-2373eda6-54d7-4791-85cb-0f04f46f82af 다시 시작 실행 취소는 루틴 완료를 요청하고 루틴이 목록에 다시 나타난다`() {
        val environment = screenTestEnvironment()
        setScreen(viewModel = environment.viewModel)
        swipeCard { swipeRight() }

        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()

        verify(exactly = 1) { environment.viewModel.finish(id = environment.item.routine.id) }
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
        waitUntilCardIsDisplayed()
    }

    @Test
    fun `TC-2373eda6-54d7-4791-85cb-0f04f46f82af 삭제 실행 취소는 루틴 복구를 요청하고 루틴이 목록에 다시 나타난다`() {
        val environment = screenTestEnvironment()
        setScreen(viewModel = environment.viewModel)
        swipeCard { swipeLeft() }

        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.item.routine.id) }
        verify(exactly = 0) { environment.viewModel.finish(id = any()) }
        waitUntilCardIsDisplayed()
    }

    @Test
    fun `TC-bb771e7c-c591-45d6-9227-e528841c8b61 다시 시작이나 삭제가 저장되지 못해 안내가 오지 않으면 루틴이 남고 안내와 실행 취소를 표시하지 않는다`() {
        val environment = screenTestEnvironment(isSaved = false)
        setScreen(viewModel = environment.viewModel)

        swipeCard { swipeRight() }
        composeRule.mainClock.advanceTimeBy(SWIPE_RESET_MILLIS)
        swipeCard { swipeLeft() }

        verify(exactly = 1) { environment.viewModel.restart(id = environment.item.routine.id) }
        verify(exactly = 1) { environment.viewModel.delete(id = environment.item.routine.id) }
        composeRule.onNodeWithText(ITEM_TITLE).assertIsDisplayed()
        listOf(DEFAULT_RESTARTED_MESSAGE, DEFAULT_DELETED_MESSAGE, DEFAULT_UNDO_ACTION).forEach { text ->
            composeRule.onNodeWithText(text).assertDoesNotExist()
        }
    }

    @Test
    fun `TC-0661f905-e083-4a04-b9f6-a29d4fdcd207 루틴을 선택하면 그 루틴의 상세 이동을 요청한다`() {
        val environment = screenTestEnvironment()
        val navigatedIdList = mutableListOf<Uuid>()
        setScreen(viewModel = environment.viewModel, navigateToRoutineDetail = navigatedIdList::add)

        composeRule.onNodeWithText(ITEM_TITLE).performClick()

        navigatedIdList shouldBe listOf(environment.item.routine.id)
    }

    @Test
    fun `TC-ec07790d-e535-491c-9bf1-2cd0a6651514 뒤로가기 버튼을 선택하면 화면을 떠난다`() {
        var navigateUpCount = 0
        setScreen(viewModel = screenTestEnvironment().viewModel, navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    private fun swipeCard(swipe: TouchInjectionScope.() -> Unit) {
        composeRule.onNode(hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(ITEM_TITLE)).performTouchInput(swipe)
        composeRule.waitForIdle()
    }

    private fun assertCardIsGone() {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(ITEM_TITLE).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun waitUntilCardIsDisplayed() {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(ITEM_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(ITEM_TITLE).assertIsDisplayed()
    }

    private fun setScreen(
        viewModel: TagRoutineFinishedListViewModel,
        navigateUp: () -> Unit = {},
        navigateToRoutineDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                TagRoutineFinishedListScreen(
                    navigateUp = navigateUp,
                    navigateToRoutineDetail = navigateToRoutineDetail,
                    routineViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                )
            }
        }
        waitUntilCardIsDisplayed()
    }

    // 저장이 성공하면 완료된 루틴 목록이 다시 조회되는 것과 같게 목록 조회 결과를 바꾸고 결과 Effect를 보낸다.
    private fun screenTestEnvironment(isSaved: Boolean = true): ScreenTestEnvironment {
        val item = tagRoutine(title = ITEM_TITLE, isFinished = true)
        val pagingData = MutableStateFlow(tagEntityPagingData(itemList = listOf(item)))
        val effectChannel = Channel<RoutineListEffect>(capacity = Channel.BUFFERED)
        val viewModel = mockk<TagRoutineFinishedListViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(TagRoutineFinishedListUiState(title = TAG_TITLE))
        every { viewModel.routinePagingData } returns pagingData
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        if (isSaved) {
            every { viewModel.restart(id = item.routine.id) } answers {
                pagingData.value = tagEntityPagingData(itemList = emptyList())
                effectChannel.trySend(RoutineListEffect.Restarted(id = item.routine.id)).getOrThrow()
            }
            every { viewModel.delete(id = item.routine.id) } answers {
                pagingData.value = tagEntityPagingData(itemList = emptyList())
                effectChannel.trySend(RoutineListEffect.Deleted(id = item.routine.id)).getOrThrow()
            }
            every { viewModel.finish(id = item.routine.id) } answers { pagingData.value = tagEntityPagingData(itemList = listOf(item)) }
            every { viewModel.restore(id = item.routine.id) } answers { pagingData.value = tagEntityPagingData(itemList = listOf(item)) }
        }

        return ScreenTestEnvironment(item = item, viewModel = viewModel)
    }

    private data class ScreenTestEnvironment(
        val item: RoutineSummary,
        val viewModel: TagRoutineFinishedListViewModel,
    )

    private companion object {
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val SWIPE_RESET_MILLIS = 2_000L
        const val TAG_TITLE = "📌 TagTitle"
        const val ITEM_TITLE = "TagRoutineFinishedListScreenRoutine"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val DEFAULT_RESTARTED_MESSAGE = "Routine restarted."
        const val KOREAN_RESTARTED_MESSAGE = "루틴을 다시 시작했습니다."
        const val DEFAULT_DELETED_MESSAGE = "Routine deleted."
        const val KOREAN_DELETED_MESSAGE = "루틴이 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
    }
}
