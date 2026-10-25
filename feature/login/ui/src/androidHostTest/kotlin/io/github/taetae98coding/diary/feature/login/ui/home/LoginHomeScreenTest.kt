package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsUserCancelException
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsUserCancelException
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoginHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-532981c4-18f3-4d1a-bbc2-37689b26fb9f 뒤로가기 선택 시 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        composeRule.setLoginHomeScreen(
            viewModel = screenTestViewModel(),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-56edd934-9a22-4d83-b348-176f7027fb87 앱 로그인 처리 중에도 이전 화면으로 돌아갈 수 있다`() {
        var navigateUpCount = 0
        composeRule.setLoginHomeScreen(
            viewModel = screenTestViewModel(uiState = MutableStateFlow(LoginHomeUiState(isInProgress = true))),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-cc06c3c7-3563-432b-85f4-1e2ebef86ed5 Google 로그인 흐름 시작`() {
        val credential = screenTestFixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
        val viewModel = screenTestViewModel()
        val googleCredentialsManager = mockGoogleCredentialsManager()
        val appleCredentialsManager = mockAppleCredentialsManager()
        coEvery { googleCredentialsManager.signIn() } returns credential
        every { viewModel.signInWithGoogle(credential) } returns Unit
        composeRule.setLoginHomeScreen(
            viewModel = viewModel,
            googleCredentialsManager = googleCredentialsManager,
            appleCredentialsManager = appleCredentialsManager,
        )

        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { googleCredentialsManager.signIn() }
        coVerify(exactly = 0) { appleCredentialsManager.signIn() }
        verify(exactly = 1) { viewModel.signInWithGoogle(credential) }
        verify(exactly = 0) { viewModel.signInWithApple(any()) }
    }

    @Test
    fun `TC-cc06c3c7-3563-432b-85f4-1e2ebef86ed5 Apple 로그인 흐름 시작`() {
        val credential = screenTestFixtureMonkey.giveMeOne<AppleCredential>()
        val viewModel = screenTestViewModel()
        val googleCredentialsManager = mockGoogleCredentialsManager()
        val appleCredentialsManager = mockAppleCredentialsManager()
        coEvery { appleCredentialsManager.signIn() } returns credential
        every { viewModel.signInWithApple(credential) } returns Unit
        composeRule.setLoginHomeScreen(
            viewModel = viewModel,
            googleCredentialsManager = googleCredentialsManager,
            appleCredentialsManager = appleCredentialsManager,
        )

        composeRule.onNodeWithContentDescription(DEFAULT_APPLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { appleCredentialsManager.signIn() }
        coVerify(exactly = 0) { googleCredentialsManager.signIn() }
        verify(exactly = 1) { viewModel.signInWithApple(credential) }
        verify(exactly = 0) { viewModel.signInWithGoogle(any()) }
    }

    @Test
    fun `TC-d706775c-402c-4029-8b07-4757e11f0aa4 Google 취소는 오류로 안내하지 않는다`() {
        val googleCredentialsManager = mockGoogleCredentialsManager()
        coEvery { googleCredentialsManager.signIn() } throws GoogleCredentialsUserCancelException()
        composeRule.setLoginHomeScreen(
            viewModel = screenTestViewModel(),
            googleCredentialsManager = googleCredentialsManager,
        )

        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_SIGN_IN_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 2) { googleCredentialsManager.signIn() }
    }

    @Test
    fun `TC-d706775c-402c-4029-8b07-4757e11f0aa4 Apple 취소는 오류로 안내하지 않는다`() {
        val appleCredentialsManager = mockAppleCredentialsManager()
        coEvery { appleCredentialsManager.signIn() } throws AppleCredentialsUserCancelException()
        composeRule.setLoginHomeScreen(
            viewModel = screenTestViewModel(),
            appleCredentialsManager = appleCredentialsManager,
        )

        composeRule.onNodeWithContentDescription(DEFAULT_APPLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_SIGN_IN_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 2) { appleCredentialsManager.signIn() }
    }

    @Test
    fun `TC-8bb48c53-91c9-48a5-b1c5-2de1255812e1 Google 로그인 성공 시 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        val credential = screenTestFixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
        val effect = Channel<LoginHomeEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        val googleCredentialsManager = mockGoogleCredentialsManager()
        coEvery { googleCredentialsManager.signIn() } returns credential
        every { viewModel.signInWithGoogle(credential) } answers {
            effect.trySend(LoginHomeEffect.SignInSucceeded).getOrThrow()
        }
        composeRule.setLoginHomeScreen(
            viewModel = viewModel,
            googleCredentialsManager = googleCredentialsManager,
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) { navigateUpCount == 1 }

        coVerify(exactly = 1) { googleCredentialsManager.signIn() }
        verify(exactly = 1) { viewModel.signInWithGoogle(credential) }
        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-8bb48c53-91c9-48a5-b1c5-2de1255812e1 Apple 로그인 성공 시 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        val credential = screenTestFixtureMonkey.giveMeOne<AppleCredential>()
        val effect = Channel<LoginHomeEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        val appleCredentialsManager = mockAppleCredentialsManager()
        coEvery { appleCredentialsManager.signIn() } returns credential
        every { viewModel.signInWithApple(credential) } answers {
            effect.trySend(LoginHomeEffect.SignInSucceeded).getOrThrow()
        }
        composeRule.setLoginHomeScreen(
            viewModel = viewModel,
            appleCredentialsManager = appleCredentialsManager,
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_APPLE_BUTTON_DESCRIPTION).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) { navigateUpCount == 1 }

        coVerify(exactly = 1) { appleCredentialsManager.signIn() }
        verify(exactly = 1) { viewModel.signInWithApple(credential) }
        navigateUpCount shouldBe 1
    }
}
