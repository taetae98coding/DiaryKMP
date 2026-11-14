package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.ui.RoutineTestTheme
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordEffect
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordUiState
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffoldFilterUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.loadedRoutinePagingData
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.routine.ui.routineEntry
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * 제품 routineEntry로 NavDisplay를 구성해 RoutineDetail 화면의 진입과 떠남을 확인한다. 다른 기능 모듈의 화면은 대역 키로 대신한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h3000dp")
class RoutineDetailNavDisplayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val routine: Routine = Arb.routine(isDeleted = false, detail = routineDetailOf()).next()
    private val recordList: List<RoutineRecord> =
        listOf(
            routineDetailRecordOf(routineId = routine.id, date = LocalDate(2026, 4, 1), amount = 750),
            routineDetailRecordOf(routineId = routine.id, date = LocalDate(2026, 3, 12), amount = 500),
            routineDetailRecordOf(routineId = routine.id, date = LocalDate(2026, 3, 5), amount = 250),
        )
    private val tagA: Tag = Arb.tag(title = TAG_A_TITLE).next().copy(isDeleted = false)
    private val backStack = NavBackStack<ScreenNavKey>()
    private val detailViewModelList = mutableListOf<RoutineDetailViewModel>()
    private val homeViewModelList = mutableListOf<RoutineHomeViewModel>()
    private val syncViewModelList = mutableListOf<SyncRefreshViewModel>()
    private val homeRoutineList = MutableStateFlow(listOf(routine))
    private val homePagingData = MutableStateFlow(loadedRoutinePagingData(listOf(RoutineSummary(routine = routine, cycleAmount = 0L))))
    private val calendarUiState = MutableStateFlow(RoutineHomeCalendarUiState())

    // 루틴과 태그의 저장된 연결이다.
    private val storedTagUiState = MutableStateFlow(EntityTagInputUiState())

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-adadec35-debe-400d-bc82-7895002dc187 RoutineHome 목록에서 진입해 삭제하면 RoutineHome으로 돌아간다`() {
        enterFromHomeList()

        clickDelete()

        backStack.toList() shouldBe listOf(RoutineHomeNavKey)
        composeRule.onAllNodesWithText(DEFAULT_HOME_TITLE).fetchSemanticsNodes().isNotEmpty() shouldBe true
    }

    @Test
    fun `TC-adadec35-debe-400d-bc82-7895002dc187 RoutineHome 달력의 아이템에서 진입해 삭제하면 RoutineHome으로 돌아간다`() {
        val calendarRecordList = calendarRecordList()
        enterHomeCalendar(calendarRecordList)
        clickCalendarRecord(calendarRecordList.first())

        clickDelete()

        backStack.toList() shouldBe listOf(RoutineHomeNavKey)
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE).assertIsDisplayed()
    }

    @Test
    fun `TC-adadec35-debe-400d-bc82-7895002dc187 TagDetail 루틴 탭에서 진입해 삭제하면 그 TagDetail로 돌아간다`() {
        backStack.add(TagDetailNavKey(id = tagA.id))
        setNavDisplay()
        composeRule.onNodeWithText(TAG_DETAIL_ROUTINE_TAB).performClick()
        composeRule.waitForIdle()
        composeRule.runOnIdle { backStack.add(RoutineDetailNavKey(id = routine.id)) }
        composeRule.waitForIdle()

        clickDelete()

        backStack.toList() shouldBe listOf(TagDetailNavKey(id = tagA.id))
        composeRule.onNodeWithText("$TAG_DETAIL_SELECTED_TAB_PREFIX$TAG_DETAIL_ROUTINE_TAB").assertIsDisplayed()
    }

    @Test
    fun `TC-adadec35-debe-400d-bc82-7895002dc187 SearchHome에서 진입해 삭제하면 SearchHome으로 돌아간다`() {
        backStack.addAll(listOf(RoutineDetailSearchHomeStandInNavKey, RoutineDetailNavKey(id = routine.id)))
        setNavDisplay()

        clickDelete()

        backStack.toList() shouldBe listOf(RoutineDetailSearchHomeStandInNavKey)
        composeRule.onNodeWithText(SEARCH_CONTENT).assertIsDisplayed()
    }

    private fun clickDelete() {
        composeRule.onNodeWithContentDescription(ROUTINE_DETAIL_DEFAULT_DELETE).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(isDialog()).assertDoesNotExist()
        detailViewModelList.forEach { viewModel -> verify(exactly = 1) { viewModel.delete() } }
    }

    @Test
    fun `TC-bc5bf049-06a6-45b1-99da-673a1959883e 상세에서 삭제해 돌아온 RoutineHome에는 삭제 안내와 실행 취소가 없다`() {
        enterFromHomeList()

        clickDelete()

        backStack.toList() shouldBe listOf(RoutineHomeNavKey)
        composeRule.onNode(card()).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO).assertDoesNotExist()
        homeViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.delete(id = any()) } }
    }

    @Test
    fun `TC-408068ef-ac2f-42eb-991b-b2921b333539 RoutineDetail에 진입해 기록 내역을 봐도 서버 동기화를 요청하지 않는다`() {
        enterFromHomeList()

        assertStoredContent()

        syncViewModelList.forEach { viewModel -> verify(exactly = 0) { viewModel.refresh() } }
    }

    @Test
    fun `TC-7920ba3e-c7e4-4b73-b646-c5747a66ffc7 달력의 어느 날짜 아이템에서 들어와도 같은 루틴의 같은 내용을 보여 준다`() {
        val calendarRecordList = calendarRecordList()
        enterHomeCalendar(calendarRecordList)

        val recordInputList =
            calendarRecordList.map { record ->
                clickCalendarRecord(record)

                assertStoredContent()
                val recordInput = composeRule.routineDetailRecordDateText() to composeRule.routineDetailRecordAmountInput().routineDetailEditableText()

                composeRule.onNodeWithContentDescription(ROUTINE_DETAIL_DEFAULT_NAVIGATE_UP).performClick()
                composeRule.waitForIdle()
                backStack.toList() shouldBe listOf(RoutineHomeNavKey)
                recordInput
            }

        recordInputList.size shouldBe calendarRecordList.size
        recordInputList.distinct() shouldBe listOf(recordInputList.first())
        recordInputList.first().second shouldBe routine.detail.amountPerRecord.toString()
    }

    @Test
    fun `TC-580cb060-7017-464b-925b-0505c43c30db 연결한 태그를 누르면 TagDetail로 이동하고 뒤로가면 이 루틴 상세로 돌아온다`() {
        storedTagUiState.value = EntityTagInputUiState(tagList = listOf(tagA))
        backStack.addAll(listOf(RoutineDetailSearchHomeStandInNavKey, RoutineDetailNavKey(id = routine.id)))
        setNavDisplay()

        composeRule.onNode(routineDetailTagChip(TAG_A_TITLE)).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        backStack.toList() shouldBe listOf(RoutineDetailSearchHomeStandInNavKey, RoutineDetailNavKey(id = routine.id), TagDetailNavKey(id = tagA.id))
        composeRule.onNodeWithText(TAG_DETAIL_CONTENT).assertIsDisplayed()

        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(RoutineDetailSearchHomeStandInNavKey, RoutineDetailNavKey(id = routine.id))
        composeRule.routineDetailTitleInput().routineDetailEditableText() shouldBe routine.detail.title
    }

    private fun enterFromHomeList() {
        backStack.add(RoutineHomeNavKey)
        setNavDisplay()
        composeRule.waitUntil(timeoutMillis = PAGING_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(routine.detail.title).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNode(card()).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id))
    }

    // RoutineHome을 달력 보기로 바꾸고 오늘이 속한 달에 그 루틴의 기록을 날짜별로 둔다.
    private fun enterHomeCalendar(calendarRecordList: List<CalendarRoutineRecord>) {
        calendarUiState.value = RoutineHomeCalendarUiState(recordList = calendarRecordList)
        backStack.add(RoutineHomeNavKey)
        setNavDisplay()
        composeRule.onNodeWithContentDescription(DEFAULT_CALENDAR_VIEW_MODE).performClick()
        composeRule.waitForIdle()
    }

    private fun clickCalendarRecord(record: CalendarRoutineRecord) {
        composeRule
            .onNode(hasTestTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG) and hasContentDescription("${record.title} ${record.amount}ml, routine"))
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id))
    }

    // 달력이 처음 보여 주는 오늘이 속한 달의 세 날짜에 날짜별 합계를 둔다. 합계가 달라 아이템을 구분할 수 있다.
    private fun calendarRecordList(): List<CalendarRoutineRecord> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        return CALENDAR_DAY_LIST.mapIndexed { index, day ->
            CalendarRoutineRecord(
                routineId = routine.id,
                title = routine.detail.title,
                color = routine.detail.color,
                date = LocalDate(year = today.year, month = today.month, day = day),
                amount = (index + 1) * CALENDAR_AMOUNT_STEP,
                unit = routine.detail.unit,
            )
        }
    }

    private fun assertStoredContent() {
        composeRule.routineDetailTitleInput().routineDetailEditableText() shouldBe routine.detail.title
        composeRule.routineDetailDescriptionInput().routineDetailEditableText() shouldBe routine.detail.description
        composeRule.routineDetailDisplayedColorHex() shouldBe routine.detail.color.routineDetailColorHex()
        composeRule.routineDetailDateButton(routine.detail.startDate).assertExists()
        composeRule.routineDetailSelectedCycle() shouldBe ROUTINE_DETAIL_DEFAULT_CYCLE_WEEKLY
        composeRule.routineDetailInputtedUnit() shouldBe ROUTINE_DETAIL_UNIT_MILLILITER
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe "2000"
        composeRule.routineDetailAmountPerRecordInput().routineDetailEditableText() shouldBe "250"
        composeRule.waitUntil(timeoutMillis = PAGING_TIMEOUT_MILLIS) { composeRule.routineDetailRecordRowList().size == recordList.size }
        composeRule.routineDetailRecordRowList() shouldBe
            listOf(
                routineDetailDisplayText(LocalDate(2026, 4, 1)) to "750ml",
                routineDetailDisplayText(LocalDate(2026, 3, 12)) to "500ml",
                routineDetailDisplayText(LocalDate(2026, 3, 5)) to "250ml",
            )
    }

    private fun card(): SemanticsMatcher = hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(routine.detail.title)

    private fun setNavDisplay() {
        composeRule.setContent {
            NavDisplayTestHost {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            entry<RoutineDetailSearchHomeStandInNavKey> { Text(text = SEARCH_CONTENT) }
                            entry<TagDetailNavKey> { TagDetailStandIn() }
                            routineEntry(backStack = backStack, homeReselectEvent = emptyFlow())
                        },
                )
            }
        }
        composeRule.waitForIdle()
    }

    // TagDetail 화면을 대신한다. 탭 선택은 그 화면이 저장하는 상태이므로 같은 방식으로 저장해 돌아왔을 때의 탭을 드러낸다.
    @Composable
    private fun TagDetailStandIn() {
        var selectedTab by rememberSaveable { mutableStateOf(TAG_DETAIL_MEMO_TAB) }

        Column {
            Text(text = TAG_DETAIL_CONTENT)
            Text(text = "$TAG_DETAIL_SELECTED_TAB_PREFIX$selectedTab")
            TextButton(onClick = { selectedTab = TAG_DETAIL_ROUTINE_TAB }) { Text(text = TAG_DETAIL_ROUTINE_TAB) }
        }
    }

    @Composable
    private fun NavDisplayTestHost(content: @Composable () -> Unit) {
        val viewModelStoreOwner =
            remember {
                object : ViewModelStoreOwner {
                    override val viewModelStore: ViewModelStore = ViewModelStore()
                }
            }

        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            KoinApplication(configuration = koinConfiguration { modules(navDisplayViewModelModule()) }) {
                RoutineTestTheme(content = content)
            }
        }
    }

    // 화면을 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    private fun navDisplayViewModelModule() =
        module {
            factory {
                mockk<RoutineHomeViewModel>(relaxed = true)
                    .apply {
                        every { routinePagingData } returns homePagingData
                        every { filterUiState } returns MutableStateFlow(RoutineHomeScaffoldFilterUiState())
                        every { effect } returns emptyFlow()
                        every { recordEffect } returns emptyFlow()
                    }.also(homeViewModelList::add)
            }
            factory {
                mockk<RoutineHomeCalendarViewModel>(relaxed = true).apply { every { uiState } returns calendarUiState }
            }
            factory {
                mockk<RoutineHomeHolidayViewModel>(relaxed = true).apply { every { uiState } returns MutableStateFlow(RoutineHomeHolidayUiState()) }
            }
            factory {
                mockk<SyncRefreshViewModel>(relaxed = true)
                    .apply { every { uiState } returns MutableStateFlow(SyncRefreshUiState()) }
                    .also(syncViewModelList::add)
            }
            factory { parameters ->
                val id = parameters.get<Uuid>()
                val target = homeRoutineList.value.first { value -> value.id == id }
                val effectChannel = Channel<RoutineDetailEffect>(capacity = Channel.BUFFERED)
                mockk<RoutineDetailViewModel>(relaxed = true)
                    .apply {
                        every { uiState } returns MutableStateFlow(RoutineDetailUiState.Content(id = id, detail = target.detail, isFinished = target.isFinished))
                        every { effect } returns effectChannel.receiveAsFlow()
                        // 삭제에 성공하면 기기에서 삭제 상태가 되어 RoutineHome 목록에서 빠진다.
                        every { delete() } answers {
                            homeRoutineList.update { list -> list.filterNot { value -> value.id == id } }
                            homePagingData.value = loadedRoutinePagingData(homeRoutineList.value.map { value -> RoutineSummary(routine = value, cycleAmount = 0L) })
                            effectChannel.trySend(RoutineDetailEffect.DeleteSucceeded)
                        }
                    }.also(detailViewModelList::add)
            }
            factory {
                mockk<RoutineDetailRecordViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(RoutineDetailRecordUiState())
                    every { recordPagingData } returns MutableStateFlow(routineDetailRecordPagingDataOf(recordList))
                    every { effect } returns Channel<RoutineDetailRecordEffect>().receiveAsFlow()
                }
            }
            factory {
                mockk<EntityDetailTagViewModel>(relaxed = true).apply {
                    every { uiState } returns storedTagUiState
                    every { tagPagingData } returns MutableStateFlow(PagingData.from(listOf(tagA)))
                    every { selectableTagPagingData } returns MutableStateFlow(PagingData.from(listOf(tagA)))
                }
            }
        }

    private companion object {
        const val DEFAULT_HOME_TITLE = "Routine"
        const val DEFAULT_CALENDAR_VIEW_MODE = "Show calendar"
        const val DEFAULT_LIST_VIEW_MODE = "Show list"
        const val DEFAULT_DELETED_MESSAGE = "Routine deleted."
        const val DEFAULT_UNDO = "Undo"
        const val SEARCH_CONTENT = "SearchHomeContent"
        const val TAG_DETAIL_CONTENT = "TagDetailContent"
        const val TAG_DETAIL_MEMO_TAB = "MemoTab"
        const val TAG_DETAIL_ROUTINE_TAB = "RoutineTab"
        const val TAG_DETAIL_SELECTED_TAB_PREFIX = "Selected: "
        const val TAG_A_TITLE = "RoutineDetailNavTagA"
        const val CALENDAR_AMOUNT_STEP = 250L
        const val PAGING_TIMEOUT_MILLIS = 5_000L
        val CALENDAR_DAY_LIST = listOf(5, 12, 19)
    }
}

// 다른 기능 모듈의 화면은 이 모듈의 의존이 아니므로 대역 키로 대신한다.
private data object RoutineDetailSearchHomeStandInNavKey : ScreenNavKey {
    override val screenName: String get() = "SearchHome"
}
