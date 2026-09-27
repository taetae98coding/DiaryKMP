package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.MemoListUiState
import io.github.taetae98coding.diary.compose.place.PlaceListEffect
import io.github.taetae98coding.diary.compose.web.WebListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.tag.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.tag.ui.detail.form.TagDetailLinkViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.memo.TagDetailMemoSyncViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.memo.TagDetailMemoViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceListUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceMapViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceViewModel
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
internal val memoListUiStateFlow = MutableStateFlow(MemoListUiState())
internal val memoEffectFlow = MutableSharedFlow<MemoListEffect>(extraBufferCapacity = EFFECT_BUFFER_CAPACITY)
internal val webPagingDataFlow = MutableStateFlow(PagingData.empty<Web>())
internal val webEffectFlow = MutableSharedFlow<WebListEffect>(extraBufferCapacity = EFFECT_BUFFER_CAPACITY)
internal val placePagingDataFlow = MutableStateFlow(PagingData.empty<Place>())
internal val placeListUiStateFlow = MutableStateFlow(TagDetailPlaceListUiState())
internal val placeEffectFlow = MutableSharedFlow<PlaceListEffect>(extraBufferCapacity = EFFECT_BUFFER_CAPACITY)
internal val placeMapUiStateFlow = MutableStateFlow<TagDetailPlaceUiState>(TagDetailPlaceUiState.Loading)
internal val isRefreshingFlow = MutableStateFlow(false)

// 탭이 Koin으로 얻는 ViewModel은 호출 검증을 위해 테스트가 참조할 수 있어야 한다.
internal var linkViewModelRef: TagDetailLinkViewModel? = null
    private set

internal var memoViewModelRef: TagDetailMemoViewModel? = null
    private set

internal var memoSyncViewModelRef: TagDetailMemoSyncViewModel? = null
    private set

internal var webViewModelRef: TagDetailWebViewModel? = null
    private set

internal var placeViewModelRef: TagDetailPlaceViewModel? = null
    private set

internal var placeMapViewModelRef: TagDetailPlaceMapViewModel? = null
    private set

internal var syncViewModelRef: TagDetailSyncViewModel? = null
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
                    every { sort } returns MutableStateFlow(ListSort.DEFAULT)
                    every { scope } returns MutableStateFlow(TagScope.SELF)
                    every { effect } returns memoEffectFlow
                }.also { memoViewModelRef = it }
        }
        factory {
            mockk<TagDetailMemoSyncViewModel>(relaxed = true)
                .apply { every { uiState } returns memoListUiStateFlow }
                .also { memoSyncViewModelRef = it }
        }
        factory {
            mockk<TagDetailWebViewModel>(relaxed = true)
                .apply {
                    every { webPagingData } returns webPagingDataFlow
                    every { effect } returns webEffectFlow
                    every { sort } returns MutableStateFlow(ListSort.TITLE)
                    every { scope } returns MutableStateFlow(TagScope.SELF)
                }.also { webViewModelRef = it }
        }
        factory {
            mockk<TagDetailPlaceViewModel>(relaxed = true)
                .apply {
                    every { placePagingData } returns placePagingDataFlow
                    every { placeListUiState } returns placeListUiStateFlow
                    every { effect } returns placeEffectFlow
                    every { sort } returns MutableStateFlow(ListSort.TITLE)
                    every { scope } returns MutableStateFlow(TagScope.SELF)
                }.also { placeViewModelRef = it }
        }
        factory {
            mockk<TagDetailPlaceMapViewModel>(relaxed = true)
                .apply { every { uiState } returns placeMapUiStateFlow }
                .also { placeMapViewModelRef = it }
        }
        factory {
            mockk<TagDetailSyncViewModel>(relaxed = true)
                .apply { every { isRefreshing } returns isRefreshingFlow }
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
    memoListUiState: MemoListUiState = MemoListUiState(),
    webPagingData: PagingData<Web> = PagingData.empty(),
    placePagingData: PagingData<Place> = PagingData.empty(),
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
) {
    prepareTagDetailTabViewModels(
        linkUiState = linkUiState,
        tagPagingData = tagPagingData,
        memoPagingData = memoPagingData,
        memoListUiState = memoListUiState,
        webPagingData = webPagingData,
        placePagingData = placePagingData,
    )

    setContent {
        TagDetailScreenTestHost(resultEventBus = resultEventBus) {
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
            )
        }
    }
    waitForIdle()
}

internal fun prepareTagDetailTabViewModels(
    linkUiState: TagLinkInputUiState = TagLinkInputUiState(),
    tagPagingData: PagingData<Tag> = PagingData.empty(),
    memoPagingData: PagingData<MemoListItem> = PagingData.empty(),
    memoListUiState: MemoListUiState = MemoListUiState(),
    webPagingData: PagingData<Web> = PagingData.empty(),
    placePagingData: PagingData<Place> = PagingData.empty(),
    isRefreshing: Boolean = false,
) {
    linkUiStateFlow.value = linkUiState
    linkTagPagingDataFlow.value = tagPagingData
    memoPagingDataFlow.value = memoPagingData
    memoListUiStateFlow.value = memoListUiState
    webPagingDataFlow.value = webPagingData
    placePagingDataFlow.value = placePagingData
    placeListUiStateFlow.value = TagDetailPlaceListUiState()
    placeMapUiStateFlow.value = TagDetailPlaceUiState.Loading
    isRefreshingFlow.value = isRefreshing
    linkViewModelRef = null
    memoViewModelRef = null
    memoSyncViewModelRef = null
    webViewModelRef = null
    placeViewModelRef = null
    placeMapViewModelRef = null
    syncViewModelRef = null
}

@Composable
internal fun TagDetailScreenTestHost(
    resultEventBus: ResultEventBus = ResultEventBus(),
    content: @Composable () -> Unit,
) {
    // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로,
    // 테스트마다 새 소유자를 제공해 이전 테스트의 탭별 ViewModel이 재사용되지 않게 한다.
    val viewModelStoreOwner =
        remember {
            object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = ViewModelStore()
            }
        }

    CompositionLocalProvider(
        LocalViewModelStoreOwner provides viewModelStoreOwner,
        LocalResultEventBus provides resultEventBus,
    ) {
        KoinApplication(configuration = koinConfiguration { modules(tagDetailTabViewModelModule) }) {
            DiaryTheme(content = content)
        }
    }
}

internal fun ComposeContentTestRule.selectTagDetailTab(contentDescription: String) {
    onNodeWithContentDescription(contentDescription).performClick()
    waitForIdle()
}
