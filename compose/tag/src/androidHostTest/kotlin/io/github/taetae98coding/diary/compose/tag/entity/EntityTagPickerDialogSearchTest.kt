package io.github.taetae98coding.diary.compose.tag.entity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.platform.app.InstrumentationRegistry
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
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
class EntityTagPickerDialogSearchTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-018 목록을 열면 검색어가 비어 있고 대상 태그가 모두 나타난다`() {
        val tagList = listOf(entityTestTag(title = WORK_TAG_TITLE), entityTestTag(title = EXERCISE_TAG_TITLE))
        val queryList = mutableListOf<String>()
        composeRule.setEntityTagPickerDialogHost(tagList = tagList, onQueryChange = queryList::add)
        composeRule.awaitEntityTagPickerRows()

        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_PLACEHOLDER).assertExists()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        queryList shouldBe listOf("")
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-032 목록을 열어도 검색어 입력에 초점이 놓이지 않고 검색어 입력을 누르면 초점을 받는다`() {
        InstrumentationRegistry.getInstrumentation().setInTouchMode(true)
        val dialogState = DialogState(isVisible = false)
        composeRule.setEntityTagPickerDialogHost(
            dialogState = dialogState,
            tagList = listOf(entityTestTag(title = WORK_TAG_TITLE), entityTestTag(title = EXERCISE_TAG_TITLE)),
        )

        composeRule.runOnIdle { dialogState.show() }
        composeRule.awaitEntityTagPickerRows()

        composeRule.dialogSearchField().assertIsNotFocused()
        composeRule.dialogSearchField().performClick()
        composeRule.dialogSearchField().assertIsFocused()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-019 검색어를 입력하면 별도의 확정 동작 없이 목록이 좁혀진다`() {
        val tagList = listOf(entityTestTag(title = WORK_TAG_TITLE), entityTestTag(title = EXERCISE_TAG_TITLE))
        setQueryFilteredPickerDialogHost(tagList = tagList)
        composeRule.awaitEntityTagPickerRow(title = EXERCISE_TAG_TITLE)

        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)

        composeRule.awaitEntityTagPickerRowGone(title = EXERCISE_TAG_TITLE)
        composeRule.awaitEntityTagPickerRow(title = WORK_TAG_TITLE)
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-020 검색어를 지우면 대상 전체가 다시 나타난다`() {
        val tagList = listOf(entityTestTag(title = WORK_TAG_TITLE), entityTestTag(title = EXERCISE_TAG_TITLE))
        setQueryFilteredPickerDialogHost(tagList = tagList)
        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)
        composeRule.awaitEntityTagPickerRowGone(title = EXERCISE_TAG_TITLE)

        composeRule.dialogSearchField().performTextClearance()

        composeRule.awaitEntityTagPickerRow(title = EXERCISE_TAG_TITLE)
        composeRule.awaitEntityTagPickerRow(title = WORK_TAG_TITLE)
        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_PLACEHOLDER).assertExists()
    }

    @Test
    fun `검색어를 입력하거나 지우면 확정 동작 없이 그 검색어를 바로 전달한다`() {
        val tagList = listOf(entityTestTag(title = WORK_TAG_TITLE), entityTestTag(title = EXERCISE_TAG_TITLE))
        val queryList = mutableListOf<String>()
        composeRule.setEntityTagPickerDialogHost(tagList = tagList, onQueryChange = queryList::add)
        composeRule.awaitEntityTagPickerRows()

        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)
        composeRule.waitForIdle()
        queryList.last() shouldBe WORK_TAG_QUERY

        composeRule.dialogSearchField().performTextClearance()
        composeRule.waitForIdle()
        queryList.last() shouldBe ""
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-021 검색어로 좁힌 목록에서도 연결과 해제를 전달하고 그 태그는 목록에 남는다`() {
        val workTag = entityTestTag(title = WORK_TAG_TITLE)
        val linkedIdList = mutableListOf<Uuid>()
        val unlinkedIdList = mutableListOf<Uuid>()
        val uiState = mutableStateOf(EntityTagInputUiState())
        composeRule.setContent {
            DiaryTheme {
                EntityTagPickerDialog(
                    searchFieldState = rememberDiaryPickerSearchFieldState(initialText = WORK_TAG_QUERY),
                    tagPagingItems = remember { MutableStateFlow(entityTagPagingDataOf(listOf(workTag))) }.collectAsLazyPagingItems(),
                    uiStateProvider = { uiState.value },
                    onDismissRequest = {},
                    onEvent = { event ->
                        if (event is EntityTagPickerEvent.Add) {
                            linkedIdList += event.id
                            uiState.value = EntityTagInputUiState(tagList = listOf(workTag))
                        }
                        if (event is EntityTagPickerEvent.Remove) {
                            unlinkedIdList += event.id
                            uiState.value = EntityTagInputUiState()
                        }
                    },
                )
            }
        }
        composeRule.awaitEntityTagPickerRow(title = WORK_TAG_TITLE)

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        linkedIdList shouldBe listOf(workTag.id)
        unlinkedIdList shouldBe emptyList()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        linkedIdList shouldBe listOf(workTag.id)
        unlinkedIdList shouldBe listOf(workTag.id)
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-022 검색어에 맞는 태그가 없으면 결과 없음을 알린다`() {
        composeRule.setEntityTagPickerDialog(
            tagList = emptyList(),
            query = WORK_TAG_QUERY,
        )

        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_EMPTY_TITLE).assertExists()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_EMPTY_DESCRIPTION).assertExists()
        composeRule.onAllNodes(isToggleable() and hasAnyAncestor(isDialog())).assertCountEquals(0)
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-031 검색 결과가 없어도 태그 추가 항목으로 TagAdd 이동을 요청할 수 있다`() {
        var clickAddCount = 0
        composeRule.setEntityTagPickerDialog(
            tagList = emptyList(),
            query = WORK_TAG_QUERY,
            onClickAdd = { clickAddCount += 1 },
        )

        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        clickAddCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 태그 선택 목록의 검색 문구를 표시한다`() {
        composeRule.setEntityTagPickerDialog(
            tagList = emptyList(),
            query = WORK_TAG_QUERY,
        )

        composeRule.dialogNodeWithText(KOREAN_ENTITY_PICKER_SEARCH_EMPTY_TITLE).assertExists()
        composeRule.dialogNodeWithText(KOREAN_ENTITY_PICKER_SEARCH_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 검색 입력의 자리 표시 문구를 표시한다`() {
        composeRule.setEntityTagPickerDialog(tagList = listOf(entityTestTag(title = WORK_TAG_TITLE)))

        composeRule.dialogNodeWithText(KOREAN_PICKER_SEARCH_PLACEHOLDER).assertExists()
    }

    @Test
    fun `검색어가 비어 있으면 나타낼 태그가 없어도 결과 없음을 알리지 않는다`() {
        composeRule.setEntityTagPickerDialog(tagList = emptyList())

        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_EMPTY_TITLE).assertDoesNotExist()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_EMPTY_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-FEATURE-023 목록을 닫았다가 다시 열면 검색어가 비어 있다`() {
        val tagList = listOf(entityTestTag(title = WORK_TAG_TITLE), entityTestTag(title = EXERCISE_TAG_TITLE))
        val dialogState = DialogState(isVisible = true)
        val queryList = mutableListOf<String>()
        composeRule.setEntityTagPickerDialogHost(dialogState = dialogState, tagList = tagList, onQueryChange = queryList::add)
        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)
        composeRule.waitForIdle()

        composeRule.runOnIdle { dialogState.hide() }
        composeRule.waitForIdle()
        composeRule.runOnIdle { dialogState.show() }
        composeRule.awaitEntityTagPickerRows()

        queryList.last() shouldBe ""
        composeRule.dialogNodeWithText(DEFAULT_PICKER_SEARCH_PLACEHOLDER).assertExists()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-DOMAIN-010 화면이 회전해도 열려 있는 목록의 검색어와 좁힌 결과가 유지된다`() {
        val workTag = entityTestTag(title = WORK_TAG_TITLE)
        val exerciseTag = entityTestTag(title = EXERCISE_TAG_TITLE)
        val tagPagingDataFlow = MutableStateFlow(entityTagPagingDataOf(listOf(workTag, exerciseTag)))
        val queryList = mutableListOf<String>()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                EntityTagPickerDialogHost(
                    dialogState = rememberDialogState(initialVisible = true),
                    onEvent = { event ->
                        if (event is EntityTagPickerEvent.ChangeQuery) {
                            queryList += event.query
                            val narrowedList = if (event.query.isEmpty()) listOf(workTag, exerciseTag) else listOf(workTag)
                            tagPagingDataFlow.value = entityTagPagingDataOf(narrowedList)
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
        queryList.count { query -> query.isEmpty() } shouldBe 1
        composeRule.dialogNodeWithText(WORK_TAG_QUERY).assertExists()
        composeRule.awaitEntityTagPickerRows()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-ENTITY-TAG-INPUT-DOMAIN-017 메모리 정리 뒤 복원하면 목록이 다시 열리고 검색어가 다시 나타나 그 검색어를 다시 전달한다`() {
        val tagList = listOf(entityTestTag(title = WORK_TAG_TITLE), entityTestTag(title = EXERCISE_TAG_TITLE))
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var dialogState: DialogState
        lateinit var receivedQueryList: MutableList<String>
        restorationTester.setContent {
            dialogState = rememberDialogState(initialVisible = true)
            // 메모리 정리 뒤에는 검색어를 받던 쪽도 새로 만들어지므로, 받은 검색어를 저장하지 않는 상태로 둔다.
            receivedQueryList = remember { mutableListOf() }
            DiaryTheme {
                EntityTagPickerDialogHost(
                    dialogState = dialogState,
                    onEvent = { event -> if (event is EntityTagPickerEvent.ChangeQuery) receivedQueryList += event.query },
                    tagPagingItems = remember { MutableStateFlow(entityTagPagingDataOf(tagList)) }.collectAsLazyPagingItems(),
                )
            }
        }
        composeRule.dialogSearchField().performTextInput(WORK_TAG_QUERY)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.runOnIdle { dialogState.isVisible shouldBe true }
        composeRule.dialogNodeWithText(WORK_TAG_QUERY).assertExists()
        receivedQueryList shouldBe listOf(WORK_TAG_QUERY)
    }

    private fun setQueryFilteredPickerDialogHost(tagList: List<Tag>) {
        composeRule.setContent {
            var query by remember { mutableStateOf("") }
            // 같은 목록에 이어서 넘긴 조회 결과는 화면 스레드의 공용 디스패처를 거쳐야 도착해 결과가 일정하지 않다.
            // 검색어마다 새 목록을 만들어 목록이 처음 그릴 때 그 검색어의 조회 결과를 받게 한다.
            val tagPagingItems =
                remember(query) {
                    MutableStateFlow(entityTagPagingDataOf(tagList.filter { tag -> tag.detail.title.contains(query, ignoreCase = true) }))
                }.collectAsLazyPagingItems()

            DiaryTheme {
                EntityTagPickerDialogHost(
                    dialogState = remember { DialogState(isVisible = true) },
                    onEvent = { event -> if (event is EntityTagPickerEvent.ChangeQuery) query = event.query },
                    tagPagingItems = tagPagingItems,
                )
            }
        }
    }
}
