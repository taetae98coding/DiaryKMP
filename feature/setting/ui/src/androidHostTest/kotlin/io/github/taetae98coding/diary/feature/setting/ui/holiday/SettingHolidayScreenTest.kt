package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.kotest.matchers.shouldBe
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingHolidayScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-003 뒤로가기 동작을 선택하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        val viewModel =
            screenTestViewModel(
                uiState =
                    SettingHolidayUiState.Loaded(
                        holidaySettingList = listOf(holidaySetting(isHoliday = true, isVisible = true)),
                    ),
            )
        composeRule.setSettingHolidayScreen(
            viewModel = viewModel,
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `공휴일 항목 선택 이벤트를 ViewModel에 전달한다`() {
        val holidaySetting = holidaySetting(isHoliday = true, isVisible = true)
        val viewModel =
            screenTestViewModel(
                uiState = SettingHolidayUiState.Loaded(holidaySettingList = listOf(holidaySetting)),
            )
        composeRule.setSettingHolidayScreen(viewModel = viewModel)

        composeRule
            .onNode(hasText(holidaySetting.name) and hasRole(Role.Checkbox))
            .performClick()

        verify(exactly = 1) { viewModel.toggleHoliday(key = holidaySetting.key) }
    }

    @Test
    fun `일괄 선택 이벤트를 ViewModel의 각 동작에 전달한다`() {
        val viewModel =
            screenTestViewModel(
                uiState =
                    SettingHolidayUiState.Loaded(
                        holidaySettingList = listOf(holidaySetting(isHoliday = true, isVisible = true)),
                    ),
            )
        composeRule.setSettingHolidayScreen(viewModel = viewModel)

        composeRule.expandBulkAction()
        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).performClick()
        composeRule.expandBulkAction()
        composeRule.onNodeWithText(DEFAULT_DESELECT_ALL).performClick()
        composeRule.expandBulkAction()
        composeRule.onNodeWithText(DEFAULT_SELECT_DAYS_OFF).performClick()

        verify(exactly = 1) { viewModel.selectAll() }
        verify(exactly = 1) { viewModel.deselectAll() }
        verify(exactly = 1) { viewModel.selectDaysOff() }
    }
}
