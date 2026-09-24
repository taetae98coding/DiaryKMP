package io.github.taetae98coding.diary.compose.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.mockk.mockk
import io.mockk.verify
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

    @Test
    fun `TC-WEB-DETAIL-FEATURE-047 로그인 정보를 가져오는 동안에는 진행 표시를 두고 웹 표시 수단을 두지 않는다`() {
        setDiaryWebView(session = mutableStateOf(DiaryWebSession(isPreparing = true)))

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-050 가져오는 중이 아니면 진행 표시 없이 웹 표시 수단이 주소를 연다`() {
        setDiaryWebView(session = mutableStateOf(DiaryWebSession()))

        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
    }

    @Test
    fun `제공된 세션이 없어도 웹 표시 수단이 주소를 연다`() {
        composeRule.setContent {
            DiaryTheme {
                DiaryWebView(
                    url = URL,
                    onSessionImportFailed = {},
                )
            }
        }

        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-052 TC-WEB-DETAIL-FEATURE-053 가져오기가 시작되면 진행 표시로 바뀌고 끝나면 웹 표시 수단이 다시 열린다`() {
        val session = mutableStateOf(DiaryWebSession())

        setDiaryWebView(session = session)
        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()

        composeRule.runOnIdle { session.value = DiaryWebSession(isPreparing = true) }
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertDoesNotExist()

        composeRule.runOnIdle { session.value = DiaryWebSession(importCount = 1) }
        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-049 가져오기가 실패로 끝나면 한 번 알리고 웹 표시 수단이 주소를 연다`() {
        val session = mutableStateOf(DiaryWebSession(isPreparing = true))
        val onSessionImportFailed = mockk<() -> Unit>(relaxed = true)

        setDiaryWebView(session = session, onSessionImportFailed = onSessionImportFailed)
        composeRule.runOnIdle { session.value = DiaryWebSession(importCount = 1, failureId = 1) }

        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
        composeRule.runOnIdle { verify(exactly = 1) { onSessionImportFailed() } }
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-054 들어왔을 때 마지막 결과가 실패였으면 한 번 알린다`() {
        val onSessionImportFailed = mockk<() -> Unit>(relaxed = true)

        setDiaryWebView(session = mutableStateOf(DiaryWebSession(failureId = 1)), onSessionImportFailed = onSessionImportFailed)

        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
        composeRule.runOnIdle { verify(exactly = 1) { onSessionImportFailed() } }
    }

    @Test
    fun `마지막 결과가 성공이면 알리지 않는다`() {
        val onSessionImportFailed = mockk<() -> Unit>(relaxed = true)

        setDiaryWebView(session = mutableStateOf(DiaryWebSession(importCount = 2)), onSessionImportFailed = onSessionImportFailed)

        composeRule.runOnIdle { verify(exactly = 0) { onSessionImportFailed() } }
    }

    @Test
    fun `실패가 반복되면 그때마다 알린다`() {
        val session = mutableStateOf(DiaryWebSession(failureId = 1))
        val onSessionImportFailed = mockk<() -> Unit>(relaxed = true)

        setDiaryWebView(session = session, onSessionImportFailed = onSessionImportFailed)
        composeRule.runOnIdle { session.value = DiaryWebSession(isPreparing = true) }
        composeRule.runOnIdle { session.value = DiaryWebSession(importCount = 1, failureId = 2) }

        composeRule.runOnIdle { verify(exactly = 2) { onSessionImportFailed() } }
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-044 화면이 재생성되어도 이미 알린 실패를 다시 알리지 않는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val onSessionImportFailed = mockk<() -> Unit>(relaxed = true)

        restorationTester.setContent {
            DiaryWebViewUnderTest(session = DiaryWebSession(failureId = 1), onSessionImportFailed = onSessionImportFailed)
        }
        composeRule.runOnIdle { verify(exactly = 1) { onSessionImportFailed() } }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { verify(exactly = 1) { onSessionImportFailed() } }
    }

    private fun setDiaryWebView(
        session: MutableState<DiaryWebSession>,
        onSessionImportFailed: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryWebViewUnderTest(session = session.value, onSessionImportFailed = onSessionImportFailed)
        }
    }

    @Composable
    private fun DiaryWebViewUnderTest(
        session: DiaryWebSession,
        onSessionImportFailed: () -> Unit,
    ) {
        DiaryTheme {
            CompositionLocalProvider(LocalDiaryWebSession provides session) {
                DiaryWebView(
                    url = URL,
                    onSessionImportFailed = onSessionImportFailed,
                )
            }
        }
    }
}
