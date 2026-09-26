package io.github.taetae98coding.diary.feature.setting.ui.browser

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.core.testing.browser.chromeProfileList
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingBrowserScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SETTING-BROWSER-FEATURE-001 바꾼 선택을 되돌리지 않고 뒤로가기 동작으로 이전 화면으로 돌아간다`() {
        val profile = fixtureMonkey.chromeProfileList(size = 1).single()
        var navigateUpCount = 0
        val viewModel = screenTestViewModel(profile = profile, selectedProfileDirectory = "")
        setSettingBrowserScreen(
            viewModel = viewModel,
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithText(profile.name).performClick()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        verify(exactly = 1) { viewModel.selectProfile(directory = profile.directory) }
        verify(exactly = 0) { viewModel.unselectProfile() }
    }

    @Test
    fun `TC-SETTING-BROWSER-FEATURE-004 프로필을 고르면 그 프로필 선택을 한 번 실행한다`() {
        val profile = fixtureMonkey.chromeProfileList(size = 1).single()
        val viewModel = screenTestViewModel(profile = profile, selectedProfileDirectory = "")
        setSettingBrowserScreen(viewModel = viewModel)

        composeRule.onNodeWithText(profile.name).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.selectProfile(directory = profile.directory) }
        verify(exactly = 0) { viewModel.unselectProfile() }
    }

    @Test
    fun `TC-SETTING-BROWSER-FEATURE-004 선택 안 함을 고르면 선택 해제를 한 번 실행한다`() {
        val profile = fixtureMonkey.chromeProfileList(size = 1).single()
        val viewModel = screenTestViewModel(profile = profile, selectedProfileDirectory = profile.directory)
        setSettingBrowserScreen(viewModel = viewModel)

        composeRule.onNodeWithText(DEFAULT_NONE_LABEL).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.unselectProfile() }
        verify(exactly = 0) { viewModel.selectProfile(directory = any()) }
    }

    private fun setSettingBrowserScreen(
        viewModel: SettingBrowserViewModel,
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingBrowserScreen(
                    navigateUp = navigateUp,
                    componentVisibleProvider = { SettingBrowserScaffoldComponentVisible() },
                    viewModel = viewModel,
                )
            }
        }
    }

    public companion object {
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_NONE_LABEL = "None"
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun screenTestViewModel(
            profile: ChromeProfile,
            selectedProfileDirectory: String,
        ): SettingBrowserViewModel {
            val viewModel = mockk<SettingBrowserViewModel>()
            every { viewModel.uiState } returns
                MutableStateFlow(SettingBrowserUiState.Loaded(profileList = listOf(profile), selectedProfileDirectory = selectedProfileDirectory))
            justRun { viewModel.selectProfile(directory = any()) }
            justRun { viewModel.unselectProfile() }
            return viewModel
        }
    }
}
