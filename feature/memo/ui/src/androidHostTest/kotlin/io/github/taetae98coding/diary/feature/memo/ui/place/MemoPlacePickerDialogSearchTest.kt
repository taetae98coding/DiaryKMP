package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
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
class MemoPlacePickerDialogSearchTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-029 목록을 열면 검색어가 비어 있고 대상 장소가 모두 나타난다`() {
        val placeList = listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE))
        val queryList = mutableListOf<String>()
        composeRule.setMemoPlacePickerDialogHost(placeList = placeList, onQueryChange = queryList::add)
        composeRule.waitForIdle()

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_SEARCH_PLACEHOLDER).assertExists()
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertExists()
        composeRule.placeDialogNodeWithText(OFFICE_PLACE_TITLE).assertExists()
        queryList shouldBe listOf("")
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-030 검색어를 입력하면 확정 동작 없이 그 검색어가 즉시 반영된다`() {
        val placeList = listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE))
        val queryList = mutableListOf<String>()
        composeRule.setMemoPlacePickerDialogHost(placeList = placeList, onQueryChange = queryList::add)
        composeRule.waitForIdle()

        composeRule.placeDialogSearchField().performTextInput(HOME_PLACE_QUERY)
        composeRule.waitForIdle()

        queryList.last() shouldBe HOME_PLACE_QUERY
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-031 검색어를 지우면 검색어가 없는 상태가 즉시 반영된다`() {
        val placeList = listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE))
        val queryList = mutableListOf<String>()
        composeRule.setMemoPlacePickerDialogHost(placeList = placeList, onQueryChange = queryList::add)
        composeRule.placeDialogSearchField().performTextInput(HOME_PLACE_QUERY)
        composeRule.waitForIdle()

        composeRule.placeDialogSearchField().performTextClearance()
        composeRule.waitForIdle()

        queryList.last() shouldBe ""
        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_SEARCH_PLACEHOLDER).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-032 검색어로 좁힌 목록에서도 선택과 해제를 전달한다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val selectedIdList = mutableListOf<Uuid>()
        val unselectedIdList = mutableListOf<Uuid>()
        composeRule.setMemoPlacePickerDialog(
            placeList = listOf(homePlace),
            queryState = TextFieldState(initialText = HOME_PLACE_QUERY),
            onPlaceSelect = selectedIdList::add,
            onPlaceUnselect = unselectedIdList::add,
        )

        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).performClick()
        composeRule.waitForIdle()

        selectedIdList shouldBe listOf(homePlace.id)
        unselectedIdList shouldBe emptyList()
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-033 검색어에 맞는 장소가 없으면 결과 없음을 알린다`() {
        composeRule.setMemoPlacePickerDialog(
            placeList = emptyList(),
            queryState = TextFieldState(initialText = HOME_PLACE_QUERY),
        )

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_SEARCH_EMPTY_TITLE).assertExists()
        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_SEARCH_EMPTY_DESCRIPTION).assertExists()
        composeRule.onAllNodes(isToggleable() and hasAnyAncestor(isDialog())).assertCountEquals(0)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 장소 선택 목록의 검색 문구를 표시한다`() {
        composeRule.setMemoPlacePickerDialog(
            placeList = emptyList(),
            queryState = TextFieldState(initialText = HOME_PLACE_QUERY),
        )

        composeRule.placeDialogNodeWithText(KOREAN_PLACE_PICKER_SEARCH_EMPTY_TITLE).assertExists()
        composeRule.placeDialogNodeWithText(KOREAN_PLACE_PICKER_SEARCH_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 장소 검색 입력의 자리 표시 문구를 표시한다`() {
        composeRule.setMemoPlacePickerDialog(placeList = listOf(testPlace(title = HOME_PLACE_TITLE)))

        composeRule.placeDialogNodeWithText(KOREAN_PLACE_PICKER_SEARCH_PLACEHOLDER).assertExists()
    }

    @Test
    fun `검색어가 비어 있으면 나타낼 장소가 없어도 결과 없음을 알리지 않는다`() {
        composeRule.setMemoPlacePickerDialog(placeList = emptyList())

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_SEARCH_EMPTY_TITLE).assertDoesNotExist()
        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_SEARCH_EMPTY_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-034 검색 결과가 없어도 장소 추가 항목으로 이동을 전달한다`() {
        var placeAddCount = 0
        composeRule.setMemoPlacePickerDialog(
            placeList = emptyList(),
            queryState = TextFieldState(initialText = HOME_PLACE_QUERY),
            onPlaceAdd = { placeAddCount += 1 },
        )

        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).assertExists()
        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_PLACE_ADD).performClick()
        composeRule.waitForIdle()

        placeAddCount shouldBe 1
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-035 목록을 닫았다가 다시 열면 검색어가 비어 있다`() {
        val placeList = listOf(testPlace(title = HOME_PLACE_TITLE), testPlace(title = OFFICE_PLACE_TITLE))
        val dialogState = DialogState(isVisible = true)
        val queryList = mutableListOf<String>()
        composeRule.setMemoPlacePickerDialogHost(dialogState = dialogState, placeList = placeList, onQueryChange = queryList::add)
        composeRule.placeDialogSearchField().performTextInput(HOME_PLACE_QUERY)
        composeRule.waitForIdle()

        composeRule.runOnIdle { dialogState.hide() }
        composeRule.waitForIdle()
        composeRule.runOnIdle { dialogState.show() }
        composeRule.waitForIdle()

        queryList.last() shouldBe ""
        composeRule.placeDialogNodeWithText(DEFAULT_PLACE_PICKER_SEARCH_PLACEHOLDER).assertExists()
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertExists()
        composeRule.placeDialogNodeWithText(OFFICE_PLACE_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-DOMAIN-023 화면이 재생성되어도 열려 있는 목록의 검색어가 유지된다`() {
        val homePlace = testPlace(title = HOME_PLACE_TITLE)
        val placePagingDataFlow = MutableStateFlow(placePagingDataOf(listOf(homePlace)))
        val queryList = mutableListOf<String>()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                MemoPlacePickerDialogHost(
                    dialogState = rememberDialogState(initialVisible = true),
                    onEvent = { event -> if (event is MemoPlacePickerEvent.ChangeQuery) queryList += event.query },
                    placePagingItems = placePagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
        composeRule.placeDialogSearchField().performTextInput(HOME_PLACE_QUERY)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        queryList.last() shouldBe HOME_PLACE_QUERY
        composeRule.placeDialogNodeWithText(HOME_PLACE_QUERY).assertExists()
        composeRule.placeDialogNodeWithText(HOME_PLACE_TITLE).assertExists()
    }
}
