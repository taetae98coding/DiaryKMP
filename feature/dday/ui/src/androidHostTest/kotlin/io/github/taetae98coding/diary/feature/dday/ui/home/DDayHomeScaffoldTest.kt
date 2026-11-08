package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.dday.DDayListEvent
import io.github.taetae98coding.diary.compose.dday.rememberDDayListState
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.dday.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class DDayHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-6bdbd5aa-9cd9-443d-b6cf-15569cd8af16 한국어 환경에서 상단 바에 제목을 표시한다`() {
        setDDayHomeScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
    }

    @Test
    fun `TC-6bdbd5aa-9cd9-443d-b6cf-15569cd8af16 기본 환경에서 상단 바에 제목을 표시한다`() {
        setDDayHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        setDDayHomeScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).assertExists()
    }

    @Test
    fun `뒤로가기를 누르면 뒤로가기 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<DDayHomeScaffoldEvent>()
        setDDayHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(DDayHomeScaffoldEvent.ClickNavigateUp)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-dbfa3393-be4d-44ed-911f-494552f3182a 제목, 뒤로가기, 필터 열기, 목록과 추가 외의 정보와 동작이 없다`() {
        val dday = homeDDayOf(title = WEDDING_TITLE) { detail -> detail.copy(baseDate = TODAY, countsBaseDay = false) }
        setDDayHomeScaffold(pagingData = homeDDayPagingDataOf(listOf(dday)))

        composeRule.homeVisibleTextList() shouldContainExactlyInAnyOrder listOf(KOREAN_TITLE, WEDDING_TITLE, TODAY_CARD_TEXT)
        composeRule.homeActionNameList() shouldContainExactlyInAnyOrder
            listOf(
                KOREAN_NAVIGATE_UP_DESCRIPTION,
                KOREAN_FILTER_DESCRIPTION,
                KOREAN_ADD_DESCRIPTION,
                "$WEDDING_TITLE $TODAY_CARD_TEXT",
            )
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-04b394e3-662a-45a9-a0ae-6a147b37611c 계정에 저장된 디데이를 제목과 카드 문구로 표시한다`() {
        val wedding = homeDDayOf(title = WEDDING_TITLE) { detail -> detail.copy(baseDate = LocalDate(year = 2025, month = 1, day = 15), countsBaseDay = true) }
        val exam = homeDDayOf(title = EXAM_TITLE) { detail -> detail.copy(baseDate = TODAY) }
        setDDayHomeScaffold(pagingData = homeDDayPagingDataOf(listOf(wedding, exam)))

        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(WEDDING_TITLE) and hasText("430일(1년 2개월)")).assertExists()
        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(EXAM_TITLE) and hasText(TODAY_CARD_TEXT)).assertExists()
    }

    @Test
    fun `TC-363e235e-e7cb-49fc-82b1-b4ec76fdfa5c 디데이 카드에는 제목과 카드 문구 외의 글을 표시하지 않는다`() {
        val dday =
            homeDDayOf(title = WEDDING_TITLE) { detail ->
                detail.copy(
                    description = DESCRIPTION,
                    baseDate = TODAY,
                    repeats = setOf(DateTimeUnit.DayBased(days = REPEAT_DAYS)),
                    endDate = TODAY.plus(END_DATE_OFFSET_DAYS, DateTimeUnit.DAY),
                )
            }
        setDDayHomeScaffold(pagingData = homeDDayPagingDataOf(listOf(dday)))

        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(WEDDING_TITLE)).assertExists()
        composeRule.homeVisibleTextList() shouldContainExactlyInAnyOrder listOf(DEFAULT_TITLE, WEDDING_TITLE, TODAY_CARD_TEXT)
    }

    @Test
    fun `TC-7e906050-2851-4551-84fc-d1c96cfd5c2b 그 외 기본 언어에서는 디자인이 정한 형식으로 카드 문구를 표시한다`() {
        val caseList =
            listOf(
                homeDDayOf(title = "Upcoming400") { detail -> detail.copy(baseDate = TODAY.plus(400, DateTimeUnit.DAY)) } to "D-400 (1 year 1 month)",
                homeDDayOf(title = "Upcoming20") { detail -> detail.copy(baseDate = TODAY.plus(20, DateTimeUnit.DAY)) } to "D-20",
                homeDDayOf(title = "Today") { detail -> detail.copy(baseDate = TODAY) } to TODAY_CARD_TEXT,
                homeDDayOf(title = "Passed430") { detail -> detail.copy(baseDate = LocalDate(year = 2025, month = 1, day = 15), countsBaseDay = true) } to "430 days (1 year 2 months)",
                homeDDayOf(title = "Passed366") { detail -> detail.copy(baseDate = TODAY.minus(365, DateTimeUnit.DAY), countsBaseDay = true) } to "366 days (1 year)",
                homeDDayOf(title = "Passed1") { detail -> detail.copy(baseDate = TODAY.minus(1, DateTimeUnit.DAY), countsBaseDay = false) } to "1 day",
            )
        setDDayHomeScaffold(pagingData = homeDDayPagingDataOf(caseList.map { (dday, _) -> dday }))

        caseList.forEach { (dday, cardText) ->
            composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(dday.detail.title) and hasText(cardText)).assertExists()
        }
    }

    @Test
    fun `TC-4263ae3e-c814-4efc-8994-23ca99f1d815 종료일이 지난 디데이도 목록에 카드 문구와 함께 남는다`() {
        val baseDate = TODAY.minus(10, DateTimeUnit.DAY)
        val dday =
            homeDDayOf(title = WEDDING_TITLE) { detail ->
                detail.copy(
                    baseDate = baseDate,
                    countsBaseDay = false,
                    repeats = setOf(DateTimeUnit.DayBased(days = 2)),
                    endDate = TODAY.minus(1, DateTimeUnit.DAY),
                )
            }
        setDDayHomeScaffold(pagingData = homeDDayPagingDataOf(listOf(dday)))

        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(WEDDING_TITLE) and hasText("10 days")).assertExists()
    }

    @Test
    fun `TC-06e948ee-3e6e-404c-9ba6-820f94d42c6c 디데이가 새로 저장되면 제목 순서에 맞는 자리에 카드 문구와 함께 나타난다`() {
        val first = homeDDayOf(title = ALPHA_TITLE) { detail -> detail.copy(baseDate = TODAY) }
        val third = homeDDayOf(title = CHARLIE_TITLE) { detail -> detail.copy(baseDate = TODAY) }
        val added = homeDDayOf(title = BRAVO_TITLE) { detail -> detail.copy(baseDate = TODAY.plus(20, DateTimeUnit.DAY)) }
        val pagingDataFlow = MutableStateFlow(homeDDayPagingDataOf(listOf(first, third)))
        setDDayHomeScaffold(pagingDataFlow = pagingDataFlow)

        composeRule.runOnIdle { pagingDataFlow.value = homeDDayPagingDataOf(listOf(first, added, third)) }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasText(added.detail.title)).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(added.detail.title) and hasText("D-20")).assertExists()
        composeRule.homeCardTitleList(listOf(first, added, third).map { dday -> dday.detail.title }) shouldBe listOf(ALPHA_TITLE, BRAVO_TITLE, CHARLIE_TITLE)
    }

    @Test
    fun `TC-74601fdf-3695-4b1b-b8e5-cf7c44531ce5 기준일이 수정되면 카드 문구가 다시 정해지고 자리는 그대로다`() {
        assertCardChanged(
            transform = { detail -> detail.copy(baseDate = TODAY.plus(20, DateTimeUnit.DAY)) },
            expectedCardText = "D-20",
        )
    }

    @Test
    fun `TC-74601fdf-3695-4b1b-b8e5-cf7c44531ce5 지난 디데이의 기준일을 1일로 세기가 바뀌면 지난 날 수가 1만큼 바뀐다`() {
        assertCardChanged(
            initial = { detail -> detail.copy(baseDate = TODAY.minus(10, DateTimeUnit.DAY), countsBaseDay = false) },
            transform = { detail -> detail.copy(countsBaseDay = true) },
            initialCardText = "10 days",
            expectedCardText = "11 days",
        )
    }

    @Test
    fun `TC-74601fdf-3695-4b1b-b8e5-cf7c44531ce5 반복 간격, 종료일, 달력에 없는 날짜 처리가 수정되어도 카드 문구와 자리가 그대로다`() {
        assertCardChanged(
            transform = { detail ->
                detail.copy(
                    repeats = setOf(DateTimeUnit.MonthBased(months = 1)),
                    endDate = TODAY.plus(END_DATE_OFFSET_DAYS, DateTimeUnit.DAY),
                    overflowPolicy = DDayOverflowPolicy.entries.first { policy -> policy != detail.overflowPolicy },
                )
            },
            expectedCardText = TODAY_CARD_TEXT,
        )
    }

    @Test
    fun `TC-74601fdf-3695-4b1b-b8e5-cf7c44531ce5 제목이 수정되면 바뀐 제목으로 제목 순서에 맞는 자리로 옮겨진다`() {
        val first = homeDDayOf(title = ALPHA_TITLE) { detail -> detail.copy(baseDate = TODAY) }
        val second = homeDDayOf(title = BRAVO_TITLE) { detail -> detail.copy(baseDate = TODAY) }
        val third = homeDDayOf(title = CHARLIE_TITLE) { detail -> detail.copy(baseDate = TODAY) }
        val pagingDataFlow = MutableStateFlow(homeDDayPagingDataOf(listOf(first, second, third)))
        setDDayHomeScaffold(pagingDataFlow = pagingDataFlow)
        val renamed = first.copy(detail = first.detail.copy(title = DELTA_TITLE))

        composeRule.runOnIdle { pagingDataFlow.value = homeDDayPagingDataOf(listOf(second, third, renamed)) }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasText(DELTA_TITLE)).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.homeCardTitleList(listOf(ALPHA_TITLE, BRAVO_TITLE, CHARLIE_TITLE, DELTA_TITLE)) shouldBe listOf(BRAVO_TITLE, CHARLIE_TITLE, DELTA_TITLE)
    }

    @Test
    fun `TC-d14ac637-3a8d-4a0f-be17-7cfaf6e01948 목록의 디데이를 누르면 그 디데이의 상세 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<DDayListEvent>()
        val ddayList = listOf(homeDDayOf(title = WEDDING_TITLE), homeDDayOf(title = EXAM_TITLE))
        setDDayHomeScaffold(pagingData = homeDDayPagingDataOf(ddayList), onDDayListEvent = eventList::add)

        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(EXAM_TITLE)).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(DDayListEvent.ClickDDay(id = ddayList.last().id))
    }

    @Test
    fun `TC-fedf3ba4-1dab-4432-8c0c-96a29514657f 선택한 태그가 없으면 필터 열기가 적용 상태를 알리지 않는다`() {
        setDDayHomeScaffold(filterUiState = DDayHomeScaffoldFilterUiState())

        filterButtonStateDescription() shouldBe null
    }

    @Test
    fun `TC-fedf3ba4-1dab-4432-8c0c-96a29514657f 태그를 하나 이상 선택하면 필터 열기가 적용 상태를 알린다`() {
        val tagId = Arb.kotlinUuid().next()
        setDDayHomeScaffold(filterUiState = DDayHomeScaffoldFilterUiState(selectedTagIdSet = setOf(tagId), storedTagIdSet = setOf(tagId)))

        filterButtonStateDescription() shouldBe DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION
    }

    @Test
    fun `TC-fedf3ba4-1dab-4432-8c0c-96a29514657f 선택한 태그가 모두 무시되고 있으면 필터 열기가 적용 상태를 알리지 않는다`() {
        setDDayHomeScaffold(filterUiState = DDayHomeScaffoldFilterUiState(selectedTagIdSet = emptySet(), storedTagIdSet = setOf(Arb.kotlinUuid().next())))

        filterButtonStateDescription() shouldBe null
    }

    @Test
    fun `필터 열기를 누르면 필터 열기 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<DDayHomeScaffoldEvent>()
        setDDayHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(DDayHomeScaffoldEvent.ClickFilter)
    }

    @Test
    fun `TC-c49f3ae8-9bc1-4cd9-bcef-ec27f3b740da 새로고침이 실행되는 동안 진행이 표시되고 끝나면 사라진다`() {
        val isRefreshing = mutableStateOf(false)
        setDDayHomeScaffold(
            pagingData = homeDDayPagingDataOf(listOf(homeDDayOf(title = WEDDING_TITLE))),
            syncUiStateProvider = { SyncRefreshUiState(isRefreshing = isRefreshing.value) },
        )
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()

        composeRule.runOnIdle { isRefreshing.value = true }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-cf634e0f-ad76-4cfe-9089-1cbbc5af69d2 디데이가 표시된 목록을 당기면 새로고침 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<DDayHomeScaffoldEvent>()
        setDDayHomeScaffold(pagingData = homeDDayPagingDataOf(listOf(homeDDayOf(title = WEDDING_TITLE))), onEvent = eventList::add)

        composeRule.onNode(hasTestTag(PULL_TO_REFRESH_TEST_TAG)).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(DDayHomeScaffoldEvent.Refresh)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-7d712476-da39-4587-9384-a24db8612d4c 한국어 환경에서 디데이 추가는 접근성 이름으로 제공된다`() {
        setDDayHomeScaffold()

        composeRule.onNode(hasClickAction() and hasContentDescription(KOREAN_ADD_DESCRIPTION)).assertExists()
    }

    @Test
    fun `TC-7d712476-da39-4587-9384-a24db8612d4c 기본 환경에서 디데이 추가는 접근성 이름으로 제공된다`() {
        setDDayHomeScaffold()

        composeRule.onNode(hasClickAction() and hasContentDescription(DEFAULT_ADD_DESCRIPTION)).assertExists()
    }

    @Test
    fun `디데이 카드는 컬러 원형 표시를 읽지 않고 제목과 카드 문구를 이 순서로 읽는다`() {
        val dday = homeDDayOf(title = WEDDING_TITLE) { detail -> detail.copy(baseDate = TODAY) }
        setDDayHomeScaffold(pagingData = homeDDayPagingDataOf(listOf(dday)))

        composeRule
            .onNode(hasTestTag(DDAY_CARD_TEST_TAG))
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .map { text -> text.text } shouldBe listOf(WEDDING_TITLE, TODAY_CARD_TEXT)
    }

    // 목록 가운데 놓인 디데이 하나만 바꾸어 다시 전달하고, 그 카드의 문구와 목록에서의 자리를 확인한다.
    private fun assertCardChanged(
        initial: (DDayDetail) -> DDayDetail = { detail -> detail.copy(baseDate = TODAY) },
        transform: (DDayDetail) -> DDayDetail,
        initialCardText: String = TODAY_CARD_TEXT,
        expectedCardText: String,
    ) {
        val first = homeDDayOf(title = ALPHA_TITLE) { detail -> detail.copy(baseDate = TODAY) }
        val target = homeDDayOf(title = BRAVO_TITLE, transform = initial)
        val third = homeDDayOf(title = CHARLIE_TITLE) { detail -> detail.copy(baseDate = TODAY) }
        val pagingDataFlow = MutableStateFlow(homeDDayPagingDataOf(listOf(first, target, third)))
        setDDayHomeScaffold(pagingDataFlow = pagingDataFlow)
        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(BRAVO_TITLE) and hasText(initialCardText)).assertExists()

        composeRule.runOnIdle { pagingDataFlow.value = homeDDayPagingDataOf(listOf(first, target.copy(detail = transform(target.detail)), third)) }
        composeRule.waitForIdle()

        composeRule.onNode(hasTestTag(DDAY_CARD_TEST_TAG) and hasText(BRAVO_TITLE) and hasText(expectedCardText)).assertExists()
        composeRule.homeCardTitleList(listOf(ALPHA_TITLE, BRAVO_TITLE, CHARLIE_TITLE)) shouldBe listOf(ALPHA_TITLE, BRAVO_TITLE, CHARLIE_TITLE)
    }

    private fun filterButtonStateDescription(): String? =
        composeRule
            .onNodeWithContentDescription(DEFAULT_FILTER_DESCRIPTION)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.StateDescription)

    private fun setDDayHomeScaffold(
        pagingData: PagingData<DDay> = homeDDayPagingDataOf(emptyList()),
        pagingDataFlow: MutableStateFlow<PagingData<DDay>> = MutableStateFlow(pagingData),
        onEvent: (DDayHomeScaffoldEvent) -> Unit = {},
        onDDayListEvent: (DDayListEvent) -> Unit = {},
        filterUiState: DDayHomeScaffoldFilterUiState = DDayHomeScaffoldFilterUiState(),
        syncUiStateProvider: () -> SyncRefreshUiState = { SyncRefreshUiState() },
    ) {
        composeRule.setContent {
            DiaryTheme {
                DDayHomeScaffold(
                    onEvent = onEvent,
                    onDDayListEvent = onDDayListEvent,
                    listState = rememberDDayListState(initialToday = TODAY),
                    ddayPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    filterUiStateProvider = { filterUiState },
                    syncUiStateProvider = syncUiStateProvider,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 3, day = 20)
        const val KOREAN_TITLE = "디데이"
        const val DEFAULT_TITLE = "D-Day"
        const val ALPHA_TITLE = "Alpha"
        const val BRAVO_TITLE = "Bravo"
        const val CHARLIE_TITLE = "Charlie"
        const val DELTA_TITLE = "Delta"
        const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val KOREAN_FILTER_DESCRIPTION = "태그 필터"
        const val DEFAULT_FILTER_DESCRIPTION = "Filter by tag"
        const val DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION = "Tag filter applied"
        const val KOREAN_ADD_DESCRIPTION = "디데이 추가"
        const val DEFAULT_ADD_DESCRIPTION = "Add D-Day"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        const val TODAY_CARD_TEXT = "D-Day"
        const val WEDDING_TITLE = "결혼"
        const val EXAM_TITLE = "시험"
        const val DESCRIPTION = "디데이 설명"
        const val REPEAT_DAYS = 100
        const val END_DATE_OFFSET_DAYS = 365
        const val TIMEOUT_MILLIS = 5_000L
    }
}
