package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.domain.location.usecase.FetchCurrentLocationUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailPlaceExecutionBoundaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var placeState: TagDetailPlaceState
    private lateinit var mapState: DiaryMapState

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-006 다른 탭을 선택한 뒤 장소 탭으로 돌아와도 지도 모드를 유지하고 현재 위치를 다시 확인하지 않는다`() {
        val fetch = fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>())
        val mapViewModel = mapViewModel(fetch)
        val hoistedState = TagDetailPlaceState()
        var isPlaceTabSelected by mutableStateOf(true)
        composeRule.setContent {
            if (isPlaceTabSelected) PlaceTab(mapViewModel = mapViewModel, state = hoistedState)
        }
        selectMapMode()

        composeRule.runOnIdle { isPlaceTabSelected = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isPlaceTabSelected = true }
        composeRule.waitForIdle()

        composeRule.runOnIdle { placeState.viewMode shouldBe TagDetailPlaceViewMode.MAP }
        coVerify(exactly = 1) { fetch(parameter = Unit) }
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-006 화면이 회전하거나 창 크기가 바뀌어도 지도 모드를 유지하고 현재 위치를 다시 확인하지 않는다`() {
        val fetch = fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>())
        val mapViewModel = mapViewModel(fetch)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { PlaceTab(mapViewModel = mapViewModel) }
        selectMapMode()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.runOnIdle { placeState.viewMode shouldBe TagDetailPlaceViewMode.MAP }
        coVerify(exactly = 1) { fetch(parameter = Unit) }
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-006 앱이 종료되지 않은 채 백그라운드에 갔다가 돌아와도 지도 모드를 유지하고 현재 위치를 다시 확인하지 않는다`() {
        val fetch = fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>())
        val mapViewModel = mapViewModel(fetch)
        composeRule.setContent { PlaceTab(mapViewModel = mapViewModel) }
        selectMapMode()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        composeRule.runOnIdle { placeState.viewMode shouldBe TagDetailPlaceViewMode.MAP }
        coVerify(exactly = 1) { fetch(parameter = Unit) }
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-006 장소 추가나 장소 상세로 이동한 뒤 뒤로 돌아와도 지도 모드를 유지하고 현재 위치를 다시 확인하지 않는다`() {
        val fetch = fetchCurrentLocationUseCase(fixtureMonkey.giveMeOne<Coordinate>())
        val mapViewModel = mapViewModel(fetch)
        var isTagDetailOnTop by mutableStateOf(true)
        composeRule.setContent {
            // 내비게이션이 뒤에 쌓인 화면을 컴포지션에서 내리고 저장 상태만 보관하는 것을 그대로 따른다.
            val saveableStateHolder = rememberSaveableStateHolder()
            if (isTagDetailOnTop) {
                saveableStateHolder.SaveableStateProvider(key = TAG_DETAIL_ENTRY_KEY) {
                    PlaceTab(mapViewModel = mapViewModel)
                }
            }
        }
        selectMapMode()

        listOf("장소 추가", "장소 상세").forEach { destination ->
            withClue(destination) {
                composeRule.runOnIdle { isTagDetailOnTop = false }
                composeRule.waitForIdle()
                composeRule.runOnIdle { isTagDetailOnTop = true }
                composeRule.waitForIdle()

                composeRule.runOnIdle { placeState.viewMode shouldBe TagDetailPlaceViewMode.MAP }
                coVerify(exactly = 1) { fetch(parameter = Unit) }
            }
        }
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-012 시스템이 앱을 종료했다가 되살려도 지도 모드를 유지하고 새로 확인한 위치에서 지도를 시작한다`() {
        val beforeCoordinate = fixtureMonkey.giveMeOne<Coordinate>()
        val movedCoordinate = fixtureMonkey.giveMeOne<Coordinate>()
        val afterCoordinate = fixtureMonkey.giveMeOne<Coordinate>()
        val beforeFetch = fetchCurrentLocationUseCase(beforeCoordinate)
        val afterFetch = fetchCurrentLocationUseCase(afterCoordinate)
        val restorationTester = StateRestorationTester(composeRule)
        var mapViewModel = mapViewModel(beforeFetch)
        restorationTester.setContent { PlaceTab(mapViewModel = mapViewModel) }
        selectMapMode()
        composeRule.runOnIdle { mapState.coordinate shouldBe beforeCoordinate.toDiaryMapCoordinate() }
        composeRule.runOnIdle { mapState.moveTo(movedCoordinate.toDiaryMapCoordinate()) }

        mapViewModel = mapViewModel(afterFetch)
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            placeState.viewMode shouldBe TagDetailPlaceViewMode.MAP
            mapState.coordinate shouldBe afterCoordinate.toDiaryMapCoordinate()
        }
        coVerify(exactly = 1) { beforeFetch(parameter = Unit) }
        coVerify(exactly = 1) { afterFetch(parameter = Unit) }
    }

    // Robolectric은 지도 SDK의 뷰를 불러오지 못하므로 탭 본문 대신 보기 모드, 현재 위치 확인, 지도 상태를 정하는 부분만 구성한다.
    @Composable
    private fun PlaceTab(
        mapViewModel: TagDetailPlaceMapViewModel,
        state: TagDetailPlaceState = rememberTagDetailPlaceState(),
    ) {
        val uiState by mapViewModel.uiState.collectAsState()
        placeState = state
        TagDetailPlaceFetchCurrentLocationEffect(mapViewModel = mapViewModel, state = state)
        mapState = rememberTagDetailPlaceMapState(uiState = uiState)
    }

    private fun selectMapMode() {
        composeRule.waitForIdle()
        composeRule.runOnIdle { placeState.toggleViewMode() }
        composeRule.waitForIdle()
    }

    private companion object {
        private const val TAG_DETAIL_ENTRY_KEY = "TagDetail"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun fetchCurrentLocationUseCase(coordinate: Coordinate): FetchCurrentLocationUseCase {
            val useCase = mockk<FetchCurrentLocationUseCase>()
            coEvery { useCase(parameter = Unit) } returns Result.success(coordinate)

            return useCase
        }

        private fun mapViewModel(fetchCurrentLocationUseCase: FetchCurrentLocationUseCase): TagDetailPlaceMapViewModel {
            val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
            every { getDefaultMapProviderUseCase(parameter = Unit) } returns flowOf(Result.success(MapProvider.NAVER))

            return TagDetailPlaceMapViewModel(
                fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
            )
        }
    }
}
