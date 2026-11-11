package io.github.taetae98coding.diary.feature.tag.ui.detail.dday

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.dday.DDayListEvent
import io.github.taetae98coding.diary.compose.dday.SwipeDDayCard
import io.github.taetae98coding.diary.compose.dday.rememberDDayListState
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagDDay
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityLoadingPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailDDayTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-08893b99-d83c-4ef2-8400-eb88e6695b68 대상 태그와 연결된 디데이를 목록에 표시한다`() {
        setDDayTab(pagingData = tagEntityPagingData(itemList = listOf(tagDDay(title = FIRST_TITLE), tagDDay(title = SECOND_TITLE))))

        composeRule.refreshableList().assertExists()
        composeRule.onAllNodesWithTag(DDAY_CARD_TEST_TAG).assertCountEquals(2)
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(SECOND_TITLE).assertIsDisplayed()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-4b70ffce-8100-48bd-9e5e-d9dc5017875b 각 디데이의 제목과 카드 문구를 표시한다`() {
        setDDayTab(
            pagingData = tagEntityPagingData(itemList = listOf(tagDDay(title = KOREAN_TITLE, baseDate = LocalDate(year = 2025, month = 1, day = 15), countsBaseDay = true))),
            today = LocalDate(year = 2026, month = 3, day = 20),
        )

        composeRule.onNodeWithText(KOREAN_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_CARD_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-ad79fabc-7400-4ea8-9a8e-3bf040031533 목록은 전달된 제목 오름차순 그대로 표시한다`() {
        val titleList = listOf(FIRST_TITLE, SECOND_TITLE, THIRD_TITLE)
        setDDayTab(pagingData = tagEntityPagingData(itemList = titleList.map { title -> tagDDay(title = title) }))

        // 격자는 왼쪽에서 오른쪽, 위에서 아래 순서로 놓인다.
        val positionList =
            titleList.map { title ->
                composeRule
                    .onNodeWithText(title)
                    .fetchSemanticsNode()
                    .boundsInRoot
                    .let { bounds -> bounds.top to bounds.left }
            }

        positionList shouldBe positionList.sortedWith(compareBy({ position -> position.first }, { position -> position.second }))
        positionList.distinct().size shouldBe titleList.size
    }

    @Test
    fun `TC-c53efc05-c285-476b-9798-400a84506805 디데이 탭은 정렬 선택을 제공하지 않는다`() {
        setDDayTab(pagingData = tagEntityPagingData(itemList = listOf(tagDDay(title = FIRST_TITLE))))

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-7535ed5e-06e6-4ea6-ac03-2226c7e77493 자리 표시 상태의 디데이는 선택해도 상세 이동을 요청하지 않는다`() {
        val eventList = mutableListOf<DDayListEvent>()
        setPlaceholderCard(onEvent = eventList::add)

        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
    }

    @Test
    fun `TC-7fe765f0-29ac-4040-aee6-45b9fd277036 아직 준비되지 않은 자리는 밀어도 삭제를 요청하지 않는다`() {
        val eventList = mutableListOf<DDayListEvent>()
        setPlaceholderCard(onEvent = eventList::add)

        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun `TC-8e4c6495-f011-4ca9-a9b2-f8d1af9fef39 최초 조회 실패는 오류와 재시도를 표시하지 않는다`() {
        setDDayTab(
            pagingData =
                tagEntityPagingData<DDay>(
                    itemList = emptyList(),
                    refresh = LoadState.Error(IllegalStateException("Refresh failed")),
                ),
        )

        composeRule.onAllNodesWithTag(DDAY_CARD_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-8e4c6495-f011-4ca9-a9b2-f8d1af9fef39 추가 조회 실패는 표시된 디데이를 유지한다`() {
        setDDayTab(
            pagingData =
                tagEntityPagingData(
                    itemList = listOf(tagDDay(title = FIRST_TITLE)),
                    append = LoadState.Error(IllegalStateException("Append failed")),
                ),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithTag(DDAY_CARD_TEST_TAG).assertCountEquals(1)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-8e4c6495-f011-4ca9-a9b2-f8d1af9fef39 다시 조회가 실패해 빈 목록이 되면 이전 디데이를 남기지 않는다`() {
        val pagingDataFlow = MutableStateFlow(tagEntityPagingData(itemList = listOf(tagDDay(title = FIRST_TITLE))))
        setDDayTab(pagingDataFlow = pagingDataFlow)
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()

        composeRule.runOnIdle { pagingDataFlow.value = tagEntityPagingData(itemList = emptyList()) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(FIRST_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-8a780f18-6ca9-43cd-b372-c55d623c510f 디데이를 선택하면 그 디데이의 상세 이동을 요청한다`() {
        val item = tagDDay(title = FIRST_TITLE)
        val eventList = mutableListOf<TagDetailDDayContentEvent>()
        setDDayTab(pagingData = tagEntityPagingData(itemList = listOf(item)), onEvent = eventList::add)

        composeRule.onNodeWithText(FIRST_TITLE).performClick()

        eventList shouldBe listOf(TagDetailDDayContentEvent.ClickDDay(id = item.id))
    }

    @Test
    fun `TC-9c8de74c-b24a-4fef-8958-6773c8d25f19 연결된 디데이가 없으면 빈 상태 안내를 표시한다`() {
        setDDayTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
        composeRule.refreshableList().assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-9c8de74c-b24a-4fef-8958-6773c8d25f19 한국어 환경에서 빈 상태 안내를 표시한다`() {
        setDDayTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-6e485d20-1f74-4b89-b7bf-4f6455e6449d 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setDDayTab(pagingData = tagEntityLoadingPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-10f1384d-dd6f-44bf-93ff-07ed71aaf699 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailDDayContentEvent>()
        setDDayTab(pagingData = tagEntityPagingData(itemList = listOf(tagDDay(title = FIRST_TITLE))), onEvent = eventList::add)

        composeRule.refreshableList().performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailDDayContentEvent.Refresh)
    }

    @Test
    fun `TC-10f1384d-dd6f-44bf-93ff-07ed71aaf699 빈 상태에서도 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailDDayContentEvent>()
        setDDayTab(pagingData = tagEntityPagingData(itemList = emptyList()), onEvent = eventList::add)

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailDDayContentEvent.Refresh)
    }

    @Test
    fun `TC-10f1384d-dd6f-44bf-93ff-07ed71aaf699 디데이가 표시된 채 새로고침이 진행 중이면 진행 표시가 나타나고 끝나면 사라진다`() {
        assertRefreshingIndicator(pagingData = tagEntityPagingData(itemList = listOf(tagDDay(title = FIRST_TITLE))))
    }

    @Test
    fun `TC-10f1384d-dd6f-44bf-93ff-07ed71aaf699 빈 상태에서 새로고침이 진행 중이면 진행 표시가 나타나고 끝나면 사라진다`() {
        assertRefreshingIndicator(pagingData = tagEntityPagingData(itemList = emptyList()))
    }

    @Test
    fun `TC-4382250e-5614-4d93-b8e0-3a543a092105 디데이 카드를 삭제 방향으로 밀면 그 디데이의 삭제를 전달한다`() {
        val item = tagDDay(title = FIRST_TITLE)
        val eventList = mutableListOf<TagDetailDDayContentEvent>()
        setDDayTab(pagingData = tagEntityPagingData(itemList = listOf(item)), onEvent = eventList::add)

        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailDDayContentEvent.DeleteDDay(id = item.id))
    }

    @Test
    fun `TC-e4c34ecb-550c-4880-a2e1-39a34b9ed31a 디데이 카드를 반대 방향으로 밀면 아무 동작을 요청하지 않는다`() {
        val eventList = mutableListOf<TagDetailDDayContentEvent>()
        setDDayTab(pagingData = tagEntityPagingData(itemList = listOf(tagDDay(title = FIRST_TITLE))), onEvent = eventList::add)

        composeRule.onNodeWithTag(DDAY_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-1f64fda5-cb8b-4c2a-b5c2-6df4e1939631 기준일이 바뀐 디데이는 카드 문구가 바뀐다`() {
        val exam = tagDDay(title = EXAM_TITLE, baseDate = LocalDate(year = 2026, month = 3, day = 20), countsBaseDay = false)
        val pagingDataFlow = MutableStateFlow(tagEntityPagingData(itemList = listOf(exam)))
        setDDayTab(pagingDataFlow = pagingDataFlow, today = LocalDate(year = 2026, month = 3, day = 19))
        composeRule.onNodeWithText(EXAM_CARD_TEXT).assertIsDisplayed()

        val changed = exam.copy(detail = exam.detail.copy(baseDate = LocalDate(year = 2026, month = 3, day = 21)))
        composeRule.runOnIdle { pagingDataFlow.value = tagEntityPagingData(itemList = listOf(changed)) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CHANGED_EXAM_CARD_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(EXAM_CARD_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(EXAM_TITLE).assertIsDisplayed()
    }

    private fun assertRefreshingIndicator(pagingData: PagingData<DDay>) {
        val isRefreshing = mutableStateOf(true)
        setDDayTab(pagingDataFlow = MutableStateFlow(pagingData), isRefreshingProvider = { isRefreshing.value })

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    private fun setPlaceholderCard(onEvent: (DDayListEvent) -> Unit) {
        // 목록은 아직 준비되지 않은 자리를 디데이 없이 같은 카드로 그린다.
        composeRule.setContent {
            DiaryTheme {
                SwipeDDayCard(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth(),
                    dday = null,
                )
            }
        }
    }

    private fun setDDayTab(
        pagingData: PagingData<DDay> = PagingData.empty(),
        onEvent: (TagDetailDDayContentEvent) -> Unit = {},
        today: LocalDate? = null,
    ) {
        setDDayTab(pagingDataFlow = MutableStateFlow(pagingData), onEvent = onEvent, today = today)
    }

    private fun setDDayTab(
        pagingDataFlow: MutableStateFlow<PagingData<DDay>>,
        onEvent: (TagDetailDDayContentEvent) -> Unit = {},
        today: LocalDate? = null,
        isRefreshingProvider: () -> Boolean = { false },
    ) {
        composeRule.setContent {
            DiaryTheme {
                TagDetailDDayTab(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize(),
                    listState = rememberDDayListState(initialToday = today),
                    ddayPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    isRefreshingProvider = isRefreshingProvider,
                )
            }
        }
    }

    private companion object {
        const val FIRST_TITLE = "AlphaDDay"
        const val SECOND_TITLE = "BetaDDay"
        const val THIRD_TITLE = "GammaDDay"
        const val KOREAN_TITLE = "우리"
        const val KOREAN_CARD_TEXT = "430일(1년 2개월)"
        const val EXAM_TITLE = "수능"
        const val EXAM_CARD_TEXT = "D-1"
        const val CHANGED_EXAM_CARD_TEXT = "D-2"
        const val DEFAULT_EMPTY_TITLE = "No D-Days linked to this tag"
        const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a D-Day."
        const val KOREAN_EMPTY_TITLE = "이 태그에 연결된 디데이가 없습니다"
        const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 디데이를 만들 수 있습니다"
        const val DEFAULT_ERROR_TEXT = "Error"
        const val DEFAULT_RETRY_TEXT = "Retry"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        const val DEFAULT_SORT_DESCRIPTION = "List sort"
    }
}
