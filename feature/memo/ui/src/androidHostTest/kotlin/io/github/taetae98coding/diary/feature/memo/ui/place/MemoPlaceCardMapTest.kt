package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.feature.memo.ui.ProvideTestDiaryMap
import io.github.taetae98coding.diary.feature.memo.ui.TEST_DIARY_MAP_TAG
import io.github.taetae98coding.diary.feature.memo.ui.TestDiaryMap
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoPlaceCardMapTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val map = TestDiaryMap()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-a7e0269c-f3f0-49d9-b62d-af38505b8a0c 노출하는 장소 확인이 끝나면 지도를 표시한다`() {
        var uiState by mutableStateOf(
            cardUiState(
                placeUiState = MemoPlaceInputUiState(isSelectedPlaceLoaded = false),
                mapUiState = locationFetchedMapUiState(),
            ),
        )
        setCard(uiStateProvider = { uiState })
        composeRule.onNodeWithTag(TEST_DIARY_MAP_TAG).assertDoesNotExist()

        uiState = uiState.copy(placeUiState = MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(testPlace(title = HOME_PLACE_TITLE))))
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TEST_DIARY_MAP_TAG).assertExists()
    }

    @Test
    fun `TC-a7e0269c-f3f0-49d9-b62d-af38505b8a0c 노출하는 장소가 없으면 현재 위치 확인이 끝난 뒤 지도를 표시한다`() {
        var uiState by mutableStateOf(
            cardUiState(
                placeUiState = MemoPlaceInputUiState(isSelectedPlaceLoaded = true),
                mapUiState = MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = false, currentCoordinate = null),
            ),
        )
        setCard(uiStateProvider = { uiState })
        composeRule.onNodeWithTag(TEST_DIARY_MAP_TAG).assertDoesNotExist()

        uiState = uiState.copy(mapUiState = locationFetchedMapUiState())
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TEST_DIARY_MAP_TAG).assertExists()
    }

    @Test
    fun `TC-af968649-99d1-496c-86c9-9f650901d204 화면이 재생성되어도 지도가 알려온 위치와 확대 수준, 바꾼 제공자를 유지한다`() {
        val reportedCoordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val uiState =
            cardUiState(
                placeUiState = MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(testPlace(title = HOME_PLACE_TITLE))),
                mapUiState = locationFetchedMapUiState(),
            )
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { MapCard(uiStateProvider = { uiState }) }
        composeRule.onNodeWithText(GOOGLE_PROVIDER_NAME).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.runOnIdle { map.scopes.single().reportCamera(coordinate = reportedCoordinate, zoom = TEST_ZOOM) }

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            val restoredMap = map.scopes.single()

            restoredMap.state.provider shouldBe DiaryMapProvider.GOOGLE
            restoredMap.state.coordinate shouldBe reportedCoordinate
            restoredMap.zoom shouldBe TEST_ZOOM
        }
    }

    private fun setCard(uiStateProvider: () -> MemoPlaceCardUiState) {
        composeRule.setContent { MapCard(uiStateProvider = uiStateProvider) }
    }

    @Composable
    private fun MapCard(uiStateProvider: () -> MemoPlaceCardUiState) {
        ProvideTestDiaryMap(map = map) {
            DiaryTheme {
                val uiState = uiStateProvider()

                MemoPlaceCard(
                    onPlaceClick = {},
                    onAddClick = {},
                    mapState = rememberMemoPlaceMapState(uiState = uiState),
                    uiStateProvider = { uiState },
                )
            }
        }
    }

    private fun cardUiState(
        placeUiState: MemoPlaceInputUiState,
        mapUiState: MemoPlaceMapUiState,
    ): MemoPlaceCardUiState = MemoPlaceCardUiState(mapUiState = mapUiState, placeUiState = placeUiState)

    private fun locationFetchedMapUiState(): MemoPlaceMapUiState.Content = MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = true, currentCoordinate = fixtureMonkey.giveMeOne<Coordinate>())

    private companion object {
        const val TEST_ZOOM = 12.0

        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
