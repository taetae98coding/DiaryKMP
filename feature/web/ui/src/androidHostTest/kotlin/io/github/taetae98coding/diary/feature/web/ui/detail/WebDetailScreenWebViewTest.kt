package io.github.taetae98coding.diary.feature.web.ui.detail

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.domain.web.usecase.FetchWebPageUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.web.ui.add.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageUiState
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.time.Instant

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class WebDetailScreenWebViewTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `TC-bf42a2a8-5753-490b-8398-15339b3e2d41 URL 방식에서는 저장된 URL만 넘기고 요청 헤더는 넘기지 않는다`() {
        val detail = testWebDetail(headerList = List(HEADER_COUNT) { testWebHeader() })

        setWebDetailScreen(webViewModel = webViewModel(detail = detail))

        val webView = findWebViewList().single()
        shadowOf(webView).lastLoadedUrl shouldBe detail.url
        shadowOf(webView).lastAdditionalHttpHeaders.orEmpty() shouldBe emptyMap()
    }

    @Test
    fun `TC-73bd7b0a-5095-4950-a2da-08208ce8a52d 응답 본문은 응답을 받은 주소를 기준 주소로 넘긴다`() {
        val detail = testWebDetail(headerList = List(HEADER_COUNT) { testWebHeader() })
        val page = testWebPage()
        setWebDetailScreen(
            webViewModel = webViewModel(detail = detail),
            pageViewModel = pageViewModel(detail = detail, fetchWebPageUseCase = fetchWebPageUseCase(page = page)),
        )

        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)

        // 상대 경로 해석과 그 리소스 요청은 웹 표시 수단이 하므로, 웹 표시 수단에 넘긴 기준 주소와 헤더로 판정한다.
        val webView = awaitPageWebView(page = page)
        shadowOf(webView).lastLoadDataWithBaseURL?.baseUrl shouldBe page.baseUrl
    }

    @Test
    fun `TC-e58626cc-51a7-46d4-a321-aa44352da423 응답 본문은 스크립트를 실행하는 웹 표시 수단에 넘긴다`() {
        val detail = testWebDetail(headerList = List(HEADER_COUNT) { testWebHeader() })
        val page = testWebPage()
        setWebDetailScreen(
            webViewModel = webViewModel(detail = detail),
            pageViewModel = pageViewModel(detail = detail, fetchWebPageUseCase = fetchWebPageUseCase(page = page)),
        )

        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)

        // 스크립트 실행은 웹 표시 수단이 하므로, 웹 표시 수단의 스크립트 실행 설정으로 판정한다.
        val webView = awaitPageWebView(page = page)
        webView.settings.javaScriptEnabled shouldBe true
    }

    @Test
    fun `TC-2ce03775-17d3-41df-b21b-86d3a92eb8eb 웹 페이지 안에서 이동해도 외부로 여는 주소는 저장된 URL이다`() {
        val detail = testWebDetail()
        val uriHandler = mockk<UriHandler>(relaxed = true)
        setWebDetailScreen(webViewModel = webViewModel(detail = detail), uriHandler = uriHandler)
        val webView = findWebViewList().single()

        // 문서 안의 링크를 따라가는 이동은 앱을 거치지 않고 웹 표시 수단 안에서 일어나므로, 웹 표시 수단을 직접 다른 주소로 옮긴다.
        composeRule.runOnUiThread { webView.loadUrl(NAVIGATED_URL) }
        composeRule.waitForIdle()
        shadowOf(webView).lastLoadedUrl shouldBe NAVIGATED_URL

        composeRule.onNodeWithContentDescription(DEFAULT_OPEN_IN_NEW_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { uriHandler.openUri(detail.url) }
        verify(exactly = 0) { uriHandler.openUri(NAVIGATED_URL) }
    }

    @Test
    fun `TC-bfc3e32b-068c-476e-b1db-204c02ba8aac 앱을 다시 실행해 진입하면 웹 페이지를 다시 불러온다`() {
        val detail = testWebDetail()
        val page = testWebPage()
        // 앱을 다시 실행하면 화면의 ViewModel도 새로 만들어지므로, 실행마다 새 ViewModel을 넘긴다.
        val launchedWebViewModelList = List(LAUNCH_COUNT) { webViewModel(detail = detail) }
        val launchedPageViewModelList =
            listOf(
                memoScreenPageViewModel(pageUiState = WebDetailPageUiState.Content(page = page)),
                memoScreenPageViewModel(),
            )
        var launchCount by mutableIntStateOf(0)

        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없으므로, 새 키로 저장 상태 없이 화면을 다시 구성한다.
        composeRule.setContent {
            key(launchCount) {
                WebDetailScreenUnderTest(
                    webViewModel = launchedWebViewModelList[launchCount],
                    pageViewModel = launchedPageViewModelList[launchCount],
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        findWebViewList().any { webView -> shadowOf(webView).lastLoadDataWithBaseURL?.data == page.body } shouldBe true

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_URL_VIEW_MODE_LABEL).assertExists()
        val webView = findWebViewList().single()
        shadowOf(webView).lastLoadedUrl shouldBe detail.url
        shadowOf(webView).lastLoadDataWithBaseURL shouldBe null
        verify(exactly = 0) { launchedPageViewModelList[1].load() }
    }

    @Test
    fun `TC-f25eea61-c106-4e88-abf1-1ffd75cfba49 URL 방식에서 화면이 재생성되면 저장된 URL을 처음부터 다시 연다`() {
        val detail = testWebDetail()
        val fetchWebPageUseCase = fetchWebPageUseCase(page = testWebPage())
        val webViewModel = webViewModel(detail = detail)
        val pageViewModel = pageViewModel(detail = detail, fetchWebPageUseCase = fetchWebPageUseCase)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { WebDetailScreenUnderTest(webViewModel = webViewModel, pageViewModel = pageViewModel) }
        composeRule.waitForIdle()
        val webView = findWebViewList().single()
        moveInsideDocument(webView = webView)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        val redrawnWebView = findWebViewList().single()
        assertRedrawnFromStart(previous = webView, redrawn = redrawnWebView)
        shadowOf(redrawnWebView).lastLoadedUrl shouldBe detail.url
        coVerify(exactly = 0) { fetchWebPageUseCase(parameter = any()) }
    }

    @Test
    fun `TC-f25eea61-c106-4e88-abf1-1ffd75cfba49 응답 본문 방식에서 화면이 재생성되면 이미 받은 응답 본문을 처음부터 다시 그린다`() {
        val detail = testWebDetail()
        val page = testWebPage()
        val fetchWebPageUseCase = fetchWebPageUseCase(page = page)
        val webViewModel = webViewModel(detail = detail)
        val pageViewModel = pageViewModel(detail = detail, fetchWebPageUseCase = fetchWebPageUseCase)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { WebDetailScreenUnderTest(webViewModel = webViewModel, pageViewModel = pageViewModel) }
        composeRule.waitForIdle()
        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        val webView = awaitPageWebView(page = page)
        moveInsideDocument(webView = webView)

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        val redrawnWebView = awaitPageWebView(page = page)
        assertRedrawnFromStart(previous = webView, redrawn = redrawnWebView)
        shadowOf(redrawnWebView).lastLoadedUrl shouldBe null
        coVerify(exactly = 1) { fetchWebPageUseCase(parameter = any()) }
    }

    @Test
    fun `TC-14f16d67-3917-4aa8-9f7a-86c3f73f7a17 URL 방식에서 다른 탭에 다녀오면 저장된 URL을 처음부터 다시 연다`() {
        val detail = testWebDetail()
        val fetchWebPageUseCase = fetchWebPageUseCase(page = testWebPage())
        setWebDetailScreen(
            webViewModel = webViewModel(detail = detail),
            pageViewModel = pageViewModel(detail = detail, fetchWebPageUseCase = fetchWebPageUseCase),
        )
        val webView = findWebViewList().single()
        moveInsideDocument(webView = webView)

        composeRule.selectFormTab()
        composeRule.selectPageTab()

        val redrawnWebView = findWebViewList().single()
        assertRedrawnFromStart(previous = webView, redrawn = redrawnWebView)
        shadowOf(redrawnWebView).lastLoadedUrl shouldBe detail.url
        coVerify(exactly = 0) { fetchWebPageUseCase(parameter = any()) }
    }

    @Test
    fun `TC-14f16d67-3917-4aa8-9f7a-86c3f73f7a17 응답 본문 방식에서 다른 탭에 다녀오면 이미 받은 응답 본문을 처음부터 다시 그린다`() {
        val detail = testWebDetail()
        val page = testWebPage()
        val fetchWebPageUseCase = fetchWebPageUseCase(page = page)
        setWebDetailScreen(
            webViewModel = webViewModel(detail = detail),
            pageViewModel = pageViewModel(detail = detail, fetchWebPageUseCase = fetchWebPageUseCase),
        )
        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        val webView = awaitPageWebView(page = page)
        moveInsideDocument(webView = webView)

        composeRule.selectFormTab()
        composeRule.selectPageTab()

        val redrawnWebView = awaitPageWebView(page = page)
        assertRedrawnFromStart(previous = webView, redrawn = redrawnWebView)
        shadowOf(redrawnWebView).lastLoadedUrl shouldBe null
        coVerify(exactly = 1) { fetchWebPageUseCase(parameter = any()) }
    }

    // 문서 안의 링크 이동과 스크롤은 앱을 거치지 않고 웹 표시 수단 안에서 일어나므로, 웹 표시 수단을 직접 다른 주소로 옮기고 스크롤한다.
    private fun moveInsideDocument(webView: WebView) {
        composeRule.runOnUiThread {
            webView.loadUrl(NAVIGATED_URL)
            webView.scrollTo(0, SCROLLED_Y)
        }
        composeRule.waitForIdle()

        shadowOf(webView).lastLoadedUrl shouldBe NAVIGATED_URL
        webView.scrollY shouldBe SCROLLED_Y
    }

    // 웹 표시 수단을 새로 만들어 그려야 스크롤 위치와 문서 안의 이동이 남지 않는다.
    private fun assertRedrawnFromStart(
        previous: WebView,
        redrawn: WebView,
    ) {
        redrawn shouldNotBeSameInstanceAs previous
        redrawn.scrollY shouldBe 0
    }

    private fun awaitPageWebView(page: WebPage): WebView {
        composeRule.waitUntil(timeoutMillis = PAGE_TIMEOUT_MILLIS) { findWebViewList().any { webView -> webView.isShowing(page = page) } }
        return findWebViewList().single { webView -> webView.isShowing(page = page) }
    }

    private fun WebView.isShowing(page: WebPage): Boolean = shadowOf(this).lastLoadDataWithBaseURL?.data == page.body

    private fun setWebDetailScreen(
        webViewModel: WebDetailViewModel,
        pageViewModel: WebDetailPageViewModel = memoScreenPageViewModel(),
        uriHandler: UriHandler = mockk(relaxed = true),
    ) {
        composeRule.setContent {
            WebDetailScreenUnderTest(
                webViewModel = webViewModel,
                pageViewModel = pageViewModel,
                uriHandler = uriHandler,
            )
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun WebDetailScreenUnderTest(
        webViewModel: WebDetailViewModel,
        pageViewModel: WebDetailPageViewModel,
        uriHandler: UriHandler = mockk(relaxed = true),
    ) {
        WebDetailScreenTestTheme {
            CompositionLocalProvider(LocalUriHandler provides uriHandler) {
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
                )
            }
        }
    }

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
        const val HEADER_COUNT = 2
        const val LAUNCH_COUNT = 2
        const val NAVIGATED_URL = "https://kotlinlang.org/"
        const val SCROLLED_Y = 480
        const val PAGE_TIMEOUT_MILLIS = 5_000L

        fun webViewModel(detail: WebDetail): WebDetailViewModel = memoScreenWebViewModel(uiState = MutableStateFlow(testContentUiState(detail = detail)))

        fun fetchWebPageUseCase(page: WebPage): FetchWebPageUseCase {
            val useCase = mockk<FetchWebPageUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.success(page)
            return useCase
        }

        // 웹 페이지 요청이 몇 번 일어났는지를 요청 경계에서 세도록, 불러오기 상태는 실제 ViewModel이 들고 요청만 대역으로 둔다.
        fun pageViewModel(
            detail: WebDetail,
            fetchWebPageUseCase: FetchWebPageUseCase,
        ): WebDetailPageViewModel {
            val web =
                Web(
                    id = FIRST_WEB_ID,
                    detail = detail,
                    isDeleted = false,
                    updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                    createdAt = fixtureMonkey.giveMeOne<Instant>(),
                )
            val findWebUseCase = mockk<FindWebUseCase>()
            every { findWebUseCase(parameter = FIRST_WEB_ID) } returns flowOf<Result<Web?>>(Result.success(web))

            return WebDetailPageViewModel(
                id = FIRST_WEB_ID,
                findWebUseCase = findWebUseCase,
                fetchWebPageUseCase = fetchWebPageUseCase,
            )
        }

        fun testWebHeader(): WebHeader = WebHeader(name = "header-${fixtureMonkey.giveMeOne<String>()}", value = fixtureMonkey.giveMeOne<String>())
    }
}
