package io.github.taetae98coding.diary.feature.memo.ui.home.filter

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
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.memo.ui.tag.failedTagPagingData
import io.github.taetae98coding.diary.feature.memo.ui.tag.refreshingTagPagingData
import io.github.taetae98coding.diary.feature.memo.ui.tag.tagPagingDataOf
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
class MemoHomeFilterBottomSheetContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

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
    fun `TC-MEMO-HOME-FEATURE-057 TC-MEMO-HOME-DOMAIN-024 보이는 선택한 태그가 없으면 지우기를 실행할 수 없다`() {
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
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsNotEnabled()
        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertIsNotEnabled()
        composeRule.onNodeWithContentDescription(UNSELECT_ALL_CONTENT_DESCRIPTION).assertIsNotEnabled()

        composeRule.onNodeWithText(FIRST_TAG_TITLE).performClick()
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

    @Test
    fun `TC-MEMO-HOME-FEATURE-066 태그가 있으면 태그 칩 뒤에 태그 추가 칩을 표시한다`() {
        setBottomSheetContent(tagList = listOf(tag(title = FIRST_TAG_TITLE)))

        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-066 태그 없이 조회가 끝나도 태그 추가 칩을 표시한다`() {
        setBottomSheetContent(tagPagingData = tagPagingDataOf(emptyList()))

        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-066 태그 없이 조회가 끝나면 태그 칩만 사라지고 빈 상태 안내는 나타나지 않는다`() {
        val tag = tag(title = FIRST_TAG_TITLE)
        val tagPagingFlow = MutableStateFlow(tagPagingDataOf(listOf(tag)))
        val emptyTagPagingFlow = MutableStateFlow(tagPagingDataOf(emptyList()))
        var isTagEmpty by mutableStateOf(false)
        composeRule.setContent {
            DiaryTheme {
                MemoHomeFilterBottomSheetContent(
                    tagPagingItems = (if (isTagEmpty) emptyTagPagingFlow else tagPagingFlow).collectAsLazyPagingItems(),
                    uiStateProvider = { MemoHomeFilterUiState() },
                    onEvent = {},
                    onTagFilterEvent = {},
                )
            }
        }
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertExists()
        val textListWithTag = displayedTextList()

        isTagEmpty = true
        composeRule.waitUntil(timeoutMillis = TAG_UPDATE_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(FIRST_TAG_TITLE).fetchSemanticsNodes().isEmpty()
        }

        displayedTextList() shouldBe textListWithTag - FIRST_TAG_TITLE
        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-066 태그 조회가 진행 중이어도 태그 추가 칩을 표시한다`() {
        setBottomSheetContent(tagPagingData = refreshingTagPagingData())

        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-066 태그 조회에 실패해도 태그 추가 칩을 표시한다`() {
        setBottomSheetContent(tagPagingData = failedTagPagingData())

        composeRule.onNodeWithText(ADD_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-064 태그 추가 칩을 누르면 태그 선택을 바꾸지 않고 태그 추가 Event만 전달한다`() {
        val selectedTag = tag(title = FIRST_TAG_TITLE)
        val unselectedTag = tag(title = SECOND_TAG_TITLE)
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(
            tagList = listOf(selectedTag, unselectedTag),
            uiState = MemoHomeFilterUiState(selectedTagIdSet = setOf(selectedTag.id)),
            onTagFilterEvent = eventList::add,
        )

        composeRule.onNodeWithText(ADD_LABEL).performClick()

        eventList shouldBe listOf(TagFilterEvent.ClickAdd)
        composeRule.onNodeWithText(FIRST_TAG_TITLE).assertIsSelected()
        composeRule.onNodeWithText(SECOND_TAG_TITLE).assertIsNotSelected()
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-065 태그 축이 없음이면 태그 추가 칩을 누를 수 없다`() {
        val eventList = mutableListOf<TagFilterEvent>()
        setBottomSheetContent(
            uiState = MemoHomeFilterUiState(existence = MemoExistenceFilter(tag = MemoFilterExistence.NOT_EXIST)),
            onTagFilterEvent = eventList::add,
        )

        composeRule.onNodeWithText(ADD_LABEL).assertIsNotEnabled()
        composeRule.onNodeWithText(ADD_LABEL).performClick()

        eventList.shouldBeEmpty()
    }

    private fun displayedTextList(): List<String> =
        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .flatMap { node -> node.config[SemanticsProperties.Text].map { text -> text.text } }

    @Test
    fun `TC-MEMO-HOME-FEATURE-078 태그를 조회하지 못하면 오류 안내 없이 태그 추가 칩만 표시한다`() {
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
    fun `TC-MEMO-HOME-FEATURE-079 태그의 이모지, 제목이나 컬러가 바뀌면 필터에 반영하고 선택 여부를 유지한다`() {
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
    fun `TC-MEMO-HOME-FEATURE-080 필터를 연 채로 태그가 추가되면 필터에 바로 나타난다`() {
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
                MemoHomeFilterBottomSheetContent(
                    tagPagingItems = tagPagingFlowProvider().collectAsLazyPagingItems(),
                    uiStateProvider = { MemoHomeFilterUiState(selectedTagIdSet = selectedTagIdSet) },
                    onEvent = {},
                    onTagFilterEvent = {},
                )
            }
        }
    }

    private fun setBottomSheetContent(
        tagList: List<Tag> = emptyList(),
        uiState: MemoHomeFilterUiState = MemoHomeFilterUiState(),
        tagPagingData: PagingData<Tag> = tagPagingDataOf(tagList),
        onEvent: (MemoHomeFilterBottomSheetEvent) -> Unit = {},
        onTagFilterEvent: (TagFilterEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                MemoHomeFilterBottomSheetContent(
                    tagPagingItems = MutableStateFlow(tagPagingData).collectAsLazyPagingItems(),
                    uiStateProvider = { uiState },
                    onEvent = onEvent,
                    onTagFilterEvent = onTagFilterEvent,
                )
            }
        }
    }

    companion object {
        private const val FIRST_TAG_TITLE = "MemoFilterAlpha"
        private const val CHANGED_FIRST_TAG_TITLE = "ChangedFilterAlpha"
        private const val CHANGED_SECOND_TAG_TITLE = "ChangedFilterBravo"
        private const val SECOND_TAG_TITLE = "MemoFilterBravo"
        private const val TAG_EMOJI = "🏃"
        private const val DATE_LABEL = "Date"
        private const val TAG_LABEL = "Tag"
        private const val PLACE_LABEL = "Place"
        private const val EXIST_LABEL = "With"
        private const val NOT_EXIST_LABEL = "Without"
        private const val ALL_LABEL = "All"
        private const val UNSELECT_ALL_CONTENT_DESCRIPTION = "Clear tag filter"
        private const val ADD_LABEL = "Add tag"
        private const val TAG_UPDATE_TIMEOUT_MILLIS = 5_000L
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
                .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()

        private fun Tag.changed(title: String): Tag = copy(detail = detail.copy(emoji = TAG_EMOJI, title = title, color = detail.color.inv()))
    }
}
