package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import io.github.taetae98coding.diary.feature.memo.ui.TEST_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.contact.screenTestContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlacePickerMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_PICKER_TAG_ADD
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_PICKER_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_TAG_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.tag.EXERCISE_TAG_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.tag.MemoTagInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.tag.WORK_TAG_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.tag.awaitTagPickerRows
import io.github.taetae98coding.diary.feature.memo.ui.tag.dialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.tag.dialogNodesWithContentDescription
import io.github.taetae98coding.diary.feature.memo.ui.tag.hasPrimaryTagState
import io.github.taetae98coding.diary.feature.memo.ui.tag.tagPagingDataOf
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
import io.github.taetae98coding.diary.feature.memo.ui.web.screenTestWebViewModel
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
class MemoDetailScreenTagTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-5282a1f8-8c3c-4dd2-b017-585e42c532b0 첫진입 시 저장된 태그 연결과 대표 태그가 태그 입력에 표시된다`() {
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
        composeRule.onNode(hasPrimaryTagState()).assertExists()
    }

    @Test
    fun `TC-6fbe4030-a373-4a94-90db-e244df84df81 화면이 재생성되면 태그 입력은 저장된 태그를 다시 표시한다`() {
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
                    addRequestKey = TEST_ADD_REQUEST_KEY,
                    detailViewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(detail = memoDetail(MEMO_TITLE)))),
                    tagViewModel = tagViewModel,
                    webViewModel = screenTestWebViewModel(),
                    contactViewModel = screenTestContactViewModel(),
                    placeViewModel = screenTestPlaceViewModel(uiState = MutableStateFlow(MemoPlaceInputUiState(isSelectedPlaceLoaded = true))),
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    placePickerMapViewModel = screenTestPlacePickerMapViewModel(),
                    geminiViewModel = screenTestGeminiViewModel(),
                    navigateUp = {},
                    navigateToCopiedMemo = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToContactAdd = {},
                    navigateToContactDetail = {},
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
        composeRule.onNode(hasPrimaryTagState()).assertExists()
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
    fun `TC-ef9f8c54-750a-42b9-9491-ef622a6f420d 저장된 태그 연결이 바뀌면 태그 입력에 반영된다`() {
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
        composeRule.onNode(hasPrimaryTagState()).assertExists()
    }

    @Test
    fun `TC-4178547d-4ad2-4d73-a5e3-68e4350e7dd9 TC-fc9a90ad-aa02-460e-b0dc-7e42d8bfce62 선택 목록에 없는 완료된 태그도 대표 태그 칩으로 표시된다`() {
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
        composeRule.onNode(hasPrimaryTagState()).assertExists()
    }

    @Test
    fun `TC-f845b60b-e09c-42ac-8851-e3c6e08b035c 연결된 태그가 완료되어도 태그 칩 표시가 유지된다`() {
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
        composeRule.onNode(hasPrimaryTagState()).assertExists()
    }

    @Test
    @Config(qualifiers = "w480dp-h1200dp")
    fun `TC-5b370c4c-d285-41ba-965b-da7e6ecf8c95 연결된 완료된 태그는 태그 선택 목록에 함께 나타난다`() {
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
    fun `TC-c8b46a53-3227-4f18-9061-defb7a9cf9de 연결되지 않은 완료된 태그는 태그 선택 목록에 나타나지 않는다`() {
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
    fun `TC-97393c65-b806-4cd0-af8d-7aaddba9b897 태그가 하나도 없어도 추가 항목을 누르면 태그 선택 목록이 열린다`() {
        var tagAddCount = 0

        composeRule.setMemoDetailScreenWithTag(
            tagUiState = MutableStateFlow(MemoTagInputUiState()),
            tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(emptyList())),
            navigateToTagAdd = { tagAddCount += 1 },
        )
        composeRule.openMemoDetailTagPicker()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).assertExists()
        tagAddCount shouldBe 0
    }

    @Test
    fun `TC-20490a86-d4b9-497f-96fc-6cf6c586e5bc 다른 메모를 선택하면 태그 입력이 새 메모의 태그로 바뀐다`() {
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
