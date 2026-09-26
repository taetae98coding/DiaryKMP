package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
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
        val queryList = mutableListOf<QueryReport>()
        setSearchHomeQueryReport(queryList = queryList)

        queryList shouldBe listOf(QueryReport.Show(""))
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-002 질의를 입력하면 확정 동작 없이 그 질의를 전달한다`() {
        val queryList = mutableListOf<QueryReport>()
        setSearchHomeQueryReport(queryList = queryList)

        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(QUERY)
        composeRule.waitForIdle()

        queryList shouldBe listOf(QueryReport.Show(""), QueryReport.Change(QUERY))
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-003 질의를 바꾸면 바뀐 질의를 전달한다`() {
        val queryList = mutableListOf<QueryReport>()
        setSearchHomeQueryReport(queryList = queryList)

        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(QUERY)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(OTHER_QUERY)
        composeRule.waitForIdle()

        queryList.last() shouldBe QueryReport.Change(OTHER_QUERY)
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-004 질의를 비우면 빈 질의를 전달한다`() {
        val queryList = mutableListOf<QueryReport>()
        setSearchHomeQueryReport(queryList = queryList)

        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(QUERY)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextClearance()
        composeRule.waitForIdle()

        queryList.last() shouldBe QueryReport.Change("")
    }

    @Test
    fun `같은 질의가 이어지면 다시 전달하지 않는다`() {
        val queryList = mutableListOf<QueryReport>()
        setSearchHomeQueryReport(queryList = queryList)

        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(QUERY)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(QUERY)
        composeRule.waitForIdle()

        queryList shouldBe listOf(QueryReport.Show(""), QueryReport.Change(QUERY), QueryReport.Change(""), QueryReport.Change(QUERY))
    }

    @Test
    fun `TC-SEARCH-HOME-FEATURE-048 유형 결과가 다시 나타나면 지금 질의를 곧바로 반영하도록 알린다`() {
        val queryList = mutableListOf<QueryReport>()
        var isResultShown by mutableStateOf(true)
        setSearchHomeQueryReport(queryList = queryList, isResultShownProvider = { isResultShown })
        composeRule.runOnIdle { isResultShown = false }
        composeRule.onNodeWithTag(SEARCH_HOME_QUERY_INPUT_TEST_TAG).performTextInput(OTHER_QUERY)
        composeRule.waitForIdle()

        composeRule.runOnIdle { isResultShown = true }
        composeRule.waitForIdle()

        queryList shouldBe listOf(QueryReport.Show(""), QueryReport.Show(OTHER_QUERY))
    }

    private fun setSearchHomeQueryReport(
        queryList: MutableList<QueryReport>,
        isResultShownProvider: () -> Boolean = { true },
    ) {
        composeRule.setContent {
            DiaryTheme {
                val state = rememberSearchHomeScaffoldState()

                // 유형 결과가 화면에 있는 동안에만 질의를 받으므로, 결과가 사라졌다 나타나는 것을 유형 전환으로 본다.
                if (isResultShownProvider()) {
                    SearchHomeQueryEffect(
                        queryState = state.queryState,
                        onQueryShow = { query -> queryList += QueryReport.Show(query) },
                        onQueryChange = { query -> queryList += QueryReport.Change(query) },
                    )
                }
                SearchHomeTopBar(
                    onEvent = {},
                    state = state,
                )
            }
        }
    }

    private sealed interface QueryReport {
        data class Show(
            val query: String,
        ) : QueryReport

        data class Change(
            val query: String,
        ) : QueryReport
    }

    public companion object {
        private const val QUERY = "여행"
        private const val OTHER_QUERY = "회의"
    }
}
