package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.feature.place.ui.add.mapCoordinateInFormPrecision
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.ReflectCoordinateEffect
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceDetailFormState
import io.github.taetae98coding.diary.feature.place.ui.toCoordinateText
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailMapReflectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-DOMAIN-015 기본 지도가 바뀌면 그 시점의 저장된 좌표에서 새로 시작하고 수정 중인 내용을 유지한다`() {
        composeRule.mainClock.autoAdvance = false
        val firstSavedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        val latestSavedCoordinate = generateSequence { fixtureMonkey.mapCoordinateInFormPrecision() }.first { coordinate -> coordinate != firstSavedCoordinate }
        val typedCoordinate =
            generateSequence { fixtureMonkey.mapCoordinateInFormPrecision() }
                .first { coordinate -> coordinate != firstSavedCoordinate && coordinate != latestSavedCoordinate }
        var savedDetail by mutableStateOf(placeDetail(coordinate = firstSavedCoordinate.toCoordinate()))
        var defaultProvider by mutableStateOf(MapProvider.NAVER)
        lateinit var state: PlaceFormState

        composeRule.setContent {
            DiaryTheme {
                state = rememberPlaceDetailFormState(initialDetail = savedDetail, defaultProvider = defaultProvider)
                ReflectCoordinateEffect(state = state)
            }
        }
        composeRule.write {
            state.titleState.setText(CHANGED_TITLE)
            state.descriptionState.setText(CHANGED_DESCRIPTION)
            state.addressState.setTextAndPlaceCursorAtEnd(CHANGED_ADDRESS)
            state.typeCoordinate(typedCoordinate)
        }
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS * 2)
        val colorBefore = composeRule.runOnIdle { state.detail.color }

        composeRule.write {
            savedDetail = savedDetail.copy(coordinate = latestSavedCoordinate.toCoordinate())
            defaultProvider = MapProvider.GOOGLE
        }
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS / 2)

        composeRule.runOnIdle {
            state.mapState.provider shouldBe DiaryMapProvider.GOOGLE
            state.mapState.coordinate shouldBe latestSavedCoordinate
            state.mapState.spot.shouldBeNull()
            state.detail.title shouldBe CHANGED_TITLE
            state.detail.description shouldBe CHANGED_DESCRIPTION
            state.detail.address shouldBe CHANGED_ADDRESS
            state.detail.color shouldBe colorBefore
            state.latitudeState.text.toString() shouldBe typedCoordinate.latitude.toCoordinateText()
            state.longitudeState.text.toString() shouldBe typedCoordinate.longitude.toCoordinateText()
        }

        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS)

        composeRule.runOnIdle {
            state.mapState.spot shouldBe typedCoordinate
            state.mapState.coordinate shouldBe typedCoordinate
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-058 좌표가 성립하지 않아도 지도가 보고 있는 위치를 옮기지 않는다`() {
        composeRule.mainClock.autoAdvance = false
        val savedCoordinate = fixtureMonkey.mapCoordinateInFormPrecision()
        lateinit var state: PlaceFormState

        composeRule.setContent {
            DiaryTheme {
                state =
                    rememberPlaceDetailFormState(
                        initialDetail = placeDetail(coordinate = savedCoordinate.toCoordinate()),
                        defaultProvider = MapProvider.NAVER,
                    )
                ReflectCoordinateEffect(state = state)
            }
        }
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS * 2)
        composeRule.runOnIdle { state.mapState.coordinate shouldBe savedCoordinate }

        composeRule.write { state.latitudeState.setTextAndPlaceCursorAtEnd(INVALID_LATITUDE) }
        composeRule.mainClock.advanceTimeBy(REFLECT_DELAY_MILLIS * 2)

        composeRule.runOnIdle {
            state.detail.coordinate.isRepresentable shouldBe false
            state.mapState.coordinate shouldBe savedCoordinate
        }
    }

    // 화면에 입력 칸을 두지 않으므로, 프레임이 상태 변경을 알리는 것과 같게 변경 직후 알림을 보낸다.
    private fun ComposeContentTestRule.write(action: () -> Unit) {
        runOnIdle {
            action()
            Snapshot.sendApplyNotifications()
        }
    }

    private fun PlaceFormState.typeCoordinate(coordinate: DiaryMapCoordinate) {
        latitudeState.setTextAndPlaceCursorAtEnd(coordinate.latitude.toCoordinateText())
        longitudeState.setTextAndPlaceCursorAtEnd(coordinate.longitude.toCoordinateText())
    }

    private fun DiaryMapCoordinate.toCoordinate(): Coordinate = Coordinate(latitude = latitude, longitude = longitude)

    private companion object {
        private const val REFLECT_DELAY_MILLIS = 200L

        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
