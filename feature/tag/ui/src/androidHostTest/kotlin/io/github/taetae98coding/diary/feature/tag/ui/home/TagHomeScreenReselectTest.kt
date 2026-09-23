package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagHomeScreenReselectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-FEATURE-009 태그 목적지를 다시 선택하면 태그 목록의 첫 항목으로 돌아간다`() {
        val reselectEvent = reselectEvent()
        setTagHomeScreen(reselectEvent = reselectEvent)

        composeRule.onNodeWithTag(TAG_HOME_LIST_TEST_TAG).performScrollToIndex(LAST_INDEX)
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(tagTitle(index = 0)).fetchSemanticsNodes().isEmpty() shouldBe true

        reselectEvent.tryEmit(Unit)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(tagTitle(index = 0)).assertIsDisplayed()
    }

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-DOMAIN-010 이미 첫 태그를 보고 있으면 목록 자리를 바꾸지 않는다`() {
        val reselectEvent = reselectEvent()
        setTagHomeScreen(reselectEvent = reselectEvent)

        reselectEvent.tryEmit(Unit)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(tagTitle(index = 0)).assertIsDisplayed()
    }

    private fun setTagHomeScreen(reselectEvent: MutableSharedFlow<Unit>) {
        composeRule.setContent {
            DiaryTheme {
                // TagEntry가 조립하는 것과 같은 구성으로 둔다.
                val gridState = rememberLazyGridState()

                ScrollToFirstTagOnReselectEffect(
                    reselectEvent = reselectEvent,
                    gridState = gridState,
                )
                TagHomeScreen(
                    navigateToAdd = {},
                    navigateToDetail = {},
                    navigateToFilter = {},
                    navigateToFinishedList = {},
                    navigateToSearch = {},
                    gridState = gridState,
                    tagViewModel = reselectTestViewModel(),
                    syncViewModel = screenTestSyncViewModel(),
                    componentVisibleProvider = { TagHomeScaffoldComponentVisible() },
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(tagTitle(index = 0)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    public companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val TAG_COUNT = 40
        private const val LAST_INDEX = TAG_COUNT - 1
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tagTitle(index: Int): String = "TagHomeReselectTitle$index"

        private fun reselectEvent(): MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

        private fun reselectTestViewModel(): TagHomeViewModel {
            val tagList =
                List(TAG_COUNT) { index ->
                    fixtureMonkey
                        .giveMeKotlinBuilder<Tag>()
                        .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = tagTitle(index = index)))
                        .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                        .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                        .sample()
                }
            val viewModel = mockk<TagHomeViewModel>()

            every { viewModel.sort } returns MutableStateFlow(ListSort.TITLE)
            every { viewModel.tagPagingData } returns MutableStateFlow(tagPagingDataOf(tagList))
            every { viewModel.filterUiState } returns MutableStateFlow(TagHomeScaffoldFilterUiState())

            return viewModel
        }
    }
}
