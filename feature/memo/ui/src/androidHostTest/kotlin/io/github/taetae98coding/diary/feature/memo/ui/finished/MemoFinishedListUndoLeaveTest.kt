package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.feature.memo.ui.home.memoPagingDataOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoFinishedListUndoLeaveTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-034 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        val baseMemo = fixtureMonkey.memo(isFinished = true, isDeleted = false, primaryTagId = null)
        val memo = baseMemo.copy(detail = baseMemo.detail.copy(title = MEMO_TITLE, dateTime = null))
        val effectChannel = Channel<MemoFinishedListEffect>(capacity = Channel.BUFFERED)
        val viewModel = mockk<MemoFinishedListViewModel>()
        every { viewModel.sort } returns MutableStateFlow(ListSort.DEFAULT)
        every { viewModel.memoPagingData } returns MutableStateFlow(memoPagingDataOf(listOf(MemoListItem.Content(memo = memo))))
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        every { viewModel.restart(id = memo.id) } answers { effectChannel.trySend(MemoFinishedListEffect.Restarted(id = memo.id)).getOrThrow() }
        justRun { viewModel.finish(id = any()) }
        var isShown by mutableStateOf(true)
        composeRule.setContent {
            DiaryTheme {
                if (isShown) {
                    MemoFinishedListScreen(
                        navigateUp = {},
                        navigateToDetail = {},
                        memoViewModel = viewModel,
                        syncViewModel = screenTestSyncViewModel(),
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(MEMO_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_RESTARTED_MESSAGE).assertExists()

        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_RESTARTED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { viewModel.finish(id = any()) }
    }

    private companion object {
        private const val MEMO_TITLE = "MemoFinishedUndoLeaveTitle"
        private const val DEFAULT_RESTARTED_MESSAGE = "Memo restarted."
        private const val DEFAULT_UNDO_ACTION = "Undo"
        private const val TIMEOUT_MILLIS = 5_000L

        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
