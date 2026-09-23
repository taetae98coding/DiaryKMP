package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.key
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceDetailFormState
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailScaffoldStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-DOMAIN-013 대상 장소의 저장된 좌표를 초기 지도 위치와 지점으로 지정한다`() {
        val coordinateList =
            listOf(
                Coordinate(latitude = 37.5665, longitude = 126.978),
                Coordinate(latitude = -33.4489, longitude = -70.6693),
            )
        val stateList = mutableListOf<PlaceFormState>()

        composeRule.setContent {
            coordinateList.forEach { coordinate ->
                key(coordinate) {
                    stateList +=
                        rememberPlaceDetailFormState(
                            initialDetail = placeDetail(coordinate = coordinate),
                            defaultProvider = MapProvider.NAVER,
                        )
                }
            }
        }

        composeRule.runOnIdle {
            coordinateList.forEachIndexed { index, coordinate ->
                val expected = DiaryMapCoordinate(latitude = coordinate.latitude, longitude = coordinate.longitude)

                stateList[index].mapState.coordinate shouldBe expected
                stateList[index].spot shouldBe expected
            }
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-DOMAIN-012 저장된 기본 지도를 초기 지도 제공자로 지정한다`() {
        val providerList =
            listOf(
                MapProvider.NAVER to DiaryMapProvider.NAVER,
                MapProvider.GOOGLE to DiaryMapProvider.GOOGLE,
            )
        val stateList = mutableListOf<PlaceFormState>()

        composeRule.setContent {
            providerList.forEach { (provider, _) ->
                key(provider) {
                    stateList +=
                        rememberPlaceDetailFormState(
                            initialDetail = placeDetail(),
                            defaultProvider = provider,
                        )
                }
            }
        }

        composeRule.runOnIdle {
            providerList.forEachIndexed { index, (_, expected) ->
                stateList[index].mapState.provider shouldBe expected
            }
        }
    }

    @Test
    fun `성립하지 않는 좌표로 시작하면 초기 지도 위치와 지점을 지정하지 않는다`() {
        lateinit var state: PlaceFormState

        composeRule.setContent {
            state =
                rememberPlaceDetailFormState(
                    initialDetail = placeDetail(coordinate = Coordinate(latitude = Double.NaN, longitude = Double.NaN)),
                    defaultProvider = MapProvider.NAVER,
                )
        }

        composeRule.runOnIdle {
            state.mapState.coordinate.shouldBeNull()
            state.spot.shouldBeNull()
            state.latitudeState.text.toString() shouldBe ""
            state.longitudeState.text.toString() shouldBe ""
        }
    }
}
