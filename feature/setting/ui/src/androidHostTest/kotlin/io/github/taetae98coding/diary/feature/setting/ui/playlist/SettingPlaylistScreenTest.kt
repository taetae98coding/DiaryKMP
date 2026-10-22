package io.github.taetae98coding.diary.feature.setting.ui.playlist

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ADDRESS = "http://192.168.0.10:27180"
private const val OTHER_ADDRESS = "http://10.0.0.5:27180"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingPlaylistScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-74dc722a-89cb-4ad7-ad07-c14e9514261e 뒤로가기 동작을 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setScreen(navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-add84e31-1f7d-4282-8e59-4f2c9483eb3c 저장을 실행하면 입력한 주소로 저장을 요청한다`() {
        val viewModel = viewModel(SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting(address = ADDRESS)))
        setScreen(viewModel = viewModel)

        composeRule.onNodeWithText(ADDRESS).performTextReplacement(OTHER_ADDRESS)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.save(setting = MusicDownloadProxySetting(address = OTHER_ADDRESS)) }
    }

    @Test
    fun `TC-885785ac-394d-4cd6-8fe0-cdbe5471ec98 저장하지 않고 뒤로가면 확인 없이 떠나고 저장하지 않는다`() {
        var navigateUpCount = 0
        val viewModel = viewModel(SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting(address = ADDRESS)))
        setScreen(viewModel = viewModel, navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithText(ADDRESS).performTextReplacement(OTHER_ADDRESS)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        verify(exactly = 0) { viewModel.save(setting = any()) }
    }

    @Test
    fun `TC-7fa11d70-a289-4ad6-9e49-6755b5eac1e5 저장에 성공하면 입력을 유지하고 성공을 알린다`() {
        val uiState = MutableStateFlow<SettingPlaylistUiState>(SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting(address = ADDRESS)))
        val effect = MutableSharedFlow<SettingPlaylistEffect>(extraBufferCapacity = 1)
        setScreen(viewModel = viewModel(uiState = uiState, effect = effect))

        composeRule.onNodeWithText(ADDRESS).performTextReplacement(OTHER_ADDRESS)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).performClick()
        composeRule.runOnIdle {
            uiState.value = SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting(address = OTHER_ADDRESS))
            effect.tryEmit(SettingPlaylistEffect.SaveSucceeded)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_SAVE_SUCCEEDED_MESSAGE).assertExists()
        composeRule.onNodeWithText(OTHER_ADDRESS).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-2817b1b7-db7b-4300-85ee-fb49c9b2f54c 저장에 실패하면 입력을 유지하고 실패를 알린다`() {
        val uiState = MutableStateFlow<SettingPlaylistUiState>(SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting(address = ADDRESS)))
        val effect = MutableSharedFlow<SettingPlaylistEffect>(extraBufferCapacity = 1)
        setScreen(viewModel = viewModel(uiState = uiState, effect = effect))

        composeRule.onNodeWithText(ADDRESS).performTextReplacement(OTHER_ADDRESS)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).performClick()
        composeRule.runOnIdle { effect.tryEmit(SettingPlaylistEffect.SaveFailed) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_SAVE_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(OTHER_ADDRESS).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-010790ad-0dda-4a9d-8a8a-57d320cf68ba 다른 앱에 다녀와도 입력 중이던 주소를 유지한다`() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        val viewModel = viewModel(SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting(address = ADDRESS)))
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DiaryTheme {
                    SettingPlaylistScreen(
                        navigateUp = {},
                        componentVisibleProvider = { SettingPlaylistScaffoldComponentVisible() },
                        viewModel = viewModel,
                    )
                }
            }
        }

        composeRule.onNodeWithText(ADDRESS).performTextReplacement(OTHER_ADDRESS)
        composeRule.waitForIdle()
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(OTHER_ADDRESS).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()
        verify(exactly = 0) { viewModel.save(setting = any()) }
    }

    // 앱을 다시 실행하면 저장해 둔 화면 상태도, 화면 상태를 들고 있던 객체도 없으므로 새 키와 새 인스턴스로 같은 화면을 다시 구성한다.
    @Test
    fun `TC-f49b0a4c-709a-4496-a0b7-cf8cf3613a76 앱을 종료한 뒤 새로 시작하면 저장된 주소로 시작한다`() {
        val firstViewModel = viewModel(SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting(address = ADDRESS)))
        val relaunchedViewModel = viewModel(SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting(address = ADDRESS)))
        var launchCount by mutableIntStateOf(0)
        composeRule.setContent {
            key(launchCount) {
                DiaryTheme {
                    SettingPlaylistScreen(
                        navigateUp = {},
                        componentVisibleProvider = { SettingPlaylistScaffoldComponentVisible() },
                        viewModel = if (launchCount == 0) firstViewModel else relaunchedViewModel,
                    )
                }
            }
        }
        composeRule.onNodeWithText(ADDRESS).performTextReplacement(OTHER_ADDRESS)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertExists()

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(ADDRESS).assertExists()
        composeRule.onNodeWithText(OTHER_ADDRESS).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_SAVE_DESCRIPTION).assertDoesNotExist()
        verify(exactly = 0) { firstViewModel.save(setting = any()) }
    }

    private fun setScreen(
        viewModel: SettingPlaylistViewModel = viewModel(SettingPlaylistUiState.Consumer(setting = MusicDownloadProxySetting.EMPTY)),
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingPlaylistScreen(
                    navigateUp = navigateUp,
                    componentVisibleProvider = { SettingPlaylistScaffoldComponentVisible() },
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

        private fun viewModel(uiState: SettingPlaylistUiState): SettingPlaylistViewModel =
            mockk(relaxed = true) {
                every { this@mockk.uiState } returns MutableStateFlow(uiState)
                every { this@mockk.effect } returns emptyFlow()
            }

        private fun viewModel(
            uiState: StateFlow<SettingPlaylistUiState>,
            effect: Flow<SettingPlaylistEffect>,
        ): SettingPlaylistViewModel =
            mockk(relaxed = true) {
                every { this@mockk.uiState } returns uiState
                every { this@mockk.effect } returns effect
            }
    }
}
