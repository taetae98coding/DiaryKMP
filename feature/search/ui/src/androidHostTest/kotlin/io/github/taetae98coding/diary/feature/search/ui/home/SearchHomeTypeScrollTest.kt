package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.feature.search.ui.home.memo.SEARCH_HOME_MEMO_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.search.ui.resetAndroidUiDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class SearchHomeTypeScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-9b3f0ed9-6005-4f72-a6ca-6e2a9222c95d 유형을 바꿨다가 돌아오면 그 유형에서 보던 자리를 유지한다`() {
        val memoList = List(RESULT_COUNT) { resultMemo() }
        composeRule.setSearchHomeScreen(memoList = memoList)
        inputQuery()
        scrollMemoListToLast(memoList = memoList)

        composeRule.selectSearchHomeTab(WEB_TAB_LABEL)
        composeRule.selectSearchHomeTab(MEMO_TAB_LABEL)

        composeRule.onNodeWithText(memoList.last().detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-4a8fd490-8ef7-46b2-8a42-c5d6528c3e76 다른 유형을 보는 동안 질의가 바뀌면 돌아온 유형도 처음부터 본다`() {
        assertMemoListStartsFromFirstAfterReturn(appliedQueryList = listOf(OTHER_QUERY))
    }

    @Test
    fun `TC-4a8fd490-8ef7-46b2-8a42-c5d6528c3e76 다른 유형을 보는 동안 고친 질의가 반영된 뒤 원래 검색어로 되돌려도 돌아온 유형은 처음부터 본다`() {
        assertMemoListStartsFromFirstAfterReturn(appliedQueryList = listOf(OTHER_QUERY, QUERY))
    }

    private fun assertMemoListStartsFromFirstAfterReturn(appliedQueryList: List<String>) {
        val memoList = List(RESULT_COUNT) { resultMemo() }
        composeRule.setSearchHomeScreen(memoList = memoList)
        inputQuery()
        scrollMemoListToLast(memoList = memoList)

        composeRule.selectSearchHomeTab(WEB_TAB_LABEL)
        appliedQueryList.forEach { query ->
            composeRule.onNode(hasSetTextAction()).performTextReplacement(query)
            composeRule.waitForIdle()
        }
        composeRule.selectSearchHomeTab(MEMO_TAB_LABEL)

        composeRule.onNodeWithText(memoList.first().detail.title).assertIsDisplayed()
    }

    private fun scrollMemoListToLast(memoList: List<Memo>) {
        composeRule.onNodeWithTag(SEARCH_HOME_MEMO_LIST_TEST_TAG).performScrollToIndex(memoList.lastIndex)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(memoList.first().detail.title).assertDoesNotExist()
    }

    private fun inputQuery() {
        composeRule.onNode(hasSetTextAction()).performTextInput(QUERY)
        composeRule.waitForIdle()
    }

    private companion object {
        private const val QUERY = "여행"
        private const val OTHER_QUERY = "회의"
        private const val RESULT_COUNT = 30
    }
}
