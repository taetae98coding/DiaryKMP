package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.web.DiaryWebSession
import io.github.taetae98coding.diary.compose.web.LocalDiaryWebSession
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.web.ui.add.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageUiState
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageViewModel
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class WebDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-DETAIL-FEATURE-037 URL 방식으로 시작하면 웹 페이지 불러오기를 시작하지 않는다`() {
        val pageViewModel = pageViewModel()

        setWebDetailScreen(pageViewModel = pageViewModel)

        verify(exactly = 0) { pageViewModel.load() }
        composeRule.onNodeWithText(DEFAULT_URL_VIEW_MODE_LABEL).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-004 TC-WEB-DETAIL-FEATURE-040 응답 본문 방식을 고르면 웹 페이지 불러오기를 한 번 시작한다`() {
        val pageViewModel = pageViewModel()

        setWebDetailScreen(pageViewModel = pageViewModel)
        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)

        verify(exactly = 1) { pageViewModel.load() }
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-041 URL 방식으로 되돌려도 웹 페이지를 다시 불러오지 않는다`() {
        val pageViewModel = pageViewModel(pageUiState = WebDetailPageUiState.Content(page = testWebPage()))

        setWebDetailScreen(pageViewModel = pageViewModel)
        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        composeRule.selectViewMode(label = DEFAULT_URL_VIEW_MODE_LABEL)

        composeRule.onNodeWithText(DEFAULT_URL_VIEW_MODE_LABEL).assertExists()
        verify(exactly = 0) { pageViewModel.retry() }
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-009 다시 시도를 선택하면 다시 불러오기를 한 번 실행한다`() {
        val pageViewModel = pageViewModel(pageUiState = WebDetailPageUiState.Failure)

        setWebDetailScreen(pageViewModel = pageViewModel)
        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        composeRule.onNodeWithText(DEFAULT_RETRY_BUTTON).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { pageViewModel.retry() }
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-015 뒤로가기를 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0

        setWebDetailScreen(navigateUp = { navigateUpCount++ })
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-006 불러오기에 성공하면 웹 페이지 영역에 받은 웹 페이지를 표시한다`() {
        setWebDetailScreen(pageViewModel = pageViewModel(pageUiState = WebDetailPageUiState.Content(page = testWebPage())))
        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)

        composeRule.onNodeWithContentDescription(DEFAULT_PAGE_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_RETRY_BUTTON).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-016 외부로 열기를 선택하면 저장된 URL을 앱 밖에서 한 번 연다`() {
        val detail = testWebDetail()
        val uriHandler = mockk<UriHandler>(relaxed = true)

        setWebDetailScreen(uiState = testContentUiState(detail = detail), uriHandler = uriHandler)
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { uriHandler.openUri(detail.url) }
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-032 반영하지 않은 URL 입력은 외부로 여는 주소를 바꾸지 않는다`() {
        val detail = testWebDetail()
        val uriHandler = mockk<UriHandler>(relaxed = true)

        setWebDetailScreen(uiState = testContentUiState(detail = detail), uriHandler = uriHandler)
        composeRule.selectFormTab()
        composeRule.urlInput().performTextReplacement(TYPED_URL)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { uriHandler.openUri(detail.url) }
        verify(exactly = 0) { uriHandler.openUri(TYPED_URL) }
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-017 앱 밖에서 열지 못해도 화면을 유지하고 알리지 않는다`() {
        val detail = testWebDetail()
        val uriHandler =
            mockk<UriHandler> {
                every { openUri(any()) } throws IllegalStateException("브라우저를 열 수 없습니다.")
            }

        setWebDetailScreen(uiState = testContentUiState(detail = detail), uriHandler = uriHandler)
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(detail.title).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-014 외부로 열기는 웹 페이지 요청을 일으키지 않는다`() {
        val detail = testWebDetail()
        val pageViewModel = pageViewModel(pageUiState = WebDetailPageUiState.Content(page = testWebPage()))

        setWebDetailScreen(uiState = testContentUiState(detail = detail), pageViewModel = pageViewModel)
        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { pageViewModel.load() }
        verify(exactly = 0) { pageViewModel.retry() }
        verify(exactly = 0) { pageViewModel.refresh() }
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-031 반영하지 않은 URL 입력은 다시 시도에 쓰지 않는다`() {
        val webViewModel = webViewModel()
        val pageViewModel = pageViewModel(pageUiState = WebDetailPageUiState.Failure)

        setWebDetailScreen(webViewModel = webViewModel, pageViewModel = pageViewModel)
        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        composeRule.selectFormTab()
        composeRule.urlInput().performTextReplacement(TYPED_URL)
        composeRule.selectPageTab()
        composeRule.onNodeWithText(DEFAULT_RETRY_BUTTON).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { pageViewModel.retry() }
        verify(exactly = 0) { webViewModel.update(any()) }
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-020 삭제를 선택하면 삭제를 한 번 실행한다`() {
        val webViewModel = webViewModel(uiState = testContentUiState())

        setWebDetailScreen(webViewModel = webViewModel)
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { webViewModel.delete() }
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-020 TC-TAG-DETAIL-WEB-FEATURE-020 삭제에 성공하면 뒤로가기와 같은 닫기로 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        val webViewModel = webViewModel(effect = flowOf(WebDetailEffect.DeleteSucceeded))

        setWebDetailScreen(webViewModel = webViewModel, navigateUp = { navigateUpCount++ })
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-023 진입할 때는 어느 입력에도 초점을 두지 않는다`() {
        setWebDetailScreen()
        composeRule.selectFormTab()

        composeRule.onAllNodes(isFocused()).fetchSemanticsNodes().shouldBeEmpty()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-024 수정을 선택하면 입력한 내용으로 수정을 한 번 실행한다`() {
        val detail = testWebDetail()
        val webViewModel = webViewModel(uiState = testContentUiState(detail = detail))

        setWebDetailScreen(webViewModel = webViewModel)
        composeRule.selectFormTab()
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { webViewModel.update(detail = detail.copy(title = TYPED_TITLE)) }
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-026 수정에 성공하면 입력을 유지하고 성공 안내를 표시한다`() {
        val detail = testWebDetail()
        val webViewModel =
            webViewModel(
                uiState = testContentUiState(detail = detail),
                effect = flowOf(WebDetailEffect.UpdateSucceeded),
            )

        setWebDetailScreen(webViewModel = webViewModel)
        composeRule.selectFormTab()

        composeRule.awaitText(DEFAULT_UPDATE_SUCCEEDED_MESSAGE)
        composeRule.titleInput().assert(hasText(detail.title))
        composeRule.urlInput().assert(hasText(detail.url))
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-030 수정에 성공하면 웹 페이지 다시 불러오기를 한 번 요청한다`() {
        val pageViewModel = pageViewModel()
        val webViewModel = webViewModel(effect = flowOf(WebDetailEffect.UpdateSucceeded))

        setWebDetailScreen(webViewModel = webViewModel, pageViewModel = pageViewModel)
        composeRule.waitForIdle()

        verify(exactly = 1) { pageViewModel.refresh() }
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-028 이름이 비어 있는 헤더 항목이 있으면 헤더 이름 안내를 표시한다`() {
        val detail = testWebDetail()
        val webViewModel =
            webViewModel(
                uiState = testContentUiState(detail = detail),
                effect = flowOf(WebDetailEffect.HeaderNameBlank),
            )

        setWebDetailScreen(webViewModel = webViewModel)
        composeRule.selectFormTab()

        composeRule.awaitText(DEFAULT_HEADER_NAME_BLANK_MESSAGE)
        composeRule.titleInput().assert(hasText(detail.title))
        composeRule.urlInput().assert(hasText(detail.url))
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-021 저장 내용이 바뀌어도 입력을 다시 채우지 않고 화면 제목만 갱신한다`() {
        val detail = testWebDetail()
        val changedDetail = detail.copy(title = CHANGED_TITLE, url = CHANGED_URL)
        val content = testContentUiState(detail = detail)
        val uiStateFlow = MutableStateFlow<WebDetailUiState>(content)

        setWebDetailScreen(webViewModel = webViewModel(uiStateFlow = uiStateFlow))
        composeRule.selectFormTab()
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()

        uiStateFlow.value = content.copy(detail = changedDetail)
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.urlInput().assert(hasText(detail.url))
        composeRule.onNodeWithText(CHANGED_TITLE).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-029 화면이 재생성되어도 입력 중이던 내용을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val uiStateFlow = MutableStateFlow<WebDetailUiState>(testContentUiState())

        restorationTester.setContent {
            WebDetailScreenTestTheme {
                WebDetailScreen(
                    navigateToMemoAdd = {},
                    navigateToMemoDetail = {},
                    id = FIRST_WEB_ID,
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    webViewModel = webViewModel(uiStateFlow = uiStateFlow),
                    pageViewModel = pageViewModel(),
                    navigateToTagDetail = {},
                    tagViewModel = detailTagScreenTestViewModel(),
                )
            }
        }

        composeRule.selectFormTab()
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.urlInput().performTextReplacement(TYPED_URL)
        composeRule.addHeaderRow()
        composeRule.headerNameInput().performTextInput(TYPED_HEADER_NAME)
        composeRule.headerValueInput().performTextInput(TYPED_HEADER_VALUE)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.urlInput().assert(hasText(TYPED_URL))
        composeRule.headerNameInput().assert(hasText(TYPED_HEADER_NAME))
        composeRule.headerValueInput().assert(hasText(TYPED_HEADER_VALUE))
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-049 TC-WEB-DETAIL-FEATURE-054 로그인 정보를 가져오지 못했다고 알리면 안내를 표시하고 웹 표시 수단이 주소를 연다`() {
        val uiState = testContentUiState()

        setWebDetailScreen(
            uiState = uiState,
            webSession = DiaryWebSession(failureId = 1),
        )

        composeRule.awaitText(DEFAULT_CHROME_SESSION_IMPORT_FAILED_MESSAGE)
        composeRule.onNodeWithContentDescription(DEFAULT_PAGE_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_RETRY_BUTTON).assertDoesNotExist()
    }

    private fun setWebDetailScreen(
        uiState: WebDetailUiState = testContentUiState(),
        webViewModel: WebDetailViewModel = webViewModel(uiState = uiState),
        pageViewModel: WebDetailPageViewModel = pageViewModel(),
        uriHandler: UriHandler = mockk(relaxed = true),
        navigateUp: () -> Unit = {},
        webSession: DiaryWebSession = DiaryWebSession(),
    ) {
        composeRule.setContent {
            WebDetailScreenTestTheme {
                CompositionLocalProvider(LocalUriHandler provides uriHandler, LocalDiaryWebSession provides webSession) {
                    WebDetailScreen(
                        navigateToMemoAdd = {},
                        navigateToMemoDetail = {},
                        id = FIRST_WEB_ID,
                        navigateToTagAdd = {},
                        tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                        navigateUp = navigateUp,
                        webViewModel = webViewModel,
                        pageViewModel = pageViewModel,
                        navigateToTagDetail = {},
                        tagViewModel = detailTagScreenTestViewModel(),
                    )
                }
            }
        }
    }

    private companion object {
        private const val TYPED_TITLE = "WebDetailTypedTitle"
        private const val TYPED_URL = "https://typed.example.com"
        private const val TYPED_HEADER_NAME = "Authorization"
        private const val TYPED_HEADER_VALUE = "Bearer typed-token"
        private const val CHANGED_TITLE = "WebDetailChangedTitle"
        private const val CHANGED_URL = "https://changed.example.com"

        private const val DEFAULT_UPDATE_SUCCEEDED_MESSAGE = "Web updated."
        private const val DEFAULT_HEADER_NAME_BLANK_MESSAGE = "Please enter a header name."
        private const val DEFAULT_CHROME_SESSION_IMPORT_FAILED_MESSAGE = "Couldn't get Chrome logins."

        private fun webViewModel(
            uiState: WebDetailUiState = testContentUiState(),
            uiStateFlow: MutableStateFlow<WebDetailUiState> = MutableStateFlow(uiState),
            effect: Flow<WebDetailEffect> = emptyFlow(),
        ): WebDetailViewModel {
            val viewModel = mockk<WebDetailViewModel>()
            every { viewModel.uiState } returns uiStateFlow
            every { viewModel.effect } returns effect
            justRun { viewModel.update(any()) }
            justRun { viewModel.delete() }

            return viewModel
        }

        private fun pageViewModel(pageUiState: WebDetailPageUiState = WebDetailPageUiState.Loading): WebDetailPageViewModel {
            val viewModel = mockk<WebDetailPageViewModel>()
            every { viewModel.uiState } returns MutableStateFlow(pageUiState)
            justRun { viewModel.load() }
            justRun { viewModel.retry() }
            justRun { viewModel.refresh() }

            return viewModel
        }

        private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.awaitText(text: String) {
            waitUntil { onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        }
    }
}
