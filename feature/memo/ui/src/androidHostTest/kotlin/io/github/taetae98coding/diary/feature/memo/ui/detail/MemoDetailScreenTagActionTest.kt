package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.github.taetae98coding.diary.feature.memo.ui.closeDialogByBack
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_TAG_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.tag.EXERCISE_TAG_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagViewModel
import io.github.taetae98coding.diary.feature.memo.ui.tag.WORK_TAG_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.tag.dialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.tag.dialogNodesWithContentDescription
import io.github.taetae98coding.diary.feature.memo.ui.tag.tagPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
import io.kotest.matchers.shouldBe
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MemoDetailScreenTagActionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-035 태그를 선택해도 수정 버튼이 나타나지 않는다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)

        composeRule.setMemoDetailTagScreen(tagList = listOf(workTag))
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-035 대표 태그를 지정해도 수정 버튼이 나타나지 않는다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)

        composeRule.setMemoDetailTagScreen(tagList = listOf(workTag))
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-036 태그를 선택해도 안내 스낵바를 표시하지 않는다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)

        composeRule.setMemoDetailTagScreen(tagList = listOf(workTag))
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-037 태그 변경이 저장에 반영되지 않으면 저장된 태그를 그대로 표시한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)

        composeRule.setMemoDetailTagScreen(tagList = listOf(workTag))
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-040 완료된 메모에서도 태그를 선택할 수 있다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val tagViewModel = memoDetailTagViewModel(tagList = listOf(workTag))

        composeRule.setMemoDetailScreenWithTag(
            uiState = MutableStateFlow(memoDetailUiState(detail = memoDetail(MEMO_TITLE), isFinished = true)),
            tagViewModel = tagViewModel,
        )
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()

        verify(exactly = 1) { tagViewModel.selectTag(tagId = workTag.id) }
    }

    @Test
    fun `TC-MEMO-DETAIL-DOMAIN-005 태그 조작을 이어서 수행하면 모두 전달된다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val exerciseTag = testTag(title = EXERCISE_TAG_TITLE)
        val tagViewModel = memoDetailTagViewModel(tagList = listOf(workTag, exerciseTag))

        composeRule.setMemoDetailTagScreen(tagViewModel = tagViewModel)
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).performClick()

        verify(exactly = 1) { tagViewModel.selectTag(tagId = workTag.id) }
        verify(exactly = 1) { tagViewModel.selectTag(tagId = exerciseTag.id) }
    }

    @Test
    fun `TC-MEMO-DETAIL-DATA-004 태그를 선택하면 선택한 태그의 연결 반영을 요청한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val tagViewModel = memoDetailTagViewModel(tagList = listOf(workTag))

        composeRule.setMemoDetailTagScreen(tagViewModel = tagViewModel)
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()

        verify(exactly = 1) { tagViewModel.selectTag(tagId = workTag.id) }
    }

    @Test
    fun `TC-MEMO-DETAIL-DATA-005 태그 선택을 해제하면 연결 해제 반영을 요청한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val tagViewModel = memoDetailTagViewModel(tagList = listOf(workTag), selectedTagList = listOf(workTag))

        composeRule.setMemoDetailTagScreen(tagViewModel = tagViewModel)
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()

        verify(exactly = 1) { tagViewModel.unselectTag(tagId = workTag.id) }
    }

    @Test
    fun `TC-MEMO-DETAIL-DATA-008 연결되지 않은 태그를 대표 태그로 지정하면 대표 지정 반영을 요청한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val tagViewModel = memoDetailTagViewModel(tagList = listOf(workTag))

        composeRule.setMemoDetailTagScreen(tagViewModel = tagViewModel)
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()

        verify(exactly = 1) { tagViewModel.selectPrimaryTag(tagId = workTag.id) }
    }

    @Test
    fun `TC-MEMO-DETAIL-DATA-009 대표 태그 지정을 해제하면 대표 지정 해제 반영을 요청한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val tagViewModel =
            memoDetailTagViewModel(
                tagList = listOf(workTag),
                selectedTagList = listOf(workTag),
                primaryTagId = workTag.id,
            )

        composeRule.setMemoDetailTagScreen(tagViewModel = tagViewModel)
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_UNSET_DESCRIPTION)[0].performClick()

        verify(exactly = 1) { tagViewModel.unselectPrimaryTag() }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MemoDetailScreenFinishedTagActionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-053 목록의 연결된 완료된 태그를 누르면 연결 해제 반영을 요청한다`() {
        val finishedTag = testTag(title = EXERCISE_TAG_TITLE).copy(isFinished = true)
        val tagViewModel = memoDetailTagViewModel(tagList = listOf(finishedTag), selectedTagList = listOf(finishedTag))

        composeRule.setMemoDetailTagScreen(tagViewModel = tagViewModel)
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).performClick()

        verify(exactly = 1) { tagViewModel.unselectTag(tagId = finishedTag.id) }
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-054 목록의 연결된 완료된 태그를 대표 태그로 지정하면 대표 지정 반영을 요청한다`() {
        val finishedTag = testTag(title = EXERCISE_TAG_TITLE).copy(isFinished = true)
        val tagViewModel = memoDetailTagViewModel(tagList = listOf(finishedTag), selectedTagList = listOf(finishedTag))

        composeRule.setMemoDetailTagScreen(tagViewModel = tagViewModel)
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()

        verify(exactly = 1) { tagViewModel.selectPrimaryTag(tagId = finishedTag.id) }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScreenTagNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "w480dp-h1200dp")
    fun `TC-MEMO-TAG-INPUT-FEATURE-021 태그 칩을 누르면 그 태그의 TagDetail 화면으로 이동한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val exerciseTag = testTag(title = EXERCISE_TAG_TITLE)
        val navigatedTagIdList = mutableListOf<Uuid>()

        composeRule.setMemoDetailScreenWithTag(
            tagUiState =
                MutableStateFlow(
                    MemoTagInputUiState(selectedTagList = listOf(workTag, exerciseTag), primaryTagId = exerciseTag.id),
                ),
            tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(listOf(workTag, exerciseTag))),
            navigateToTagAdd = {},
            navigateToTagDetail = { id -> navigatedTagIdList += id },
            navigateToPlaceAdd = {},
            navigateToPlaceDetail = {},
        )
        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo()

        composeRule.onNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).performClick()

        navigatedTagIdList shouldBe listOf(workTag.id, exerciseTag.id)
    }
}
