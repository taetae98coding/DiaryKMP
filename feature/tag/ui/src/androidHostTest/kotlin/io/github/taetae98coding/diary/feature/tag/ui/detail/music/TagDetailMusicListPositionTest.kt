package io.github.taetae98coding.diary.feature.tag.ui.detail.music

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MUSIC_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.selectTagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.setTagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagMusic
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
class TagDetailMusicListPositionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-88aee913-a13a-4d95-a7b0-0288dcc32f84 다른 탭에 다녀와도 곡 목록에서 보던 위치를 유지한다`() {
        val itemList = itemList()
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE)))),
            musicPagingData = tagEntityPagingData(itemList = itemList),
        )
        composeRule.selectTagDetailTab(DEFAULT_MUSIC_TAB_DESCRIPTION)
        composeRule.refreshableList().performScrollToIndex(SCROLLED_INDEX)
        composeRule.waitForIdle()

        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.selectTagDetailTab(DEFAULT_MUSIC_TAB_DESCRIPTION)

        composeRule.onNodeWithText(itemList[SCROLLED_INDEX].detail.title).assertIsDisplayed()
        isDisplayed(itemList.first().detail.title).shouldBeFalse()
    }

    private fun isDisplayed(text: String): Boolean =
        runCatching { composeRule.onNodeWithText(text).assertIsDisplayed() }
            .isSuccess

    private companion object {
        private const val ITEM_COUNT = 40
        private const val SCROLLED_INDEX = 30

        fun itemList(): List<Music> {
            val titlePrefix = Arb.alphanumericString(prefix = "TagDetailMusic").next()
            return List(ITEM_COUNT) { index -> tagMusic(title = "${titlePrefix}Index$index") }
        }
    }
}
