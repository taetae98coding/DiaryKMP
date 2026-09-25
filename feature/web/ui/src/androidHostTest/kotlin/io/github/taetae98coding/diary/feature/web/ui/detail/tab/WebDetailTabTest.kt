package io.github.taetae98coding.diary.feature.web.ui.detail.tab

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.web.ui.add.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.DEFAULT_DELETE_DESCRIPTION
import io.github.taetae98coding.diary.feature.web.ui.detail.DEFAULT_FORM_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.web.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.web.ui.detail.DEFAULT_NAVIGATE_UP_DESCRIPTION
import io.github.taetae98coding.diary.feature.web.ui.detail.DEFAULT_OPEN_IN_NEW_DESCRIPTION
import io.github.taetae98coding.diary.feature.web.ui.detail.DEFAULT_PAGE_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.web.ui.detail.DEFAULT_UPDATE_DESCRIPTION
import io.github.taetae98coding.diary.feature.web.ui.detail.DEFAULT_VIEW_MODE_DESCRIPTION
import io.github.taetae98coding.diary.feature.web.ui.detail.FIRST_WEB_ID
import io.github.taetae98coding.diary.feature.web.ui.detail.KOREAN_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.web.ui.detail.WEB_DETAIL_FORM_TEST_TAG
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailScreen
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailScreenTestTheme
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailUiState
import io.github.taetae98coding.diary.feature.web.ui.detail.memoScreenPageViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.memoScreenWebViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.prepareWebDetailTabViewModels
import io.github.taetae98coding.diary.feature.web.ui.detail.selectWebDetailTab
import io.github.taetae98coding.diary.feature.web.ui.detail.setWebDetailMemoScreen
import io.github.taetae98coding.diary.feature.web.ui.detail.testContentUiState
import io.github.taetae98coding.diary.feature.web.ui.detail.testWebDetail
import io.github.taetae98coding.diary.feature.web.ui.detail.titleInput
import io.github.taetae98coding.diary.feature.web.ui.detail.webMemo
import io.github.taetae98coding.diary.feature.web.ui.detail.webMemoPagingData
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class WebDetailTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-DETAIL-FEATURE-056 화면에 처음 진입하면 메모 탭은 선택되어 있지 않다`() {
        setScreen()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsNotSelected()
        composeRule.onNodeWithContentDescription(DEFAULT_PAGE_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithText(MEMO_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-WEB-DETAIL-FEATURE-056 한국어 환경 메모 탭 접근성 이름을 표시한다`() {
        setScreen()

        composeRule.onNodeWithContentDescription(KOREAN_MEMO_TAB_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-057 메모 탭을 선택하면 연결된 메모 목록을 표시하고 수정 폼 탭으로 돌아올 수 있다`() {
        setScreen()

        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        waitUntilMemoListExists()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithTag(WEB_DETAIL_FORM_TEST_TAG).assertDoesNotExist()

        composeRule.selectWebDetailTab(DEFAULT_FORM_TAB_DESCRIPTION)

        composeRule.onNodeWithTag(WEB_DETAIL_FORM_TEST_TAG).assertExists()
        composeRule.onNodeWithText(MEMO_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-WEB-DETAIL-FEATURE-057 넓은 창에서 메모 탭을 선택하면 웹 페이지 영역은 그대로 두고 시작 쪽 영역만 메모로 바뀐다`() {
        setScreen()

        composeRule.onNodeWithTag(WEB_DETAIL_FORM_TEST_TAG).assertExists()
        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        waitUntilMemoListExists()

        composeRule.onNodeWithTag(WEB_DETAIL_FORM_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_VIEW_MODE_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PAGE_TAB_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-058 조회 중에도 메모 탭을 선택할 수 있다`() {
        setScreen(uiState = WebDetailUiState.Loading)

        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        waitUntilMemoListExists()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-059 메모 탭에 다녀와도 수정 중이던 내용이 유지된다`() {
        setScreen()
        composeRule.selectWebDetailTab(DEFAULT_FORM_TAB_DESCRIPTION)
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()

        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.selectWebDetailTab(DEFAULT_FORM_TAB_DESCRIPTION)

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-060 외부로 열기와 삭제는 메모 탭에서도 제공된다`() {
        setScreen()
        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-062 메모 탭에서 뒤로가도 진입하기 전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setScreen(navigateUp = { navigateUpCount += 1 })
        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-063 수정 반영 동작은 메모 탭에서 제공되지 않는다`() {
        setScreen()
        composeRule.selectWebDetailTab(DEFAULT_FORM_TAB_DESCRIPTION)
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assert(hasClickAction())

        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertDoesNotExist()

        composeRule.selectWebDetailTab(DEFAULT_FORM_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-046 삭제 상태인 웹 항목에서도 메모 탭을 사용할 수 있다`() {
        // 삭제 상태인 웹 항목도 조회 결과로 표시되므로 내용 표시 상태로 관찰된다.
        setScreen()

        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        waitUntilMemoListExists()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-045 화면을 떠났다 다시 들어오면 메모 탭이 선택되지 않은 상태로 시작한다`() {
        var isShown by mutableStateOf(true)
        prepareWebDetailTabViewModels()
        composeRule.setContent {
            WebDetailScreenTestTheme {
                if (isShown) {
                    WebDetailScreen(
                        navigateUp = {},
                        navigateToTagAdd = {},
                        navigateToTagDetail = {},
                        navigateToMemoAdd = {},
                        navigateToMemoDetail = {},
                        id = FIRST_WEB_ID,
                        tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                        webViewModel = memoScreenWebViewModel(uiState = MutableStateFlow(testContentUiState(detail = testWebDetail(title = WEB_TITLE)))),
                        pageViewModel = memoScreenPageViewModel(),
                        tagViewModel = detailTagScreenTestViewModel(),
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()

        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsNotSelected()
    }

    private fun waitUntilMemoListExists() {
        composeRule.waitUntil(timeoutMillis = PAGE_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(MEMO_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun setScreen(
        uiState: WebDetailUiState = testContentUiState(detail = testWebDetail(title = WEB_TITLE)),
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setWebDetailMemoScreen(
            viewModel = memoScreenWebViewModel(uiState = MutableStateFlow(uiState)),
            memoPagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = webMemo(title = MEMO_TITLE)))),
            navigateUp = navigateUp,
        )
    }

    private companion object {
        const val WEB_TITLE = "WebDetailTabTitle"
        const val TYPED_TITLE = "WebDetailTabTypedTitle"
        const val MEMO_TITLE = "WebDetailTabMemo"
        const val PAGE_TIMEOUT_MILLIS = 5_000L
    }
}
