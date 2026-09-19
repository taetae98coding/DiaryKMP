package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.closeDialogByBack
import io.github.taetae98coding.diary.feature.memo.ui.place.DEFAULT_PLACE_PICKER_PLACE_ADD
import io.github.taetae98coding.diary.feature.memo.ui.place.DEFAULT_PLACE_PICKER_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.place.DEFAULT_PLACE_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.place.HOME_PLACE_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.place.OFFICE_PLACE_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.place.placeDialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.testPlace
import io.github.taetae98coding.diary.feature.place.api.PlaceAddedResult
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoAddScreenPlaceAddTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-025 목록의 장소 추가 항목을 누르면 목록이 닫히고 PlaceAdd 이동을 요청한다`() {
        val placeList = listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE))
        var placeAddCount = 0
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(placeList = placeList),
            navigateToPlaceAdd = { placeAddCount += 1 },
        )

        composeRule.openPlacePicker()
        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).performClick()
        composeRule.waitForIdle()

        placeAddCount shouldBe 1
        composeRule.onNodeWithText(DEFAULT_PLACE_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-026 PlaceAdd 화면에서 추가한 장소 하나가 돌아왔을 때 선택된다`() {
        val selectedPlace = testPlace(title = HOME_PLACE_TITLE)
        val addedPlace = testPlace(title = OFFICE_PLACE_TITLE)
        val resultEventBus = ResultEventBus()
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(placeList = listOf(selectedPlace, addedPlace)),
            resultEventBus = resultEventBus,
        )
        composeRule.selectPlace(title = HOME_PLACE_TITLE)

        resultEventBus.sendPlaceAddedResult(addedPlace)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()
        composeRule.onNodeWithText(OFFICE_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-026 PlaceAdd 화면에서 추가한 장소 여러 개가 돌아왔을 때 모두 선택된다`() {
        val firstAddedPlace = testPlace(title = FIRST_ADDED_PLACE_TITLE)
        val secondAddedPlace = testPlace(title = SECOND_ADDED_PLACE_TITLE)
        val resultEventBus = ResultEventBus()
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(placeList = listOf(firstAddedPlace, secondAddedPlace)),
            resultEventBus = resultEventBus,
        )

        resultEventBus.sendPlaceAddedResult(firstAddedPlace)
        resultEventBus.sendPlaceAddedResult(secondAddedPlace)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(FIRST_ADDED_PLACE_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_ADDED_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-027 장소를 하나도 추가하지 않고 돌아오면 선택이 그대로 유지된다`() {
        val placeList = listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE))
        var placeAddCount = 0
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(placeList = placeList),
            navigateToPlaceAdd = { placeAddCount += 1 },
        )
        composeRule.selectPlace(title = HOME_PLACE_TITLE)

        composeRule.openPlacePicker()
        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).performClick()
        composeRule.waitForIdle()

        placeAddCount shouldBe 1
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()
        composeRule.onNodeWithText(OFFICE_PLACE_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-028 PlaceAdd 화면에서 돌아와도 장소 선택 목록이 저절로 열리지 않는다`() {
        val addedPlace = testPlace(title = OFFICE_PLACE_TITLE)
        val resultEventBus = ResultEventBus()
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(placeList = listOf(addedPlace)),
            resultEventBus = resultEventBus,
        )
        composeRule.openPlacePicker()
        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).performClick()
        composeRule.waitForIdle()

        resultEventBus.sendPlaceAddedResult(addedPlace)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_PLACE_PICKER_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(OFFICE_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-DOMAIN-019 자동 선택된 장소의 선택을 해제하면 다시 선택되지 않는다`() {
        val addedPlace = testPlace(title = OFFICE_PLACE_TITLE)
        val resultEventBus = ResultEventBus()
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(placeList = listOf(addedPlace)),
            resultEventBus = resultEventBus,
        )
        resultEventBus.sendPlaceAddedResult(addedPlace)
        composeRule.waitForIdle()

        composeRule.openPlacePicker()
        composeRule.placeDialogNodeWithText(OFFICE_PLACE_TITLE).performClick()
        composeRule.closeDialogByBack()

        composeRule.onNodeWithText(OFFICE_PLACE_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-DOMAIN-025 지도가 표시되지 않으면 지도 위치를 넘기지 않는다`() {
        var placeAddCount = 0
        var passedCoordinate: Coordinate? = null
        setMemoAddScreen(
            viewModels = screenTestRealViewModel(placeList = emptyList()),
            navigateToPlaceAdd = { coordinate ->
                placeAddCount += 1
                passedCoordinate = coordinate
            },
        )

        composeRule.openPlacePicker()

        placeAddCount shouldBe 1
        passedCoordinate shouldBe null
    }

    private fun ResultEventBus.sendPlaceAddedResult(place: Place) {
        sendResult<PlaceAddedResult>(result = PlaceAddedResult(id = place.id))
    }

    private fun ComposeContentTestRule.openPlacePicker() {
        onNodeWithText(DEFAULT_PLACE_SELECT_LABEL).performSemanticsAction(SemanticsActions.OnClick)
        waitForIdle()
    }

    private fun ComposeContentTestRule.selectPlace(title: String) {
        openPlacePicker()
        placeDialogNodeWithText(title).performClick()
        closeDialogByBack()
    }

    private fun setMemoAddScreen(
        viewModels: MemoAddScreenViewModels,
        navigateToPlaceAdd: (Coordinate?) -> Unit = {},
        resultEventBus: ResultEventBus = ResultEventBus(),
    ) {
        composeRule.setContent {
            MemoAddScreenTestTheme(resultEventBus = resultEventBus) {
                MemoAddScreen(
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    addViewModel = viewModels.viewModel,
                    tagViewModel = viewModels.tagViewModel,
                    webViewModel = viewModels.webViewModel,
                    placeViewModel = viewModels.placeViewModel,
                    placeMapViewModel = screenTestPlaceMapViewModel(),
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToPlaceAdd = navigateToPlaceAdd,
                    navigateToPlaceDetail = {},
                    initialDateRange = null,
                    componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
                    isStandalone = true,
                )
            }
        }
    }

    private companion object {
        private const val FIRST_ADDED_PLACE_TITLE: String = "MemoPlaceFirstAdded"
        private const val SECOND_ADDED_PLACE_TITLE: String = "MemoPlaceSecondAdded"
    }
}
