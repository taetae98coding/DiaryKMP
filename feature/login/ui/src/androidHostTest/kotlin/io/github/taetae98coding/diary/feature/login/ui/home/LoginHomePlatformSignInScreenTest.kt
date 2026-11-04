package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManager
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsUserCancelException
import io.github.taetae98coding.diary.compose.credential.apple.AppleSignInResult
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsException
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsManager
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsUserCancelException
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.core.testing.authentication.googleCredentialIdToken
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.coroutines.cancellation.CancellationException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoginHomePlatformSignInScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-f0710c82-73ec-469f-b025-d677731d900a 흐름 중 같은 수단을 다시 선택하면 새 흐름을 연다`() {
        val googleCredentialsManager = mockGoogleCredentialsManager()
        coEvery { googleCredentialsManager.signIn() } coAnswers { CompletableDeferred<GoogleCredential>().await() }
        composeRule.setLoginHomeScreen(viewModel = screenTestViewModel(), googleCredentialsManager = googleCredentialsManager)

        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        assertButtonsWithoutProgress()
        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 2) { googleCredentialsManager.signIn() }
        assertButtonsWithoutProgress()
    }

    @Test
    fun `TC-f0710c82-73ec-469f-b025-d677731d900a 흐름 중 다른 수단을 선택하면 새 흐름을 연다`() {
        val googleCredentialsManager = mockGoogleCredentialsManager()
        val appleCredentialsManager = mockAppleCredentialsManager()
        coEvery { googleCredentialsManager.signIn() } coAnswers { CompletableDeferred<GoogleCredential>().await() }
        coEvery { appleCredentialsManager.signIn() } coAnswers { CompletableDeferred<AppleSignInResult>().await() }
        composeRule.setLoginHomeScreen(
            viewModel = screenTestViewModel(),
            googleCredentialsManager = googleCredentialsManager,
            appleCredentialsManager = appleCredentialsManager,
        )

        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        assertButtonsWithoutProgress()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { googleCredentialsManager.signIn() }
        coVerify(exactly = 1) { appleCredentialsManager.signIn() }
        assertButtonsWithoutProgress()
    }

    @Test
    fun `TC-6244d246-78da-4803-9a83-38d4785949d9 다시 선택한 뒤 이전 흐름이 돌려준 인증 결과는 받지 않는다`() {
        val staleCredential = Arb.googleCredentialIdToken().next()
        assertPreviousFlowResultIgnored { previous -> previous.complete(staleCredential) }
    }

    @Test
    fun `TC-6244d246-78da-4803-9a83-38d4785949d9 다시 선택한 뒤 이전 흐름이 돌려준 사용자 취소는 받지 않는다`() {
        assertPreviousFlowResultIgnored { previous -> previous.completeExceptionally(GoogleCredentialsUserCancelException()) }
    }

    @Test
    fun `TC-6244d246-78da-4803-9a83-38d4785949d9 다시 선택한 뒤 이전 흐름이 돌려준 인증 결과 획득 실패는 받지 않는다`() {
        assertPreviousFlowResultIgnored { previous -> previous.completeExceptionally(GoogleCredentialsException()) }
    }

    @Test
    fun `TC-1950bb16-007e-49e7-8799-945c0c0f8f8a 흐름 중 뒤로가기를 실행하면 흐름의 결과를 받지 않는다`() {
        val credential = Arb.googleCredentialIdToken().next()
        val viewModel = screenTestViewModel()
        val googleCredentialsManager = mockGoogleCredentialsManager()
        val platformResult = CompletableDeferred<GoogleCredential>()
        val cancelledList = mutableListOf<Throwable>()
        var isLoginShown by mutableStateOf(true)
        coEvery { googleCredentialsManager.signIn() } coAnswers {
            try {
                platformResult.await()
            } catch (exception: CancellationException) {
                cancelledList += exception
                throw exception
            }
        }
        composeRule.setContent {
            DiaryTheme {
                if (isLoginShown) {
                    LoginHomeScreen(
                        navigateUp = { isLoginShown = false },
                        googleCredentialsManager = googleCredentialsManager,
                        appleCredentialsManager = mockAppleCredentialsManager(),
                        viewModel = viewModel,
                    )
                }
            }
        }

        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        platformResult.complete(credential)
        composeRule.waitForIdle()

        isLoginShown shouldBe false
        cancelledList.size shouldBe 1
        verify(exactly = 0) { viewModel.signInWithGoogle(any()) }
    }

    @Test
    fun `TC-bf35c0ee-ee31-4ebd-8b72-937d90ccccda 앱 로그인 처리가 시작된 뒤 전달된 로그인 수단 선택은 새 흐름을 열지 않는다`() {
        val uiState = MutableStateFlow(LoginHomeUiState())
        val googleCredentialsManager = mockGoogleCredentialsManager()
        val appleCredentialsManager = mockAppleCredentialsManager()
        composeRule.setLoginHomeScreen(
            viewModel = screenTestViewModel(uiState = uiState),
            googleCredentialsManager = googleCredentialsManager,
            appleCredentialsManager = appleCredentialsManager,
        )

        composeRule.mainClock.autoAdvance = false
        uiState.value = LoginHomeUiState(isInProgress = true)
        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLE_BUTTON_DESCRIPTION).performClick()
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        coVerify(exactly = 0) { googleCredentialsManager.signIn() }
        coVerify(exactly = 0) { appleCredentialsManager.signIn() }
    }

    private fun assertPreviousFlowResultIgnored(completePrevious: (CompletableDeferred<GoogleCredential>) -> Unit) {
        val newCredential = Arb.googleCredentialIdToken().next()
        val viewModel = screenTestViewModel()
        val googleCredentialsManager = mockGoogleCredentialsManager()
        val platformResultList = List(size = 2) { CompletableDeferred<GoogleCredential>() }
        coEvery { googleCredentialsManager.signIn() } coAnswers { platformResultList[0].await() } coAndThen
            { platformResultList[1].await() }
        every { viewModel.signInWithGoogle(any()) } returns Unit
        composeRule.setLoginHomeScreen(viewModel = viewModel, googleCredentialsManager = googleCredentialsManager)

        clickGoogleSignIn(times = 2)
        completePrevious(platformResultList[0])
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_SIGN_IN_FAILED_MESSAGE).assertDoesNotExist()
        platformResultList[1].complete(newCredential)
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.signInWithGoogle(any()) }
        verify(exactly = 1) { viewModel.signInWithGoogle(newCredential) }
        composeRule.onNodeWithText(DEFAULT_SIGN_IN_FAILED_MESSAGE).assertDoesNotExist()
    }

    private fun clickGoogleSignIn(times: Int) {
        repeat(times) {
            composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()
            composeRule.waitForIdle()
        }
    }

    private fun assertButtonsWithoutProgress() {
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLE_BUTTON_DESCRIPTION).assertExists()
    }
}
