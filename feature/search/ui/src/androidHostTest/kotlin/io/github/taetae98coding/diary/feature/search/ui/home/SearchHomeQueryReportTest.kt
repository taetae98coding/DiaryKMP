package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
    fun `TC-79faf25e-ec2c-44fc-8ae9-ccd46eb0f997 진입하면 빈 질의로 시작한다`() {
        val queryList = mutableListOf<String>()
        setSearchHomeQueryReport(queryList = queryList)

        queryList shouldBe listOf("")
    }

    @Test
    fun `TC-346988d0-4bef-426f-84c5-0f9054e4072e 질의를 입력하면 확정 동작 없이 그 질의를 전달한다`() {
        val queryList = mutableListOf<String>()
        setSearchHomeQueryReport(queryList = queryList)

        composeRule.onNode(hasSetTextAction()).performTextInput(QUERY)
        composeRule.waitForIdle()

        queryList shouldBe listOf("", QUERY)
    }

    @Test
    fun `TC-bfe7428a-4630-4061-a412-7e2dfb88cd9a 질의를 바꾸면 바뀐 질의를 전달한다`() {
        val queryList = mutableListOf<String>()
        setSearchHomeQueryReport(queryList = queryList)

        composeRule.onNode(hasSetTextAction()).performTextInput(QUERY)
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction()).performTextClearance()
        composeRule.onNode(hasSetTextAction()).performTextInput(OTHER_QUERY)
        composeRule.waitForIdle()

        queryList.last() shouldBe OTHER_QUERY
    }

    @Test
    fun `TC-c4fe9d3f-01c4-4b1a-a7bb-2fd1ca334fdb 질의를 비우면 빈 질의를 전달한다`() {
        val queryList = mutableListOf<String>()
        setSearchHomeQueryReport(queryList = queryList)

        composeRule.onNode(hasSetTextAction()).performTextInput(QUERY)
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction()).performTextClearance()
        composeRule.waitForIdle()

        queryList.last() shouldBe ""
    }

    @Test
    fun `같은 질의가 이어지면 다시 전달하지 않는다`() {
        val queryList = mutableListOf<String>()
        setSearchHomeQueryReport(queryList = queryList)

        composeRule.onNode(hasSetTextAction()).performTextInput(QUERY)
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction()).performTextClearance()
        composeRule.onNode(hasSetTextAction()).performTextInput(QUERY)
        composeRule.waitForIdle()

        queryList shouldBe listOf("", QUERY, "", QUERY)
    }

    private fun setSearchHomeQueryReport(queryList: MutableList<String>) {
        composeRule.setContent {
            DiaryTheme {
                val state = rememberSearchHomeScaffoldState()

                SearchHomeQueryEffect(
                    queryState = state.queryState,
                    onQueryChange = { query -> queryList += query },
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
