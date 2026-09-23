package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
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
class PlaceHomeRefreshTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-HOME-FEATURE-032 새로고침으로 받은 장소가 보이는 영역 안에 있으면 목록에 나타난다`() {
        val place = viewModeTestPlace()
        val receivedPlace = viewModeTestPlace()
        val uiStateFlow = MutableStateFlow(loadedUiState(listOf(place)))
        setPlaceList(uiStateFlow = uiStateFlow, isRefreshingFlow = MutableStateFlow(true))
        composeRule.onNodeWithText(place.detail.title).assertExists()

        uiStateFlow.value = loadedUiState(listOf(place, receivedPlace))
        composeRule.waitUntil { runCatching { composeRule.onNodeWithText(receivedPlace.detail.title).assertExists() }.isSuccess }

        composeRule.onNodeWithText(place.detail.title).assertExists()
        composeRule.onNodeWithText(receivedPlace.detail.title).assertExists()
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-033 새로고침이 실패해도 표시 중인 장소는 유지된다`() {
        val place = viewModeTestPlace()
        val isRefreshingFlow = MutableStateFlow(true)
        setPlaceList(
            uiStateFlow = MutableStateFlow(loadedUiState(listOf(place))),
            isRefreshingFlow = isRefreshingFlow,
        )
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        isRefreshingFlow.value = false
        composeRule.waitForIdle()

        composeRule.onNodeWithText(place.detail.title).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-034 진행 표시 중에도 목록의 장소를 선택할 수 있다`() {
        val place = viewModeTestPlace()
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setPlaceList(
            uiStateFlow = MutableStateFlow(loadedUiState(listOf(place))),
            isRefreshingFlow = MutableStateFlow(true),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithText(place.detail.title).performClick()
        composeRule.waitForIdle()

        eventList.withoutMoveMap() shouldBe listOf(PlaceHomeScaffoldEvent.ClickPlace(id = place.id))
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-034 진행 표시 중에도 장소 추가로 이동할 수 있다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        composeRule.setContent {
            ViewModeTestPlaceHomeScaffold(
                onEvent = eventList::add,
                state = PlaceHomeScaffoldState(initialViewMode = PlaceHomeViewMode.LIST),
                uiState = PlaceHomeUiState.Loading,
                placePagingDataFlow = placePagingDataFlowOf(listOf(viewModeTestPlace())),
                isRefreshing = true,
            )
        }

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList.withoutMoveMap() shouldBe listOf(PlaceHomeScaffoldEvent.ClickAdd(coordinate = null))
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-035 새로고침 중에 보이는 영역이 바뀌면 목록은 곧바로 갱신된다`() {
        val beforePlace = viewModeTestPlace()
        val afterPlace = viewModeTestPlace()
        val uiStateFlow = MutableStateFlow(loadedUiState(listOf(beforePlace)))
        setPlaceList(uiStateFlow = uiStateFlow, isRefreshingFlow = MutableStateFlow(true))
        composeRule.onNodeWithText(beforePlace.detail.title).assertExists()

        uiStateFlow.value = loadedUiState(listOf(afterPlace))
        composeRule.waitUntil { runCatching { composeRule.onNodeWithText(afterPlace.detail.title).assertExists() }.isSuccess }

        composeRule.onNodeWithText(beforePlace.detail.title).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    private fun setPlaceList(
        uiStateFlow: MutableStateFlow<PlaceHomePlaceListUiState>,
        isRefreshingFlow: MutableStateFlow<Boolean>,
        onEvent: (PlaceHomeScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                val uiState by uiStateFlow.collectAsStateWithLifecycle()
                val isRefreshing by isRefreshingFlow.collectAsStateWithLifecycle()

                PlaceList(
                    onEvent = onEvent,
                    placeListUiStateProvider = { uiState },
                    isRefreshingProvider = { isRefreshing },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun loadedUiState(placeList: List<Place>): PlaceHomePlaceListUiState =
        PlaceHomePlaceListUiState(
            isLoaded = true,
            placeList = placeList,
        )

    private companion object {
        private const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
    }
}
