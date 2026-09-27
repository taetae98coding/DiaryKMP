package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.height
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.testing.place.coordinateInFormPrecision
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.Uuid

private const val SCROLL_PLACE_TITLE_PREFIX: String = "MemoPlaceScroll"
private const val SCROLL_PLACE_COUNT: Int = 30

internal const val MEMO_PLACE_CARD_TAG: String = "MemoPlaceCard"
internal const val HOME_PLACE_TITLE: String = "MemoPlaceHome"
internal const val OFFICE_PLACE_TITLE: String = "MemoPlaceOffice"
internal const val HOME_PLACE_QUERY: String = "Home"
internal const val KOREAN_MAP_DESCRIPTION: String = "메모 장소 지도"
internal const val DEFAULT_MAP_DESCRIPTION: String = "Memo place map"
internal const val KOREAN_SHOW_ON_MAP_ACTION: String = "지도에서 위치 보기"
internal const val DEFAULT_SHOW_ON_MAP_ACTION: String = "Show location on map"
internal const val KOREAN_PLACE_SELECT_LABEL: String = "장소 선택"
internal const val DEFAULT_PLACE_SELECT_LABEL: String = "Select place"
internal const val KOREAN_PLACE_PICKER_TITLE: String = "장소 선택"
internal const val DEFAULT_PLACE_PICKER_TITLE: String = "Select Place"
internal const val DEFAULT_PLACE_PICKER_CONFIRM: String = "Confirm"
internal const val KOREAN_PLACE_PICKER_PLACE_ADD: String = "장소 추가"
internal const val DEFAULT_PLACE_PICKER_PLACE_ADD: String = "Add place"
internal const val DEFAULT_PLACE_PICKER_SEARCH_PLACEHOLDER: String = "Search places"
internal const val KOREAN_PLACE_PICKER_SEARCH_PLACEHOLDER: String = "장소 검색"
internal const val DEFAULT_PLACE_PICKER_SEARCH_EMPTY_TITLE: String = "No search results"
internal const val DEFAULT_PLACE_PICKER_SEARCH_EMPTY_DESCRIPTION: String = "Try a different search query."
internal const val KOREAN_PLACE_PICKER_SEARCH_EMPTY_TITLE: String = "검색 결과가 없습니다"
internal const val KOREAN_PLACE_PICKER_SEARCH_EMPTY_DESCRIPTION: String = "다른 검색어로 찾아보세요"
internal const val NAVER_PROVIDER_NAME: String = "Naver"
internal const val GOOGLE_PROVIDER_NAME: String = "Google"

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

internal fun testPlace(
    title: String,
    coordinate: Coordinate = fixtureMonkey.coordinateInFormPrecision(),
): Place =
    fixtureMonkey.place(isDeleted = false).let { place ->
        place.copy(detail = place.detail.copy(title = title, coordinate = coordinate))
    }

/**
 * 칩이 칩 영역 높이를 넘겨 여러 줄이 되도록 충분히 많은 장소를 만든다.
 */
internal fun scrollTestPlaceList(): List<Place> = List(size = SCROLL_PLACE_COUNT) { index -> testPlace(title = "$SCROLL_PLACE_TITLE_PREFIX$index") }

internal fun placeCardUiState(selectedPlaceList: List<Place> = emptyList()): MemoPlaceCardUiState = MemoPlaceCardUiState(placeUiState = MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = selectedPlaceList))

internal fun placePagingData(placeList: List<Place> = emptyList()): Flow<PagingData<Place>> = MutableStateFlow(placePagingDataOf(placeList))

internal fun screenTestPlaceViewModel(
    uiState: StateFlow<MemoPlaceInputUiState> = MutableStateFlow(MemoPlaceInputUiState(isSelectedPlaceLoaded = true)),
    placePagingData: Flow<PagingData<Place>> = placePagingData(),
): MemoPlaceViewModel {
    val viewModel = mockk<MemoPlaceViewModel>(relaxed = true)
    every { viewModel.uiState } returns uiState
    every { viewModel.placePagingData } returns placePagingData
    every { viewModel.selectablePlacePagingData } returns placePagingData

    return viewModel
}

internal fun screenTestPlaceMapViewModel(uiState: StateFlow<MemoPlaceMapUiState> = MutableStateFlow(MemoPlaceMapUiState.Loading)): MemoPlaceMapViewModel {
    val viewModel = mockk<MemoPlaceMapViewModel>()
    every { viewModel.uiState } returns uiState
    every { viewModel.fetchCurrentLocation() } returns Unit

    return viewModel
}

internal fun ComposeContentTestRule.setMemoPlaceCard(
    uiStateProvider: () -> MemoPlaceCardUiState,
    onPlaceClick: (Uuid) -> Unit = {},
    onAddClick: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            MemoPlaceCard(
                uiStateProvider = uiStateProvider,
                onPlaceClick = onPlaceClick,
                onAddClick = onAddClick,
                modifier = Modifier.testTag(MEMO_PLACE_CARD_TAG),
            )
        }
    }
}

/**
 * 좌우로 나눈 배치처럼 카드가 높이를 지정받는 자리에 [MemoPlaceCard]를 배치한다.
 */
internal fun ComposeContentTestRule.setFillHeightMemoPlaceCard(
    height: Dp,
    uiStateProvider: () -> MemoPlaceCardUiState,
) {
    setContent {
        DiaryTheme {
            Column(modifier = Modifier.height(height)) {
                MemoPlaceFillHeightCard(
                    uiStateProvider = uiStateProvider,
                    onPlaceClick = {},
                    onAddClick = {},
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .testTag(MEMO_PLACE_CARD_TAG),
                )
            }
        }
    }
}

internal fun ComposeContentTestRule.memoPlaceCardHeight(): Dp = onNodeWithTag(MEMO_PLACE_CARD_TAG).getUnclippedBoundsInRoot().height

internal fun ComposeContentTestRule.setMemoPlacePickerDialog(
    placeList: List<Place> = emptyList(),
    uiState: MemoPlaceInputUiState = MemoPlaceInputUiState(isSelectedPlaceLoaded = true),
    placePagingData: Flow<PagingData<Place>> = placePagingData(placeList),
    query: String = "",
    onDismissRequest: () -> Unit = {},
    onPlaceSelect: (Uuid) -> Unit = {},
    onPlaceUnselect: (Uuid) -> Unit = {},
    onPlaceAdd: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            MemoPlacePickerDialog(
                searchFieldState = rememberDiaryPickerSearchFieldState(initialText = query),
                uiStateProvider = { uiState },
                placePagingItems = remember(placePagingData) { placePagingData }.collectAsLazyPagingItems(),
                onDismissRequest = onDismissRequest,
                onEvent = { event ->
                    when (event) {
                        is MemoPlacePickerEvent.Select -> onPlaceSelect(event.id)
                        is MemoPlacePickerEvent.Unselect -> onPlaceUnselect(event.id)
                        is MemoPlacePickerEvent.ClickAdd -> onPlaceAdd()
                        is MemoPlacePickerEvent.ChangeQuery -> Unit
                    }
                },
            )
        }
    }
}

internal fun ComposeContentTestRule.setMemoPlacePickerDialogHost(
    dialogState: DialogState = DialogState(isVisible = true),
    placeList: List<Place> = emptyList(),
    placePagingData: Flow<PagingData<Place>> = placePagingData(placeList),
    uiState: MemoPlaceInputUiState = MemoPlaceInputUiState(isSelectedPlaceLoaded = true),
    onQueryChange: (String) -> Unit = {},
    onPlaceSelect: (Uuid) -> Unit = {},
    onPlaceAdd: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            MemoPlacePickerDialogHost(
                dialogState = dialogState,
                onEvent = { event ->
                    when (event) {
                        is MemoPlacePickerEvent.Select -> onPlaceSelect(event.id)
                        is MemoPlacePickerEvent.ClickAdd -> onPlaceAdd()
                        is MemoPlacePickerEvent.ChangeQuery -> onQueryChange(event.query)
                        is MemoPlacePickerEvent.Unselect -> Unit
                    }
                },
                uiStateProvider = { uiState },
                placePagingItems = remember(placePagingData) { placePagingData }.collectAsLazyPagingItems(),
            )
        }
    }
}

internal fun ComposeContentTestRule.placeDialogSearchField(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasAnyAncestor(isDialog()))

internal fun ComposeContentTestRule.placeDialogNodeWithText(text: String): SemanticsNodeInteraction = onNode(hasText(text) and hasAnyAncestor(isDialog()))

internal fun hasClickLabel(label: String): SemanticsMatcher =
    SemanticsMatcher("has click label $label") { node ->
        node.config.getOrNull(SemanticsActions.OnClick)?.label == label
    }
