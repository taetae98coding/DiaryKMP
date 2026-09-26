package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.feature.memo.ui.closeDialogByBack
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoPlacePickerDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `목록의 장소가 선택 여부와 함께 표시된다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val officePlace = testPlace(title = OFFICE_PLACE_TITLE)
        composeRule.setMemoPlacePickerDialog(
            placeList = listOf(homePlace, officePlace),
            uiState = MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(homePlace)),
        )

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_TITLE).assertExists()
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertIsOn()
        composeRule.placeDialogNodeWithText(OFFICE_PLACE_TITLE).assertIsOff()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 장소 선택 목록의 문구를 표시한다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        composeRule.setMemoPlacePickerDialog(placeList = listOf(homePlace))

        composeRule.placeDialogNodeWithText(KOREAN_PLACE_PICKER_TITLE).assertExists()
        composeRule.placeDialogNodeWithText(KOREAN_PLACE_PICKER_PLACE_ADD).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-DATA-002 목록 조회에 실패해도 이미 표시된 장소를 유지한다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        composeRule.setMemoPlacePickerDialog(
            placePagingData = MutableStateFlow(appendFailedPlacePagingDataOf(listOf(homePlace))),
        )

        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertIsOff()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-039 다음 장소를 불러오는 동안에도 이미 나타난 장소를 조작할 수 있다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val selectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoPlacePickerDialog(
            placePagingData = MutableStateFlow(appendingPlacePagingDataOf(listOf(homePlace))),
            onPlaceSelect = selectedIdList::add,
        )

        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        selectedIdList shouldBe listOf(homePlace.id)
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-024 나타낼 장소가 없어도 장소 추가 항목을 표시한다`() {
        composeRule.setMemoPlacePickerDialog(placeList = emptyList())

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-024 나타낼 장소가 여러 개여도 장소 추가 항목을 표시한다`() {
        val placeList = listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE))
        composeRule.setMemoPlacePickerDialog(placeList = placeList)

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-025 장소 추가 항목을 누르면 장소 추가 행동만 전달한다`() {
        val placeList = listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE))
        var placeAddCount = 0
        val selectedIdList = mutableListOf<Uuid>()
        val unselectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoPlacePickerDialog(
            placeList = placeList,
            onPlaceSelect = selectedIdList::add,
            onPlaceUnselect = unselectedIdList::add,
            onPlaceAdd = { placeAddCount += 1 },
        )

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).performClick()
        composeRule.waitForIdle()

        placeAddCount shouldBe 1
        selectedIdList shouldBe emptyList()
        unselectedIdList shouldBe emptyList()
    }

    @Test
    fun `장소 추가 항목에 장소 추가 동작 이름을 제공한다`() {
        composeRule.setMemoPlacePickerDialog(placeList = listOf(testPlace(title = HOME_PLACE_TITLE)))

        composeRule
            .onNode(hasClickLabel(DEFAULT_PLACE_PICKER_PLACE_ADD) and hasAnyAncestor(isDialog()))
            .assertExists()
    }

    @Test
    fun `목록에 확인 버튼을 두지 않는다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        composeRule.setMemoPlacePickerDialog(
            placeList = listOf(homePlace),
            uiState = MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(homePlace)),
        )

        composeRule.onNodeWithText(DEFAULT_PLACE_PICKER_CONFIRM).assertDoesNotExist()
    }

    @Test
    fun `뒤로가기로 닫으면 닫기 요청을 전달하고 선택 행동은 전달하지 않는다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        var dismissCount = 0
        val selectedIdList = mutableListOf<Uuid>()
        val unselectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoPlacePickerDialog(
            placeList = listOf(homePlace),
            uiState = MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = listOf(homePlace)),
            onDismissRequest = { dismissCount += 1 },
            onPlaceSelect = selectedIdList::add,
            onPlaceUnselect = unselectedIdList::add,
        )

        composeRule.closeDialogByBack()

        dismissCount shouldBe 1
        selectedIdList shouldBe emptyList()
        unselectedIdList shouldBe emptyList()
    }
}
