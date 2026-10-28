package io.github.taetae98coding.diary.compose.tag.entity

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.height
import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DIARY_PICKER_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EntityTagPickerDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-6955c5ed-dd16-4f8e-b27c-07e7d5b5ab7f 목록에 나타나는 태그와 연결 여부를 표시한다`() {
        val linkedTag = entityTestTag(title = WORK_TAG_TITLE)
        val unlinkedTag = entityTestTag(title = EXERCISE_TAG_TITLE)

        composeRule.setEntityTagPickerDialog(
            tagList = listOf(linkedTag, unlinkedTag),
            uiState = EntityTagInputUiState(tagList = listOf(linkedTag)),
        )
        composeRule.awaitEntityTagPickerRows()

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.pickerRows()[0].assertIsOn()
        composeRule.pickerRows()[1].assertIsOff()
    }

    @Test
    fun `TC-8037706b-864b-41f3-a552-f4761dab9ebe 나타낼 태그가 있으면 목록과 함께 태그 추가 항목이 표시된다`() {
        composeRule.setEntityTagPickerDialog(tagList = listOf(entityTestTag(title = WORK_TAG_TITLE)))
        composeRule.awaitEntityTagPickerRows()

        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).assertExists()
    }

    @Test
    fun `TC-8037706b-864b-41f3-a552-f4761dab9ebe 나타낼 태그가 없어도 태그 추가 항목이 표시된다`() {
        composeRule.setEntityTagPickerDialog(tagList = emptyList())
        composeRule.waitForIdle()

        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).assertExists()
    }

    @Test
    fun `TC-6397d89f-db81-4ca1-bbe0-13d8857d9eed 목록의 태그 추가 항목을 누르면 태그 추가를 요청한다`() {
        val workTag = entityTestTag(title = WORK_TAG_TITLE)
        val addedIdList = mutableListOf<Uuid>()
        val removedIdList = mutableListOf<Uuid>()
        var clickAddCount = 0
        composeRule.setEntityTagPickerDialog(
            tagList = listOf(workTag),
            onClickAdd = { clickAddCount += 1 },
            onAdd = addedIdList::add,
            onRemove = removedIdList::add,
        )
        composeRule.awaitEntityTagPickerRows()

        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        clickAddCount shouldBe 1
        addedIdList.shouldBeEmpty()
        removedIdList.shouldBeEmpty()
    }

    @Test
    fun `TC-51a413b9-ca9e-454e-afc7-6183017b3c1f 목록에서 태그를 누르면 연결을 요청한다`() {
        val workTag = entityTestTag(title = WORK_TAG_TITLE)
        val exerciseTag = entityTestTag(title = EXERCISE_TAG_TITLE)
        val linkedIdList = mutableListOf<Uuid>()
        composeRule.setEntityTagPickerDialog(
            tagList = listOf(workTag, exerciseTag),
            onAdd = linkedIdList::add,
        )
        composeRule.awaitEntityTagPickerRows()

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        linkedIdList shouldBe listOf(workTag.id, exerciseTag.id)
    }

    @Test
    fun `TC-eba5daa8-8d3e-4003-9b60-82720604c453 목록에서 연결된 태그를 누르면 해제를 요청한다`() {
        val workTag = entityTestTag(title = WORK_TAG_TITLE)
        val exerciseTag = entityTestTag(title = EXERCISE_TAG_TITLE)
        val unlinkedIdList = mutableListOf<Uuid>()
        val linkedIdList = mutableListOf<Uuid>()
        composeRule.setEntityTagPickerDialog(
            tagList = listOf(workTag, exerciseTag),
            uiState = EntityTagInputUiState(tagList = listOf(workTag, exerciseTag)),
            onAdd = linkedIdList::add,
            onRemove = unlinkedIdList::add,
        )
        composeRule.awaitEntityTagPickerRows()

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        unlinkedIdList shouldBe listOf(workTag.id)
        linkedIdList.shouldBeEmpty()
    }

    @Test
    fun `TC-95ad66fd-91b5-4afd-b5ca-57910c2c8090 다음 태그를 불러오는 동안에도 이미 나타난 태그를 조작할 수 있다`() {
        val tag = entityTestTag(title = WORK_TAG_TITLE)
        val linkedIdList = mutableListOf<Uuid>()
        composeRule.setEntityTagPickerDialog(
            tagPagingData = appendingEntityTagPagingDataOf(listOf(tag)),
            onAdd = linkedIdList::add,
        )
        composeRule.awaitEntityTagPickerRows()

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        linkedIdList shouldBe listOf(tag.id)
    }

    @Test
    fun `TC-f377bcc2-56dc-409b-805d-7b65e703d4bc 다음 태그를 불러오지 못해도 이미 나타난 태그를 유지한다`() {
        val workTag = entityTestTag(title = WORK_TAG_TITLE)
        val exerciseTag = entityTestTag(title = EXERCISE_TAG_TITLE)
        composeRule.setEntityTagPickerDialog(tagPagingData = appendFailedEntityTagPagingDataOf(listOf(workTag, exerciseTag)))
        composeRule.awaitEntityTagPickerRows()

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        // 오류 안내나 재시도 항목이 없으므로 목록 항목 수는 준비된 태그 수와 같다.
        composeRule.pickerRows().assertCountEquals(2)
    }

    @Test
    fun `TC-f377bcc2-56dc-409b-805d-7b65e703d4bc 첫 조회에 실패하면 목록 영역이 비어 있고 오류 안내가 표시되지 않는다`() {
        var tagPagingItems: LazyPagingItems<Tag>? = null
        composeRule.setEntityTagPickerDialog(
            tagPagingData = refreshFailedEntityTagPagingData(),
            onTagPagingItems = { items -> tagPagingItems = items },
        )
        composeRule.awaitEntityTagPickerRefreshSettled { tagPagingItems }

        composeRule
            .entityTagPickerList()
            .fetchSemanticsNode()
            .children
            .shouldBeEmpty()
        composeRule.pickerRows().assertCountEquals(0)
        composeRule.onNode(hasTestTag(DIARY_PICKER_EMPTY_BOX_TEST_TAG)).assertDoesNotExist()
    }

    @Test
    fun `준비되지 않은 자리는 준비된 항목과 같은 높이의 빈 항목으로 표시한다`() {
        val tag = entityTestTag(title = WORK_TAG_TITLE)
        val linkedIdList = mutableListOf<Uuid>()
        val unlinkedIdList = mutableListOf<Uuid>()

        composeRule.setContent {
            DiaryTheme {
                Column {
                    EntityTagPickerRow(
                        onEvent = { event -> recordLinkPickerEvent(event, linkedIdList, unlinkedIdList) },
                        tag = tag,
                        isSelected = true,
                        modifier = Modifier.testTag(PREPARED_ROW_TEST_TAG),
                    )
                    EntityTagPickerRow(
                        onEvent = { event -> recordLinkPickerEvent(event, linkedIdList, unlinkedIdList) },
                        tag = null,
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
        linkedIdList.shouldBeEmpty()
        unlinkedIdList.shouldBeEmpty()
    }

    private fun recordLinkPickerEvent(
        event: EntityTagPickerEvent,
        linkedIdList: MutableList<Uuid>,
        unlinkedIdList: MutableList<Uuid>,
    ) {
        when (event) {
            is EntityTagPickerEvent.ClickAdd -> Unit
            is EntityTagPickerEvent.Add -> linkedIdList += event.id
            is EntityTagPickerEvent.Remove -> unlinkedIdList += event.id
            is EntityTagPickerEvent.ChangeQuery -> Unit
        }
    }

    public companion object {
        private const val PREPARED_ROW_TEST_TAG: String = "PreparedEntityTagPickerRow"
        private const val PLACEHOLDER_ROW_TEST_TAG: String = "PlaceholderEntityTagPickerRow"
    }
}
