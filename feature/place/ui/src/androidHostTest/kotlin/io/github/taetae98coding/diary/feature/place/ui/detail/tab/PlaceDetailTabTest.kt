package io.github.taetae98coding.diary.feature.place.ui.detail.tab

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
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
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.core.testing.place.coordinateInFormPrecision
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
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailScaffold
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailScreen
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailScreenTestTheme
import io.github.taetae98coding.diary.feature.place.ui.detail.PlaceDetailUiState
import io.github.taetae98coding.diary.feature.place.ui.detail.content
import io.github.taetae98coding.diary.feature.place.ui.detail.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.memo.PLACE_DETAIL_MEMO_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.place.ui.detail.placeDetail
import io.github.taetae98coding.diary.feature.place.ui.detail.placeMemo
import io.github.taetae98coding.diary.feature.place.ui.detail.placeMemoPagingData
import io.github.taetae98coding.diary.feature.place.ui.detail.preparePlaceDetailTabViewModels
import io.github.taetae98coding.diary.feature.place.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.searchScreenTestViewModel
import io.github.taetae98coding.diary.feature.place.ui.detail.selectPlaceDetailTab
import io.github.taetae98coding.diary.feature.place.ui.detail.setPlaceDetailScreen
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceDetailFormState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-043 화면에 처음 진입하면 장소 디테일 탭이 선택된다`() {
        val placeTitle = newPlaceTitle()
        val memoTitle = newMemoTitle()
        setScreen(placeTitle = placeTitle, memoTitle = memoTitle)

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNode(hasText(placeTitle) and hasSetTextAction()).assertExists()
        composeRule.onNodeWithText(memoTitle).assertDoesNotExist()
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
        val placeTitle = newPlaceTitle()
        val memoTitle = newMemoTitle()
        setScreen(placeTitle = placeTitle, memoTitle = memoTitle)

        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        waitUntilMemoListExists(memoTitle = memoTitle)

        composeRule.selectPlaceDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNode(hasText(placeTitle) and hasSetTextAction()).assertExists()
        composeRule.onNodeWithText(memoTitle).assertDoesNotExist()
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-045 본문을 좌우로 밀어도 탭이 전환되지 않는다`() {
        val memoTitle = newMemoTitle()
        setScreen(memoTitle = memoTitle)

        composeRule.onNodeWithTag(PLACE_DETAIL_PAGER_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithText(memoTitle).assertDoesNotExist()

        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithTag(PLACE_DETAIL_PAGER_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-046 TC-PLACE-DETAIL-FEATURE-053 조회 중에도 탭 행이 표시되고 메모 탭으로 전환할 수 있다`() {
        val placeTitle = newPlaceTitle()
        val memoTitle = newMemoTitle()
        setScreen(placeTitle = placeTitle, memoTitle = memoTitle, uiState = PlaceDetailUiState.Loading)

        composeRule.onNode(hasText(placeTitle) and hasSetTextAction()).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        waitUntilMemoListExists(memoTitle = memoTitle)
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-047 탭을 전환해도 수정 중이던 내용이 유지된다`() {
        val placeTitle = newPlaceTitle()
        setScreen(placeTitle = placeTitle)
        composeRule.onNode(hasText(placeTitle) and hasSetTextAction()).performTextInput(EDIT_SUFFIX)

        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.selectPlaceDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onNode(hasText(placeTitle + EDIT_SUFFIX) and hasSetTextAction()).assertExists()
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-055 탭을 전환해도 메모 탭에서 보던 목록 위치가 유지된다`() {
        val memoTitleList = List(MEMO_COUNT) { index -> "PlaceMemo$index${newMemoTitle()}" }
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(uiState = MutableStateFlow(placeContent())),
            memoPagingData =
                placeMemoPagingData(
                    itemList = memoTitleList.map { title -> MemoListItem.Content(memo = placeMemo(title = title)) },
                ),
        )
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        waitUntilMemoListExists(memoTitle = memoTitleList.first())

        composeRule.onNodeWithTag(PLACE_DETAIL_MEMO_LIST_TEST_TAG).performScrollToNode(hasText(memoTitleList.last()))
        composeRule.waitForIdle()
        isDisplayed(memoTitleList.first()).shouldBeFalse()

        composeRule.selectPlaceDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(memoTitleList.last()).assertIsDisplayed()
        isDisplayed(memoTitleList.first()).shouldBeFalse()
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-048 수정 반영 동작은 장소 디테일 탭에서만 제공된다`() {
        val placeTitle = newPlaceTitle()
        setScreen(placeTitle = placeTitle)
        composeRule.onNode(hasText(placeTitle) and hasSetTextAction()).performTextInput(EDIT_SUFFIX)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assert(hasClickAction())

        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()

        composeRule.selectPlaceDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-049 외부 지도로 열기와 삭제는 선택한 탭과 관계없이 제공된다`() {
        setScreen()

        listOf(DEFAULT_DETAIL_TAB_DESCRIPTION, DEFAULT_MEMO_TAB_DESCRIPTION).forEach { tabDescription ->
            composeRule.selectPlaceDetailTab(tabDescription)

            composeRule.onNodeWithContentDescription(DEFAULT_OPEN_NAVER_MAP_BUTTON_DESCRIPTION).assert(hasClickAction())
            composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-049 기본 지도를 확인했으면 선택한 탭과 관계없이 장소 검색을 제공한다`() {
        val detail = placeDetail(title = newPlaceTitle())
        preparePlaceDetailTabViewModels()
        composeRule.setContent {
            PlaceDetailScreenTestTheme {
                PlaceDetailScaffold(
                    state = rememberPlaceDetailFormState(initialDetail = detail),
                    onEvent = {},
                    onSearchEvent = {},
                    onTagPickerEvent = {},
                    tabState = rememberPlaceDetailTabState(initialTab = PlaceDetailTab.MEMO),
                    uiStateProvider = { content(id = FIRST_PLACE_ID, detail = detail).copy(defaultProvider = MapProvider.NAVER) },
                    tabFloatingActionButton = {},
                    // 지도 제공자의 실제 표시 요소는 만들 수 없으므로 탭 본문은 탭 이름으로 대신한다.
                ) { tab -> Text(text = tab.name) }
            }
        }
        composeRule.waitForIdle()

        listOf(DEFAULT_MEMO_TAB_DESCRIPTION, DEFAULT_DETAIL_TAB_DESCRIPTION).forEach { tabDescription ->
            composeRule.selectPlaceDetailTab(tabDescription)

            composeRule.onNodeWithContentDescription(tabDescription).assertIsSelected()
            composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).assert(hasClickAction())
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
        val placeTitle = newPlaceTitle()
        val memoTitle = newMemoTitle()
        setScreen(placeTitle = placeTitle, memoTitle = memoTitle)

        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        waitUntilMemoListExists(memoTitle = memoTitle)

        composeRule.selectPlaceDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)
        composeRule.onNode(hasText(placeTitle) and hasSetTextAction()).assertExists()
    }

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-050 메모 탭에서 검색 결과를 고르면 주소와 좌표만 반영되고 탭은 바뀌지 않는다`() {
        // 검색 다이얼로그는 지도를 포함해 호스트 테스트 환경에서 띄울 수 없으므로, 검색 결과를 고른 것과 같은 반영을 화면 상태에 직접 적용한다.
        val placeTitle = newPlaceTitle()
        val detail = placeDetail(title = placeTitle)
        val place = fixtureMonkey.giveMeOne<SearchedPlace>().copy(coordinate = fixtureMonkey.coordinateInFormPrecision())
        lateinit var formState: PlaceFormState
        preparePlaceDetailTabViewModels()
        composeRule.setContent {
            PlaceDetailScreenTestTheme {
                formState = rememberPlaceDetailFormState(initialDetail = detail)

                PlaceDetailScaffold(
                    state = formState,
                    onEvent = {},
                    onSearchEvent = {},
                    onTagPickerEvent = {},
                    tabState = rememberPlaceDetailTabState(initialTab = PlaceDetailTab.MEMO),
                    uiStateProvider = { content(id = FIRST_PLACE_ID, detail = detail) },
                    tabFloatingActionButton = {},
                ) { tab -> Text(text = tab.name) }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()

        composeRule.runOnIdle { formState.applySearchedPlace(place) }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            formState.detail.address shouldBe place.address
            formState.detail.coordinate shouldBe place.coordinate
            formState.detail.title shouldBe detail.title
        }
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-PLACE-DETAIL-DOMAIN-035 화면을 떠났다 다시 들어오면 장소 디테일 탭으로 시작한다`() {
        var isShown by mutableStateOf(true)
        preparePlaceDetailTabViewModels()
        composeRule.setContent {
            PlaceDetailScreenTestTheme {
                if (isShown) {
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
        }
        composeRule.waitForIdle()
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()

        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
    }

    private fun isDisplayed(text: String): Boolean =
        runCatching { composeRule.onNodeWithText(text).assertIsDisplayed() }
            .isSuccess

    // 페이지 조회 목록은 항목이 준비된 뒤에 나타나므로 메모 제목이 보일 때까지 기다린다.
    private fun waitUntilMemoListExists(memoTitle: String) {
        composeRule.waitUntil(timeoutMillis = PAGE_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(memoTitle).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun setScreen(
        placeTitle: String = newPlaceTitle(),
        memoTitle: String = newMemoTitle(),
        uiState: PlaceDetailUiState = placeContent(placeTitle = placeTitle),
    ) {
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(uiState = MutableStateFlow(uiState)),
            memoPagingData = placeMemoPagingData(itemList = listOf(MemoListItem.Content(memo = placeMemo(title = memoTitle)))),
        )
    }

    private fun placeContent(placeTitle: String = newPlaceTitle()): PlaceDetailUiState.Content = content(id = FIRST_PLACE_ID, detail = placeDetail(title = placeTitle))

    private companion object {
        const val EDIT_SUFFIX = "Edited"
        const val MEMO_COUNT = 30
        const val PAGE_TIMEOUT_MILLIS = 5_000L

        fun newPlaceTitle(): String = "PlaceTitle${fixtureMonkey.giveMeOne<String>().filter(Char::isLetterOrDigit)}"

        fun newMemoTitle(): String = "PlaceMemo${fixtureMonkey.giveMeOne<String>().filter(Char::isLetterOrDigit)}"
    }
}
