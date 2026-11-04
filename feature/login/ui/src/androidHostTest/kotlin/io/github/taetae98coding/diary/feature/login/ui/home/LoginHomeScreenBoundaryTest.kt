package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsException
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsUserCancelException
import io.github.taetae98coding.diary.compose.credential.apple.AppleSignInResult
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsException
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsUserCancelException
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.coroutines.cancellation.CancellationException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoginHomeScreenBoundaryTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-bc91f0c6-4bba-4e54-b547-f3db93b94fbf 흐름 중 화면이 재생성되면 흐름을 중단하고 안내 없이 다시 선택할 수 있다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val viewModel = screenTestViewModel()
        val googleCredentialsManager = mockGoogleCredentialsManager()
        val cancelledList = mutableListOf<Throwable>()
        coEvery { googleCredentialsManager.signIn() } coAnswers {
            try {
                awaitCancellation()
            } catch (exception: CancellationException) {
                cancelledList += exception
                throw exception
            }
        }
        restorationTester.setContent {
            DiaryTheme {
                LoginHomeScreen(
                    navigateUp = {},
                    googleCredentialsManager = googleCredentialsManager,
                    appleCredentialsManager = mockk(),
                    viewModel = viewModel,
                )
            }
        }

        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        cancelledList.size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_SIGN_IN_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        verify(exactly = 0) { viewModel.signInWithGoogle(any()) }

        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 2) { googleCredentialsManager.signIn() }
    }

    @Test
    fun `TC-d67168e5-76ef-4d03-bf1e-5dfa0686cf02 앱 로그인 처리 중 화면이 재생성되어도 진행 표시가 이어진다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val viewModel = mockk<LoginHomeViewModel>()
        every { viewModel.uiState } returns MutableStateFlow(LoginHomeUiState(isInProgress = true))
        every { viewModel.effect } returns emptyFlow()
        restorationTester.setContent {
            DiaryTheme {
                LoginHomeScreen(
                    navigateUp = {},
                    googleCredentialsManager = mockk(),
                    appleCredentialsManager = mockk(),
                    viewModel = viewModel,
                )
            }
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-0599fd8a-8e41-4262-869a-ae93c4a8b472 앱 로그인 전에 끝난 취소와 인증 결과 획득 실패는 오류 보고를 남기지 않는다`() {
        val logList = recordLog()
        val viewModel = screenTestViewModel()
        val googleCredentialsManager = mockGoogleCredentialsManager()
        val appleCredentialsManager = mockAppleCredentialsManager()
        coEvery { googleCredentialsManager.signIn() } throws GoogleCredentialsUserCancelException() andThenThrows GoogleCredentialsException()
        coEvery { appleCredentialsManager.signIn() } throws AppleCredentialsUserCancelException() andThenThrows AppleCredentialsException()
        composeRule.setLoginHomeScreen(
            viewModel = viewModel,
            googleCredentialsManager = googleCredentialsManager,
            appleCredentialsManager = appleCredentialsManager,
        )

        listOf(
            DEFAULT_GOOGLE_BUTTON_DESCRIPTION,
            DEFAULT_GOOGLE_BUTTON_DESCRIPTION,
            DEFAULT_APPLE_BUTTON_DESCRIPTION,
            DEFAULT_APPLE_BUTTON_DESCRIPTION,
        ).forEach { buttonDescription ->
            composeRule.onNodeWithContentDescription(buttonDescription).performClick()
            composeRule.waitForIdle()
        }

        coVerify(exactly = 2) { googleCredentialsManager.signIn() }
        coVerify(exactly = 2) { appleCredentialsManager.signIn() }
        logList.shouldBeEmpty()
    }

    private fun recordLog(): List<DiaryLog> {
        val logList = mutableListOf<DiaryLog>()
        val delegate = mockk<DiaryLoggerDelegate>()
        every { delegate.log(log = any()) } answers { logList += firstArg<DiaryLog>() }
        DiaryLogger.add(delegate = delegate)
        return logList
    }
}
