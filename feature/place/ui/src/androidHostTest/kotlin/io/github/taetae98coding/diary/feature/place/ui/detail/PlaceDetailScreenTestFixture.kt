package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.MemoListUiState
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.place.ui.detail.memo.PlaceDetailMemoSyncViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.memo.PlaceDetailMemoViewModel
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceDetailFormState
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchUiState
import io.github.taetae98coding.diary.feature.place.ui.search.PlaceSearchViewModel
import io.github.taetae98coding.diary.library.compose.ui.color.toColorLong
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import kotlin.uuid.Uuid

internal const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
internal const val DEFAULT_DETAIL_TAB_DESCRIPTION = "Place detail"
internal const val DEFAULT_MEMO_TAB_DESCRIPTION = "Memo"
internal const val KOREAN_DETAIL_TAB_DESCRIPTION = "장소 디테일"
internal const val KOREAN_MEMO_TAB_DESCRIPTION = "메모"
internal const val DEFAULT_MEMO_ADD_DESCRIPTION = "Add memo"
internal const val KOREAN_MEMO_ADD_DESCRIPTION = "메모 추가"
internal const val DEFAULT_SEARCH_BUTTON_DESCRIPTION = "Search place"
internal val FIRST_PLACE_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000001")

private const val EFFECT_BUFFER_CAPACITY = 8

// KoinApplication에 넘긴 모듈은 첫 테스트의 것이 이어서 쓰이므로, 메모 탭이 얻는 값은 모듈이 붙잡는 이 흐름들로 테스트마다 바꾼다.
internal val memoPagingDataFlow = MutableStateFlow(PagingData.empty<MemoListItem>())
internal val memoListUiStateFlow = MutableStateFlow(MemoListUiState())
internal val memoEffectFlow = MutableSharedFlow<MemoListEffect>(extraBufferCapacity = EFFECT_BUFFER_CAPACITY)

internal var memoViewModelRef: PlaceDetailMemoViewModel? = null
    private set

internal var memoSyncViewModelRef: PlaceDetailMemoSyncViewModel? = null
    private set

private val placeDetailTabViewModelModule =
    module {
        factory {
            mockk<PlaceDetailMemoViewModel>(relaxed = true)
                .apply {
                    every { memoPagingData } returns memoPagingDataFlow
                    every { sort } returns MutableStateFlow(ListSort.DEFAULT)
                    every { effect } returns memoEffectFlow
                }.also { memoViewModelRef = it }
        }
        factory {
            mockk<PlaceDetailMemoSyncViewModel>(relaxed = true)
                .apply { every { uiState } returns memoListUiStateFlow }
                .also { memoSyncViewModelRef = it }
        }
    }

internal fun preparePlaceDetailTabViewModels(
    memoPagingData: PagingData<MemoListItem> = PagingData.empty(),
    memoListUiState: MemoListUiState = MemoListUiState(),
) {
    memoPagingDataFlow.value = memoPagingData
    memoListUiStateFlow.value = memoListUiState
    memoViewModelRef = null
    memoSyncViewModelRef = null
}

internal const val DEFAULT_UPDATE_BUTTON_DESCRIPTION = "Update place"
internal const val DEFAULT_DELETE_BUTTON_DESCRIPTION = "Delete place"
internal const val DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION = "Open in Naver Map"
internal const val DEFAULT_OPEN_GOOGLE_MAP_BUTTON_DESCRIPTION = "Open in Google Maps"
internal const val DEFAULT_MAP_DESCRIPTION = "Place location map"
internal const val DEFAULT_UPDATE_SUCCEEDED_MESSAGE = "Place updated."
internal const val DEFAULT_COORDINATE_INVALID_MESSAGE =
    "Select a location on the map, or enter a latitude between -90 and 90 and a longitude between -180 and 180."

internal const val TITLE_INDEX = 0
internal const val DESCRIPTION_INDEX = 1
internal const val ADDRESS_INDEX = 2
internal const val LATITUDE_INDEX = 3
internal const val LONGITUDE_INDEX = 4

// 제목, 설명, 주소, 위도, 경도 입력
internal const val INPUT_COUNT = 5

// 상단 바 제목과 제목 입력
internal const val TITLE_DISPLAY_COUNT = 2

internal const val CHANGED_TITLE = "바뀐 제목"
internal const val CHANGED_DESCRIPTION = "바뀐 설명"
internal const val CHANGED_ADDRESS = "바뀐 주소"
internal const val CHANGED_LATITUDE = "38.0"
internal const val CHANGED_LONGITUDE = "128.0"
internal const val INVALID_LATITUDE = "abc"

private const val SAVED_LATITUDE = 37.5665
private const val SAVED_LONGITUDE = 126.978
internal const val SAVED_LATITUDE_TEXT = "37.566500"
internal const val SAVED_LONGITUDE_TEXT = "126.978000"
private const val SAVED_COLOR = 0xFF3A7BD5
internal const val SAVED_ADDRESS = "Saved Address"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal fun placeDetail(
    title: String = "title-${fixtureMonkey.giveMeOne<String>()}",
    description: String = "description-${fixtureMonkey.giveMeOne<String>()}",
    // 컬러 입력이 다루는 값과 같은 표현을 쓰기 위해 앱이 저장하는 방식으로 만든다.
    color: Long = Color(SAVED_COLOR).toColorLong(),
    coordinate: Coordinate = Coordinate(latitude = SAVED_LATITUDE, longitude = SAVED_LONGITUDE),
    address: String = SAVED_ADDRESS,
): PlaceDetail =
    PlaceDetail(
        title = title,
        description = description,
        color = color,
        coordinate = coordinate,
        address = address,
    )

internal fun content(
    id: Uuid = Uuid.random(),
    detail: PlaceDetail = placeDetail(),
    isUpdateInProgress: Boolean = false,
    isDeleteInProgress: Boolean = false,
): PlaceDetailUiState.Content =
    PlaceDetailUiState.Content(
        id = id,
        detail = detail,
        // 호스트 테스트 환경은 지도 제공자의 표시 요소를 만들 수 없으므로 지도를 표시하지 않는 상태로 검증한다.
        defaultProvider = null,
        isUpdateInProgress = isUpdateInProgress,
        isDeleteInProgress = isDeleteInProgress,
    )

internal fun screenTestViewModel(
    uiState: StateFlow<PlaceDetailUiState> = MutableStateFlow(content()),
    effect: Flow<PlaceDetailEffect> = emptyFlow(),
): PlaceDetailViewModel {
    val viewModel = mockk<PlaceDetailViewModel>(relaxed = true)
    every { viewModel.uiState } returns uiState
    every { viewModel.effect } returns effect
    return viewModel
}

internal fun searchScreenTestViewModel(uiState: PlaceSearchUiState = PlaceSearchUiState.Idle): PlaceSearchViewModel {
    val viewModel = mockk<PlaceSearchViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    return viewModel
}

internal fun ComposeContentTestRule.input(index: Int): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[index]

internal fun detailTagScreenTestViewModel(
    tagList: List<Tag> = emptyList(),
    selectableTagList: List<Tag> = emptyList(),
): PlaceDetailTagViewModel {
    val viewModel = mockk<PlaceDetailTagViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(EntityTagInputUiState(tagList = tagList))
    every { viewModel.tagPagingData } returns MutableStateFlow(PagingData.from(selectableTagList))
    return viewModel
}

internal fun ComposeContentTestRule.setPlaceDetailScreen(
    viewModel: PlaceDetailViewModel,
    id: Uuid = FIRST_PLACE_ID,
    navigateUp: () -> Unit = {},
    navigateToTagDetail: (Uuid) -> Unit = {},
    navigateToMemoAdd: () -> Unit = {},
    navigateToMemoDetail: (Uuid) -> Unit = {},
    memoPagingData: PagingData<MemoListItem> = PagingData.empty(),
    memoListUiState: MemoListUiState = MemoListUiState(),
    searchViewModel: PlaceSearchViewModel = searchScreenTestViewModel(),
    tagViewModel: PlaceDetailTagViewModel = detailTagScreenTestViewModel(),
    uriHandler: UriHandler = mockk(relaxed = true),
) {
    preparePlaceDetailTabViewModels(memoPagingData = memoPagingData, memoListUiState = memoListUiState)

    setContent {
        CompositionLocalProvider(LocalUriHandler provides uriHandler) {
            PlaceDetailScreenTestTheme {
                PlaceDetailScreen(
                    navigateToTagAdd = {},
                    navigateToMemoAdd = navigateToMemoAdd,
                    navigateToMemoDetail = navigateToMemoDetail,
                    id = id,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = viewModel,
                    searchViewModel = searchViewModel,
                    navigateUp = navigateUp,
                    navigateToTagDetail = navigateToTagDetail,
                    tagViewModel = tagViewModel,
                )
            }
        }
    }
    waitForIdle()
}

internal fun ComposeContentTestRule.selectPlaceDetailTab(contentDescription: String) {
    onNodeWithContentDescription(contentDescription).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.setPlaceDetailScaffoldState(initialDetail: PlaceDetail): PlaceFormState {
    lateinit var state: PlaceFormState

    setContent {
        DiaryTheme {
            state = rememberPlaceDetailFormState(initialDetail = initialDetail)
        }
    }
    waitForIdle()

    return state
}

/**
 * PlaceDetail 화면은 결과 이벤트 버스를 [LocalResultEventBus]에서 읽고 메모 탭의 ViewModel을 Koin에서 얻으므로, 화면을 배치하는 테스트는 이 테마로 감싼다.
 */
@Composable
internal fun PlaceDetailScreenTestTheme(
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
        LocalResultEventBus provides resultEventBus,
        LocalViewModelStoreOwner provides viewModelStoreOwner,
    ) {
        KoinApplication(configuration = koinConfiguration { modules(placeDetailTabViewModelModule) }) {
            DiaryTheme(content = content)
        }
    }
}
