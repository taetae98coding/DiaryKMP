package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.height
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoTagPickerDialogPagingTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-025 다음 태그를 불러오는 동안에도 이미 나타난 태그를 조작할 수 있다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        val selectedIdList = mutableListOf<Uuid>()
        val primarySelectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoTagPickerDialog(
            tagPagingData = appendingTagPagingDataOf(listOf(tag)),
            onTagSelect = selectedIdList::add,
            onPrimaryTagSelect = primarySelectedIdList::add,
        )

        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)[0].performClick()
        composeRule.dialogNodeWithText(WORK_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        selectedIdList shouldBe listOf(tag.id)
        primarySelectedIdList shouldBe listOf(tag.id)
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-026 다음 태그를 불러오지 못해도 이미 나타난 태그를 유지한다`() {
        val workTag = testTag(title = WORK_TAG_TITLE)
        val exerciseTag = testTag(title = EXERCISE_TAG_TITLE)
        composeRule.setMemoTagPickerDialog(tagPagingData = appendFailedTagPagingDataOf(listOf(workTag, exerciseTag)))

        composeRule.dialogNodeWithText(WORK_TAG_TITLE).assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        // 오류 안내나 재시도 항목이 없으므로 목록 항목 수는 준비된 태그 수와 같다.
        composeRule.dialogNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION).assertCountEquals(2)
        composeRule.onNodeWithText(DEFAULT_PICKER_TITLE).assertExists()
    }

    @Test
    fun `준비되지 않은 자리는 준비된 항목과 같은 높이의 빈 항목으로 표시한다`() {
        val tag = testTag(title = WORK_TAG_TITLE)
        val unselectedIdList = mutableListOf<Uuid>()
        val primarySelectedIdList = mutableListOf<Uuid>()

        composeRule.setContent {
            DiaryTheme {
                Column {
                    MemoTagPickerRow(
                        onEvent = { event -> recordTagPickerEvent(event, unselectedIdList, primarySelectedIdList) },
                        tag = tag,
                        isSelected = true,
                        isPrimary = false,
                        modifier = Modifier.testTag(PREPARED_ROW_TEST_TAG),
                    )
                    MemoTagPickerRow(
                        onEvent = { event -> recordTagPickerEvent(event, unselectedIdList, primarySelectedIdList) },
                        tag = null,
                        isSelected = false,
                        isPrimary = false,
                        modifier = Modifier.testTag(PLACEHOLDER_ROW_TEST_TAG),
                    )
                }
            }
        }

        val placeholder = composeRule.onNodeWithTag(PLACEHOLDER_ROW_TEST_TAG)
        placeholder.performClick()
        composeRule
            .onAllNodesWithContentDescription(DEFAULT_PRIMARY_SET_DESCRIPTION)
            .assertCountEquals(1)
        composeRule.waitForIdle()

        placeholder.getUnclippedBoundsInRoot().height shouldBe
            composeRule
                .onNodeWithTag(PREPARED_ROW_TEST_TAG)
                .getUnclippedBoundsInRoot()
                .height
        unselectedIdList.shouldBeEmpty()
        primarySelectedIdList.shouldBeEmpty()
    }

    private fun recordTagPickerEvent(
        event: MemoTagPickerEvent,
        unselectedIdList: MutableList<Uuid>,
        primarySelectedIdList: MutableList<Uuid>,
    ) {
        when (event) {
            is MemoTagPickerEvent.Unselect -> unselectedIdList += event.id
            is MemoTagPickerEvent.SelectPrimary -> primarySelectedIdList += event.id
            else -> Unit
        }
    }

    public companion object {
        private const val PREPARED_ROW_TEST_TAG: String = "PreparedTagPickerRow"
        private const val PLACEHOLDER_ROW_TEST_TAG: String = "PlaceholderTagPickerRow"
    }
}
