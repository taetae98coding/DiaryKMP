package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MEMO_DATE_HEADER_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.rememberMemoListState
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-HOME-FEATURE-001 목록에 메모 제목이 카드로 표시된다`() {
        val itemList =
            listOf(
                MemoListItem.Content(memo = memo(title = FIRST_TITLE)),
                MemoListItem.Content(memo = memo(title = SECOND_TITLE)),
            )

        setMemoHomeScaffold(itemList = itemList)

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-LIST-DETAIL-FEATURE-013 상세 영역에 메모 추가 화면이 표시되면 메모 추가 버튼이 표시되지 않는다`() {
        setMemoHomeScaffold(componentVisibleProvider = { MemoHomeScaffoldComponentVisible(isAddButtonVisible = false) })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-LIST-DETAIL-FEATURE-011 TC-MEMO-LIST-DETAIL-FEATURE-014 추가 버튼 표시 상태이면 메모 추가 버튼이 표시된다`() {
        setMemoHomeScaffold(componentVisibleProvider = { MemoHomeScaffoldComponentVisible(isAddButtonVisible = true) })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-013 기간이 있는 메모의 카드 위에 시작 날짜 헤더가 표시된다`() {
        setMemoHomeScaffold(itemList = dateHeaderMemoItemList())

        composeRule.onAllNodes(dateHeaderMatcher(DEFAULT_ALL_DAY_HEADER)).assertCountEquals(1)
        composeRule.onAllNodes(dateHeaderMatcher(DEFAULT_DATE_TIME_HEADER)).assertCountEquals(1)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-HOME-FEATURE-013 한국어 환경에서 시작 날짜 헤더를 표시한다`() {
        setMemoHomeScaffold(itemList = dateHeaderMemoItemList())

        composeRule.onAllNodes(dateHeaderMatcher(KOREAN_ALL_DAY_HEADER)).assertCountEquals(1)
        composeRule.onAllNodes(dateHeaderMatcher(KOREAN_DATE_TIME_HEADER)).assertCountEquals(1)
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-014 시작 날짜가 같은 메모들에는 날짜 헤더가 하나만 표시된다`() {
        val itemList =
            listOf(
                MemoListItem.DateHeader(date = LocalDate(year = 2026, month = 7, day = 19)),
                MemoListItem.Content(memo = memo(title = FIRST_TITLE, dateTime = allDay(day = 19))),
                MemoListItem.Content(memo = memo(title = SECOND_TITLE, dateTime = dateTime(day = 19, hour = 13))),
                MemoListItem.DateHeader(date = LocalDate(year = 2026, month = 7, day = 21)),
                MemoListItem.Content(memo = memo(title = THIRD_TITLE, dateTime = allDay(day = 21))),
            )

        setMemoHomeScaffold(itemList = itemList)

        composeRule.onAllNodes(dateHeaderMatcher(DEFAULT_ALL_DAY_HEADER)).assertCountEquals(1)
        composeRule.onAllNodes(dateHeaderMatcher(DEFAULT_DATE_TIME_HEADER)).assertCountEquals(1)
        composeRule.onAllNodesWithTag(MEMO_DATE_HEADER_TEST_TAG).assertCountEquals(2)
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-016 기간이 없는 메모에는 날짜 헤더가 표시되지 않는다`() {
        val itemList =
            listOf(
                MemoListItem.Content(memo = memo(title = FIRST_TITLE)),
                MemoListItem.Content(memo = memo(title = SECOND_TITLE)),
            )

        setMemoHomeScaffold(itemList = itemList)

        composeRule.onNodeWithText(FIRST_TITLE).assertExists()
        composeRule.onAllNodesWithTag(MEMO_DATE_HEADER_TEST_TAG).assertCountEquals(0)
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-072 유무 필터의 한 축이 있음이면 필터 버튼이 적용 상태를 알리고 누르면 필터 열기 Event를 전달한다`() {
        val eventList = mutableListOf<MemoHomeScaffoldEvent>()
        setMemoHomeScaffold(
            filterUiState = MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(date = MemoFilterExistence.EXIST)),
            onEvent = eventList::add,
        )

        composeRule
            .onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION)
            .assert(hasStateDescription(DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION))
            .performClick()

        eventList shouldBe listOf(MemoHomeScaffoldEvent.ClickFilter)
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-072 세 축이 모두 전체이고 선택한 태그가 없으면 필터 버튼이 적용 상태를 알리지 않는다`() {
        setMemoHomeScaffold(filterUiState = MemoHomeScaffoldFilterUiState())

        filterButtonStateDescription() shouldBe null
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-072 유무 필터의 한 축이 없음이면 필터 버튼이 적용 상태를 알린다`() {
        setMemoHomeScaffold(filterUiState = MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(tag = MemoFilterExistence.NOT_EXIST)))

        filterButtonStateDescription() shouldBe DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-072 태그를 하나 이상 선택하면 필터 버튼이 적용 상태를 알린다`() {
        val tagId = fixtureMonkey.giveMeOne<Uuid>()
        setMemoHomeScaffold(filterUiState = MemoHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)))

        filterButtonStateDescription() shouldBe DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-072 선택한 태그가 모두 판정에서 무시되고 있으면 필터 버튼이 적용 상태를 알리지 않는다`() {
        setMemoHomeScaffold(filterUiState = MemoHomeScaffoldFilterUiState(selectedTagIdSet = emptySet(), storedTagIdSet = setOf(fixtureMonkey.giveMeOne<Uuid>())))

        filterButtonStateDescription() shouldBe null
    }

    @Test
    fun `TC-MEMO-HOME-FEATURE-029 TC-MEMO-HOME-FEATURE-030 완료된 메모 버튼을 누르면 완료 목록 열기 Event를 전달한다`() {
        val eventList = mutableListOf<MemoHomeScaffoldEvent>()
        setMemoHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_BUTTON_LABEL).performClick()

        eventList shouldBe listOf(MemoHomeScaffoldEvent.ClickFinishedList)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MEMO-HOME-FEATURE-029 한국어 환경에서 완료된 메모 버튼 이름은 완료된 메모이다`() {
        setMemoHomeScaffold()

        composeRule.onNodeWithText(KOREAN_FINISHED_LIST_BUTTON_LABEL).assert(hasClickAction())
    }

    @Test
    fun `같은 시작 날짜의 카드를 스크롤하는 동안 그 날짜 헤더가 화면에 계속 표시된다`() {
        val itemList =
            buildList {
                add(MemoListItem.DateHeader(date = LocalDate(year = 2026, month = 7, day = 19)))
                addAll(
                    List(SCROLL_MEMO_COUNT) { index ->
                        MemoListItem.Content(
                            memo = memo(title = "$SCROLL_TITLE_PREFIX$index", dateTime = allDay(day = 19)),
                        )
                    },
                )
            }

        setMemoHomeScaffold(itemList = itemList)

        composeRule.onNodeWithText("${SCROLL_TITLE_PREFIX}0").assertIsDisplayed()

        composeRule.onNodeWithTag(MEMO_HOME_LIST_TEST_TAG).performScrollToIndex(SCROLL_MEMO_COUNT)
        composeRule.waitForIdle()

        composeRule.onNodeWithText("${SCROLL_TITLE_PREFIX}0").assertDoesNotExist()
        composeRule.onNodeWithText("$SCROLL_TITLE_PREFIX${SCROLL_MEMO_COUNT - 1}").assertIsDisplayed()
        composeRule.onAllNodes(dateHeaderMatcher(DEFAULT_ALL_DAY_HEADER)).assertCountEquals(1)
        composeRule.onNode(dateHeaderMatcher(DEFAULT_ALL_DAY_HEADER)).assertIsDisplayed()
    }

    private fun setMemoHomeScaffold(
        itemList: List<MemoListItem> = emptyList(),
        filterUiState: MemoHomeScaffoldFilterUiState = MemoHomeScaffoldFilterUiState(),
        onEvent: (MemoHomeScaffoldEvent) -> Unit = {},
        onMemoListEvent: (MemoListEvent) -> Unit = {},
        componentVisibleProvider: () -> MemoHomeScaffoldComponentVisible = { MemoHomeScaffoldComponentVisible() },
    ) {
        val memoPagingData = MutableStateFlow(memoPagingDataOf(itemList))

        composeRule.setContent {
            DiaryTheme {
                MemoHomeScaffold(
                    memoListState = rememberMemoListState(),
                    snackbarHostState = remember { SnackbarHostState() },
                    memoPagingItems = memoPagingData.collectAsLazyPagingItems(),
                    filterUiStateProvider = { filterUiState },
                    onEvent = onEvent,
                    onMemoListEvent = onMemoListEvent,
                    componentVisibleProvider = componentVisibleProvider,
                )
            }
        }

        itemList
            .filterIsInstance<MemoListItem.Content>()
            .firstOrNull()
            ?.memo
            ?.detail
            ?.title
            ?.let(::waitUntilMemoIsDisplayed)
    }

    private fun filterButtonStateDescription(): String? =
        composeRule
            .onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.StateDescription)

    private fun waitUntilMemoIsDisplayed(title: String) {
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    public companion object {
        private const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L
        private const val FIRST_TITLE = "FirstMemoTitle"
        private const val SECOND_TITLE = "SecondMemoTitle"
        private const val THIRD_TITLE = "ThirdMemoTitle"
        private const val SCROLL_TITLE_PREFIX = "ScrollMemoTitle"
        private const val SCROLL_MEMO_COUNT = 20
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add memo"
        private const val DEFAULT_FILTER_BUTTON_DESCRIPTION = "Filter"
        private const val DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION = "Filter applied"
        private const val DEFAULT_FINISHED_LIST_BUTTON_LABEL = "Finished memos"
        private const val KOREAN_FINISHED_LIST_BUTTON_LABEL = "완료된 메모"
        private const val DEFAULT_ALL_DAY_HEADER = "Jul 19, 2026"
        private const val DEFAULT_DATE_TIME_HEADER = "Jul 21, 2026"
        private const val KOREAN_ALL_DAY_HEADER = "2026. 7. 19."
        private const val KOREAN_DATE_TIME_HEADER = "2026. 7. 21."
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(
            title: String,
            dateTime: MemoDateTime? = null,
        ): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title, dateTime = dateTime))
                .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun dateHeaderMemoItemList(): List<MemoListItem> =
            listOf(
                MemoListItem.DateHeader(date = LocalDate(year = 2026, month = 7, day = 19)),
                MemoListItem.Content(memo = memo(title = FIRST_TITLE, dateTime = allDay(day = 19))),
                MemoListItem.DateHeader(date = LocalDate(year = 2026, month = 7, day = 21)),
                MemoListItem.Content(memo = memo(title = SECOND_TITLE, dateTime = dateTime(day = 21, hour = 13))),
            )

        private fun allDay(day: Int): MemoDateTime.AllDay = MemoDateTime.AllDay(dateRange = LocalDate(year = 2026, month = 7, day = day)..LocalDate(year = 2026, month = 7, day = day))

        private fun dateTime(
            day: Int,
            hour: Int,
        ): MemoDateTime.DateTime =
            MemoDateTime.DateTime(
                start = LocalDateTime(year = 2026, month = 7, day = day, hour = hour, minute = 30),
                endInclusive = LocalDateTime(year = 2026, month = 7, day = day + 1, hour = hour, minute = 30),
            )
    }
}

private fun dateHeaderMatcher(text: String): SemanticsMatcher = hasTestTag(MEMO_DATE_HEADER_TEST_TAG) and hasText(text)
