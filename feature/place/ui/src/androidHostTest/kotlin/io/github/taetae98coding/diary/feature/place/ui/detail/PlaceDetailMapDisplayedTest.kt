package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.map.LocalDiaryMapContentOverride
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.place.ui.TestDiaryMap
import io.github.taetae98coding.diary.feature.place.ui.diaryMapCoordinateInFormPrecision
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
        val savedCoordinate = Arb.diaryMapCoordinateInFormPrecision().next()
        val viewedCoordinate = generateSequence { Arb.diaryMapCoordinateInFormPrecision().next() }.first { coordinate -> coordinate != savedCoordinate }
        val changedCoordinate =
            generateSequence { Arb.diaryMapCoordinateInFormPrecision().next() }
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
    fun `TC-feafcf73-daab-4274-94ad-a7310523a3b3 Google 지도로 바꾸면 Google 웹 지도 링크를 공유한다`() {
        val contentSharer = testContentSharer()
        val id = Uuid.random()
        val detail = placeDetail()
        setPlaceDetailScreen(id = id, uiState = MutableStateFlow(mapDisplayedContent(id = id, detail = detail)), contentSharer = contentSharer)
        composeRule.runOnIdle { map.firstScope().state.provider shouldBe DiaryMapProvider.NAVER }

        composeRule.onNode(hasText(DEFAULT_GOOGLE_LABEL) and hasAnyAncestor(hasContentDescription(DEFAULT_MAP_DESCRIPTION))).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        val sharedText = composeRule.sharedText(contentSharer = contentSharer)
        sharedText.lines().last() shouldBe
            externalMapWebUri(
                provider = DiaryMapProvider.GOOGLE,
                coordinate = detail.coordinate.toDiaryMapCoordinate(),
                title = detail.title,
                address = detail.address,
            )
    }

    @Test
    fun `TC-744926fc-9b16-4d87-bd7e-6bd123f904d1 좌표가 성립하지 않는 채 수정을 실행해도 지도가 보고 있는 위치를 옮기지 않는다`() {
        val savedCoordinate = Arb.diaryMapCoordinateInFormPrecision().next()
        val id = Uuid.random()
        val detail = placeDetail(coordinate = Coordinate(latitude = savedCoordinate.latitude, longitude = savedCoordinate.longitude))
        val uiState = MutableStateFlow<PlaceDetailUiState>(mapDisplayedContent(id = id, detail = detail))
        val effect = Channel<PlaceDetailEffect>(Channel.BUFFERED)
        val viewModel = screenTestViewModel(uiState = uiState, effect = effect.receiveAsFlow())
        every { viewModel.update(any()) } answers { effect.trySend(PlaceDetailEffect.CoordinateInvalid).getOrThrow() }
        setPlaceDetailScreen(id = id, uiState = uiState, viewModel = viewModel)
        composeRule.mainClock.advanceTimeBy(REFLECT_WAIT_MILLIS)
        composeRule.runOnIdle { map.firstScope().state.coordinate shouldBe savedCoordinate }

        composeRule.input(LATITUDE_INDEX).performTextReplacement(INVALID_LATITUDE)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).performClick()
        composeRule.mainClock.advanceTimeBy(REFLECT_WAIT_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_COORDINATE_INVALID_MESSAGE).assertIsDisplayed()
        composeRule.runOnIdle { map.firstScope().state.coordinate shouldBe savedCoordinate }
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
        val viewedCoordinate = Arb.diaryMapCoordinateInFormPrecision().next()
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
        viewModel: PlaceDetailViewModel = screenTestViewModel(uiState = uiState),
        contentSharer: ContentSharer = testContentSharer(),
    ) {
        preparePlaceDetailTabViewModels()

        composeRule.setContent {
            PlaceDetailScreenContent(id = id, uiState = uiState, uriHandler = uriHandler, viewModel = viewModel, contentSharer = contentSharer)
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun PlaceDetailScreenContent(
        id: Uuid,
        uiState: MutableStateFlow<PlaceDetailUiState>,
        uriHandler: UriHandler,
        viewModel: PlaceDetailViewModel = screenTestViewModel(uiState = uiState),
        contentSharer: ContentSharer = testContentSharer(),
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
                    detailViewModel = viewModel,
                    searchViewModel = searchScreenTestViewModel(),
                    navigateUp = {},
                    navigateToTagDetail = {},
                    tagViewModel = detailTagScreenTestViewModel(),
                    contentSharerFactory = contentSharerFactoryOf(contentSharer = contentSharer),
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
