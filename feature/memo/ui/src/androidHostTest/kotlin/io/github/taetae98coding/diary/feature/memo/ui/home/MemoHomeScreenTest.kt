package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.foundation.lazy.rememberLazyListState
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
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.MemoListUiState
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-HOME-FEATURE-003 좌에서 우로 스와이프하면 메모가 완료되어 목록에서 사라진다`() {
        val environment = screenTestEnvironment()
        setMemoHomeScreen(environment.viewModel)

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.finish(id = environment.memo.id) }
        composeRule.onNodeWithText(DEFAULT_FINISHED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-HOME-FEATURE-003 한국어 환경에서 완료 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setMemoHomeScreen(environment.viewModel)

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_FINISHED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-004 완료를 실행 취소하면 메모가 목록에 다시 나타난다`() {
        val environment = screenTestEnvironment()
        setMemoHomeScreen(environment.viewModel)

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restart(id = environment.memo.id) }
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-005 우에서 좌로 스와이프하면 메모가 삭제되어 목록에서 사라진다`() {
        val environment = screenTestEnvironment()
        setMemoHomeScreen(environment.viewModel)

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.delete(id = environment.memo.id) }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-HOME-FEATURE-005 한국어 환경에서 삭제 안내와 실행 취소를 표시한다`() {
        val environment = screenTestEnvironment()
        setMemoHomeScreen(environment.viewModel)

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-006 삭제를 실행 취소하면 메모가 목록에 다시 나타난다`() {
        val environment = screenTestEnvironment()
        setMemoHomeScreen(environment.viewModel)

        composeRule.onNodeWithText(MEMO_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.memo.id) }
    }

    @Test
    fun `필터 버튼을 누르면 필터 화면으로 이동한다`() {
        val environment = screenTestEnvironment()
        var navigateToFilterCount = 0
        setMemoHomeScreen(
            viewModel = environment.viewModel,
            navigateToFilter = { navigateToFilterCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION).performClick()

        navigateToFilterCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-029 완료된 메모 버튼을 선택하면 완료된 메모 목록으로 이동한다`() {
        val environment = screenTestEnvironment()
        var navigateToFinishedListCount = 0
        setMemoHomeScreen(
            viewModel = environment.viewModel,
            navigateToFinishedList = { navigateToFinishedListCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_BUTTON_LABEL).performClick()

        navigateToFinishedListCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-047 검색 버튼을 선택하면 검색 화면으로 이동한다`() {
        val environment = screenTestEnvironment()
        var navigateToSearchCount = 0
        setMemoHomeScreen(
            viewModel = environment.viewModel,
            navigateToSearch = { navigateToSearchCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).performClick()

        navigateToSearchCount shouldBe 1
    }

    private fun setMemoHomeScreen(
        viewModel: MemoHomeViewModel,
        navigateToFilter: () -> Unit = {},
        navigateToFinishedList: () -> Unit = {},
        navigateToSearch: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                MemoHomeScreen(
                    navigateToAdd = {},
                    navigateToDetail = {},
                    navigateToFilter = navigateToFilter,
                    navigateToFinishedList = navigateToFinishedList,
                    navigateToSearch = navigateToSearch,
                    listState = rememberLazyListState(),
                    memoViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                    componentVisibleProvider = { MemoHomeScaffoldComponentVisible() },
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(MEMO_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
    }

    public companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val DEFAULT_FINISHED_MESSAGE = "Memo finished."
        private const val KOREAN_FINISHED_MESSAGE = "메모가 완료되었습니다."
        private const val DEFAULT_DELETED_MESSAGE = "Memo deleted."
        private const val KOREAN_DELETED_MESSAGE = "메모가 삭제되었습니다."
        private const val DEFAULT_UNDO_ACTION = "Undo"
        private const val KOREAN_UNDO_ACTION = "실행 취소"
        private const val DEFAULT_FILTER_BUTTON_DESCRIPTION = "Filter"
        private const val DEFAULT_FINISHED_LIST_BUTTON_LABEL = "Finished memos"
        private const val DEFAULT_SEARCH_BUTTON_DESCRIPTION = "Search"
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
        private const val MEMO_TITLE = "MemoHomeScreenTitle"

        private fun screenTestEnvironment(): ScreenTestEnvironment {
            val memo =
                fixtureMonkey
                    .giveMeKotlinBuilder<Memo>()
                    .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(title = MEMO_TITLE))
                    .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                    .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                    .sample()
            val pagingFlow =
                MutableStateFlow(
                    memoPagingDataOf(
                        itemList = listOf(MemoListItem.Content(memo = memo)),
                    ),
                )
            val effectChannel = Channel<MemoListEffect>(capacity = Channel.BUFFERED)
            val viewModel = mockk<MemoHomeViewModel>()
            every { viewModel.sort } returns MutableStateFlow(ListSort.DEFAULT)

            every { viewModel.memoPagingData } returns pagingFlow
            every { viewModel.filterUiState } returns MutableStateFlow(MemoHomeScaffoldFilterUiState())
            every { viewModel.effect } returns effectChannel.receiveAsFlow()
            every { viewModel.finish(id = memo.id) } answers {
                effectChannel.trySend(MemoListEffect.Finished(id = memo.id)).getOrThrow()
            }
            every { viewModel.delete(id = memo.id) } answers {
                effectChannel.trySend(MemoListEffect.Deleted(id = memo.id)).getOrThrow()
            }
            justRun { viewModel.restart(id = memo.id) }
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
    val viewModel: MemoHomeViewModel,
)
