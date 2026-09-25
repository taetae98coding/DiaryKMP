package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagFinishedListScaffoldRefreshTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-001 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagFinishedListScaffoldEvent>()
        setTagFinishedListScaffold(
            tagList = listOf(tag()),
            onEvent = { event -> eventList += event },
        )

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagFinishedListScaffoldEvent.Refresh)
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-002 진행 표시 상태이면 진행 표시가 나타난다`() {
        setTagFinishedListScaffold(uiState = TagFinishedListUiState(isRefreshing = true))

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-004 진행 표시 상태가 아니면 진행 표시가 나타나지 않는다`() {
        setTagFinishedListScaffold(uiState = TagFinishedListUiState(isRefreshing = false))

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    private fun setTagFinishedListScaffold(
        tagList: List<Tag> = emptyList(),
        uiState: TagFinishedListUiState = TagFinishedListUiState(),
        onEvent: (TagFinishedListScaffoldEvent) -> Unit = {},
    ) {
        val tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(tagList))

        composeRule.setContent {
            DiaryTheme {
                TagFinishedListScaffold(
                    onTagListEvent = {},
                    tagPagingItems = tagPagingDataFlow.collectAsLazyPagingItems(),
                    onEvent = onEvent,
                    uiStateProvider = { uiState },
                )
            }
        }
    }

    public companion object {
        private const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = "FinishedTagTitle"))
                .setExp(Tag::isFinished, true)
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
