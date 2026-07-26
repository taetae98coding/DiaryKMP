package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoginHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 제목은 로그인이다`() {
        setLoginHomeScaffold()

        composeRule.onNodeWithText("로그인").assertExists()
    }

    @Test
    fun `기본 환경에서 제목은 Login이다`() {
        setLoginHomeScaffold()

        composeRule.onNodeWithText(SCAFFOLD_TEST_DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 이름은 Google로 로그인이다`() {
        setLoginHomeScaffold()

        composeRule.onNodeWithContentDescription("Google로 로그인").assert(hasClickAction())
    }

    @Test
    fun `기본 이름은 Sign in with Google이다`() {
        setLoginHomeScaffold()

        composeRule.onNodeWithContentDescription(SCAFFOLD_TEST_DEFAULT_GOOGLE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 이름은 Apple로 로그인이다`() {
        setLoginHomeScaffold()

        composeRule.onNodeWithContentDescription("Apple로 로그인").assert(hasClickAction())
    }

    @Test
    fun `기본 이름은 Sign in with Apple이다`() {
        setLoginHomeScaffold()

        composeRule.onNodeWithContentDescription(SCAFFOLD_TEST_DEFAULT_APPLE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-LOGIN-FEATURE-020 두 로그인 버튼을 함께 표시한다`() {
        setLoginHomeScaffold()

        composeRule.onNodeWithContentDescription(SCAFFOLD_TEST_DEFAULT_APPLE_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(SCAFFOLD_TEST_DEFAULT_GOOGLE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-LOGIN-FEATURE-014 진행 중 로딩 표시와 상단 바를 유지한다`() {
        val events = mutableListOf<LoginHomeScaffoldEvent>()
        setLoginHomeScaffold(
            uiStateProvider = { LoginHomeUiState(isInProgress = true) },
            onEvent = events::add,
        )

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithContentDescription(SCAFFOLD_TEST_DEFAULT_GOOGLE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(SCAFFOLD_TEST_DEFAULT_APPLE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(SCAFFOLD_TEST_DEFAULT_TITLE).assertExists()
        composeRule
            .onNodeWithContentDescription(SCAFFOLD_TEST_NAVIGATE_UP_DESCRIPTION)
            .assert(hasClickAction())
            .performClick()
        events shouldBe listOf(LoginHomeScaffoldEvent.ClickNavigateUp)
    }

    @Test
    fun `로그인 버튼을 선택하면 해당 수단의 이벤트를 보낸다`() {
        val events = mutableListOf<LoginHomeScaffoldEvent>()
        setLoginHomeScaffold(onEvent = events::add)

        composeRule.onNodeWithContentDescription(SCAFFOLD_TEST_DEFAULT_APPLE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithContentDescription(SCAFFOLD_TEST_DEFAULT_GOOGLE_BUTTON_DESCRIPTION).performClick()

        events shouldBe listOf(LoginHomeScaffoldEvent.ClickAppleSignIn, LoginHomeScaffoldEvent.ClickGoogleSignIn)
    }

    private fun setLoginHomeScaffold(
        uiStateProvider: () -> LoginHomeUiState = { LoginHomeUiState() },
        onEvent: (LoginHomeScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                LoginHomeScaffold(
                    uiStateProvider = uiStateProvider,
                    onEvent = onEvent,
                )
            }
        }
    }

    public companion object {
        private const val SCAFFOLD_TEST_DEFAULT_GOOGLE_BUTTON_DESCRIPTION = "Sign in with Google"
        private const val SCAFFOLD_TEST_DEFAULT_APPLE_BUTTON_DESCRIPTION = "Sign in with Apple"
        private const val SCAFFOLD_TEST_DEFAULT_TITLE = "Login"
        private const val SCAFFOLD_TEST_NAVIGATE_UP_DESCRIPTION = "Navigate up"
    }
}
