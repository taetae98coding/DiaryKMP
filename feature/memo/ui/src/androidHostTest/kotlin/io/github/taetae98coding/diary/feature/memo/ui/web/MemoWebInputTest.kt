package io.github.taetae98coding.diary.feature.memo.ui.web

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoWebInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-011 선택한 웹 항목을 제목 칩으로 표시한다`() {
        val wikiWeb = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val docsWeb = testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL)

        composeRule.setMemoWebInput(uiState = MemoWebInputUiState(selectedWebList = listOf(wikiWeb, docsWeb)))

        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()
        composeRule.onNodeWithText(DOCS_WEB_TITLE).assertExists()
    }

    @Test
    fun `칩에는 제목만 표시하고 URL은 표시하지 않는다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)

        composeRule.setMemoWebInput(uiState = MemoWebInputUiState(selectedWebList = listOf(web)))

        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()
        composeRule.onNodeWithText(WIKI_WEB_URL).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-015 선택한 웹 항목의 제목이 바뀌면 웹 칩에 반영된다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val renamedWeb = web.copy(detail = web.detail.copy(title = RENAMED_WEB_TITLE))
        val selectWebList = composeRule.setMemoWebInputWithSelection()

        selectWebList(listOf(web))
        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()

        selectWebList(listOf(renamedWeb))

        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(RENAMED_WEB_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-012 선택을 해제한 웹 항목은 칩 영역에서 사라진다`() {
        val wikiWeb = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val docsWeb = testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL)
        val selectWebList = composeRule.setMemoWebInputWithSelection()

        selectWebList(listOf(wikiWeb, docsWeb))
        composeRule.onNodeWithText(DOCS_WEB_TITLE).assertExists()

        selectWebList(listOf(wikiWeb))

        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()
        composeRule.onNodeWithText(DOCS_WEB_TITLE).assertDoesNotExist()
    }

    @Test
    fun `칩이 칩 영역보다 많으면 웹 입력이 그만큼 높아진다`() {
        val webList = List(size = SCROLL_WEB_COUNT) { index -> testWeb(title = "$SCROLL_WEB_TITLE_PREFIX$index") }
        val selectWebList = composeRule.setMemoWebInputWithSelection()
        val emptyHeight = composeRule.memoWebInputHeight()

        selectWebList(webList)

        composeRule.onNodeWithText(webList.last().detail.title).assertExists()
        composeRule.memoWebInputHeight() shouldBeGreaterThan emptyHeight
    }

    @Test
    fun `칩이 많아도 칩 영역은 세로 스크롤을 가져가지 않는다`() {
        val webList = List(size = SCROLL_WEB_COUNT) { index -> testWeb(title = "$SCROLL_WEB_TITLE_PREFIX$index") }

        composeRule.setMemoWebInput(uiState = MemoWebInputUiState(selectedWebList = webList))

        composeRule.onNode(hasScrollAction()).assertDoesNotExist()
    }

    @Test
    fun `웹 항목을 선택해 칩이 칩 영역 안에 들어가면 웹 입력의 높이가 바뀌지 않는다`() {
        val webList = listOf(testWeb(title = WIKI_WEB_TITLE), testWeb(title = DOCS_WEB_TITLE))
        val selectWebList = composeRule.setMemoWebInputWithSelection()
        val emptyHeight = composeRule.memoWebInputHeight()

        selectWebList(webList)

        composeRule.onNodeWithText(WIKI_WEB_TITLE).assertExists()
        composeRule.memoWebInputHeight() shouldBe emptyHeight
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-017 웹 칩을 눌러도 추가 항목의 동작이 실행되지 않는다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        var addClickCount = 0
        composeRule.setMemoWebInput(
            uiState = MemoWebInputUiState(selectedWebList = listOf(web)),
            onAddClick = { addClickCount += 1 },
        )

        composeRule.onNodeWithText(WIKI_WEB_TITLE).performClick()

        addClickCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_WEB_PICKER_TITLE).assertDoesNotExist()
    }

    public companion object {
        private const val SCROLL_WEB_TITLE_PREFIX = "MemoWebScroll"
        private const val SCROLL_WEB_COUNT = 30
        private const val RENAMED_WEB_TITLE = "MemoWebRenamed"
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoWebInputAddChipTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 웹 입력의 문구를 표시한다`() {
        composeRule.setMemoWebInput(uiState = MemoWebInputUiState())

        composeRule.onNodeWithText(KOREAN_WEB_SELECT_LABEL).assert(hasWebClickLabel(KOREAN_WEB_SELECT_LABEL))
    }

    @Test
    fun `기본 환경에서 웹 입력의 문구를 표시한다`() {
        composeRule.setMemoWebInput(uiState = MemoWebInputUiState())

        composeRule.onNodeWithText(DEFAULT_WEB_SELECT_LABEL).assert(hasWebClickLabel(DEFAULT_WEB_SELECT_LABEL))
    }

    @Test
    fun `추가 항목을 누르면 추가 항목의 동작이 한 번 실행된다`() {
        var addClickCount = 0
        composeRule.setMemoWebInput(onAddClick = { addClickCount += 1 })

        composeRule.onNodeWithText(DEFAULT_WEB_SELECT_LABEL).performClick()

        addClickCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-002 선택한 웹 항목이 없거나 여러 개여도 웹 추가 항목이 표시된다`() {
        val webList = listOf(testWeb(title = WIKI_WEB_TITLE), testWeb(title = DOCS_WEB_TITLE))
        val selectWebList = composeRule.setMemoWebInputWithSelection()

        listOf(
            emptyList(),
            webList,
        ).forEach { selectedWebList ->
            selectWebList(selectedWebList)

            composeRule.onNodeWithText(DEFAULT_WEB_SELECT_LABEL).assertExists()
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoWebInputNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-016 웹 칩을 누르면 그 웹 항목을 대상으로 상세 이동을 요청한다`() {
        val wikiWeb = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val docsWeb = testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL)
        val clickedWebIdList = mutableListOf<Uuid>()

        composeRule.setMemoWebInput(
            uiState = MemoWebInputUiState(selectedWebList = listOf(wikiWeb, docsWeb)),
            onWebClick = { id -> clickedWebIdList += id },
        )

        composeRule.onNodeWithText(DOCS_WEB_TITLE).assert(hasWebClickLabel(DEFAULT_WEB_DETAIL_ACTION))
        composeRule.onNodeWithText(DOCS_WEB_TITLE).performClick()
        composeRule.onNodeWithText(WIKI_WEB_TITLE).performClick()

        clickedWebIdList shouldBe listOf(docsWeb.id, wikiWeb.id)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-WEB-INPUT-FEATURE-016 한국어 환경에서 웹 칩에 상세 이동 동작 이름을 제공한다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)

        composeRule.setMemoWebInput(uiState = MemoWebInputUiState(selectedWebList = listOf(web)))

        composeRule.onNodeWithText(WIKI_WEB_TITLE).assert(hasWebClickLabel(KOREAN_WEB_DETAIL_ACTION))
    }
}
