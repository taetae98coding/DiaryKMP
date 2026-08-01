package io.github.taetae98coding.diary.feature.memo.ui.home.filter

import androidx.compose.ui.test.assertIsEnabled
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
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.memo.ui.tag.tagPagingDataOf
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
class MemoHomeFilterBottomSheetContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-HOME-FEATURE-020 태그 필터에 전체 태그와 선택 여부를 표시한다`() {
        val tagList = listOf(tag(title = FIRST_TAG_TITLE), tag(title = SECOND_TAG_TITLE))

        setBottomSheetContent(
            tagList = tagList,
            uiState = MemoHomeFilterUiState(selectedTagIdSet = setOf(tagList.first().id)),
        )

        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsSelected()
        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertIsNotSelected()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-028 필터 칩에 이모지와 제목을 함께 표시한다`() {
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
            onTagFilterEvent = eventList::add,
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
            uiState = MemoHomeFilterUiState(selectedTagIdSet = setOf(tag.id)),
            onTagFilterEvent = eventList::add,
        )

        composeRule.onNodeWithText(FIRST_TAG_TITLE).performClick()

        eventList shouldBe listOf(TagFilterEvent.Unselect(id = tag.id))
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-056 지우기를 누르면 선택 전체 해제 Event를 전달한다`() {
        val tag = tag(title = FIRST_TAG_TITLE)
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(
            tagList = listOf(tag),
            uiState = MemoHomeFilterUiState(selectedTagIdSet = setOf(tag.id)),
            onTagFilterEvent = eventList::add,
        )

        composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).performClick()

        eventList shouldBe listOf(TagFilterEvent.UnselectAll)
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-057 선택한 태그가 없으면 지우기를 실행할 수 없다`() {
        val tag = tag(title = FIRST_TAG_TITLE)
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(
            tagList = listOf(tag),
            onTagFilterEvent = eventList::add,
        )

        composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).assertIsNotEnabled()
        composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).performClick()

        eventList.shouldBeEmpty()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-055 태그 축이 없음이면 미적용 안내를 표시하고 태그 필터를 조작할 수 없다`() {
        val firstTag = tag(title = FIRST_TAG_TITLE)
        val secondTag = tag(title = SECOND_TAG_TITLE)
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(
            tagList = listOf(firstTag, secondTag),
            uiState =
                MemoHomeFilterUiState(
                    selectedTagIdSet = setOf(firstTag.id),
                    existence = MemoExistenceFilter(tag = MemoFilterExistence.NOT_EXIST),
                ),
            onTagFilterEvent = eventList::add,
        )

        composeRule.onNodeWithText(TAG_INACTIVE_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertIsNotEnabled()
        composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).assertIsNotEnabled()

        composeRule.onNodeWithText(SECOND_TAG_TITLE).performClick()
        composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).performClick()

        eventList.shouldBeEmpty()
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsSelected()
        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertIsNotSelected()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-055 태그 축이 없음이 아니면 미적용 안내를 표시하지 않는다`() {
        val tag = tag(title = FIRST_TAG_TITLE)

        setBottomSheetContent(
            tagList = listOf(tag),
            uiState = MemoHomeFilterUiState(existence = MemoExistenceFilter(tag = MemoFilterExistence.EXIST)),
        )

        composeRule.onNodeWithText(TAG_INACTIVE_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsEnabled()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-032 유무 필터의 세 축에 현재 상태를 표시한다`() {
        setBottomSheetContent(
            uiState =
                MemoHomeFilterUiState(
                    existence =
                        MemoExistenceFilter(
                            date = MemoFilterExistence.EXIST,
                            tag = MemoFilterExistence.NOT_EXIST,
                            place = MemoFilterExistence.ALL,
                        ),
                ),
        )

        composeRule.onNodeWithContentDescription("$DATE_LABEL $EXIST_LABEL").assertIsSelected()
        composeRule.onNodeWithContentDescription("$DATE_LABEL $NOT_EXIST_LABEL").assertIsNotSelected()
        composeRule.onNodeWithContentDescription("$DATE_LABEL $ALL_LABEL").assertIsNotSelected()

        composeRule.onNodeWithContentDescription("$TAG_LABEL $NOT_EXIST_LABEL").assertIsSelected()
        composeRule.onNodeWithContentDescription("$TAG_LABEL $EXIST_LABEL").assertIsNotSelected()

        composeRule.onNodeWithContentDescription("$PLACE_LABEL $ALL_LABEL").assertIsSelected()
        composeRule.onNodeWithContentDescription("$PLACE_LABEL $EXIST_LABEL").assertIsNotSelected()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-033 유무 필터를 고르지 않으면 세 축이 모두 전체로 표시된다`() {
        setBottomSheetContent()

        listOf(DATE_LABEL, TAG_LABEL, PLACE_LABEL).forEach { label ->
            composeRule.onNodeWithContentDescription("$label $ALL_LABEL").assertIsSelected()
            composeRule.onNodeWithContentDescription("$label $EXIST_LABEL").assertIsNotSelected()
            composeRule.onNodeWithContentDescription("$label $NOT_EXIST_LABEL").assertIsNotSelected()
        }
    }

    @Test
    fun `축의 있음을 누르면 그 축의 있음 Event를 전달한다`() {
        val eventList = mutableListOf<MemoHomeFilterBottomSheetEvent>()
        setBottomSheetContent(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription("$DATE_LABEL $EXIST_LABEL").performClick()
        composeRule.onNodeWithContentDescription("$TAG_LABEL $EXIST_LABEL").performClick()
        composeRule.onNodeWithContentDescription("$PLACE_LABEL $EXIST_LABEL").performClick()

        eventList shouldBe
            listOf(
                MemoHomeFilterBottomSheetEvent.SetDateExistence(existence = MemoFilterExistence.EXIST),
                MemoHomeFilterBottomSheetEvent.SetTagExistence(existence = MemoFilterExistence.EXIST),
                MemoHomeFilterBottomSheetEvent.SetPlaceExistence(existence = MemoFilterExistence.EXIST),
            )
    }

    @Test
    fun `축의 없음과 전체를 누르면 그 상태의 Event를 전달한다`() {
        val eventList = mutableListOf<MemoHomeFilterBottomSheetEvent>()
        setBottomSheetContent(
            uiState = MemoHomeFilterUiState(existence = MemoExistenceFilter(date = MemoFilterExistence.EXIST)),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithContentDescription("$DATE_LABEL $NOT_EXIST_LABEL").performClick()
        composeRule.onNodeWithContentDescription("$DATE_LABEL $ALL_LABEL").performClick()

        eventList shouldBe
            listOf(
                MemoHomeFilterBottomSheetEvent.SetDateExistence(existence = MemoFilterExistence.NOT_EXIST),
                MemoHomeFilterBottomSheetEvent.SetDateExistence(existence = MemoFilterExistence.ALL),
            )
    }

    private fun setBottomSheetContent(
        tagList: List<Tag> = emptyList(),
        uiState: MemoHomeFilterUiState = MemoHomeFilterUiState(),
        onEvent: (MemoHomeFilterBottomSheetEvent) -> Unit = {},
        onTagFilterEvent: (TagFilterEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                MemoHomeFilterBottomSheetContent(
                    tagPagingItems = MutableStateFlow(tagPagingDataOf(tagList)).collectAsLazyPagingItems(),
                    uiStateProvider = { uiState },
                    onEvent = onEvent,
                    onTagFilterEvent = onTagFilterEvent,
                )
            }
        }
    }

    companion object {
        private const val FIRST_TAG_TITLE = "MemoFilterAlpha"
        private const val SECOND_TAG_TITLE = "MemoFilterBravo"
        private const val TAG_EMOJI = "🏃"
        private const val DATE_LABEL = "Date"
        private const val TAG_LABEL = "Tag"
        private const val PLACE_LABEL = "Place"
        private const val EXIST_LABEL = "With"
        private const val NOT_EXIST_LABEL = "Without"
        private const val ALL_LABEL = "All"
        private const val UNSELECT_ALL_CONTENT_DESCRIPTION = "Clear tag filter"
        private const val TAG_INACTIVE_DESCRIPTION = "Tag filter is not applied while the tag axis is Without."

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
