package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.map.LocalDiaryMapContentOverride
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.domain.place.usecase.FetchSearchedPlaceUseCase
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.place.ui.TestDiaryMap
import io.github.taetae98coding.diary.feature.place.ui.add.ADDRESS_INDEX
import io.github.taetae98coding.diary.feature.place.ui.add.DEFAULT_SEARCH_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.add.PlaceAddScreen
import io.github.taetae98coding.diary.feature.place.ui.add.PlaceAddScreenTestTheme
import io.github.taetae98coding.diary.feature.place.ui.add.PlaceAddUiState
import io.github.taetae98coding.diary.feature.place.ui.add.addTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.add.screenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PlaceSearchDialogMapDisplayedTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val map = TestDiaryMap()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-5e5b894d-afc7-449e-b129-3ed99682efad 지도가 표시된 PlaceAdd에서 검색을 선택하면 다이얼로그가 열린다`() {
        setPlaceAddScreen(searchViewModel = searchViewModel(resultList = emptyList()))
        composeRule.onNodeWithContentDescription(DEFAULT_PLACE_MAP_DESCRIPTION).assertExists()
        composeRule.onNode(isDialog()).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(isDialog()).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_MAP_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-0df47f0a-7a0a-42c9-86ed-5bafa3e83118 찾은 장소가 모두 이름을 라벨로 지도에 표시된다`() {
        val placeList = placeList()
        setPlaceAddScreen(searchViewModel = searchViewModel(resultList = placeList))

        openSearchAndType()

        composeRule.runOnIdle {
            val pinList = map.lastScope().state.pins
            pinList.map { pin -> pin.id } shouldBe placeList.map { place -> place.id }
            pinList.map { pin -> pin.label } shouldBe placeList.map { place -> place.name }
            pinList.map { pin -> pin.coordinate } shouldBe placeList.map { place -> place.coordinate.toDiaryMapCoordinate() }
        }
    }

    @Test
    fun `TC-2004cda0-48ca-431a-965b-b2a0a28aeadc 지도의 핀을 누르면 다이얼로그가 닫히고 그 장소를 고른다`() {
        val placeList = placeList()
        val picked = placeList.last()
        setPlaceAddScreen(searchViewModel = searchViewModel(resultList = placeList))
        openSearchAndType()

        composeRule.runOnIdle { checkNotNull(map.lastScope().onPinClick).invoke(picked.id) }
        composeRule.waitForIdle()

        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].assert(hasText(picked.address))
        composeRule.runOnIdle { map.firstScope().state.spot shouldBe picked.coordinate.toDiaryMapCoordinate() }
    }

    @Test
    fun `TC-d718fcea-ccdf-48e7-bdae-59b07e09bff1 장소를 고르지 않고 닫으면 다이얼로그가 표시되지 않는다`() {
        val placeList = placeList()
        setPlaceAddScreen(searchViewModel = searchViewModel(resultList = placeList))
        openSearchAndType()
        composeRule.onNodeWithText(placeList.first().name).assertExists()

        composeRule.runOnIdle { ShadowDialog.getLatestDialog().onBackPressed() }
        composeRule.waitForIdle()

        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].assert(hasText(""))
    }

    @Test
    fun `TC-d867a118-96a5-4ef7-96c8-bc9f8ee75c24 닫았다 다시 열면 검색어와 결과가 비어 있다`() {
        val placeList = placeList()
        setPlaceAddScreen(searchViewModel = searchViewModel(resultList = placeList))
        openSearchAndType()
        composeRule.onNodeWithText(placeList.first().name).assertExists()
        composeRule.runOnIdle { ShadowDialog.getLatestDialog().onBackPressed() }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(isDialog()).assertExists()
        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).assertExists()
        placeList.forEach { place -> composeRule.onNodeWithText(place.name).assertDoesNotExist() }
        composeRule.runOnIdle { map.lastScope().state.pins shouldBe emptyList() }
    }

    @Test
    fun `TC-a6930ab5-2187-480e-bdef-e5ae0ec66ebf 목록과 지도 위 핀은 같은 장소를 같은 컬러로 표시한다`() {
        val placeList = placeList()
        setPlaceAddScreen(searchViewModel = searchViewModel(resultList = placeList))

        openSearchAndType()

        val pinList = composeRule.runOnIdle { map.lastScope().state.pins }
        pinList.size shouldBe placeList.size
        pinList.forEach { pin ->
            val indicator =
                composeRule.onNode(
                    hasTestTag(PLACE_SEARCH_ROW_COLOR_INDICATOR_TEST_TAG) and
                        hasAnyAncestor(hasClickAction() and hasAnyDescendant(hasText(pin.label))),
                    useUnmergedTree = true,
                )
            val pixelMap = indicator.captureToImage().toPixelMap()

            pixelMap[pixelMap.width / 2, pixelMap.height / 2] shouldBe pin.color
        }
    }

    @Test
    fun `TC-36f62361-3809-48fb-8358-662301f74cfc Google 결과는 바꾼 네이버 지도 위에 표시되지 않는다`() {
        val googleList = placeList()
        val naverList = placeList()
        val fetchSearchedPlaceUseCase = mockk<FetchSearchedPlaceUseCase>()
        coEvery { fetchSearchedPlaceUseCase(match { parameter -> parameter.provider == MapProvider.GOOGLE }) } returns Result.success(googleList)
        coEvery { fetchSearchedPlaceUseCase(match { parameter -> parameter.provider == MapProvider.NAVER }) } returns Result.success(naverList)
        setPlaceAddScreen(
            defaultProvider = MapProvider.GOOGLE,
            searchViewModel = PlaceSearchViewModel(fetchSearchedPlaceUseCase = fetchSearchedPlaceUseCase),
        )
        openSearchAndType()
        composeRule.runOnIdle {
            map
                .lastScope()
                .state.pins
                .map { pin -> pin.id } shouldBe googleList.map { place -> place.id }
        }

        composeRule.mainClock.autoAdvance = false
        composeRule.onNode(hasText(DEFAULT_NAVER_LABEL) and hasAnyAncestor(isDialog())).performClick()
        composeRule.mainClock.advanceTimeByFrame()

        composeRule.runOnIdle {
            map
                .lastScope()
                .state.pins
                .map { pin -> pin.id } shouldNotContainAnyOf googleList.map { place -> place.id }
        }

        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            map
                .lastScope()
                .state.pins
                .map { pin -> pin.id } shouldNotContainAnyOf googleList.map { place -> place.id }
            map
                .lastScope()
                .state.pins
                .map { pin -> pin.id } shouldBe naverList.map { place -> place.id }
        }
    }

    private fun setPlaceAddScreen(
        searchViewModel: PlaceSearchViewModel,
        defaultProvider: MapProvider = MapProvider.NAVER,
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalDiaryMapContentOverride provides map) {
                PlaceAddScreenTestTheme {
                    PlaceAddScreen(
                        addedResultRequestKey = null,
                        navigateToTagAdd = {},
                        tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                        navigateUp = {},
                        initialCoordinate = null,
                        addViewModel = screenTestViewModel(uiState = PlaceAddUiState(defaultProvider = defaultProvider)),
                        searchViewModel = searchViewModel,
                        navigateToTagDetail = {},
                        tagViewModel = addTagScreenTestViewModel(),
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun openSearchAndType() {
        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).performTextInput(randomText(prefix = QUERY_PREFIX))
        composeRule.waitForIdle()
    }

    private fun searchViewModel(resultList: List<SearchedPlace>): PlaceSearchViewModel {
        val uiState = MutableStateFlow<PlaceSearchUiState>(PlaceSearchUiState.Idle)
        val viewModel = mockk<PlaceSearchViewModel>(relaxed = true)
        every { viewModel.uiState } returns uiState
        every { viewModel.search(any()) } answers { uiState.value = PlaceSearchUiState.Content(placeList = resultList) }
        every { viewModel.clear() } answers { uiState.value = PlaceSearchUiState.Idle }
        return viewModel
    }

    private fun placeList(): List<SearchedPlace> =
        listOf(FIRST_PLACE_NAME_PREFIX, SECOND_PLACE_NAME_PREFIX).mapIndexed { index, prefix ->
            searchedPlace(
                name = randomText(prefix = prefix),
                address = randomText(prefix = PLACE_ADDRESS_PREFIX),
                coordinate = Coordinate(latitude = BASE_LATITUDE + index, longitude = BASE_LONGITUDE + index),
            )
        }

    private companion object {
        private const val DEFAULT_PLACE_MAP_DESCRIPTION = "Place location map"
        private const val DEFAULT_SEARCH_MAP_DESCRIPTION = "Place search map"
        private const val DEFAULT_NAVER_LABEL = "Naver"
        private const val QUERY_PREFIX = "query"
        private const val PLACE_ADDRESS_PREFIX = "address"
        private const val FIRST_PLACE_NAME_PREFIX = "first-place"
        private const val SECOND_PLACE_NAME_PREFIX = "second-place"
        private const val BASE_LATITUDE = 37.0
        private const val BASE_LONGITUDE = 127.0
    }
}
