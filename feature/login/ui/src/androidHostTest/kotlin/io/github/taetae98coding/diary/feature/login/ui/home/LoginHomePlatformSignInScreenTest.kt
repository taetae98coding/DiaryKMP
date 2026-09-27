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
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsManager
import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsUserCancelException
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsException
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsManager
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsUserCancelException
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
    fun `TC-LOGIN-FEATURE-030 Google 흐름이 진행되는 동안 진행 표시를 보이고 취소되면 버튼을 다시 보인다`() {
        val platformResult = CompletableDeferred<GoogleCredential>()
        val googleCredentialsManager = mockGoogleCredentialsManager()
        coEvery { googleCredentialsManager.signIn() } coAnswers { platformResult.await() }

        assertProgressWhilePlatformSignIn(
            buttonDescription = DEFAULT_GOOGLE_BUTTON_DESCRIPTION,
            googleCredentialsManager = googleCredentialsManager,
            cancel = { platformResult.completeExceptionally(GoogleCredentialsUserCancelException()) },
        )
    }

    @Test
    fun `TC-LOGIN-FEATURE-030 Apple 흐름이 진행되는 동안 진행 표시를 보이고 취소되면 버튼을 다시 보인다`() {
        val platformResult = CompletableDeferred<AppleCredential>()
        val appleCredentialsManager = mockAppleCredentialsManager()
        coEvery { appleCredentialsManager.signIn() } coAnswers { platformResult.await() }

        assertProgressWhilePlatformSignIn(
            buttonDescription = DEFAULT_APPLE_BUTTON_DESCRIPTION,
            appleCredentialsManager = appleCredentialsManager,
            cancel = { platformResult.completeExceptionally(AppleCredentialsUserCancelException()) },
        )
    }

    @Test
    fun `TC-LOGIN-FEATURE-031 인증 결과를 받으면 버튼을 다시 보이지 않고 진행 표시가 앱 로그인 처리로 이어진다`() {
        val credential = screenTestFixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
        val uiState = MutableStateFlow(LoginHomeUiState())
        val viewModel = screenTestViewModel(uiState = uiState)
        val googleCredentialsManager = mockGoogleCredentialsManager()
        val platformResult = CompletableDeferred<GoogleCredential>()
        coEvery { googleCredentialsManager.signIn() } coAnswers { platformResult.await() }
        every { viewModel.signInWithGoogle(credential) } answers { uiState.value = LoginHomeUiState(isInProgress = true) }
        composeRule.setLoginHomeScreen(viewModel = viewModel, googleCredentialsManager = googleCredentialsManager)

        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
        platformResult.complete(credential)
        repeat(FRAME_COUNT_TO_OBSERVE) {
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).assertDoesNotExist()
            composeRule.onNodeWithContentDescription(DEFAULT_APPLE_BUTTON_DESCRIPTION).assertDoesNotExist()
        }

        verify(exactly = 1) { viewModel.signInWithGoogle(credential) }
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
    }

    @Test
    fun `TC-LOGIN-FEATURE-032 끝을 알 수 없는 흐름 중 같은 수단을 다시 선택하면 새 흐름을 연다`() {
        val googleCredentialsManager = mockGoogleCredentialsManager(isSignInEndDetectable = false)
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
    fun `TC-LOGIN-FEATURE-032 끝을 알 수 없는 흐름 중 다른 수단을 선택하면 새 흐름을 연다`() {
        val googleCredentialsManager = mockGoogleCredentialsManager(isSignInEndDetectable = false)
        val appleCredentialsManager = mockAppleCredentialsManager(isSignInEndDetectable = false)
        coEvery { googleCredentialsManager.signIn() } coAnswers { CompletableDeferred<GoogleCredential>().await() }
        coEvery { appleCredentialsManager.signIn() } coAnswers { CompletableDeferred<AppleCredential>().await() }
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
    fun `TC-LOGIN-FEATURE-033 먼저 받은 인증 결과 뒤에 온 인증 결과는 무시한다`() {
        val lateCredential = screenTestFixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
        assertLateResultIgnored { late -> late.complete(lateCredential) }
    }

    @Test
    fun `TC-LOGIN-FEATURE-033 먼저 받은 인증 결과 뒤에 온 사용자 취소는 무시한다`() {
        assertLateResultIgnored { late -> late.completeExceptionally(GoogleCredentialsUserCancelException()) }
    }

    @Test
    fun `TC-LOGIN-FEATURE-033 먼저 받은 인증 결과 뒤에 온 인증 결과 획득 실패는 무시한다`() {
        assertLateResultIgnored { late -> late.completeExceptionally(GoogleCredentialsException()) }
    }

    @Test
    fun `TC-LOGIN-FEATURE-034 앱 로그인 처리 실패 뒤 이전 흐름의 결과는 무시하고 새 흐름의 결과는 받는다`() {
        val firstCredential = screenTestFixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
        val staleCredential = screenTestFixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
        val newCredential = screenTestFixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
        val effect = Channel<LoginHomeEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        val googleCredentialsManager = mockGoogleCredentialsManager(isSignInEndDetectable = false)
        val platformResultList = List(size = 3) { CompletableDeferred<GoogleCredential>() }
        coEvery { googleCredentialsManager.signIn() } coAnswers { platformResultList[0].await() } coAndThen
            { platformResultList[1].await() } coAndThen { platformResultList[2].await() }
        every { viewModel.signInWithGoogle(any()) } answers { effect.trySend(LoginHomeEffect.SignInFailed).getOrThrow() }
        composeRule.setLoginHomeScreen(viewModel = viewModel, googleCredentialsManager = googleCredentialsManager)

        clickGoogleSignIn(times = 2)
        platformResultList[0].complete(firstCredential)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_SIGN_IN_FAILED_MESSAGE).assertExists()
        clickGoogleSignIn(times = 1)
        platformResultList[1].complete(staleCredential)
        composeRule.waitForIdle()
        platformResultList[2].complete(newCredential)
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.signInWithGoogle(firstCredential) }
        verify(exactly = 0) { viewModel.signInWithGoogle(staleCredential) }
        verify(exactly = 1) { viewModel.signInWithGoogle(newCredential) }
    }

    @Test
    fun `TC-LOGIN-FEATURE-035 앱 로그인 처리 전 한 흐름이 실패해도 나머지 흐름의 인증 결과로 로그인한다`() {
        val credential = screenTestFixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
        val viewModel = screenTestViewModel()
        val googleCredentialsManager = mockGoogleCredentialsManager(isSignInEndDetectable = false)
        val platformResultList = List(size = 2) { CompletableDeferred<GoogleCredential>() }
        coEvery { googleCredentialsManager.signIn() } coAnswers { platformResultList[0].await() } coAndThen
            { platformResultList[1].await() }
        every { viewModel.signInWithGoogle(credential) } returns Unit
        composeRule.setLoginHomeScreen(viewModel = viewModel, googleCredentialsManager = googleCredentialsManager)

        clickGoogleSignIn(times = 2)
        platformResultList[0].completeExceptionally(GoogleCredentialsException())
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_SIGN_IN_FAILED_MESSAGE).assertExists()
        platformResultList[1].complete(credential)
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.signInWithGoogle(credential) }
    }

    @Test
    fun `TC-LOGIN-FEATURE-036 끝을 알 수 있는 흐름 중 뒤로가기를 실행하면 흐름의 결과를 받지 않는다`() {
        assertNavigateUpStopsPlatformSignIn(isSignInEndDetectable = true)
    }

    @Test
    fun `TC-LOGIN-FEATURE-036 끝을 알 수 없는 흐름 중 뒤로가기를 실행하면 흐름의 결과를 받지 않는다`() {
        assertNavigateUpStopsPlatformSignIn(isSignInEndDetectable = false)
    }

    @Test
    fun `TC-LOGIN-FEATURE-010 앱 로그인 처리가 시작된 뒤 전달된 로그인 수단 선택은 새 흐름을 열지 않는다`() {
        val uiState = MutableStateFlow(LoginHomeUiState())
        val googleCredentialsManager = mockGoogleCredentialsManager(isSignInEndDetectable = false)
        val appleCredentialsManager = mockAppleCredentialsManager(isSignInEndDetectable = false)
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

    private fun assertProgressWhilePlatformSignIn(
        buttonDescription: String,
        cancel: () -> Unit,
        googleCredentialsManager: GoogleCredentialsManager = mockGoogleCredentialsManager(),
        appleCredentialsManager: AppleCredentialsManager = mockAppleCredentialsManager(),
    ) {
        var navigateUpCount = 0
        composeRule.setLoginHomeScreen(
            viewModel = screenTestViewModel(),
            googleCredentialsManager = googleCredentialsManager,
            appleCredentialsManager = appleCredentialsManager,
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(buttonDescription).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        navigateUpCount shouldBe 1

        cancel()
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(buttonDescription).assertExists()
        composeRule.onNodeWithText(DEFAULT_SIGN_IN_FAILED_MESSAGE).assertDoesNotExist()
    }

    private fun assertLateResultIgnored(completeLate: (CompletableDeferred<GoogleCredential>) -> Unit) {
        val firstCredential = screenTestFixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
        val viewModel = screenTestViewModel()
        val googleCredentialsManager = mockGoogleCredentialsManager(isSignInEndDetectable = false)
        val platformResultList = List(size = 2) { CompletableDeferred<GoogleCredential>() }
        coEvery { googleCredentialsManager.signIn() } coAnswers { platformResultList[0].await() } coAndThen
            { platformResultList[1].await() }
        every { viewModel.signInWithGoogle(any()) } returns Unit
        composeRule.setLoginHomeScreen(viewModel = viewModel, googleCredentialsManager = googleCredentialsManager)

        clickGoogleSignIn(times = 2)
        platformResultList[0].complete(firstCredential)
        composeRule.waitForIdle()
        completeLate(platformResultList[1])
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.signInWithGoogle(any()) }
        verify(exactly = 1) { viewModel.signInWithGoogle(firstCredential) }
        composeRule.onNodeWithText(DEFAULT_SIGN_IN_FAILED_MESSAGE).assertDoesNotExist()
    }

    private fun assertNavigateUpStopsPlatformSignIn(isSignInEndDetectable: Boolean) {
        val credential = screenTestFixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
        val viewModel = screenTestViewModel()
        val googleCredentialsManager = mockGoogleCredentialsManager(isSignInEndDetectable = isSignInEndDetectable)
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

    private companion object {
        private const val FRAME_COUNT_TO_OBSERVE = 10
    }
}
