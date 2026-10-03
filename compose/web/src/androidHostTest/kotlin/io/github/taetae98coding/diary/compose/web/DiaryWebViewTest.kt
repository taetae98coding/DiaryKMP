package io.github.taetae98coding.diary.compose.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.mockk.mockk
import io.mockk.verify
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
    fun `TC-WEB-DETAIL-FEATURE-047 로그인 정보를 가져오는 동안에는 진행 표시를 두고 웹 표시 수단을 두지 않는다`() {
        setDiaryWebView(session = DiaryWebSession(isPreparing = true))

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-050 가져오는 중이 아니면 진행 표시 없이 웹 표시 수단이 주소를 연다`() {
        setDiaryWebView(session = DiaryWebSession())

        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
    }

    @Test
    fun `앱이 세션을 갱신하기 전에도 웹 표시 수단이 주소를 연다`() {
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
        setDiaryWebView(session = DiaryWebSession())
        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()

        composeRule.runOnIdle { SingletonDiaryWebSession.set(DiaryWebSession(isPreparing = true)) }
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertDoesNotExist()

        composeRule.runOnIdle { SingletonDiaryWebSession.set(DiaryWebSession(importCount = 1)) }
        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-049 가져오기가 실패로 끝나면 한 번 알리고 웹 표시 수단이 주소를 연다`() {
        val onSessionImportFailed = mockk<() -> Unit>(relaxed = true)

        setDiaryWebView(session = DiaryWebSession(isPreparing = true), onSessionImportFailed = onSessionImportFailed)
        composeRule.runOnIdle { SingletonDiaryWebSession.set(DiaryWebSession(importCount = 1, failureId = 1)) }

        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
        composeRule.runOnIdle { verify(exactly = 1) { onSessionImportFailed() } }
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-054 들어왔을 때 마지막 결과가 실패였으면 한 번 알린다`() {
        val onSessionImportFailed = mockk<() -> Unit>(relaxed = true)

        setDiaryWebView(session = DiaryWebSession(failureId = 1), onSessionImportFailed = onSessionImportFailed)

        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
        composeRule.runOnIdle { verify(exactly = 1) { onSessionImportFailed() } }
    }

    @Test
    fun `마지막 결과가 성공이면 알리지 않는다`() {
        val onSessionImportFailed = mockk<() -> Unit>(relaxed = true)

        setDiaryWebView(session = DiaryWebSession(importCount = 2), onSessionImportFailed = onSessionImportFailed)

        composeRule.runOnIdle { verify(exactly = 0) { onSessionImportFailed() } }
    }

    @Test
    fun `실패가 반복되면 그때마다 알린다`() {
        val onSessionImportFailed = mockk<() -> Unit>(relaxed = true)

        setDiaryWebView(session = DiaryWebSession(failureId = 1), onSessionImportFailed = onSessionImportFailed)
        composeRule.runOnIdle { SingletonDiaryWebSession.set(DiaryWebSession(isPreparing = true)) }
        composeRule.runOnIdle { SingletonDiaryWebSession.set(DiaryWebSession(importCount = 1, failureId = 2)) }

        composeRule.runOnIdle { verify(exactly = 2) { onSessionImportFailed() } }
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-044 화면이 재생성되어도 이미 알린 실패를 다시 알리지 않는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val onSessionImportFailed = mockk<() -> Unit>(relaxed = true)

        SingletonDiaryWebSession.set(DiaryWebSession(failureId = 1))
        restorationTester.setContent {
            DiaryWebViewUnderTest(onSessionImportFailed = onSessionImportFailed)
        }
        composeRule.runOnIdle { verify(exactly = 1) { onSessionImportFailed() } }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { verify(exactly = 1) { onSessionImportFailed() } }
    }

    @Test
    fun `웹 표시 영역이 빠졌다가 다시 들어와도 알린 실패 번호를 가진 상태가 같으면 다시 알리지 않는다`() {
        val isVisible = mutableStateOf(true)
        val onSessionImportFailed = mockk<() -> Unit>(relaxed = true)

        SingletonDiaryWebSession.set(DiaryWebSession(failureId = 1))
        composeRule.setContent {
            val sessionImportFailureState = rememberDiaryWebSessionImportFailureState()

            DiaryTheme {
                if (isVisible.value) {
                    DiaryWebView(
                        url = URL,
                        onSessionImportFailed = onSessionImportFailed,
                        sessionImportFailureState = sessionImportFailureState,
                    )
                }
            }
        }
        composeRule.runOnIdle { verify(exactly = 1) { onSessionImportFailed() } }

        composeRule.runOnIdle { isVisible.value = false }
        composeRule.runOnIdle { isVisible.value = true }

        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
        composeRule.runOnIdle { verify(exactly = 1) { onSessionImportFailed() } }
    }

    private fun setDiaryWebView(
        session: DiaryWebSession,
        onSessionImportFailed: () -> Unit = {},
    ) {
        SingletonDiaryWebSession.set(session)
        composeRule.setContent {
            DiaryWebViewUnderTest(onSessionImportFailed = onSessionImportFailed)
        }
    }

    @Composable
    private fun DiaryWebViewUnderTest(onSessionImportFailed: () -> Unit) {
        DiaryTheme {
            DiaryWebView(
                url = URL,
                onSessionImportFailed = onSessionImportFailed,
            )
        }
    }
}
