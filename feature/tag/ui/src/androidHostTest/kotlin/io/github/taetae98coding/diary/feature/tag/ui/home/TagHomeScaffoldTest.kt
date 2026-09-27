package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
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
import com.navercorp.fixturemonkey.kotlin.giveMe
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
class TagHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-HOME-FEATURE-001 목록에 태그 제목을 카드로 표시한다`() {
        val tagList = listOf(tag(title = FIRST_TITLE), tag(title = SECOND_TITLE))

        setTagHomeScaffold(tagList = tagList)

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_TITLE).assertExists()
        composeRule.onAllNodesWithTag(TAG_CARD_TEST_TAG).assertCountEquals(2)
    }

    @Test
    @Config(qualifiers = "w1000dp-h1000dp")
    fun `넓은 화면에서도 태그 카드는 두 열로 배치된다`() {
        val tagList =
            fixtureMonkey
                .giveMe<String>(GRID_TAG_COUNT)
                .mapIndexed { index, title -> tag(title = "$title-$index") }

        setTagHomeScaffold(tagList = tagList)

        val cardBounds =
            composeRule
                .onAllNodesWithTag(TAG_CARD_TEST_TAG)
                .fetchSemanticsNodes()
                .map { node -> node.boundsInRoot }
        cardBounds.size shouldBe GRID_TAG_COUNT
        cardBounds[0].top shouldBe cardBounds[1].top
        cardBounds[0].left shouldBe cardBounds[2].left
        (cardBounds[1].left > cardBounds[0].left) shouldBe true
        (cardBounds[2].top > cardBounds[0].top) shouldBe true
    }

    @Test
    fun `조회 결과가 비어 있으면 태그 카드를 표시하지 않는다`() {
        setTagHomeScaffold()

        composeRule.onAllNodesWithTag(TAG_CARD_TEST_TAG).assertCountEquals(0)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 태그 추가 버튼 이름은 태그 추가이다`() {
        setTagHomeScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `기본 환경에서 태그 추가 버튼 이름은 Add tag이다`() {
        setTagHomeScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `태그 추가 버튼을 선택하면 화면 전환 행동을 한 번 전달한다`() {
        val eventList = mutableListOf<TagHomeScaffoldEvent>()
        setTagHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        eventList shouldBe listOf(TagHomeScaffoldEvent.ClickAdd)
    }

    @Test
    fun `태그 카드를 선택하면 해당 태그의 상세 화면 전환 행동을 한 번 전달한다`() {
        val tag = tag(title = FIRST_TITLE)
        val eventList = mutableListOf<TagHomeScaffoldEvent>()
        val tagListEventList = mutableListOf<TagListEvent>()
        setTagHomeScaffold(tagList = listOf(tag), onEvent = eventList::add, onTagListEvent = tagListEventList::add)

        composeRule.onAllNodesWithTag(TAG_CARD_TEST_TAG).onFirst().performClick()

        eventList.shouldBeEmpty()
        tagListEventList shouldBe listOf(TagListEvent.ClickTag(tag.id))
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-013 TC-TAG-HOME-FEATURE-014 완료된 태그 버튼을 누르면 완료 목록 열기 행동을 전달한다`() {
        val eventList = mutableListOf<TagHomeScaffoldEvent>()
        setTagHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_BUTTON_LABEL).performClick()

        eventList shouldBe listOf(TagHomeScaffoldEvent.ClickFinishedList)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-HOME-FEATURE-013 한국어 환경에서 완료된 태그 버튼 이름은 완료된 태그이다`() {
        setTagHomeScaffold()

        composeRule.onNodeWithText(KOREAN_FINISHED_LIST_BUTTON_LABEL).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-039 격자의 태그 카드를 좌에서 우로 밀면 그 태그의 완료를 한 번 전달한다`() {
        val tag = tag(title = FIRST_TITLE)
        val tagListEventList = mutableListOf<TagListEvent>()
        setTagHomeScaffold(tagList = listOf(tag), onTagListEvent = tagListEventList::add)

        composeRule.onNodeWithText(FIRST_TITLE).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        tagListEventList shouldBe listOf(TagListEvent.SwipeFinish(id = tag.id))
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-040 격자의 태그 카드를 우에서 좌로 밀면 그 태그의 삭제를 한 번 전달한다`() {
        val tag = tag(title = FIRST_TITLE)
        val tagListEventList = mutableListOf<TagListEvent>()
        setTagHomeScaffold(tagList = listOf(tag), onTagListEvent = tagListEventList::add)

        composeRule.onNodeWithText(FIRST_TITLE).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        tagListEventList shouldBe listOf(TagListEvent.SwipeDelete(id = tag.id))
    }

    @Test
    fun `상세 영역에 태그 추가 화면이 표시되면 태그 추가 버튼이 표시되지 않는다`() {
        setTagHomeScaffold(componentVisibleProvider = { TagHomeScaffoldComponentVisible(isAddButtonVisible = false) })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `추가 버튼 표시 상태이면 태그 추가 버튼이 표시된다`() {
        setTagHomeScaffold(componentVisibleProvider = { TagHomeScaffoldComponentVisible(isAddButtonVisible = true) })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    private fun setTagHomeScaffold(
        tagList: List<Tag> = emptyList(),
        onEvent: (TagHomeScaffoldEvent) -> Unit = {},
        onTagListEvent: (TagListEvent) -> Unit = {},
        componentVisibleProvider: () -> TagHomeScaffoldComponentVisible = { TagHomeScaffoldComponentVisible() },
    ) {
        val tagPagingDataFlow = MutableStateFlow(tagPagingDataOf(tagList))

        composeRule.setContent {
            DiaryTheme {
                TagHomeScaffold(
                    onTagListEvent = onTagListEvent,
                    tagPagingItems = tagPagingDataFlow.collectAsLazyPagingItems(),
                    onEvent = onEvent,
                    componentVisibleProvider = componentVisibleProvider,
                )
            }
        }
    }

    public companion object {
        private const val KOREAN_ADD_BUTTON_DESCRIPTION = "태그 추가"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add tag"
        private const val DEFAULT_FINISHED_LIST_BUTTON_LABEL = "Finished tags"
        private const val KOREAN_FINISHED_LIST_BUTTON_LABEL = "완료된 태그"
        private const val FIRST_TITLE = "FirstTagTitle"
        private const val SECOND_TITLE = "SecondTagTitle"
        private const val GRID_TAG_COUNT = 3
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(
            title: String,
            emoji: String = "",
        ): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = emoji, title = title))
                .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
