package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingHolidayScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        composeRule.setSettingHolidayScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `기본 환경에서 뒤로가기 접근성 이름을 제공한다`() {
        composeRule.setSettingHolidayScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-006 저장된 공휴일과 각 선택 상태를 표시한다`() {
        val selectedHolidayList =
            listOf(
                holidaySetting(isHoliday = true, isVisible = true),
                holidaySetting(isHoliday = false, isVisible = true),
            )
        val deselectedHoliday = holidaySetting(isHoliday = false, isVisible = false)
        composeRule.setSettingHolidayScaffold(
            uiState =
                SettingHolidayUiState.Loaded(
                    holidaySettingList = selectedHolidayList + deselectedHoliday,
                ),
        )

        selectedHolidayList.forEach { holidaySetting ->
            composeRule
                .onNode(hasText(holidaySetting.name) and hasRole(Role.Checkbox))
                .assertIsOn()
        }
        composeRule
            .onNode(hasText(deselectedHoliday.name) and hasRole(Role.Checkbox))
            .assertIsOff()
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-020 목록을 스크롤해도 일괄 선택 동작을 사용할 수 있다`() {
        val holidaySettingList =
            List(SCROLLABLE_HOLIDAY_COUNT) { holidaySetting(isHoliday = true, isVisible = true) }
        val eventList = mutableListOf<SettingHolidayScaffoldEvent>()
        composeRule.setSettingHolidayScaffold(
            uiState = SettingHolidayUiState.Loaded(holidaySettingList = holidaySettingList),
            onEvent = eventList::add,
        )

        composeRule
            .onNode(hasScrollToNodeAction())
            .performScrollToNode(hasText(holidaySettingList.last().name))
        composeRule.onNodeWithContentDescription(DEFAULT_BULK_ACTION_DESCRIPTION).assertIsDisplayed()
        composeRule.expandBulkAction()

        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_DESELECT_ALL).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_SELECT_DAYS_OFF).performClick()

        eventList shouldBe listOf(SettingHolidayScaffoldEvent.ClickSelectDaysOff)
    }

    @Test
    fun `공휴일 항목을 선택하면 해당 key의 전환 이벤트를 전달한다`() {
        val holidaySetting = holidaySetting(isHoliday = true, isVisible = true)
        val eventList = mutableListOf<SettingHolidayScaffoldEvent>()
        composeRule.setSettingHolidayScaffold(
            uiState = SettingHolidayUiState.Loaded(holidaySettingList = listOf(holidaySetting)),
            onEvent = eventList::add,
        )

        composeRule
            .onNode(hasText(holidaySetting.name) and hasRole(Role.Checkbox))
            .performClick()

        eventList shouldBe listOf(SettingHolidayScaffoldEvent.ToggleHoliday(key = holidaySetting.key))
    }

    companion object {
        private const val SCROLLABLE_HOLIDAY_COUNT = 30
    }
}
