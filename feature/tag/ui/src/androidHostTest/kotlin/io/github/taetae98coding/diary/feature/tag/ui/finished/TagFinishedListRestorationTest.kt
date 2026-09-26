package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.fixtureText
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagFinishedListRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-FINISHED-LIST-DOMAIN-010 화면이 재생성되어도 정렬 선택과 보던 자리가 그대로다`() {
        val titlePrefix = fixtureText(prefix = "FinishedTag")
        val tagList = List(TAG_COUNT) { index -> finishedTag(title = "${titlePrefix}Index$index") }
        val sortFlow = MutableStateFlow(ListSort.TITLE)
        val viewModel = mockk<TagFinishedListViewModel>(relaxed = true)
        every { viewModel.sort } returns sortFlow
        every { viewModel.select(sort = any()) } answers { sortFlow.value = firstArg() }
        every { viewModel.tagPagingData } returns MutableStateFlow(tagPagingDataOf(tagList))
        every { viewModel.effect } returns emptyFlow()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                TagFinishedListScreen(
                    navigateUp = {},
                    navigateToDetail = {},
                    tagViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(tagList.first().detail.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_SORT_SHEET_TITLE).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag(TAG_FINISHED_LIST_TEST_TAG).performScrollToIndex(SCROLL_INDEX)
        composeRule.onNodeWithText(tagList[SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(tagList.first().detail.title).assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertDoesNotExist()
        composeRule.onNodeWithText(tagList[SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(tagList.first().detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-DOMAIN-013 시스템이 앱을 정리한 뒤 다시 만들면 정렬은 처음으로 돌아가고 보던 위치는 다시 보인다`() {
        val titlePrefix = fixtureText(prefix = "FinishedTag")
        val tagList = List(TAG_COUNT) { index -> finishedTag(title = "${titlePrefix}Index$index") }
        var nextViewModel = sortableViewModel(tagList = tagList)
        val restorationTester = StateRestorationTester(composeRule)
        // 시스템이 앱을 정리하면 정렬을 들고 있던 ViewModel도 사라지므로, 복원으로 컴포지션을 다시 만들 때만 새 ViewModel을 받게 한다.
        restorationTester.setContent {
            val viewModel = remember { nextViewModel }
            DiaryTheme {
                TagFinishedListScreen(
                    navigateUp = {},
                    navigateToDetail = {},
                    tagViewModel = viewModel,
                    syncViewModel = screenTestSyncViewModel(),
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(tagList.first().detail.title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).performClick()
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_SORT_SHEET_TITLE).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag(TAG_FINISHED_LIST_TEST_TAG).performScrollToIndex(SCROLL_INDEX)
        composeRule.onNodeWithText(tagList[SCROLL_INDEX].detail.title).assertIsDisplayed()
        nextViewModel = sortableViewModel(tagList = tagList)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_TITLE_SORT).assertExists()
        composeRule.onNodeWithText(DEFAULT_RECENTLY_UPDATED_SORT).assertDoesNotExist()
        composeRule.onNodeWithText(tagList[SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(tagList.first().detail.title).assertDoesNotExist()
    }

    private fun sortableViewModel(tagList: List<Tag>): TagFinishedListViewModel {
        val sortFlow = MutableStateFlow(ListSort.TITLE)
        val viewModel = mockk<TagFinishedListViewModel>(relaxed = true)
        every { viewModel.sort } returns sortFlow
        every { viewModel.select(sort = any()) } answers { sortFlow.value = firstArg() }
        every { viewModel.tagPagingData } returns MutableStateFlow(tagPagingDataOf(tagList))
        every { viewModel.effect } returns emptyFlow()

        return viewModel
    }

    private fun finishedTag(title: String): Tag =
        fixtureMonkey
            .giveMeKotlinBuilder<Tag>()
            .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = title))
            .setExp(Tag::isFinished, true)
            .setExp(Tag::isDeleted, false)
            .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
            .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
            .sample()

    private companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val TAG_COUNT = 60
        private const val SCROLL_INDEX = 50
        private const val DEFAULT_SORT_DESCRIPTION = "List sort"
        private const val DEFAULT_SORT_SHEET_TITLE = "Sort"
        private const val DEFAULT_TITLE_SORT = "Title"
        private const val DEFAULT_RECENTLY_UPDATED_SORT = "Recently updated"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
