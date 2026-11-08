package io.github.taetae98coding.diary.feature.tag.ui.detail.dday

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DDAY_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.selectTagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.setTagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.tagDDay
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
class TagDetailDDayListPositionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-9f0cf5dd-f93b-4487-9f81-e163de6da9c0 다른 탭에 다녀와도 디데이 목록에서 보던 위치를 유지한다`() {
        val itemList = itemList()
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE)))),
            ddayPagingData = tagEntityPagingData(itemList = itemList),
        )
        composeRule.selectTagDetailTab(DEFAULT_DDAY_TAB_DESCRIPTION)
        composeRule.refreshableList().performScrollToIndex(SCROLLED_INDEX)
        composeRule.waitForIdle()

        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.selectTagDetailTab(DEFAULT_DDAY_TAB_DESCRIPTION)

        composeRule.onNodeWithText(itemList[SCROLLED_INDEX].detail.title).assertIsDisplayed()
        isDisplayed(itemList.first().detail.title).shouldBeFalse()
    }

    private fun isDisplayed(text: String): Boolean =
        runCatching { composeRule.onNodeWithText(text).assertIsDisplayed() }
            .isSuccess

    private companion object {
        private const val ITEM_COUNT = 40
        private const val SCROLLED_INDEX = 30

        fun itemList(): List<DDay> {
            val titlePrefix = Arb.alphanumericString(prefix = "TagDetailDDay").next()
            return List(ITEM_COUNT) { index -> tagDDay(title = "${titlePrefix}Index$index") }
        }
    }
}
