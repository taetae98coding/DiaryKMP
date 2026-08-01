package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MemoListItem
import io.github.taetae98coding.diary.compose.memo.MemoListUiState
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.home.memoPagingDataOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
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
import kotlin.time.Instant
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoFinishedListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-007 완료된 메모가 하나도 없어도 화면을 사용할 수 있다`() {
        val viewModel = mockk<MemoFinishedListViewModel>()
        every { viewModel.sort } returns MutableStateFlow(ListSort.DEFAULT)
        every { viewModel.memoPagingData } returns MutableStateFlow(memoPagingDataOf(emptyList()))
        every { viewModel.effect } returns Channel<MemoFinishedListEffect>(capacity = Channel.BUFFERED).receiveAsFlow()

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

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-015 좌에서 우로 스와이프하면 메모를 다시 시작하고 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setMemoFinishedListScreen(environment.viewModel)

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restart(id = environment.memo.id) }
        composeRule.onNodeWithText(DEFAULT_RESTARTED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-FINISHED-LIST-FEATURE-015 한국어 환경에서 다시 시작 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setMemoFinishedListScreen(environment.viewModel)

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_RESTARTED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-016 다시 시작을 실행 취소하면 메모를 다시 완료한다`() {
        val environment = screenTestEnvironment()
        setMemoFinishedListScreen(environment.viewModel)

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.finish(id = environment.memo.id) }
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-017 우에서 좌로 스와이프하면 메모를 삭제하고 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setMemoFinishedListScreen(environment.viewModel)

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.delete(id = environment.memo.id) }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-FINISHED-LIST-FEATURE-017 한국어 환경에서 삭제 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setMemoFinishedListScreen(environment.viewModel)

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-018 삭제를 실행 취소하면 메모를 복구한다`() {
        val environment = screenTestEnvironment()
        setMemoFinishedListScreen(environment.viewModel)

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.memo.id) }
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-019 메모를 선택하면 그 메모의 상세로 이동한다`() {
        val environment = screenTestEnvironment()
        val navigatedIdList = mutableListOf<Uuid>()
        setMemoFinishedListScreen(
            viewModel = environment.viewModel,
            navigateToDetail = navigatedIdList::add,
        )

        composeRule.onNodeWithText(MEMO_TITLE).performClick()
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(environment.memo.id)
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-020 뒤로가기 버튼을 선택하면 이전 화면으로 돌아간다`() {
        val environment = screenTestEnvironment()
        var navigateUpCount = 0
        setMemoFinishedListScreen(
            viewModel = environment.viewModel,
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    private fun setMemoFinishedListScreen(
        viewModel: MemoFinishedListViewModel,
        navigateUp: () -> Unit = {},
        navigateToDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                MemoFinishedListScreen(
                    navigateUp = navigateUp,
                    navigateToDetail = navigateToDetail,
                    memoViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(MEMO_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
    }

    public companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val MEMO_TITLE = "MemoFinishedListScreenTitle"
        private const val DEFAULT_TITLE = "Finished Memos"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_RESTARTED_MESSAGE = "Memo restarted."
        private const val KOREAN_RESTARTED_MESSAGE = "메모를 다시 시작했습니다."
        private const val DEFAULT_DELETED_MESSAGE = "Memo deleted."
        private const val KOREAN_DELETED_MESSAGE = "메모가 삭제되었습니다."
        private const val DEFAULT_UNDO_ACTION = "Undo"
        private const val KOREAN_UNDO_ACTION = "실행 취소"
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun screenTestEnvironment(): ScreenTestEnvironment {
            val memo =
                fixtureMonkey
                    .giveMeKotlinBuilder<Memo>()
                    .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(title = MEMO_TITLE, dateTime = null))
                    .setExp(Memo::isFinished, true)
                    .setExp(Memo::isDeleted, false)
                    .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                    .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                    .sample()
            val effectChannel = Channel<MemoFinishedListEffect>(capacity = Channel.BUFFERED)
            val viewModel = mockk<MemoFinishedListViewModel>()
            every { viewModel.sort } returns MutableStateFlow(ListSort.DEFAULT)

            every { viewModel.memoPagingData } returns
                MutableStateFlow(memoPagingDataOf(listOf(MemoListItem.Content(memo = memo))))
            every { viewModel.effect } returns effectChannel.receiveAsFlow()
            every { viewModel.restart(id = memo.id) } answers {
                effectChannel.trySend(MemoFinishedListEffect.Restarted(id = memo.id)).getOrThrow()
            }
            every { viewModel.delete(id = memo.id) } answers {
                effectChannel.trySend(MemoFinishedListEffect.Deleted(id = memo.id)).getOrThrow()
            }
            justRun { viewModel.finish(id = memo.id) }
            justRun { viewModel.restore(id = memo.id) }

            return ScreenTestEnvironment(
                memo = memo,
                viewModel = viewModel,
            )
        }
    }
}

private class ScreenTestEnvironment(
    val memo: Memo,
    val viewModel: MemoFinishedListViewModel,
)
