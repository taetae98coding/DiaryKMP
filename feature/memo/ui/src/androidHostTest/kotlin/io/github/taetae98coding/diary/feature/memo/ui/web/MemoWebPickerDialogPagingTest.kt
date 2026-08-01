package io.github.taetae98coding.diary.feature.memo.ui.web

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.height
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoWebPickerDialogPagingTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-019 다음 웹 항목을 불러오는 동안에도 이미 나타난 웹 항목을 조작할 수 있다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val selectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoWebPickerDialog(
            webPagingData = appendingWebPagingDataOf(listOf(web)),
            onWebSelect = selectedIdList::add,
        )

        composeRule.webDialogNodeWithText(WIKI_WEB_TITLE).performClick()
        composeRule.waitForIdle()

        selectedIdList shouldBe listOf(web.id)
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-020 다음 웹 항목을 불러오지 못해도 이미 나타난 웹 항목을 유지한다`() {
        val wikiWeb = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val docsWeb = testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL)
        composeRule.setMemoWebPickerDialog(webPagingData = appendFailedWebPagingDataOf(listOf(wikiWeb, docsWeb)))

        composeRule.webDialogNodeWithText(WIKI_WEB_TITLE).assertExists()
        composeRule.webDialogNodeWithText(DOCS_WEB_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_WEB_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-005 목록을 처음 불러오는 중에는 목록 자리를 비워 둔다`() {
        composeRule.setMemoWebPickerDialog(webPagingData = refreshingWebPagingData())

        composeRule.onNodeWithText(DEFAULT_WEB_PICKER_TITLE).assertExists()
        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_SEARCH_EMPTY_TITLE).assertDoesNotExist()
        composeRule
            .webPickerList()
            .fetchSemanticsNode()
            .children
            .shouldBeEmpty()
    }

    @Test
    fun `준비되지 않은 자리는 준비된 항목과 같은 높이의 빈 항목으로 표시한다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val unselectedIdList = mutableListOf<Uuid>()

        composeRule.setContent {
            DiaryTheme {
                Column {
                    MemoWebPickerRow(
                        onEvent = { event -> if (event is MemoWebPickerEvent.Unselect) unselectedIdList += event.id },
                        web = web,
                        isSelected = true,
                        modifier = Modifier.testTag(PREPARED_ROW_TEST_TAG),
                    )
                    MemoWebPickerRow(
                        onEvent = { event -> if (event is MemoWebPickerEvent.Unselect) unselectedIdList += event.id },
                        web = null,
                        isSelected = false,
                        modifier = Modifier.testTag(PLACEHOLDER_ROW_TEST_TAG),
                    )
                }
            }
        }

        val placeholder = composeRule.onNodeWithTag(PLACEHOLDER_ROW_TEST_TAG)
        placeholder.performClick()
        composeRule.waitForIdle()

        placeholder.getUnclippedBoundsInRoot().height shouldBe
            composeRule
                .onNodeWithTag(PREPARED_ROW_TEST_TAG)
                .getUnclippedBoundsInRoot()
                .height
        unselectedIdList.shouldBeEmpty()
    }

    public companion object {
        private const val PREPARED_ROW_TEST_TAG: String = "PreparedWebPickerRow"
        private const val PLACEHOLDER_ROW_TEST_TAG: String = "PlaceholderWebPickerRow"
    }
}
