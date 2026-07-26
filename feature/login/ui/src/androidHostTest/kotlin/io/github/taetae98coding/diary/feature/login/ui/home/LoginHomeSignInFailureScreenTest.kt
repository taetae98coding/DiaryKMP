package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsException
import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsUserCancelException
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsException
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsUserCancelException
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoginHomeSignInFailureScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-LOGIN-FEATURE-007 기본 Google credential 실패 안내`() {
        composeRule.assertGoogleCredentialFailureMessage(
            buttonDescription = DEFAULT_GOOGLE_BUTTON_DESCRIPTION,
            expectedMessage = DEFAULT_SIGN_IN_FAILED_MESSAGE,
        )
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-LOGIN-FEATURE-007 한국어 Google credential 실패 안내`() {
        composeRule.assertGoogleCredentialFailureMessage(
            buttonDescription = KOREAN_GOOGLE_BUTTON_DESCRIPTION,
            expectedMessage = KOREAN_SIGN_IN_FAILED_MESSAGE,
        )
    }

    @Test
    fun `TC-LOGIN-FEATURE-007 기본 Apple credential 실패 안내`() {
        composeRule.assertAppleCredentialFailureMessage(
            buttonDescription = DEFAULT_APPLE_BUTTON_DESCRIPTION,
            expectedMessage = DEFAULT_SIGN_IN_FAILED_MESSAGE,
        )
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-LOGIN-FEATURE-007 한국어 Apple credential 실패 안내`() {
        composeRule.assertAppleCredentialFailureMessage(
            buttonDescription = KOREAN_APPLE_BUTTON_DESCRIPTION,
            expectedMessage = KOREAN_SIGN_IN_FAILED_MESSAGE,
        )
    }

    @Test
    fun `TC-LOGIN-FEATURE-007 기본 앱 로그인 실패 안내`() {
        composeRule.assertAppSignInFailureMessage(
            buttonDescription = DEFAULT_GOOGLE_BUTTON_DESCRIPTION,
            expectedMessage = DEFAULT_SIGN_IN_FAILED_MESSAGE,
        )
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-LOGIN-FEATURE-007 한국어 앱 로그인 실패 안내`() {
        composeRule.assertAppSignInFailureMessage(
            buttonDescription = KOREAN_GOOGLE_BUTTON_DESCRIPTION,
            expectedMessage = KOREAN_SIGN_IN_FAILED_MESSAGE,
        )
    }

    @Test
    fun `TC-LOGIN-DATA-005 Google 로그인을 취소하면 앱 로그인을 요청하지 않는다`() {
        composeRule.assertNoAppSignInRequest(
            buttonDescription = DEFAULT_GOOGLE_BUTTON_DESCRIPTION,
            googleFailure = GoogleCredentialsUserCancelException(),
        )
    }

    @Test
    fun `TC-LOGIN-DATA-005 Google 인증 결과를 받지 못하면 앱 로그인을 요청하지 않는다`() {
        composeRule.assertNoAppSignInRequest(
            buttonDescription = DEFAULT_GOOGLE_BUTTON_DESCRIPTION,
            googleFailure = GoogleCredentialsException(),
        )
    }

    @Test
    fun `TC-LOGIN-DATA-005 Apple 로그인을 취소하면 앱 로그인을 요청하지 않는다`() {
        composeRule.assertNoAppSignInRequest(
            buttonDescription = DEFAULT_APPLE_BUTTON_DESCRIPTION,
            appleFailure = AppleCredentialsUserCancelException(),
        )
    }

    @Test
    fun `TC-LOGIN-DATA-005 Apple 인증 결과를 받지 못하면 앱 로그인을 요청하지 않는다`() {
        composeRule.assertNoAppSignInRequest(
            buttonDescription = DEFAULT_APPLE_BUTTON_DESCRIPTION,
            appleFailure = AppleCredentialsException(),
        )
    }
}
