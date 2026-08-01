package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.memo.ui.closeDialogByBack
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoTagPickerDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-008 목록에서 태그를 선택하면 선택 행동을 즉시 전달한다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        val selectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoTagPickerDialog(tagList = tagList, onTagSelect = selectedIdList::add)

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        selectedIdList shouldBe listOf(tagList.first().id, tagList.last().id)
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-009 목록에서 선택을 해제하면 해제 행동을 즉시 전달한다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        val unselectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoTagPickerDialog(
            tagList = listOf(tag),
            uiState = MemoTagInputUiState(selectedTagList = listOf(tag)),
            onTagUnselect = unselectedIdList::add,
        )

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        unselectedIdList shouldBe listOf(tag.id)
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-015 대표 태그 지정 버튼을 누르면 대표 지정 행동을 즉시 전달한다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        val primarySelectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoTagPickerDialog(tagList = listOf(tag), onPrimaryTagSelect = primarySelectedIdList::add)

        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()
        composeRule.waitForIdle()

        primarySelectedIdList shouldBe listOf(tag.id)
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-016 대표 태그 지정 버튼을 다시 누르면 대표 지정 해제 행동을 전달한다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        var primaryUnselectCount = 0
        composeRule.setMemoTagPickerDialog(
            tagList = listOf(tag),
            uiState = MemoTagInputUiState(selectedTagList = listOf(tag), primaryTagId = tag.id),
            onPrimaryTagUnselect = { primaryUnselectCount += 1 },
        )

        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_UNSET_DESCRIPTION)[0].performClick()
        composeRule.waitForIdle()

        primaryUnselectCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-017 대표 태그로 지정된 태그만 대표 지정 해제 버튼을 제공한다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        composeRule.setMemoTagPickerDialog(
            tagList = tagList,
            uiState = MemoTagInputUiState(selectedTagList = tagList, primaryTagId = tagList.first().id),
        )

        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_UNSET_DESCRIPTION).assertCountEquals(1)
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-DOMAIN-002 목록의 태그는 전달된 제목 오름차순으로 표시된다`() {
        val tagList =
            listOf(
                testTag(title = EXERCISE_TAG_TITLE),
                testTag(title = WORK_TAG_TITLE),
            )
        composeRule.setMemoTagPickerDialog(tagList = tagList)

        val titleList =
            composeRule
                .onAllNodes(hasAnyTagTitle(tagList))
                .fetchSemanticsNodes()
                .flatMap { node -> node.config.getOrNull(SemanticsProperties.Text).orEmpty() }
                .map { text -> text.text }

        titleList shouldBe listOf(EXERCISE_TAG_TITLE, WORK_TAG_TITLE)
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-DOMAIN-010 목록의 태그를 완료 여부로 구분하지 않고 주어진 순서대로 표시한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val finishedTag = testTag(title = EXERCISE_TAG_TITLE).copy(isFinished = true)
        composeRule.setMemoTagPickerDialog(
            tagList = listOf(workTag, finishedTag),
            uiState = MemoTagInputUiState(selectedTagList = listOf(finishedTag)),
        )

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION).assertCountEquals(2)
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-DOMAIN-011 목록의 완료된 태그도 해제와 대표 태그 지정 행동을 전달한다`() {
        val finishedTag = testTag(title = EXERCISE_TAG_TITLE).copy(isFinished = true)
        val unselectedIdList = mutableListOf<Uuid>()
        val primarySelectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoTagPickerDialog(
            tagList = listOf(finishedTag),
            uiState = MemoTagInputUiState(selectedTagList = listOf(finishedTag)),
            onTagUnselect = unselectedIdList::add,
            onPrimaryTagSelect = primarySelectedIdList::add,
        )

        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        primarySelectedIdList shouldBe listOf(finishedTag.id)
        unselectedIdList shouldBe listOf(finishedTag.id)
    }

    @Test
    fun `목록에 확인 버튼을 두지 않는다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        composeRule.setMemoTagPickerDialog(
            tagList = tagList,
            uiState = MemoTagInputUiState(selectedTagList = tagList, primaryTagId = tagList.first().id),
        )

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertDoesNotExist()
    }

    @Test
    fun `뒤로가기로 닫으면 닫기 요청을 전달하고 선택 행동은 전달하지 않는다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        var dismissCount = 0
        var selectionChangeCount = 0
        composeRule.setMemoTagPickerDialog(
            tagList = tagList,
            uiState = MemoTagInputUiState(selectedTagList = tagList, primaryTagId = tagList.first().id),
            onDismissRequest = { dismissCount += 1 },
            onTagSelect = { selectionChangeCount += 1 },
            onTagUnselect = { selectionChangeCount += 1 },
            onPrimaryTagSelect = { selectionChangeCount += 1 },
            onPrimaryTagUnselect = { selectionChangeCount += 1 },
        )

        composeRule.closeDialogByBack()

        dismissCount shouldBe 1
        selectionChangeCount shouldBe 0
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 태그 선택 목록의 문구를 표시한다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        composeRule.setMemoTagPickerDialog(
            tagList = tagList,
            uiState = MemoTagInputUiState(selectedTagList = listOf(tagList.first()), primaryTagId = tagList.first().id),
        )

        composeRule.dialogNodeWithText(KOREAN_PICKER_TITLE).assertExists()
        composeRule.dialogNodeWithText(KOREAN_PICKER_TAG_ADD).assertExists()
        composeRule.dialogNodesWithContentDescription(KOREAN_PRIMARY_UNSET_DESCRIPTION).assertCountEquals(1)
        composeRule.dialogNodesWithContentDescription(KOREAN_PRIMARY_SET_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    fun `기본 환경에서 대표 태그 지정 버튼 이름을 제공한다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        composeRule.setMemoTagPickerDialog(
            tagList = tagList,
            uiState = MemoTagInputUiState(selectedTagList = listOf(tagList.first()), primaryTagId = tagList.first().id),
        )

        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_UNSET_DESCRIPTION).assertCountEquals(1)
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION).assertCountEquals(1)
    }

    public companion object {
        private fun hasAnyTagTitle(tagList: List<Tag>): SemanticsMatcher =
            SemanticsMatcher("has any tag title") { node ->
                node.config
                    .getOrNull(SemanticsProperties.Text)
                    ?.any { text -> tagList.any { tag -> tag.detail.title == text.text } } == true
            }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoTagPickerDialogAddTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-032 나타낼 태그가 없어도 태그 추가 항목을 표시한다`() {
        composeRule.setMemoTagPickerDialog(tagList = emptyList())

        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).assertExists()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-032 나타낼 태그가 여러 개여도 태그 추가 항목을 표시한다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        composeRule.setMemoTagPickerDialog(tagList = tagList)

        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).assertExists()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-033 태그 추가 항목을 누르면 태그 추가 행동만 전달한다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        var tagAddCount = 0
        var selectionChangeCount = 0
        composeRule.setMemoTagPickerDialog(
            tagList = tagList,
            onTagSelect = { selectionChangeCount += 1 },
            onTagUnselect = { selectionChangeCount += 1 },
            onPrimaryTagSelect = { selectionChangeCount += 1 },
            onPrimaryTagUnselect = { selectionChangeCount += 1 },
            onTagAdd = { tagAddCount += 1 },
        )

        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        selectionChangeCount shouldBe 0
    }

    @Test
    fun `태그 추가 항목에 태그 추가 동작 이름을 제공한다`() {
        composeRule.setMemoTagPickerDialog(tagList = listOf(testTag(title = WORK_TAG_TITLE)))

        composeRule
            .onNode(hasClickLabel(DEFAULT_PICKER_TAG_ADD) and hasAnyAncestor(isDialog()))
            .assertExists()
    }
}
