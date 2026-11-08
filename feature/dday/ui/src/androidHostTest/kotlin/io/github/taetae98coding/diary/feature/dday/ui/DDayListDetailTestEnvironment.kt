@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.dday.ui

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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
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
import io.github.taetae98coding.diary.compose.dday.DDAY_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.dday.ddayDetail
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagSelectionUiState
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.dday.ui.add.DDayAddEffect
import io.github.taetae98coding.diary.feature.dday.ui.add.DDayAddUiState
import io.github.taetae98coding.diary.feature.dday.ui.add.DDayAddViewModel
import io.github.taetae98coding.diary.feature.dday.ui.detail.DDayDetailEffect
import io.github.taetae98coding.diary.feature.dday.ui.detail.DDayDetailUiState
import io.github.taetae98coding.diary.feature.dday.ui.detail.DDayDetailViewModel
import io.github.taetae98coding.diary.feature.dday.ui.home.DDayHomeScaffoldFilterUiState
import io.github.taetae98coding.diary.feature.dday.ui.home.DDayHomeViewModel
import io.github.taetae98coding.diary.feature.dday.ui.home.filter.DDayHomeFilterUiState
import io.github.taetae98coding.diary.feature.dday.ui.home.filter.DDayHomeFilterViewModel
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
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

// 목록·상세 배치와 디데이 목록의 내비게이션 경계를 검증하는 테스트가 함께 쓰는 환경이다.
// 제품 dDayEntry로 NavDisplay를 구성하고, 저장소 대신 ViewModel 대역이 기기에 저장된 디데이와 필터 선택을 흉내 낸다.
internal class DDayListDetailTestEnvironment(
    vararg initialBackStack: ScreenNavKey = arrayOf(DDayListDetailMoreNavKey, DDayHomeNavKey),
) {
    val backStack: NavBackStack<ScreenNavKey> = NavBackStack(*initialBackStack)

    val homeViewModelList: MutableList<DDayHomeViewModel> = mutableListOf()
    val filterViewModelList: MutableList<DDayHomeFilterViewModel> = mutableListOf()
    val addViewModelList: MutableList<DDayAddViewModel> = mutableListOf()
    val addTagViewModelList: MutableList<EntityAddTagViewModel> = mutableListOf()
    val detailViewModelList: MutableList<DDayDetailViewModel> = mutableListOf()
    val syncViewModelList: MutableList<SyncRefreshViewModel> = mutableListOf()

    var directiveOverride: PaneScaffoldDirective? by mutableStateOf(null)

    var tagList: List<Tag> = emptyList()
    var ddayTagMap: Map<Uuid, Set<Uuid>> = emptyMap()

    private var ddayList: List<DDay> = emptyList()
    private val selectedTagIdSet = MutableStateFlow<Set<Uuid>>(emptySet())
    private val homePagingData = MutableStateFlow(listDetailPagingDataOf(emptyList()))
    private val homeFilterUiState = MutableStateFlow(DDayHomeScaffoldFilterUiState())
    private val filterUiState = MutableStateFlow(DDayHomeFilterUiState())

    val storedDDayList: List<DDay>
        get() = ddayList

    fun setDDayList(list: List<DDay>) {
        ddayList = list
        publish()
    }

    fun selectTag(id: Uuid) {
        selectedTagIdSet.update { set -> set + id }
        publish()
    }

    // 저장된 디데이를 노출 기준과 필터, 제목 순서로 다시 조회한 것과 같게 목록을 갱신한다.
    private fun publish() {
        val selected = selectedTagIdSet.value
        val visible =
            ddayList
                .filter { dday -> selected.isEmpty() || ddayTagMap[dday.id].orEmpty().any(selected::contains) }
                .sortedBy { dday -> dday.detail.title }
        homePagingData.value = listDetailPagingDataOf(visible)
        homeFilterUiState.value = DDayHomeScaffoldFilterUiState(selectedTagIdSet = selected, storedTagIdSet = selected)
        filterUiState.value = DDayHomeFilterUiState(selectedTagIdSet = selected)
    }

    // 배치를 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓴다.
    fun module(): Module =
        module {
            factory { addTagViewModel().also(addTagViewModelList::add) }
            factory { detailTagViewModel() }
            factory { homeViewModel().also(homeViewModelList::add) }
            factory {
                mockk<SyncRefreshViewModel>(relaxed = true)
                    .apply { every { uiState } returns MutableStateFlow(SyncRefreshUiState()) }
                    .also(syncViewModelList::add)
            }
            factory { filterViewModel().also(filterViewModelList::add) }
            factory { parameters -> detailViewModel(id = parameters.get()).also(detailViewModelList::add) }
            factory { addViewModel().also(addViewModelList::add) }
        }

    @Composable
    fun Content(
        navBackStack: NavBackStack<ScreenNavKey> = backStack,
        viewModelStoreOwner: ViewModelStoreOwner = remember { listDetailViewModelStoreOwner() },
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
                                    entry<DDayListDetailMoreNavKey> { Text(text = DDAY_LIST_DETAIL_MORE_CONTENT) }
                                    entry<TagAddNavKey> { Text(text = DDAY_LIST_DETAIL_TAG_ADD_CONTENT) }
                                    dDayEntry(backStack = navBackStack)
                                },
                        )
                    }
                }
            }
        }
    }

    private fun homeViewModel(): DDayHomeViewModel {
        val effectChannel = Channel<DDayListEffect>(capacity = Channel.BUFFERED)

        return mockk<DDayHomeViewModel>(relaxed = true).apply {
            every { ddayPagingData } returns homePagingData
            every { filterUiState } returns homeFilterUiState
            every { effect } returns effectChannel.receiveAsFlow()
            // 삭제가 저장되면 목록에서 빠지고 삭제 안내가 오는 것과 같게 한다.
            every { delete(id = any()) } answers {
                val id = firstArg<Uuid>()
                setDDayList(ddayList.filterNot { dday -> dday.id == id })
                effectChannel.trySend(DDayListEffect.Deleted(id = id)).getOrThrow()
            }
        }
    }

    private fun filterViewModel(): DDayHomeFilterViewModel =
        mockk<DDayHomeFilterViewModel>(relaxed = true).apply {
            every { uiState } returns filterUiState
            every { tagPagingData } returns MutableStateFlow(PagingData.from(tagList))
            every { selectTag(id = any()) } answers { this@DDayListDetailTestEnvironment.selectTag(id = firstArg<Uuid>()) }
            every { unselectTag(id = any()) } answers {
                selectedTagIdSet.update { set -> set - firstArg<Uuid>() }
                publish()
            }
        }

    private fun detailViewModel(id: Uuid): DDayDetailViewModel {
        val dday = ddayList.first { dday -> dday.id == id }
        val uiState = MutableStateFlow<DDayDetailUiState>(DDayDetailUiState.Content(id = id, detail = dday.detail))
        val effectChannel = Channel<DDayDetailEffect>(capacity = Channel.BUFFERED)

        return mockk<DDayDetailViewModel>(relaxed = true).apply {
            every { this@apply.uiState } returns uiState
            every { effect } returns effectChannel.receiveAsFlow()
            every { delete() } answers {
                setDDayList(ddayList.filterNot { value -> value.id == id })
                effectChannel.trySend(DDayDetailEffect.DeleteSucceeded).getOrThrow()
            }
            // 수정이 저장되면 상세와 목록이 저장된 내용으로 다시 조회되는 것과 같게 한다.
            every { update(input = any()) } answers {
                val detail = firstArg<DDayInput>().toListDetailDDayDetail(color = dday.detail.color)
                setDDayList(ddayList.map { value -> if (value.id == id) value.copy(detail = detail) else value })
                uiState.value = DDayDetailUiState.Content(id = id, detail = detail)
                effectChannel.trySend(DDayDetailEffect.UpdateSucceeded).getOrThrow()
            }
        }
    }

    private fun addViewModel(): DDayAddViewModel {
        val effectChannel = Channel<DDayAddEffect>(capacity = Channel.BUFFERED)

        return mockk<DDayAddViewModel>(relaxed = true).apply {
            every { uiState } returns MutableStateFlow(DDayAddUiState())
            every { effect } returns effectChannel.receiveAsFlow()
            // 추가가 저장되면 목록이 다시 조회되는 것과 같게 추가한 디데이를 저장된 디데이에 더한다.
            every { add(input = any(), tagIdSet = any()) } answers {
                val input = firstArg<DDayInput>()
                val added = Arb.dday().next().copy(detail = input.toListDetailDDayDetail(color = Arb.ddayDetail().next().color), isDeleted = false)
                setDDayList(ddayList + added)
                effectChannel.trySend(DDayAddEffect.AddSucceeded).getOrThrow()
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

// 디데이 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
internal data object DDayListDetailMoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}

internal const val DDAY_LIST_DETAIL_MORE_CONTENT: String = "MoreContent"
internal const val DDAY_LIST_DETAIL_TAG_ADD_CONTENT: String = "TagAddContent"

internal fun listDetailViewModelStoreOwner(): ViewModelStoreOwner =
    object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }

// 카드 문구가 정해지도록 반복 간격과 종료일이 없는 디데이를 만든다.
internal fun listDetailDDay(
    title: String,
    baseDate: LocalDate,
): DDay =
    Arb.dday().next().copy(
        detail =
            Arb.ddayDetail().next().copy(
                title = title,
                baseDate = baseDate,
                countsBaseDay = false,
                repeats = emptySet(),
                endDate = null,
            ),
        isDeleted = false,
    )

internal fun listDetailPagingDataOf(list: List<DDay>): PagingData<DDay> =
    PagingData.from(
        data = list,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

// 목록은 격자라 한 줄에 여러 카드가 놓일 수 있으므로, 위에서 아래로, 같은 줄이면 왼쪽에서 오른쪽으로 읽은 순서로 비교한다.
internal fun Rect.isBeforeInGrid(other: Rect): Boolean = top < other.top || (top == other.top && left < other.left)

internal fun listDetailCard(title: String): SemanticsMatcher = hasTestTag(DDAY_CARD_TEST_TAG) and hasText(title)

internal fun listDetailCard(
    title: String,
    countText: String,
): SemanticsMatcher = listDetailCard(title) and hasText(countText)

// 카드 문구와 디데이 추가의 기준일은 기기의 오늘로 정해지므로, 기기 시계를 그 날의 정오로 맞춘다.
internal fun setListDetailToday(date: LocalDate) {
    mockkObject(Clock.System)
    every { Clock.System.now() } returns LocalDateTime(date, LocalTime(hour = 12, minute = 0)).toInstant(TimeZone.currentSystemDefault())
}

internal fun releaseListDetailToday() {
    unmockkObject(Clock.System)
}

private fun DDayInput.toListDetailDDayDetail(color: Long): DDayDetail =
    Arb.ddayDetail().next().copy(
        title = title,
        description = description,
        color = color,
        baseDate = baseDate,
        countsBaseDay = countsBaseDay,
        repeats = emptySet(),
        endDate = endDate,
        overflowPolicy = overflowPolicy,
    )
