package io.github.taetae98coding.diary.feature.more.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MoreHomeScaffoldSettingTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 설정 동작 접근성 이름을 제공한다`() {
        setMoreHomeScaffold()

        composeRule.onNodeWithContentDescription("설정").assert(hasClickAction())
    }

    @Test
    fun `기본 환경에서 설정 동작 접근성 이름을 제공한다`() {
        setMoreHomeScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_SETTING_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-MORE-HOME-FEATURE-012 계정 상태와 관계없이 설정 동작을 표시한다`() {
        val accountUiState = mutableStateOf<MoreHomeAccountUiState>(MoreHomeAccountUiState.Loading)
        setMoreHomeScaffold(accountUiStateProvider = { accountUiState.value })

        ACCOUNT_UI_STATES.forEach { uiState ->
            composeRule.runOnIdle { accountUiState.value = uiState }

            composeRule.onNodeWithContentDescription(DEFAULT_SETTING_DESCRIPTION).assert(hasClickAction())
        }
    }

    private fun setMoreHomeScaffold(
        accountUiStateProvider: () -> MoreHomeAccountUiState = { MoreHomeAccountUiState.Guest },
        onEvent: (MoreHomeScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                MoreHomeScaffold(
                    accountUiStateProvider = accountUiStateProvider,
                    onEvent = onEvent,
                )
            }
        }
    }

    public companion object {
        private const val DEFAULT_SETTING_DESCRIPTION = "Settings"
        private const val USER_EMAIL = "diary@example.com"
        private val ACCOUNT_UI_STATES =
            listOf(
                MoreHomeAccountUiState.Loading,
                MoreHomeAccountUiState.Guest,
                MoreHomeAccountUiState.User(profileImage = null, email = USER_EMAIL),
            )
    }
}
