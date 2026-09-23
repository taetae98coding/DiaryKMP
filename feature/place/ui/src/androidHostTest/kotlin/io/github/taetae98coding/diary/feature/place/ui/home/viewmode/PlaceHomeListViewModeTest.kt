package io.github.taetae98coding.diary.feature.place.ui.home.viewmode

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.place.PLACE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.place.ui.home.DEFAULT_ADD_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.home.PlaceHomeScaffoldEvent
import io.github.taetae98coding.diary.feature.place.ui.home.PlaceHomeScaffoldState
import io.github.taetae98coding.diary.feature.place.ui.home.PlaceHomeUiState
import io.github.taetae98coding.diary.feature.place.ui.home.ViewModeTestPlaceHomeScaffold
import io.github.taetae98coding.diary.feature.place.ui.home.placePagingDataFlowOf
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
class PlaceHomeListViewModeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-HOME-FEATURE-039 지도가 표시되지 않아도 목록 모드의 목록을 표시한다`() {
        val place = viewModeTestPlace()

        setListViewModePlaceHomeScaffold(placeList = listOf(place))

        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-040 목록 모드에 노출할 장소가 없으면 장소 카드를 표시하지 않는다`() {
        setListViewModePlaceHomeScaffold()

        composeRule.onAllNodesWithTag(PLACE_CARD_TEST_TAG).fetchSemanticsNodes().size shouldBe 0
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-041 목록 모드에서 장소를 선택하면 그 장소로 상세 이동을 요청한다`() {
        val place = viewModeTestPlace()
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setListViewModePlaceHomeScaffold(
            placeList = listOf(place),
            onEvent = { event -> eventList += event },
        )

        composeRule.onNodeWithText(place.detail.title).performClick()
        composeRule.waitForIdle()

        eventList.withoutMoveMap() shouldBe listOf(PlaceHomeScaffoldEvent.ClickPlace(id = place.id))
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-045 목록 모드에서 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setListViewModePlaceHomeScaffold(
            placeList = listOf(viewModeTestPlace()),
            onEvent = { event -> eventList += event },
        )

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList.withoutMoveMap() shouldBe listOf(PlaceHomeScaffoldEvent.Refresh)
    }

    @Test
    fun `TC-PLACE-HOME-DOMAIN-019 목록 모드에서 장소 추가를 선택하면 지도 위치를 넘기지 않는다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setListViewModePlaceHomeScaffold(onEvent = { event -> eventList += event })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList.withoutMoveMap() shouldBe listOf(PlaceHomeScaffoldEvent.ClickAdd(coordinate = null))
    }

    private fun setListViewModePlaceHomeScaffold(
        placeList: List<Place> = emptyList(),
        onEvent: (PlaceHomeScaffoldEvent) -> Unit = {},
    ) {
        val placePagingDataFlow = placePagingDataFlowOf(placeList)

        composeRule.setContent {
            ViewModeTestPlaceHomeScaffold(
                onEvent = onEvent,
                state = PlaceHomeScaffoldState(initialViewMode = PlaceHomeViewMode.LIST),
                uiState = PlaceHomeUiState.Loading,
                placePagingDataFlow = placePagingDataFlow,
            )
        }
    }
}
