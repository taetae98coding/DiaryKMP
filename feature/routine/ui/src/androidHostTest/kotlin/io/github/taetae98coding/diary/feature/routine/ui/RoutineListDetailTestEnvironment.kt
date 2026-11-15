@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.routine.ui

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.ui.NavDisplay
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.scene.BottomSheetSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineProgress
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineDetail
import io.github.taetae98coding.diary.core.testing.routine.routineRecord
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_VIEW_SCOPE
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.progress.progress
import io.github.taetae98coding.diary.domain.routine.progress.range
import io.github.taetae98coding.diary.domain.routine.progress.routineCurrentCycle
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagSelectionUiState
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.ui.add.RoutineAddEffect
import io.github.taetae98coding.diary.feature.routine.ui.add.RoutineAddUiState
import io.github.taetae98coding.diary.feature.routine.ui.add.RoutineAddViewModel
import io.github.taetae98coding.diary.feature.routine.ui.detail.RoutineDetailEffect
import io.github.taetae98coding.diary.feature.routine.ui.detail.RoutineDetailUiState
import io.github.taetae98coding.diary.feature.routine.ui.detail.RoutineDetailViewModel
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordEffect
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordUiState
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordViewModel
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffoldFilterUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.filter.RoutineHomeFilterUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.filter.RoutineHomeFilterViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.record.RoutineHomeRecord
import io.github.taetae98coding.diary.feature.routine.ui.home.record.RoutineHomeRecordEffect
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.uuid.Uuid

// 목록·상세 배치를 검증하는 테스트가 함께 쓰는 환경이다.
// 제품 routineEntry로 NavDisplay를 구성하고, 저장소 대신 ViewModel 대역이 기기에 저장된 루틴과 기록, 필터 선택을 흉내 낸다.
internal class RoutineListDetailTestEnvironment(
    private var today: LocalDate,
    vararg initialBackStack: ScreenNavKey = arrayOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey),
) {
    val backStack: NavBackStack<ScreenNavKey> = NavBackStack(*initialBackStack)
    val reselectEvent: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1)

    val homeViewModelList: MutableList<RoutineHomeViewModel> = mutableListOf()
    val addViewModelList: MutableList<RoutineAddViewModel> = mutableListOf()
    val addTagViewModelList: MutableList<EntityAddTagViewModel> = mutableListOf()
    val detailViewModelList: MutableList<RoutineDetailViewModel> = mutableListOf()

    var directiveOverride: PaneScaffoldDirective? by mutableStateOf(null)

    var tagList: List<Tag> = emptyList()
    var routineTagMap: Map<Uuid, Set<Uuid>> = emptyMap()

    private var routineList: List<Routine> = emptyList()

    // 넣은 순서대로 둔다. 같은 날짜에서는 나중에 넣은 기록이 먼저 보인다.
    private var recordList: List<RoutineRecord> = emptyList()
    private val selectedTagIdSet = MutableStateFlow<Set<Uuid>>(emptySet())

    // 계정에 저장된 보기 범위 선택이다. 화면과 ViewModel이 다시 만들어져도 남는다.
    private val viewScope = MutableStateFlow(ROUTINE_DEFAULT_VIEW_SCOPE)

    // 보기 범위 선택의 저장이 실패하도록 제어한다.
    var isViewScopeSaveFailing: Boolean = false

    // 보기 범위와 오늘로 조회했을 때 목록에 놓일 루틴의 제목을 순서대로 테스트가 정해 둔다. 정해 두지 않은 조회는 완료되지 않은 루틴을 모두 돌려준다.
    private var homeTitleListMap: Map<Pair<RoutineViewScope, LocalDate>, List<String>> = emptyMap()

    val storedViewScope: RoutineViewScope
        get() = viewScope.value

    // 기기에 저장된 루틴이다. 보기 범위에 맞지 않아 목록에 놓이지 않은 루틴도 담는다.
    val storedRoutineList: List<Routine>
        get() = routineList
    private val homePagingData = MutableStateFlow(routineListDetailPagingDataOf(emptyList<RoutineSummary>()))
    private val finishedPagingData = MutableStateFlow(routineListDetailPagingDataOf(emptyList<RoutineSummary>()))
    private val homeFilterUiState = MutableStateFlow(RoutineHomeScaffoldFilterUiState())
    private val filterUiState = MutableStateFlow(RoutineHomeFilterUiState())
    private val calendarUiState = MutableStateFlow(RoutineHomeCalendarUiState())
    private val detailUiStateMap = mutableMapOf<Uuid, MutableStateFlow<RoutineDetailUiState>>()
    private val recordPagingDataMap = mutableMapOf<Uuid, MutableStateFlow<PagingData<RoutineRecord>>>()

    val storedRecordList: List<RoutineRecord>
        get() = recordList

    fun setRoutineList(
        routineList: List<Routine>,
        recordList: List<RoutineRecord> = emptyList(),
    ) {
        this.routineList = routineList
        this.recordList = recordList
        publish()
    }

    fun setHomeTitleList(
        viewScope: RoutineViewScope,
        today: LocalDate,
        titleList: List<String>,
    ) {
        homeTitleListMap = homeTitleListMap + ((viewScope to today) to titleList)
        publish()
    }

    // 다른 경로에서 보기 범위 선택이 저장된 것과 같게 한다.
    fun setViewScope(viewScope: RoutineViewScope) {
        this.viewScope.value = viewScope
        publish()
    }

    // 상세 화면이나 서버에서 내려받은 변경처럼 다른 경로에서 루틴이 바뀌어 저장된 것과 같게 한다.
    fun updateRoutine(
        id: Uuid,
        transform: (Routine) -> Routine,
    ) {
        setRoutineList(routineList = routineList.map { routine -> if (routine.id == id) transform(routine) else routine }, recordList = recordList)
    }

    fun addRoutine(routine: Routine) {
        setRoutineList(routineList = routineList + routine, recordList = recordList)
    }

    // 저장된 루틴과 기록을 노출 기준과 필터, 목록 순서로 다시 조회한 것과 같게 목록과 달력, 상세를 갱신한다.
    private fun publish() {
        val selected = selectedTagIdSet.value
        val scope = viewScope.value
        val visible = routineList.filter { routine -> selected.isEmpty() || routineTagMap[routine.id].orEmpty().any(selected::contains) }
        val sort = compareBy<Routine> { routine -> routine.detail.progress(today = today).listOrder() }.thenBy { routine -> routine.detail.title }
        val cycle = routineCurrentCycle(today = today)

        val unfinished = visible.filterNot { routine -> routine.isFinished }.sortedWith(sort)
        val homeTitleList = homeTitleListMap[scope to today]

        homePagingData.value =
            routineListDetailPagingDataOf(
                (homeTitleList?.mapNotNull { title -> unfinished.firstOrNull { routine -> routine.detail.title == title } } ?: unfinished)
                    .map { routine ->
                        val range = cycle.range(cycle = routine.detail.cycle)
                        RoutineSummary(
                            routine = routine,
                            cycleAmount = recordList.filter { record -> record.routineId == routine.id && record.date in range }.sumOf { record -> record.amount.toLong() },
                        )
                    },
            )
        // 완료된 루틴 목록은 필터와 관계없이 완료된 루틴을 같은 순서로 보여 준다.
        finishedPagingData.value =
            routineListDetailPagingDataOf(
                routineList
                    .filter { routine -> routine.isFinished }
                    .sortedWith(sort)
                    .map { routine -> RoutineSummary(routine = routine, cycleAmount = 0L) },
            )
        calendarUiState.value =
            RoutineHomeCalendarUiState(
                recordList =
                    visible.flatMap { routine ->
                        recordList
                            .filter { record -> record.routineId == routine.id }
                            .groupBy { record -> record.date }
                            .map { (date, list) ->
                                CalendarRoutineRecord(
                                    routineId = routine.id,
                                    title = routine.detail.title,
                                    color = routine.detail.color,
                                    date = date,
                                    amount = list.sumOf { record -> record.amount.toLong() },
                                    unit = routine.detail.unit,
                                )
                            }
                    },
            )
        homeFilterUiState.value =
            RoutineHomeScaffoldFilterUiState(
                selectedTagIdSet = selected,
                storedTagIdSet = selected,
                viewScope = scope,
                hasUnfinishedRoutine = routineList.any { routine -> !routine.isFinished },
            )
        filterUiState.value = RoutineHomeFilterUiState(selectedTagIdSet = selected, viewScope = scope)
        detailUiStateMap.forEach { (id, uiState) ->
            routineList.firstOrNull { routine -> routine.id == id }?.let { routine -> uiState.value = RoutineDetailUiState.Content(id = id, detail = routine.detail, isFinished = routine.isFinished) }
        }
        recordPagingDataMap.forEach { (routineId, pagingData) -> pagingData.value = recordPagingDataOf(routineId = routineId) }
    }

    private fun recordPagingDataOf(routineId: Uuid): PagingData<RoutineRecord> =
        routineListDetailPagingDataOf(
            recordList
                .filter { record -> record.routineId == routineId }
                .asReversed()
                .sortedByDescending { record -> record.date },
        )

    private fun addRecord(
        routineId: Uuid,
        date: LocalDate,
        amount: Int,
    ): Uuid {
        val record = Arb.routineRecord(routineId = routineId).next().copy(date = date, amount = amount)
        recordList = recordList + record
        publish()
        return record.id
    }

    private fun removeRecord(recordId: Uuid) {
        recordList = recordList.filterNot { record -> record.id == recordId }
        publish()
    }

    // 배치를 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    fun module(): Module =
        module {
            factory { homeViewModel().also(homeViewModelList::add) }
            factory { calendarViewModel() }
            factory { routineHomeHolidayViewModel() }
            factory { routineSyncRefreshViewModel() }
            factory { filterViewModel() }
            factory { finishedListViewModel() }
            factory { addViewModel().also(addViewModelList::add) }
            factory { addTagViewModel().also(addTagViewModelList::add) }
            factory { parameters -> detailViewModel(id = parameters.get()).also(detailViewModelList::add) }
            factory { parameters -> recordViewModel(routineId = parameters.get()) }
            factory { detailTagViewModel() }
        }

    @Composable
    fun Content(
        navBackStack: NavBackStack<ScreenNavKey> = backStack,
        viewModelStoreOwner: ViewModelStoreOwner = remember { routineListDetailViewModelStoreOwner() },
    ) {
        val resultEventBus = remember { ResultEventBus() }

        CompositionLocalProvider(
            LocalViewModelStoreOwner provides viewModelStoreOwner,
            LocalResultEventBus provides resultEventBus,
        ) {
            KoinApplication(configuration = koinConfiguration { modules(module()) }) {
                DiaryTheme {
                    val holder = rememberListDetailPlaceholderStateHolder()

                    CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                        NavDisplay(
                            backStack = navBackStack,
                            sceneStrategies =
                                listOf(
                                    remember { BottomSheetSceneStrategy<ScreenNavKey>() },
                                    rememberDiaryListDetailSceneStrategy(
                                        directive = directiveOverride ?: calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()),
                                    ),
                                ),
                            entryDecorators =
                                listOf(
                                    rememberSaveableStateHolderNavEntryDecorator(),
                                    rememberViewModelStoreNavEntryDecorator(),
                                    rememberListDetailPlaceholderNavEntryDecorator(holder),
                                ),
                            entryProvider =
                                entryProvider {
                                    entry<RoutineListDetailDefaultNavKey> { Text(text = ROUTINE_LIST_DETAIL_DEFAULT_CONTENT) }
                                    entry<RoutineListDetailOtherNavKey> { Text(text = ROUTINE_LIST_DETAIL_OTHER_CONTENT) }
                                    entry<TagAddNavKey> { Text(text = ROUTINE_LIST_DETAIL_TAG_ADD_CONTENT) }
                                    routineEntry(backStack = navBackStack, homeReselectEvent = reselectEvent)
                                },
                        )
                    }
                }
            }
        }
    }

    private fun homeViewModel(): RoutineHomeViewModel {
        val effectChannel = Channel<RoutineListEffect>(capacity = Channel.BUFFERED)
        val recordEffectChannel = Channel<RoutineHomeRecordEffect>(capacity = Channel.BUFFERED)

        return mockk<RoutineHomeViewModel>(relaxed = true).apply {
            every { routinePagingData } returns homePagingData
            every { filterUiState } returns homeFilterUiState
            every { effect } returns effectChannel.receiveAsFlow()
            every { recordEffect } returns recordEffectChannel.receiveAsFlow()
            // 카드 문구와 목록 순서는 화면이 정해 넘긴 오늘로 조회한다.
            every { updateToday(today = any()) } answers {
                today = firstArg()
                publish()
            }
            // 삭제가 저장되면 목록에서 빠지고 삭제 안내가 오는 것과 같게 한다.
            every { delete(id = any()) } answers {
                val id = firstArg<Uuid>()
                setRoutineList(routineList = routineList.filterNot { routine -> routine.id == id }, recordList = recordList)
                effectChannel.trySend(RoutineListEffect.Deleted(id = id)).getOrThrow()
            }
            // 완료가 저장되면 목록에서 빠지고 완료 안내가 오며, 실행 취소하면 안내 없이 돌아오는 것과 같게 한다.
            every { finish(id = any()) } answers {
                val id = firstArg<Uuid>()
                setFinished(id = id, isFinished = true)
                effectChannel.trySend(RoutineListEffect.Finished(id = id)).getOrThrow()
            }
            every { undo(effect = any()) } answers {
                when (val effect = firstArg<RoutineListEffect>()) {
                    is RoutineListEffect.Finished -> setFinished(id = effect.id, isFinished = false)
                    is RoutineListEffect.Restarted -> setFinished(id = effect.id, isFinished = true)
                    is RoutineListEffect.Deleted -> Unit
                }
            }
            // 기록이 저장되면 화면이 정해 둔 오늘의 기록이 늘고 기록 안내가 오는 것과 같게 한다.
            every { quickRecord(record = any(), today = any()) } answers {
                val record = firstArg<RoutineHomeRecord>()
                val recordId = addRecord(routineId = record.routineId, date = secondArg(), amount = record.amount)
                recordEffectChannel.trySend(RoutineHomeRecordEffect.Recorded(recordId = recordId, record = record)).getOrThrow()
            }
            every { amountRecord(record = any(), today = any()) } answers {
                val record = firstArg<RoutineHomeRecord>()
                val recordId = addRecord(routineId = record.routineId, date = secondArg(), amount = record.amount)
                recordEffectChannel.trySend(RoutineHomeRecordEffect.Recorded(recordId = recordId, record = record)).getOrThrow()
            }
            every { undoRecord(recordId = any()) } answers { removeRecord(recordId = firstArg()) }
        }
    }

    private fun setFinished(
        id: Uuid,
        isFinished: Boolean,
    ) {
        setRoutineList(routineList = routineList.map { routine -> if (routine.id == id) routine.copy(isFinished = isFinished) else routine }, recordList = recordList)
    }

    private fun finishedListViewModel(): RoutineFinishedListViewModel =
        mockk<RoutineFinishedListViewModel>(relaxed = true).apply {
            every { routinePagingData } returns finishedPagingData
            every { effect } returns Channel<RoutineListEffect>(capacity = Channel.BUFFERED).receiveAsFlow()
        }

    private fun calendarViewModel(): RoutineHomeCalendarViewModel =
        mockk<RoutineHomeCalendarViewModel>(relaxed = true).apply {
            every { uiState } returns calendarUiState
        }

    private fun filterViewModel(): RoutineHomeFilterViewModel =
        mockk<RoutineHomeFilterViewModel>(relaxed = true).apply {
            every { uiState } returns filterUiState
            every { tagPagingData } returns MutableStateFlow(PagingData.from(tagList))
            every { selectTag(id = any()) } answers {
                selectedTagIdSet.update { set -> set + firstArg<Uuid>() }
                publish()
            }
            every { unselectTag(id = any()) } answers {
                selectedTagIdSet.update { set -> set - firstArg<Uuid>() }
                publish()
            }
            every { unselectAllTag() } answers {
                selectedTagIdSet.value = emptySet()
                publish()
            }
            // 보기 범위 선택이 저장되면 목록이 그 범위로 다시 조회되고, 저장하지 못하면 아무것도 바뀌지 않는 것과 같게 한다.
            every { setViewScope(viewScope = any()) } answers {
                if (!isViewScopeSaveFailing) this@RoutineListDetailTestEnvironment.setViewScope(viewScope = firstArg())
            }
        }

    private fun detailViewModel(id: Uuid): RoutineDetailViewModel {
        val routine = routineList.first { routine -> routine.id == id }
        val uiState = detailUiStateMap.getOrPut(id) { MutableStateFlow(RoutineDetailUiState.Content(id = id, detail = routine.detail, isFinished = routine.isFinished)) }
        val effectChannel = Channel<RoutineDetailEffect>(capacity = Channel.BUFFERED)

        return mockk<RoutineDetailViewModel>(relaxed = true).apply {
            every { this@apply.uiState } returns uiState
            every { effect } returns effectChannel.receiveAsFlow()
            every { finish() } answers { setFinished(id = id, isFinished = true) }
            every { restart() } answers { setFinished(id = id, isFinished = false) }
            every { delete() } answers {
                setRoutineList(routineList = routineList.filterNot { value -> value.id == id }, recordList = recordList)
                effectChannel.trySend(RoutineDetailEffect.DeleteSucceeded).getOrThrow()
            }
            // 수정이 저장되면 상세와 목록, 달력이 저장된 내용으로 다시 조회되는 것과 같게 한다.
            every { update(input = any()) } answers {
                val detail = firstArg<RoutineInput>().toRoutineListDetailDetail()
                setRoutineList(routineList = routineList.map { value -> if (value.id == id) value.copy(detail = detail) else value }, recordList = recordList)
                effectChannel.trySend(RoutineDetailEffect.UpdateSucceeded).getOrThrow()
            }
        }
    }

    private fun recordViewModel(routineId: Uuid): RoutineDetailRecordViewModel {
        val pagingData = recordPagingDataMap.getOrPut(routineId) { MutableStateFlow(recordPagingDataOf(routineId = routineId)) }
        val effectChannel = Channel<RoutineDetailRecordEffect>(capacity = Channel.BUFFERED)

        return mockk<RoutineDetailRecordViewModel>(relaxed = true).apply {
            every { recordPagingData } returns pagingData
            every { uiState } returns MutableStateFlow(RoutineDetailRecordUiState())
            every { effect } returns effectChannel.receiveAsFlow()
            every { add(date = any(), amount = any()) } answers {
                addRecord(routineId = routineId, date = firstArg(), amount = secondArg())
                effectChannel.trySend(RoutineDetailRecordEffect.AddSucceeded).getOrThrow()
            }
            every { delete(recordId = any()) } answers { removeRecord(recordId = firstArg()) }
        }
    }

    private fun addViewModel(): RoutineAddViewModel {
        val effectChannel = Channel<RoutineAddEffect>(capacity = Channel.BUFFERED)

        return mockk<RoutineAddViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(RoutineAddUiState())
            every { effect } returns effectChannel.receiveAsFlow()
            // 추가가 저장되면 목록이 다시 조회되는 것과 같게 추가한 루틴을 저장된 루틴에 더한다.
            every { add(input = any(), tagIdSet = any()) } answers {
                val added = Arb.routine(isDeleted = false, detail = firstArg<RoutineInput>().toRoutineListDetailDetail()).next()
                setRoutineList(routineList = routineList + added, recordList = recordList)
                effectChannel.trySend(RoutineAddEffect.AddSucceeded).getOrThrow()
            }
        }
    }

    // 고른 태그는 태그 입력 ViewModel이 들고 있으므로, 인스턴스마다 따로 선택을 기억한다.
    private fun addTagViewModel(): EntityAddTagViewModel {
        val selectedTagList = MutableStateFlow<List<Tag>>(emptyList())
        val uiState = MutableStateFlow(EntityTagInputUiState())
        val selectionUiState = MutableStateFlow(EntityAddTagSelectionUiState(tagIdSet = emptySet()))

        return mockk<EntityAddTagViewModel>(relaxed = true).apply {
            every { this@apply.uiState } returns uiState
            every { this@apply.selectionUiState } returns selectionUiState
            every { tagPagingData } returns MutableStateFlow(PagingData.from(tagList))
            every { selectableTagPagingData } returns MutableStateFlow(PagingData.from(tagList))
            every { add(tagId = any()) } answers {
                val tag = tagList.first { tag -> tag.id == firstArg<Uuid>() }
                selectedTagList.update { list -> list + tag }
                uiState.value = EntityTagInputUiState(tagList = selectedTagList.value)
                selectionUiState.value = EntityAddTagSelectionUiState(tagIdSet = selectedTagList.value.map { value -> value.id }.toSet())
            }
        }
    }

    private fun detailTagViewModel(): EntityDetailTagViewModel =
        mockk<EntityDetailTagViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(EntityTagInputUiState())
            every { tagPagingData } returns MutableStateFlow(PagingData.from(tagList))
            every { selectableTagPagingData } returns MutableStateFlow(PagingData.from(tagList))
        }
}

// 기본 목적지인 캘린더 홈을 대신한다. 다른 기능의 화면 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
internal data object RoutineListDetailDefaultNavKey : ScreenNavKey {
    override val screenName: String
        get() = "RoutineListDetailDefault"
}

// 메모 목록처럼 루틴이 아닌 다른 주요 목적지를 대신한다.
internal data object RoutineListDetailOtherNavKey : ScreenNavKey {
    override val screenName: String
        get() = "RoutineListDetailOther"
}

internal const val ROUTINE_LIST_DETAIL_DEFAULT_CONTENT: String = "DefaultTopLevelContent"
internal const val ROUTINE_LIST_DETAIL_OTHER_CONTENT: String = "OtherTopLevelContent"
internal const val ROUTINE_LIST_DETAIL_TAG_ADD_CONTENT: String = "TagAddContent"

internal fun routineListDetailViewModelStoreOwner(): ViewModelStoreOwner =
    object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }

// 카드 문구가 정해지도록 진행 상태와 주기, 단위, 목표량, 한 번에 기록할 양을 정한 루틴을 만든다.
internal fun routineListDetailRoutine(
    title: String,
    today: LocalDate,
    unit: String = "회",
    goalAmount: Int? = null,
    amountPerRecord: Int = 1,
    cycle: RoutineCycle = RoutineCycle.DAILY,
    startDate: LocalDate = today.minus(1, DateTimeUnit.MONTH),
    isFinished: Boolean = false,
): Routine =
    Arb
        .routine(
            isDeleted = false,
            detail =
                Arb.routineDetail().next().copy(
                    title = title,
                    description = "",
                    startDate = startDate,
                    cycle = cycle,
                    unit = unit,
                    goalAmount = goalAmount,
                    amountPerRecord = amountPerRecord,
                ),
            isFinished = isFinished,
        ).next()

internal fun routineListDetailRecord(
    routine: Routine,
    date: LocalDate,
    amount: Int,
): RoutineRecord = Arb.routineRecord(routineId = routine.id).next().copy(date = date, amount = amount)

internal fun <T : Any> routineListDetailPagingDataOf(list: List<T>): PagingData<T> =
    PagingData.from(
        data = list,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

// 카드 문구와 루틴 추가의 시작 날짜, 기록할 날짜는 기기의 오늘로 정해지므로, 기기 시계를 그 날의 정오로 맞춘다.
internal fun setRoutineListDetailToday(date: LocalDate) {
    mockkObject(Clock.System)
    every { Clock.System.now() } returns LocalDateTime(date, LocalTime(hour = 12, minute = 0)).toInstant(TimeZone.currentSystemDefault())
}

internal fun releaseRoutineListDetailToday() {
    unmockkObject(Clock.System)
}

// 목록 순서는 진행 중, 시작 전 순이다.
private fun RoutineProgress.listOrder(): Int =
    when (this) {
        RoutineProgress.IN_PROGRESS -> 0
        RoutineProgress.NOT_STARTED -> 1
    }

private fun RoutineInput.toRoutineListDetailDetail(): RoutineDetail =
    RoutineDetail(
        title = title,
        description = description,
        color = color,
        startDate = startDate,
        cycle = cycle,
        unit = unit,
        goalAmount = goalAmount,
        amountPerRecord = amountPerRecord,
    )
