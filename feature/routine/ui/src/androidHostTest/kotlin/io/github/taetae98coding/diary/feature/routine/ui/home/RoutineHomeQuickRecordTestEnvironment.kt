package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.activity.ComponentDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineDetail
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_VIEW_SCOPE
import io.github.taetae98coding.diary.domain.routine.usecase.AddQuickRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageViewScopeRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineFinishedListNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.record.ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineViewScopeUseCase
import io.github.taetae98coding.diary.feature.routine.ui.unfinishedRoutineExistsUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.robolectric.shadows.ShadowDialog
import kotlin.time.Clock
import kotlin.uuid.Uuid

// 빠른 기록으로 저장된 기록이다. 실행 취소가 저장되면 삭제 상태가 된다.
internal data class RoutineHomeQuickRecordSavedRecord(
    val id: Uuid,
    val routineId: Uuid,
    val date: LocalDate,
    val amount: Int,
    val isDeleted: Boolean = false,
)

// 목록 보기의 빠른 기록, 양 입력 창, 기록 안내와 오늘 기준을 검증하는 테스트가 함께 쓰는 환경이다.
// 화면과 RoutineHomeViewModel은 제품 코드를 쓰고, UseCase mock이 기기에 저장된 루틴과 기록을 흉내 낸다.
// RoutineHome을 떠났다 돌아오는 경로는 다른 화면을 대역으로 둔 NavDisplay로 지난다.
internal class RoutineHomeQuickRecordTestEnvironment {
    val backStack: NavBackStack<ScreenNavKey> = NavBackStack(RoutineHomeQuickRecordOtherNavKey, RoutineHomeNavKey)
    val detailNavigationList: MutableList<Uuid> = mutableListOf()
    val calendarUiState: MutableStateFlow<RoutineHomeCalendarUiState> = MutableStateFlow(RoutineHomeCalendarUiState())
    val calendarViewModel: RoutineHomeCalendarViewModel = routineHomeCalendarViewModel().apply { every { uiState } returns calendarUiState }
    val holidayViewModel: RoutineHomeHolidayViewModel = routineHomeHolidayViewModel()

    // 화면 재생성에는 ViewModel이 유지되고, 시스템이 앱을 정리했다가 다시 만들면 새로 만들어진다.
    var viewModelStoreOwner: ViewModelStoreOwner by mutableStateOf(routineHomeQuickRecordViewModelStoreOwner())

    var isRecordSaveSuccess: Boolean = true
    var isUndoSaveSuccess: Boolean = true
    var isPageSuccess: Boolean = true
    var isFinishSaveSuccess: Boolean = true
    var isRestartSaveSuccess: Boolean = true

    // 참이면 목록이 처음 불러오는 중에 머문다.
    var isPageLoading: Boolean = false

    // 필터에 선택해 둔 태그다. ViewModel이 만들어질 때의 값을 쓴다.
    var filterTagList: List<Tag> = emptyList()

    // 계정에 저장된 보기 범위 선택이다. 바꾸면 열어 둔 목록이 그 범위로 다시 조회된다.
    val viewScope: MutableStateFlow<RoutineViewScope> = MutableStateFlow(ROUTINE_DEFAULT_VIEW_SCOPE)

    // 보기 범위와 오늘로 조회했을 때 돌려줄 루틴의 제목을 순서대로 테스트가 정해 둔다. 정해 두지 않은 조회는 저장된 루틴을 넣은 순서대로 돌려준다.
    private val pageTitleListMap = MutableStateFlow<Map<Pair<RoutineViewScope, LocalDate>, List<String>>>(emptyMap())

    val syncUiState: MutableStateFlow<SyncRefreshUiState> = MutableStateFlow(SyncRefreshUiState())

    // 주요 날짜를 강조하는 글자색이다. 테마 안에서 정해지므로 화면을 구성할 때 받아 둔다.
    var onPrimaryColor: Color = Color.Unspecified
        private set

    // 루틴마다 기록 저장이 끝나는 시점을 테스트가 정한다. 없으면 바로 끝난다.
    val recordGateMap: MutableMap<Uuid, CompletableDeferred<Unit>> = mutableMapOf()

    // 오늘마다 목록 정렬이 달라지는 테스트는 그날의 제목 순서를 정해 둔다. 없으면 넣은 순서다.
    var titleOrderByToday: Map<LocalDate, List<String>> = emptyMap()

    // 오늘이 바뀌면 이번 주기가 달라지는 테스트는 그날의 제목별 주기 합계를 정해 둔다. 없으면 저장된 합계다.
    var cycleAmountByToday: Map<LocalDate, Map<String, Long>> = emptyMap()

    val pageTodayList: MutableList<LocalDate> = mutableListOf()
    val recordParameterList: MutableList<AddQuickRoutineRecordUseCase.Parameter> = mutableListOf()
    val undoRecordIdList: MutableList<Uuid> = mutableListOf()
    val restoreRoutineIdList: MutableList<Uuid> = mutableListOf()
    val finishRoutineIdList: MutableList<Uuid> = mutableListOf()
    val restartRoutineIdList: MutableList<Uuid> = mutableListOf()
    var finishedListNavigationCount: Int = 0
        private set

    private val summaryList = MutableStateFlow<List<RoutineSummary>>(emptyList())
    private val deletedSummaryMap = mutableMapOf<Uuid, RoutineSummary>()
    private val recordList = mutableListOf<RoutineHomeQuickRecordSavedRecord>()
    private var calendarRecordList: List<CalendarRoutineRecord> = emptyList()

    val savedRecordList: List<RoutineHomeQuickRecordSavedRecord>
        get() = recordList.toList()

    val storedSummaryList: List<RoutineSummary>
        get() = summaryList.value

    fun setPageTitleList(
        viewScope: RoutineViewScope,
        today: LocalDate,
        titleList: List<String>,
    ) {
        pageTitleListMap.update { map -> map + ((viewScope to today) to titleList) }
    }

    fun setSummaryList(list: List<RoutineSummary>) {
        summaryList.value = list
        publishCalendar()
    }

    fun addSummary(summary: RoutineSummary) {
        summaryList.update { list -> list + summary }
    }

    // 루틴 상세나 서버에서 내려받은 변경처럼 이 화면 밖에서 루틴의 정보가 바뀌어 저장된 것을 흉내 낸다. 삭제해 둔 루틴도 함께 바뀐다.
    fun updateDetail(
        id: Uuid,
        transform: (RoutineDetail) -> RoutineDetail,
    ) {
        fun RoutineSummary.updated(): RoutineSummary = if (routine.id == id) copy(routine = routine.copy(detail = transform(routine.detail))) else this

        deletedSummaryMap[id]?.let { summary -> deletedSummaryMap[id] = summary.updated() }
        summaryList.update { list -> list.map { summary -> summary.updated() } }
    }

    fun removeSummary(id: Uuid) {
        summaryList.update { list -> list.filterNot { summary -> summary.routine.id == id } }
        publishCalendar()
    }

    // 달력은 삭제되지 않은 루틴의 기록을 완료 여부와 관계없이 보여 준다.
    fun setCalendarRecordList(list: List<CalendarRoutineRecord>) {
        calendarRecordList = list
        publishCalendar()
    }

    // 루틴 상세처럼 이 화면 밖에서 완료 여부가 바뀌어 저장된 것을 흉내 낸다.
    fun setFinished(
        id: Uuid,
        isFinished: Boolean,
    ) {
        summaryList.update { list ->
            list.map { summary -> if (summary.routine.id == id) summary.copy(routine = summary.routine.copy(isFinished = isFinished)) else summary }
        }
    }

    private fun publishCalendar() {
        val idSet = summaryList.value.map { summary -> summary.routine.id }.toSet()

        calendarUiState.value = RoutineHomeCalendarUiState(recordList = calendarRecordList.filter { record -> record.routineId in idSet })
    }

    @Composable
    fun Content(navBackStack: NavBackStack<ScreenNavKey> = backStack) {
        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            DiaryTheme {
                onPrimaryColor = DiaryTheme.colorScheme.onPrimary

                NavDisplay(
                    backStack = navBackStack,
                    entryDecorators =
                        listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                    entryProvider =
                        entryProvider {
                            entry<RoutineHomeQuickRecordOtherNavKey> { Text(text = ROUTINE_HOME_QUICK_RECORD_OTHER_CONTENT) }
                            entry<RoutineDetailNavKey> { Text(text = ROUTINE_HOME_QUICK_RECORD_DETAIL_CONTENT) }
                            entry<RoutineAddNavKey> { Text(text = ROUTINE_HOME_QUICK_RECORD_ADD_CONTENT) }
                            entry<RoutineFinishedListNavKey> { Text(text = ROUTINE_HOME_QUICK_RECORD_FINISHED_LIST_CONTENT) }
                            entry<RoutineHomeNavKey> {
                                RoutineHomeScreen(
                                    navigateToAdd = { navBackStack.add(RoutineAddNavKey()) },
                                    navigateToDetail = { id ->
                                        detailNavigationList += id
                                        navBackStack.add(RoutineDetailNavKey(id = id))
                                    },
                                    navigateToFilter = {},
                                    navigateToFinishedList = {
                                        finishedListNavigationCount += 1
                                        navBackStack.add(RoutineFinishedListNavKey)
                                    },
                                    componentVisibleProvider = { RoutineHomeScaffoldComponentVisible() },
                                    homeReselectEvent = emptyFlow(),
                                    routineViewModel = viewModel { routineViewModel() },
                                    calendarViewModel = calendarViewModel,
                                    holidayViewModel = holidayViewModel,
                                    syncViewModel = remember { syncViewModel() },
                                )
                            }
                        },
                )
            }
        }
    }

    private fun syncViewModel(): SyncRefreshViewModel =
        mockk<SyncRefreshViewModel>(relaxed = true).apply {
            every { uiState } returns syncUiState
        }

    private fun routineViewModel(): RoutineHomeViewModel {
        val getRoutineFilterUseCase = mockk<GetRoutineFilterUseCase>()
        every { getRoutineFilterUseCase() } returns flowOf(Result.success(filterTagList))
        val getRoutineFilterTagIdUseCase = mockk<GetRoutineFilterTagIdUseCase>()
        every { getRoutineFilterTagIdUseCase() } returns flowOf(Result.success(filterTagList.map { tag -> tag.id }.toSet()))

        return RoutineHomeViewModel(
            getRoutineFilterUseCase = getRoutineFilterUseCase,
            getRoutineFilterTagIdUseCase = getRoutineFilterTagIdUseCase,
            getRoutineViewScopeUseCase = routineViewScopeUseCase(viewScope = viewScope),
            getUnfinishedRoutineExistsUseCase = unfinishedRoutineExistsUseCase(exists = summaryList.map { list -> list.any { summary -> !summary.routine.isFinished } }),
            pageViewScopeRoutineUseCase = pageRoutineUseCase(),
            finishRoutineUseCase = finishRoutineUseCase(),
            restartRoutineUseCase = restartRoutineUseCase(),
            deleteRoutineUseCase = deleteRoutineUseCase(),
            restoreRoutineUseCase = restoreRoutineUseCase(),
            addQuickRoutineRecordUseCase = addQuickRoutineRecordUseCase(),
            deleteRoutineRecordUseCase = deleteRoutineRecordUseCase(),
        )
    }

    private fun pageRoutineUseCase(): PageViewScopeRoutineUseCase {
        val useCase = mockk<PageViewScopeRoutineUseCase>()
        every { useCase(parameter = any()) } answers {
            val today = firstArg<LocalDate>()
            pageTodayList += today
            combine(summaryList, viewScope, pageTitleListMap) { list, scope, titleListMap ->
                if (isPageLoading) {
                    Result.success(loadingRoutineHomeQuickRecordPagingData())
                } else if (isPageSuccess) {
                    val titleOrder = titleOrderByToday[today]
                    val cycleAmountByTitle = cycleAmountByToday[today].orEmpty()
                    val unfinished =
                        list
                            .filterNot { summary -> summary.routine.isFinished }
                            .map { summary -> cycleAmountByTitle[summary.routine.detail.title]?.let { amount -> summary.copy(cycleAmount = amount) } ?: summary }
                    val ordered = if (titleOrder == null) unfinished else unfinished.sortedBy { summary -> titleOrder.indexOf(summary.routine.detail.title) }
                    val pageTitleList = titleListMap[scope to today]
                    val paged = pageTitleList?.mapNotNull { title -> unfinished.firstOrNull { summary -> summary.routine.detail.title == title } } ?: ordered
                    Result.success(routineHomeQuickRecordPagingDataOf(paged))
                } else {
                    Result.failure(IllegalStateException(SAVE_FAILURE_MESSAGE))
                }
            }
        }

        return useCase
    }

    // 기록이 저장되면 그 루틴의 주기 합계가 기록한 양만큼 늘어 목록이 다시 조회되는 것과 같게 한다.
    private fun addQuickRoutineRecordUseCase(): AddQuickRoutineRecordUseCase {
        val useCase = mockk<AddQuickRoutineRecordUseCase>()
        coEvery { useCase(parameter = any()) } coAnswers {
            val parameter = firstArg<AddQuickRoutineRecordUseCase.Parameter>()
            recordParameterList += parameter
            recordGateMap[parameter.routineId]?.await()
            val summary = summaryList.value.firstOrNull { summary -> summary.routine.id == parameter.routineId }

            if (!isRecordSaveSuccess || summary == null) {
                Result.failure(IllegalStateException(SAVE_FAILURE_MESSAGE))
            } else {
                val amount = parameter.amount ?: summary.routine.detail.amountPerRecord
                val record = RoutineHomeQuickRecordSavedRecord(id = Arb.kotlinUuid().next(), routineId = parameter.routineId, date = parameter.today, amount = amount)
                recordList += record
                addCycleAmount(routineId = parameter.routineId, amount = amount.toLong())
                Result.success(record.id)
            }
        }

        return useCase
    }

    private fun deleteRoutineRecordUseCase(): DeleteRoutineRecordUseCase {
        val useCase = mockk<DeleteRoutineRecordUseCase>()
        coEvery { useCase(parameter = any()) } coAnswers {
            val recordId = firstArg<Uuid>()
            undoRecordIdList += recordId
            val index = recordList.indexOfFirst { record -> record.id == recordId && !record.isDeleted }

            if (!isUndoSaveSuccess || index < 0) {
                Result.failure(IllegalStateException(SAVE_FAILURE_MESSAGE))
            } else {
                val record = recordList[index]
                recordList[index] = record.copy(isDeleted = true)
                addCycleAmount(routineId = record.routineId, amount = -record.amount.toLong())
                Result.success(1)
            }
        }

        return useCase
    }

    private fun finishRoutineUseCase(): FinishRoutineUseCase {
        val useCase = mockk<FinishRoutineUseCase>()
        coEvery { useCase(parameter = any()) } coAnswers {
            val id = firstArg<Uuid>()
            finishRoutineIdList += id

            if (isFinishSaveSuccess) {
                setFinished(id = id, isFinished = true)
                Result.success(1)
            } else {
                Result.failure(IllegalStateException(SAVE_FAILURE_MESSAGE))
            }
        }

        return useCase
    }

    private fun restartRoutineUseCase(): RestartRoutineUseCase {
        val useCase = mockk<RestartRoutineUseCase>()
        coEvery { useCase(parameter = any()) } coAnswers {
            val id = firstArg<Uuid>()
            restartRoutineIdList += id

            if (isRestartSaveSuccess) {
                setFinished(id = id, isFinished = false)
                Result.success(1)
            } else {
                Result.failure(IllegalStateException(SAVE_FAILURE_MESSAGE))
            }
        }

        return useCase
    }

    private fun deleteRoutineUseCase(): DeleteRoutineUseCase {
        val useCase = mockk<DeleteRoutineUseCase>()
        coEvery { useCase(parameter = any()) } coAnswers {
            val id = firstArg<Uuid>()
            summaryList.value.firstOrNull { summary -> summary.routine.id == id }?.let { summary -> deletedSummaryMap[id] = summary }
            summaryList.update { list -> list.filterNot { summary -> summary.routine.id == id } }
            publishCalendar()
            Result.success(1)
        }

        return useCase
    }

    private fun restoreRoutineUseCase(): RestoreRoutineUseCase {
        val useCase = mockk<RestoreRoutineUseCase>()
        coEvery { useCase(parameter = any()) } coAnswers {
            val id = firstArg<Uuid>()
            restoreRoutineIdList += id
            deletedSummaryMap.remove(id)?.let { summary -> summaryList.update { list -> list + summary } }
            publishCalendar()
            Result.success(1)
        }

        return useCase
    }

    private fun addCycleAmount(
        routineId: Uuid,
        amount: Long,
    ) {
        summaryList.update { list ->
            list.map { summary -> if (summary.routine.id == routineId) summary.copy(cycleAmount = summary.cycleAmount + amount) else summary }
        }
    }
}

// RoutineHome 밖의 화면을 대신한다. 다른 기능의 화면 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
internal data object RoutineHomeQuickRecordOtherNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}

internal const val ROUTINE_HOME_QUICK_RECORD_OTHER_CONTENT: String = "OtherContent"
internal const val ROUTINE_HOME_QUICK_RECORD_DETAIL_CONTENT: String = "RoutineDetailContent"
internal const val ROUTINE_HOME_QUICK_RECORD_ADD_CONTENT: String = "RoutineAddContent"
internal const val ROUTINE_HOME_QUICK_RECORD_FINISHED_LIST_CONTENT: String = "RoutineFinishedListContent"

internal fun routineHomeQuickRecordViewModelStoreOwner(): ViewModelStoreOwner =
    object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }

// 카드 문구를 정하는 값은 테스트가 정하고 나머지는 임의로 채운다. 제목은 화면에서 글자로 찾으므로 테스트가 정한다.
internal fun routineHomeQuickRecordSummary(
    title: String,
    startDate: LocalDate,
    cycle: RoutineCycle = RoutineCycle.DAILY,
    unit: String = "ml",
    goalAmount: Int? = null,
    amountPerRecord: Int = 1,
    cycleAmount: Long = 0L,
    isFinished: Boolean = false,
    transform: (RoutineDetail) -> RoutineDetail = { detail -> detail },
): RoutineSummary {
    val detail =
        Arb
            .routineDetail(startDate = startDate, cycle = cycle, goalAmount = goalAmount)
            .next()
            .copy(title = title, unit = unit, amountPerRecord = amountPerRecord)

    return RoutineSummary(
        routine = Arb.routine(isDeleted = false, detail = transform(detail), isFinished = isFinished).next(),
        cycleAmount = cycleAmount,
    )
}

internal fun routineHomeQuickRecordPagingDataOf(list: List<RoutineSummary>): PagingData<RoutineSummary> =
    PagingData.from(
        data = list,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun loadingRoutineHomeQuickRecordPagingData(): PagingData<RoutineSummary> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

// 카드 문구와 빠른 기록의 날짜는 기기의 오늘로 정해지므로, 기기 시계를 그 날의 정오로 맞춘다.
internal fun setRoutineHomeQuickRecordToday(date: LocalDate) {
    mockkObject(Clock.System)
    every { Clock.System.now() } returns LocalDateTime(date, LocalTime(hour = 12, minute = 0)).toInstant(TimeZone.currentSystemDefault())
}

internal fun releaseRoutineHomeQuickRecordToday() {
    unmockkObject(Clock.System)
}

internal fun routineHomeQuickRecordCard(title: String): SemanticsMatcher = hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(title)

internal fun routineHomeQuickRecordCard(
    title: String,
    vararg cardText: String,
): SemanticsMatcher = cardText.fold(routineHomeQuickRecordCard(title)) { matcher, text -> matcher and hasText(text) }

internal fun routineHomeQuickRecordButton(title: String): SemanticsMatcher = hasTestTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG) and hasAnyAncestor(routineHomeQuickRecordCard(title))

internal fun routineAmountDialogField(): SemanticsMatcher = hasSetTextAction() and hasAnyAncestor(isDialog())

internal fun routineAmountDialogButton(text: String): SemanticsMatcher = hasText(text) and hasAnyAncestor(isDialog())

internal fun SemanticsNodeInteractionsProvider.routineAmountDialogAmount(): String =
    onNode(routineAmountDialogField())
        .fetchSemanticsNode()
        .config
        .getOrNull(SemanticsProperties.EditableText)
        ?.text
        .orEmpty()

internal fun SemanticsNodeInteractionsProvider.routineHomeQuickRecordCardTextList(title: String): List<String> =
    onNode(routineHomeQuickRecordCard(title))
        .fetchSemanticsNode()
        .config
        .getOrNull(SemanticsProperties.Text)
        .orEmpty()
        .map { text -> text.text }

// 양 입력 창 밖으로 벗어나는 것과 같게 창의 뒤로가기를 실행한다.
internal fun dismissRoutineAmountDialogOutside() {
    (ShadowDialog.getLatestDialog() as ComponentDialog).onBackPressedDispatcher.onBackPressed()
}

internal fun ComposeTestRule.waitUntilRoutineHomeQuickRecordNode(matcher: SemanticsMatcher) {
    waitUntil(timeoutMillis = ROUTINE_HOME_QUICK_RECORD_TIMEOUT_MILLIS) { onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
}

internal fun ComposeTestRule.waitUntilRoutineHomeQuickRecordNodeGone(matcher: SemanticsMatcher) {
    waitUntil(timeoutMillis = ROUTINE_HOME_QUICK_RECORD_TIMEOUT_MILLIS) { onAllNodes(matcher).fetchSemanticsNodes().isEmpty() }
}

internal fun ComposeTestRule.clickRoutineHomeQuickRecord(title: String) {
    onNode(routineHomeQuickRecordButton(title)).performClick()
    waitForIdle()
}

internal fun ComposeTestRule.longPressRoutineHomeQuickRecord(title: String) {
    onNode(routineHomeQuickRecordButton(title)).performTouchInput { longClick() }
    waitForIdle()
}

internal const val ROUTINE_HOME_QUICK_RECORD_TIMEOUT_MILLIS: Long = 5_000L
internal const val ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION: String = "실행 취소"
internal const val ROUTINE_HOME_QUICK_RECORD_DELETED_MESSAGE: String = "루틴이 삭제되었습니다."
internal const val ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION: String = "달력으로 보기"
internal const val ROUTINE_HOME_QUICK_RECORD_LIST_VIEW_DESCRIPTION: String = "목록으로 보기"
internal const val ROUTINE_AMOUNT_DIALOG_CONFIRM: String = "확인"
internal const val ROUTINE_AMOUNT_DIALOG_CANCEL: String = "취소"
internal const val ROUTINE_HOME_QUICK_RECORD_AFTER_SNACKBAR_DISMISS_MILLIS: Long = 11_000L

// 스낵바는 안내를 읽어 주도록 live region으로 놓이므로, 기록 안내와 실패 안내가 모두 없는지는 그 수로 판정한다.
internal fun SemanticsNodeInteractionsProvider.routineHomeQuickRecordSnackbarCount(): Int = onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion)).fetchSemanticsNodes().size

// 같은 글을 표시하는 노드 중 주요 날짜를 강조하는 글자색을 쓰는 노드의 수다. 오늘로 이동 동작도 오늘의 일 숫자를 표시하므로 색으로 가른다.
internal fun ComposeTestRule.routineHomeQuickRecordPrimaryDateCount(
    day: Int,
    primaryColor: Color,
): Int {
    val nodes = onAllNodes(hasText(day.toString()), useUnmergedTree = true)

    return List(nodes.fetchSemanticsNodes().size) { index ->
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        nodes[index].performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action -> action(textLayoutResults) }
        textLayoutResults
            .single()
            .layoutInput.style.color
    }.count { color -> color == primaryColor }
}

private const val SAVE_FAILURE_MESSAGE = "저장 실패"
