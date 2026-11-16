package io.github.taetae98coding.diary.feature.web.ui.detail

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.core.testing.web.webDetail
import io.github.taetae98coding.diary.core.testing.web.webHeader
import io.github.taetae98coding.diary.core.testing.web.webPage
import io.github.taetae98coding.diary.domain.web.usecase.FetchWebPageUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.web.ui.add.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.page.AndroidWebViewContent
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageViewModel
import io.github.taetae98coding.diary.feature.web.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class WebDetailScreenHiddenChangeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var isCovered by mutableStateOf(false)

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-5958e1bb-c753-4e11-bb67-1b0b97aff259 다른 화면에 가려진 동안 URL과 헤더가 바뀌면 돌아와 다시 불러온 본문을 본다`() {
        val detail = testWebDetail(headerList = listOf(Arb.webHeader().next()))
        val changedDetail = detail.copy(url = Arb.webDetail().next().url, headerList = detail.headerList + Arb.webHeader().next())
        val firstPage = Arb.webPage().next()
        val reloadedPage = Arb.webPage().next()
        val webFlow = MutableStateFlow<Result<Web?>>(Result.success(Arb.web().next().copy(id = FIRST_WEB_ID, detail = detail, isDeleted = false)))
        val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
        coEvery { fetchWebPageUseCase(parameter = detail.toParameter()) } returns Result.success(firstPage)
        coEvery { fetchWebPageUseCase(parameter = changedDetail.toParameter()) } returns Result.success(reloadedPage)
        setResponseModeScreen(detail = detail, webFlow = webFlow, fetchWebPageUseCase = fetchWebPageUseCase, page = firstPage)

        cover()
        composeRule.runOnIdle { webFlow.value = Result.success(Arb.web().next().copy(id = FIRST_WEB_ID, detail = changedDetail, isDeleted = false)) }
        composeRule.waitForIdle()
        coVerify(exactly = 1) { fetchWebPageUseCase(parameter = changedDetail.toParameter()) }
        uncover()

        awaitPageWebView(page = reloadedPage)
        findWebViewList().filter { webView -> webView.isShowing(page = firstPage) }.shouldBeEmpty()
        coVerify(exactly = 2) { fetchWebPageUseCase(parameter = any()) }
    }

    @Test
    fun `TC-5958e1bb-c753-4e11-bb67-1b0b97aff259 백그라운드에 있는 동안 URL과 헤더가 바뀌면 돌아와 다시 불러온 본문을 본다`() {
        val detail = testWebDetail(headerList = listOf(Arb.webHeader().next()))
        val changedDetail = detail.copy(url = Arb.webDetail().next().url, headerList = detail.headerList + Arb.webHeader().next())
        val firstPage = Arb.webPage().next()
        val reloadedPage = Arb.webPage().next()
        val webFlow = MutableStateFlow<Result<Web?>>(Result.success(Arb.web().next().copy(id = FIRST_WEB_ID, detail = detail, isDeleted = false)))
        val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
        coEvery { fetchWebPageUseCase(parameter = detail.toParameter()) } returns Result.success(firstPage)
        coEvery { fetchWebPageUseCase(parameter = changedDetail.toParameter()) } returns Result.success(reloadedPage)
        setResponseModeScreen(detail = detail, webFlow = webFlow, fetchWebPageUseCase = fetchWebPageUseCase, page = firstPage)

        moveToBackground()
        composeRule.runOnIdle { webFlow.value = Result.success(Arb.web().next().copy(id = FIRST_WEB_ID, detail = changedDetail, isDeleted = false)) }
        composeRule.waitForIdle()
        coVerify(exactly = 1) { fetchWebPageUseCase(parameter = changedDetail.toParameter()) }
        returnToForeground()

        awaitPageWebView(page = reloadedPage)
        findWebViewList().filter { webView -> webView.isShowing(page = firstPage) }.shouldBeEmpty()
        coVerify(exactly = 2) { fetchWebPageUseCase(parameter = any()) }
    }

    @Test
    fun `TC-f896e680-8ad6-4277-9c1a-3eaa17620add 백그라운드에서 시작된 다시 불러오기가 진행 중이면 돌아와 진행 상태를 본다`() {
        val detail = testWebDetail()
        val changedDetail = detail.copy(url = Arb.webDetail().next().url)
        val firstPage = Arb.webPage().next()
        val webFlow = MutableStateFlow<Result<Web?>>(Result.success(Arb.web().next().copy(id = FIRST_WEB_ID, detail = detail, isDeleted = false)))
        val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
        coEvery { fetchWebPageUseCase(parameter = detail.toParameter()) } returns Result.success(firstPage)
        coEvery { fetchWebPageUseCase(parameter = changedDetail.toParameter()) } coAnswers { CompletableDeferred<Result<WebPage>>().await() }
        setResponseModeScreen(detail = detail, webFlow = webFlow, fetchWebPageUseCase = fetchWebPageUseCase, page = firstPage)

        moveToBackground()
        composeRule.runOnIdle { webFlow.value = Result.success(Arb.web().next().copy(id = FIRST_WEB_ID, detail = changedDetail, isDeleted = false)) }
        composeRule.waitForIdle()
        returnToForeground()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        findWebViewList().filter { webView -> webView.isShowing(page = firstPage) }.shouldBeEmpty()
    }

    @Test
    fun `TC-f896e680-8ad6-4277-9c1a-3eaa17620add 백그라운드에서 시작된 다시 불러오기가 실패했으면 돌아와 실패 안내를 본다`() {
        val detail = testWebDetail()
        val changedDetail = detail.copy(url = Arb.webDetail().next().url)
        val firstPage = Arb.webPage().next()
        val webFlow = MutableStateFlow<Result<Web?>>(Result.success(Arb.web().next().copy(id = FIRST_WEB_ID, detail = detail, isDeleted = false)))
        val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
        coEvery { fetchWebPageUseCase(parameter = detail.toParameter()) } returns Result.success(firstPage)
        coEvery { fetchWebPageUseCase(parameter = changedDetail.toParameter()) } returns Result.failure(IllegalStateException(Arb.string().next()))
        setResponseModeScreen(detail = detail, webFlow = webFlow, fetchWebPageUseCase = fetchWebPageUseCase, page = firstPage)

        moveToBackground()
        composeRule.runOnIdle { webFlow.value = Result.success(Arb.web().next().copy(id = FIRST_WEB_ID, detail = changedDetail, isDeleted = false)) }
        composeRule.waitForIdle()
        returnToForeground()

        composeRule.onNodeWithText(PAGE_FAILURE_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_RETRY_BUTTON).assertExists()
        findWebViewList().filter { webView -> webView.isShowing(page = firstPage) }.shouldBeEmpty()
    }

    @Test
    fun `TC-872aa2b3-f7bb-47a1-8fc0-9e369b06cf70 다른 화면에 다녀와도 URL과 헤더가 그대로면 다시 불러오지 않는다`() {
        val detail = testWebDetail(headerList = listOf(Arb.webHeader().next()))
        val page = Arb.webPage().next()
        val webFlow = MutableStateFlow<Result<Web?>>(Result.success(Arb.web().next().copy(id = FIRST_WEB_ID, detail = detail, isDeleted = false)))
        val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
        coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.success(page)
        setResponseModeScreen(detail = detail, webFlow = webFlow, fetchWebPageUseCase = fetchWebPageUseCase, page = page)

        cover()
        uncover()

        awaitPageWebView(page = page)
        composeRule.onNodeWithText(DEFAULT_RESPONSE_VIEW_MODE_LABEL).assertExists()
        coVerify(exactly = 1) { fetchWebPageUseCase(parameter = any()) }
    }

    private fun setResponseModeScreen(
        detail: WebDetail,
        webFlow: MutableStateFlow<Result<Web?>>,
        fetchWebPageUseCase: FetchWebPageUseCase,
        page: WebPage,
    ) {
        val webViewModel = memoScreenWebViewModel(uiState = MutableStateFlow(testContentUiState(detail = detail)))
        // 웹 페이지 요청이 몇 번 일어났는지를 요청 경계에서 세도록, 불러오기 상태는 실제 ViewModel이 들고 요청만 대역으로 둔다.
        val findWebUseCase = mockk<FindWebUseCase>()
        every { findWebUseCase(parameter = FIRST_WEB_ID) } returns webFlow
        val pageViewModel = WebDetailPageViewModel(id = FIRST_WEB_ID, findWebUseCase = findWebUseCase, fetchWebPageUseCase = fetchWebPageUseCase)

        composeRule.setContent {
            WebDetailScreenTestTheme {
                // 다른 화면으로 이동하면 이 화면은 화면에서 빠지고, 화면이 저장한 상태와 ViewModel만 남았다가 돌아올 때 다시 쓰인다.
                val stateHolder = rememberSaveableStateHolder()

                if (!isCovered) {
                    stateHolder.SaveableStateProvider(SCREEN_STATE_KEY) {
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
                            pageViewModel = pageViewModel,
                            tagViewModel = detailTagScreenTestViewModel(),
                            contentSharerFactory = testContentSharerFactory(),
                            webViewContent = AndroidWebViewContent(),
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        awaitPageWebView(page = page)
    }

    private fun cover() {
        composeRule.runOnIdle { isCovered = true }
        composeRule.waitForIdle()
        findWebViewList().shouldBeEmpty()
    }

    private fun uncover() {
        composeRule.runOnIdle { isCovered = false }
        composeRule.waitForIdle()
    }

    private fun moveToBackground() {
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
    }

    private fun returnToForeground() {
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()
    }

    private fun awaitPageWebView(page: WebPage): WebView {
        composeRule.waitUntil(timeoutMillis = PAGE_TIMEOUT_MILLIS) { findWebViewList().any { webView -> webView.isShowing(page = page) } }
        return findWebViewList().single { webView -> webView.isShowing(page = page) }
    }

    private fun WebView.isShowing(page: WebPage): Boolean = shadowOf(this).lastLoadDataWithBaseURL?.data == page.body

    private fun findWebViewList(): List<WebView> =
        composeRule.activity.window.decorView
            .webViewList()

    private fun View.webViewList(): List<WebView> =
        when (this) {
            is WebView -> listOf(this)
            is ViewGroup -> (0 until childCount).flatMap { index -> getChildAt(index).webViewList() }
            else -> emptyList()
        }

    private companion object {
        const val SCREEN_STATE_KEY = "WebDetail"
        const val PAGE_TIMEOUT_MILLIS = 5_000L
        const val PAGE_FAILURE_TITLE = "Unable to load the web page"

        fun WebDetail.toParameter(): FetchWebPageUseCase.Parameter = FetchWebPageUseCase.Parameter(url = url, headerList = headerList)
    }
}
