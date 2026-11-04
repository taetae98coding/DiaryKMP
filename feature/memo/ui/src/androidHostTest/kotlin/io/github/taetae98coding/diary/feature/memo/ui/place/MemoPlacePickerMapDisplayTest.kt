package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.testing.location.coordinate
import io.github.taetae98coding.diary.feature.memo.ui.ProvideTestDiaryMap
import io.github.taetae98coding.diary.feature.memo.ui.TEST_DIARY_MAP_TAG
import io.github.taetae98coding.diary.feature.memo.ui.TestDiaryMap
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoPlacePickerMapDisplayTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val map = TestDiaryMap()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-7c9c807f-154d-4ab8-a673-9c472ab71eb7 카드의 지도를 표시할 수 있게 되면 지도 보기의 내용으로 돌아간다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        var mapState by mutableStateOf<DiaryMapState?>(null)
        setRestoredMapViewDialog(
            mapStateProvider = { mapState },
            mapUiState = MemoPlacePickerMapUiState(isLoaded = true, placeList = listOf(homePlace)),
        )
        composeRule.placeDialogSearchField().assertExists()

        mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER)
        composeRule.waitForIdle()

        composeRule.onNode(hasTestTag(TEST_DIARY_MAP_TAG) and hasAnyAncestor(isDialog())).assertExists()
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertExists()
        composeRule.placeDialogSearchField().assertDoesNotExist()
    }

    @Test
    fun `TC-6f7e7eb7-7b0f-4556-9d47-24e0d4fe9605 지도 보기지만 카드의 지도를 표시할 수 없어 목록 보기의 내용을 보여 주면 카드의 지도 위치를 넘긴다`() {
        val cardCoordinate = Arb.coordinate().next()
        val eventList = mutableListOf<MemoPlacePickerEvent>()
        setRestoredMapViewDialog(
            mapStateProvider = { null },
            cardCoordinate = cardCoordinate,
            onEvent = eventList::add,
        )

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(MemoPlacePickerEvent.ClickAdd(coordinate = cardCoordinate))
    }

    private fun setRestoredMapViewDialog(
        mapStateProvider: () -> DiaryMapState?,
        mapUiState: MemoPlacePickerMapUiState = MemoPlacePickerMapUiState(),
        cardCoordinate: Coordinate? = null,
        onEvent: (MemoPlacePickerEvent) -> Unit = {},
    ) {
        val pagingData = placePagingData(mapUiState.placeList)

        composeRule.setContent {
            ProvideTestDiaryMap(map = map) {
                DiaryTheme {
                    MemoPlacePickerDialog(
                        onDismissRequest = {},
                        onEvent = onEvent,
                        state = remember { MemoPlacePickerState(initialViewMode = MemoPlacePickerViewMode.MAP, initialIsMapViewVisited = true) },
                        searchFieldState = rememberDiaryPickerSearchFieldState(),
                        mapState = mapStateProvider(),
                        placePagingItems = remember(pagingData) { pagingData }.collectAsLazyPagingItems(),
                        uiStateProvider = { MemoPlaceInputUiState(isSelectedPlaceLoaded = true) },
                        mapUiStateProvider = { mapUiState },
                        coordinateProvider = { cardCoordinate },
                    )
                }
            }
        }
    }
}
