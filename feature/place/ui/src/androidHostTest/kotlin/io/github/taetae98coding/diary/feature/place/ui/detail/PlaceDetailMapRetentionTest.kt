package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.testing.place.coordinateInFormPrecision
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceDetailFormState
import io.github.taetae98coding.diary.feature.place.ui.toCoordinateText
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

/**
 * 지도 제공자의 실제 표시 요소는 만들 수 없으므로, 화면이 지도 기능에 지정한 지점과 지도 위치, 제공자로 유지 결과를 확인한다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailMapRetentionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-021 화면이 재생성되어도 보고 있던 지도와 지점 표시를 유지한다`() {
        val savedDetail = placeDetail(coordinate = fixtureMonkey.coordinateInFormPrecision())
        val movedCamera = fixtureMonkey.coordinateInFormPrecision().toDiaryMapCoordinate()
        val changedSpot = fixtureMonkey.coordinateInFormPrecision().toDiaryMapCoordinate()
        val changedLatitude = changedSpot.latitude.toCoordinateText()
        val changedLongitude = changedSpot.longitude.toCoordinateText()
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: PlaceFormState

        restorationTester.setContent {
            DiaryTheme {
                state =
                    rememberPlaceDetailFormState(
                        initialDetail = savedDetail,
                        defaultProvider = MapProvider.NAVER,
                    )
            }
        }
        composeRule.runOnIdle {
            state.mapState.moveTo(movedCamera)
            state.mapState.select(DiaryMapProvider.GOOGLE)
            state.latitudeState.setTextAndPlaceCursorAtEnd(changedLatitude)
            state.longitudeState.setTextAndPlaceCursorAtEnd(changedLongitude)
            state.mapState.selectSpot(state.spot)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            state.latitudeState.text.toString() shouldBe changedLatitude
            state.longitudeState.text.toString() shouldBe changedLongitude
            state.mapState.spot shouldBe changedSpot
            state.mapState.coordinate shouldBe movedCamera
            state.mapState.provider shouldBe DiaryMapProvider.GOOGLE
        }
    }
}
