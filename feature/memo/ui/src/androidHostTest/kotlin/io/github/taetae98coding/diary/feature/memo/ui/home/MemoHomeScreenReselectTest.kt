package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoHomeScreenReselectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-FEATURE-009 메모 목적지를 다시 선택하면 메모 목록의 첫 항목으로 돌아간다`() {
        val reselectEvent = reselectEvent()
        setMemoHomeScreen(reselectEvent = reselectEvent)

        composeRule.onNodeWithTag(MEMO_HOME_LIST_TEST_TAG).performScrollToIndex(LAST_INDEX)
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(memoTitle(index = 0)).fetchSemanticsNodes().isEmpty() shouldBe true

        reselectEvent.tryEmit(Unit)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(memoTitle(index = 0)).assertIsDisplayed()
    }

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-DOMAIN-010 이미 첫 메모를 보고 있으면 목록 자리를 바꾸지 않는다`() {
        val reselectEvent = reselectEvent()
        setMemoHomeScreen(reselectEvent = reselectEvent)

        reselectEvent.tryEmit(Unit)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(memoTitle(index = 0)).assertIsDisplayed()
    }

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-DOMAIN-013 다시 선택해도 목록의 조회 조건을 바꾸지 않는다`() {
        val reselectEvent = reselectEvent()
        val viewModel = reselectTestViewModel()
        setMemoHomeScreen(
            reselectEvent = reselectEvent,
            viewModel = viewModel,
        )

        composeRule.onNodeWithTag(MEMO_HOME_LIST_TEST_TAG).performScrollToIndex(LAST_INDEX)
        composeRule.waitForIdle()

        reselectEvent.tryEmit(Unit)
        composeRule.waitForIdle()

        verify(exactly = 0) { viewModel.select(sort = any()) }
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT_LABEL).assertIsDisplayed()
        composeRule.onNode(hasStateDescription(DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION)).assertExists()
        composeRule.onNodeWithText(memoTitle(index = 0)).assertIsDisplayed()
        composeRule.onNodeWithText(memoTitle(index = 1)).assertIsDisplayed()
    }

    private fun setMemoHomeScreen(
        reselectEvent: MutableSharedFlow<Unit>,
        viewModel: MemoHomeViewModel = reselectTestViewModel(),
    ) {
        composeRule.setContent {
            DiaryTheme {
                // MemoEntry가 조립하는 것과 같은 구성으로 둔다.
                val listState = rememberLazyListState()

                ScrollToFirstMemoOnReselectEffect(
                    reselectEvent = reselectEvent,
                    listState = listState,
                )
                MemoHomeScreen(
                    navigateToAdd = {},
                    navigateToDetail = {},
                    navigateToFilter = {},
                    navigateToFinishedList = {},
                    navigateToSearch = {},
                    listState = listState,
                    memoViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                    componentVisibleProvider = { MemoHomeScaffoldComponentVisible() },
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(memoTitle(index = 0)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    public companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val MEMO_COUNT = 40
        private const val LAST_INDEX = MEMO_COUNT - 1
        private const val DEFAULT_TITLE_SORT_LABEL = "Title"
        private const val DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION = "Filter applied"
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memoTitle(index: Int): String = "MemoHomeReselectTitle$index"

        private fun reselectEvent(): MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

        private fun reselectTestViewModel(): MemoHomeViewModel {
            val itemList =
                List(MEMO_COUNT) { index ->
                    val memo =
                        fixtureMonkey
                            .giveMeKotlinBuilder<Memo>()
                            .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(title = memoTitle(index = index)))
                            .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                            .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                            .sample()

                    MemoListItem.Content(memo = memo)
                }
            val viewModel = mockk<MemoHomeViewModel>()

            every { viewModel.sort } returns MutableStateFlow(ListSort.TITLE)
            every { viewModel.memoPagingData } returns MutableStateFlow(memoPagingDataOf(itemList = itemList))
            every {
                viewModel.filterUiState
            } returns
                MutableStateFlow(
                    MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(date = MemoFilterExistence.EXIST)),
                )
            every { viewModel.effect } returns emptyFlow()

            return viewModel
        }
    }
}
