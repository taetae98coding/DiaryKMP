package io.github.taetae98coding.diary.feature.tag.ui.link

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagLinkPickerDialogSearchTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-018 목록을 열면 검색어가 비어 있고 대상 태그가 모두 나타난다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        val queryList = mutableListOf<String>()
        composeRule.setTagLinkPickerDialogHost(tagList = tagList, onQueryChange = queryList::add)
        composeRule.awaitTagLinkPickerRows()

        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_PLACEHOLDER).assertExists()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        queryList shouldBe listOf("")
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-019 검색어를 입력하면 확정 동작 없이 그 검색어가 즉시 반영된다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        val queryList = mutableListOf<String>()
        composeRule.setTagLinkPickerDialogHost(tagList = tagList, onQueryChange = queryList::add)
        composeRule.awaitTagLinkPickerRows()

        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)
        composeRule.waitForIdle()

        queryList.last() shouldBe WORK_TAG_QUERY
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-020 검색어를 지우면 검색어가 없는 상태가 즉시 반영된다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        val queryList = mutableListOf<String>()
        composeRule.setTagLinkPickerDialogHost(tagList = tagList, onQueryChange = queryList::add)
        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)
        composeRule.waitForIdle()

        composeRule.dialogSearchField().performTextClearance()
        composeRule.waitForIdle()

        queryList.last() shouldBe ""
        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_PLACEHOLDER).assertExists()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-021 검색어로 좁힌 목록에서도 연결과 해제를 전달한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val linkedIdList = mutableListOf<Uuid>()
        val unlinkedIdList = mutableListOf<Uuid>()
        composeRule.setTagLinkPickerDialog(
            tagList = listOf(workTag),
            queryState = TextFieldState(initialText = WORK_TAG_QUERY),
            onLink = linkedIdList::add,
            onUnlink = unlinkedIdList::add,
        )

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        linkedIdList shouldBe listOf(workTag.id)
        unlinkedIdList shouldBe emptyList()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-022 검색어에 맞는 태그가 없으면 결과 없음을 알린다`() {
        composeRule.setTagLinkPickerDialog(
            tagList = emptyList(),
            queryState = TextFieldState(initialText = WORK_TAG_QUERY),
        )

        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_EMPTY_TITLE).assertExists()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_EMPTY_DESCRIPTION).assertExists()
        composeRule.onAllNodes(isToggleable() and hasAnyAncestor(isDialog())).assertCountEquals(0)
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-031 검색 결과가 없어도 태그 추가 항목으로 TagAdd 이동을 요청할 수 있다`() {
        var clickAddCount = 0
        composeRule.setTagLinkPickerDialog(
            tagList = emptyList(),
            queryState = TextFieldState(initialText = WORK_TAG_QUERY),
            onClickAdd = { clickAddCount += 1 },
        )

        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        clickAddCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 태그 선택 목록의 검색 문구를 표시한다`() {
        composeRule.setTagLinkPickerDialog(
            tagList = emptyList(),
            queryState = TextFieldState(initialText = WORK_TAG_QUERY),
        )

        composeRule.dialogNodeWithText(KOREAN_PICKER_SEARCH_EMPTY_TITLE).assertExists()
        composeRule.dialogNodeWithText(KOREAN_PICKER_SEARCH_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 검색 입력의 자리 표시 문구를 표시한다`() {
        composeRule.setTagLinkPickerDialog(tagList = listOf(testTag(title = WORK_TAG_TITLE)))

        composeRule.dialogNodeWithText(KOREAN_PICKER_SEARCH_PLACEHOLDER).assertExists()
    }

    @Test
    fun `검색어가 비어 있으면 나타낼 태그가 없어도 결과 없음을 알리지 않는다`() {
        composeRule.setTagLinkPickerDialog(tagList = emptyList())

        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_EMPTY_TITLE).assertDoesNotExist()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_EMPTY_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-FEATURE-023 목록을 닫았다가 다시 열면 검색어가 비어 있다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        val dialogState = DialogState(isVisible = true)
        val queryList = mutableListOf<String>()
        composeRule.setTagLinkPickerDialogHost(dialogState = dialogState, tagList = tagList, onQueryChange = queryList::add)
        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)
        composeRule.waitForIdle()

        composeRule.runOnIdle { dialogState.hide() }
        composeRule.waitForIdle()
        composeRule.runOnIdle { dialogState.show() }
        composeRule.awaitTagLinkPickerRows()

        queryList.last() shouldBe ""
        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_PLACEHOLDER).assertExists()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-LINK-INPUT-DOMAIN-010 화면이 재생성되어도 열려 있는 목록의 검색어가 유지된다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(listOf(workTag)))
        val queryList = mutableListOf<String>()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                TagLinkPickerDialogHost(
                    dialogState = rememberDialogState(initialVisible = true),
                    onEvent = { event -> if (event is TagLinkPickerEvent.ChangeQuery) queryList += event.query },
                    tagPagingItems = tagPagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        queryList.last() shouldBe WORK_TAG_QUERY
        composeRule.dialogNodeWithText(WORK_TAG_QUERY).assertExists()
        composeRule.awaitTagLinkPickerRows()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
    }
}
