package io.github.taetae98coding.diary.feature.tag.ui.detail.checklist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_CHECKLIST_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.selectTagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.setTagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.tagChecklist
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailChecklistListPositionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-bb9a0ad4-238f-4737-9e51-068c5a3f7c7b 다른 탭에 다녀와도 체크리스트 목록에서 보던 위치를 유지한다`() {
        val itemList = itemList()
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE)))),
            checklistPagingData = tagEntityPagingData(itemList = itemList),
        )
        composeRule.selectTagDetailTab(DEFAULT_CHECKLIST_TAB_DESCRIPTION)
        composeRule.refreshableList().performScrollToIndex(SCROLLED_INDEX)
        composeRule.waitForIdle()

        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.selectTagDetailTab(DEFAULT_CHECKLIST_TAB_DESCRIPTION)

        composeRule.onNodeWithText(itemList[SCROLLED_INDEX].checklist.detail.title).assertIsDisplayed()
        isDisplayed(
            itemList
                .first()
                .checklist.detail.title,
        ).shouldBeFalse()
    }

    private fun isDisplayed(text: String): Boolean =
        runCatching { composeRule.onNodeWithText(text).assertIsDisplayed() }
            .isSuccess

    private companion object {
        private const val ITEM_COUNT = 40
        private const val SCROLLED_INDEX = 30

        fun itemList(): List<ChecklistSummary> {
            val titlePrefix = Arb.alphanumericString(prefix = "TagDetailChecklist").next()
            return List(ITEM_COUNT) { index -> tagChecklist(title = "${titlePrefix}Index$index") }
        }
    }
}
