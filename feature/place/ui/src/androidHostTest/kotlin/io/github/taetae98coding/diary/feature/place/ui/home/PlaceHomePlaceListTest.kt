package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.place.PLACE_CARD_TEST_TAG
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

    @Test
    fun `TC-PLACE-HOME-FEATURE-055 지도 모드의 장소 목록에서 카드를 삭제 방향으로 밀면 그 장소의 삭제만 요청한다`() {
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
}
