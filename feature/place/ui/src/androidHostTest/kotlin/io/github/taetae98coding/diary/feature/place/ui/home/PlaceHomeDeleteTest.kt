package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.place.PLACE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.place.PlaceListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.location.usecase.FetchCurrentLocationUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.GetProgressReportedUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceHomePlaceListUiState
import io.github.taetae98coding.diary.feature.place.ui.home.list.PlaceHomePlaceListViewModel
import io.github.taetae98coding.diary.feature.place.ui.home.map.PlaceHomeMapViewModel
import io.github.taetae98coding.diary.feature.place.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class PlaceHomeDeleteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-054 목록 모드에서 장소 카드를 삭제 방향으로 밀면 삭제를 요청하고 기본 안내와 실행 취소를 표시한다`() {
        val environment = setPlaceHomeScreen()

        swipeCardLeft(environment.first)

        verify(exactly = 1) { environment.viewModel.delete(id = environment.first.id) }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-PLACE-HOME-FEATURE-054 한국어 환경에서 삭제 안내와 실행 취소를 표시한다`() {
        val environment = setPlaceHomeScreen(listViewModeDescription = KOREAN_LIST_VIEW_MODE_DESCRIPTION)

        swipeCardLeft(environment.first)

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-057 장소 카드를 반대 방향으로 밀면 삭제를 요청하지 않고 안내도 표시하지 않는다`() {
        val environment = setPlaceHomeScreen()

        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(environment.first.detail.title)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 0) { environment.viewModel.delete(id = any()) }
        composeRule.onNodeWithText(environment.first.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-059 삭제를 실행 취소하면 그 장소의 삭제 되돌리기를 요청한다`() {
        val environment = setPlaceHomeScreen()

        swipeCardLeft(environment.first)
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.first.id) }
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-060 안내가 보이는 동안 다른 장소를 삭제하면 마지막 삭제에만 실행 취소가 적용된다`() {
        val environment = setPlaceHomeScreen()

        swipeCardLeft(environment.first)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        environment.effectChannel.trySend(PlaceListEffect.Deleted(id = environment.second.id)).getOrThrow()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.second.id) }
        verify(exactly = 0) { environment.viewModel.restore(id = environment.first.id) }
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-061 실행 취소를 선택하지 않으면 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val environment = setPlaceHomeScreen()

        swipeCardLeft(environment.first)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-063 안내가 보이는 동안 화면을 떠나면 안내가 닫히고 되돌릴 수 없다`() {
        var isShown by mutableStateOf(true)
        val environment = setPlaceHomeScreen(isShownProvider = { isShown })

        swipeCardLeft(environment.first)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
    }

    @Test
    fun `TC-PLACE-HOME-FEATURE-064 안내가 보이는 동안 보기 모드를 바꿔도 안내가 남고 실행 취소할 수 있다`() {
        val environment = setPlaceHomeScreen()

        swipeCardLeft(environment.first)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_MAP_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = environment.first.id) }
    }

    private fun swipeCardLeft(place: Place) {
        composeRule.onNode(hasTestTag(PLACE_CARD_TEST_TAG) and hasText(place.detail.title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun setPlaceHomeScreen(
        listViewModeDescription: String = DEFAULT_LIST_VIEW_MODE_DESCRIPTION,
        isShownProvider: () -> Boolean = { true },
    ): Environment {
        val first = viewModeTestPlace()
        val second = viewModeTestPlace()
        val effectChannel = Channel<PlaceListEffect>(capacity = Channel.BUFFERED)
        val viewModel = mockk<PlaceHomePlaceListViewModel>()
        every { viewModel.sort } returns MutableStateFlow(ListSort.TITLE)
        every { viewModel.placeListUiState } returns MutableStateFlow(PlaceHomePlaceListUiState())
        every { viewModel.placePagingData } returns placePagingDataFlowOf(listOf(first, second))
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        every { viewModel.delete(id = any()) } answers {
            effectChannel.trySend(PlaceListEffect.Deleted(id = firstArg())).getOrThrow()
        }
        justRun { viewModel.restore(id = any()) }
        justRun { viewModel.updateVisibleBounds(bounds = any()) }
        val mapViewModel = mapViewModel()
        val syncViewModel = syncViewModel()

        composeRule.setContent {
            DiaryTheme {
                if (isShownProvider()) {
                    PlaceHomeScreen(
                        navigateUp = {},
                        navigateToAdd = {},
                        navigateToDetail = {},
                        navigateToSearch = {},
                        mapViewModel = mapViewModel,
                        placeListViewModel = viewModel,
                        syncViewModel = syncViewModel,
                    )
                }
            }
        }
        composeRule.onNodeWithContentDescription(listViewModeDescription).performClick()
        composeRule.waitUntil(timeoutMillis = LIST_ITEM_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(first.detail.title).fetchSemanticsNodes().isNotEmpty()
        }

        return Environment(viewModel = viewModel, effectChannel = effectChannel, first = first, second = second)
    }

    private class Environment(
        val viewModel: PlaceHomePlaceListViewModel,
        val effectChannel: Channel<PlaceListEffect>,
        val first: Place,
        val second: Place,
    )

    private companion object {
        const val DEFAULT_DELETED_MESSAGE = "Place deleted."
        const val KOREAN_DELETED_MESSAGE = "장소가 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
        const val LIST_ITEM_TIMEOUT_MILLIS = 5_000L

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun mapViewModel(): PlaceHomeMapViewModel {
            // Robolectric은 지도 SDK의 뷰를 불러오지 못하므로 기본 지도를 확인하지 못한 채로 두어 지도를 그리지 않는다.
            val getDefaultMapProviderUseCase = mockk<GetDefaultMapProviderUseCase>()
            every { getDefaultMapProviderUseCase(parameter = Unit) } returns emptyFlow()
            val fetchCurrentLocationUseCase = mockk<FetchCurrentLocationUseCase>()
            coEvery { fetchCurrentLocationUseCase(parameter = Unit) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))

            return PlaceHomeMapViewModel(
                fetchCurrentLocationUseCase = fetchCurrentLocationUseCase,
                getDefaultMapProviderUseCase = getDefaultMapProviderUseCase,
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
