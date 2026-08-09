package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
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
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-003 검색어가 비어 있으면 결과를 표시하지 않는다`() {
        val place = searchedPlace(name = PLACE_NAME, address = PLACE_ADDRESS)

        composeRule.setPlaceSearchContent(uiStateProvider = { PlaceSearchUiState.Loaded(placeList = listOf(place)) })

        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).assertExists()
        composeRule
            .onAllNodes(hasText(place.name))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-004 검색어를 입력하면 찾은 장소가 목록에 표시된다`() {
        val first = searchedPlace(name = FIRST_PLACE_NAME, address = PLACE_ADDRESS)
        val second = searchedPlace(name = SECOND_PLACE_NAME, address = PLACE_ADDRESS)

        composeRule.setPlaceSearchContent(uiStateProvider = { PlaceSearchUiState.Loaded(placeList = listOf(first, second)) })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(QUERY)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(first.name).assertExists()
        composeRule.onNodeWithText(second.name).assertExists()
    }

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-005 결과 항목에 이름과 주소가 함께 표시된다`() {
        val place = searchedPlace(name = PLACE_NAME, address = PLACE_ADDRESS)

        composeRule.setPlaceSearchContent(uiStateProvider = { PlaceSearchUiState.Loaded(placeList = listOf(place)) })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(QUERY)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(place.name).assertExists()
        composeRule.onNodeWithText(place.address).assertExists()
    }

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-006 결과가 없으면 결과 없음 안내가 표시된다`() {
        composeRule.setPlaceSearchContent(uiStateProvider = { PlaceSearchUiState.Loaded() })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(QUERY)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_EMPTY_MESSAGE).assertExists()
        composeRule
            .onAllNodes(hasText(DEFAULT_FAILED_MESSAGE))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-007 검색에 실패하면 실패 안내가 표시된다`() {
        val place = searchedPlace(name = PLACE_NAME, address = PLACE_ADDRESS)
        val uiState = mutableStateOf<PlaceSearchUiState>(PlaceSearchUiState.Loaded(placeList = listOf(place)))

        composeRule.setPlaceSearchContent(uiStateProvider = { uiState.value })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(QUERY)
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
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-008 검색어를 지우면 결과가 사라진다`() {
        val place = searchedPlace(name = PLACE_NAME, address = PLACE_ADDRESS)

        composeRule.setPlaceSearchContent(uiStateProvider = { PlaceSearchUiState.Loaded(placeList = listOf(place)) })

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(QUERY)
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
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-010 목록의 장소를 고르면 고른 장소가 전달된다`() {
        val place = searchedPlace(name = PLACE_NAME, address = PLACE_ADDRESS)
        var selected: SearchedPlace? = null

        composeRule.setPlaceSearchContent(
            uiStateProvider = { PlaceSearchUiState.Loaded(placeList = listOf(place)) },
            onSelect = { value -> selected = value },
        )

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(QUERY)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(place.name).assert(hasClickAction()).performClick()
        composeRule.waitForIdle()

        selected shouldBe place
    }

    private companion object {
        private const val QUERY = "place"
        private const val PLACE_NAME = "Named Place"
        private const val PLACE_ADDRESS = "Road Address"
        private const val FIRST_PLACE_NAME = "First Place"
        private const val SECOND_PLACE_NAME = "Second Place"
    }
}
