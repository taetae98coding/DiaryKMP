package io.github.taetae98coding.diary.feature.setting.ui

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.feature.setting.ui.holiday.SettingHolidayScaffold
import io.github.taetae98coding.diary.feature.setting.ui.holiday.SettingHolidayScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.setting.ui.holiday.SettingHolidayUiState
import io.github.taetae98coding.diary.feature.setting.ui.map.SettingMapScaffold
import io.github.taetae98coding.diary.feature.setting.ui.map.SettingMapScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.setting.ui.map.SettingMapUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingDetailScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `공휴일 상세는 뒤로가기 버튼을 숨긴다`() {
        composeRule.setContent {
            DiaryTheme {
                SettingHolidayScaffold(
                    uiStateProvider = { SettingHolidayUiState.Loaded(holidaySettingList = emptyList()) },
                    onEvent = {},
                    componentVisibleProvider = {
                        SettingHolidayScaffoldComponentVisible(isNavigateUpButtonVisible = false)
                    },
                )
            }
        }

        composeRule.onNodeWithText("Search for a holiday").assertExists()
        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `지도 상세는 뒤로가기 버튼을 숨긴다`() {
        composeRule.setContent {
            DiaryTheme {
                SettingMapScaffold(
                    uiStateProvider = { SettingMapUiState.Loaded(defaultProvider = MapProvider.NAVER) },
                    onEvent = {},
                    componentVisibleProvider = {
                        SettingMapScaffoldComponentVisible(isNavigateUpButtonVisible = false)
                    },
                )
            }
        }

        composeRule.onNodeWithText("Map Settings").assertExists()
        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).assertDoesNotExist()
    }

    public companion object {
        private const val NAVIGATE_UP_DESCRIPTION = "Navigate up"
    }
}
