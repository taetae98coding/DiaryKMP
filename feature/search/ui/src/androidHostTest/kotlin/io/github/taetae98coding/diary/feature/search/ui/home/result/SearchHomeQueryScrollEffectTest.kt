package io.github.taetae98coding.diary.feature.search.ui.home.result

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SEARCH_HOME_MEMO_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SearchHomeMemoList
import io.github.taetae98coding.diary.feature.search.ui.home.pagingDataFlowOf
import io.github.taetae98coding.diary.feature.search.ui.home.resultMemo
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class SearchHomeQueryScrollEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SEARCH-HOME-FEATURE-026 질의를 바꾸면 결과 목록이 맨 위에서 다시 시작한다`() {
        val query = mutableStateOf(QUERY)
        val listState = setMemoListWithScrollEffect(query = query)

        scrollToLast(listState = listState)

        composeRule.runOnIdle { query.value = OTHER_QUERY }
        composeRule.waitForIdle()

        listState.firstVisibleItemIndex shouldBe 0
    }

    @Test
    fun `반영된 질의가 같은 값으로 다시 반영되어도 목록 자리를 유지한다`() {
        val query = mutableStateOf(QUERY)
        val listState = setMemoListWithScrollEffect(query = query)

        scrollToLast(listState = listState)
        val scrolledIndex = listState.firstVisibleItemIndex

        composeRule.runOnIdle { query.value = QUERY }
        composeRule.waitForIdle()

        listState.firstVisibleItemIndex shouldBe scrolledIndex
    }

    private fun scrollToLast(listState: LazyListState) {
        composeRule.onNodeWithTag(SEARCH_HOME_MEMO_LIST_TEST_TAG).performScrollToIndex(RESULT_COUNT - 1)
        composeRule.waitForIdle()

        listState.firstVisibleItemIndex shouldBeGreaterThan 0
    }

    private fun setMemoListWithScrollEffect(query: MutableState<String>): LazyListState {
        val memoPagingDataFlow = pagingDataFlowOf(List(RESULT_COUNT) { resultMemo() })
        lateinit var listState: LazyListState

        composeRule.setContent {
            DiaryTheme {
                val currentQuery by query

                listState = rememberLazyListState()

                SearchHomeQueryScrollEffect(
                    listState = listState,
                    queryProvider = { currentQuery },
                )
                SearchHomeMemoList(
                    onEvent = {},
                    onMemoListEvent = {},
                    modifier = Modifier.fillMaxSize(),
                    listState = listState,
                    memoPagingItems = memoPagingDataFlow.collectAsLazyPagingItems(),
                    query = currentQuery,
                )
            }
        }
        composeRule.waitForIdle()

        return listState
    }

    private companion object {
        private const val QUERY = "여행"
        private const val OTHER_QUERY = "회의"
        private const val RESULT_COUNT = 30
    }
}
