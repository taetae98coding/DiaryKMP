package io.github.taetae98coding.diary.feature.setting.ui.map

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.kotest.matchers.shouldBe
import io.mockk.every
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
class SettingMapScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SETTING-MAP-FEATURE-003 뒤로가기 동작을 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setSettingMapScreen(
            viewModel = screenTestViewModel(SettingMapUiState.Loaded(defaultProvider = MapProvider.NAVER)),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-SETTING-MAP-FEATURE-008 선택되어 있지 않은 줄을 선택하면 기본 지도가 저장된다`() {
        var navigateUpCount = 0
        val viewModel = screenTestViewModel(SettingMapUiState.Loaded(defaultProvider = MapProvider.NAVER))
        every { viewModel.selectDefaultProvider(any()) } returns Unit
        setSettingMapScreen(
            viewModel = viewModel,
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.selectDefaultProvider(provider = MapProvider.GOOGLE) }
        navigateUpCount shouldBe 0
    }

    private fun setSettingMapScreen(
        viewModel: SettingMapViewModel,
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingMapScreen(
                    navigateUp = navigateUp,
                    viewModel = viewModel,
                    componentVisibleProvider = { SettingMapScaffoldComponentVisible() },
                )
            }
        }
    }

    public companion object {
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_GOOGLE_LABEL = "Google"

        private fun screenTestViewModel(uiState: SettingMapUiState): SettingMapViewModel {
            val viewModel = mockk<SettingMapViewModel>()
            every { viewModel.uiState } returns MutableStateFlow(uiState)
            return viewModel
        }
    }
}
