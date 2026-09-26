package io.github.taetae98coding.diary.feature.place.ui.home.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.feature.place.ui.home.PlaceHomeUiState
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceHomeMapStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-HOME-FEATURE-010 기본 지도를 고르지 않았으면 공용 지도 기능의 기본 제공자로 시작한다`() {
        lateinit var placeHomeState: DiaryMapState
        lateinit var defaultState: DiaryMapState

        composeRule.setContent {
            // 기본 지도를 고르지 않으면 설정은 네이버 지도를 제공한다.
            placeHomeState = rememberPlaceHomeMapState(uiState = PlaceHomeUiState.Loaded(defaultProvider = MapProvider.NAVER))
            defaultState = rememberDiaryMapState()
        }

        composeRule.runOnIdle {
            placeHomeState.provider shouldBe defaultState.provider
        }
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-012 저장된 기본 지도가 초기 제공자로 지정된다`() {
        val providerList =
            listOf(
                MapProvider.NAVER to DiaryMapProvider.NAVER,
                MapProvider.GOOGLE to DiaryMapProvider.GOOGLE,
            )
        val stateList = mutableListOf<DiaryMapState>()

        composeRule.setContent {
            providerList.forEach { (provider, _) ->
                key(provider) {
                    stateList += rememberPlaceHomeMapState(uiState = PlaceHomeUiState.Loaded(defaultProvider = provider))
                }
            }
        }

        composeRule.runOnIdle {
            providerList.forEachIndexed { index, (_, expected) ->
                stateList[index].provider shouldBe expected
            }
        }
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-013 화면이 표시된 뒤 기본 지도가 바뀌면 바뀐 지도로 새로 시작한다`() {
        var uiState: PlaceHomeUiState by mutableStateOf(PlaceHomeUiState.Loaded(defaultProvider = MapProvider.NAVER))
        lateinit var state: DiaryMapState

        composeRule.setContent {
            state = rememberPlaceHomeMapState(uiState = uiState)
        }
        lateinit var naverState: DiaryMapState
        composeRule.runOnIdle {
            state.provider shouldBe DiaryMapProvider.NAVER
            naverState = state
            uiState = PlaceHomeUiState.Loaded(defaultProvider = MapProvider.GOOGLE)
        }

        composeRule.runOnIdle {
            state.provider shouldBe DiaryMapProvider.GOOGLE
            state shouldNotBe naverState
        }
    }
}
