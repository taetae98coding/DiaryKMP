package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingHolidayBulkActionRetentionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-039 검색어를 지워 동작 버튼이 다시 나타나면 접힌 상태다`() {
        val holidaySetting = holidaySetting(isHoliday = true, isVisible = true)
        composeRule.setSettingHolidayScaffold(uiState = loadedUiState(holidaySettingList = listOf(holidaySetting)))

        composeRule.expandBulkAction()
        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).assertExists()

        composeRule.searchInputField().performTextInput(holidaySetting.name)
        composeRule.waitForIdle()
        composeRule.searchInputField().performTextClearance()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_BULK_ACTION_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_DESELECT_ALL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SELECT_DAYS_OFF).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-041 화면이 재생성되면 펼쳐 둔 일괄 선택이 접힌다`() {
        val uiState = loadedUiState(holidaySettingList = listOf(holidaySetting(isHoliday = true, isVisible = true)))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                SettingHolidayScaffold(
                    onEvent = {},
                    uiStateProvider = { uiState },
                )
            }
        }

        composeRule.expandBulkAction()
        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_BULK_ACTION_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_DESELECT_ALL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SELECT_DAYS_OFF).assertDoesNotExist()
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-043 화면이 재생성되어도 검색어와 필터링된 목록이 유지된다`() {
        val matched = holidaySetting(isHoliday = true, isVisible = true)
        val unmatched = holidaySetting(isHoliday = true, isVisible = true)
        val uiState = loadedUiState(holidaySettingList = listOf(matched, unmatched))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                SettingHolidayScaffold(
                    onEvent = {},
                    uiStateProvider = { uiState },
                )
            }
        }

        composeRule.searchInputField().performTextInput(matched.name)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.searchInputField().assert(hasText(matched.name))
        composeRule.holidayItemNode(matched.name).assertExists()
        composeRule.holidayItemNode(unmatched.name).assertDoesNotExist()
    }
}
