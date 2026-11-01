package io.github.taetae98coding.diary.feature.place.ui.home.viewmode

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.place.ui.home.DEFAULT_LIST_VIEW_MODE_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.home.DEFAULT_MAP_PROVIDER_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.home.DEFAULT_MAP_VIEW_MODE_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.home.DEFAULT_SEARCH_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.home.KOREAN_LIST_VIEW_MODE_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.home.KOREAN_MAP_VIEW_MODE_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.home.PlaceHomeScaffoldEvent
import io.github.taetae98coding.diary.feature.place.ui.home.PlaceHomeScaffoldState
import io.github.taetae98coding.diary.feature.place.ui.home.PlaceHomeUiState
import io.github.taetae98coding.diary.feature.place.ui.home.ViewModeTestPlaceHomeScaffold
import io.github.taetae98coding.diary.feature.place.ui.home.placePagingDataFlowOf
import io.github.taetae98coding.diary.feature.place.ui.home.rememberPlaceHomeScaffoldState
import io.github.taetae98coding.diary.feature.place.ui.home.viewModeTestPlace
import io.github.taetae98coding.diary.feature.place.ui.home.withoutMoveMap
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class PlaceHomeViewModeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-6e8c1c58-3b06-481a-a8cd-f214abf35584 진입하면 계정의 장소 전체 목록을 표시하지 않는 지도 모드로 시작한다`() {
        val place = viewModeTestPlace()

        setPlaceHomeScaffold(placeList = listOf(place))

        composeRule.onNodeWithText(place.detail.title).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-05161899-429d-4417-b1ce-f8ba45295c43 보기 모드를 바꾸면 계정의 장소가 목록에 표시된다`() {
        val place = viewModeTestPlace()
        setPlaceHomeScaffold(placeList = listOf(place))

        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_MAP_PROVIDER_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-7cebc285-6fc6-493a-956b-59f375f83e0d 목록 모드에서 다시 전환하면 목록이 사라지고 지도 모드의 본문으로 돌아온다`() {
        val place = viewModeTestPlace()
        setPlaceHomeScaffold(
            state = PlaceHomeScaffoldState(initialViewMode = PlaceHomeViewMode.LIST),
            uiState = PlaceHomeUiState.Content(defaultProvider = MapProvider.GOOGLE, initialCoordinate = null),
            placeList = listOf(place),
        )
        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_MAP_PROVIDER_DESCRIPTION).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(DEFAULT_MAP_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(place.detail.title).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_MAP_PROVIDER_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `TC-65378f37-f7f6-4bba-a121-f6b2289f8fc9 화면이 회전하거나 창 크기가 바뀌어도 목록 모드의 목록을 유지한다`() {
        val place = viewModeTestPlace()
        val placePagingDataFlow = placePagingDataFlowOf(listOf(place))
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            ViewModeTestPlaceHomeScaffold(
                onEvent = {},
                state = rememberPlaceHomeScaffoldState(),
                uiState = PlaceHomeUiState.Loading,
                placePagingDataFlow = placePagingDataFlow,
            )
        }
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_MAP_VIEW_MODE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-9320d463-7a36-4e65-980d-08c87e8c5838 보기 모드를 바꿔도 새로고침을 요청하지 않는다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setPlaceHomeScaffold(onEvent = { event -> eventList += event })

        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList.withoutMoveMap() shouldBe emptyList()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 보기 모드 전환 버튼은 이동할 모드를 알린다`() {
        setPlaceHomeScaffold(state = PlaceHomeScaffoldState(initialViewMode = PlaceHomeViewMode.LIST))

        composeRule.onNodeWithContentDescription(KOREAN_MAP_VIEW_MODE_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_LIST_VIEW_MODE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-41fc41e1-dd6c-4625-924f-7c992bc83387 목록 모드에서도 검색 버튼을 표시한다`() {
        setPlaceHomeScaffold(state = PlaceHomeScaffoldState(initialViewMode = PlaceHomeViewMode.LIST))

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_DESCRIPTION).assertExists()
    }

    private fun setPlaceHomeScaffold(
        state: PlaceHomeScaffoldState = PlaceHomeScaffoldState(),
        uiState: PlaceHomeUiState = PlaceHomeUiState.Loading,
        placeList: List<Place> = emptyList(),
        onEvent: (PlaceHomeScaffoldEvent) -> Unit = {},
    ) {
        val placePagingDataFlow = placePagingDataFlowOf(placeList)

        composeRule.setContent {
            ViewModeTestPlaceHomeScaffold(
                onEvent = onEvent,
                state = state,
                uiState = uiState,
                placePagingDataFlow = placePagingDataFlow,
            )
        }
    }
}
