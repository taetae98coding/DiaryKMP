package io.github.taetae98coding.diary.feature.web.ui.detail.page

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.web.DiaryWebSession
import io.github.taetae98coding.diary.compose.web.LocalDiaryWebSession
import io.github.taetae98coding.diary.core.testing.web.webDetail
import io.github.taetae98coding.diary.core.testing.web.webPage
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailScaffoldState
import io.github.taetae98coding.diary.feature.web.ui.detail.rememberWebDetailScaffoldState
import io.github.taetae98coding.diary.feature.web.ui.detail.viewmode.WebDetailViewMode
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WebDetailPageWebViewTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var session by mutableStateOf(DiaryWebSession())

    @Test
    fun `TC-05590cc5-b758-4119-a847-d5ad0b3dd5ae 로그인 정보를 되돌리는 동안에는 진행 표시를 두고 웹 표시 수단을 두지 않는다`() {
        setUrlWebView(url = Arb.webDetail().next().url, session = DiaryWebSession(isRestoring = true))

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithTag(WEB_VIEW_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-90df42c6-a0d3-4e73-a0d8-b2a10b64ea06 되돌리기가 끝나면 진행 표시가 사라지고 웹 표시 수단이 주소를 연다`() {
        setUrlWebView(url = Arb.webDetail().next().url, session = DiaryWebSession(isRestoring = true))

        composeRule.runOnIdle { session = DiaryWebSession(isRestoring = false) }

        composeRule.onNodeWithTag(WEB_VIEW_TEST_TAG).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
    }

    @Test
    fun `TC-fd4b6e27-e955-4a2e-9ab9-c3441881797b 되돌리는 중이 아니면 진행 표시 없이 웹 표시 수단이 주소를 연다`() {
        setUrlWebView(url = Arb.webDetail().next().url, session = DiaryWebSession())

        composeRule.onNodeWithTag(WEB_VIEW_TEST_TAG).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
    }

    @Test
    fun `TC-2c7098bf-a237-42c5-bcbb-eb55788d7132 응답 본문 방식은 되돌리기를 기다리지 않고 URL 방식으로 바꾸면 진행 표시를 거친다`() {
        val url = Arb.webDetail().next().url
        val page = Arb.webPage().next()
        session = DiaryWebSession(isRestoring = true)
        lateinit var state: WebDetailScaffoldState
        composeRule.setContent {
            state = rememberWebDetailScaffoldState(initialViewMode = WebDetailViewMode.RESPONSE)

            DiaryTheme {
                CompositionLocalProvider(LocalDiaryWebSession provides session) {
                    WebDetailPage(
                        onEvent = {},
                        webViewContent = remember { AndroidWebViewContent() },
                        state = state,
                        urlProvider = { url },
                        uiStateProvider = { WebDetailPageUiState.Content(page = page) },
                    )
                }
            }
        }

        composeRule.onNodeWithTag(WEB_VIEW_TEST_TAG).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()

        composeRule.runOnIdle { state.select(viewMode = WebDetailViewMode.URL) }

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithTag(WEB_VIEW_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `앱이 세션을 제공하지 않아도 웹 표시 수단이 주소를 연다`() {
        val url = Arb.webDetail().next().url

        composeRule.setContent {
            DiaryTheme {
                WebDetailPage(
                    onEvent = {},
                    webViewContent = remember { AndroidWebViewContent() },
                    urlProvider = { url },
                )
            }
        }

        composeRule.onNodeWithTag(WEB_VIEW_TEST_TAG).assertExists()
    }

    private fun setUrlWebView(
        url: String,
        session: DiaryWebSession,
    ) {
        this.session = session
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalDiaryWebSession provides this.session) {
                    WebDetailPage(
                        onEvent = {},
                        webViewContent = remember { AndroidWebViewContent() },
                        urlProvider = { url },
                    )
                }
            }
        }
    }
}
