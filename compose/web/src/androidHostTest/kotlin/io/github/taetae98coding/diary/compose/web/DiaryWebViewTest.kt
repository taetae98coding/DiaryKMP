package io.github.taetae98coding.diary.compose.web

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.web.WebPage
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val URL = "https://example.com"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryWebViewTest {
    @get:Rule
    val composeRule = createComposeRule()

    @After
    fun resetSession() {
        SingletonDiaryWebSession.set(DiaryWebSession())
    }

    @Test
    fun `TC-05590cc5-b758-4119-a847-d5ad0b3dd5ae 로그인 정보를 되돌리는 동안에는 진행 표시를 두고 웹 표시 수단을 두지 않는다`() {
        setUrlWebView(session = DiaryWebSession(isRestoring = true))

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-90df42c6-a0d3-4e73-a0d8-b2a10b64ea06 되돌리기가 끝나면 진행 표시가 사라지고 웹 표시 수단이 주소를 연다`() {
        setUrlWebView(session = DiaryWebSession(isRestoring = true))

        composeRule.runOnIdle { SingletonDiaryWebSession.set(DiaryWebSession(isRestoring = false)) }

        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
    }

    @Test
    fun `TC-fd4b6e27-e955-4a2e-9ab9-c3441881797b 되돌리는 중이 아니면 진행 표시 없이 웹 표시 수단이 주소를 연다`() {
        setUrlWebView(session = DiaryWebSession())

        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
    }

    @Test
    fun `TC-2c7098bf-a237-42c5-bcbb-eb55788d7132 응답 본문 방식은 되돌리기를 기다리지 않고 URL 방식으로 바꾸면 진행 표시를 거친다`() {
        SingletonDiaryWebSession.set(DiaryWebSession(isRestoring = true))
        var isUrlMode by mutableStateOf(false)
        composeRule.setContent {
            DiaryTheme {
                if (isUrlMode) {
                    DiaryWebView(url = URL)
                } else {
                    DiaryWebView(page = WebPage(baseUrl = URL, body = "<html></html>"))
                }
            }
        }

        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()

        composeRule.runOnIdle { isUrlMode = true }

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `앱이 세션을 갱신하기 전에도 웹 표시 수단이 주소를 연다`() {
        composeRule.setContent {
            DiaryTheme {
                DiaryWebView(url = URL)
            }
        }

        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
    }

    private fun setUrlWebView(session: DiaryWebSession) {
        SingletonDiaryWebSession.set(session)
        composeRule.setContent {
            DiaryTheme {
                DiaryWebView(url = URL)
            }
        }
    }
}
