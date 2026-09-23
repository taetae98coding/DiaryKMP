package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val SAVED_COORDINATE = Coordinate(latitude = 37.5665, longitude = 126.978)

private val INVALID_COORDINATE_LIST =
    listOf(
        "" to "",
        "abc" to "127.0",
        "90.1" to "127.0",
        "37.5" to "180.1",
    )

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailOpenExternalMapTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-032 외부 지도로 열기를 실행하면 입력된 좌표가 앱 밖의 지도에서 열린다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        val detail = placeDetail(coordinate = SAVED_COORDINATE)

        composeRule.setPlaceDetailScreen(viewModel = savedViewModel(detail = detail), uriHandler = uriHandler)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION).performClick()

        composeRule.runOnIdle {
            verify(exactly = 1) { uriHandler.openUri(savedAppUri(detail = detail)) }
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-033 좌표가 성립하지 않으면 지도를 열지 않고 좌표를 알린다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)

        composeRule.setPlaceDetailScreen(
            viewModel = savedViewModel(detail = placeDetail(coordinate = SAVED_COORDINATE)),
            uriHandler = uriHandler,
        )
        composeRule.waitForIdle()

        INVALID_COORDINATE_LIST.forEach { (latitude, longitude) ->
            composeRule.input(LATITUDE_INDEX).performTextReplacement(latitude)
            composeRule.input(LONGITUDE_INDEX).performTextReplacement(longitude)
            composeRule.onNodeWithContentDescription(DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION).performClick()
            composeRule.waitForIdle()

            composeRule.onNodeWithText(DEFAULT_COORDINATE_INVALID_MESSAGE).assertIsDisplayed()
        }

        composeRule.runOnIdle {
            verify(exactly = 0) { uriHandler.openUri(any()) }
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-034 외부 지도로 열어도 입력 중인 내용과 화면을 유지하고 장소를 수정하지 않는다`() {
        val viewModel = savedViewModel(detail = placeDetail(coordinate = SAVED_COORDINATE))

        composeRule.setPlaceDetailScreen(viewModel = viewModel)
        composeRule.waitForIdle()

        composeRule.input(TITLE_INDEX).performTextReplacement(CHANGED_TITLE)
        composeRule.input(DESCRIPTION_INDEX).performTextReplacement(CHANGED_DESCRIPTION)
        composeRule.input(ADDRESS_INDEX).performTextReplacement(CHANGED_ADDRESS)
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.input(TITLE_INDEX).assert(hasText(CHANGED_TITLE))
        composeRule.input(DESCRIPTION_INDEX).assert(hasText(CHANGED_DESCRIPTION))
        composeRule.input(ADDRESS_INDEX).assert(hasText(CHANGED_ADDRESS))
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertIsDisplayed()
        composeRule.runOnIdle {
            verify(exactly = 0) { viewModel.update(any()) }
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-035 지도 앱과 웹 지도를 모두 열지 못해도 화면을 유지하고 알리지 않는다`() {
        val uriHandler =
            mockk<UriHandler> {
                every { openUri(any()) } throws IllegalArgumentException("외부 지도를 열 수 없습니다.")
            }

        composeRule.setPlaceDetailScreen(
            viewModel = savedViewModel(detail = placeDetail(coordinate = SAVED_COORDINATE)),
            uriHandler = uriHandler,
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.input(LATITUDE_INDEX).assert(hasText(SAVED_COORDINATE.latitude.toString()))
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertIsDisplayed()
        composeRule
            .onAllNodes(hasText(DEFAULT_COORDINATE_INVALID_MESSAGE))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-042 지도 앱을 열지 못하면 같은 위치를 웹 지도로 연다`() {
        val detail = placeDetail(coordinate = SAVED_COORDINATE)
        val appUri = savedAppUri(detail = detail)
        val webUri = savedWebUri(detail = detail)
        val uriHandler =
            mockk<UriHandler> {
                every { openUri(appUri) } throws IllegalArgumentException("네이버 지도 앱을 열 수 없습니다.")
                every { openUri(webUri) } returns Unit
            }

        composeRule.setPlaceDetailScreen(viewModel = savedViewModel(detail = detail), uriHandler = uriHandler)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertIsDisplayed()
        composeRule
            .onAllNodes(hasText(DEFAULT_COORDINATE_INVALID_MESSAGE))
            .fetchSemanticsNodes()
            .size shouldBe 0
        composeRule.runOnIdle {
            verifyOrder {
                uriHandler.openUri(appUri)
                uriHandler.openUri(webUri)
            }
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-036 지도가 표시되지 않아도 외부 지도로 열기를 제공한다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)

        val detail = placeDetail(coordinate = SAVED_COORDINATE)

        composeRule.setPlaceDetailScreen(viewModel = savedViewModel(detail = detail), uriHandler = uriHandler)
        composeRule.waitForIdle()

        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_MAP_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION).performClick()

        composeRule.runOnIdle {
            verify(exactly = 1) { uriHandler.openUri(savedAppUri(detail = detail)) }
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-DOMAIN-021 외부 지도로 여는 위치는 저장된 좌표가 아니라 입력된 좌표다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)

        val detail = placeDetail(coordinate = SAVED_COORDINATE)

        composeRule.setPlaceDetailScreen(viewModel = savedViewModel(detail = detail), uriHandler = uriHandler)
        composeRule.waitForIdle()

        composeRule.input(LATITUDE_INDEX).performTextReplacement(CHANGED_LATITUDE)
        composeRule.input(LONGITUDE_INDEX).performTextReplacement(CHANGED_LONGITUDE)
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION).performClick()

        val changedUri =
            savedAppUri(
                detail = detail,
                coordinate =
                    Coordinate(
                        latitude = CHANGED_LATITUDE.toDouble(),
                        longitude = CHANGED_LONGITUDE.toDouble(),
                    ),
            )

        composeRule.runOnIdle {
            verify(exactly = 1) { uriHandler.openUri(changedUri) }
            verify(exactly = 0) { uriHandler.openUri(savedAppUri(detail = detail)) }
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-DOMAIN-023 수정을 처리하는 동안에도 외부 지도로 열기를 실행할 수 있다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)

        val detail = placeDetail(coordinate = SAVED_COORDINATE)

        composeRule.setPlaceDetailScreen(
            viewModel = savedViewModel(detail = detail, isUpdateInProgress = true),
            uriHandler = uriHandler,
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION).performClick()

        composeRule.runOnIdle {
            verify(exactly = 1) { uriHandler.openUri(savedAppUri(detail = detail)) }
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-DOMAIN-023 삭제를 처리하는 동안에도 외부 지도로 열기를 실행할 수 있다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)

        val detail = placeDetail(coordinate = SAVED_COORDINATE)

        composeRule.setPlaceDetailScreen(
            viewModel = savedViewModel(detail = detail, isDeleteInProgress = true),
            uriHandler = uriHandler,
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION).performClick()

        composeRule.runOnIdle {
            verify(exactly = 1) { uriHandler.openUri(savedAppUri(detail = detail)) }
        }
    }

    @Test
    fun `Google 지도를 보고 있으면 열기 버튼이 Google 지도로 알려진다`() {
        composeRule.setContent {
            DiaryTheme {
                PlaceDetailTopBar(
                    onEvent = {},
                    uiStateProvider = { PlaceDetailUiState.Content(id = Uuid.NIL, detail = placeDetail()) },
                    mapProviderProvider = { DiaryMapProvider.GOOGLE },
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_GOOGLE_MAP_BUTTON_DESCRIPTION).assertIsDisplayed()
        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    private fun savedViewModel(
        detail: PlaceDetail,
        isUpdateInProgress: Boolean = false,
        isDeleteInProgress: Boolean = false,
    ): PlaceDetailViewModel =
        screenTestViewModel(
            uiState =
                MutableStateFlow(
                    content(
                        detail = detail,
                        isUpdateInProgress = isUpdateInProgress,
                        isDeleteInProgress = isDeleteInProgress,
                    ),
                ),
        )

    // Android는 네이버 지도 앱 주소를 먼저 열므로, 열기에 성공하는 환경에서 관찰되는 주소는 앱 주소다.
    private fun savedAppUri(
        detail: PlaceDetail,
        coordinate: Coordinate = SAVED_COORDINATE,
    ): String =
        naverMapAppUri(
            coordinate = coordinate.toDiaryMapCoordinate(),
            title = detail.title,
            appName = RuntimeEnvironment.getApplication().packageName,
        )

    private fun savedWebUri(detail: PlaceDetail): String =
        externalMapWebUri(
            provider = DiaryMapProvider.NAVER,
            coordinate = SAVED_COORDINATE.toDiaryMapCoordinate(),
            title = detail.title,
            address = detail.address,
        )
}
