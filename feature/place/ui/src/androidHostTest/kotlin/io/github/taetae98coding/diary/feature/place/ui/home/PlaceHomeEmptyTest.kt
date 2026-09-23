package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceHomePlaceListUiState
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceList
import io.github.taetae98coding.diary.feature.place.ui.home.viewmode.PlaceHomeViewMode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class PlaceHomeEmptyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-HOME-FEATURE-046 목록 모드에 노출할 장소가 없으면 아직 장소가 없음을 알린다`() {
        setListViewModeScaffold(placePagingDataFlowOf(emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_LIST_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_LIST_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-PLACE-HOME-FEATURE-046 한국어 환경에서 목록 모드 빈 상태 안내는 아직 장소가 없습니다이다`() {
        setListViewModeScaffold(placePagingDataFlowOf(emptyList()))

        composeRule.onNodeWithText(KOREAN_LIST_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_LIST_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-047 지도 모드에서 보이는 영역 안에 장소가 없으면 이 지역에 장소가 없음을 알린다`() {
        setPlaceList(PlaceHomePlaceListUiState(isLoaded = true, placeList = emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_MAP_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_MAP_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-PLACE-HOME-FEATURE-047 한국어 환경에서 지도 모드 빈 상태 안내는 이 지역에 장소가 없습니다이다`() {
        setPlaceList(PlaceHomePlaceListUiState(isLoaded = true, placeList = emptyList()))

        composeRule.onNodeWithText(KOREAN_MAP_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_MAP_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-048 목록 모드에서 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setListViewModeScaffold(loadingPlacePagingDataFlow())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-049 보이는 영역 안에 장소가 들어오면 빈 상태 안내가 사라진다`() {
        val place = viewModeTestPlace()
        val uiStateHolder = MutableStateFlow(PlaceHomePlaceListUiState(isLoaded = true, placeList = emptyList()))
        setPlaceListFlow(uiStateFlow = uiStateHolder)
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()

        uiStateHolder.value = PlaceHomePlaceListUiState(isLoaded = true, placeList = listOf(place))
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithTag(DIARY_EMPTY_BOX_TEST_TAG).fetchSemanticsNodes().isEmpty()
        }

        composeRule.onNodeWithText(place.detail.title).assertExists()
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-050 빈 상태에서도 장소 추가와 보기 모드 전환을 실행할 수 있다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setListViewModeScaffold(placePagingDataFlowOf(emptyList()), onEvent = eventList::add)

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_MAP_VIEW_MODE_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()

        eventList.withoutMoveMap() shouldBe listOf(PlaceHomeScaffoldEvent.ClickAdd(coordinate = null))
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-051 지도 모드에서 보이는 영역을 확인하기 전에는 빈 상태 안내를 표시하지 않는다`() {
        setPlaceList(PlaceHomePlaceListUiState())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-031 목록이 비어 있어도 당김으로 새로고침할 수 있다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setPlaceList(
            uiState = PlaceHomePlaceListUiState(isLoaded = true, placeList = emptyList()),
            onEvent = eventList::add,
        )
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(PlaceHomeScaffoldEvent.Refresh)
    }

    private fun setListViewModeScaffold(
        placePagingDataFlow: MutableStateFlow<PagingData<Place>>,
        onEvent: (PlaceHomeScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            ViewModeTestPlaceHomeScaffold(
                onEvent = onEvent,
                state = PlaceHomeScaffoldState(initialViewMode = PlaceHomeViewMode.LIST),
                uiState = PlaceHomeUiState.Loading,
                placePagingDataFlow = placePagingDataFlow,
            )
        }
    }

    private fun setPlaceList(
        uiState: PlaceHomePlaceListUiState,
        onEvent: (PlaceHomeScaffoldEvent) -> Unit = {},
    ) {
        setPlaceListFlow(uiStateFlow = MutableStateFlow(uiState), onEvent = onEvent)
    }

    private fun setPlaceListFlow(
        uiStateFlow: MutableStateFlow<PlaceHomePlaceListUiState>,
        onEvent: (PlaceHomeScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                val uiState by uiStateFlow.collectAsStateWithLifecycle()

                PlaceList(
                    onEvent = onEvent,
                    placeListUiStateProvider = { uiState },
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_LIST_EMPTY_TITLE = "No places yet"
        private const val DEFAULT_LIST_EMPTY_DESCRIPTION = "Use the add button to create a place."
        private const val KOREAN_LIST_EMPTY_TITLE = "아직 장소가 없습니다"
        private const val KOREAN_LIST_EMPTY_DESCRIPTION = "추가 버튼으로 새 장소를 만들 수 있습니다"
        private const val DEFAULT_MAP_EMPTY_TITLE = "No places in this area"
        private const val DEFAULT_MAP_EMPTY_DESCRIPTION = "Move the map to see other places."
        private const val KOREAN_MAP_EMPTY_TITLE = "이 지역에 장소가 없습니다"
        private const val KOREAN_MAP_EMPTY_DESCRIPTION = "지도를 옮기면 다른 장소를 볼 수 있습니다"
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
