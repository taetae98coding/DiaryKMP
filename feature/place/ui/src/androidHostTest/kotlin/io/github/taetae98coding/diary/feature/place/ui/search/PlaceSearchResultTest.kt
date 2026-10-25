package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceSearchResultTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-8613724f-e814-4641-a21d-94ca669415fd 검색어가 비어 있으면 결과를 표시하지 않는다`() {
        val placeName = randomText(prefix = PLACE_NAME_PREFIX)
        val placeAddress = randomText(prefix = PLACE_ADDRESS_PREFIX)
        val place = searchedPlace(name = placeName, address = placeAddress)

        composeRule.setPlaceSearchContent(uiStateProvider = { PlaceSearchUiState.Content(placeList = listOf(place)) })

        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).assertExists()
        composeRule
            .onAllNodes(hasText(place.name))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    @Test
    fun `TC-7fcc2059-175a-4c24-bddb-b370ce5dde6e 검색어를 입력하면 찾은 장소가 받은 순서대로 목록에 표시된다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val placeAddress = randomText(prefix = PLACE_ADDRESS_PREFIX)
        val firstPlaceName = randomText(prefix = FIRST_PLACE_NAME_PREFIX)
        val secondPlaceName = randomText(prefix = SECOND_PLACE_NAME_PREFIX)
        val first = searchedPlace(name = firstPlaceName, address = placeAddress)
        val second = searchedPlace(name = secondPlaceName, address = placeAddress)

        composeRule.setPlaceSearchContent(uiStateProvider = { PlaceSearchUiState.Content(placeList = listOf(first, second)) })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(first.name).assertExists()
        composeRule.onNodeWithText(second.name).assertExists()
        composeRule.onNodeWithText(first.name).getUnclippedBoundsInRoot().top shouldBeLessThan
            composeRule.onNodeWithText(second.name).getUnclippedBoundsInRoot().top
    }

    @Test
    fun `TC-07080369-8d01-4737-95b1-e856e13270ae 결과 항목에 이름과 주소가 함께 표시된다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val placeName = randomText(prefix = PLACE_NAME_PREFIX)
        val placeAddress = randomText(prefix = PLACE_ADDRESS_PREFIX)
        val place = searchedPlace(name = placeName, address = placeAddress)

        composeRule.setPlaceSearchContent(uiStateProvider = { PlaceSearchUiState.Content(placeList = listOf(place)) })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(place.name).assertExists()
        composeRule.onNodeWithText(place.address).assertExists()
    }

    @Test
    fun `TC-0b99edc9-1e06-4e1d-b80a-e4e1f0d37bf8 결과가 없으면 결과 없음 안내가 표시된다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        composeRule.setPlaceSearchContent(uiStateProvider = { PlaceSearchUiState.Content() })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_EMPTY_MESSAGE).assertExists()
        composeRule
            .onAllNodes(hasText(DEFAULT_FAILED_MESSAGE))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    @Test
    fun `TC-8b804367-027f-4059-98fe-215c08778d31 검색에 실패하면 실패 안내가 표시된다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val placeName = randomText(prefix = PLACE_NAME_PREFIX)
        val placeAddress = randomText(prefix = PLACE_ADDRESS_PREFIX)
        val place = searchedPlace(name = placeName, address = placeAddress)
        val uiState = mutableStateOf<PlaceSearchUiState>(PlaceSearchUiState.Content(placeList = listOf(place)))

        composeRule.setPlaceSearchContent(uiStateProvider = { uiState.value })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.waitForIdle()
        composeRule.runOnIdle { uiState.value = PlaceSearchUiState.Failed }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_FAILED_MESSAGE).assertExists()
        composeRule
            .onAllNodes(hasText(place.name))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    @Test
    fun `TC-514c3942-86ec-40d4-a90a-696a6bb33b8b 검색어를 지우면 결과가 사라진다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val placeName = randomText(prefix = PLACE_NAME_PREFIX)
        val placeAddress = randomText(prefix = PLACE_ADDRESS_PREFIX)
        val place = searchedPlace(name = placeName, address = placeAddress)

        composeRule.setPlaceSearchContent(uiStateProvider = { PlaceSearchUiState.Content(placeList = listOf(place)) })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(place.name).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule
            .onAllNodes(hasText(place.name))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    @Test
    fun `TC-779e6a90-ec04-4057-9765-0a0c9f60a0a9 목록의 장소를 누르면 지도가 그 장소의 좌표를 가운데에 둔다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val place = searchedPlace(name = randomText(prefix = PLACE_NAME_PREFIX), address = randomText(prefix = PLACE_ADDRESS_PREFIX))
        val otherCoordinate = DiaryMapCoordinate(latitude = place.coordinate.latitude + 1.0, longitude = place.coordinate.longitude + 1.0)
        val placeCoordinate = DiaryMapCoordinate(latitude = place.coordinate.latitude, longitude = place.coordinate.longitude)
        val state = composeRule.setPlaceSearchContent(uiStateProvider = { PlaceSearchUiState.Content(placeList = listOf(place)) })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.runOnIdle { state().mapState.moveTo(otherCoordinate) }
        composeRule.onNodeWithText(place.name).assert(hasClickAction()).performClick()
        composeRule.runOnIdle { state().mapState.coordinate shouldBe placeCoordinate }

        composeRule.runOnIdle { state().mapState.moveTo(otherCoordinate) }
        composeRule.onNodeWithText(place.name).performClick()
        composeRule.runOnIdle { state().mapState.coordinate shouldBe placeCoordinate }
    }

    @Test
    fun `TC-a0ec67c2-62cf-4a51-8088-dace5a7091e1 지도 가운데에 있지 않은 장소를 목록에서 눌러도 장소를 고르지 않고 결과 목록이 그대로 표시된다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val first = searchedPlace(name = randomText(prefix = FIRST_PLACE_NAME_PREFIX), address = randomText(prefix = PLACE_ADDRESS_PREFIX))
        val second = searchedPlace(name = randomText(prefix = SECOND_PLACE_NAME_PREFIX), address = randomText(prefix = PLACE_ADDRESS_PREFIX))
        val selectedList = mutableListOf<SearchedPlace>()
        val state =
            composeRule.setPlaceSearchContent(
                uiStateProvider = { PlaceSearchUiState.Content(placeList = listOf(first, second)) },
                onSelect = selectedList::add,
            )

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.runOnIdle { state().mapState.moveTo(first.coordinate.toDiaryMapCoordinate().shifted()) }
        composeRule.onNodeWithText(first.name).performClick()
        composeRule.waitForIdle()

        selectedList shouldBe emptyList()
        composeRule.onNodeWithText(first.name).assertExists()
        composeRule.onNodeWithText(second.name).assertExists()
        composeRule.runOnIdle { state().query shouldBe query }
    }

    @Test
    fun `TC-97ce25be-aefd-4aa2-a719-ce41283abafd 지도 가운데에 있는 장소를 목록에서 누르면 그 장소를 고른다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val place = searchedPlace(name = randomText(prefix = PLACE_NAME_PREFIX), address = randomText(prefix = PLACE_ADDRESS_PREFIX))
        val selectedList = mutableListOf<SearchedPlace>()
        val state =
            composeRule.setPlaceSearchContent(
                uiStateProvider = { PlaceSearchUiState.Content(placeList = listOf(place)) },
                onSelect = selectedList::add,
            )

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.runOnIdle { state().mapState.moveTo(place.coordinate.toDiaryMapCoordinate()) }
        composeRule.onNodeWithText(place.name).performClick()
        composeRule.waitForIdle()

        selectedList shouldBe listOf(place)
        composeRule.runOnIdle { state().mapState.coordinate shouldBe place.coordinate.toDiaryMapCoordinate() }
    }

    @Test
    fun `TC-a48e3fc4-c94a-4708-8d7a-534704f7eb09 목록의 장소를 두 번 누르면 처음에는 지도를 옮기고 다음에는 그 장소를 고른다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val place = searchedPlace(name = randomText(prefix = PLACE_NAME_PREFIX), address = randomText(prefix = PLACE_ADDRESS_PREFIX))
        val selectedList = mutableListOf<SearchedPlace>()
        val state =
            composeRule.setPlaceSearchContent(
                uiStateProvider = { PlaceSearchUiState.Content(placeList = listOf(place)) },
                onSelect = selectedList::add,
            )

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.runOnIdle { state().mapState.moveTo(place.coordinate.toDiaryMapCoordinate().shifted()) }
        composeRule.onNodeWithText(place.name).performClick()
        composeRule.runOnIdle {
            state().mapState.coordinate shouldBe place.coordinate.toDiaryMapCoordinate()
            selectedList shouldBe emptyList()
        }

        composeRule.onNodeWithText(place.name).performClick()
        composeRule.runOnIdle { selectedList shouldBe listOf(place) }
    }

    @Test
    fun `TC-ccb05c09-28aa-430c-ac57-052686eb9f4f 결과 항목마다 그 장소의 컬러 표시가 하나씩 있다`() {
        val query = randomText(prefix = QUERY_PREFIX)
        val placeList =
            listOf(FIRST_PLACE_NAME_PREFIX, SECOND_PLACE_NAME_PREFIX).map { prefix ->
                searchedPlace(name = randomText(prefix = prefix), address = randomText(prefix = PLACE_ADDRESS_PREFIX))
            }

        composeRule.setPlaceSearchContent(uiStateProvider = { PlaceSearchUiState.Content(placeList = placeList) })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(query)
        composeRule.waitForIdle()

        composeRule
            .onAllNodesWithTag(PLACE_SEARCH_ROW_COLOR_INDICATOR_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .size shouldBe placeList.size
    }

    private companion object {
        private const val QUERY_PREFIX = "query"
        private const val PLACE_NAME_PREFIX = "place"
        private const val PLACE_ADDRESS_PREFIX = "address"
        private const val FIRST_PLACE_NAME_PREFIX = "first-place"
        private const val SECOND_PLACE_NAME_PREFIX = "second-place"
        private const val SHIFT_DEGREE = 1.0

        private fun DiaryMapCoordinate.shifted(): DiaryMapCoordinate = copy(latitude = latitude + SHIFT_DEGREE, longitude = longitude + SHIFT_DEGREE)
    }
}
