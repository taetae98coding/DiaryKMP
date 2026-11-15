package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.compose.core.share.ShareResult
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.web.ui.add.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageUiState
import io.github.taetae98coding.diary.feature.web.ui.resetAndroidUiDispatcher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class WebDetailShareTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
        prepareWebDetailTabViewModels()
    }

    @Test
    fun `TC-54f52242-03ce-4491-b3ad-73d33c58f730 입력 중인 제목과 URL을 글로 담아 공유한다`() {
        val detail = testWebDetail(headerList = listOf(WebHeader(name = SAVED_HEADER_NAME, value = SAVED_HEADER_VALUE)))
        val contentSharer = webDetailContentSharer()

        setWebDetailScreen(uiState = testContentUiState(detail = detail), contentSharer = contentSharer)
        composeRule.selectFormTab()
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.descriptionInput().performTextReplacement(TYPED_DESCRIPTION)
        composeRule.urlInput().performTextReplacement(TYPED_URL)
        composeRule.headerValueInput().performTextReplacement(TYPED_HEADER_VALUE)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { contentSharer.share(content = ShareContent.Text(text = "$TYPED_TITLE\n$TYPED_URL", title = TYPED_TITLE)) }
    }

    @Test
    fun `TC-3562383e-38cd-46eb-ad67-dec9328a9c5e 조회 중에는 공유를 실행할 수 없다`() {
        setWebDetailScreen(uiState = WebDetailUiState.Loading, contentSharer = webDetailContentSharer())

        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-72c60f2e-37d1-494a-a211-31e671039f4d 공유를 제공하지 않으면 공유 버튼이 없다`() {
        setWebDetailScreen(contentSharer = webDetailContentSharer(isTextSupported = false))

        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-c8819c7a-b231-4887-af93-2401f98ff217 공유 시트를 열지 못하면 화면을 유지하고 알린다`() {
        val detail = testWebDetail()

        setWebDetailScreen(uiState = testContentUiState(detail = detail), contentSharer = webDetailContentSharer(result = ShareResult.FAILED))
        composeRule.selectFormTab()
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_SHARE_FAILED_MESSAGE).assertExists()
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.urlInput().assert(hasText(detail.url))
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-c1ee70e8-66f1-4fd2-b13b-11b42b1d22b7 복사로 대신하면 화면을 유지하고 복사했다고 알린다`() {
        val detail = testWebDetail()

        setWebDetailScreen(uiState = testContentUiState(detail = detail), contentSharer = webDetailContentSharer(result = ShareResult.COPIED))
        composeRule.selectFormTab()
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_SHARE_COPIED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_SHARE_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.urlInput().assert(hasText(detail.url))
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-a427c325-0fcc-4585-b222-4c9c602ba43a 공유를 마치면 아무것도 안내하지 않는다`() {
        assertNoFeedbackAfterShare(result = ShareResult.COMPLETED)
    }

    @Test
    fun `TC-a427c325-0fcc-4585-b222-4c9c602ba43a 공유 시트를 닫으면 아무것도 안내하지 않는다`() {
        assertNoFeedbackAfterShare(result = ShareResult.DISMISSED)
    }

    @Test
    fun `TC-8416d17e-852f-40cc-97bf-92621c2523d0 공유해도 입력과 탭이 그대로이고 수정하지 않는다`() {
        val webViewModel = webViewModel(uiState = testContentUiState())

        setWebDetailScreen(webViewModel = webViewModel)
        composeRule.selectFormTab()
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.urlInput().performTextReplacement(TYPED_URL)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_FORM_TAB_DESCRIPTION).assertIsSelected()
        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.urlInput().assert(hasText(TYPED_URL))
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).assertExists()
        verify(exactly = 0) { webViewModel.update(detail = any()) }
    }

    @Test
    fun `TC-2902543f-42fd-494a-88c3-f06499de91cf 메모 탭에서도 공유할 수 있다`() {
        val detail = testWebDetail()
        val contentSharer = webDetailContentSharer()

        setWebDetailScreen(uiState = testContentUiState(detail = detail), contentSharer = contentSharer)
        composeRule.selectWebDetailTab(contentDescription = DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { contentSharer.share(content = ShareContent.Text(text = "${detail.title}\n${detail.url}", title = detail.title)) }
    }

    @Test
    fun `TC-89538bd7-494c-4ffd-95d4-e545b3a7fe96 삭제를 처리하는 동안에도 공유할 수 있다`() {
        val detail = testWebDetail()
        val contentSharer = webDetailContentSharer()

        setWebDetailScreen(uiState = testContentUiState(detail = detail).copy(isDeleteInProgress = true), contentSharer = contentSharer)
        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { contentSharer.share(content = ShareContent.Text(text = "${detail.title}\n${detail.url}", title = detail.title)) }
    }

    @Test
    fun `TC-d0c48546-8cba-4d1a-aab4-9f1af410bcc3 공유 시트가 열린 동안 재생성되어도 공유를 다시 시작하지 않는다`() {
        val contentSharer = webDetailContentSharer()
        coEvery { contentSharer.share(any()) } coAnswers { awaitCancellation() }
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent { WebDetailScreenUnderTest(webViewModel = webViewModel(uiState = testContentUiState()), contentSharer = contentSharer) }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        coVerify(exactly = 1) { contentSharer.share(content = any()) }

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { contentSharer.share(content = any()) }
    }

    private fun assertNoFeedbackAfterShare(result: ShareResult) {
        val contentSharer = webDetailContentSharer(result = result)

        setWebDetailScreen(contentSharer = contentSharer)
        composeRule.onNodeWithContentDescription(DEFAULT_SHARE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { contentSharer.share(content = any()) }
        composeRule.onNodeWithText(DEFAULT_SHARE_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PAGE_DESCRIPTION).assertExists()
    }

    private fun setWebDetailScreen(
        uiState: WebDetailUiState = testContentUiState(),
        webViewModel: WebDetailViewModel = webViewModel(uiState = uiState),
        contentSharer: ContentSharer = webDetailContentSharer(),
    ) {
        composeRule.setContent { WebDetailScreenUnderTest(webViewModel = webViewModel, contentSharer = contentSharer) }
        composeRule.waitForIdle()
    }

    @Composable
    private fun WebDetailScreenUnderTest(
        webViewModel: WebDetailViewModel,
        contentSharer: ContentSharer,
    ) {
        WebDetailScreenTestTheme {
            WebDetailScreen(
                navigateUp = {},
                navigateToTagAdd = {},
                navigateToTagDetail = {},
                navigateToMemoAdd = {},
                navigateToMemoDetail = {},
                navigateToMemoFinishedList = {},
                id = FIRST_WEB_ID,
                tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                webViewModel = webViewModel,
                pageViewModel = memoScreenPageViewModel(pageUiState = WebDetailPageUiState.Loading),
                tagViewModel = detailTagScreenTestViewModel(),
                contentSharerFactory = contentSharer.asFactory(),
            )
        }
    }

    private companion object {
        private const val TYPED_TITLE = "WebDetailShareTitle"
        private const val TYPED_DESCRIPTION = "WebDetailShareDescription"
        private const val TYPED_URL = "https://share.example.com"
        private const val SAVED_HEADER_NAME = "X-Saved-Header"
        private const val SAVED_HEADER_VALUE = "WebDetailSavedHeaderValue"
        private const val TYPED_HEADER_VALUE = "WebDetailTypedHeaderValue"

        private fun webViewModel(uiState: WebDetailUiState): WebDetailViewModel {
            val viewModel = mockk<WebDetailViewModel>()
            every { viewModel.uiState } returns MutableStateFlow(uiState)
            every { viewModel.effect } returns emptyFlow()
            justRun { viewModel.update(any()) }
            justRun { viewModel.delete() }

            return viewModel
        }
    }
}
