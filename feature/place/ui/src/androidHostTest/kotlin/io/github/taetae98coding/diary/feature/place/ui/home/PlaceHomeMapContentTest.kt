package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.LocalDiaryMapContentOverride
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.place.PLACE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.testing.place.coordinateInFormPrecision
import io.github.taetae98coding.diary.domain.location.usecase.FetchCurrentLocationUseCase
import io.github.taetae98coding.diary.domain.place.usecase.DeletePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceHomeUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.place.ui.TestDiaryMap
import io.github.taetae98coding.diary.feature.place.ui.boundsAround
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceHomePlaceListViewModel
import io.github.taetae98coding.diary.feature.place.ui.home.map.PlaceHomeMapViewModel
import io.github.taetae98coding.diary.feature.place.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class PlaceHomeMapContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-e58f9381-fbd8-4b33-8c2e-d1740d611245 지도 핀을 누르면 그 장소의 상세 이동을 한 번 요청하고 지도 위치와 확대 수준은 그대로다`() {
        val center = fixtureMonkey.coordinateInFormPrecision()
        val selected = placeAt(center.shifted(latitude = SPAN / 4))
        val other = placeAt(center.shifted(latitude = -SPAN / 4))
        val environment = setPlaceHomeScreen(placeList = listOf(selected, other))
        environment.reportCamera(center)
        environment.awaitPins(selected, other)

        composeRule.runOnIdle { checkNotNull(environment.map.firstScope().onPinClick).invoke(selected.id) }
        composeRule.waitForIdle()

        environment.detailRequests shouldBe listOf(selected.id)
        environment.map
            .firstScope()
            .state.coordinate shouldBe center.toDiaryMapCoordinate()
        environment.map.firstScope().zoom shouldBe ZOOM
    }

    @Test
    fun `TC-6543ccdc-af73-4b8b-84b2-dc6209abe57f 지도 모드의 목록에서 삭제한 장소는 카드와 핀에서 함께 사라진다`() {
        val center = fixtureMonkey.coordinateInFormPrecision()
        val deleted = placeAt(center.shifted(latitude = SPAN / 4))
        val remained = placeAt(center.shifted(latitude = -SPAN / 4))
        val environment = setPlaceHomeScreen(placeList = listOf(deleted, remained))
        environment.reportCamera(center)
        environment.awaitPins(deleted, remained)
        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(deleted.detail.title)).assertExists()

        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(deleted.detail.title)).performTouchInput { swipeLeft() }
        environment.awaitPins(remained)

        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(deleted.detail.title)).assertDoesNotExist()
        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(remained.detail.title)).assertExists()
    }

    @Test
    fun `TC-57a1bc64-5002-4bd9-9d16-8335f806a412 지도 모드의 목록에서 장소를 누르면 지도 모드 그대로 그 장소로 지도를 옮기도록 지정한다`() {
        val center = fixtureMonkey.coordinateInFormPrecision()
        val edge = placeAt(center.shifted(latitude = SPAN * EDGE_RATIO))
        val environment = setPlaceHomeScreen(placeList = listOf(edge))
        environment.reportCamera(center)
        environment.awaitPins(edge)

        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(edge.detail.title)).performClick()
        composeRule.waitForIdle()

        environment.map
            .firstScope()
            .state.coordinate shouldBe edge.detail.coordinate.toDiaryMapCoordinate()
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).assertExists()
        environment.detailRequests shouldBe emptyList()
    }

    @Test
    fun `TC-57a1bc64-5002-4bd9-9d16-8335f806a412 옮겨진 지도가 알려 온 영역으로 갱신된 목록과 핀에 누른 장소가 남는다`() {
        val center = fixtureMonkey.coordinateInFormPrecision()
        val edge = placeAt(center.shifted(latitude = SPAN * EDGE_RATIO))
        val environment = setPlaceHomeScreen(placeList = listOf(edge))
        environment.reportCamera(center)
        environment.awaitPins(edge)
        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(edge.detail.title)).performClick()
        composeRule.waitForIdle()

        environment.reportCamera(edge.detail.coordinate)

        environment.awaitPins(edge)
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).assertExists()
        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(edge.detail.title)).assertExists()
    }

    @Test
    fun `TC-41fc41e1-dd6c-4625-924f-7c992bc83387 지도 모드에서 지도가 표시되어 있으면 검색 동작을 제공한다`() {
        setPlaceHomeScreen()

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-6e45770e-5557-4df2-8abb-b3290de441d7 위치 권한이 없어도 지도가 표시된 화면은 권한 요청을 시작하지 않는다`() {
        val registry = spyk<ActivityResultRegistry>()
        every { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) } answers {
            registry.dispatchResult(firstArg(), emptyMap<String, Boolean>())
        }

        setPlaceHomeScreen(activityResultRegistry = registry)

        verify(exactly = 0) { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) }
    }

    @Test
    fun `TC-71fb1a30-04b1-4c7d-a3bd-e5cb8b080265 서울 도심을 보고 있으면 장소 추가와 추가 단축키가 그 위치를 넘긴다`() {
        assertAddPassesReportedCoordinate(Coordinate(latitude = SEOUL_LATITUDE, longitude = SEOUL_LONGITUDE))
    }

    @Test
    fun `TC-71fb1a30-04b1-4c7d-a3bd-e5cb8b080265 남반구와 서반구를 보고 있으면 장소 추가와 추가 단축키가 그 위치를 넘긴다`() {
        assertAddPassesReportedCoordinate(Coordinate(latitude = SOUTHERN_LATITUDE, longitude = WESTERN_LONGITUDE))
    }

    @Test
    fun `TC-440f4a4b-22f1-4197-a0bf-150a899fdd74 잠시 바꿔 본 지도 제공자는 장소 추가로 넘기지 않는다`() {
        val initialCoordinate = fixtureMonkey.coordinateInFormPrecision()
        val environment = setPlaceHomeScreen(defaultProvider = MapProvider.NAVER, initialCoordinate = initialCoordinate)

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).performClick()
        composeRule.waitForIdle()
        environment.map
            .firstScope()
            .state.provider shouldBe DiaryMapProvider.GOOGLE

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        environment.addRequests shouldBe listOf(initialCoordinate)
    }

    @Test
    fun `TC-b5d53633-e3f9-46ab-9f5d-85137c3aca96 지도를 옮기고 제공자를 바꾼 뒤 목록 모드에서 고른 장소는 식별자만 상세로 넘긴다`() {
        val place = placeAt(fixtureMonkey.coordinateInFormPrecision())
        val environment = setPlaceHomeScreen(defaultProvider = MapProvider.NAVER, placeList = listOf(place))
        environment.reportCamera(fixtureMonkey.coordinateInFormPrecision())
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(place.detail.title)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(place.detail.title)).performClick()
        composeRule.waitForIdle()

        environment.detailRequests shouldBe listOf(place.id)
    }

    @Test
    fun `TC-a7bb2c96-a33b-4a95-9440-8dfb068f462b 목록 모드에 다녀와도 옮긴 지도 위치와 확대 수준, 고른 제공자를 유지한다`() {
        val moved = fixtureMonkey.coordinateInFormPrecision()
        val environment = setPlaceHomeScreen(defaultProvider = MapProvider.NAVER)
        environment.reportCamera(moved)
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_MAP_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        val state = environment.map.firstScope().state
        state.provider shouldBe DiaryMapProvider.GOOGLE
        state.coordinate shouldBe moved.toDiaryMapCoordinate()
        environment.map.firstScope().zoom shouldBe ZOOM
    }

    private fun setPlaceHomeScreen(
        defaultProvider: MapProvider = MapProvider.NAVER,
        initialCoordinate: Coordinate = fixtureMonkey.coordinateInFormPrecision(),
        placeList: List<Place> = emptyList(),
        activityResultRegistry: ActivityResultRegistry = spyk<ActivityResultRegistry>(),
    ): Environment {
        val environment = Environment(storedPlaces = MutableStateFlow(placeList))
        val mapViewModel = mapViewModel(defaultProvider = defaultProvider, initialCoordinate = initialCoordinate)
        val placeListViewModel = placeListViewModel(storedPlaces = environment.storedPlaces, placeList = placeList)
        val syncViewModel = syncViewModel()
        val registryOwner = mockk<ActivityResultRegistryOwner>()
        every { registryOwner.activityResultRegistry } returns activityResultRegistry

        composeRule.setContent {
            CompositionLocalProvider(
                LocalDiaryMapContentOverride provides environment.map,
                LocalActivityResultRegistryOwner provides registryOwner,
            ) {
                DiaryTheme {
                    PlaceHomeScreen(
                        navigateUp = {},
                        navigateToAdd = environment.addRequests::add,
                        navigateToDetail = environment.detailRequests::add,
                        navigateToSearch = {},
                        mapViewModel = mapViewModel,
                        placeListViewModel = placeListViewModel,
                        syncViewModel = syncViewModel,
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { environment.map.isShown }

        return environment
    }

    private fun assertAddPassesReportedCoordinate(reported: Coordinate) {
        val environment = setPlaceHomeScreen()
        environment.reportCamera(reported)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        performAddShortcut()

        environment.addRequests shouldBe List(ADD_ACTION_COUNT) { reported }
    }

    private fun Environment.reportCamera(coordinate: Coordinate) {
        val center = coordinate.toDiaryMapCoordinate()

        composeRule.runOnIdle { map.firstScope().reportCamera(coordinate = center, zoom = ZOOM, bounds = boundsAround(center = center, span = SPAN)) }
        composeRule.waitForIdle()
    }

    private fun Environment.awaitPins(vararg placeList: Place) {
        val expected = placeList.map { place -> place.id }

        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            map
                .firstScope()
                .state.pins
                .map { pin -> pin.id }
                .toSet() == expected.toSet()
        }
        map
            .firstScope()
            .state.pins
            .map { pin -> pin.id } shouldContainExactlyInAnyOrder expected
    }

    private fun performAddShortcut() {
        composeRule.onRoot().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.A)
            keyUp(Key.A)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()
    }

    private class Environment(
        val storedPlaces: MutableStateFlow<List<Place>>,
    ) {
        val map = TestDiaryMap()
        val addRequests = mutableListOf<Coordinate?>()
        val detailRequests = mutableListOf<Uuid>()
    }

    private companion object {
        const val DEFAULT_GOOGLE_LABEL = "Google"
        const val TIMEOUT_MILLIS = 5_000L

        // 지도가 처음 정하는 확대 수준과 구분되도록 다른 값으로 알려 온다.
        const val ZOOM = 11.0
        const val SPAN = 0.02
        const val EDGE_RATIO = 0.45
        const val SEOUL_LATITUDE = 37.5665
        const val SEOUL_LONGITUDE = 126.978
        const val SOUTHERN_LATITUDE = -34.6037
        const val WESTERN_LONGITUDE = -58.3816

        const val ADD_ACTION_COUNT = 2

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun placeAt(coordinate: Coordinate): Place {
            val place = viewModeTestPlace()

            return place.copy(detail = place.detail.copy(coordinate = coordinate))
        }

        private fun Coordinate.shifted(latitude: Double): Coordinate = copy(latitude = this.latitude + latitude)

        private fun CoordinateBounds.contains(coordinate: Coordinate): Boolean = coordinate.latitude in south..north && coordinate.longitude in west..east

        private fun mapViewModel(
            defaultProvider: MapProvider,
            initialCoordinate: Coordinate,
        ): PlaceHomeMapViewModel {
            val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
            every { getDefaultMapProviderUseCase(parameter = Unit) } returns flowOf(Result.success(defaultProvider))
            val fetchCurrentLocationUseCase = mockk<FetchCurrentLocationUseCase>()
            coEvery { fetchCurrentLocationUseCase(parameter = Unit) } returns Result.success(initialCoordinate)

            return PlaceHomeMapViewModel(
                fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
            )
        }

        private fun placeListViewModel(
            storedPlaces: MutableStateFlow<List<Place>>,
            placeList: List<Place>,
        ): PlaceHomePlaceListViewModel {
            val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
            every { getPlaceListUseCase(parameter = any()) } answers {
                val bounds = firstArg<GetPlaceListUseCase.Parameter>().bounds

                storedPlaces.map { stored -> Result.success(stored.filter { place -> bounds.contains(place.detail.coordinate) }) }
            }
            val pagePlaceHomeUseCase = mockk<PagePlaceHomeUseCase>()
            every { pagePlaceHomeUseCase(parameter = any()) } returns placePagingDataFlowOf(placeList).map { pagingData -> Result.success(pagingData) }
            val deletePlaceUseCase = mockk<DeletePlaceUseCase>()
            coEvery { deletePlaceUseCase(parameter = any()) } answers {
                val id = firstArg<Uuid>()
                storedPlaces.update { stored -> stored.filterNot { place -> place.id == id } }

                Result.success(1)
            }

            return PlaceHomePlaceListViewModel(
                getPlaceListUseCase = getPlaceListUseCase,
                pagePlaceHomeUseCase = pagePlaceHomeUseCase,
                deletePlaceUseCase = deletePlaceUseCase,
                restorePlaceUseCase = mockk(),
            )
        }

        private fun syncViewModel(): SyncRefreshViewModel {
            val viewModel = mockk<SyncRefreshViewModel>(relaxed = true)
            every { viewModel.uiState } returns MutableStateFlow(SyncRefreshUiState())

            return viewModel
        }
    }
}
