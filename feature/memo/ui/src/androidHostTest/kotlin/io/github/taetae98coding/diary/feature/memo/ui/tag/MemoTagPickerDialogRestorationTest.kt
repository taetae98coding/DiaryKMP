package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextInput
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoTagPickerDialogRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-TAG-INPUT-DOMAIN-019 화면이 회전해도 열려 있는 목록의 검색어와 좁힌 결과가 유지된다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val exerciseTag = testTag(title = EXERCISE_TAG_TITLE)
        val tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(listOf(workTag, exerciseTag)))
        val queryList = mutableListOf<String>()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                MemoTagPickerDialogHost(
                    dialogState = rememberDialogState(initialVisible = true),
                    onEvent = { event ->
                        if (event is MemoTagPickerEvent.ChangeQuery) {
                            queryList += event.query
                            val narrowedList = if (event.query.isEmpty()) listOf(workTag, exerciseTag) else listOf(workTag)
                            tagPagingDataFlow.value = tagPagingDataOf(narrowedList)
                        }
                    },
                    tagPagingItems = tagPagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        queryList.last() shouldBe WORK_TAG_QUERY
        // 재생성은 목록을 닫는 것이 아니므로 빈 검색어를 보고하지 않는다. 목록을 열 때 보고한 한 번만 있어야 한다.
        queryList.count { query -> query.isEmpty() } shouldBe 1
        composeRule.dialogNodeWithText(WORK_TAG_QUERY).assertExists()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertDoesNotExist()
    }
}
