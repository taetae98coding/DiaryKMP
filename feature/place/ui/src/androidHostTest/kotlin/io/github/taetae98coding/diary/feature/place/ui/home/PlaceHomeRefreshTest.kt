package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.location.usecase.FetchCurrentLocationUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceHomeUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.GetProgressReportedUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceHomePlaceListUiState
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceHomePlaceListViewModel
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceList
import io.github.taetae98coding.diary.feature.place.ui.home.map.PlaceHomeMapViewModel
import io.github.taetae98coding.diary.feature.place.ui.home.viewmode.PlaceHomeViewMode
import io.github.taetae98coding.diary.feature.place.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class PlaceHomeRefreshTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-032 새로고침으로 받은 장소가 보이는 영역 안에 있으면 목록에 나타난다`() {
        val place = viewModeTestPlace()
        val receivedPlace = viewModeTestPlace()
        val uiStateFlow = MutableStateFlow(loadedUiState(listOf(place)))
        setPlaceList(uiStateFlow = uiStateFlow, isRefreshingFlow = MutableStateFlow(true))
        composeRule.onNodeWithText(place.detail.title).assertExists()

        uiStateFlow.value = loadedUiState(listOf(place, receivedPlace))
        composeRule.waitUntil { runCatching { composeRule.onNodeWithText(receivedPlace.detail.title).assertExists() }.isSuccess }

        composeRule.onNodeWithText(place.detail.title).assertExists()
        composeRule.onNodeWithText(receivedPlace.detail.title).assertExists()
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-033 새로고침이 실패해도 표시 중인 장소는 유지되고 실패 안내가 없다`() {
        val basePlace = viewModeTestPlace()
        val place = basePlace.copy(detail = basePlace.detail.copy(title = "RefreshPlace${basePlace.id.toHexString()}"))
        val progressFlow = MutableStateFlow(Result.success(false))
        val syncGate = CompletableDeferred<Unit>()
        val requestSyncUseCase = mockk<RequestSyncUseCase>()
        coEvery { requestSyncUseCase(parameter = SyncTrigger.USER_REQUESTED) } coAnswers {
            progressFlow.value = Result.success(true)
            syncGate.await()
            progressFlow.value = Result.success(false)
            Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
        }
        val getProgressReportedUseCase = mockk<GetProgressReportedUseCase>()
        every { getProgressReportedUseCase(parameter = Unit) } returns progressFlow
        val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
        // Robolectric은 지도 SDK의 뷰를 불러오지 못하므로 기본 지도를 확인하지 못한 채로 두어 지도를 그리지 않는다.
        every { getDefaultMapProviderUseCase(parameter = Unit) } returns emptyFlow()
        val fetchCurrentLocationUseCase = mockk<FetchCurrentLocationUseCase>()
        coEvery { fetchCurrentLocationUseCase(parameter = Unit) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
        val getPlaceListUseCase = mockk<GetPlaceListUseCase>()
        every { getPlaceListUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
        val pagePlaceHomeUseCase = mockk<PagePlaceHomeUseCase>()
        every { pagePlaceHomeUseCase(parameter = any()) } returns placePagingDataFlowOf(listOf(place)).map { data -> Result.success(data) }

        setPlaceHomeScreen(
            mapViewModel =
                PlaceHomeMapViewModel(
                    fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                    getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
                ),
            placeListViewModel =
                PlaceHomePlaceListViewModel(
                    getPlaceListUseCase = getPlaceListUseCase,
                    pagePlaceHomeUseCase = pagePlaceHomeUseCase,
                ),
            syncViewModel =
                PlaceHomeSyncViewModel(
                    getProgressReportedUseCase = getProgressReportedUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                ),
        )
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { exists(hasText(place.detail.title)) }

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { exists(hasContentDescription(DEFAULT_REFRESHING_DESCRIPTION)) }

        composeRule.runOnIdle { syncGate.complete(Unit) }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { !exists(hasContentDescription(DEFAULT_REFRESHING_DESCRIPTION)) }

        coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.USER_REQUESTED) }
        composeRule.onNodeWithText(place.detail.title).assertIsDisplayed()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        composeRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion)).assertCountEquals(0)
    }

    private fun setPlaceHomeScreen(
        mapViewModel: PlaceHomeMapViewModel,
        placeListViewModel: PlaceHomePlaceListViewModel,
        syncViewModel: PlaceHomeSyncViewModel,
    ) {
        composeRule.setContent {
            DiaryTheme {
                PlaceHomeScreen(
                    navigateUp = {},
                    navigateToAdd = {},
                    navigateToDetail = {},
                    navigateToSearch = {},
                    mapViewModel = mapViewModel,
                    placeListViewModel = placeListViewModel,
                    syncViewModel = syncViewModel,
                )
            }
        }
    }

    private fun exists(matcher: SemanticsMatcher): Boolean = composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun `TC-PLACE-HOME-FEATURE-034 진행 표시 중에도 목록의 장소를 선택할 수 있다`() {
        val place = viewModeTestPlace()
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        setPlaceList(
            uiStateFlow = MutableStateFlow(loadedUiState(listOf(place))),
            isRefreshingFlow = MutableStateFlow(true),
            onEvent = eventList::add,
        )

        composeRule.onNodeWithText(place.detail.title).performClick()
        composeRule.waitForIdle()

        eventList.withoutMoveMap() shouldBe listOf(PlaceHomeScaffoldEvent.ClickPlace(id = place.id))
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-034 진행 표시 중에도 장소 추가로 이동할 수 있다`() {
        val eventList = mutableListOf<PlaceHomeScaffoldEvent>()
        composeRule.setContent {
            ViewModeTestPlaceHomeScaffold(
                onEvent = eventList::add,
                state = PlaceHomeScaffoldState(initialViewMode = PlaceHomeViewMode.LIST),
                uiState = PlaceHomeUiState.Loading,
                placePagingDataFlow = placePagingDataFlowOf(listOf(viewModeTestPlace())),
                isRefreshing = true,
            )
        }

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList.withoutMoveMap() shouldBe listOf(PlaceHomeScaffoldEvent.ClickAdd(coordinate = null))
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-035 새로고침 중에 보이는 영역이 바뀌면 목록은 곧바로 갱신된다`() {
        val beforePlace = viewModeTestPlace()
        val afterPlace = viewModeTestPlace()
        val uiStateFlow = MutableStateFlow(loadedUiState(listOf(beforePlace)))
        setPlaceList(uiStateFlow = uiStateFlow, isRefreshingFlow = MutableStateFlow(true))
        composeRule.onNodeWithText(beforePlace.detail.title).assertExists()

        uiStateFlow.value = loadedUiState(listOf(afterPlace))
        composeRule.waitUntil { runCatching { composeRule.onNodeWithText(afterPlace.detail.title).assertExists() }.isSuccess }

        composeRule.onNodeWithText(beforePlace.detail.title).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()
    }

    private fun setPlaceList(
        uiStateFlow: MutableStateFlow<PlaceHomePlaceListUiState>,
        isRefreshingFlow: MutableStateFlow<Boolean>,
        onEvent: (PlaceHomeScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                val uiState by uiStateFlow.collectAsStateWithLifecycle()
                val isRefreshing by isRefreshingFlow.collectAsStateWithLifecycle()

                PlaceList(
                    onEvent = onEvent,
                    placeListUiStateProvider = { uiState },
                    isRefreshingProvider = { isRefreshing },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun loadedUiState(placeList: List<Place>): PlaceHomePlaceListUiState =
        PlaceHomePlaceListUiState(
            isLoaded = true,
            placeList = placeList,
        )

    private companion object {
        private const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        private const val TIMEOUT_MILLIS = 10_000L
    }
}
