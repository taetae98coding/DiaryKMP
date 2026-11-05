package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.LocalDiaryMapContentOverride
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.place.ui.TestDiaryMap
import io.github.taetae98coding.diary.feature.place.ui.diaryMapCoordinateInFormPrecision
import io.github.taetae98coding.diary.feature.place.ui.toCoordinateText
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class PlaceAddMapDisplayedTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val map = TestDiaryMap()

    @Test
    fun `TC-fe1090cf-43f0-43bd-8b14-41edb172d785 지도가 다른 위치와 확대 수준을 알려와도 입력한 좌표는 바뀌지 않는다`() {
        val selectedCoordinate = Arb.diaryMapCoordinateInFormPrecision().next()
        val viewedCoordinate = generateSequence { Arb.diaryMapCoordinateInFormPrecision().next() }.first { coordinate -> coordinate != selectedCoordinate }
        setPlaceAddScreen(uiState = MutableStateFlow(PlaceAddUiState(defaultProvider = MapProvider.NAVER)))

        composeRule.runOnIdle { checkNotNull(map.firstScope().onSpotClick).invoke(selectedCoordinate) }
        composeRule.waitForIdle()
        assertTypedCoordinate(selectedCoordinate)

        composeRule.runOnIdle { map.firstScope().reportCamera(coordinate = viewedCoordinate, zoom = REPORTED_ZOOM) }
        composeRule.waitForIdle()
        assertTypedCoordinate(selectedCoordinate)

        composeRule.runOnIdle { map.firstScope().reportCamera(coordinate = viewedCoordinate, zoom = CHANGED_ZOOM) }
        composeRule.waitForIdle()
        assertTypedCoordinate(selectedCoordinate)
    }

    @Test
    fun `TC-f882b25c-284b-4470-b1ed-6de9707acc5e 기본 지도가 바뀌어 Google 지도로 새로 시작해도 고른 태그와 입력이 남는다`() {
        val tag = placeTestTag(title = SELECTED_TAG_TITLE)
        val uiState = MutableStateFlow(PlaceAddUiState(defaultProvider = MapProvider.NAVER))
        setPlaceAddScreen(uiState = uiState, tagViewModel = addTagScreenTestViewModel(tagList = listOf(tag)))
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.waitForIdle()
        composeRule.runOnIdle { map.firstScope().state.provider shouldBe DiaryMapProvider.NAVER }
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()

        uiState.value = PlaceAddUiState(defaultProvider = MapProvider.GOOGLE)
        composeRule.waitForIdle()

        composeRule.runOnIdle { map.firstScope().state.provider shouldBe DiaryMapProvider.GOOGLE }
        composeRule.onNodeWithContentDescription(DEFAULT_MAP_DESCRIPTION).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(TYPED_TITLE))
        composeRule.onNodeWithText(SELECTED_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-baeab7b1-e21d-42dd-b491-17b9fc0d4559 메모리 정리 뒤 복원하면 지도가 보고 있던 위치와 확대 수준이 복원된다`() {
        val viewedCoordinate = Arb.diaryMapCoordinateInFormPrecision().next()
        val viewModel = placeAddViewModel(uiState = MutableStateFlow(PlaceAddUiState(defaultProvider = MapProvider.NAVER)))
        val tagViewModel = addTagScreenTestViewModel()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { PlaceAddScreenContent(viewModel = viewModel, tagViewModel = tagViewModel) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { map.firstScope().reportCamera(coordinate = viewedCoordinate, zoom = CHANGED_ZOOM) }
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            map.firstScope().state.coordinate shouldBe viewedCoordinate
            map.firstScope().zoom shouldBe CHANGED_ZOOM
        }
    }

    private fun assertTypedCoordinate(coordinate: DiaryMapCoordinate) {
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].assert(hasText(coordinate.latitude.toCoordinateText()))
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].assert(hasText(coordinate.longitude.toCoordinateText()))
    }

    private fun setPlaceAddScreen(
        uiState: MutableStateFlow<PlaceAddUiState>,
        tagViewModel: PlaceAddTagViewModel = addTagScreenTestViewModel(),
    ) {
        val viewModel = placeAddViewModel(uiState = uiState)

        composeRule.setContent { PlaceAddScreenContent(viewModel = viewModel, tagViewModel = tagViewModel) }
        composeRule.waitForIdle()
    }

    @Composable
    private fun PlaceAddScreenContent(
        viewModel: PlaceAddViewModel,
        tagViewModel: PlaceAddTagViewModel,
    ) {
        CompositionLocalProvider(LocalDiaryMapContentOverride provides map) {
            PlaceAddScreenTestTheme {
                PlaceAddScreen(
                    addedResultRequestKey = null,
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    initialCoordinate = null,
                    addViewModel = viewModel,
                    searchViewModel = remember { searchScreenTestViewModel() },
                    navigateToTagDetail = {},
                    tagViewModel = tagViewModel,
                )
            }
        }
    }

    private fun placeAddViewModel(uiState: MutableStateFlow<PlaceAddUiState>): PlaceAddViewModel {
        val viewModel = mockk<PlaceAddViewModel>()
        every { viewModel.uiState } returns uiState
        every { viewModel.effect } returns emptyFlow()
        return viewModel
    }

    private companion object {
        private const val SELECTED_TAG_TITLE = "PlaceAddMapDisplayedTag"
        private const val DEFAULT_MAP_DESCRIPTION = "Place location map"
        private const val REPORTED_ZOOM = 15.0
        private const val CHANGED_ZOOM = 18.0
    }
}
