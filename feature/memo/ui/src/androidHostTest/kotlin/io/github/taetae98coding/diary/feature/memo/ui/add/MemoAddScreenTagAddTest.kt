package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.closeDialogByBack
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.sendTagAddedResult
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_PICKER_TAG_ADD
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_PICKER_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_PRIMARY_SET_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_PRIMARY_TAG_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_TAG_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.tag.EXERCISE_TAG_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.tag.WORK_TAG_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.tag.awaitTagPickerRows
import io.github.taetae98coding.diary.feature.memo.ui.tag.dialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.tag.dialogNodesWithContentDescription
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

internal const val FIRST_ADDED_TAG_TITLE: String = "MemoTagFirstAdded"
internal val OTHER_TAG_ADD_REQUEST_KEY: Uuid = Uuid.parse("10000000-0000-0000-0000-000000000002")
internal const val SECOND_ADDED_TAG_TITLE: String = "MemoTagSecondAdded"

internal fun ComposeContentTestRule.openTagPicker() {
    onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo().performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.setMemoAddScreenForTagAdd(
    viewModels: MemoAddScreenViewModels,
    navigateToTagAdd: () -> Unit = {},
    resultEventBus: ResultEventBus = ResultEventBus(),
) {
    setContent {
        MemoAddScreenTestTheme(resultEventBus = resultEventBus) {
            MemoAddScreen(
                tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                addViewModel = viewModels.viewModel,
                tagViewModel = viewModels.tagViewModel,
                webViewModel = viewModels.webViewModel,
                contactViewModel = viewModels.contactViewModel,
                placeViewModel = viewModels.placeViewModel,
                placeMapViewModel = screenTestPlaceMapViewModel(),
                geminiViewModel = screenTestGeminiViewModel(),
                navigateUp = {},
                navigateToTagAdd = navigateToTagAdd,
                navigateToTagDetail = {},
                navigateToWebAdd = {},
                navigateToWebDetail = {},
                navigateToContactAdd = {},
                navigateToContactDetail = {},
                navigateToPlaceAdd = {},
                navigateToPlaceDetail = {},
                initialDateRange = null,
                componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                isStandalone = true,
            )
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MemoAddScreenTagAddTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-033 목록의 태그 추가 항목을 누르면 목록이 닫히고 TagAdd 이동을 요청한다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        var tagAddCount = 0
        composeRule.setMemoAddScreenForTagAdd(
            viewModels = screenTestRealViewModel(tagList = tagList),
            navigateToTagAdd = { tagAddCount += 1 },
        )

        composeRule.openTagPicker()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-035 태그를 하나도 추가하지 않고 돌아오면 선택과 대표 태그 지정이 그대로 유지된다`() {
        val tagList = listOf(testTag(title = WORK_TAG_TITLE), testTag(title = EXERCISE_TAG_TITLE))
        var tagAddCount = 0
        composeRule.setMemoAddScreenForTagAdd(
            viewModels = screenTestRealViewModel(tagList = tagList),
            navigateToTagAdd = { tagAddCount += 1 },
        )
        composeRule.selectWorkTagAndPrimaryExerciseTag()

        composeRule.openTagPicker()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        tagAddCount shouldBe 1
        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onAllNodesWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-036 TagAdd 화면에서 돌아와도 태그 선택 목록이 저절로 열리지 않는다`() {
        val addedTag = testTag(title = EXERCISE_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        composeRule.setMemoAddScreenForTagAdd(
            viewModels = screenTestRealViewModel(tagList = listOf(addedTag)),
            resultEventBus = resultEventBus,
        )
        composeRule.openTagPicker()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodeWithText(DEFAULT_PICKER_TAG_ADD).performClick()
        composeRule.waitForIdle()

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertIsDisplayed()
    }

    private fun ComposeContentTestRule.selectWorkTagAndPrimaryExerciseTag() {
        openTagPicker()
        awaitTagPickerRows()
        dialogNodeWithText(WORK_TAG_TITLE).performClick()
        dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[1].performClick()
        closeDialogByBack()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MemoAddScreenTagAddedResultTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-034 TagAdd 화면에서 추가한 태그 하나가 돌아왔을 때 선택된다`() {
        val selectedTag = testTag(title = WORK_TAG_TITLE)
        val addedTag = testTag(title = EXERCISE_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        composeRule.setMemoAddScreenForTagAdd(
            viewModels = screenTestRealViewModel(tagList = listOf(selectedTag, addedTag)),
            resultEventBus = resultEventBus,
        )
        composeRule.selectTag(title = WORK_TAG_TITLE)

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-034 TagAdd 화면에서 추가한 태그 여러 개가 돌아왔을 때 모두 선택된다`() {
        val selectedTag = testTag(title = WORK_TAG_TITLE)
        val firstAddedTag = testTag(title = FIRST_ADDED_TAG_TITLE)
        val secondAddedTag = testTag(title = SECOND_ADDED_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        composeRule.setMemoAddScreenForTagAdd(
            viewModels = screenTestRealViewModel(tagList = listOf(selectedTag, firstAddedTag, secondAddedTag)),
            resultEventBus = resultEventBus,
        )
        composeRule.selectTag(title = WORK_TAG_TITLE)

        resultEventBus.sendTagAddedResult(id = firstAddedTag.id)
        resultEventBus.sendTagAddedResult(id = secondAddedTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(FIRST_ADDED_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_ADDED_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-DOMAIN-013 대표 태그를 지정하지 않았으면 자동 선택 후에도 대표 태그가 없다`() {
        val addedTag = testTag(title = EXERCISE_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        composeRule.setMemoAddScreenForTagAdd(
            viewModels = screenTestRealViewModel(tagList = listOf(addedTag)),
            resultEventBus = resultEventBus,
        )

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onAllNodesWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertCountEquals(0)
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-DOMAIN-013 대표 태그를 지정했으면 자동 선택 후에도 대표 태그 지정이 유지된다`() {
        val primaryTag = testTag(title = WORK_TAG_TITLE)
        val addedTag = testTag(title = EXERCISE_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        composeRule.setMemoAddScreenForTagAdd(
            viewModels = screenTestRealViewModel(tagList = listOf(primaryTag, addedTag)),
            resultEventBus = resultEventBus,
        )
        composeRule.selectPrimaryTag(index = 0)

        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onAllNodesWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-DOMAIN-014 자동 선택된 태그의 선택을 해제하면 다시 선택되지 않는다`() {
        val addedTag = testTag(title = EXERCISE_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        composeRule.setMemoAddScreenForTagAdd(
            viewModels = screenTestRealViewModel(tagList = listOf(addedTag)),
            resultEventBus = resultEventBus,
        )
        resultEventBus.sendTagAddedResult(id = addedTag.id)
        composeRule.waitForIdle()

        composeRule.openTagPicker()
        composeRule.awaitTagPickerRows()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).performClick()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-DOMAIN-020 이 입력에서 이동하지 않은 TagAdd 화면의 태그는 자동 선택되지 않는다`() {
        val addedTag = testTag(title = EXERCISE_TAG_TITLE)
        val resultEventBus = ResultEventBus()
        composeRule.setMemoAddScreenForTagAdd(
            viewModels = screenTestRealViewModel(tagList = listOf(addedTag)),
            resultEventBus = resultEventBus,
        )

        resultEventBus.sendTagAddedResult(id = addedTag.id, requestKey = OTHER_TAG_ADD_REQUEST_KEY)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertDoesNotExist()
    }

    private fun ComposeContentTestRule.selectTag(title: String) {
        openTagPicker()
        awaitTagPickerRows()
        dialogNodeWithText(title).performClick()
        closeDialogByBack()
    }

    private fun ComposeContentTestRule.selectPrimaryTag(index: Int) {
        openTagPicker()
        awaitTagPickerRows()
        dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[index].performClick()
        closeDialogByBack()
    }
}
