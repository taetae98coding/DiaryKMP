package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.contact.screenTestContactViewModel
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.DEFAULT_PLACE_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.place.HOME_PLACE_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceInputUiState
import io.github.taetae98coding.diary.feature.memo.ui.place.MemoPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.OFFICE_PLACE_TITLE
import io.github.taetae98coding.diary.feature.memo.ui.place.placeDialogNodeWithText
import io.github.taetae98coding.diary.feature.memo.ui.place.placePagingData
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.testPlace
import io.github.taetae98coding.diary.feature.memo.ui.web.screenTestWebViewModel
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScreenPlaceTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-078 TC-MEMO-PLACE-CARD-DOMAIN-009 상세 대상 메모가 바뀌면 새 대상 메모에 연결된 장소를 노출한다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val officePlace = testPlace(title = OFFICE_PLACE_TITLE)
        val placeList = listOf(homePlace, officePlace)
        val uiState = MutableStateFlow<MemoDetailUiState>(memoDetailUiState(id = FIRST_MEMO_ID, detail = memoDetail(MEMO_TITLE)))
        val placeUiState =
            MutableStateFlow<MemoPlaceInputUiState>(
                MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(homePlace)),
            )

        composeRule.setMemoDetailScreenWithTag(uiState = uiState, placeUiState = placeUiState)
        composeRule.onNodeWithText(DEFAULT_PLACE_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()

        uiState.value = memoDetailUiState(id = SECOND_PLACE_MEMO_ID, detail = memoDetail(MEMO_TITLE))
        placeUiState.value = MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(officePlace))
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_PLACE_SELECT_LABEL).performScrollTo()

        composeRule.onNodeWithText(OFFICE_PLACE_TITLE).assertExists()
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-077 장소를 선택하면 즉시 연결 변경을 요청하고 성공 안내는 표시하지 않는다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val placeViewModel = screenTestPlaceViewModel(placePagingData = placePagingData(listOf(homePlace)))

        composeRule.setMemoDetailScreenWithPlace(placeViewModel = placeViewModel)
        composeRule.performPlaceSelectClick()
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { placeViewModel.selectPlace(placeId = homePlace.id) }
        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-077 장소 선택을 해제하면 즉시 연결 해제를 요청하고 성공 안내는 표시하지 않는다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val placeViewModel =
            screenTestPlaceViewModel(
                uiState = MutableStateFlow(MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(homePlace))),
                placePagingData = placePagingData(listOf(homePlace)),
            )

        composeRule.setMemoDetailScreenWithPlace(placeViewModel = placeViewModel)
        composeRule.performPlaceSelectClick()
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { placeViewModel.unselectPlace(placeId = homePlace.id) }
        composeRule.onNodeWithText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-079 화면이 재생성되면 장소 카드는 저장된 장소 연결을 다시 표시한다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val placeViewModel =
            screenTestPlaceViewModel(
                uiState = MutableStateFlow(MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(homePlace))),
            )
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { MemoDetailScreenWithPlace(placeViewModel = placeViewModel) }
        composeRule.onNodeWithText(DEFAULT_PLACE_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.onNodeWithText(DEFAULT_PLACE_SELECT_LABEL).performScrollTo()

        composeRule.onNodeWithText(HOME_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-058 장소를 선택해도 수정 버튼이 나타나지 않는다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)

        composeRule.setMemoDetailScreenWithTag(placePagingData = placePagingData(listOf(homePlace)))
        composeRule.performPlaceSelectClick()
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-058 장소 선택을 해제해도 수정 버튼이 나타나지 않는다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val placeUiState = MutableStateFlow(MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(homePlace)))

        composeRule.setMemoDetailScreenWithTag(
            placeUiState = placeUiState,
            placePagingData = placePagingData(listOf(homePlace)),
        )
        composeRule.performPlaceSelectClick()
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    private fun ComposeContentTestRule.setMemoDetailScreenWithPlace(placeViewModel: MemoPlaceViewModel) {
        setContent { MemoDetailScreenWithPlace(placeViewModel = placeViewModel) }
    }

    @Composable
    private fun MemoDetailScreenWithPlace(placeViewModel: MemoPlaceViewModel) {
        MemoDetailScreenTestTheme {
            MemoDetailScreen(
                tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                detailViewModel = screenTestViewModel(uiState = MutableStateFlow(memoDetailUiState(detail = memoDetail(MEMO_TITLE)))),
                tagViewModel = screenTestTagViewModel(),
                webViewModel = screenTestWebViewModel(),
                contactViewModel = screenTestContactViewModel(),
                placeViewModel = placeViewModel,
                placeMapViewModel = screenTestPlaceMapViewModel(),
                geminiViewModel = screenTestGeminiViewModel(),
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
                componentVisibleProvider = { MemoDetailScaffoldComponentVisible() },
                isStandalone = true,
            )
        }
    }

    /**
     * 장소 추가 칩은 칩 영역의 자체 스크롤 안에 있어 본문 스크롤로 표시 영역까지 옮길 수 없으므로 클릭 동작을 직접 실행한다.
     */
    private fun ComposeContentTestRule.performPlaceSelectClick() {
        onNodeWithText(DEFAULT_PLACE_SELECT_LABEL).performSemanticsAction(SemanticsActions.OnClick)
        waitForIdle()
    }

    private companion object {
        val SECOND_PLACE_MEMO_ID: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000021")
    }
}
