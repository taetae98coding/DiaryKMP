package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsException
import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsManager
import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsUserCancelException
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsException
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsManager
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsUserCancelException
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
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
    fun `TC-LOGIN-FEATURE-028 Google 흐름 진행 중 Google을 다시 선택해도 새 흐름을 시작하지 않는다`() {
        assertSecondSelectionIgnored(secondButtonDescription = DEFAULT_GOOGLE_BUTTON_DESCRIPTION)
    }

    @Test
    fun `TC-LOGIN-FEATURE-028 Google 흐름 진행 중 Apple을 선택해도 새 흐름을 시작하지 않는다`() {
        assertSecondSelectionIgnored(secondButtonDescription = DEFAULT_APPLE_BUTTON_DESCRIPTION)
    }

    @Test
    fun `TC-LOGIN-FEATURE-029 끝을 알 수 있는 흐름 중 화면이 재생성되면 흐름을 중단하고 안내 없이 다시 선택할 수 있다`() {
        assertPlatformSignInInterruptedByRecreation(isSignInEndDetectable = true)
    }

    @Test
    fun `TC-LOGIN-FEATURE-029 끝을 알 수 없는 흐름 중 화면이 재생성되면 흐름을 중단하고 안내 없이 다시 선택할 수 있다`() {
        assertPlatformSignInInterruptedByRecreation(isSignInEndDetectable = false)
    }

    @Test
    fun `TC-LOGIN-FEATURE-025 앱 로그인 처리 중 화면이 재생성되어도 진행 표시가 이어진다`() {
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
    fun `TC-LOGIN-FEATURE-021 앱 로그인 전에 끝난 취소와 인증 결과 획득 실패는 오류 보고를 남기지 않는다`() {
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

    private fun assertPlatformSignInInterruptedByRecreation(isSignInEndDetectable: Boolean) {
        val restorationTester = StateRestorationTester(composeRule)
        val viewModel = screenTestViewModel()
        val googleCredentialsManager = mockGoogleCredentialsManager(isSignInEndDetectable = isSignInEndDetectable)
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

    private fun assertSecondSelectionIgnored(secondButtonDescription: String) {
        val credential = screenTestFixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
        val viewModel = screenTestViewModel()
        val googleCredentialsManager = mockGoogleCredentialsManager()
        val appleCredentialsManager = mockAppleCredentialsManager()
        val platformResult = CompletableDeferred<GoogleCredential>()
        coEvery { googleCredentialsManager.signIn() } coAnswers { platformResult.await() }
        coEvery { appleCredentialsManager.signIn() } returns screenTestFixtureMonkey.giveMeOne<AppleCredential>()
        every { viewModel.signInWithGoogle(credential) } returns Unit
        composeRule.setLoginHomeScreen(
            viewModel = viewModel,
            googleCredentialsManager = googleCredentialsManager,
            appleCredentialsManager = appleCredentialsManager,
        )

        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithContentDescription(secondButtonDescription).performClick()
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        platformResult.complete(credential)
        composeRule.waitForIdle()

        coVerify(exactly = 1) { googleCredentialsManager.signIn() }
        coVerify(exactly = 0) { appleCredentialsManager.signIn() }
        verify(exactly = 1) { viewModel.signInWithGoogle(credential) }
        verify(exactly = 0) { viewModel.signInWithApple(any()) }
    }

    private fun recordLog(): List<DiaryLog> {
        val logList = mutableListOf<DiaryLog>()
        val delegate = mockk<DiaryLoggerDelegate>()
        every { delegate.log(log = any()) } answers { logList += firstArg<DiaryLog>() }
        DiaryLogger.add(delegate = delegate)
        return logList
    }
}
