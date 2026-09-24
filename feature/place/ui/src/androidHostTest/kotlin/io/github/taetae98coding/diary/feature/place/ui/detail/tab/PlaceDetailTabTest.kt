package io.github.taetae98coding.diary.feature.place.ui.detail.tab

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.place.ui.detail.DEFAULT_DELETE_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.DEFAULT_NAVIGATE_UP_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.DEFAULT_SEARCH_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.DEFAULT_UPDATE_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.FIRST_PLACE_ID
import io.github.taetae98coding.diary.feature.place.ui.detail.KOREAN_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.KOREAN_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.place.ui.detail.PLACE_DETAIL_PAGER_TEST_TAG
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailScreen
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailScreenTestTheme
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailUiState
import io.github.taetae98coding.diary.feature.place.ui.detail.content
import io.github.taetae98coding.diary.feature.place.ui.detail.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.placeDetail
import io.github.taetae98coding.diary.feature.place.ui.detail.placeMemo
import io.github.taetae98coding.diary.feature.place.ui.detail.placeMemoPagingData
import io.github.taetae98coding.diary.feature.place.ui.detail.preparePlaceDetailTabViewModels
import io.github.taetae98coding.diary.feature.place.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.searchScreenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.selectPlaceDetailTab
import io.github.taetae98coding.diary.feature.place.ui.detail.setPlaceDetailScreen
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-043 화면에 처음 진입하면 장소 디테일 탭이 선택된다`() {
        setScreen()

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNode(hasText(PLACE_TITLE) and hasSetTextAction()).assertExists()
        composeRule.onNodeWithText(MEMO_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-PLACE-DETAIL-FEATURE-043 한국어 환경 탭 접근성 이름을 표시한다`() {
        setScreen()

        composeRule.onNodeWithContentDescription(KOREAN_DETAIL_TAB_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(KOREAN_MEMO_TAB_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-044 메모 탭을 선택하면 메모 목록을 표시하고 다시 디테일 탭으로 돌아온다`() {
        setScreen()

        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        waitUntilMemoListExists()

        composeRule.selectPlaceDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNode(hasText(PLACE_TITLE) and hasSetTextAction()).assertExists()
        composeRule.onNodeWithText(MEMO_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-045 본문을 좌우로 밀어도 탭이 전환되지 않는다`() {
        setScreen()

        composeRule.onNodeWithTag(PLACE_DETAIL_PAGER_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithText(MEMO_TITLE).assertDoesNotExist()

        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithTag(PLACE_DETAIL_PAGER_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-046 TC-PLACE-DETAIL-FEATURE-053 조회 중에도 탭 행이 표시되고 메모 탭으로 전환할 수 있다`() {
        setScreen(uiState = PlaceDetailUiState.Loading)

        composeRule.onNode(hasText(PLACE_TITLE) and hasSetTextAction()).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        waitUntilMemoListExists()
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-047 탭을 전환해도 수정 중이던 내용이 유지된다`() {
        setScreen()
        composeRule.onNode(hasText(PLACE_TITLE) and hasSetTextAction()).performTextInput(EDIT_SUFFIX)

        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.selectPlaceDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onNode(hasText(PLACE_TITLE + EDIT_SUFFIX) and hasSetTextAction()).assertExists()
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-048 수정 반영 동작은 장소 디테일 탭에서만 제공된다`() {
        setScreen()
        composeRule.onNode(hasText(PLACE_TITLE) and hasSetTextAction()).performTextInput(EDIT_SUFFIX)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assert(hasClickAction())

        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()

        composeRule.selectPlaceDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-049 외부 지도로 열기 장소 검색과 삭제는 선택한 탭과 관계없이 제공된다`() {
        setScreen()

        listOf(DEFAULT_DETAIL_TAB_DESCRIPTION, DEFAULT_MEMO_TAB_DESCRIPTION).forEach { tabDescription ->
            composeRule.selectPlaceDetailTab(tabDescription)

            composeRule.onNodeWithContentDescription(DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION).assert(hasClickAction())
            composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).assert(hasClickAction())
            composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-051 화면 재생성 후에도 선택한 탭이 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        preparePlaceDetailTabViewModels()

        restorationTester.setContent {
            PlaceDetailScreenTestTheme {
                PlaceDetailScreen(
                    navigateUp = {},
                    navigateToTagAdd = {},
                    navigateToTagDetail = {},
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    id = FIRST_PLACE_ID,
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    detailViewModel = screenTestViewModel(uiState = MutableStateFlow(placeContent())),
                    searchViewModel = searchScreenTestViewModel(),
                    tagViewModel = detailTagScreenTestViewModel(),
                )
            }
        }
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-052 메모 탭에서 뒤로가도 진입하기 전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(uiState = MutableStateFlow(placeContent())),
            navigateUp = { navigateUpCount += 1 },
        )
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-PLACE-DETAIL-DOMAIN-036 삭제 상태인 장소에서도 두 탭을 모두 사용할 수 있다`() {
        // 삭제 상태인 장소도 조회 결과로 표시되므로 내용 표시 상태로 관찰된다.
        setScreen()

        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        waitUntilMemoListExists()

        composeRule.selectPlaceDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)
        composeRule.onNode(hasText(PLACE_TITLE) and hasSetTextAction()).assertExists()
    }

    // 페이지 조회 목록은 항목이 준비된 뒤에 나타나므로 메모 제목이 보일 때까지 기다린다.
    private fun waitUntilMemoListExists() {
        composeRule.waitUntil(timeoutMillis = PAGE_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(MEMO_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun setScreen(uiState: PlaceDetailUiState = placeContent()) {
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(uiState = MutableStateFlow(uiState)),
            memoPagingData = placeMemoPagingData(itemList = listOf(MemoListItem.Content(memo = placeMemo(title = MEMO_TITLE)))),
        )
    }

    private companion object {
        const val PLACE_TITLE = "PlaceDetailTabTitle"
        const val EDIT_SUFFIX = "Edited"
        const val MEMO_TITLE = "PlaceDetailTabMemo"
        const val PAGE_TIMEOUT_MILLIS = 5_000L

        fun placeContent(): PlaceDetailUiState.Content = content(id = FIRST_PLACE_ID, detail = placeDetail(title = PLACE_TITLE))
    }
}
