package io.github.taetae98coding.diary.feature.memo.ui.web

import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoWebPickerDialogSearchTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-023 목록을 열면 검색어가 비어 있고 대상 웹 항목이 모두 나타난다`() {
        val webList = listOf(testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL), testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL))
        val queryList = mutableListOf<String>()
        composeRule.setMemoWebPickerDialogHost(webList = webList, onQueryChange = queryList::add)
        composeRule.awaitWebPickerRows()

        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_SEARCH_PLACEHOLDER).assertExists()
        composeRule.webDialogNodeWithText(WIKI_WEB_TITLE).assertExists()
        composeRule.webDialogNodeWithText(DOCS_WEB_TITLE).assertExists()
        queryList shouldBe listOf("")
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-030 목록을 열면 검색어 입력에 초점이 놓인다`() {
        // 터치 모드가 아니면 대화상자가 첫 입력에 스스로 초점을 주므로, 휴대폰과 같은 터치 모드에서 확인한다.
        InstrumentationRegistry.getInstrumentation().setInTouchMode(true)
        composeRule.setMemoWebPickerDialogHost(webList = listOf(testWeb(title = WIKI_WEB_TITLE), testWeb(title = DOCS_WEB_TITLE)))
        composeRule.awaitWebPickerRows()

        composeRule.webDialogSearchField().assertIsFocused()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-024 검색어를 입력하면 확정 동작 없이 그 검색어가 즉시 반영된다`() {
        val webList = listOf(testWeb(title = WIKI_WEB_TITLE), testWeb(title = DOCS_WEB_TITLE))
        val queryList = mutableListOf<String>()
        composeRule.setMemoWebPickerDialogHost(webList = webList, onQueryChange = queryList::add)
        composeRule.awaitWebPickerRows()

        composeRule.webDialogSearchField().performTextInput(WIKI_WEB_QUERY)
        composeRule.waitForIdle()

        queryList.last() shouldBe WIKI_WEB_QUERY
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-025 검색어를 지우면 검색어가 없는 상태가 즉시 반영된다`() {
        val webList = listOf(testWeb(title = WIKI_WEB_TITLE), testWeb(title = DOCS_WEB_TITLE))
        val queryList = mutableListOf<String>()
        composeRule.setMemoWebPickerDialogHost(webList = webList, onQueryChange = queryList::add)
        composeRule.webDialogSearchField().performTextInput(WIKI_WEB_QUERY)
        composeRule.waitForIdle()

        composeRule.webDialogSearchField().performTextClearance()
        composeRule.waitForIdle()

        queryList.last() shouldBe ""
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-026 검색어로 좁힌 목록에서도 선택을 전달한다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val selectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoWebPickerDialogHost(webList = listOf(web), onWebSelect = selectedIdList::add)
        composeRule.awaitWebPickerRows()
        composeRule.webDialogSearchField().performTextInput(WIKI_WEB_QUERY)
        composeRule.waitForIdle()

        // 검색 입력에도 같은 문자열이 들어 있으므로 목록 항목은 URL로 가려 누른다.
        composeRule.webDialogNodeWithText(WIKI_WEB_URL).performClick()
        composeRule.waitForIdle()

        selectedIdList shouldBe listOf(web.id)
        composeRule.webDialogNodeWithText(WIKI_WEB_URL).assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-024 검색어가 있으면 그 검색어로 좁힌 조회 결과만 목록에 나타난다`() {
        // 좁힌 조회 결과는 목록을 처음 그릴 때 넘긴다. 검색 입력에도 검색어가 들어 있으므로 항목은 URL로 가린다.
        composeRule.setMemoWebPickerDialog(webList = listOf(testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)), query = WIKI_WEB_QUERY)
        composeRule.awaitWebPickerRows()

        composeRule.webDialogNodeWithText(WIKI_WEB_URL).assertExists()
        composeRule.webDialogNodeWithText(DOCS_WEB_TITLE).assertDoesNotExist()
        composeRule.webDialogNodeWithText(DOCS_WEB_URL).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-025 검색어가 없으면 대상 전체의 조회 결과가 목록에 나타난다`() {
        val webList = listOf(testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL), testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL))
        composeRule.setMemoWebPickerDialog(webList = webList, query = "")
        composeRule.awaitWebPickerRows()

        composeRule.webDialogNodeWithText(WIKI_WEB_URL).assertExists()
        composeRule.webDialogNodeWithText(DOCS_WEB_URL).assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-026 검색어로 좁힌 목록에서 선택한 웹 항목을 누르면 해제를 전달하고 목록에 남는다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val unselectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoWebPickerDialog(
            webList = listOf(web),
            uiState = MemoWebInputUiState(selectedWebList = listOf(web)),
            query = WIKI_WEB_QUERY,
            onWebUnselect = unselectedIdList::add,
        )
        composeRule.awaitWebPickerRows()

        composeRule.webDialogNodeWithText(WIKI_WEB_URL).performClick()
        composeRule.waitForIdle()

        unselectedIdList shouldBe listOf(web.id)
        composeRule.webDialogNodeWithText(WIKI_WEB_URL).assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-027 검색어에 맞는 웹 항목이 없으면 결과 없음을 알린다`() {
        composeRule.setMemoWebPickerDialogHost(webList = emptyList())
        composeRule.webDialogSearchField().performTextInput(WIKI_WEB_QUERY)
        composeRule.waitForIdle()

        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_SEARCH_EMPTY_TITLE).assertExists()
        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_SEARCH_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 웹 선택 목록의 검색 결과 없음 문구를 표시한다`() {
        composeRule.setMemoWebPickerDialogHost(webList = emptyList())
        composeRule.webDialogSearchField().performTextInput(WIKI_WEB_QUERY)
        composeRule.waitForIdle()

        composeRule.webDialogNodeWithText(KOREAN_WEB_PICKER_SEARCH_EMPTY_TITLE).assertExists()
        composeRule.webDialogNodeWithText(KOREAN_WEB_PICKER_SEARCH_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-005 검색어가 비어 있으면 나타낼 웹 항목이 없어도 결과 없음을 알리지 않는다`() {
        composeRule.setMemoWebPickerDialogHost(webList = emptyList())
        composeRule.waitForIdle()

        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_SEARCH_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-028 검색 결과가 없어도 웹 추가 항목으로 이동을 전달한다`() {
        var webAddCount = 0
        composeRule.setMemoWebPickerDialogHost(webList = emptyList(), onWebAdd = { webAddCount += 1 })
        composeRule.webDialogSearchField().performTextInput(WIKI_WEB_QUERY)
        composeRule.waitForIdle()

        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_ADD_LABEL).performClick()
        composeRule.waitForIdle()

        webAddCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-029 목록을 닫았다가 다시 열면 검색어가 비어 있다`() {
        val webList = listOf(testWeb(title = WIKI_WEB_TITLE), testWeb(title = DOCS_WEB_TITLE))
        val dialogState = DialogState(isVisible = true)
        val queryList = mutableListOf<String>()
        composeRule.setMemoWebPickerDialogHost(dialogState = dialogState, webList = webList, onQueryChange = queryList::add)
        composeRule.webDialogSearchField().performTextInput(WIKI_WEB_QUERY)
        composeRule.waitForIdle()

        composeRule.runOnIdle { dialogState.hide() }
        composeRule.waitForIdle()
        composeRule.runOnIdle { dialogState.show() }
        composeRule.awaitWebPickerRows()

        queryList.last() shouldBe ""
        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_SEARCH_PLACEHOLDER).assertExists()
        composeRule.webDialogNodeWithText(WIKI_WEB_TITLE).assertExists()
        composeRule.webDialogNodeWithText(DOCS_WEB_TITLE).assertExists()
    }

    public companion object {
        private const val WIKI_WEB_QUERY = "Wiki"
    }
}
