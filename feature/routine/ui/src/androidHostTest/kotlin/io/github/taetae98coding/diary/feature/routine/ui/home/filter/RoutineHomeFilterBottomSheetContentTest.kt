package io.github.taetae98coding.diary.feature.routine.ui.home.filter

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
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.testing.asPagingSourceFactory
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.filter.TagFilterEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.tag.tagDetail
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoutineHomeFilterBottomSheetContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-ee15484d-6677-4b8f-9366-5005f128ad47 필터를 열면 선택할 수 있는 태그와 선택 여부가 표시된다`() {
        val tagList = listOf(tag(title = FIRST_TAG_TITLE), tag(title = SECOND_TAG_TITLE))

        setBottomSheetContent(tagList = tagList, selectedTagIdSet = setOf(tagList.first().id))

        composeRule.onNodeWithText(FILTER_TITLE).assertExists()
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsSelected()
        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertIsNotSelected()
    }

    @Test
    fun `TC-4820510e-4b81-4843-8a5c-5d16899442e3 태그를 누르면 확정 없이 선택 Event를 전달하고 선택이 반영되면 선택된 것으로 표시된다`() {
        val firstTag = tag(title = FIRST_TAG_TITLE)
        val secondTag = tag(title = SECOND_TAG_TITLE)
        var selectedTagIdSet by mutableStateOf(emptySet<Uuid>())
        val eventList = mutableListOf<TagFilterEvent>()
        composeRule.setContent {
            DiaryTheme {
                RoutineHomeFilterBottomSheetContent(
                    onEvent = {},
                    onTagFilterEvent = { event ->
                        eventList.add(event)
                        if (event is TagFilterEvent.Select) selectedTagIdSet = selectedTagIdSet + event.id
                    },
                    tagPagingItems = MutableStateFlow(tagPagingDataOf(listOf(firstTag, secondTag))).collectAsLazyPagingItems(),
                    uiStateProvider = { RoutineHomeFilterUiState(selectedTagIdSet = selectedTagIdSet) },
                )
            }
        }

        composeRule.onNodeWithText(FIRST_TAG_TITLE).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagFilterEvent.Select(id = firstTag.id))
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsSelected()
        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertIsNotSelected()
        composeRule.onNodeWithText(FILTER_TITLE).assertExists()
    }

    @Test
    fun `선택된 태그를 누르면 선택 해제 Event를 전달한다`() {
        val tag = tag(title = FIRST_TAG_TITLE)
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(tagList = listOf(tag), selectedTagIdSet = setOf(tag.id), onEvent = eventList::add)

        composeRule.onNodeWithText(FIRST_TAG_TITLE).performClick()

        eventList shouldBe listOf(TagFilterEvent.Unselect(id = tag.id))
    }

    @Test
    fun `TC-a60aafc3-af10-487a-8a50-21a7a7339617 태그 필터에 이모지와 제목을 함께 표시한다`() {
        val tagList = listOf(tag(title = FIRST_TAG_TITLE, emoji = TAG_EMOJI), tag(title = SECOND_TAG_TITLE))

        setBottomSheetContent(tagList = tagList)

        composeRule.onNodeWithText("$TAG_EMOJI $FIRST_TAG_TITLE").assertExists()
        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(" $SECOND_TAG_TITLE").assertDoesNotExist()
    }

    // 선택할 수 있는 태그를 한 번에 TAG_PAGE_SIZE씩 나누어 불러오도록 실제 페이지 조회로 구성한다.
    @Test
    fun `TC-c1bfcba4-e940-4456-80a1-34433eacf6b6 필터를 열면 이동 없이 선택할 수 있는 태그 전체가 제목 오름차순으로 채워진다`() {
        val tagList = List(PAGED_TAG_COUNT) { index -> tag(title = "$PAGED_TAG_TITLE_PREFIX${index.toString().padStart(length = 3, padChar = '0')}") }
        val tagPagingFlow =
            Pager(
                config = PagingConfig(pageSize = TAG_PAGE_SIZE, initialLoadSize = TAG_PAGE_SIZE, prefetchDistance = 1),
                pagingSourceFactory = tagList.asPagingSourceFactory(),
            ).flow

        composeRule.setContent {
            DiaryTheme {
                RoutineHomeFilterBottomSheetContent(
                    onEvent = {},
                    onTagFilterEvent = {},
                    tagPagingItems = tagPagingFlow.collectAsLazyPagingItems(),
                    uiStateProvider = { RoutineHomeFilterUiState() },
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(tagList.last().detail.title).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule
            .onAllNodesWithText(PAGED_TAG_TITLE_PREFIX, substring = true)
            .fetchSemanticsNodes()
            .map { node -> node.config[SemanticsProperties.Text].joinToString(separator = "") { text -> text.text } } shouldContainExactly tagList.map { tag -> tag.detail.title }.sorted()
    }

    @Test
    fun `TC-34ed066c-fe48-4e77-8971-b661ee740d0d 선택 전체 해제를 누르면 확인 없이 전체 해제 Event를 전달하고 필터는 열린 채로 남는다`() {
        val firstTag = tag(title = FIRST_TAG_TITLE)
        val secondTag = tag(title = SECOND_TAG_TITLE)
        var selectedTagIdSet by mutableStateOf(setOf(firstTag.id, secondTag.id))
        val eventList = mutableListOf<TagFilterEvent>()
        composeRule.setContent {
            DiaryTheme {
                RoutineHomeFilterBottomSheetContent(
                    onEvent = {},
                    onTagFilterEvent = { event ->
                        eventList.add(event)
                        if (event is TagFilterEvent.UnselectAll) selectedTagIdSet = emptySet()
                    },
                    tagPagingItems = MutableStateFlow(tagPagingDataOf(listOf(firstTag, secondTag))).collectAsLazyPagingItems(),
                    uiStateProvider = { RoutineHomeFilterUiState(selectedTagIdSet = selectedTagIdSet) },
                )
            }
        }

        composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagFilterEvent.UnselectAll)
        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsNotSelected()
        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertIsNotSelected()
        composeRule.onNodeWithText(FILTER_TITLE).assertExists()
    }

    @Test
    fun `TC-352ec4e8-7287-4cc1-904a-107c56600ac8 선택한 태그가 없으면 선택 전체 해제를 실행할 수 없다`() {
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(tagList = listOf(tag(title = FIRST_TAG_TITLE)), onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).assertIsNotEnabled()
        composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).performClick()

        eventList.shouldBeEmpty()
    }

    // 무시되고 있는 선택은 선택할 수 있는 태그로 조회되지 않아 보이는 선택이 없다.
    @Test
    fun `TC-71ba9e96-d9ad-428e-8dc3-5c9f3c13411f 무시되고 있는 선택만 남으면 선택 전체 해제를 실행할 수 없다`() {
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(tagList = listOf(tag(title = SECOND_TAG_TITLE)), selectedTagIdSet = emptySet(), onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).assertIsNotEnabled()
        composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).performClick()

        eventList.shouldBeEmpty()
    }

    @Test
    fun `TC-57ca4493-f020-4301-9187-9b77d574c461 선택할 수 있는 태그가 있으면 태그 추가를 실행할 수 있다`() {
        assertTagAddAvailable(tagPagingData = tagPagingDataOf(listOf(tag(title = FIRST_TAG_TITLE))))
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-57ca4493-f020-4301-9187-9b77d574c461 선택할 수 있는 태그 없이 조회가 끝나도 태그 추가를 실행할 수 있다`() {
        assertTagAddAvailable(tagPagingData = tagPagingDataOf(emptyList()))
    }

    @Test
    fun `TC-57ca4493-f020-4301-9187-9b77d574c461 태그 조회가 진행 중이어도 태그 추가를 실행할 수 있다`() {
        assertTagAddAvailable(tagPagingData = tagPagingDataWithRefresh(LoadState.Loading))
    }

    @Test
    fun `TC-57ca4493-f020-4301-9187-9b77d574c461 태그 조회에 실패해도 태그 추가를 실행할 수 있다`() {
        assertTagAddAvailable(tagPagingData = tagPagingDataWithRefresh(LoadState.Error(IllegalStateException(Arb.string().next()))))
    }

    @Test
    fun `태그 추가를 누르면 태그 선택을 바꾸지 않고 태그 추가 Event만 전달한다`() {
        val selectedTag = tag(title = FIRST_TAG_TITLE)
        val unselectedTag = tag(title = SECOND_TAG_TITLE)
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(tagList = listOf(selectedTag, unselectedTag), selectedTagIdSet = setOf(selectedTag.id), onEvent = eventList::add)

        composeRule.onNodeWithText(ADD_LABEL).performClick()

        eventList shouldBe listOf(TagFilterEvent.ClickAdd)
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsSelected()
        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertIsNotSelected()
    }

    @Test
    fun `TC-cb0ac90d-6220-4b92-8875-4b902016db1c 태그를 조회하지 못하면 오류 안내 없이 태그 추가만 표시한다`() {
        val tag = tag(title = FIRST_TAG_TITLE)
        val tagPagingFlow = MutableStateFlow(tagPagingDataOf(listOf(tag)))
        val failedTagPagingFlow = MutableStateFlow(tagPagingDataWithRefresh(LoadState.Error(IllegalStateException(Arb.string().next()))))
        var isFailed by mutableStateOf(false)
        setSwitchingContent { if (isFailed) failedTagPagingFlow else tagPagingFlow }
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertExists()
        val textListWithTag = displayedTextList()

        isFailed = true
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(FIRST_TAG_TITLE).fetchSemanticsNodes().isEmpty()
        }

        displayedTextList() shouldBe textListWithTag - FIRST_TAG_TITLE
        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
        composeRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun `TC-b83f232b-0a9a-4d7c-95f3-d7c7b82eb95e 태그의 이모지, 제목이나 컬러가 바뀌면 필터에 반영하고 선택 여부를 유지한다`() {
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
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText("$TAG_EMOJI $CHANGED_FIRST_TAG_TITLE").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("$TAG_EMOJI $CHANGED_FIRST_TAG_TITLE").assertIsSelected()
        composeRule.onNodeWithText("$TAG_EMOJI $CHANGED_SECOND_TAG_TITLE").assertIsNotSelected()
        composeRule.onAllNodesWithText(FIRST_TAG_TITLE).assertCountEquals(0)
        composeRule.onAllNodesWithText(SECOND_TAG_TITLE).assertCountEquals(0)
    }

    @Test
    fun `TC-9b191b04-3c12-4e33-a7fd-0116065066dd 필터를 연 채로 태그가 추가되면 필터에 바로 나타난다`() {
        val selectedTag = tag(title = FIRST_TAG_TITLE)
        val addedTag = tag(title = SECOND_TAG_TITLE)
        val tagPagingFlow = MutableStateFlow(tagPagingDataOf(listOf(selectedTag)))
        val addedTagPagingFlow = MutableStateFlow(tagPagingDataOf(listOf(selectedTag, addedTag)))
        var isAdded by mutableStateOf(false)
        setSwitchingContent(selectedTagIdSet = setOf(selectedTag.id)) { if (isAdded) addedTagPagingFlow else tagPagingFlow }
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsSelected()

        isAdded = true
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(SECOND_TAG_TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertIsNotSelected()
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsSelected()
    }

    private fun assertTagAddAvailable(tagPagingData: PagingData<Tag>) {
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(tagPagingData = tagPagingData, onEvent = eventList::add)

        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled().performClick()

        eventList shouldBe listOf(TagFilterEvent.ClickAdd)
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    private fun displayedTextList(): List<String> =
        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .flatMap { node -> node.config[SemanticsProperties.Text].map { text -> text.text } }

    // 필터를 연 채로 조회 결과가 바뀌는 것을 재현하려고, 화면에 넘기는 태그 흐름을 테스트가 바꾼다.
    private fun setSwitchingContent(
        selectedTagIdSet: Set<Uuid> = emptySet(),
        tagPagingFlowProvider: () -> MutableStateFlow<PagingData<Tag>>,
    ) {
        composeRule.setContent {
            DiaryTheme {
                RoutineHomeFilterBottomSheetContent(
                    onEvent = {},
                    onTagFilterEvent = {},
                    tagPagingItems = tagPagingFlowProvider().collectAsLazyPagingItems(),
                    uiStateProvider = { RoutineHomeFilterUiState(selectedTagIdSet = selectedTagIdSet) },
                )
            }
        }
    }

    private fun setBottomSheetContent(
        tagList: List<Tag> = emptyList(),
        selectedTagIdSet: Set<Uuid> = emptySet(),
        tagPagingData: PagingData<Tag> = tagPagingDataOf(tagList),
        onEvent: (TagFilterEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                RoutineHomeFilterBottomSheetContent(
                    onEvent = {},
                    onTagFilterEvent = onEvent,
                    tagPagingItems = MutableStateFlow(tagPagingData).collectAsLazyPagingItems(),
                    uiStateProvider = { RoutineHomeFilterUiState(selectedTagIdSet = selectedTagIdSet) },
                )
            }
        }
    }

    private companion object {
        const val FILTER_TITLE = "Tag Filter"
        const val FIRST_TAG_TITLE = "RoutineFilterAlpha"
        const val SECOND_TAG_TITLE = "RoutineFilterBravo"
        const val CHANGED_FIRST_TAG_TITLE = "ChangedRoutineFilterAlpha"
        const val CHANGED_SECOND_TAG_TITLE = "ChangedRoutineFilterBravo"
        const val TAG_EMOJI = "🏃"
        const val UNSELECT_ALL_CONTENT_DESCRIPTION = "Clear tag filter"
        const val ADD_LABEL = "Add tag"
        const val TIMEOUT_MILLIS = 5_000L
        const val TAG_PAGE_SIZE = 20
        const val PAGED_TAG_COUNT = TAG_PAGE_SIZE * 3
        const val PAGED_TAG_TITLE_PREFIX = "PagedRoutineFilterTag"

        fun tag(
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
                )

        fun Tag.changed(title: String): Tag = copy(detail = detail.copy(emoji = TAG_EMOJI, title = title, color = detail.color.inv()))

        fun tagPagingDataOf(tagList: List<Tag>): PagingData<Tag> =
            PagingData.from(
                data = tagList,
                sourceLoadStates =
                    LoadStates(
                        refresh = LoadState.NotLoading(endOfPaginationReached = true),
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.NotLoading(endOfPaginationReached = true),
                    ),
            )

        fun tagPagingDataWithRefresh(refresh: LoadState): PagingData<Tag> =
            PagingData.from(
                data = emptyList(),
                sourceLoadStates =
                    LoadStates(
                        refresh = refresh,
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.NotLoading(endOfPaginationReached = true),
                    ),
            )
    }
}
