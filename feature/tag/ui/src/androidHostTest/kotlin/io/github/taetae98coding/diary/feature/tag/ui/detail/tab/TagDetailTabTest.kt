package io.github.taetae98coding.diary.feature.tag.ui.detail.tab

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DELETE_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_FINISH_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_PLACE_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_UPDATE_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.DEFAULT_WEB_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.EDIT_SUFFIX
import io.github.taetae98coding.diary.feature.tag.ui.detail.FIRST_TAG_ID
import io.github.taetae98coding.diary.feature.tag.ui.detail.KOREAN_DETAIL_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.KOREAN_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.KOREAN_PLACE_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.KOREAN_WEB_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_DETAIL_PAGER_TEST_TAG
import io.github.taetae98coding.diary.feature.tag.ui.detail.TAG_TITLE
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScaffold
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailScreenTestHost
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailTestTabContent
import io.github.taetae98coding.diary.feature.tag.ui.detail.TagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.memo.TAG_DETAIL_MEMO_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TAG_DETAIL_PLACE_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.tag.ui.detail.prepareTagDetailTabViewModels
import io.github.taetae98coding.diary.feature.tag.ui.detail.screenTestViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.selectTagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.setTagDetailScreen
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetail
import io.github.taetae98coding.diary.feature.tag.ui.detail.tagDetailUiState
import io.github.taetae98coding.diary.feature.tag.ui.detail.titleInput
import io.github.taetae98coding.diary.feature.tag.ui.detail.web.TAG_DETAIL_WEB_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.tag.ui.fixtureId
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagDetailFormState
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagPlace
import io.github.taetae98coding.diary.feature.tag.ui.tagWeb
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.viewmodel.koinViewModel
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-FEATURE-037 화면에 처음 진입하면 태그 디테일 탭이 선택된다`() {
        setTagDetailScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.titleInput().assertExists()
        composeRule.onNodeWithTag(TAG_DETAIL_MEMO_LIST_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithTag(TAG_DETAIL_WEB_LIST_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithTag(TAG_DETAIL_PLACE_LIST_TEST_TAG).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-DETAIL-FEATURE-037 한국어 환경 탭 접근성 이름을 표시한다`() {
        setTagDetailScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_DETAIL_TAB_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(KOREAN_MEMO_TAB_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(KOREAN_WEB_TAB_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(KOREAN_PLACE_TAB_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-038 메모 탭을 선택하면 메모 목록을 표시한다`() {
        setTagDetailScaffold()

        selectTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithTag(TAG_DETAIL_MEMO_LIST_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-038 웹 탭을 선택하면 웹 목록을 표시한다`() {
        setTagDetailScaffold()

        selectTab(DEFAULT_WEB_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithTag(TAG_DETAIL_WEB_LIST_TEST_TAG).assertExists()
        composeRule.onNodeWithTag(TAG_DETAIL_MEMO_LIST_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-038 장소 탭을 선택하면 장소 목록을 표시한다`() {
        setTagDetailScaffold()

        selectTab(DEFAULT_PLACE_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_PLACE_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithTag(TAG_DETAIL_PLACE_LIST_TEST_TAG).assertExists()
        composeRule.onNodeWithTag(TAG_DETAIL_WEB_LIST_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-038 메모 탭에서 장소 탭으로 전환하면 장소 목록을 표시한다`() {
        setTagDetailScaffold()
        selectTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        selectTab(DEFAULT_PLACE_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_PLACE_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithTag(TAG_DETAIL_PLACE_LIST_TEST_TAG).assertExists()
        composeRule.onNodeWithTag(TAG_DETAIL_MEMO_LIST_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-038 태그 디테일 탭을 다시 선택하면 태그 입력을 표시한다`() {
        setTagDetailScaffold()
        selectTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        selectTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithTag(TAG_DETAIL_MEMO_LIST_TEST_TAG).assertDoesNotExist()
        composeRule.titleInput().assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-038 장소 탭에서 태그 디테일 탭으로 전환하면 태그 입력을 표시한다`() {
        setTagDetailScaffold()
        selectTab(DEFAULT_PLACE_TAB_DESCRIPTION)

        selectTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithTag(TAG_DETAIL_PLACE_LIST_TEST_TAG).assertDoesNotExist()
        composeRule.titleInput().assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-039 본문을 왼쪽으로 밀면 다음 탭으로 한 칸씩 전환된다`() {
        setTagDetailScaffold()

        swipePager { swipeLeft() }
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()

        swipePager { swipeLeft() }
        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsSelected()

        swipePager { swipeLeft() }
        composeRule.onNodeWithContentDescription(DEFAULT_PLACE_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-039 본문을 오른쪽으로 밀면 이전 탭으로 한 칸씩 전환된다`() {
        setTagDetailScaffold()
        selectTab(DEFAULT_PLACE_TAB_DESCRIPTION)

        swipePager { swipeRight() }
        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsSelected()

        swipePager { swipeRight() }
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()

        swipePager { swipeRight() }
        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-040 조회 중에도 탭을 전환할 수 있다`() {
        setTagDetailScaffold(uiStateProvider = { TagDetailUiState.Loading })

        selectTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithTag(TAG_DETAIL_MEMO_LIST_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-047 조회 중에도 웹 탭을 전환할 수 있다`() {
        setTagDetailScaffold(uiStateProvider = { TagDetailUiState.Loading })

        selectTab(DEFAULT_WEB_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_WEB_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithTag(TAG_DETAIL_WEB_LIST_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-047 조회 중에도 장소 탭을 전환할 수 있다`() {
        setTagDetailScaffold(uiStateProvider = { TagDetailUiState.Loading })

        selectTab(DEFAULT_PLACE_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_PLACE_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithTag(TAG_DETAIL_PLACE_LIST_TEST_TAG).assertExists()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-041 탭을 전환해도 수정 중이던 내용이 유지된다`() {
        setTagDetailScaffold(uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_TITLE)) })
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)

        listOf(
            DEFAULT_MEMO_TAB_DESCRIPTION,
            DEFAULT_WEB_TAB_DESCRIPTION,
            DEFAULT_PLACE_TAB_DESCRIPTION,
        ).forEach { tabDescription ->
            selectTab(tabDescription)
            selectTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

            composeRule.titleInput().assert(hasText(TAG_TITLE + EDIT_SUFFIX))
        }
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-043 완료와 삭제는 선택한 탭과 관계없이 제공된다`() {
        setTagDetailScaffold(uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_TITLE)) })

        listOf(
            DEFAULT_DETAIL_TAB_DESCRIPTION,
            DEFAULT_MEMO_TAB_DESCRIPTION,
            DEFAULT_WEB_TAB_DESCRIPTION,
            DEFAULT_PLACE_TAB_DESCRIPTION,
        ).forEach { tabDescription ->
            selectTab(tabDescription)

            composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assert(hasClickAction())
            composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
        }
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-060 완료된 태그의 TagDetail 화면에서도 네 탭을 모두 사용할 수 있다`() {
        setTagDetailScaffold(uiStateProvider = { tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinished = true) })

        mapOf(
            DEFAULT_MEMO_TAB_DESCRIPTION to TAG_DETAIL_MEMO_LIST_TEST_TAG,
            DEFAULT_WEB_TAB_DESCRIPTION to TAG_DETAIL_WEB_LIST_TEST_TAG,
            DEFAULT_PLACE_TAB_DESCRIPTION to TAG_DETAIL_PLACE_LIST_TEST_TAG,
        ).forEach { (tabDescription, listTestTag) ->
            selectTab(tabDescription)

            composeRule.onNodeWithContentDescription(tabDescription).assertIsSelected()
            composeRule.onNodeWithTag(listTestTag).assertExists()
        }

        selectTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.titleInput().assertExists()
    }

    private fun selectTab(contentDescription: String) {
        composeRule.onNodeWithContentDescription(contentDescription).performClick()
        composeRule.waitForIdle()
    }

    private fun swipePager(block: androidx.compose.ui.test.TouchInjectionScope.() -> Unit) {
        composeRule.onNodeWithTag(TAG_DETAIL_PAGER_TEST_TAG).performTouchInput(block)
        composeRule.waitForIdle()
    }

    private fun setTagDetailScaffold(uiStateProvider: () -> TagDetailUiState = { tagDetailUiState() }) {
        val state = mutableStateOf(uiStateProvider())

        composeRule.setContent {
            val formState = rememberTagDetailFormState(initialDetail = (state.value as? TagDetailUiState.Content)?.detail ?: TagDetail.EMPTY)
            val webPagingItems =
                remember { MutableStateFlow(tagEntityPagingData(itemList = listOf(tagWeb(title = WEB_TITLE)))) }
                    .collectAsLazyPagingItems()
            val placePagingItems =
                remember { MutableStateFlow(tagEntityPagingData(itemList = listOf(tagPlace(title = PLACE_TITLE)))) }
                    .collectAsLazyPagingItems()

            DiaryTheme {
                TagDetailScaffold(
                    onEvent = {},
                    uiStateProvider = { state.value },
                    state = formState,
                    tabFloatingActionButton = {},
                ) { tab ->
                    TagDetailTestTabContent(
                        tab = tab,
                        uiStateProvider = { state.value },
                        state = formState,
                        webPagingItems = webPagingItems,
                        placePagingItems = placePagingItems,
                    )
                }
            }
        }
    }

    private companion object {
        const val WEB_TITLE = "TagDetailTabWeb"
        const val PLACE_TITLE = "TagDetailTabPlace"
    }
}

// 떠 있는 버튼과 탭 유지는 화면이 배선하므로, 실제 배선을 거치는 화면 단위로 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailTabScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-FEATURE-042 수정 반영 동작은 메모 탭에서 제공되지 않는다`() {
        setTagDetailScreen()
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertExists()

        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-042 수정 반영 동작은 웹 탭과 장소 탭에서 제공되지 않는다`() {
        setTagDetailScreen()
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)

        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_WEB_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())

        composeRule.selectTagDetailTab(DEFAULT_PLACE_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_PLACE_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())

        composeRule.selectTagDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-042 태그 디테일 탭으로 돌아오면 수정 반영 동작이 다시 제공된다`() {
        setTagDetailScreen()
        composeRule.titleInput().performTextInput(EDIT_SUFFIX)
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.selectTagDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-044 화면 재생성 후에도 선택한 메모 탭이 유지된다`() {
        assertSelectedTabIsRestored(tabDescription = DEFAULT_MEMO_TAB_DESCRIPTION)
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-044 화면 재생성 후에도 선택한 웹 탭이 유지된다`() {
        assertSelectedTabIsRestored(tabDescription = DEFAULT_WEB_TAB_DESCRIPTION)
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-044 화면 재생성 후에도 선택한 장소 탭이 유지된다`() {
        assertSelectedTabIsRestored(tabDescription = DEFAULT_PLACE_TAB_DESCRIPTION)
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-046 메모 탭에서 뒤로가도 진입하기 전 화면으로 돌아간다`() {
        assertNavigateUpKeepsTab(tabDescription = DEFAULT_MEMO_TAB_DESCRIPTION)
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-046 웹 탭에서 뒤로가도 진입하기 전 화면으로 돌아간다`() {
        assertNavigateUpKeepsTab(tabDescription = DEFAULT_WEB_TAB_DESCRIPTION)
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-046 장소 탭에서 뒤로가도 진입하기 전 화면으로 돌아간다`() {
        assertNavigateUpKeepsTab(tabDescription = DEFAULT_PLACE_TAB_DESCRIPTION)
    }

    private fun assertSelectedTabIsRestored(tabDescription: String) {
        val restorationTester = StateRestorationTester(composeRule)
        prepareTagDetailTabViewModels()
        restorationTester.setContent {
            TagDetailScreenTestHost {
                TagDetailScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    navigateToDetail = {},
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    navigateToMemoFinishedList = {},
                    id = FIRST_TAG_ID,
                    componentVisibleProvider = { TagDetailScaffoldComponentVisible() },
                    detailViewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE)))),
                    placeMapViewModel = koinViewModel(),
                    navigateToWebAdd = {},
                    navigateToWebDetail = {},
                    navigateToPlaceAdd = {},
                    navigateToPlaceDetail = {},
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.selectTagDetailTab(tabDescription)
        composeRule.onNodeWithContentDescription(tabDescription).assertIsSelected()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(tabDescription).assertIsSelected()
    }

    @Test
    fun `TC-TAG-DETAIL-FEATURE-057 목록에서 다른 태그를 선택하면 태그 디테일 탭에서 다시 시작한다`() {
        val detailIdState = mutableStateOf(FIRST_TAG_ID)
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE)))),
            detailIdState = detailIdState,
            viewModelFor = { id -> screenTestViewModel(MutableStateFlow(tagDetailUiState(id = id, detail = tagDetail(TAG_TITLE)))) },
        )
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()

        composeRule.runOnIdle { detailIdState.value = fixtureId() }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_DETAIL_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `목록 탭 사이를 옮기면 추가 버튼은 그대로 남고 접근성 이름만 바뀐다`() {
        setTagDetailScreen()
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())

        composeRule.selectTagDetailTab(DEFAULT_WEB_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_WEB_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()

        composeRule.selectTagDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_WEB_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    private fun assertNavigateUpKeepsTab(tabDescription: String) {
        var navigateUpCount = 0
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE)))),
            navigateUp = { navigateUpCount += 1 },
        )
        composeRule.selectTagDetailTab(tabDescription)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
        composeRule.onNodeWithContentDescription(tabDescription).assertIsSelected()
    }

    private fun setTagDetailScreen() {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE)))),
        )
    }

    private companion object {
        const val DEFAULT_MEMO_ADD_BUTTON_DESCRIPTION = "Add memo"
        const val DEFAULT_WEB_ADD_BUTTON_DESCRIPTION = "Add web"
        const val DEFAULT_PLACE_ADD_BUTTON_DESCRIPTION = "Add place"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
    }
}
