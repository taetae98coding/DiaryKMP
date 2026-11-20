package io.github.taetae98coding.diary.compose.tag.primary

import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.dialog.DIARY_PAGING_PICKER_LIST_TEST_TAG
import io.github.taetae98coding.diary.compose.core.dialog.DIARY_PICKER_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** 선택 목록의 고정 높이를 다 쓸 수 있는 표시 영역에서만 고정 여부를 확인할 수 있으므로 큰 창으로 실행한다. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class PrimaryTagPickerDialogSizeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `태그 선택 목록의 자리 높이는 항목이 하나여도 고정된다`() {
        composeRule.setPrimaryTagPickerDialog(tagList = listOf(Arb.tag(title = WORK_TAG_TITLE, isFinished = false).next()))
        composeRule.awaitTagPickerRows()

        composeRule.onNode(hasTestTag(DIARY_PAGING_PICKER_LIST_TEST_TAG)).assertHeightIsEqualTo(PICKER_LIST_HEIGHT)
    }

    @Test
    fun `태그 선택 목록의 자리 높이는 항목이 자리를 넘겨도 고정된다`() {
        composeRule.setPrimaryTagPickerDialog(tagList = List(ROW_COUNT_OVER_AREA) { index -> Arb.tag(title = "$WORK_TAG_TITLE$index", isFinished = false).next() })
        composeRule.awaitTagPickerRows()

        composeRule.onNode(hasTestTag(DIARY_PAGING_PICKER_LIST_TEST_TAG)).assertHeightIsEqualTo(PICKER_LIST_HEIGHT)
    }

    @Test
    fun `태그 선택 목록의 검색 결과 없음 안내도 같은 자리 높이를 쓴다`() {
        composeRule.setPrimaryTagPickerDialog(
            tagList = emptyList(),
            query = WORK_TAG_QUERY,
        )

        composeRule.onNode(hasTestTag(DIARY_PICKER_EMPTY_BOX_TEST_TAG)).assertHeightIsEqualTo(PICKER_LIST_HEIGHT)
    }

    private companion object {
        private val PICKER_LIST_HEIGHT = 288.dp
        private const val ROW_COUNT_OVER_AREA: Int = 10
    }
}
