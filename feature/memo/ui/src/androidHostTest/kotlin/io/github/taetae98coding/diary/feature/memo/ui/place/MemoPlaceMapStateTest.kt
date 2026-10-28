package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 카드의 지도는 지도 SDK 표시 요소가 필요해 호스트 테스트에서 구성할 수 없으므로, 카드가 지도에 넘기는 상태를 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoPlaceMapStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-62d68aa6-8d3c-4213-8b3a-05d73b19b3a0 카드가 장소 여러 개를 노출하면 첫 번째 장소의 좌표를 지도의 초기 위치로 지정한다`() {
        val firstPlace = testPlace(title = FIRST_PLACE_TITLE)
        val secondPlace = testPlace(title = SECOND_PLACE_TITLE)
        var mapState: DiaryMapState? = null

        composeRule.setContent {
            mapState = rememberMemoPlaceMapState(uiState = cardUiState(selectedPlaceList = listOf(firstPlace, secondPlace)))
        }

        composeRule.runOnIdle {
            mapState.shouldNotBeNull().coordinate shouldBe firstPlace.detail.coordinate?.toDiaryMapCoordinate()
        }
    }

    @Test
    fun `TC-9abc5d57-6f0f-4359-9e90-c0c44ca76380 장소를 선택하거나 해제해도 지도가 보고 있는 위치를 바꾸지 않는다`() {
        val firstPlace = testPlace(title = FIRST_PLACE_TITLE)
        val secondPlace = testPlace(title = SECOND_PLACE_TITLE)
        var uiState by mutableStateOf(cardUiState(selectedPlaceList = listOf(firstPlace)))
        val states = mutableListOf<DiaryMapState?>()

        composeRule.setContent {
            states += rememberMemoPlaceMapState(uiState = uiState)
        }
        val initialState = composeRule.runOnIdle { states.last().shouldNotBeNull() }

        // 다른 장소를 선택한 뒤 처음 장소를 해제한다.
        composeRule.runOnIdle { uiState = cardUiState(selectedPlaceList = listOf(firstPlace, secondPlace), mapUiState = uiState.mapUiState) }
        composeRule.runOnIdle { uiState = cardUiState(selectedPlaceList = listOf(secondPlace), mapUiState = uiState.mapUiState) }

        composeRule.runOnIdle {
            // 바뀐 선택으로 다시 그려졌는지부터 확인한다.
            (states.size > 1) shouldBe true
            states.last() shouldBeSameInstanceAs initialState
            initialState.coordinate shouldBe firstPlace.detail.coordinate.toDiaryMapCoordinate()
        }
    }

    @Test
    fun `TC-af968649-99d1-496c-86c9-9f650901d204 화면이 재생성되어도 사용자가 옮긴 지도 위치와 바꾼 제공자를 유지한다`() {
        val movedCoordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val restorationTester = StateRestorationTester(composeRule)
        var mapState: DiaryMapState? = null

        restorationTester.setContent {
            mapState = rememberMemoPlaceMapState(uiState = cardUiState(selectedPlaceList = listOf(testPlace(title = FIRST_PLACE_TITLE))))
        }
        composeRule.runOnIdle {
            mapState.shouldNotBeNull().select(DiaryMapProvider.GOOGLE)
            mapState.shouldNotBeNull().moveTo(movedCoordinate)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            mapState.shouldNotBeNull().provider shouldBe DiaryMapProvider.GOOGLE
            mapState.shouldNotBeNull().coordinate shouldBe movedCoordinate
        }
    }

    @Test
    fun `TC-b46cd42b-ec37-4db5-b9cb-df9d7986a676 노출하는 장소가 있으면 현재 위치 확인을 기다리지 않고 첫 번째 장소에서 지도를 시작한다`() {
        val firstPlace = testPlace(title = FIRST_PLACE_TITLE)
        var mapState: DiaryMapState? = null

        composeRule.setContent {
            mapState =
                rememberMemoPlaceMapState(
                    uiState = cardUiState(selectedPlaceList = listOf(firstPlace), mapUiState = locationNotFetchedMapUiState()),
                )
        }

        composeRule.runOnIdle {
            mapState.shouldNotBeNull().coordinate shouldBe firstPlace.detail.coordinate.toDiaryMapCoordinate()
        }
    }

    @Test
    fun `TC-a7e0269c-f3f0-49d9-b62d-af38505b8a0c 노출하는 장소가 없으면 현재 위치 확인이 끝나기 전에는 지도를 표시하지 않는다`() {
        var mapState: DiaryMapState? = null

        composeRule.setContent {
            mapState = rememberMemoPlaceMapState(uiState = cardUiState(selectedPlaceList = emptyList(), mapUiState = locationNotFetchedMapUiState()))
        }

        composeRule.runOnIdle { mapState shouldBe null }
    }

    private fun cardUiState(
        selectedPlaceList: List<Place>,
        mapUiState: MemoPlaceMapUiState = MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = true, currentCoordinate = fixtureMonkey.giveMeOne<Coordinate>()),
    ): MemoPlaceCardUiState =
        MemoPlaceCardUiState(
            mapUiState = mapUiState,
            placeUiState = MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = selectedPlaceList),
        )

    private fun locationNotFetchedMapUiState(): MemoPlaceMapUiState = MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = false, currentCoordinate = null)

    private companion object {
        const val FIRST_PLACE_TITLE = "집"
        const val SECOND_PLACE_TITLE = "회사"

        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
