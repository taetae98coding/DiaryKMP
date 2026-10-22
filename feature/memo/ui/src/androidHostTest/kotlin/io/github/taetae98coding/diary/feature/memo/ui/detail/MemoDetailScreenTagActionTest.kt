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
import io.github.taetae98coding.diary.feature.memo.ui.tag.WORK_TAG_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.tag.dialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.tag.dialogNodesWithContentDescription
import io.github.taetae98coding.diary.feature.memo.ui.tag.tagPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
import io.kotest.matchers.shouldBe
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
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
    fun `TC-53cc153b-b376-47ae-bc33-f71d628c7545 태그를 선택해도 수정 버튼이 나타나지 않는다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)

        composeRule.setMemoDetailTagScreen(tagList = listOf(workTag))
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-53cc153b-b376-47ae-bc33-f71d628c7545 대표 태그를 지정해도 수정 버튼이 나타나지 않는다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)

        composeRule.setMemoDetailTagScreen(tagList = listOf(workTag))
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-53cc153b-b376-47ae-bc33-f71d628c7545 태그 선택을 해제해도 수정 버튼이 나타나지 않는다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)

        composeRule.setMemoDetailTagScreen(tagViewModel = memoDetailTagViewModel(tagList = listOf(workTag), selectedTagList = listOf(workTag)))
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-53cc153b-b376-47ae-bc33-f71d628c7545 대표 태그 지정을 해제해도 수정 버튼이 나타나지 않는다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)

        composeRule.setMemoDetailTagScreen(
            tagViewModel = memoDetailTagViewModel(tagList = listOf(workTag), selectedTagList = listOf(workTag), primaryTagId = workTag.id),
        )
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_UNSET_DESCRIPTION)[0].performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-06ad0a11-9bf1-4db0-b463-d4f9f2a19cc2 태그를 선택해도 안내 스낵바를 표시하지 않는다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)

        composeRule.setMemoDetailTagScreen(tagList = listOf(workTag))
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-56f753c7-fccd-447c-a3bf-3a875a383274 태그 변경이 저장에 반영되지 않으면 저장된 태그를 그대로 표시한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)

        composeRule.setMemoDetailTagScreen(tagList = listOf(workTag))
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-e21d7a9c-b715-4b2a-9b65-813e345bcc10 완료된 메모에서도 태그를 선택할 수 있다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val tagUiState = MutableStateFlow(MemoTagInputUiState())
        val tagViewModel = screenTestTagViewModel(uiState = tagUiState, tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(listOf(workTag))))

        composeRule.setMemoDetailScreenWithTag(
            uiState = MutableStateFlow(memoDetailUiState(detail = memoDetail(MEMO_TITLE), isFinished = true)),
            tagViewModel = tagViewModel,
        )
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        // 연결이 저장되면 태그 입력은 저장된 연결을 다시 조회해 보여 준다.
        tagUiState.value = MemoTagInputUiState(selectedTagList = listOf(workTag))
        composeRule.closeDialogByBack()
        composeRule.waitForIdle()

        verify(exactly = 1) { tagViewModel.selectTag(tagId = workTag.id) }
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-39024d94-6c0e-4735-852c-1a56f7a559c3 태그 조작을 이어서 수행하면 모두 전달된다`() {
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
    fun `TC-130270d5-4801-4215-b84d-029a6f8d8b96 태그를 선택하면 선택한 태그의 연결 반영을 요청한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val tagViewModel = memoDetailTagViewModel(tagList = listOf(workTag))

        composeRule.setMemoDetailTagScreen(tagViewModel = tagViewModel)
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()

        verify(exactly = 1) { tagViewModel.selectTag(tagId = workTag.id) }
    }

    @Test
    fun `TC-278d2a9d-3386-4cee-a542-56f2577f0050 태그 선택을 해제하면 연결 해제 반영을 요청한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val tagViewModel = memoDetailTagViewModel(tagList = listOf(workTag), selectedTagList = listOf(workTag))

        composeRule.setMemoDetailTagScreen(tagViewModel = tagViewModel)
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()

        verify(exactly = 1) { tagViewModel.unselectTag(tagId = workTag.id) }
    }

    @Test
    fun `TC-121409a2-4451-472a-bd3b-c6986bf6ff27 연결되지 않은 태그를 대표 태그로 지정하면 대표 지정 반영을 요청한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val tagViewModel = memoDetailTagViewModel(tagList = listOf(workTag))

        composeRule.setMemoDetailTagScreen(tagViewModel = tagViewModel)
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()

        verify(exactly = 1) { tagViewModel.selectPrimaryTag(tagId = workTag.id) }
    }

    @Test
    fun `TC-8f163fe3-4081-49db-adc8-22e85df13732 대표 태그 지정을 해제하면 대표 지정 해제 반영을 요청한다`() {
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
    fun `TC-435c14a0-7434-48a6-9775-b8910c85738a 목록의 연결된 완료된 태그를 누르면 연결 해제 반영을 요청한다`() {
        val finishedTag = testTag(title = EXERCISE_TAG_TITLE).copy(isFinished = true)
        val tagViewModel = memoDetailTagViewModel(tagList = listOf(finishedTag), selectedTagList = listOf(finishedTag))

        composeRule.setMemoDetailTagScreen(tagViewModel = tagViewModel)
        composeRule.openMemoDetailTagPicker()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).performClick()

        verify(exactly = 1) { tagViewModel.unselectTag(tagId = finishedTag.id) }
    }

    @Test
    fun `TC-57b8be47-262b-4644-9fbb-ec7776bdff22 목록의 연결된 완료된 태그를 대표 태그로 지정하면 대표 지정 반영을 요청한다`() {
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
    fun `TC-39d55be9-24bb-4cf6-a389-20fa8a9896be 태그 칩을 누르면 그 태그의 TagDetail 화면으로 이동한다`() {
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
