package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.place.PLACE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceHomePlaceListUiState
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceList
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class PlaceHomePlaceListTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-588ffabf-ec6f-4f9b-9ef9-8cf4bcf16a43 지도 모드의 목록에서 지도 가운데에 있지 않은 장소를 누르면 그 장소의 좌표로 지도를 옮기고 상세 이동은 요청하지 않는다`() {
        val selectedPlace = viewModeTestPlace()
        val otherPlace = viewModeTestPlace()
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        val mapState = setPlaceList(placeList = listOf(selectedPlace, otherPlace), onEvent = eventList::add)

        composeRule.runOnIdle { mapState.moveTo(otherPlace.detail.coordinate.toDiaryMapCoordinate()) }
        composeRule.onNodeWithText(selectedPlace.detail.title).performClick()
        composeRule.runOnIdle { mapState.coordinate shouldBe selectedPlace.detail.coordinate.toDiaryMapCoordinate() }

        composeRule.runOnIdle { mapState.moveTo(otherPlace.detail.coordinate.toDiaryMapCoordinate()) }
        composeRule.onNodeWithText(selectedPlace.detail.title).performClick()
        composeRule.runOnIdle { mapState.coordinate shouldBe selectedPlace.detail.coordinate.toDiaryMapCoordinate() }

        eventList.withoutMoveMap() shouldBe emptyList()
    }

    @Test
    fun `TC-15a78d5c-a0f7-4fa7-950d-25b3b2b43a55 지도 모드에서 지도 가운데에 있는 장소를 목록에서 누르면 그 장소의 상세 이동을 한 번 요청한다`() {
        val selectedPlace = viewModeTestPlace()
        val otherPlace = viewModeTestPlace()
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        val mapState = setPlaceList(placeList = listOf(selectedPlace, otherPlace), onEvent = eventList::add)

        composeRule.runOnIdle { mapState.moveTo(selectedPlace.detail.coordinate.toDiaryMapCoordinate()) }
        composeRule.onNodeWithText(selectedPlace.detail.title).performClick()
        composeRule.waitForIdle()

        eventList.withoutMoveMap() shouldBe listOf(PlaceHomeScaffoldEvent.ClickPlace(id = selectedPlace.id))
        mapState.coordinate shouldBe selectedPlace.detail.coordinate.toDiaryMapCoordinate()
    }

    @Test
    fun `TC-3ed7810b-552b-4868-b739-31dbf8784142 지도 모드의 목록에서 같은 장소를 두 번 누르면 처음에는 지도를 옮기고 다음에는 상세 이동을 요청한다`() {
        val selectedPlace = viewModeTestPlace()
        val otherPlace = viewModeTestPlace()
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        val mapState = setPlaceList(placeList = listOf(selectedPlace, otherPlace), onEvent = eventList::add)

        composeRule.runOnIdle { mapState.moveTo(otherPlace.detail.coordinate.toDiaryMapCoordinate()) }
        composeRule.onNodeWithText(selectedPlace.detail.title).performClick()
        composeRule.waitForIdle()

        mapState.coordinate shouldBe selectedPlace.detail.coordinate.toDiaryMapCoordinate()
        eventList.withoutMoveMap() shouldBe emptyList()

        composeRule.onNodeWithText(selectedPlace.detail.title).performClick()
        composeRule.waitForIdle()

        eventList.withoutMoveMap() shouldBe listOf(PlaceHomeScaffoldEvent.ClickPlace(id = selectedPlace.id))
    }

    @Test
    fun `지도 모드의 장소 카드에는 지도에서 위치를 보여 준다는 동작 접근성 이름을 제공한다`() {
        val place = viewModeTestPlace()

        composeRule.setContent {
            DiaryTheme {
                PlaceList(
                    onEvent = {},
                    placeListUiStateProvider = { PlaceHomePlaceListUiState(isLoaded = true, placeList = listOf(place)) },
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(place.detail.title)).assert(hasClickLabel(DEFAULT_SHOW_ON_MAP_ACTION))
    }

    @Test
    fun `TC-5ac56bc9-b05f-4dff-950c-d9b6eacba51d 지도 모드의 장소 목록에서 카드를 삭제 방향으로 밀면 그 장소의 삭제만 요청한다`() {
        val deletedPlace = viewModeTestPlace()
        val otherPlace = viewModeTestPlace()
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()

        composeRule.setContent {
            DiaryTheme {
                PlaceList(
                    onEvent = eventList::add,
                    placeListUiStateProvider = {
                        PlaceHomePlaceListUiState(
                            isLoaded = true,
                            placeList = listOf(deletedPlace, otherPlace),
                        )
                    },
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(deletedPlace.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList.withoutMoveMap() shouldBe listOf(PlaceHomeScaffoldEvent.DeletePlace(id = deletedPlace.id))
    }

    private fun setPlaceList(
        placeList: List<Place>,
        onEvent: (PlaceHomeScaffoldEvent) -> Unit,
    ): DiaryMapState {
        val mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)

        composeRule.setContent {
            DiaryTheme {
                PlaceList(
                    onEvent = onEvent,
                    mapState = mapState,
                    placeListUiStateProvider = { PlaceHomePlaceListUiState(isLoaded = true, placeList = placeList) },
                )
            }
        }
        composeRule.waitForIdle()

        return mapState
    }

    private companion object {
        private const val DEFAULT_SHOW_ON_MAP_ACTION = "Show location on map"

        private fun hasClickLabel(label: String): SemanticsMatcher = SemanticsMatcher("OnClick label is $label") { node -> node.config.getOrNull(SemanticsActions.OnClick)?.label == label }
    }
}
