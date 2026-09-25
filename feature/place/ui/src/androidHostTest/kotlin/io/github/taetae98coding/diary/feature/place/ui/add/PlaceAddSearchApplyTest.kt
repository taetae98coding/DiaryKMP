package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceAddSearchApplyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-ADD-FEATURE-026 검색에서 고른 장소의 좌표가 위도와 경도에 채워진다`() {
        val state = composeRule.setPlaceAddScaffoldState()
        val place = searchedPlace()

        composeRule.runOnIdle { state.applySearchedPlace(place) }

        composeRule.runOnIdle {
            state.detail.coordinate shouldBe place.coordinate
        }
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-027 제목이 비어 있으면 고른 장소의 이름이 제목에 채워진다`() {
        val state = composeRule.setPlaceAddScaffoldState()
        val place = searchedPlace()

        composeRule.runOnIdle { state.applySearchedPlace(place) }

        composeRule.runOnIdle { state.detail.title shouldBe place.name }
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-027 제목이 공백뿐이면 고른 장소의 이름이 제목에 채워진다`() {
        val state = composeRule.setPlaceAddScaffoldState()
        val place = searchedPlace()

        composeRule.runOnIdle { state.titleState.setText(BLANK_TITLE) }
        composeRule.runOnIdle { state.applySearchedPlace(place) }

        composeRule.runOnIdle { state.detail.title shouldBe place.name }
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-027 제목이 입력되어 있으면 고른 장소의 이름이 제목을 덮어쓰지 않는다`() {
        val state = composeRule.setPlaceAddScaffoldState()
        val place = searchedPlace()

        composeRule.runOnIdle { state.titleState.setText(TYPED_TITLE) }
        composeRule.runOnIdle { state.applySearchedPlace(place) }

        composeRule.runOnIdle { state.detail.title shouldBe TYPED_TITLE }
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-028 검색에서 고른 장소로 추가할 내용이 만들어진다`() {
        val state = composeRule.setPlaceAddScaffoldState()
        val place = searchedPlace()

        composeRule.runOnIdle { state.applySearchedPlace(place) }

        composeRule.runOnIdle {
            state.detail.title shouldBe place.name
            state.detail.coordinate shouldBe place.coordinate
            state.detail.address shouldBe place.address
        }
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-030 검색에서 고른 장소의 주소가 주소 입력에 채워진다`() {
        val state = composeRule.setPlaceAddScaffoldState()

        listOf(
            Triple("", SEARCHED_ADDRESS, SEARCHED_ADDRESS),
            Triple(TYPED_ADDRESS, SEARCHED_ADDRESS, SEARCHED_ADDRESS),
            Triple(TYPED_ADDRESS, "", ""),
        ).forEach { (typedAddress, searchedAddress, expected) ->
            val place = searchedPlace().copy(address = searchedAddress)

            composeRule.runOnIdle { state.addressState.setTextAndPlaceCursorAtEnd(typedAddress) }
            composeRule.runOnIdle { state.applySearchedPlace(place) }

            composeRule.runOnIdle { state.detail.address shouldBe expected }
        }
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-029 장소를 고르지 않고 검색을 닫으면 입력 내용이 그대로 남는다`() {
        val state = composeRule.setPlaceAddScaffoldState()

        composeRule.runOnIdle {
            state.titleState.setText(TYPED_TITLE)
            state.addressState.setTextAndPlaceCursorAtEnd(TYPED_ADDRESS)
            state.latitudeState.setTextAndPlaceCursorAtEnd(TYPED_LATITUDE)
            state.longitudeState.setTextAndPlaceCursorAtEnd(TYPED_LONGITUDE)
            state.searchDialogState.show()
        }
        composeRule.runOnIdle { state.searchDialogState.hide() }

        composeRule.runOnIdle {
            state.searchDialogState.isVisible shouldBe false
            state.detail.title shouldBe TYPED_TITLE
            state.detail.address shouldBe TYPED_ADDRESS
            state.detail.coordinate shouldBe Coordinate(latitude = TYPED_LATITUDE.toDouble(), longitude = TYPED_LONGITUDE.toDouble())
        }
    }

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-010 장소를 고르면 검색이 닫힌다`() {
        val state = composeRule.setPlaceAddScaffoldState()

        composeRule.runOnIdle { state.searchDialogState.show() }
        composeRule.runOnIdle { state.applySearchedPlace(searchedPlace()) }

        composeRule.runOnIdle { state.searchDialogState.isVisible shouldBe false }
    }

    private companion object {
        private const val BLANK_TITLE = "   "
        private const val SEARCHED_ADDRESS = "Searched Address"

        private fun searchedPlace(): SearchedPlace =
            SearchedPlace(
                id = Uuid.random(),
                name = "Searched Place",
                address = SEARCHED_ADDRESS,
                coordinate = Coordinate(latitude = 37.5665, longitude = 126.9780),
            )
    }
}
