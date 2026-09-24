package io.github.taetae98coding.diary.feature.setting.ui.download

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ADDRESS = "http://192.168.0.10:27180"
private const val OTHER_ADDRESS = "http://10.0.0.5:27180"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingDownloadScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-014 뒤로가기 동작을 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setScreen(navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-009 저장을 실행하면 입력한 주소로 저장을 요청한다`() {
        val viewModel = viewModel(SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting(address = ADDRESS)))
        setScreen(viewModel = viewModel)

        composeRule.onNodeWithText(ADDRESS).performTextReplacement(OTHER_ADDRESS)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.save(setting = MusicDownloadProxySetting(address = OTHER_ADDRESS)) }
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-016 저장하지 않고 뒤로가면 확인 없이 떠나고 저장하지 않는다`() {
        var navigateUpCount = 0
        val viewModel = viewModel(SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting(address = ADDRESS)))
        setScreen(viewModel = viewModel, navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithText(ADDRESS).performTextReplacement(OTHER_ADDRESS)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        verify(exactly = 0) { viewModel.save(setting = any()) }
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-012 저장에 성공하면 성공을 알린다`() {
        val viewModel =
            viewModel(
                uiState = SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting(address = ADDRESS)),
                effect = SettingDownloadEffect.SaveSucceeded,
            )
        setScreen(viewModel = viewModel)

        composeRule.onNodeWithText(DEFAULT_SAVE_SUCCEEDED_MESSAGE).assertExists()
    }

    @Test
    fun `TC-SETTING-DOWNLOAD-FEATURE-013 저장에 실패하면 실패를 알린다`() {
        val viewModel =
            viewModel(
                uiState = SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting(address = ADDRESS)),
                effect = SettingDownloadEffect.SaveFailed,
            )
        setScreen(viewModel = viewModel)

        composeRule.onNodeWithText(DEFAULT_SAVE_FAILED_MESSAGE).assertExists()
    }

    private fun setScreen(
        viewModel: SettingDownloadViewModel = viewModel(SettingDownloadUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY)),
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingDownloadScreen(
                    navigateUp = navigateUp,
                    componentVisibleProvider = { SettingDownloadScaffoldComponentVisible() },
                    viewModel = viewModel,
                )
            }
        }
    }

    public companion object {
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_SAVE_DESCRIPTION = "Save"
        private const val DEFAULT_SAVE_SUCCEEDED_MESSAGE = "Saved."
        private const val DEFAULT_SAVE_FAILED_MESSAGE = "Couldn't save."

        private fun viewModel(
            uiState: SettingDownloadUiState,
            effect: SettingDownloadEffect? = null,
        ): SettingDownloadViewModel =
            mockk(relaxed = true) {
                every { this@mockk.uiState } returns MutableStateFlow(uiState)
                every { this@mockk.effect } returns if (effect == null) emptyFlow() else flowOf(effect)
            }
    }
}
