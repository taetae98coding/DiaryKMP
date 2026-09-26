package io.github.taetae98coding.diary.feature.calendar.ui.home.filter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.filter.TagFilterEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.calendar.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeFilterBottomSheetContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

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
    fun `TC-CALENDAR-HOME-FEATURE-049 선택되지 않은 태그를 누르면 선택 Event를 전달한다`() {
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
    fun `TC-CALENDAR-HOME-FEATURE-051 선택된 태그를 누르면 선택 해제 Event를 전달한다`() {
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
    fun `TC-CALENDAR-HOME-FEATURE-083 TC-CALENDAR-HOME-DOMAIN-019 보이는 선택한 태그가 없으면 지우기를 실행할 수 없다`() {
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

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-084 태그가 있으면 태그 칩 뒤에 태그 추가 칩을 표시한다`() {
        setBottomSheetContent(tagList = listOf(tag(title = FIRST_TAG_TITLE)))

        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-084 태그 없이 조회가 끝나도 태그 추가 칩을 표시한다`() {
        setBottomSheetContent(tagPagingData = tagPagingDataOf(emptyList()))

        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-084 태그 조회가 진행 중이어도 태그 추가 칩을 표시한다`() {
        setBottomSheetContent(tagPagingData = refreshingTagPagingData())

        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-084 태그 조회에 실패해도 태그 추가 칩을 표시한다`() {
        setBottomSheetContent(tagPagingData = failedTagPagingData())

        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-085 태그 추가 칩을 누르면 태그 추가 Event만 전달한다`() {
        val tag = tag(title = FIRST_TAG_TITLE)
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(
            tagList = listOf(tag),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithText(ADD_LABEL).performClick()

        eventList shouldBe listOf(TagFilterEvent.ClickAdd)
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-098 태그를 조회하지 못하면 오류 안내 없이 태그 추가 칩만 표시한다`() {
        val tag = tag(title = FIRST_TAG_TITLE)
        val tagPagingFlow = MutableStateFlow(tagPagingDataOf(listOf(tag)))
        val failedTagPagingFlow = MutableStateFlow(failedTagPagingData())
        var isFailed by mutableStateOf(false)
        setSwitchingContent { if (isFailed) failedTagPagingFlow else tagPagingFlow }
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertExists()
        val textListWithTag = displayedTextList()

        isFailed = true
        composeRule.waitUntil(timeoutMillis = TAG_UPDATE_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(FIRST_TAG_TITLE).fetchSemanticsNodes().isEmpty()
        }

        displayedTextList() shouldBe textListWithTag - FIRST_TAG_TITLE
        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-099 태그의 이모지, 제목이나 컬러가 바뀌면 필터에 반영하고 선택 여부를 유지한다`() {
        val selectedTag = tag(title = FIRST_TAG_TITLE)
        val unselectedTag = tag(title = SECOND_TAG_TITLE)
        val tagPagingFlow = MutableStateFlow(tagPagingDataOf(listOf(selectedTag, unselectedTag)))
        val changedTagPagingFlow =
            MutableStateFlow(
                tagPagingDataOf(
                    listOf(
                        selectedTag.changed(title = CHANGED_FIRST_TAG_TITLE),
                        unselectedTag.changed(title = CHANGED_SECOND_TAG_TITLE),
                    ),
                ),
            )
        var isChanged by mutableStateOf(false)
        setSwitchingContent(selectedTagIdSet = setOf(selectedTag.id)) { if (isChanged) changedTagPagingFlow else tagPagingFlow }
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsSelected()

        isChanged = true
        composeRule.waitUntil(timeoutMillis = TAG_UPDATE_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText("$TAG_EMOJI $CHANGED_FIRST_TAG_TITLE").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("$TAG_EMOJI $CHANGED_FIRST_TAG_TITLE").assertIsSelected()
        composeRule.onNodeWithText("$TAG_EMOJI $CHANGED_SECOND_TAG_TITLE").assertIsNotSelected()
        composeRule.onAllNodesWithText(FIRST_TAG_TITLE).assertCountEquals(0)
        composeRule.onAllNodesWithText(SECOND_TAG_TITLE).assertCountEquals(0)
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-100 필터를 연 채로 태그가 추가되면 필터에 바로 나타난다`() {
        val selectedTag = tag(title = FIRST_TAG_TITLE)
        val addedTag = tag(title = SECOND_TAG_TITLE)
        val tagPagingFlow = MutableStateFlow(tagPagingDataOf(listOf(selectedTag)))
        val addedTagPagingFlow = MutableStateFlow(tagPagingDataOf(listOf(selectedTag, addedTag)))
        var isAdded by mutableStateOf(false)
        setSwitchingContent(selectedTagIdSet = setOf(selectedTag.id)) { if (isAdded) addedTagPagingFlow else tagPagingFlow }
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsSelected()

        isAdded = true
        composeRule.waitUntil(timeoutMillis = TAG_UPDATE_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(SECOND_TAG_TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertIsNotSelected()
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsSelected()
    }

    // 필터를 연 채로 조회 결과가 바뀌는 것을 재현하려고, 화면에 넘기는 태그 흐름을 테스트가 바꾼다.
    private fun setSwitchingContent(
        selectedTagIdSet: Set<Uuid> = emptySet(),
        tagPagingFlowProvider: () -> MutableStateFlow<PagingData<Tag>>,
    ) {
        composeRule.setContent {
            DiaryTheme {
                CalendarHomeFilterBottomSheetContent(
                    tagPagingItems = tagPagingFlowProvider().collectAsLazyPagingItems(),
                    uiStateProvider = { CalendarHomeFilterUiState(selectedTagIdSet = selectedTagIdSet) },
                    onEvent = {},
                )
            }
        }
    }

    private fun displayedTextList(): List<String> =
        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .flatMap { node -> node.config[SemanticsProperties.Text].map { text -> text.text } }

    private fun setBottomSheetContent(
        tagList: List<Tag> = emptyList(),
        uiState: CalendarHomeFilterUiState = CalendarHomeFilterUiState(),
        tagPagingData: PagingData<Tag> = tagPagingDataOf(tagList),
        onEvent: (TagFilterEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                CalendarHomeFilterBottomSheetContent(
                    tagPagingItems = MutableStateFlow(tagPagingData).collectAsLazyPagingItems(),
                    uiStateProvider = { uiState },
                    onEvent = onEvent,
                )
            }
        }
    }

    companion object {
        private const val FIRST_TAG_TITLE = "CalendarFilterAlpha"
        private const val CHANGED_FIRST_TAG_TITLE = "ChangedFilterAlpha"
        private const val CHANGED_SECOND_TAG_TITLE = "ChangedFilterBravo"
        private const val SECOND_TAG_TITLE = "CalendarFilterBravo"
        private const val TAG_EMOJI = "🏃"
        private const val UNSELECT_ALL_CONTENT_DESCRIPTION = "Clear tag filter"
        private const val ADD_LABEL = "Add tag"
        private const val TAG_UPDATE_TIMEOUT_MILLIS = 5_000L

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
                .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()

        private fun Tag.changed(title: String): Tag = copy(detail = detail.copy(emoji = TAG_EMOJI, title = title, color = detail.color.inv()))
    }
}
