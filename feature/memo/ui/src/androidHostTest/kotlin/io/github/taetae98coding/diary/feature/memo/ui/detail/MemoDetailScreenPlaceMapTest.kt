package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.testing.location.coordinate
import io.github.taetae98coding.diary.feature.memo.ui.ProvideTestDiaryMap
import io.github.taetae98coding.diary.feature.memo.ui.TEST_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.TestDiaryMap
import io.github.taetae98coding.diary.feature.memo.ui.contact.screenTestContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.GOOGLE_PROVIDER_NAME
import io.github.taetae98coding.diary.feature.memo.ui.place.HOME_PLACE_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceMapUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.OFFICE_PLACE_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.place.placePagingData
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlacePickerMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.testPlace
import io.github.taetae98coding.diary.feature.memo.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.memo.ui.web.screenTestWebViewModel
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScreenPlaceMapTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val map = TestDiaryMap()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-051a9522-ef23-47a0-b4ce-4c5154f8f9f4 상세 대상 메모가 바뀌면 초기 지도 제공자와 초기 지도 위치를 다시 정한다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val officePlace = testPlace(title = OFFICE_PLACE_TITLE)
        val uiState = MutableStateFlow<MemoDetailUiState>(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE)))
        val placeUiState = MutableStateFlow(MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(homePlace)))
        setMemoDetailScreen(uiState = uiState, placeUiState = placeUiState)
        composeRule.onNodeWithText(GOOGLE_PROVIDER_NAME).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.runOnIdle { map.scopes.single().reportCamera(coordinate = Arb.coordinate().next().toDiaryMapCoordinate(), zoom = TEST_ZOOM) }

        placeUiState.value = MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(officePlace))
        uiState.value = memoDetailUiState(id = SECOND_MEMO_ID, detail = memoDetail(MEMO_TITLE))
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            map.scopes
                .single()
                .state.provider shouldBe DiaryMapProvider.NAVER
            map.scopes
                .single()
                .state.coordinate shouldBe DiaryMapCoordinate(latitude = officePlace.detail.coordinate.latitude, longitude = officePlace.detail.coordinate.longitude)
        }
    }

    private fun setMemoDetailScreen(
        uiState: MutableStateFlow<MemoDetailUiState>,
        placeUiState: MutableStateFlow<MemoPlaceInputUiState>,
    ) {
        val placeMapViewModel =
            screenTestPlaceMapViewModel(
                uiState = MutableStateFlow(MemoPlaceMapUiState.Content(provider = MapProvider.NAVER, isCurrentLocationFetched = true, currentCoordinate = Arb.coordinate().next())),
            )

        composeRule.setContent {
            ProvideTestDiaryMap(map = map) {
                MemoDetailScreenTestTheme {
                    MemoDetailScreen(
                        addRequestKey = TEST_ADD_REQUEST_KEY,
                        detailViewModel = screenTestViewModel(uiState = uiState),
                        tagViewModel = screenTestTagViewModel(),
                        webViewModel = screenTestWebViewModel(),
                        contactViewModel = screenTestContactViewModel(),
                        placeViewModel = screenTestPlaceViewModel(uiState = placeUiState, placePagingData = placePagingData()),
                        placeMapViewModel = placeMapViewModel,
                        placePickerMapViewModel = screenTestPlacePickerMapViewModel(),
                        geminiViewModel = screenTestGeminiViewModel(),
                        contentSharerFactory = screenTestContentSharerFactory(),
                        navigateUp = {},
                        navigateToCopiedMemo = {},
                        navigateToTagAdd = {},
                        navigateToTagDetail = {},
                        navigateToWebAdd = {},
                        navigateToWebDetail = {},
                        navigateToContactAdd = {},
                        navigateToContactDetail = {},
                        navigateToPlaceAdd = {},
                        navigateToPlaceDetail = {},
                        navigateToGeminiSetting = {},
                        componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                        isStandalone = true,
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val TEST_ZOOM = 12.0
        val SECOND_MEMO_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000031")
    }
}
