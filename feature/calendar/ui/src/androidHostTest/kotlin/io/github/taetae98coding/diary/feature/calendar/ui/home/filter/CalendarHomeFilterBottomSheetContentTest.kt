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
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.testing.asPagingSourceFactory
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.filter.TagFilterEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.tag.tagDetail
import io.github.taetae98coding.diary.feature.calendar.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
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
    fun `TC-6f17aa62-bea3-43a3-8326-f74db698660a 태그 필터에 전체 태그와 선택 여부를 표시한다`() {
        val tagList = listOf(tag(title = FIRST_TAG_TITLE), tag(title = SECOND_TAG_TITLE))

        setBottomSheetContent(
            tagList = tagList,
            uiState = CalendarHomeFilterUiState(selectedTagIdSet = setOf(tagList.first().id)),
        )

        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsSelected()
        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertIsNotSelected()
    }

    @Test
    fun `TC-cb8879fd-e66b-4a71-b669-63389d14c135 필터 칩에 이모지와 제목을 함께 표시한다`() {
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
    fun `TC-a2875f01-2a62-49e3-9c26-8efdca098261 선택되지 않은 태그를 누르면 선택 Event를 전달한다`() {
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
    fun `TC-c23ef9af-019a-4817-a559-2e8ed41cfc09 선택된 태그를 누르면 선택 해제 Event를 전달한다`() {
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
    fun `TC-efd8e7dc-b217-44f8-b3d8-64d62806ef15 지우기를 누르면 선택 전체 해제 Event를 전달한다`() {
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
    fun `TC-e637f434-20f6-4733-b310-e193c60c7163 TC-8c9b3391-704b-4fe7-aef3-92547492cfbf 보이는 선택한 태그가 없으면 지우기를 실행할 수 없다`() {
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
    fun `TC-8749ddd0-f38d-465b-8fa0-d60938c99cb2 태그가 있으면 태그 칩 뒤에 태그 추가 칩을 표시한다`() {
        setBottomSheetContent(tagList = listOf(tag(title = FIRST_TAG_TITLE)))

        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-8749ddd0-f38d-465b-8fa0-d60938c99cb2 태그 없이 조회가 끝나도 태그 추가 칩을 표시한다`() {
        val tagPagingFlow = MutableStateFlow(tagPagingDataOf(listOf(tag(title = FIRST_TAG_TITLE))))
        val emptyTagPagingFlow = MutableStateFlow(tagPagingDataOf(emptyList()))
        var isEmpty by mutableStateOf(false)
        setSwitchingContent { if (isEmpty) emptyTagPagingFlow else tagPagingFlow }
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertExists()
        val textListWithTag = displayedTextList()

        isEmpty = true
        composeRule.waitUntil(timeoutMillis = TAG_UPDATE_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(FIRST_TAG_TITLE).fetchSemanticsNodes().isEmpty()
        }

        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
        displayedTextList() shouldBe textListWithTag - FIRST_TAG_TITLE
    }

    @Test
    fun `TC-8749ddd0-f38d-465b-8fa0-d60938c99cb2 태그 조회가 진행 중이어도 태그 추가 칩을 표시한다`() {
        setBottomSheetContent(tagPagingData = refreshingTagPagingData())

        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-8749ddd0-f38d-465b-8fa0-d60938c99cb2 태그 조회에 실패해도 태그 추가 칩을 표시한다`() {
        setBottomSheetContent(tagPagingData = failedTagPagingData())

        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-5e48b2cd-da3e-49eb-b2eb-4add737877af 태그 추가 칩을 누르면 태그 추가 Event만 전달한다`() {
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
    fun `TC-ec235ab8-30cc-4fd6-b94c-32cdbbc3217f 태그를 조회하지 못하면 오류 안내 없이 태그 추가 칩만 표시한다`() {
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
    fun `TC-10be1733-fa94-481d-8aea-c7b6f7e4ea52 태그의 이모지, 제목이나 컬러가 바뀌면 필터에 반영하고 선택 여부를 유지한다`() {
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
    fun `TC-09613f11-35d8-4373-a624-1fd11cb13684 필터를 연 채로 태그가 추가되면 필터에 바로 나타난다`() {
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

    // 선택할 수 있는 태그를 한 번에 TAG_PAGE_SIZE씩 나누어 불러오도록 실제 페이지 조회로 구성한다.
    @Test
    fun `TC-646d0ca9-49b0-4009-bc06-ed0f98bf20ee 태그 필터를 열면 이동 없이 선택할 수 있는 태그 전체가 제목 오름차순으로 채워진다`() {
        val tagList = List(PAGED_TAG_COUNT) { index -> tag(title = "$PAGED_TAG_TITLE_PREFIX${index.toString().padStart(length = 3, padChar = '0')}") }
        val tagPagingFlow =
            Pager(
                config = PagingConfig(pageSize = TAG_PAGE_SIZE, initialLoadSize = TAG_PAGE_SIZE, prefetchDistance = 1),
                pagingSourceFactory = tagList.asPagingSourceFactory(),
            ).flow

        composeRule.setContent {
            DiaryTheme {
                CalendarHomeFilterBottomSheetContent(
                    tagPagingItems = tagPagingFlow.collectAsLazyPagingItems(),
                    uiStateProvider = { CalendarHomeFilterUiState() },
                    onEvent = {},
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = TAG_UPDATE_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(tagList.last().detail.title).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule
            .onAllNodesWithText(PAGED_TAG_TITLE_PREFIX, substring = true)
            .fetchSemanticsNodes()
            .map { node -> node.config[SemanticsProperties.Text].joinToString(separator = "") { text -> text.text } } shouldContainExactly tagList.map { tag -> tag.detail.title }.sorted()
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
        private const val TAG_PAGE_SIZE = 20
        private const val PAGED_TAG_COUNT = TAG_PAGE_SIZE * 3
        private const val PAGED_TAG_TITLE_PREFIX = "PagedFilterTag"

        private fun tag(
            title: String,
            emoji: String = "",
        ): Tag =
            Arb
                .tag()
                .next()
                .copy(
                    detail = Arb.tagDetail().next().copy(emoji = emoji, title = title),
                    isFinished = false,
                    isDeleted = false,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

        private fun Tag.changed(title: String): Tag = copy(detail = detail.copy(emoji = TAG_EMOJI, title = title, color = detail.color.inv()))
    }
}
