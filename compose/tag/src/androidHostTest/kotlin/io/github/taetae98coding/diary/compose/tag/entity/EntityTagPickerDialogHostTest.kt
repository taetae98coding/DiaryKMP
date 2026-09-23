package io.github.taetae98coding.diary.compose.tag.entity

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EntityTagPickerDialogHostTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-004 나타낼 태그가 없어도 태그 추가 항목을 누르면 태그 선택 목록이 열린다`() {
        setEntityTagInputWithPicker(tagList = emptyList())

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-004 나타낼 태그가 여러 개여도 태그 추가 항목을 누르면 태그 선택 목록이 열린다`() {
        val tagList = listOf(entityTestTag(title = WORK_TAG_TITLE), entityTestTag(title = EXERCISE_TAG_TITLE))
        setEntityTagInputWithPicker(tagList = tagList)

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-010 목록을 닫아도 반영된 연결이 유지된다`() {
        val workTag = entityTestTag(title = WORK_TAG_TITLE)
        val exerciseTag = entityTestTag(title = EXERCISE_TAG_TITLE)
        setEntityTagInputWithPicker(tagList = listOf(workTag, exerciseTag), selectedTagList = listOf(exerciseTag))
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.awaitEntityTagPickerRows()

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).performClick()
        composeRule.waitForIdle()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-013 목록의 끝으로 이동하면 다음 태그가 이어서 나타난다`() {
        val tagList = List(TAG_COUNT) { index -> entityTestTag(title = tagTitle(index)) }
        setEntityTagInputWithPicker(tagList = tagList)
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.awaitEntityTagPickerRows()

        composeRule.dialogNodeWithText(tagTitle(TAG_COUNT - 1)).assertDoesNotExist()

        composeRule.pickerList().performScrollToIndex(TAG_COUNT - 1)
        composeRule.waitForIdle()

        composeRule.dialogNodeWithText(tagTitle(TAG_COUNT - 1)).assertExists()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-016 목록을 닫았다가 다시 열면 앞부분부터 나타난다`() {
        val tagList = List(TAG_COUNT) { index -> entityTestTag(title = tagTitle(index)) }
        setEntityTagInputWithPicker(tagList = tagList)
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.awaitEntityTagPickerRows()
        composeRule.pickerList().performScrollToIndex(TAG_COUNT - 1)
        composeRule.waitForIdle()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.awaitEntityTagPickerRows()

        composeRule.dialogNodeWithText(tagTitle(0)).assertExists()
        composeRule.dialogNodeWithText(tagTitle(TAG_COUNT - 1)).assertDoesNotExist()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-017 화면이 재생성되어도 태그 선택 목록의 열림 상태가 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val tagPagingDataFlow = MutableStateFlow(entityTagPagingDataOf(listOf(entityTestTag(title = WORK_TAG_TITLE))))
        restorationTester.setContent {
            val dialogState = rememberDialogState()

            DiaryTheme {
                Column {
                    EntityTagInput(
                        uiStateProvider = { EntityTagInputUiState() },
                        onTagClick = {},
                        onAddClick = { dialogState.show() },
                    )
                    EntityTagPickerDialogHost(
                        dialogState = dialogState,
                        onEvent = {},
                        tagPagingItems = remember { tagPagingDataFlow }.collectAsLazyPagingItems(),
                        uiStateProvider = { EntityTagInputUiState() },
                    )
                }
            }
        }
        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-DOMAIN-004 태그가 복구되면 유지되어 있던 연결이 다시 나타난다`() {
        val workTag = entityTestTag(title = WORK_TAG_TITLE)
        val updateSelectedTagList = composeRule.setEntityTagInputWithTagList()

        updateSelectedTagList(emptyList())
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()

        updateSelectedTagList(listOf(workTag))

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-DOMAIN-008 앞뒤 공백만 있는 검색어는 목록을 좁히지 않는다`() {
        val tagList = listOf(entityTestTag(title = WORK_TAG_TITLE), entityTestTag(title = EXERCISE_TAG_TITLE))
        composeRule.setEntityTagPickerDialog(tagList = tagList)
        composeRule.awaitEntityTagPickerRows()

        composeRule.dialogSearchField().performTextInput(BLANK_QUERY)
        composeRule.waitForIdle()

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_PICKER_SEARCH_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-DOMAIN-012 목록에는 대표 태그를 지정하는 동작이 없다`() {
        val tagList = listOf(entityTestTag(title = WORK_TAG_TITLE), entityTestTag(title = EXERCISE_TAG_TITLE))
        composeRule.setEntityTagPickerDialog(tagList = tagList)
        composeRule.awaitEntityTagPickerRows()

        // 항목마다 연결을 바꾸는 조작 하나만 있고 그 밖의 동작은 두지 않는다.
        composeRule
            .onAllNodes(hasClickAction() and hasAnyAncestor(hasTestTag(ENTITY_TAG_PICKER_LIST_TEST_TAG)))
            .assertCountEquals(tagList.size)
        composeRule
            .onAllNodes(isToggleable() and hasAnyAncestor(isDialog()))
            .assertCountEquals(tagList.size)
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-DATA-003 저장된 태그가 바뀌면 태그 입력과 태그 선택 목록에 함께 반영된다`() {
        val workTag = entityTestTag(title = WORK_TAG_TITLE)
        var selectedTagList by mutableStateOf(listOf(workTag))
        val tagPagingDataFlow = MutableStateFlow(entityTagPagingDataOf(listOf(workTag)))

        composeRule.setContent {
            DiaryTheme {
                Column {
                    EntityTagInput(
                        uiStateProvider = { EntityTagInputUiState(tagList = selectedTagList) },
                        onTagClick = {},
                        onAddClick = {},
                    )
                    EntityTagPickerDialog(
                        onDismissRequest = {},
                        onEvent = {},
                        tagPagingItems = remember { tagPagingDataFlow }.collectAsLazyPagingItems(),
                        uiStateProvider = { EntityTagInputUiState(tagList = selectedTagList) },
                    )
                }
            }
        }
        composeRule.awaitEntityTagPickerRows()
        composeRule.pickerRows()[0].assertIsOn()

        val renamedTag = workTag.copy(detail = workTag.detail.copy(title = RENAMED_TAG_TITLE))
        composeRule.runOnIdle {
            selectedTagList = listOf(renamedTag)
            tagPagingDataFlow.value = entityTagPagingDataOf(listOf(renamedTag))
        }
        composeRule.waitForIdle()

        composeRule.dialogNodeWithText(RENAMED_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
        composeRule.pickerRows()[0].assertIsOn()
    }

    @Test
    fun `연결되지 않은 태그의 항목은 연결되지 않은 상태로 표시된다`() {
        val workTag = entityTestTag(title = WORK_TAG_TITLE)
        composeRule.setEntityTagPickerDialog(tagList = listOf(workTag))
        composeRule.awaitEntityTagPickerRows()

        composeRule.pickerRows()[0].assertIsOff()
    }

    private fun setEntityTagInputWithPicker(
        tagList: List<Tag>,
        selectedTagList: List<Tag> = emptyList(),
    ) {
        val tagPagingDataFlow = MutableStateFlow(entityTagPagingDataOf(tagList))

        composeRule.setContent {
            var selected by remember { mutableStateOf(selectedTagList) }
            val dialogState = rememberDialogState()

            DiaryTheme {
                Column {
                    EntityTagInput(
                        uiStateProvider = { EntityTagInputUiState(tagList = selected) },
                        onTagClick = {},
                        onAddClick = { dialogState.show() },
                    )
                    EntityTagPickerDialogHost(
                        dialogState = dialogState,
                        onEvent = { event ->
                            when (event) {
                                is EntityTagPickerEvent.ClickAdd -> Unit

                                is EntityTagPickerEvent.Add ->
                                    selected = selected + tagList.first { tag -> tag.id == event.id }

                                is EntityTagPickerEvent.Remove ->
                                    selected = selected.filterNot { tag -> tag.id == event.id }

                                is EntityTagPickerEvent.ChangeQuery -> Unit
                            }
                        },
                        tagPagingItems = remember { tagPagingDataFlow }.collectAsLazyPagingItems(),
                        uiStateProvider = { EntityTagInputUiState(tagList = selected) },
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val TAG_COUNT = 30
        const val BLANK_QUERY = "   "

        fun tagTitle(index: Int): String = "EntityTagPaged${index.toString().padStart(length = 2, padChar = '0')}"
    }
}
