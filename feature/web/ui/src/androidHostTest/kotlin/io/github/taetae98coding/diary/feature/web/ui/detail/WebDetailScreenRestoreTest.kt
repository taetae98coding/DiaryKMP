package io.github.taetae98coding.diary.feature.web.ui.detail

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.web.DiaryWebSession
import io.github.taetae98coding.diary.compose.web.SingletonDiaryWebSession
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.domain.web.usecase.FetchWebPageUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.web.ui.add.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
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
class WebDetailScreenRestoreTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @After
    fun resetSession() {
        SingletonDiaryWebSession.set(DiaryWebSession())
    }

    @Test
    fun `TC-05590cc5-b758-4119-a847-d5ad0b3dd5ae 로그인 정보를 되돌리는 동안에는 진행 상태를 표시하고 주소를 열지 않으며 다른 조작은 그대로 할 수 있다`() {
        val detail = testWebDetail()
        val webViewModel = webViewModel(detail = detail)
        var navigateUpCount = 0
        SingletonDiaryWebSession.set(DiaryWebSession(isRestoring = true))
        setWebDetailScreen(webViewModel = webViewModel, navigateUp = { navigateUpCount++ })

        isProgressShown() shouldBe true
        findWebViewList().shouldBeEmpty()

        composeRule.selectFormTab()
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        verify(exactly = 1) { webViewModel.update(detail = detail.copy(title = TYPED_TITLE)) }

        composeRule.selectPageTab()
        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        composeRule.onNodeWithText(DEFAULT_RESPONSE_VIEW_MODE_LABEL).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-90df42c6-a0d3-4e73-a0d8-b2a10b64ea06 되돌리기가 끝나면 따로 알리지 않고 저장된 URL을 연다`() {
        val detail = testWebDetail()
        SingletonDiaryWebSession.set(DiaryWebSession(isRestoring = true))
        setWebDetailScreen(webViewModel = webViewModel(detail = detail))

        composeRule.runOnIdle { SingletonDiaryWebSession.set(DiaryWebSession(isRestoring = false)) }
        composeRule.waitForIdle()

        isProgressShown() shouldBe false
        shadowOf(findWebViewList().single()).lastLoadedUrl shouldBe detail.url
        composeRule.onNodeWithText(DEFAULT_RETRY_BUTTON).assertDoesNotExist()
        composeRule.onNodeWithText(PAGE_FAILURE_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-fd4b6e27-e955-4a2e-9ab9-c3441881797b 로그인 정보를 되돌리는 중이 아니면 곧바로 주소를 연다`() {
        val detail = testWebDetail()
        setWebDetailScreen(webViewModel = webViewModel(detail = detail))

        isProgressShown() shouldBe false
        shadowOf(findWebViewList().single()).lastLoadedUrl shouldBe detail.url
    }

    @Test
    fun `TC-2c7098bf-a237-42c5-bcbb-eb55788d7132 응답을 아직 받지 못하면 응답 본문 방식은 불러오는 중이고 URL 방식은 되돌리기를 기다린다`() {
        val detail = testWebDetail()
        val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
        coEvery { fetchWebPageUseCase(parameter = any()) } coAnswers { awaitCancellation() }
        SingletonDiaryWebSession.set(DiaryWebSession(isRestoring = true))
        setWebDetailScreen(webViewModel = webViewModel(detail = detail), pageViewModel = pageViewModel(detail = detail, fetchWebPageUseCase = fetchWebPageUseCase))

        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        coVerify(exactly = 1) { fetchWebPageUseCase(parameter = any()) }
        isProgressShown() shouldBe true
        findWebViewList().shouldBeEmpty()

        assertUrlModeWaitsForRestore()

        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        isProgressShown() shouldBe true
        findWebViewList().shouldBeEmpty()
    }

    @Test
    fun `TC-2c7098bf-a237-42c5-bcbb-eb55788d7132 응답을 받으면 응답 본문을 곧바로 보여 주고 다시 바꿨을 때 다시 요청하지 않는다`() {
        val detail = testWebDetail()
        val page = testWebPage()
        val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
        coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.success(page)
        SingletonDiaryWebSession.set(DiaryWebSession(isRestoring = true))
        setWebDetailScreen(webViewModel = webViewModel(detail = detail), pageViewModel = pageViewModel(detail = detail, fetchWebPageUseCase = fetchWebPageUseCase))

        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        awaitPageWebView(page = page)

        assertUrlModeWaitsForRestore()

        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        awaitPageWebView(page = page)
        coVerify(exactly = 1) { fetchWebPageUseCase(parameter = any()) }
    }

    @Test
    fun `TC-2c7098bf-a237-42c5-bcbb-eb55788d7132 응답을 받지 못하면 응답 본문 방식은 불러오기 실패 안내를 보여 준다`() {
        val detail = testWebDetail()
        val fetchWebPageUseCase = mockk<FetchWebPageUseCase>()
        coEvery { fetchWebPageUseCase(parameter = any()) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
        SingletonDiaryWebSession.set(DiaryWebSession(isRestoring = true))
        setWebDetailScreen(webViewModel = webViewModel(detail = detail), pageViewModel = pageViewModel(detail = detail, fetchWebPageUseCase = fetchWebPageUseCase))

        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        composeRule.onNodeWithText(PAGE_FAILURE_TITLE).assertExists()

        assertUrlModeWaitsForRestore()

        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        composeRule.onNodeWithText(PAGE_FAILURE_TITLE).assertExists()
    }

    @Test
    fun `TC-68bf8a56-f005-4f03-a1e5-e5b7113a22cb 주소를 연 뒤에는 화면을 계속 사용해도 되돌리는 중으로 돌아가지 않는다`() {
        val detail = testWebDetail()
        val webViewModel = webViewModel(detail = detail)
        SingletonDiaryWebSession.set(DiaryWebSession(isRestoring = true))
        setWebDetailScreen(webViewModel = webViewModel)
        composeRule.runOnIdle { SingletonDiaryWebSession.set(DiaryWebSession(isRestoring = false)) }
        composeRule.waitForIdle()
        val webView = findWebViewList().single()

        composeRule.openViewModeSheet()
        composeRule.closeDialogByBack()
        isProgressShown() shouldBe false
        findWebViewList().single() shouldBeSameInstanceAs webView

        composeRule.selectFormTab()
        composeRule.titleInput().performTextReplacement(TYPED_TITLE)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_DESCRIPTION).performClick()
        composeRule.selectPageTab()
        isProgressShown() shouldBe false
        shadowOf(findWebViewList().single()).lastLoadedUrl shouldBe detail.url

        composeRule.selectViewMode(label = DEFAULT_RESPONSE_VIEW_MODE_LABEL)
        composeRule.selectViewMode(label = DEFAULT_URL_VIEW_MODE_LABEL)
        isProgressShown() shouldBe false
        shadowOf(findWebViewList().single()).lastLoadedUrl shouldBe detail.url
    }

    private fun assertUrlModeWaitsForRestore() {
        composeRule.selectViewMode(label = DEFAULT_URL_VIEW_MODE_LABEL)

        isProgressShown() shouldBe true
        findWebViewList().all { webView -> shadowOf(webView).lastLoadedUrl == null } shouldBe true
    }

    private fun isProgressShown(): Boolean =
        composeRule
            .onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .fetchSemanticsNodes()
            .isNotEmpty()

    private fun awaitPageWebView(page: WebPage): WebView {
        composeRule.waitUntil(timeoutMillis = PAGE_TIMEOUT_MILLIS) { findWebViewList().any { webView -> webView.isShowing(page = page) } }
        return findWebViewList().single { webView -> webView.isShowing(page = page) }
    }

    private fun WebView.isShowing(page: WebPage): Boolean = shadowOf(this).lastLoadDataWithBaseURL?.data == page.body

    private fun setWebDetailScreen(
        webViewModel: WebDetailViewModel,
        pageViewModel: WebDetailPageViewModel = memoScreenPageViewModel(),
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            WebDetailScreenUnderTest(webViewModel = webViewModel, pageViewModel = pageViewModel, navigateUp = navigateUp)
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun WebDetailScreenUnderTest(
        webViewModel: WebDetailViewModel,
        pageViewModel: WebDetailPageViewModel,
        navigateUp: () -> Unit,
    ) {
        WebDetailScreenTestTheme {
            WebDetailScreen(
                navigateUp = navigateUp,
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
        const val TYPED_TITLE = "수정한 제목"
        const val PAGE_FAILURE_TITLE = "Unable to load the web page"
        const val PAGE_TIMEOUT_MILLIS = 5_000L

        fun webViewModel(detail: WebDetail): WebDetailViewModel = memoScreenWebViewModel(uiState = MutableStateFlow(testContentUiState(detail = detail)))

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
    }
}
