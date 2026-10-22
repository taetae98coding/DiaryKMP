package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.lifecycle.Lifecycle
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.justRun
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoHomeUndoBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-3d292bef-5de8-4ab7-b0e5-9ed63652fd5e 안내가 보이는 동안 화면이 재생성되면 안내가 닫히고 되돌릴 수 없다`() {
        val memo = fixtureMonkey.memo(title = MEMO_TITLE)
        val viewModel = viewModel(memo = memo)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { Home(viewModel = viewModel) }
        finishMemo()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_FINISHED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { viewModel.restart(id = any()) }
    }

    @Test
    fun `TC-43c0f892-e929-4317-9cc9-ce4674b164e6 안내가 보이는 동안 앱이 백그라운드에 다녀와도 안내가 남고 실행 취소할 수 있다`() {
        val memo = fixtureMonkey.memo(title = MEMO_TITLE)
        val viewModel = viewModel(memo = memo)
        composeRule.setContent { Home(viewModel = viewModel) }
        finishMemo()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_FINISHED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.restart(id = memo.id) }
    }

    @Test
    fun `TC-42079533-47ce-41c2-89aa-502d7c995b10 마지막 메모를 완료하면 빈 상태 안내가 나타나고 실행 취소하면 사라진다`() {
        val memo = fixtureMonkey.memo(title = MEMO_TITLE)
        val viewModel = storedListViewModel(memo = memo)
        composeRule.setContent { Home(viewModel = viewModel) }
        finishMemo()

        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_EMPTY_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(MEMO_TITLE).assertDoesNotExist()

        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(MEMO_TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(MEMO_TITLE).assertExists()
    }

    @Composable
    private fun Home(viewModel: MemoHomeViewModel) {
        DiaryTheme {
            MemoHomeScreen(
                navigateToAdd = {},
                navigateToDetail = {},
                navigateToFilter = {},
                navigateToFinishedList = {},
                navigateToSearch = {},
                componentVisibleProvider = { MemoHomeScaffoldComponentVisible() },
                listState = rememberLazyListState(),
                memoViewModel = viewModel,
                syncViewModel = screenTestSyncViewModel(),
            )
        }
    }

    private fun finishMemo() {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(MEMO_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_FINISHED_MESSAGE).assertExists()
    }

    // 완료는 저장에 성공한 것으로 보고 곧바로 안내를 보낸다. 목록은 고정되어 있어 완료한 메모가 자리에 남는다.
    private fun viewModel(memo: Memo): MemoHomeViewModel {
        val effectChannel = Channel<MemoListEffect>(capacity = Channel.BUFFERED)
        val viewModel = mockk<MemoHomeViewModel>()
        every { viewModel.sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.DEFAULT))
        every { viewModel.memoPagingData } returns MutableStateFlow(memoPagingDataOf(listOf(MemoListItem.Content(memo = memo))))
        every { viewModel.filterUiState } returns MutableStateFlow(MemoHomeScaffoldFilterUiState())
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        every { viewModel.finish(id = memo.id) } answers { effectChannel.trySend(MemoListEffect.Finished(id = memo.id)).getOrThrow() }
        justRun { viewModel.restart(id = any()) }
        return viewModel
    }

    // 완료와 실행 취소가 기기에 저장된 목록을 바꾸는 것처럼, 완료하면 목록을 비우고 실행 취소하면 메모를 되돌린다.
    private fun storedListViewModel(memo: Memo): MemoHomeViewModel {
        val effectChannel = Channel<MemoListEffect>(capacity = Channel.BUFFERED)
        val storedPagingData = MutableStateFlow(memoPagingDataOf(listOf(MemoListItem.Content(memo = memo))))
        val viewModel = mockk<MemoHomeViewModel>()
        every { viewModel.sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.DEFAULT))
        every { viewModel.memoPagingData } returns storedPagingData
        every { viewModel.filterUiState } returns MutableStateFlow(MemoHomeScaffoldFilterUiState())
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        every { viewModel.finish(id = memo.id) } answers {
            storedPagingData.value = memoPagingDataOf(emptyList())
            effectChannel.trySend(MemoListEffect.Finished(id = memo.id)).getOrThrow()
        }
        every { viewModel.restart(id = memo.id) } answers { storedPagingData.value = memoPagingDataOf(listOf(MemoListItem.Content(memo = memo))) }
        return viewModel
    }

    private companion object {
        private const val MEMO_TITLE = "MemoHomeUndoBoundaryTitle"
        private const val DEFAULT_FINISHED_MESSAGE = "Memo finished."
        private const val DEFAULT_UNDO_ACTION = "Undo"
        private const val DEFAULT_EMPTY_TITLE = "No memos yet"
        private const val TIMEOUT_MILLIS = 5_000L

        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
