package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
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
class PlaceDetailSearchApplyTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-024 검색에서 고른 장소의 좌표가 위도와 경도에 채워진다`() {
        val initialDetail = savedDetail()
        val state = composeRule.setPlaceDetailScaffoldState(initialDetail = initialDetail)
        val place = searchedPlace()

        composeRule.runOnIdle { state.applySearchedPlace(place) }

        composeRule.runOnIdle { state.detail.coordinate shouldBe place.coordinate }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-025 저장된 제목이 채워져 있으면 고른 장소의 이름이 제목을 덮어쓰지 않는다`() {
        val initialDetail = savedDetail()
        val state = composeRule.setPlaceDetailScaffoldState(initialDetail = initialDetail)

        composeRule.runOnIdle { state.applySearchedPlace(searchedPlace()) }

        composeRule.runOnIdle { state.detail.title shouldBe initialDetail.title }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-026 제목을 비운 뒤 검색하면 고른 장소의 이름이 제목에 채워진다`() {
        val state = composeRule.setPlaceDetailScaffoldState(initialDetail = savedDetail())
        val place = searchedPlace()

        composeRule.runOnIdle { state.titleState.clearText() }
        composeRule.runOnIdle { state.applySearchedPlace(place) }

        composeRule.runOnIdle { state.detail.title shouldBe place.name }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-027 검색에서 고른 장소의 주소가 주소 입력을 덮어쓴다`() {
        val state = composeRule.setPlaceDetailScaffoldState(initialDetail = savedDetail())

        listOf(
            SEARCHED_ADDRESS to SEARCHED_ADDRESS,
            "" to "",
        ).forEach { (searchedAddress, expected) ->
            val place = searchedPlace().copy(address = searchedAddress)

            composeRule.runOnIdle { state.applySearchedPlace(place) }

            composeRule.runOnIdle { state.detail.address shouldBe expected }
        }
    }

    private companion object {
        private const val SEARCHED_ADDRESS = "Searched Address"

        private fun savedDetail(): PlaceDetail =
            PlaceDetail(
                title = "Saved Title",
                description = "Saved Description",
                color = 0xFF0000FFL,
                coordinate = Coordinate(latitude = 35.0, longitude = 129.0),
                address = "Saved Address",
            )

        private fun searchedPlace(): SearchedPlace =
            SearchedPlace(
                id = Uuid.random(),
                name = "Searched Place",
                address = SEARCHED_ADDRESS,
                coordinate = Coordinate(latitude = 37.5665, longitude = 126.9780),
            )
    }
}
