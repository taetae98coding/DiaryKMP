package io.github.taetae98coding.diary.feature.memo.ui.web

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performClick
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoWebPickerDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-001 목록에 웹 항목의 제목과 URL, 선택 여부를 함께 표시한다`() {
        val selectedWeb = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val unselectedWeb = testWeb(title = DOCS_WEB_TITLE, url = DOCS_WEB_URL)
        composeRule.setMemoWebPickerDialog(
            webList = listOf(selectedWeb, unselectedWeb),
            uiState = MemoWebInputUiState(selectedWebList = listOf(selectedWeb)),
        )
        composeRule.awaitWebPickerRows()

        composeRule.webDialogNodeWithText(WIKI_WEB_TITLE).assertExists()
        composeRule.webDialogNodeWithText(WIKI_WEB_URL).assertExists()
        composeRule.webDialogNodeWithText(DOCS_WEB_TITLE).assertExists()
        composeRule.webDialogNodeWithText(DOCS_WEB_URL).assertExists()
        composeRule.onAllNodes(isToggleable() and hasAnyAncestor(isDialog())).onFirst().assertIsOn()
        composeRule.onAllNodes(isToggleable() and hasAnyAncestor(isDialog())).onLast().assertIsOff()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-011 목록에서 웹 항목을 선택하면 선택 행동을 즉시 전달한다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val selectedIdList = mutableListOf<Uuid>()
        val unselectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoWebPickerDialog(
            webList = listOf(web),
            onWebSelect = selectedIdList::add,
            onWebUnselect = unselectedIdList::add,
        )
        composeRule.awaitWebPickerRows()

        composeRule.webDialogNodeWithText(WIKI_WEB_TITLE).performClick()

        selectedIdList shouldBe listOf(web.id)
        unselectedIdList shouldBe emptyList()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-012 목록에서 선택을 해제하면 해제 행동을 즉시 전달한다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val selectedIdList = mutableListOf<Uuid>()
        val unselectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoWebPickerDialog(
            webList = listOf(web),
            uiState = MemoWebInputUiState(selectedWebList = listOf(web)),
            onWebSelect = selectedIdList::add,
            onWebUnselect = unselectedIdList::add,
        )
        composeRule.awaitWebPickerRows()

        composeRule.webDialogNodeWithText(WIKI_WEB_TITLE).performClick()

        unselectedIdList shouldBe listOf(web.id)
        selectedIdList shouldBe emptyList()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-DOMAIN-002 목록의 웹 항목은 전달된 제목 오름차순으로 표시된다`() {
        val firstWeb = testWeb(title = APPLE_WEB_TITLE, url = WIKI_WEB_URL)
        val lastWeb = testWeb(title = ZEBRA_WEB_TITLE, url = DOCS_WEB_URL)
        composeRule.setMemoWebPickerDialog(webList = listOf(firstWeb, lastWeb))
        composeRule.awaitWebPickerRows()

        val firstTop = composeRule.webDialogNodeWithText(APPLE_WEB_TITLE).getUnclippedBoundsInRoot().top
        val lastTop = composeRule.webDialogNodeWithText(ZEBRA_WEB_TITLE).getUnclippedBoundsInRoot().top

        firstTop shouldBeLessThan lastTop
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-022 목록의 웹 항목을 눌러도 상세 이동을 요청하지 않는다`() {
        val web = testWeb(title = WIKI_WEB_TITLE, url = WIKI_WEB_URL)
        val selectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoWebPickerDialog(webList = listOf(web), onWebSelect = selectedIdList::add)
        composeRule.awaitWebPickerRows()

        // 목록 항목에는 상세 이동 동작 이름이 없고 선택만 전달한다.
        composeRule
            .onAllNodes(hasWebClickLabel(DEFAULT_WEB_DETAIL_ACTION) and hasAnyAncestor(isDialog()))
            .assertCountEquals(0)
        composeRule.webDialogNodeWithText(WIKI_WEB_TITLE).performClick()

        selectedIdList shouldBe listOf(web.id)
    }

    @Test
    fun `목록에 확인 버튼을 두지 않는다`() {
        composeRule.setMemoWebPickerDialog(webList = listOf(testWeb(title = WIKI_WEB_TITLE)))
        composeRule.awaitWebPickerRows()

        composeRule.webDialogNodeWithText(DEFAULT_CONFIRM_LABEL).assertDoesNotExist()
    }

    @Test
    fun `기본 환경에서 웹 선택 목록의 문구를 표시한다`() {
        composeRule.setMemoWebPickerDialog()

        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_TITLE).assertExists()
        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_ADD_LABEL).assertExists()
        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_SEARCH_PLACEHOLDER).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 웹 선택 목록의 문구를 표시한다`() {
        composeRule.setMemoWebPickerDialog()

        composeRule.webDialogNodeWithText(KOREAN_WEB_PICKER_TITLE).assertExists()
        composeRule.webDialogNodeWithText(KOREAN_WEB_PICKER_ADD_LABEL).assertExists()
        composeRule.webDialogNodeWithText(KOREAN_WEB_PICKER_SEARCH_PLACEHOLDER).assertExists()
    }

    public companion object {
        private const val APPLE_WEB_TITLE = "AppleWeb"
        private const val ZEBRA_WEB_TITLE = "ZebraWeb"
        private const val DEFAULT_CONFIRM_LABEL = "Confirm"
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoWebPickerDialogAddTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-006 나타낼 웹 항목이 없어도 웹 추가 항목을 표시한다`() {
        composeRule.setMemoWebPickerDialog(webList = emptyList())

        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_ADD_LABEL).assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-006 나타낼 웹 항목이 여러 개여도 웹 추가 항목을 표시한다`() {
        composeRule.setMemoWebPickerDialog(webList = listOf(testWeb(title = WIKI_WEB_TITLE), testWeb(title = DOCS_WEB_TITLE)))
        composeRule.awaitWebPickerRows()

        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_ADD_LABEL).assertExists()
    }

    @Test
    fun `TC-MEMO-WEB-INPUT-FEATURE-007 웹 추가 항목을 누르면 웹 추가 행동만 전달한다`() {
        val web = testWeb(title = WIKI_WEB_TITLE)
        var webAddCount = 0
        val selectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoWebPickerDialog(
            webList = listOf(web),
            onWebSelect = selectedIdList::add,
            onWebAdd = { webAddCount += 1 },
        )
        composeRule.awaitWebPickerRows()

        composeRule.webDialogNodeWithText(DEFAULT_WEB_PICKER_ADD_LABEL).performClick()

        webAddCount shouldBe 1
        selectedIdList shouldBe emptyList()
    }

    @Test
    fun `웹 추가 항목에 웹 추가 동작 이름을 제공한다`() {
        composeRule.setMemoWebPickerDialog()

        composeRule
            .onAllNodes(hasWebClickLabel(DEFAULT_WEB_PICKER_ADD_LABEL) and hasAnyAncestor(isDialog()))
            .assertCountEquals(1)
    }
}
