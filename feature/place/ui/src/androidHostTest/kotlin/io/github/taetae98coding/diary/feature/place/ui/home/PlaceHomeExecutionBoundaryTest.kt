package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.location.usecase.FetchCurrentLocationUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceHomeUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.GetProgressReportedUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceHomePlaceListViewModel
import io.github.taetae98coding.diary.feature.place.ui.home.map.PlaceHomeMapViewModel
import io.github.taetae98coding.diary.feature.place.ui.home.map.rememberPlaceHomeMapState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class PlaceHomeExecutionBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `TC-PLACE-HOME-DOMAIN-003 화면이 회전하거나 창 크기가 바뀌어도 현재 위치를 다시 확인하지 않는다`() {
        val fetch = fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>())
        val mapViewModel = mapViewModel(fetch)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { PlaceHomeTestScreen(mapViewModel = mapViewModel) }
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { fetch(parameter = Unit) }
    }

    @Test
    fun `TC-PLACE-HOME-DOMAIN-003 앱이 종료되지 않은 채 백그라운드에 갔다가 복귀해도 현재 위치를 다시 확인하지 않는다`() {
        val fetch = fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>())
        val mapViewModel = mapViewModel(fetch)
        composeRule.setContent { PlaceHomeTestScreen(mapViewModel = mapViewModel) }
        composeRule.waitForIdle()

        goToBackgroundAndReturn()

        coVerify(exactly = 1) { fetch(parameter = Unit) }
    }

    @Test
    fun `TC-PLACE-HOME-DOMAIN-022 화면이 회전하거나 창 크기가 바뀌어도 바꿔 둔 보기 모드를 유지한다`() {
        val mapViewModel = mapViewModel(fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>()))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { PlaceHomeTestScreen(mapViewModel = mapViewModel) }
        selectListMode()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_MAP_VIEW_MODE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-PLACE-HOME-DOMAIN-022 시스템이 앱을 종료했다가 같은 화면으로 되살려도 바꿔 둔 보기 모드를 유지한다`() {
        var mapViewModel = mapViewModel(fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>()))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { PlaceHomeTestScreen(mapViewModel = mapViewModel) }
        selectListMode()

        mapViewModel = mapViewModel(fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>()))
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_MAP_VIEW_MODE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-PLACE-HOME-DOMAIN-022 앱의 백그라운드 진입 후 복귀해도 바꿔 둔 보기 모드를 유지한다`() {
        val mapViewModel = mapViewModel(fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>()))
        composeRule.setContent { PlaceHomeTestScreen(mapViewModel = mapViewModel) }
        selectListMode()

        goToBackgroundAndReturn()

        composeRule.onNodeWithContentDescription(DEFAULT_MAP_VIEW_MODE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-PLACE-HOME-DOMAIN-022 장소 추가, 장소 상세나 검색으로 이동한 뒤 뒤로 돌아와도 바꿔 둔 보기 모드를 유지한다`() {
        val mapViewModel = mapViewModel(fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>()))
        var isPlaceHomeOnTop by mutableStateOf(true)
        composeRule.setContent {
            // 내비게이션이 뒤에 쌓인 화면을 컴포지션에서 내리고 저장 상태만 보관하는 것을 그대로 따른다.
            val saveableStateHolder = rememberSaveableStateHolder()
            if (isPlaceHomeOnTop) {
                saveableStateHolder.SaveableStateProvider(key = PLACE_HOME_ENTRY_KEY) {
                    PlaceHomeTestScreen(mapViewModel = mapViewModel)
                }
            }
        }
        selectListMode()

        listOf("장소 추가", "장소 상세", "검색").forEach { destination ->
            withClue(destination) {
                composeRule.runOnIdle { isPlaceHomeOnTop = false }
                composeRule.waitForIdle()
                composeRule.runOnIdle { isPlaceHomeOnTop = true }
                composeRule.waitForIdle()

                composeRule.onNodeWithContentDescription(DEFAULT_MAP_VIEW_MODE_DESCRIPTION).assertExists()
            }
        }
    }

    @Test
    fun `TC-PLACE-HOME-DOMAIN-025 지도 모드로 되살아나면 즉시 현재 위치를 다시 확인한다`() {
        val beforeFetch = fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>())
        val afterFetch = fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>())
        val restorationTester = StateRestorationTester(composeRule)
        var mapViewModel = mapViewModel(beforeFetch)
        restorationTester.setContent { PlaceHomeTestScreen(mapViewModel = mapViewModel) }
        composeRule.waitForIdle()
        coVerify(exactly = 1) { beforeFetch(parameter = Unit) }

        mapViewModel = mapViewModel(afterFetch)
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { afterFetch(parameter = Unit) }
    }

    @Test
    fun `TC-PLACE-HOME-DOMAIN-025 목록 모드로 되살아나도 즉시 현재 위치를 다시 확인하고 지도 모드로 바꿀 때는 다시 확인하지 않는다`() {
        val beforeFetch = fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>())
        val afterFetch = fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>())
        val restorationTester = StateRestorationTester(composeRule)
        var mapViewModel = mapViewModel(beforeFetch)
        restorationTester.setContent { PlaceHomeTestScreen(mapViewModel = mapViewModel) }
        selectListMode()
        coVerify(exactly = 1) { beforeFetch(parameter = Unit) }

        mapViewModel = mapViewModel(afterFetch)
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_MAP_VIEW_MODE_DESCRIPTION).assertExists()
        coVerify(exactly = 1) { afterFetch(parameter = Unit) }

        composeRule.onNodeWithContentDescription(DEFAULT_MAP_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { afterFetch(parameter = Unit) }
    }

    @Test
    fun `TC-PLACE-HOME-DOMAIN-025 되살아난 뒤 새로 확인한 위치에서 지도를 시작하고 옮겨 두었던 위치는 이어지지 않는다`() {
        val beforeCoordinate = fixtureMonkey.giveMeOne<Coordinate>()
        val movedCoordinate = fixtureMonkey.giveMeOne<Coordinate>()
        val afterCoordinate = fixtureMonkey.giveMeOne<Coordinate>()
        val restorationTester = StateRestorationTester(composeRule)
        var uiState by mutableStateOf(loaded(beforeCoordinate))
        lateinit var mapState: DiaryMapState
        restorationTester.setContent { mapState = rememberPlaceHomeMapState(uiState = uiState) }
        composeRule.runOnIdle { mapState.moveTo(movedCoordinate.toDiaryMapCoordinate()) }

        uiState = PlaceHomeUiState.Loading
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle { uiState = loaded(afterCoordinate) }
        composeRule.waitForIdle()

        composeRule.runOnIdle { mapState.coordinate shouldBe afterCoordinate.toDiaryMapCoordinate() }
    }

    @Composable
    private fun PlaceHomeTestScreen(mapViewModel: PlaceHomeMapViewModel) {
        DiaryTheme {
            PlaceHomeScreen(
                navigateUp = {},
                navigateToAdd = {},
                navigateToDetail = {},
                navigateToSearch = {},
                mapViewModel = mapViewModel,
                placeListViewModel = remember { placeListViewModel() },
                syncViewModel = remember { syncViewModel() },
            )
        }
    }

    private fun selectListMode() {
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun goToBackgroundAndReturn() {
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()
    }

    private companion object {
        private const val PLACE_HOME_ENTRY_KEY = "PlaceHome"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun loaded(coordinate: Coordinate): PlaceHomeUiState = PlaceHomeUiState.Loaded(defaultProvider = MapProvider.NAVER, initialCoordinate = coordinate)

        private fun fetchCurrentLocationUseCase(coordinate: Coordinate): FetchCurrentLocationUseCase {
            val useCase = mockk<FetchCurrentLocationUseCase>()
            coEvery { useCase(parameter = Unit) } returns Result.success(coordinate)

            return useCase
        }

        private fun mapViewModel(fetchCurrentLocationUseCase: FetchCurrentLocationUseCase): PlaceHomeMapViewModel {
            // Robolectric은 지도 SDK의 뷰를 불러오지 못하므로 기본 지도를 확인하지 못한 채로 두어 지도를 그리지 않는다.
            val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
            every { getDefaultMapProviderUseCase(parameter = Unit) } returns emptyFlow()

            return PlaceHomeMapViewModel(
                fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
            )
        }

        private fun placeListViewModel(): PlaceHomePlaceListViewModel {
            val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
            every { getPlaceListUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
            val pagePlaceHomeUseCase = mockk<PagePlaceHomeUseCase>()
            every { pagePlaceHomeUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.empty<Place>()))

            return PlaceHomePlaceListViewModel(
                getPlaceListUseCase = getPlaceListUseCase,
                pagePlaceHomeUseCase = pagePlaceHomeUseCase,
                deletePlaceUseCase = mockk(),
                restorePlaceUseCase = mockk(),
            )
        }

        private fun syncViewModel(): PlaceHomeSyncViewModel {
            val getProgressReportedUseCase = mockk<GetProgressReportedUseCase>()
            every { getProgressReportedUseCase(parameter = Unit) } returns flowOf(Result.success(false))

            return PlaceHomeSyncViewModel(
                getProgressReportedUseCase = getProgressReportedUseCase,
                requestSyncUseCase = mockk<RequestSyncUseCase>(relaxed = true),
            )
        }
    }
}
