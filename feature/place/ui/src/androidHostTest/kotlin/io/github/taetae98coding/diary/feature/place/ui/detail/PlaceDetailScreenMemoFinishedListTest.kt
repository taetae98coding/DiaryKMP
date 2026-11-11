package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.place.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailScreenMemoFinishedListTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-ede97071-e52c-4fd4-a217-5de367225eb2 TC-9e01404b-fd4a-4c29-a173-785dc7b22d51 완료된 메모 확인을 선택하면 완료된 메모 목록 이동을 요청한다`() {
        var navigateCount = 0
        setScreenOnMemoTab(navigateToMemoFinishedList = { navigateCount += 1 })

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertHasClickAction().performClick()

        navigateCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-ede97071-e52c-4fd4-a217-5de367225eb2 한국어 환경 완료된 메모 확인 라벨을 표시한다`() {
        setScreenOnMemoTab(memoTabDescription = KOREAN_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(KOREAN_FINISHED_LIST_LABEL).assertHasClickAction()
    }

    @Test
    fun `TC-1ad3bf57-d0be-4852-8af4-351c45c47c44 연결된 메모가 표시되어 있어도 완료된 메모 확인을 실행할 수 있다`() {
        val memo = placeMemo(title = MEMO_TITLE)
        var navigateCount = 0
        setScreenOnMemoTab(
            memoPagingData = placeMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            navigateToMemoFinishedList = { navigateCount += 1 },
        )
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(memo.detail.title).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        navigateCount shouldBe 1
    }

    @Test
    fun `TC-1ad3bf57-d0be-4852-8af4-351c45c47c44 TC-fa086b84-94ef-4d78-a1b4-2dd04e8a8211 빈 상태와 대상 조회 상태와 무관하게 완료된 메모 확인을 실행할 수 있다`() {
        val uiStateList = listOf(content(id = FIRST_PLACE_ID), PlaceDetailUiState.Loading)
        val uiStateFlow = MutableStateFlow<PlaceDetailUiState>(uiStateList.first())
        var navigateCount = 0
        setScreenOnMemoTab(uiStateFlow = uiStateFlow, navigateToMemoFinishedList = { navigateCount += 1 })

        uiStateList.forEachIndexed { index, uiState ->
            composeRule.runOnIdle { uiStateFlow.value = uiState }
            composeRule.waitForIdle()

            composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

            navigateCount shouldBe index + 1
        }
    }

    @Test
    fun `TC-8ce3f84d-7ff6-471b-92d5-2ae9cbfafb5b 장소 디테일 탭에서는 완료된 메모 확인을 제공하지 않고 메모 탭으로 전환하면 제공한다`() {
        var navigateCount = 0
        val uiStateFlow = MutableStateFlow<PlaceDetailUiState>(content(id = FIRST_PLACE_ID))
        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel(uiStateFlow), navigateToMemoFinishedList = { navigateCount += 1 })
        composeRule.selectPlaceDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertDoesNotExist()

        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        navigateCount shouldBe 1
    }

    // 내비게이션은 뒤에 쌓인 화면을 컴포지션에서 내리고 저장 상태만 보관한다. 완료 메모 목록이 위에 놓였다가 닫히는 것을 같은 방식으로 재현한다.
    // 뒤로가기가 완료 메모 목록과 상세 영역을 함께 닫는 이력은 PlaceMemoFinishedListNavigationTest가 확인한다.
    @Test
    fun `TC-bd6997b9-d2e8-4e97-acbb-84fa17a7d124 TC-5ecf6737-24b8-40d6-a688-4586275ef513 TC-e26ded17-7725-49f3-9a27-08e5ba713edb 닫히면 메모 탭 유지`() {
        var isDetailOnTop by mutableStateOf(true)
        val viewModel = screenTestViewModel(MutableStateFlow(content(id = FIRST_PLACE_ID)))
        val searchViewModel = searchScreenTestViewModel()
        val tagViewModel = detailTagScreenTestViewModel()
        preparePlaceDetailTabViewModels(memoPagingData = placeMemoPagingData(itemList = emptyList()))
        composeRule.setContent {
            CompositionLocalProvider(LocalUriHandler provides mockk<UriHandler>(relaxed = true)) {
                PlaceDetailScreenTestTheme {
                    val saveableStateHolder = rememberSaveableStateHolder()

                    if (isDetailOnTop) {
                        saveableStateHolder.SaveableStateProvider(key = DETAIL_ENTRY_KEY) {
                            PlaceDetailScreen(
                                navigateToTagAdd = {},
                                navigateToMemoAdd = {},
                                navigateToMemoDetail = {},
                                navigateToMemoFinishedList = { isDetailOnTop = false },
                                id = FIRST_PLACE_ID,
                                tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                                detailViewModel = viewModel,
                                searchViewModel = searchViewModel,
                                navigateUp = {},
                                navigateToTagDetail = {},
                                tagViewModel = tagViewModel,
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertDoesNotExist()

        composeRule.runOnIdle { isDetailOnTop = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertExists()
    }

    private fun setScreenOnMemoTab(
        memoPagingData: PagingData<MemoListItem> = placeMemoPagingData(itemList = emptyList()),
        memoTabDescription: String = DEFAULT_MEMO_TAB_DESCRIPTION,
        uiStateFlow: MutableStateFlow<PlaceDetailUiState> = MutableStateFlow(content(id = FIRST_PLACE_ID)),
        navigateToMemoFinishedList: () -> Unit = {},
    ) {
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(uiStateFlow),
            memoPagingData = memoPagingData,
            navigateToMemoFinishedList = navigateToMemoFinishedList,
        )
        composeRule.selectPlaceDetailTab(memoTabDescription)
    }

    private companion object {
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val MEMO_TITLE = "PlaceDetailFinishedEntryMemo"
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished memos"
        const val KOREAN_FINISHED_LIST_LABEL = "완료된 메모"
        const val DETAIL_ENTRY_KEY = "PlaceDetail"
    }
}
