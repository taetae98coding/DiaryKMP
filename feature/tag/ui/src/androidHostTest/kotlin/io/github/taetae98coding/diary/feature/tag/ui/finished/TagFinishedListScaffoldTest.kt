package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.TAG_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.tag.list.TagListEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.list.tagPagingDataOf
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
class TagFinishedListScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-002 목록에 완료된 태그의 이모지와 제목을 카드로 표시한다`() {
        val tagList =
            listOf(
                tag(title = FIRST_TITLE, emoji = EMOJI),
                tag(title = SECOND_TITLE, emoji = EMOJI),
            )

        setTagFinishedListScaffold(tagList = tagList)

        composeRule.onNodeWithText("$EMOJI $FIRST_TITLE").assertExists()
        composeRule.onNodeWithText("$EMOJI $SECOND_TITLE").assertExists()
        composeRule.onAllNodesWithTag(TAG_CARD_TEST_TAG).assertCountEquals(2)
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-003 이모지가 비어 있으면 제목만 표시한다`() {
        setTagFinishedListScaffold(tagList = listOf(tag(title = FIRST_TITLE, emoji = "")))

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
    }

    @Test
    fun `조회 결과가 비어 있으면 태그 카드를 표시하지 않는다`() {
        setTagFinishedListScaffold()

        composeRule.onAllNodesWithTag(TAG_CARD_TEST_TAG).assertCountEquals(0)
    }

    @Test
    fun `기본 환경에서 상단 바 제목은 Finished Tags이다`() {
        setTagFinishedListScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 상단 바 제목은 완료된 태그이다`() {
        setTagFinishedListScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-013 이 화면에는 태그 추가 버튼을 표시하지 않는다`() {
        setTagFinishedListScaffold(tagList = listOf(tag(title = FIRST_TITLE)))

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-011 태그 카드를 선택하면 해당 태그의 상세 화면 전환 행동을 한 번 전달한다`() {
        val tag = tag(title = FIRST_TITLE)
        val eventList = mutableListOf<TagFinishedListScaffoldEvent>()
        val tagListEventList = mutableListOf<TagListEvent>()
        setTagFinishedListScaffold(tagList = listOf(tag), onEvent = eventList::add, onTagListEvent = tagListEventList::add)

        composeRule.onAllNodesWithTag(TAG_CARD_TEST_TAG).onFirst().performClick()

        eventList.shouldBeEmpty()
        tagListEventList shouldBe listOf(TagListEvent.ClickTag(tag.id))
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-012 뒤로가기 버튼을 선택하면 뒤로가기 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<TagFinishedListScaffoldEvent>()
        setTagFinishedListScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        eventList shouldBe listOf(TagFinishedListScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-021 격자의 태그 카드를 좌에서 우로 밀면 그 태그의 다시 시작을 한 번 전달한다`() {
        val tag = tag(title = FIRST_TITLE)
        val tagListEventList = mutableListOf<TagListEvent>()
        setTagFinishedListScaffold(tagList = listOf(tag), onTagListEvent = tagListEventList::add)

        composeRule.onNodeWithText(FIRST_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        tagListEventList shouldBe listOf(TagListEvent.SwipeRestart(id = tag.id))
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-022 격자의 태그 카드를 우에서 좌로 밀면 그 태그의 삭제를 한 번 전달한다`() {
        val tag = tag(title = FIRST_TITLE)
        val tagListEventList = mutableListOf<TagListEvent>()
        setTagFinishedListScaffold(tagList = listOf(tag), onTagListEvent = tagListEventList::add)

        composeRule.onNodeWithText(FIRST_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        tagListEventList shouldBe listOf(TagListEvent.SwipeDelete(id = tag.id))
    }

    private fun setTagFinishedListScaffold(
        tagList: List<Tag> = emptyList(),
        uiState: TagFinishedListUiState = TagFinishedListUiState(),
        onEvent: (TagFinishedListScaffoldEvent) -> Unit = {},
        onTagListEvent: (TagListEvent) -> Unit = {},
    ) {
        val tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(tagList))

        composeRule.setContent {
            DiaryTheme {
                TagFinishedListScaffold(
                    onTagListEvent = onTagListEvent,
                    tagPagingItems = tagPagingDataFlow.collectAsLazyPagingItems(),
                    onEvent = onEvent,
                    uiStateProvider = { uiState },
                )
            }
        }
    }

    public companion object {
        private const val FIRST_TITLE = "FirstFinishedTagTitle"
        private const val SECOND_TITLE = "SecondFinishedTagTitle"
        private const val EMOJI = "📌"
        private const val DEFAULT_TITLE = "Finished Tags"
        private const val KOREAN_TITLE = "완료된 태그"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add tag"
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(
            title: String,
            emoji: String = "",
        ): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = emoji, title = title))
                .setExp(Tag::isFinished, true)
                .setExp(Tag::isDeleted, false)
                .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
