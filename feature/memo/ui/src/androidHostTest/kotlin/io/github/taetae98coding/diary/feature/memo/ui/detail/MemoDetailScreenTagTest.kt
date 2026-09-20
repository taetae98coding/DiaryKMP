package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_PRIMARY_TAG_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_TAG_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.tag.EXERCISE_TAG_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.tag.WORK_TAG_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.tag.awaitTagPickerRows
import io.github.taetae98coding.diary.feature.memo.ui.tag.dialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.tag.dialogNodesWithContentDescription
import io.github.taetae98coding.diary.feature.memo.ui.tag.tagPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
import io.github.taetae98coding.diary.feature.memo.ui.web.screenTestWebViewModel
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScreenTagTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-033 첫진입 시 저장된 태그 연결과 대표 태그가 태그 입력에 표시된다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val exerciseTag = testTag(title = EXERCISE_TAG_TITLE)

        composeRule.setMemoDetailScreenWithTag(
            tagUiState =
                MutableStateFlow(
                    MemoTagInputUiState(selectedTagList = listOf(workTag, exerciseTag), primaryTagId = workTag.id),
                ),
            tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(listOf(workTag, exerciseTag))),
        )
        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-041 화면이 재생성되면 태그 입력은 저장된 태그를 다시 표시한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val exerciseTag = testTag(title = EXERCISE_TAG_TITLE)
        val tagUiState =
            MutableStateFlow(
                MemoTagInputUiState(selectedTagList = listOf(workTag, exerciseTag), primaryTagId = workTag.id),
            )
        val tagViewModel =
            screenTestTagViewModel(
                uiState = tagUiState,
                tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(listOf(workTag, exerciseTag))),
            )
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            MemoDetailScreenTestTheme {
                MemoDetailScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(detail = memoDetail(MEMO_TITLE)))),
                    tagViewModel = tagViewModel,
                    webViewModel = screenTestWebViewModel(),
                    placeViewModel = screenTestPlaceViewModel(uiState = MutableStateFlow(MemoPlaceInputUiState(isSelectedPlaceLoaded = true))),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToCopiedMemo = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                    componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertExists()
    }

    @Test
    fun `로딩 상태에서는 태그 입력을 표시하지 않는다`() {
        composeRule.setMemoDetailScreenWithTag(
            uiState = MutableStateFlow(MemoDetailUiState.Loading),
            tagUiState = MutableStateFlow(MemoTagInputUiState()),
            tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(listOf(testTag(title = WORK_TAG_TITLE)))),
        )

        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-038 저장된 태그 연결이 바뀌면 태그 입력에 반영된다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val tagUiState = MutableStateFlow(MemoTagInputUiState())

        composeRule.setMemoDetailScreenWithTag(
            tagUiState = tagUiState,
            tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(listOf(workTag))),
        )
        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()

        tagUiState.value = MemoTagInputUiState(selectedTagList = listOf(workTag), primaryTagId = workTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-047 TC-MEMO-DETAIL-FEATURE-048 선택 목록에 없는 완료된 태그도 대표 태그 칩으로 표시된다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val finishedTag = testTag(title = EXERCISE_TAG_TITLE).copy(isFinished = true)

        composeRule.setMemoDetailScreenWithTag(
            tagUiState =
                MutableStateFlow(
                    MemoTagInputUiState(selectedTagList = listOf(workTag, finishedTag), primaryTagId = finishedTag.id),
                ),
            tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(listOf(workTag))),
        )
        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-050 연결된 태그가 완료되어도 태그 칩 표시가 유지된다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val tagUiState = MutableStateFlow(MemoTagInputUiState(selectedTagList = listOf(workTag), primaryTagId = workTag.id))

        composeRule.setMemoDetailScreenWithTag(
            tagUiState = tagUiState,
            tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(listOf(workTag))),
        )
        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()

        val finishedTag = workTag.copy(isFinished = true)
        tagUiState.value = MemoTagInputUiState(selectedTagList = listOf(finishedTag), primaryTagId = finishedTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "w480dp-h1200dp")
    fun `TC-MEMO-DETAIL-FEATURE-051 연결된 완료된 태그는 태그 선택 목록에 함께 나타난다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val finishedTag = testTag(title = EXERCISE_TAG_TITLE).copy(isFinished = true)

        composeRule.setMemoDetailScreenWithTag(
            tagUiState =
                MutableStateFlow(
                    MemoTagInputUiState(selectedTagList = listOf(finishedTag), primaryTagId = finishedTag.id),
                ),
            tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(listOf(workTag, finishedTag))),
        )
        composeRule.openMemoDetailTagPicker()
        composeRule.awaitTagPickerRows()

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_UNSET_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    @Config(qualifiers = "w480dp-h1200dp")
    fun `TC-MEMO-DETAIL-FEATURE-052 연결되지 않은 완료된 태그는 태그 선택 목록에 나타나지 않는다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val finishedTag = testTag(title = EXERCISE_TAG_TITLE).copy(isFinished = true)

        composeRule.setMemoDetailScreenWithTag(
            tagUiState = MutableStateFlow(MemoTagInputUiState()),
            tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(listOf(workTag))),
        )
        composeRule.openMemoDetailTagPicker()
        composeRule.awaitTagPickerRows()

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(finishedTag.detail.title).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w480dp-h1200dp")
    fun `TC-MEMO-TAG-INPUT-DOMAIN-012 선택할 수 있는 태그가 없어도 목록 대상이 있으면 목록이 열린다`() {
        val finishedTag = testTag(title = WORK_TAG_TITLE).copy(isFinished = true)
        var tagAddCount = 0

        composeRule.setMemoDetailScreenWithTag(
            tagUiState = MutableStateFlow(MemoTagInputUiState(selectedTagList = listOf(finishedTag))),
            tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(listOf(finishedTag))),
            navigateToTagAdd = { tagAddCount += 1 },
        )
        composeRule.openMemoDetailTagPicker()
        composeRule.awaitTagPickerRows()

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        tagAddCount shouldBe 0
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-039 다른 메모를 선택하면 태그 입력이 새 메모의 태그로 바뀐다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val exerciseTag = testTag(title = EXERCISE_TAG_TITLE)
        val tagList = listOf(workTag, exerciseTag)
        val uiState = MutableStateFlow<MemoDetailUiState>(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE)))
        val tagUiState = MutableStateFlow(MemoTagInputUiState(selectedTagList = listOf(workTag)))

        composeRule.setMemoDetailScreenWithTag(
            uiState = uiState,
            tagUiState = tagUiState,
            tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(tagList)),
        )
        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()

        uiState.value = memoDetailUiState(id = SECOND_TAG_MEMO_ID, detail = memoDetail(MEMO_TITLE))
        tagUiState.value = MemoTagInputUiState(selectedTagList = listOf(exerciseTag))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo()

        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
    }

    private companion object {
        val SECOND_TAG_MEMO_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000012")
    }
}
