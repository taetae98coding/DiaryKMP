package io.github.taetae98coding.diary.feature.calendar.ui.home.filter

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.TagFilterEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeFilterBottomSheetContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-048 태그 필터에 전체 태그와 선택 여부를 표시한다`() {
        val tagList = listOf(tag(title = FIRST_TAG_TITLE), tag(title = SECOND_TAG_TITLE))

        setBottomSheetContent(
            tagList = tagList,
            uiState = CalendarHomeFilterUiState(selectedTagIdSet = setOf(tagList.first().id)),
        )

        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsSelected()
        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertIsNotSelected()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-056 필터 칩에 이모지와 제목을 함께 표시한다`() {
        val tagList =
            listOf(
                tag(title = FIRST_TAG_TITLE, emoji = TAG_EMOJI),
                tag(title = SECOND_TAG_TITLE),
            )

        setBottomSheetContent(tagList = tagList)

        composeRule.onNodeWithText("$TAG_EMOJI $FIRST_TAG_TITLE").assertExists()
        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(" $SECOND_TAG_TITLE").assertDoesNotExist()
    }

    @Test
    fun `선택되지 않은 태그를 누르면 선택 Event를 전달한다`() {
        val tag = tag(title = FIRST_TAG_TITLE)
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(
            tagList = listOf(tag),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithText(FIRST_TAG_TITLE).performClick()

        eventList shouldBe listOf(TagFilterEvent.Select(id = tag.id))
    }

    @Test
    fun `선택된 태그를 누르면 선택 해제 Event를 전달한다`() {
        val tag = tag(title = FIRST_TAG_TITLE)
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(
            tagList = listOf(tag),
            uiState = CalendarHomeFilterUiState(selectedTagIdSet = setOf(tag.id)),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithText(FIRST_TAG_TITLE).performClick()

        eventList shouldBe listOf(TagFilterEvent.Unselect(id = tag.id))
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-082 지우기를 누르면 선택 전체 해제 Event를 전달한다`() {
        val tag = tag(title = FIRST_TAG_TITLE)
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(
            tagList = listOf(tag),
            uiState = CalendarHomeFilterUiState(selectedTagIdSet = setOf(tag.id)),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).performClick()

        eventList shouldBe listOf(TagFilterEvent.UnselectAll)
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-083 선택한 태그가 없으면 지우기를 실행할 수 없다`() {
        val tag = tag(title = FIRST_TAG_TITLE)
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(
            tagList = listOf(tag),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).assertIsNotEnabled()
        composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).performClick()

        eventList.shouldBeEmpty()
    }

    private fun setBottomSheetContent(
        tagList: List<Tag> = emptyList(),
        uiState: CalendarHomeFilterUiState = CalendarHomeFilterUiState(),
        onEvent: (TagFilterEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                CalendarHomeFilterBottomSheetContent(
                    tagPagingItems = MutableStateFlow(tagPagingDataOf(tagList)).collectAsLazyPagingItems(),
                    uiStateProvider = { uiState },
                    onEvent = onEvent,
                )
            }
        }
    }

    companion object {
        private const val FIRST_TAG_TITLE = "CalendarFilterAlpha"
        private const val SECOND_TAG_TITLE = "CalendarFilterBravo"
        private const val TAG_EMOJI = "🏃"
        private const val UNSELECT_ALL_CONTENT_DESCRIPTION = "Clear tag filter"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(
            title: String,
            emoji: String = "",
        ): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = emoji, title = title))
                .setExp(Tag::isFinished, false)
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
