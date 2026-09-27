package io.github.taetae98coding.diary.feature.memo.ui.finished

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.Lifecycle
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.feature.memo.ui.home.memoPagingDataOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class MemoFinishedListExecutionBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `TC-MEMO-FINISHED-LIST-FEATURE-033 메모 상세에서 돌아오면 목록에서 보던 자리가 그대로다`() {
        val memoList = memoList()
        val viewModel = screenTestViewModel(memoList = memoList)
        val navigatedIdList = mutableListOf<Uuid>()
        var isFinishedListOnTop by mutableStateOf(true)
        composeRule.setContent {
            // 내비게이션이 뒤에 쌓인 화면을 컴포지션에서 내리고 저장 상태만 보관하는 것을 그대로 따른다.
            val saveableStateHolder = rememberSaveableStateHolder()
            if (isFinishedListOnTop) {
                saveableStateHolder.SaveableStateProvider(key = MEMO_FINISHED_LIST_ENTRY_KEY) {
                    FinishedList(
                        viewModel = viewModel,
                        navigateToDetail = { id ->
                            navigatedIdList.add(id)
                            isFinishedListOnTop = false
                        },
                    )
                }
            }
        }
        scrollList(memoList)

        composeRule.onNodeWithText(memoList[SCROLLED_INDEX].detail.title).performClick()
        composeRule.waitForIdle()
        navigatedIdList shouldBe listOf(memoList[SCROLLED_INDEX].id)
        composeRule.runOnIdle { isFinishedListOnTop = true }
        composeRule.waitForIdle()

        assertScrolledPosition(memoList)
    }

    @Test
    fun `TC-MEMO-FINISHED-LIST-DOMAIN-010 앱이 백그라운드에 다녀와도 정렬 선택과 보던 자리가 그대로다`() {
        val memoList = memoList()
        val viewModel = screenTestViewModel(memoList = memoList, sort = ListSort.TITLE)
        composeRule.setContent { FinishedList(viewModel = viewModel, navigateToDetail = {}) }
        scrollList(memoList)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_DEFAULT_SORT).assertDoesNotExist()
        assertScrolledPosition(memoList)
    }

    @Composable
    private fun FinishedList(
        viewModel: MemoFinishedListViewModel,
        navigateToDetail: (Uuid) -> Unit,
    ) {
        DiaryTheme {
            MemoFinishedListScreen(
                navigateUp = {},
                navigateToDetail = navigateToDetail,
                memoViewModel = viewModel,
                syncViewModel = screenTestSyncViewModel(),
            )
        }
    }

    private fun scrollList(memoList: List<Memo>) {
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(memoList.first().detail.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(MEMO_FINISHED_LIST_TEST_TAG).performScrollToIndex(SCROLLED_INDEX)
        composeRule.waitForIdle()
    }

    private fun assertScrolledPosition(memoList: List<Memo>) {
        composeRule.onNodeWithText(memoList[SCROLLED_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()
    }

    private fun screenTestViewModel(
        memoList: List<Memo>,
        sort: ListSort = ListSort.DEFAULT,
    ): MemoFinishedListViewModel {
        val viewModel = mockk<MemoFinishedListViewModel>(relaxed = true)
        every { viewModel.sort } returns MutableStateFlow(sort)
        every { viewModel.memoPagingData } returns MutableStateFlow(memoPagingDataOf(memoList.map { memo -> MemoListItem.Content(memo = memo) }))
        every { viewModel.effect } returns emptyFlow()
        return viewModel
    }

    private fun memoList(): List<Memo> {
        val titlePrefix = "FinishedBoundaryMemo${fixtureMonkey.giveMeOne<Int>()}"

        return List(MEMO_COUNT) { index ->
            val memo = fixtureMonkey.memo(isFinished = true, isDeleted = false, primaryTagId = null)
            memo.copy(detail = memo.detail.copy(title = "${titlePrefix}Index$index", dateTime = null))
        }
    }

    private companion object {
        private const val MEMO_FINISHED_LIST_ENTRY_KEY = "MemoFinishedList"
        private const val MEMO_COUNT = 40
        private const val SCROLLED_INDEX = 30
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val DEFAULT_DEFAULT_SORT = "Default"
        private const val DEFAULT_TITLE_SORT = "Title"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
