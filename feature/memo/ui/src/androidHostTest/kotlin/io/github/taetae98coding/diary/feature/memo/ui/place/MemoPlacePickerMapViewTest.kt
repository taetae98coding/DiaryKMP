package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 지도 보기의 지도는 지도 SDK 표시 요소가 필요해 호스트 테스트에서 구성할 수 없으므로, 지도 없이 구성되는 목록과 상태를 나누어 확인한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoPlacePickerMapViewTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-ef69ebeb-0b8f-4669-a639-a233e8fc7b53 카드의 지도가 표시되면 목록 보기로 열리고 지도 보기로 바꾸는 조작을 제공한다`() {
        setDialog(mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER))

        composeRule.placeDialogSearchField().assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_MAP_ACTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-ef69ebeb-0b8f-4669-a639-a233e8fc7b53 한국어 환경에서 지도 보기로 바꾸는 조작의 이름을 표시한다`() {
        setDialog(mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER))

        composeRule.onNodeWithContentDescription(KOREAN_SHOW_MAP_ACTION).assertExists()
    }

    @Test
    fun `TC-29017b00-830b-4219-a55b-b8ba0de808b5 카드의 지도가 표시되지 않으면 지도 보기로 바꾸는 조작을 제공하지 않는다`() {
        setDialog(mapState = null)

        composeRule.placeDialogSearchField().assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_MAP_ACTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_SHOW_LIST_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-12dc2410-d559-4a44-b160-e13bb9b35768 지도 보기의 목록에 보이는 영역의 장소가 선택 여부와 함께 나타난다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val officePlace = testPlace(title = OFFICE_PLACE_TITLE)
        setMapList(
            placeList = listOf(homePlace, officePlace),
            selectedPlaceList = listOf(homePlace),
        )

        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertIsOn()
        composeRule.onNodeWithText(OFFICE_PLACE_TITLE).assertIsOff()
    }

    @Test
    fun `TC-48e1e605-3ba0-47b8-abb4-3828a8cbf7a3 선택되지 않은 장소를 누르면 선택된 것으로 표시되고 상세 이동은 요청하지 않는다`() {
        assertMapListToggle(isInitiallySelected = false)
    }

    @Test
    fun `TC-48e1e605-3ba0-47b8-abb4-3828a8cbf7a3 선택된 장소를 누르면 선택되지 않은 것으로 표시되고 상세 이동은 요청하지 않는다`() {
        assertMapListToggle(isInitiallySelected = true)
    }

    @Test
    fun `TC-3b79ed3e-d33b-46d9-b09e-b3524a6bde56 보이는 영역 안에 장소가 없으면 안내를 표시한다`() {
        setMapList(placeList = emptyList())

        composeRule.onNodeWithText(DEFAULT_MAP_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_MAP_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-3b79ed3e-d33b-46d9-b09e-b3524a6bde56 한국어 환경에서 장소 없음 안내 문구를 표시한다`() {
        setMapList(placeList = emptyList())

        composeRule.onNodeWithText(KOREAN_MAP_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_MAP_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-98fb04b1-f186-4d06-a344-7b457e0b7708 보이는 영역을 확인하기 전에는 장소도 안내도 표시하지 않는다`() {
        setMapList(mapUiState = MemoPlacePickerMapUiState(isLoaded = false))

        composeRule.onNodeWithText(DEFAULT_MAP_EMPTY_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_MAP_EMPTY_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-58e5a5e8-9846-44a9-8deb-2f40012fc713 네이버 지도를 보던 카드에서 열면 지도 보기가 네이버 지도로 같은 위치에서 시작한다`() {
        assertPickerMapStartsFromCardMap(provider = DiaryMapProvider.NAVER)
    }

    @Test
    fun `TC-58e5a5e8-9846-44a9-8deb-2f40012fc713 Google 지도를 보던 카드에서 열면 지도 보기가 Google 지도로 같은 위치에서 시작한다`() {
        assertPickerMapStartsFromCardMap(provider = DiaryMapProvider.GOOGLE)
    }

    @Test
    fun `TC-58e5a5e8-9846-44a9-8deb-2f40012fc713 지도 보기에서 바꾼 제공자와 위치는 카드의 지도에 반영하지 않는다`() {
        val cardCoordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val pickerCoordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val cardMapState = DiaryMapState(initialProvider = DiaryMapProvider.GOOGLE, initialCoordinate = cardCoordinate)
        lateinit var pickerMapState: DiaryMapState

        composeRule.setContent {
            pickerMapState = rememberMemoPlacePickerMapState(cardMapState = cardMapState)
        }

        composeRule.runOnIdle {
            pickerMapState.select(DiaryMapProvider.NAVER)
            pickerMapState.moveTo(pickerCoordinate)
        }

        composeRule.runOnIdle {
            cardMapState.provider shouldBe DiaryMapProvider.GOOGLE
            cardMapState.coordinate shouldBe cardCoordinate
        }
    }

    @Test
    fun `TC-1ea4c491-ca05-4c15-b790-76ab76c61f82 기본 지도가 바뀌어 카드의 지도가 다시 시작하면 지도 보기의 지도도 새 기본 지도로 다시 시작한다`() {
        val firstCoordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val movedCoordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val restartedCoordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        var cardMapState by mutableStateOf(DiaryMapState(initialProvider = DiaryMapProvider.NAVER, initialCoordinate = firstCoordinate))
        lateinit var pickerMapState: DiaryMapState

        composeRule.setContent {
            pickerMapState = rememberMemoPlacePickerMapState(cardMapState = cardMapState)
        }
        composeRule.runOnIdle { pickerMapState.moveTo(movedCoordinate) }
        composeRule.runOnIdle {
            cardMapState = DiaryMapState(initialProvider = DiaryMapProvider.GOOGLE, initialCoordinate = restartedCoordinate)
        }

        composeRule.runOnIdle {
            pickerMapState.provider shouldBe DiaryMapProvider.GOOGLE
            pickerMapState.coordinate shouldBe restartedCoordinate
        }
    }

    @Test
    fun `TC-dcc67ec4-f921-4d3c-b8be-4de7ac04f7ee 목록 보기에서 장소 추가로 이동하면 카드의 지도 위치를 넘긴다`() {
        val coordinate = fixtureMonkey.giveMeOne<Coordinate>()
        val eventList = mutableListOf<MemoPlacePickerEvent>()
        setDialog(
            mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER),
            coordinate = coordinate,
            onEvent = eventList::add,
        )

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(MemoPlacePickerEvent.ClickAdd(coordinate = coordinate))
    }

    @Test
    fun `TC-994ab58b-cce4-4f44-811e-24276321eb0f 화면이 재생성되거나 복원되면 목록 보기로 돌아가고 지도 보기의 지도 상태는 유지된다`() {
        val coordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val cardMapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: MemoPlacePickerState
        lateinit var pickerMapState: DiaryMapState

        restorationTester.setContent {
            state = rememberMemoPlacePickerState()
            pickerMapState = rememberMemoPlacePickerMapState(cardMapState = cardMapState)
        }
        composeRule.runOnIdle {
            state.toggleViewMode()
            pickerMapState.select(DiaryMapProvider.GOOGLE)
            pickerMapState.moveTo(coordinate)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            state.viewMode shouldBe MemoPlacePickerViewMode.LIST
            pickerMapState.provider shouldBe DiaryMapProvider.GOOGLE
            pickerMapState.coordinate shouldBe coordinate
        }
    }

    @Test
    fun `TC-4f614b08-2b8d-4dae-afe6-25300f0383cf 목록을 닫았다가 다시 열면 목록 보기로 시작한다`() {
        var isVisible by mutableStateOf(true)
        var state: MemoPlacePickerState? = null

        composeRule.setContent {
            if (isVisible) {
                state = rememberMemoPlacePickerState()
            }
        }
        composeRule.runOnIdle { checkNotNull(state).toggleViewMode() }
        composeRule.runOnIdle { isVisible = false }
        composeRule.runOnIdle { isVisible = true }

        composeRule.runOnIdle {
            checkNotNull(state).viewMode shouldBe MemoPlacePickerViewMode.LIST
        }
    }

    private fun assertPickerMapStartsFromCardMap(provider: DiaryMapProvider) {
        val coordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val cardMapState = DiaryMapState(initialProvider = provider, initialCoordinate = coordinate)
        lateinit var pickerMapState: DiaryMapState

        composeRule.setContent {
            pickerMapState = rememberMemoPlacePickerMapState(cardMapState = cardMapState)
        }

        composeRule.runOnIdle {
            pickerMapState.provider shouldBe provider
            pickerMapState.coordinate shouldBe coordinate
        }
    }

    private fun assertMapListToggle(isInitiallySelected: Boolean) {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        var selectedPlaceList by mutableStateOf(if (isInitiallySelected) listOf(homePlace) else emptyList())
        val eventList = mutableListOf<MemoPlacePickerEvent>()

        composeRule.setContent {
            DiaryTheme {
                MemoPlacePickerMapList(
                    onEvent = { event ->
                        eventList.add(event)
                        when (event) {
                            is MemoPlacePickerEvent.Select -> selectedPlaceList = selectedPlaceList + homePlace
                            is MemoPlacePickerEvent.Unselect -> selectedPlaceList = selectedPlaceList - homePlace
                            else -> Unit
                        }
                    },
                    uiStateProvider = { MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = selectedPlaceList) },
                    mapUiStateProvider = { MemoPlacePickerMapUiState(isLoaded = true, placeList = listOf(homePlace)) },
                )
            }
        }

        composeRule.onNodeWithText(HOME_PLACE_TITLE).performClick()

        if (isInitiallySelected) {
            composeRule.onNodeWithText(HOME_PLACE_TITLE).assertIsOff()
            eventList shouldBe listOf(MemoPlacePickerEvent.Unselect(id = homePlace.id))
        } else {
            composeRule.onNodeWithText(HOME_PLACE_TITLE).assertIsOn()
            eventList shouldBe listOf(MemoPlacePickerEvent.Select(id = homePlace.id))
        }
    }

    private fun setDialog(
        state: MemoPlacePickerState = MemoPlacePickerState(),
        mapState: DiaryMapState? = null,
        query: String = "",
        placeList: List<Place> = emptyList(),
        coordinate: Coordinate? = null,
        onEvent: (MemoPlacePickerEvent) -> Unit = {},
    ) {
        val pagingData = placePagingData(placeList)

        composeRule.setContent {
            DiaryTheme {
                MemoPlacePickerDialog(
                    onDismissRequest = {},
                    onEvent = onEvent,
                    state = state,
                    searchFieldState = rememberDiaryPickerSearchFieldState(initialText = query),
                    mapState = mapState,
                    placePagingItems = remember(pagingData) { pagingData }.collectAsLazyPagingItems(),
                    uiStateProvider = { MemoPlaceInputUiState(isSelectedPlaceLoaded = true) },
                    coordinateProvider = { coordinate },
                )
            }
        }
    }

    private fun setMapList(
        placeList: List<Place> = emptyList(),
        selectedPlaceList: List<Place> = emptyList(),
        mapUiState: MemoPlacePickerMapUiState = MemoPlacePickerMapUiState(isLoaded = true, placeList = placeList),
        onEvent: (MemoPlacePickerEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                MemoPlacePickerMapList(
                    onEvent = onEvent,
                    uiStateProvider = { MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = selectedPlaceList) },
                    mapUiStateProvider = { mapUiState },
                )
            }
        }
    }

    private companion object {
        const val DEFAULT_SHOW_MAP_ACTION = "Show map"
        const val KOREAN_SHOW_MAP_ACTION = "지도로 보기"
        const val DEFAULT_SHOW_LIST_ACTION = "Show list"
        const val DEFAULT_MAP_EMPTY_TITLE = "No places in this area"
        const val DEFAULT_MAP_EMPTY_DESCRIPTION = "Move the map to look elsewhere."
        const val KOREAN_MAP_EMPTY_TITLE = "이 지역에 장소가 없습니다"
        const val KOREAN_MAP_EMPTY_DESCRIPTION = "지도를 옮겨 다른 곳을 찾아보세요"

        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
