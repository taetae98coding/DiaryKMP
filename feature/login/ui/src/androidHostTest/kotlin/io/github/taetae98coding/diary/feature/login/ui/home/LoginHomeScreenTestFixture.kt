package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsException
import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsManager
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsException
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsManager
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal const val DEFAULT_GOOGLE_BUTTON_DESCRIPTION: String = "Sign in with Google"
internal const val DEFAULT_APPLE_BUTTON_DESCRIPTION: String = "Sign in with Apple"
internal const val DEFAULT_SIGN_IN_FAILED_MESSAGE: String = "Sign-in failed."
internal const val DEFAULT_NAVIGATE_UP_DESCRIPTION: String = "Navigate up"
internal const val KOREAN_GOOGLE_BUTTON_DESCRIPTION: String = "Google로 로그인"
internal const val KOREAN_APPLE_BUTTON_DESCRIPTION: String = "Apple로 로그인"
internal const val KOREAN_SIGN_IN_FAILED_MESSAGE: String = "로그인에 실패했습니다."

internal val screenTestFixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal fun screenTestViewModel(
    effect: Flow<LoginHomeEffect> = emptyFlow(),
    uiState: MutableStateFlow<LoginHomeUiState> = MutableStateFlow(LoginHomeUiState()),
): LoginHomeViewModel {
    val viewModel = mockk<LoginHomeViewModel>()
    every { viewModel.uiState } returns uiState
    every { viewModel.effect } returns effect
    return viewModel
}

internal fun mockGoogleCredentialsManager(isSignInEndDetectable: Boolean = true): GoogleCredentialsManager {
    val manager = mockk<GoogleCredentialsManager>()
    every { manager.isSignInEndDetectable } returns isSignInEndDetectable
    return manager
}

internal fun mockAppleCredentialsManager(isSignInEndDetectable: Boolean = true): AppleCredentialsManager {
    val manager = mockk<AppleCredentialsManager>()
    every { manager.isSignInEndDetectable } returns isSignInEndDetectable
    return manager
}

internal fun ComposeContentTestRule.setLoginHomeScreen(
    viewModel: LoginHomeViewModel,
    googleCredentialsManager: GoogleCredentialsManager = mockk(),
    appleCredentialsManager: AppleCredentialsManager = mockk(),
    navigateUp: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            LoginHomeScreen(
                navigateUp = navigateUp,
                viewModel = viewModel,
                googleCredentialsManager = googleCredentialsManager,
                appleCredentialsManager = appleCredentialsManager,
            )
        }
    }
}

internal fun ComposeContentTestRule.assertGoogleCredentialFailureMessage(
    buttonDescription: String,
    expectedMessage: String,
) {
    val viewModel = screenTestViewModel()
    val googleCredentialsManager = mockGoogleCredentialsManager()
    coEvery { googleCredentialsManager.signIn() } throws GoogleCredentialsException()
    setLoginHomeScreen(viewModel = viewModel, googleCredentialsManager = googleCredentialsManager)

    onNodeWithContentDescription(buttonDescription).performClick()
    waitForIdle()

    coVerify(exactly = 1) { googleCredentialsManager.signIn() }
    onNodeWithText(expectedMessage).assertExists()
    onNodeWithContentDescription(buttonDescription).assertExists()
}

internal fun ComposeContentTestRule.assertAppleCredentialFailureMessage(
    buttonDescription: String,
    expectedMessage: String,
) {
    val viewModel = screenTestViewModel()
    val appleCredentialsManager = mockAppleCredentialsManager()
    coEvery { appleCredentialsManager.signIn() } throws AppleCredentialsException()
    setLoginHomeScreen(viewModel = viewModel, appleCredentialsManager = appleCredentialsManager)

    onNodeWithContentDescription(buttonDescription).performClick()
    waitForIdle()

    coVerify(exactly = 1) { appleCredentialsManager.signIn() }
    onNodeWithText(expectedMessage).assertExists()
    onNodeWithContentDescription(buttonDescription).assertExists()
}

internal fun ComposeContentTestRule.assertAppSignInFailureMessage(
    buttonDescription: String,
    expectedMessage: String,
) {
    val credential = screenTestFixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
    val effect = Channel<LoginHomeEffect>(capacity = Channel.BUFFERED)
    val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
    val googleCredentialsManager = mockGoogleCredentialsManager()
    coEvery { googleCredentialsManager.signIn() } returns credential
    every { viewModel.signInWithGoogle(credential) } answers {
        effect.trySend(LoginHomeEffect.SignInFailed).getOrThrow()
    }
    setLoginHomeScreen(viewModel = viewModel, googleCredentialsManager = googleCredentialsManager)

    onNodeWithContentDescription(buttonDescription).performClick()
    waitForIdle()

    verify(exactly = 1) { viewModel.signInWithGoogle(credential) }
    onNodeWithText(expectedMessage).assertExists()
    onNodeWithContentDescription(buttonDescription).assertExists()
}

internal fun ComposeContentTestRule.assertNoAppSignInRequest(
    buttonDescription: String,
    googleFailure: Throwable = GoogleCredentialsException(),
    appleFailure: Throwable = AppleCredentialsException(),
) {
    val viewModel = screenTestViewModel()
    val googleCredentialsManager = mockGoogleCredentialsManager()
    val appleCredentialsManager = mockAppleCredentialsManager()
    coEvery { googleCredentialsManager.signIn() } throws googleFailure
    coEvery { appleCredentialsManager.signIn() } throws appleFailure
    setLoginHomeScreen(
        viewModel = viewModel,
        googleCredentialsManager = googleCredentialsManager,
        appleCredentialsManager = appleCredentialsManager,
    )

    onNodeWithContentDescription(buttonDescription).performClick()
    waitForIdle()

    verify(exactly = 0) { viewModel.signInWithGoogle(any()) }
    verify(exactly = 0) { viewModel.signInWithApple(any()) }
}
