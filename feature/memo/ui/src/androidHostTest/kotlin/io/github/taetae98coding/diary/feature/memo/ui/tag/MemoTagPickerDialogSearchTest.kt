package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
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
class MemoTagPickerDialogSearchTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-037 목록을 열면 검색어가 비어 있고 대상 태그가 모두 나타난다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        val queryList = mutableListOf<String>()
        composeRule.setMemoTagPickerDialogHost(tagList = tagList, onQueryChange = queryList::add)
        composeRule.awaitTagPickerRows()

        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_PLACEHOLDER).assertExists()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        queryList shouldBe listOf("")
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-038 검색어를 입력하면 확정 동작 없이 그 검색어가 즉시 반영된다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        val queryList = mutableListOf<String>()
        composeRule.setMemoTagPickerDialogHost(tagList = tagList, onQueryChange = queryList::add)
        composeRule.awaitTagPickerRows()

        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)
        composeRule.waitForIdle()

        queryList.last() shouldBe WORK_TAG_QUERY
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-039 검색어를 지우면 검색어가 없는 상태가 즉시 반영된다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        val queryList = mutableListOf<String>()
        composeRule.setMemoTagPickerDialogHost(tagList = tagList, onQueryChange = queryList::add)
        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)
        composeRule.waitForIdle()

        composeRule.dialogSearchField().performTextClearance()
        composeRule.waitForIdle()

        queryList.last() shouldBe ""
        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_PLACEHOLDER).assertExists()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-040 검색어로 좁힌 목록에서도 선택과 대표 태그 지정을 전달한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val selectedIdList = mutableListOf<Uuid>()
        val primarySelectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoTagPickerDialog(
            tagList = listOf(workTag),
            queryState = TextFieldState(initialText = WORK_TAG_QUERY),
            onTagSelect = selectedIdList::add,
            onPrimaryTagSelect = primarySelectedIdList::add,
        )

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()
        composeRule.waitForIdle()

        selectedIdList shouldBe listOf(workTag.id)
        primarySelectedIdList shouldBe listOf(workTag.id)
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-041 검색어에 맞는 태그가 없으면 결과 없음을 알린다`() {
        composeRule.setMemoTagPickerDialog(
            tagList = emptyList(),
            queryState = TextFieldState(initialText = WORK_TAG_QUERY),
        )

        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_EMPTY_TITLE).assertExists()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_EMPTY_DESCRIPTION).assertExists()
        composeRule.onAllNodes(isToggleable() and hasAnyAncestor(isDialog())).assertCountEquals(0)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 태그 선택 목록의 검색 문구를 표시한다`() {
        composeRule.setMemoTagPickerDialog(
            tagList = emptyList(),
            queryState = TextFieldState(initialText = WORK_TAG_QUERY),
        )

        composeRule.dialogNodeWithText(KOREAN_PICKER_SEARCH_EMPTY_TITLE).assertExists()
        composeRule.dialogNodeWithText(KOREAN_PICKER_SEARCH_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 검색 입력의 자리 표시 문구를 표시한다`() {
        composeRule.setMemoTagPickerDialog(tagList = listOf(testTag(title = WORK_TAG_TITLE)))

        composeRule.dialogNodeWithText(KOREAN_PICKER_SEARCH_PLACEHOLDER).assertExists()
    }

    @Test
    fun `검색어가 비어 있으면 나타낼 태그가 없어도 결과 없음을 알리지 않는다`() {
        composeRule.setMemoTagPickerDialog(tagList = emptyList())

        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_EMPTY_TITLE).assertDoesNotExist()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_EMPTY_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-042 검색 결과가 없어도 태그 추가 항목으로 이동을 전달한다`() {
        var tagAddCount = 0
        composeRule.setMemoTagPickerDialog(
            tagList = emptyList(),
            queryState = TextFieldState(initialText = WORK_TAG_QUERY),
            onTagAdd = { tagAddCount += 1 },
        )

        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).assertExists()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-043 목록을 닫았다가 다시 열면 검색어가 비어 있다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        val dialogState = DialogState(isVisible = true)
        val queryList = mutableListOf<String>()
        composeRule.setMemoTagPickerDialogHost(dialogState = dialogState, tagList = tagList, onQueryChange = queryList::add)
        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)
        composeRule.waitForIdle()

        composeRule.runOnIdle { dialogState.hide() }
        composeRule.waitForIdle()
        composeRule.runOnIdle { dialogState.show() }
        composeRule.awaitTagPickerRows()

        queryList.last() shouldBe ""
        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_PLACEHOLDER).assertExists()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }
}
