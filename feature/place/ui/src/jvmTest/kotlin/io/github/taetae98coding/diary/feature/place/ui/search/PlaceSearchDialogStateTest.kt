package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.focus.FocusRequester
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.map.DiaryMapBounds
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class PlaceSearchDialogStateTest : FunSpec() {
    init {
        test("지도가 알려 온 영역을 검색 기준 영역으로 삼는다") {
            val userBounds = boundsList().first()
            val state = placeSearchDialogState()

            state.updateSearchBounds(bounds = userBounds, center = userBounds.center)

            state.searchBounds shouldBe userBounds
        }

        test("목록의 장소로 지도를 옮겨 그 장소가 가운데인 영역은 검색 기준 영역으로 삼지 않는다") {
            val (userBounds, shownBounds) = boundsList()
            val state = placeSearchDialogState()

            state.updateSearchBounds(bounds = userBounds, center = userBounds.center)
            state.showOnMap(shownBounds.center)
            state.updateSearchBounds(bounds = shownBounds, center = shownBounds.center)

            state.searchBounds shouldBe userBounds
        }

        test("지도가 알려 온 가운데가 옮긴 좌표와 조금 달라도 같은 곳으로 본다") {
            val (userBounds, shownBounds) = boundsList()
            val state = placeSearchDialogState()
            val target = shownBounds.center
            val reported = target.copy(latitude = target.latitude + (shownBounds.north - shownBounds.south) * REPORTED_CENTER_DRIFT_RATIO)

            state.updateSearchBounds(bounds = userBounds, center = userBounds.center)
            state.showOnMap(target)
            state.updateSearchBounds(bounds = shownBounds, center = reported)

            state.searchBounds shouldBe userBounds
        }

        test("목록의 장소로 옮긴 뒤 사용자가 지도를 옮기면 그 영역을 검색 기준 영역으로 삼는다") {
            val (_, shownBounds, movedBounds) = boundsList()
            val state = placeSearchDialogState()

            state.showOnMap(shownBounds.center)
            state.updateSearchBounds(bounds = shownBounds, center = shownBounds.center)
            state.updateSearchBounds(bounds = movedBounds, center = movedBounds.center)

            state.searchBounds shouldBe movedBounds
        }

        test("목록의 장소로 옮기라고 했지만 지도가 다른 곳을 알려 오면 그 영역을 검색 기준 영역으로 삼는다") {
            val (_, shownBounds, movedBounds) = boundsList()
            val state = placeSearchDialogState()

            state.showOnMap(shownBounds.center)
            state.updateSearchBounds(bounds = movedBounds, center = movedBounds.center)

            state.searchBounds shouldBe movedBounds
        }

        test("목록의 장소로 지도를 옮기면 지도가 그 좌표를 가운데에 둔다") {
            val shownBounds = boundsList().first()
            val state = placeSearchDialogState()

            state.showOnMap(shownBounds.center)

            state.mapState.coordinate shouldBe shownBounds.center
        }
    }

    private companion object {
        private const val REPORTED_CENTER_DRIFT_RATIO = 0.001
        private const val BASE_SOUTH_RANGE = 60
        private const val BASE_SOUTH_MIN = -60.0
        private const val BASE_WEST_RANGE = 300
        private const val BASE_WEST_MIN = -150.0
        private const val SPAN_STEPS = 100
        private const val SPAN_UNIT = 0.01
        private const val BOUNDS_GAP = 10.0
        private const val BOUNDS_COUNT = 3

        private val DiaryMapBounds.center: DiaryMapCoordinate
            get() = DiaryMapCoordinate(latitude = (south + north) / 2, longitude = (west + east) / 2)

        // 서로 겹치지 않는 영역이어야 가운데 판정이 성립하므로, 임의의 기준 위치에서 위도만 간격을 두고 떨어뜨린다.
        private fun boundsList(): List<DiaryMapBounds> {
            val south = BASE_SOUTH_MIN + fixtureMonkey.giveMeOne<Int>().mod(BASE_SOUTH_RANGE)
            val west = BASE_WEST_MIN + fixtureMonkey.giveMeOne<Int>().mod(BASE_WEST_RANGE)
            val span = (fixtureMonkey.giveMeOne<Int>().mod(SPAN_STEPS) + 1) * SPAN_UNIT

            return List(BOUNDS_COUNT) { index ->
                val shiftedSouth = south + index * BOUNDS_GAP

                DiaryMapBounds(south = shiftedSouth, north = shiftedSouth + span, west = west, east = west + span)
            }
        }

        private fun placeSearchDialogState(): PlaceSearchDialogState =
            PlaceSearchDialogState(
                queryState = TextFieldState(),
                focusRequester = FocusRequester(),
                mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER),
            )
    }
}
