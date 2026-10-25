package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.memo.ui.home.memoPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
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
class MemoFinishedListLastRestartTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-3b62f484-7942-4ea8-8be7-d366f4738898 마지막 메모를 다시 시작하면 빈 상태 안내가 나타난다`() {
        val baseMemo = fixtureMonkey.memo(isFinished = true, isDeleted = false, primaryTagId = null)
        val memo = baseMemo.copy(detail = baseMemo.detail.copy(title = MEMO_TITLE, dateTime = null))
        val effectChannel = Channel<MemoFinishedListEffect>(capacity = Channel.BUFFERED)
        // 다시 시작이 기기에 저장된 완료 목록에서 메모를 빼는 것처럼, 다시 시작하면 목록을 비운다.
        val storedPagingData = MutableStateFlow(memoPagingDataOf(listOf(MemoListItem.Content(memo = memo))))
        val viewModel = mockk<MemoFinishedListViewModel>()
        every { viewModel.sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.DEFAULT))
        every { viewModel.memoPagingData } returns storedPagingData
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        every { viewModel.restart(id = memo.id) } answers {
            storedPagingData.value = memoPagingDataOf(emptyList())
            effectChannel.trySend(MemoFinishedListEffect.Restarted(id = memo.id)).getOrThrow()
        }
        justRun { viewModel.finish(id = any()) }
        composeRule.setContent {
            DiaryTheme {
                MemoFinishedListScreen(
                    navigateUp = {},
                    navigateToDetail = {},
                    memoViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(MEMO_TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_EMPTY_TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(MEMO_TITLE).assertDoesNotExist()
    }

    private companion object {
        private const val MEMO_TITLE = "MemoFinishedLastRestartTitle"
        private const val DEFAULT_EMPTY_TITLE = "No finished memos"
        private const val TIMEOUT_MILLIS = 5_000L

        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
