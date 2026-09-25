package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
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
    fun `TC-PLACE-HOME-FEATURE-027 목록의 장소를 선택하면 그 장소로 상세 이동을 한 번 요청한다`() {
        val selectedPlace = viewModeTestPlace()
        val otherPlace = viewModeTestPlace()
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()

        composeRule.setContent {
            DiaryTheme {
                PlaceList(
                    onEvent = eventList::add,
                    placeListUiStateProvider = {
                        PlaceHomePlaceListUiState(
                            isLoaded = true,
                            placeList = listOf(selectedPlace, otherPlace),
                        )
                    },
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(selectedPlace.detail.title).performClick()
        composeRule.waitForIdle()

        eventList.withoutMoveMap() shouldBe listOf(PlaceHomeScaffoldEvent.ClickPlace(id = selectedPlace.id))
    }
}
