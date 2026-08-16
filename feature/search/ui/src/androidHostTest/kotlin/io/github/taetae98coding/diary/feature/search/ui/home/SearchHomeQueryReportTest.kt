package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.effect.DiarySearchQueryEffect
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SearchHomeQueryReportTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SEARCH-HOME-FEATURE-001 진입하면 빈 질의로 시작한다`() {
        val queryList = mutableListOf<String>()
        setSearchHomeQueryReport(onQueryChange = queryList::add)

        queryList shouldBe listOf("")
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-002 질의를 입력하면 확정 동작 없이 그 질의를 전달한다`() {
        val queryList = mutableListOf<String>()
        setSearchHomeQueryReport(onQueryChange = queryList::add)

        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(QUERY)
        composeRule.waitForIdle()

        queryList shouldBe listOf("", QUERY)
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-003 질의를 바꾸면 바뀐 질의를 전달한다`() {
        val queryList = mutableListOf<String>()
        setSearchHomeQueryReport(onQueryChange = queryList::add)

        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(QUERY)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(OTHER_QUERY)
        composeRule.waitForIdle()

        queryList.last() shouldBe OTHER_QUERY
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-004 질의를 비우면 빈 질의를 전달한다`() {
        val queryList = mutableListOf<String>()
        setSearchHomeQueryReport(onQueryChange = queryList::add)

        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(QUERY)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextClearance()
        composeRule.waitForIdle()

        queryList.last() shouldBe ""
    }

    @Test
    fun `같은 질의가 이어지면 다시 전달하지 않는다`() {
        val queryList = mutableListOf<String>()
        setSearchHomeQueryReport(onQueryChange = queryList::add)

        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(QUERY)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(QUERY)
        composeRule.waitForIdle()

        queryList shouldBe listOf("", QUERY, "", QUERY)
    }

    private fun setSearchHomeQueryReport(onQueryChange: (String) -> Unit) {
        composeRule.setContent {
            DiaryTheme {
                val state = rememberSearchHomeScaffoldState()

                DiarySearchQueryEffect(
                    queryState = state.queryState,
                    onQueryChange = onQueryChange,
                )
                SearchHomeTopBar(
                    onEvent = {},
                    state = state,
                )
            }
        }
    }

    public companion object {
        private const val QUERY = "여행"
        private const val OTHER_QUERY = "회의"
    }
}
