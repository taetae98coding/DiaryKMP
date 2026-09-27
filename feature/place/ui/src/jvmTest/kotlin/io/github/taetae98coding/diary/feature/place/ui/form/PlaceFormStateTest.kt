package io.github.taetae98coding.diary.feature.place.ui.form

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.feature.place.ui.toCoordinateText
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class PlaceFormStateTest :
    FunSpec({
        test("TC-PLACE-ADD-FEATURE-016 지도에서 위치를 고르면 위도와 경도가 채워진다") {
            val state = placeAddFormState(initialMapCoordinate = fixtureMonkey.mapCoordinateInFormPrecision())

            List(2) { fixtureMonkey.mapCoordinateInFormPrecision() }.forEach { selectedCoordinate ->
                state.clearCoordinate()
                state.selectSpotOnMap(selectedCoordinate)

                state.latitudeState.text.toString() shouldBe selectedCoordinate.latitude.toCoordinateText()
                state.longitudeState.text.toString() shouldBe selectedCoordinate.longitude.toCoordinateText()
            }
        }

        test("TC-PLACE-ADD-FEATURE-017 지도에서 다른 위치를 고르면 좌표가 새 위치로 바뀌고 지점은 하나만 남는다") {
            val firstCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
            val secondCoordinate = generateSequence { fixtureMonkey.mapCoordinateInFormPrecision() }.first { coordinate -> coordinate != firstCoordinate }
            val state = placeAddFormState(initialMapCoordinate = fixtureMonkey.mapCoordinateInFormPrecision())

            state.selectSpotOnMap(firstCoordinate)
            state.selectSpotOnMap(secondCoordinate)

            state.latitudeState.text.toString() shouldBe secondCoordinate.latitude.toCoordinateText()
            state.longitudeState.text.toString() shouldBe secondCoordinate.longitude.toCoordinateText()
            state.mapState.spot shouldBe secondCoordinate
        }

        test("TC-PLACE-ADD-FEATURE-018 지도에서 고른 좌표를 이어서 직접 고칠 수 있다") {
            val selectedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
            val changedLatitude =
                generateSequence { fixtureMonkey.mapCoordinateInFormPrecision().latitude.toCoordinateText() }
                    .first { latitude -> latitude != selectedCoordinate.latitude.toCoordinateText() }
            val state = placeAddFormState(initialMapCoordinate = fixtureMonkey.mapCoordinateInFormPrecision())

            state.selectSpotOnMap(selectedCoordinate)
            state.latitudeState.setTextAndPlaceCursorAtEnd(changedLatitude)

            state.latitudeState.text.toString() shouldBe changedLatitude
            state.longitudeState.text.toString() shouldBe selectedCoordinate.longitude.toCoordinateText()
        }

        test("TC-PLACE-ADD-DOMAIN-009 지도에서 고른 좌표는 항상 성립하는 좌표다") {
            val state = placeAddFormState(initialMapCoordinate = fixtureMonkey.mapCoordinateInFormPrecision())

            listOf(
                DiaryMapCoordinate(latitude = 90.0, longitude = 180.0),
                DiaryMapCoordinate(latitude = -90.0, longitude = -180.0),
                DiaryMapCoordinate(latitude = 0.0, longitude = 0.0),
                fixtureMonkey.mapCoordinateInFormPrecision(),
            ).forEach { selectedCoordinate ->
                state.selectSpotOnMap(selectedCoordinate)

                withClue("고른 좌표=$selectedCoordinate") {
                    val coordinate = state.detail.coordinate

                    coordinate shouldBe Coordinate(latitude = selectedCoordinate.latitude, longitude = selectedCoordinate.longitude)
                    coordinate.isRepresentable shouldBe true
                }
            }
        }
    })
