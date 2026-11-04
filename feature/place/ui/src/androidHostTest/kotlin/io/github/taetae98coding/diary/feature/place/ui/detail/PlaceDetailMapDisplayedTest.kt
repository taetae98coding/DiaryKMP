package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.map.LocalDiaryMapContentOverride
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.place.ui.TestDiaryMap
import io.github.taetae98coding.diary.feature.place.ui.add.mapCoordinateInFormPrecision
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class PlaceDetailMapDisplayedTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val map = TestDiaryMap()

    @Test
    fun `TC-45f8be21-1996-4488-a977-965aae3321dc 저장된 좌표가 바뀌어도 지도가 보고 있던 위치와 확대 수준이 유지된다`() {
        val savedCoordinate = mapCoordinateInFormPrecision()
        val viewedCoordinate = generateSequence { mapCoordinateInFormPrecision() }.first { coordinate -> coordinate != savedCoordinate }
        val changedCoordinate =
            generateSequence { mapCoordinateInFormPrecision() }
                .first { coordinate -> coordinate != savedCoordinate && coordinate != viewedCoordinate }
        val id = Uuid.random()
        val detail = placeDetail(coordinate = Coordinate(latitude = savedCoordinate.latitude, longitude = savedCoordinate.longitude))
        val uiState = MutableStateFlow<PlaceDetailUiState>(mapDisplayedContent(id = id, detail = detail))
        setPlaceDetailScreen(id = id, uiState = uiState)
        composeRule.mainClock.advanceTimeBy(REFLECT_WAIT_MILLIS)
        composeRule.runOnIdle { map.firstScope().state.spot shouldBe savedCoordinate }
        composeRule.runOnIdle { map.firstScope().reportCamera(coordinate = viewedCoordinate, zoom = REPORTED_ZOOM) }
        composeRule.waitForIdle()

        uiState.value =
            mapDisplayedContent(
                id = id,
                detail = detail.copy(coordinate = Coordinate(latitude = changedCoordinate.latitude, longitude = changedCoordinate.longitude)),
            )
        composeRule.mainClock.advanceTimeBy(REFLECT_WAIT_MILLIS)
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            map.firstScope().state.coordinate shouldBe viewedCoordinate
            map.firstScope().zoom shouldBe REPORTED_ZOOM
            map.firstScope().state.spot shouldBe savedCoordinate
        }
    }

    @Test
    fun `TC-98f7320a-532b-481e-a946-1120e2f5aa07 지도에서 Google 지도로 바꾸면 Google 지도에서 입력된 좌표가 열린다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        val id = Uuid.random()
        val detail = placeDetail()
        setPlaceDetailScreen(id = id, uiState = MutableStateFlow(mapDisplayedContent(id = id, detail = detail)), uriHandler = uriHandler)
        composeRule.runOnIdle { map.firstScope().state.provider shouldBe DiaryMapProvider.NAVER }

        composeRule.onNode(hasText(DEFAULT_GOOGLE_LABEL) and hasAnyAncestor(hasContentDescription(DEFAULT_MAP_DESCRIPTION))).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_GOOGLE_MAP_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            verify(exactly = 1) {
                uriHandler.openUri(
                    externalMapWebUri(
                        provider = DiaryMapProvider.GOOGLE,
                        coordinate = detail.coordinate.toDiaryMapCoordinate(),
                        title = detail.title,
                        address = detail.address,
                    ),
                )
            }
            verify(exactly = 1) { uriHandler.openUri(any()) }
        }
    }

    @Test
    fun `TC-5e5b894d-afc7-449e-b129-3ed99682efad 지도가 표시된 PlaceDetail에서 검색을 선택하면 다이얼로그가 열린다`() {
        val id = Uuid.random()
        setPlaceDetailScreen(id = id, uiState = MutableStateFlow(mapDisplayedContent(id = id, detail = placeDetail())))
        composeRule.onNodeWithContentDescription(DEFAULT_MAP_DESCRIPTION).assertExists()
        composeRule.onNode(isDialog()).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(isDialog()).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_MAP_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-42d4d33b-e660-4a59-8bed-f3506a0c36fe 화면이 재생성되어도 지도가 보고 있던 위치와 확대 수준을 유지한다`() {
        val viewedCoordinate = mapCoordinateInFormPrecision()
        val id = Uuid.random()
        val uiState = MutableStateFlow<PlaceDetailUiState>(mapDisplayedContent(id = id, detail = placeDetail()))
        val restorationTester = StateRestorationTester(composeRule)
        preparePlaceDetailTabViewModels()
        restorationTester.setContent { PlaceDetailScreenContent(id = id, uiState = uiState, uriHandler = mockk(relaxed = true)) }
        composeRule.mainClock.advanceTimeBy(REFLECT_WAIT_MILLIS)
        composeRule.runOnIdle { map.firstScope().reportCamera(coordinate = viewedCoordinate, zoom = REPORTED_ZOOM) }
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            map.firstScope().state.coordinate shouldBe viewedCoordinate
            map.firstScope().zoom shouldBe REPORTED_ZOOM
        }
    }

    private fun mapDisplayedContent(
        id: Uuid,
        detail: PlaceDetail,
    ): PlaceDetailUiState.Content = PlaceDetailUiState.Content(id = id, detail = detail, defaultProvider = MapProvider.NAVER)

    private fun setPlaceDetailScreen(
        id: Uuid,
        uiState: MutableStateFlow<PlaceDetailUiState>,
        uriHandler: UriHandler = mockk(relaxed = true),
    ) {
        preparePlaceDetailTabViewModels()

        composeRule.setContent { PlaceDetailScreenContent(id = id, uiState = uiState, uriHandler = uriHandler) }
        composeRule.waitForIdle()
    }

    @Composable
    private fun PlaceDetailScreenContent(
        id: Uuid,
        uiState: MutableStateFlow<PlaceDetailUiState>,
        uriHandler: UriHandler,
    ) {
        CompositionLocalProvider(
            LocalUriHandler provides uriHandler,
            LocalDiaryMapContentOverride provides map,
        ) {
            PlaceDetailScreenTestTheme {
                PlaceDetailScreen(
                    navigateToTagAdd = {},
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    navigateToMemoFinishedList = {},
                    id = id,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = screenTestViewModel(uiState = uiState),
                    searchViewModel = searchScreenTestViewModel(),
                    navigateUp = {},
                    navigateToTagDetail = {},
                    tagViewModel = detailTagScreenTestViewModel(),
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_GOOGLE_LABEL = "Google"
        private const val DEFAULT_SEARCH_BUTTON_DESCRIPTION = "Search place"
        private const val DEFAULT_SEARCH_MAP_DESCRIPTION = "Place search map"

        // 지도가 처음 정하는 확대 수준과 구분되도록 다른 값으로 알려 온다.
        private const val REPORTED_ZOOM = 11.0

        // 입력 정지 대기 시간이 넉넉히 지나도록 기다린다.
        private const val REFLECT_WAIT_MILLIS = 400L
    }
}
