package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.checklist.ChecklistListEffect
import io.github.taetae98coding.diary.compose.contact.ContactListEffect
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.compose.map.DiaryMapContentOverride
import io.github.taetae98coding.diary.compose.map.LocalDiaryMapContentOverride
import io.github.taetae98coding.diary.compose.map.PlatformDiaryMapContentOverride
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.music.MusicListEffect
import io.github.taetae98coding.diary.compose.place.PlaceListEffect
import io.github.taetae98coding.diary.compose.qr.QrListEffect
import io.github.taetae98coding.diary.compose.web.WebListEffect
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.tag.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.tag.ui.detail.checklist.TagDetailChecklistViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.contact.TagDetailContactViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.dday.TagDetailDDayViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.form.TagDetailLinkViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.memo.TagDetailMemoViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.music.TagDetailMusicViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceListUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceMapViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.qr.TagDetailQrViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.web.TagDetailWebViewModel
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkInputUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import kotlin.uuid.Uuid

// KoinApplication에 넘긴 모듈은 첫 테스트의 것이 이어서 쓰이므로, 탭이 얻는 값은 모듈이 붙잡는 이 흐름들로 테스트마다 바꾼다.
internal val linkUiStateFlow = MutableStateFlow(TagLinkInputUiState())
internal val linkTagPagingDataFlow = MutableStateFlow(PagingData.empty<Tag>())
internal val memoPagingDataFlow = MutableStateFlow(PagingData.empty<MemoListItem>())
internal val memoEffectFlow = MutableSharedFlow<MemoListEffect>(extraBufferCapacity = EFFECT_BUFFER_CAPACITY)
internal val webPagingDataFlow = MutableStateFlow(PagingData.empty<Web>())
internal val webEffectFlow = MutableSharedFlow<WebListEffect>(extraBufferCapacity = EFFECT_BUFFER_CAPACITY)
internal val placePagingDataFlow = MutableStateFlow(PagingData.empty<Place>())
internal val placeListUiStateFlow = MutableStateFlow(TagDetailPlaceListUiState())
internal val placeEffectFlow = MutableSharedFlow<PlaceListEffect>(extraBufferCapacity = EFFECT_BUFFER_CAPACITY)
internal val placeMapUiStateFlow = MutableStateFlow<TagDetailPlaceUiState>(TagDetailPlaceUiState.Loading)
internal val contactPagingDataFlow = MutableStateFlow(PagingData.empty<ContactListItem>())
internal val contactEffectFlow = MutableSharedFlow<ContactListEffect>(extraBufferCapacity = EFFECT_BUFFER_CAPACITY)
internal val checklistPagingDataFlow = MutableStateFlow(PagingData.empty<ChecklistSummary>())
internal val checklistEffectFlow = MutableSharedFlow<ChecklistListEffect>(extraBufferCapacity = EFFECT_BUFFER_CAPACITY)
internal val musicPagingDataFlow = MutableStateFlow(PagingData.empty<Music>())
internal val musicEffectFlow = MutableSharedFlow<MusicListEffect>(extraBufferCapacity = EFFECT_BUFFER_CAPACITY)
internal val qrPagingDataFlow = MutableStateFlow(PagingData.empty<Qr>())
internal val qrEffectFlow = MutableSharedFlow<QrListEffect>(extraBufferCapacity = EFFECT_BUFFER_CAPACITY)
internal val ddayPagingDataFlow = MutableStateFlow(PagingData.empty<DDay>())
internal val ddayEffectFlow = MutableSharedFlow<DDayListEffect>(extraBufferCapacity = EFFECT_BUFFER_CAPACITY)
internal val syncUiStateFlow = MutableStateFlow(SyncRefreshUiState())

// 탭이 Koin으로 얻는 ViewModel은 호출 검증을 위해 테스트가 참조할 수 있어야 한다.
internal var linkViewModelRef: TagDetailLinkViewModel? = null
    private set

internal var memoViewModelRef: TagDetailMemoViewModel? = null
    private set

// 탭이 같은 SyncRefreshViewModel 타입을 쓰므로 메모 탭의 참조도 같은 대역이다.
internal val memoSyncViewModelRef: SyncRefreshViewModel?
    get() = syncViewModelRef

internal var webViewModelRef: TagDetailWebViewModel? = null
    private set

internal var contactViewModelRef: TagDetailContactViewModel? = null
    private set

internal var checklistViewModelRef: TagDetailChecklistViewModel? = null
    private set

internal var musicViewModelRef: TagDetailMusicViewModel? = null
    private set

internal var qrViewModelRef: TagDetailQrViewModel? = null
    private set

internal var placeViewModelRef: TagDetailPlaceViewModel? = null
    private set

internal var ddayViewModelRef: TagDetailDDayViewModel? = null
    private set

internal var placeMapViewModelRef: TagDetailPlaceMapViewModel? = null
    private set

internal var syncViewModelRef: SyncRefreshViewModel? = null
    private set

private const val EFFECT_BUFFER_CAPACITY = 8

internal val tagDetailTabViewModelModule =
    module {
        factory {
            mockk<TagDetailLinkViewModel>(relaxed = true)
                .apply {
                    every { uiState } returns linkUiStateFlow
                    every { tagPagingData } returns linkTagPagingDataFlow
                    every { selectableTagPagingData } returns linkTagPagingDataFlow
                }.also { linkViewModelRef = it }
        }
        factory {
            mockk<TagDetailMemoViewModel>(relaxed = true)
                .apply {
                    every { memoPagingData } returns memoPagingDataFlow
                    every { sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.DEFAULT))
                    every { scopeUiState } returns MutableStateFlow(TagDetailScopeUiState())
                    every { effect } returns memoEffectFlow
                }.also { memoViewModelRef = it }
        }
        factory {
            mockk<TagDetailWebViewModel>(relaxed = true)
                .apply {
                    every { webPagingData } returns webPagingDataFlow
                    every { effect } returns webEffectFlow
                    every { sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.TITLE))
                    every { scopeUiState } returns MutableStateFlow(TagDetailScopeUiState())
                }.also { webViewModelRef = it }
        }
        factory {
            mockk<TagDetailPlaceViewModel>(relaxed = true)
                .apply {
                    every { placePagingData } returns placePagingDataFlow
                    every { placeListUiState } returns placeListUiStateFlow
                    every { effect } returns placeEffectFlow
                    every { sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.TITLE))
                    every { scopeUiState } returns MutableStateFlow(TagDetailScopeUiState())
                }.also { placeViewModelRef = it }
        }
        factory {
            mockk<TagDetailContactViewModel>(relaxed = true)
                .apply {
                    every { contactPagingData } returns contactPagingDataFlow
                    every { effect } returns contactEffectFlow
                    every { sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.NAME))
                    every { scopeUiState } returns MutableStateFlow(TagDetailScopeUiState())
                }.also { contactViewModelRef = it }
        }
        factory {
            mockk<TagDetailChecklistViewModel>(relaxed = true)
                .apply {
                    every { checklistPagingData } returns checklistPagingDataFlow
                    every { effect } returns checklistEffectFlow
                    every { scopeUiState } returns MutableStateFlow(TagDetailScopeUiState())
                }.also { checklistViewModelRef = it }
        }
        factory {
            mockk<TagDetailMusicViewModel>(relaxed = true)
                .apply {
                    every { musicPagingData } returns musicPagingDataFlow
                    every { effect } returns musicEffectFlow
                    every { sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.TITLE))
                    every { scopeUiState } returns MutableStateFlow(TagDetailScopeUiState())
                }.also { musicViewModelRef = it }
        }
        factory {
            mockk<TagDetailQrViewModel>(relaxed = true)
                .apply {
                    every { qrPagingData } returns qrPagingDataFlow
                    every { effect } returns qrEffectFlow
                    every { scopeUiState } returns MutableStateFlow(TagDetailScopeUiState())
                }.also { qrViewModelRef = it }
        }
        factory {
            mockk<TagDetailDDayViewModel>(relaxed = true)
                .apply {
                    every { ddayPagingData } returns ddayPagingDataFlow
                    every { effect } returns ddayEffectFlow
                    every { scopeUiState } returns MutableStateFlow(TagDetailScopeUiState())
                }.also { ddayViewModelRef = it }
        }
        factory {
            mockk<TagDetailPlaceMapViewModel>(relaxed = true)
                .apply { every { uiState } returns placeMapUiStateFlow }
                .also { placeMapViewModelRef = it }
        }
        factory {
            syncViewModelRef
                ?: mockk<SyncRefreshViewModel>(relaxed = true)
                    .apply { every { uiState } returns syncUiStateFlow }
                    .also { syncViewModelRef = it }
        }
    }

internal fun ComposeContentTestRule.setTagDetailScreen(
    viewModel: TagDetailViewModel,
    id: Uuid = FIRST_TAG_ID,
    detailIdState: State<Uuid> = mutableStateOf(id),
    viewModelFor: (Uuid) -> TagDetailViewModel = { viewModel },
    resultEventBus: ResultEventBus = ResultEventBus(),
    navigateToTagAdd: () -> Unit = {},
    linkUiState: TagLinkInputUiState = TagLinkInputUiState(),
    tagPagingData: PagingData<Tag> = PagingData.empty(),
    memoPagingData: PagingData<MemoListItem> = PagingData.empty(),
    memoListUiState: SyncRefreshUiState = SyncRefreshUiState(),
    webPagingData: PagingData<Web> = PagingData.empty(),
    placePagingData: PagingData<Place> = PagingData.empty(),
    contactPagingData: PagingData<ContactListItem> = PagingData.empty(),
    checklistPagingData: PagingData<ChecklistSummary> = PagingData.empty(),
    musicPagingData: PagingData<Music> = PagingData.empty(),
    qrPagingData: PagingData<Qr> = PagingData.empty(),
    ddayPagingData: PagingData<DDay> = PagingData.empty(),
    componentVisible: TagDetailScaffoldComponentVisible = TagDetailScaffoldComponentVisible(),
    navigateUp: () -> Unit = {},
    navigateToDetail: (Uuid) -> Unit = {},
    navigateToMemoAdd: () -> Unit = {},
    navigateToMemoDetail: (Uuid) -> Unit = {},
    navigateToMemoFinishedList: () -> Unit = {},
    navigateToWebAdd: () -> Unit = {},
    navigateToWebDetail: (Uuid) -> Unit = {},
    navigateToPlaceAdd: (Coordinate?) -> Unit = {},
    navigateToPlaceDetail: (Uuid) -> Unit = {},
    navigateToContactAdd: () -> Unit = {},
    navigateToContactDetail: (Uuid) -> Unit = {},
    navigateToChecklistAdd: () -> Unit = {},
    navigateToChecklistDetail: (Uuid) -> Unit = {},
    navigateToMusicAdd: () -> Unit = {},
    navigateToMusicDetail: (Uuid) -> Unit = {},
    navigateToQrAdd: () -> Unit = {},
    navigateToQrDetail: (Uuid) -> Unit = {},
    navigateToDDayAdd: () -> Unit = {},
    navigateToDDayDetail: (Uuid) -> Unit = {},
    mapContentOverride: DiaryMapContentOverride = PlatformDiaryMapContentOverride,
) {
    prepareTagDetailTabViewModels(
        linkUiState = linkUiState,
        tagPagingData = tagPagingData,
        memoPagingData = memoPagingData,
        memoListUiState = memoListUiState,
        webPagingData = webPagingData,
        placePagingData = placePagingData,
        contactPagingData = contactPagingData,
        checklistPagingData = checklistPagingData,
        musicPagingData = musicPagingData,
        qrPagingData = qrPagingData,
        ddayPagingData = ddayPagingData,
    )

    setContent {
        TagDetailScreenTestHost(resultEventBus = resultEventBus, mapContentOverride = mapContentOverride) {
            val currentId = detailIdState.value

            TagDetailScreen(
                navigateToTagAdd = navigateToTagAdd,
                tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                navigateUp = navigateUp,
                navigateToDetail = navigateToDetail,
                navigateToMemoAdd = navigateToMemoAdd,
                navigateToMemoDetail = navigateToMemoDetail,
                navigateToMemoFinishedList = navigateToMemoFinishedList,
                id = currentId,
                componentVisibleProvider = { componentVisible },
                detailViewModel = remember(currentId) { viewModelFor(currentId) },
                placeMapViewModel = koinViewModel(),
                navigateToWebAdd = navigateToWebAdd,
                navigateToWebDetail = navigateToWebDetail,
                navigateToPlaceAdd = navigateToPlaceAdd,
                navigateToPlaceDetail = navigateToPlaceDetail,
                navigateToContactAdd = navigateToContactAdd,
                navigateToContactDetail = navigateToContactDetail,
                navigateToChecklistAdd = navigateToChecklistAdd,
                navigateToChecklistDetail = navigateToChecklistDetail,
                navigateToMusicAdd = navigateToMusicAdd,
                navigateToMusicDetail = navigateToMusicDetail,
                navigateToQrAdd = navigateToQrAdd,
                navigateToQrDetail = navigateToQrDetail,
                navigateToDDayAdd = navigateToDDayAdd,
                navigateToDDayDetail = navigateToDDayDetail,
            )
        }
    }
    waitForIdle()
}

internal fun prepareTagDetailTabViewModels(
    linkUiState: TagLinkInputUiState = TagLinkInputUiState(),
    tagPagingData: PagingData<Tag> = PagingData.empty(),
    memoPagingData: PagingData<MemoListItem> = PagingData.empty(),
    memoListUiState: SyncRefreshUiState = SyncRefreshUiState(),
    webPagingData: PagingData<Web> = PagingData.empty(),
    placePagingData: PagingData<Place> = PagingData.empty(),
    contactPagingData: PagingData<ContactListItem> = PagingData.empty(),
    checklistPagingData: PagingData<ChecklistSummary> = PagingData.empty(),
    musicPagingData: PagingData<Music> = PagingData.empty(),
    qrPagingData: PagingData<Qr> = PagingData.empty(),
    ddayPagingData: PagingData<DDay> = PagingData.empty(),
    isRefreshing: Boolean = false,
) {
    linkUiStateFlow.value = linkUiState
    linkTagPagingDataFlow.value = tagPagingData
    memoPagingDataFlow.value = memoPagingData
    webPagingDataFlow.value = webPagingData
    placePagingDataFlow.value = placePagingData
    contactPagingDataFlow.value = contactPagingData
    checklistPagingDataFlow.value = checklistPagingData
    musicPagingDataFlow.value = musicPagingData
    qrPagingDataFlow.value = qrPagingData
    ddayPagingDataFlow.value = ddayPagingData
    placeListUiStateFlow.value = TagDetailPlaceListUiState()
    placeMapUiStateFlow.value = TagDetailPlaceUiState.Loading
    syncUiStateFlow.value = if (isRefreshing) SyncRefreshUiState(isRefreshing = true) else memoListUiState
    linkViewModelRef = null
    memoViewModelRef = null
    webViewModelRef = null
    placeViewModelRef = null
    contactViewModelRef = null
    checklistViewModelRef = null
    musicViewModelRef = null
    qrViewModelRef = null
    ddayViewModelRef = null
    placeMapViewModelRef = null
    syncViewModelRef = null
}

@Composable
internal fun TagDetailScreenTestHost(
    resultEventBus: ResultEventBus = ResultEventBus(),
    mapContentOverride: DiaryMapContentOverride = PlatformDiaryMapContentOverride,
    content: @Composable () -> Unit,
) {
    val viewModelStoreOwner =
        remember {
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = ViewModelStore()
            }
        }

    CompositionLocalProvider(
        LocalViewModelStoreOwner provides viewModelStoreOwner,
        LocalResultEventBus provides resultEventBus,
        LocalDiaryMapContentOverride provides mapContentOverride,
    ) {
        KoinApplication(configuration = koinConfiguration { modules(tagDetailTabViewModelModule) }) {
            DiaryTheme(content = content)
        }
    }
}

internal fun ComposeContentTestRule.selectTagDetailTab(contentDescription: String) {
    // 탭 행은 가로로 스크롤되므로 창 밖에 놓인 탭도 누를 수 있게 먼저 보이는 자리로 옮긴다.
    onNodeWithContentDescription(contentDescription).performScrollTo().performClick()
    waitForIdle()
}
