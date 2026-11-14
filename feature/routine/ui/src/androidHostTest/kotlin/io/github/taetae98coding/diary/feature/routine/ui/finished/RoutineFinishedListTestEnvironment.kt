@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.finished

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
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
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineProgress
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineDetail
import io.github.taetae98coding.diary.core.testing.routine.routineRecord
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.progress.progress
import io.github.taetae98coding.diary.domain.routine.progress.range
import io.github.taetae98coding.diary.domain.routine.progress.routineCurrentCycle
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.ui.detail.RoutineDetailEffect
import io.github.taetae98coding.diary.feature.routine.ui.detail.RoutineDetailUiState
import io.github.taetae98coding.diary.feature.routine.ui.detail.RoutineDetailViewModel
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordEffect
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordUiState
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffoldFilterUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineAddTagViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineAddViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineEntry
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.uuid.Uuid

// 완료된 루틴 목록 테스트가 함께 쓰는 환경이다.
// 완료된 루틴 목록의 ViewModel은 제품 구현을 그대로 쓰고, 기기에 저장된 루틴과 기록은 UseCase와 나머지 ViewModel의 대역이 흉내 낸다.
internal class RoutineFinishedListTestEnvironment(
    vararg initialBackStack: ScreenNavKey = arrayOf(RoutineFinishedListDefaultNavKey, RoutineHomeNavKey),
) {
    val backStack: NavBackStack<ScreenNavKey> = NavBackStack(*initialBackStack)

    val finishRoutineUseCase: FinishRoutineUseCase = mockk()
    val restartRoutineUseCase: RestartRoutineUseCase = mockk()
    val deleteRoutineUseCase: DeleteRoutineUseCase = mockk()
    val restoreRoutineUseCase: RestoreRoutineUseCase = mockk()
    val syncViewModel: SyncRefreshViewModel =
        mockk<SyncRefreshViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(SyncRefreshUiState())
        }

    // 저장되지 못하게 둘 동작이다.
    var isRestartFailing: Boolean = false
    var isDeleteFailing: Boolean = false

    var tagList: List<Tag> = emptyList()

    private val routineFlow = MutableStateFlow<List<Routine>>(emptyList())
    private val recordFlow = MutableStateFlow<List<RoutineRecord>>(emptyList())
    private val homeToday = MutableStateFlow<LocalDate?>(null)
    private val detailUiStateMap = mutableMapOf<Uuid, MutableStateFlow<RoutineDetailUiState>>()

    val storedRoutineList: List<Routine>
        get() = routineFlow.value

    val storedRecordList: List<RoutineRecord>
        get() = recordFlow.value

    init {
        coEvery { finishRoutineUseCase(parameter = any()) } coAnswers { change(id = firstArg()) { routine -> routine.copy(isFinished = true) } }
        coEvery { restartRoutineUseCase(parameter = any()) } coAnswers {
            if (isRestartFailing) Result.failure(IllegalStateException("저장하거나 불러오지 못함")) else change(id = firstArg()) { routine -> routine.copy(isFinished = false) }
        }
        coEvery { deleteRoutineUseCase(parameter = any()) } coAnswers {
            if (isDeleteFailing) Result.failure(IllegalStateException("저장하거나 불러오지 못함")) else change(id = firstArg()) { routine -> routine.copy(isDeleted = true) }
        }
        coEvery { restoreRoutineUseCase(parameter = any()) } coAnswers { change(id = firstArg()) { routine -> routine.copy(isDeleted = false) } }
    }

    fun setRoutineList(
        routineList: List<Routine>,
        recordList: List<RoutineRecord> = emptyList(),
    ) {
        recordFlow.value = recordList
        routineFlow.value = routineList
        publishDetail()
    }

    fun stored(routine: Routine): Routine = routineFlow.value.first { value -> value.id == routine.id }

    private fun change(
        id: Uuid,
        transform: (Routine) -> Routine,
    ): Result<Int> {
        routineFlow.update { list -> list.map { routine -> if (routine.id == id) transform(routine) else routine } }
        publishDetail()

        return Result.success(1)
    }

    private fun publishDetail() {
        detailUiStateMap.forEach { (id, uiState) ->
            routineFlow.value
                .firstOrNull { routine -> routine.id == id && !routine.isDeleted }
                ?.let { routine -> uiState.value = RoutineDetailUiState.Content(id = id, detail = routine.detail, isFinished = routine.isFinished) }
        }
    }

    // 완료된 루틴 목록의 ViewModel은 제품 구현이다. 조회는 저장된 루틴을 노출 기준과 목록 순서로 다시 읽은 것과 같게 답한다.
    fun finishedViewModel(): RoutineFinishedListViewModel {
        val pageFinishedRoutineUseCase = mockk<PageFinishedRoutineUseCase>()
        every { pageFinishedRoutineUseCase(parameter = any()) } answers {
            val today = firstArg<LocalDate>()

            routineFlow.map { list -> Result.success(routineFinishedListPagingDataOf(list.filter { routine -> routine.isFinished && !routine.isDeleted }.toSummaryList(today = today))) }
        }

        return RoutineFinishedListViewModel(
            pageFinishedRoutineUseCase = pageFinishedRoutineUseCase,
            finishRoutineUseCase = finishRoutineUseCase,
            restartRoutineUseCase = restartRoutineUseCase,
            deleteRoutineUseCase = deleteRoutineUseCase,
            restoreRoutineUseCase = restoreRoutineUseCase,
        )
    }

    private fun List<Routine>.toSummaryList(today: LocalDate): List<RoutineSummary> {
        val cycle = routineCurrentCycle(today = today)

        return sortedWith(compareBy<Routine> { routine -> routine.detail.progress(today = today).listOrder() }.thenBy { routine -> routine.detail.title })
            .map { routine ->
                val range = cycle.range(cycle = routine.detail.cycle)

                RoutineSummary(
                    routine = routine,
                    cycleAmount = recordFlow.value.filter { record -> record.routineId == routine.id && record.date in range }.sumOf { record -> record.amount.toLong() },
                )
            }
    }

    // 화면을 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    fun module(): Module =
        module {
            factory { homeViewModel() }
            factory { calendarViewModel() }
            factory { holidayViewModel() }
            factory { syncViewModel }
            factory { finishedViewModel() }
            factory { routineAddViewModel() }
            factory { routineAddTagViewModel() }
            factory { parameters -> detailViewModel(id = parameters.get()) }
            factory { parameters -> recordViewModel(routineId = parameters.get()) }
            factory { detailTagViewModel() }
        }

    @Composable
    fun Content(
        navBackStack: NavBackStack<ScreenNavKey> = backStack,
        viewModelStoreOwner: ViewModelStoreOwner = remember { routineFinishedListViewModelStoreOwner() },
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
                                    rememberDiaryListDetailSceneStrategy(directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())),
                                ),
                            entryDecorators =
                                listOf(
                                    rememberSaveableStateHolderNavEntryDecorator(),
                                    rememberViewModelStoreNavEntryDecorator(),
                                    rememberListDetailPlaceholderNavEntryDecorator(holder),
                                ),
                            entryProvider =
                                entryProvider {
                                    entry<RoutineFinishedListDefaultNavKey> { Text(text = ROUTINE_FINISHED_LIST_DEFAULT_CONTENT) }
                                    entry<TagAddNavKey> { Text(text = ROUTINE_FINISHED_LIST_TAG_ADD_CONTENT) }
                                    entry<TagDetailNavKey> { Text(text = ROUTINE_FINISHED_LIST_TAG_DETAIL_CONTENT) }
                                    routineEntry(backStack = navBackStack, homeReselectEvent = emptyFlow())
                                },
                        )
                    }
                }
            }
        }
    }

    // RoutineHome의 루틴 목록은 완료되지 않고 삭제되지 않은 루틴을 목록 순서로 조회한 것과 같게 답한다.
    private fun homeViewModel(): RoutineHomeViewModel =
        mockk<RoutineHomeViewModel>(relaxed = true).apply {
            every { routinePagingData } returns
                combine(routineFlow, homeToday) { list, today ->
                    if (today == null) {
                        PagingData.empty<RoutineSummary>()
                    } else {
                        routineFinishedListPagingDataOf(list.filter { routine -> !routine.isFinished && !routine.isDeleted }.toSummaryList(today = today))
                    }
                }
            every { filterUiState } returns MutableStateFlow(RoutineHomeScaffoldFilterUiState())
            every { effect } returns emptyFlow()
            every { recordEffect } returns emptyFlow()
            every { updateToday(today = any()) } answers { homeToday.value = firstArg() }
        }

    private fun calendarViewModel(): RoutineHomeCalendarViewModel =
        mockk<RoutineHomeCalendarViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(RoutineHomeCalendarUiState())
        }

    private fun holidayViewModel(): RoutineHomeHolidayViewModel =
        mockk<RoutineHomeHolidayViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(RoutineHomeHolidayUiState())
        }

    private fun detailViewModel(id: Uuid): RoutineDetailViewModel {
        val routine = routineFlow.value.first { routine -> routine.id == id }
        val uiState = detailUiStateMap.getOrPut(id) { MutableStateFlow(RoutineDetailUiState.Content(id = id, detail = routine.detail, isFinished = routine.isFinished)) }
        val effectChannel = Channel<RoutineDetailEffect>(capacity = Channel.BUFFERED)

        return mockk<RoutineDetailViewModel>(relaxed = true).apply {
            every { this@apply.uiState } returns uiState
            every { effect } returns effectChannel.receiveAsFlow()
            every { finish() } answers {
                change(id = id) { value -> value.copy(isFinished = true) }
                Unit
            }
            every { restart() } answers {
                change(id = id) { value -> value.copy(isFinished = false) }
                Unit
            }
            every { delete() } answers {
                change(id = id) { value -> value.copy(isDeleted = true) }
                effectChannel.trySend(RoutineDetailEffect.DeleteSucceeded).getOrThrow()
            }
            // 수정이 저장되면 상세와 목록이 저장된 내용으로 다시 조회되는 것과 같게 한다.
            every { update(input = any()) } answers {
                val detail = firstArg<RoutineInput>().toRoutineDetail()
                change(id = id) { value -> value.copy(detail = detail) }
                effectChannel.trySend(RoutineDetailEffect.UpdateSucceeded).getOrThrow()
            }
        }
    }

    private fun recordViewModel(routineId: Uuid): RoutineDetailRecordViewModel {
        val effectChannel = Channel<RoutineDetailRecordEffect>(capacity = Channel.BUFFERED)

        return mockk<RoutineDetailRecordViewModel>(relaxed = true).apply {
            every { recordPagingData } returns
                recordFlow.map { list ->
                    routineFinishedListPagingDataOf(
                        list
                            .filter { record -> record.routineId == routineId }
                            .asReversed()
                            .sortedByDescending { record -> record.date },
                    )
                }
            every { uiState } returns MutableStateFlow(RoutineDetailRecordUiState())
            every { effect } returns effectChannel.receiveAsFlow()
            every { add(date = any(), amount = any()) } answers {
                recordFlow.update { list -> list + routineFinishedListRecord(routineId = routineId, date = firstArg(), amount = secondArg()) }
                effectChannel.trySend(RoutineDetailRecordEffect.AddSucceeded).getOrThrow()
            }
            every { delete(recordId = any()) } answers {
                recordFlow.update { list -> list.filterNot { record -> record.id == firstArg<Uuid>() } }
            }
        }
    }

    private fun detailTagViewModel(): EntityDetailTagViewModel =
        mockk<EntityDetailTagViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(EntityTagInputUiState(tagList = tagList))
            every { tagPagingData } returns MutableStateFlow(routineFinishedListPagingDataOf(tagList))
            every { selectableTagPagingData } returns MutableStateFlow(routineFinishedListPagingDataOf(tagList))
        }
}

// 기본 목적지인 캘린더 홈을 대신한다. 다른 기능의 화면 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
internal data object RoutineFinishedListDefaultNavKey : ScreenNavKey {
    override val screenName: String
        get() = "RoutineFinishedListDefault"
}

internal const val ROUTINE_FINISHED_LIST_DEFAULT_CONTENT: String = "DefaultTopLevelContent"
internal const val ROUTINE_FINISHED_LIST_TAG_ADD_CONTENT: String = "TagAddContent"
internal const val ROUTINE_FINISHED_LIST_TAG_DETAIL_CONTENT: String = "TagDetailContent"
internal const val ROUTINE_FINISHED_LIST_TIMEOUT_MILLIS: Long = 5_000L

internal fun routineFinishedListViewModelStoreOwner(): ViewModelStoreOwner =
    object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }

// 따로 정하지 않으면 완료되었고 삭제되지 않은, 목표량이 없는 매일 루틴이다.
internal fun routineFinishedListRoutine(
    title: String,
    startDate: LocalDate,
    isFinished: Boolean = true,
    isDeleted: Boolean = false,
    unit: String = "회",
    goalAmount: Int? = null,
    amountPerRecord: Int = 1,
    description: String = "",
): Routine =
    Arb
        .routine(
            isDeleted = isDeleted,
            detail =
                Arb.routineDetail().next().copy(
                    title = title,
                    description = description,
                    startDate = startDate,
                    cycle = RoutineCycle.DAILY,
                    unit = unit,
                    goalAmount = goalAmount,
                    amountPerRecord = amountPerRecord,
                ),
            isFinished = isFinished,
        ).next()

internal fun routineFinishedListRecord(
    routineId: Uuid,
    date: LocalDate,
    amount: Int,
): RoutineRecord = Arb.routineRecord(routineId = routineId).next().copy(date = date, amount = amount)

internal fun <T : Any> routineFinishedListPagingDataOf(list: List<T>): PagingData<T> =
    PagingData.from(
        data = list,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

// 카드 문구와 목록 순서는 기기의 오늘로 정해지므로, 기기 시계를 그 날의 정오로 맞춘다.
internal fun setRoutineFinishedListToday(date: LocalDate) {
    mockkObject(Clock.System)
    every { Clock.System.now() } returns LocalDateTime(date, LocalTime(hour = 12, minute = 0)).toInstant(TimeZone.currentSystemDefault())
}

internal fun releaseRoutineFinishedListToday() {
    unmockkObject(Clock.System)
}

internal fun routineFinishedListCard(title: String): SemanticsMatcher = hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(title)

// 카드가 보여 주는 글을 위에서 아래 순서로 읽는다. 첫 글이 제목이다.
internal fun ComposeTestRule.routineFinishedListCardTextList(): List<List<String>> =
    onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG))
        .fetchSemanticsNodes()
        .sortedBy { node -> node.boundsInRoot.top }
        .map { node ->
            node.config
                .getOrNull(SemanticsProperties.Text)
                .orEmpty()
                .map { text -> text.text }
                .filter { text -> text.isNotEmpty() }
        }

internal fun ComposeTestRule.routineFinishedListCardTitleList(): List<String> = routineFinishedListCardTextList().mapNotNull { textList -> textList.firstOrNull() }

internal fun ComposeTestRule.waitUntilRoutineFinishedListExists(matcher: SemanticsMatcher) {
    waitUntil(timeoutMillis = ROUTINE_FINISHED_LIST_TIMEOUT_MILLIS) { onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
}

internal fun ComposeTestRule.waitUntilRoutineFinishedListGone(matcher: SemanticsMatcher) {
    waitUntil(timeoutMillis = ROUTINE_FINISHED_LIST_TIMEOUT_MILLIS) { onAllNodes(matcher).fetchSemanticsNodes().isEmpty() }
}

internal fun ComposeTestRule.waitUntilRoutineFinishedListTitles(titleList: List<String>) {
    waitUntil(timeoutMillis = ROUTINE_FINISHED_LIST_TIMEOUT_MILLIS) { routineFinishedListCardTitleList() == titleList }
}

private fun RoutineProgress.listOrder(): Int =
    when (this) {
        RoutineProgress.IN_PROGRESS -> 0
        RoutineProgress.NOT_STARTED -> 1
    }

private fun RoutineInput.toRoutineDetail(): RoutineDetail =
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
