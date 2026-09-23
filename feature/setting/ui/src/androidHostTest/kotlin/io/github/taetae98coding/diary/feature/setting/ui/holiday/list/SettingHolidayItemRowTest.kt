package io.github.taetae98coding.diary.feature.setting.ui.holiday.list

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting
import io.github.taetae98coding.diary.feature.setting.ui.holiday.DEFAULT_DAY_OFF_LABEL
import io.github.taetae98coding.diary.feature.setting.ui.holiday.KOREAN_DAY_OFF_LABEL
import io.github.taetae98coding.diary.feature.setting.ui.holiday.hasRole
import io.github.taetae98coding.diary.feature.setting.ui.holiday.holidaySetting
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingHolidayItemRowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-006 항목은 체크박스 역할과 선택 상태를 제공한다`() {
        val selectedHoliday = holidaySetting(isHoliday = true, isVisible = true)
        val deselectedHoliday = holidaySetting(isHoliday = false, isVisible = false)
        setHolidayItemRows(selectedHoliday, deselectedHoliday)

        composeRule
            .onNode(hasText(selectedHoliday.name) and hasRole(Role.Checkbox))
            .assertIsOn()
            .assert(hasClickAction())
        composeRule
            .onNode(hasText(deselectedHoliday.name) and hasRole(Role.Checkbox))
            .assertIsOff()
            .assert(hasClickAction())
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-007 기본 환경에서 쉬는 날인 항목만 문구와 접근성 정보를 제공한다`() {
        assertDayOffLabel(DEFAULT_DAY_OFF_LABEL)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-SETTING-HOLIDAY-FEATURE-007 한국어 환경에서 쉬는 날인 항목만 문구와 접근성 정보를 제공한다`() {
        assertDayOffLabel(KOREAN_DAY_OFF_LABEL)
    }

    private fun assertDayOffLabel(dayOffLabel: String) {
        val dayOff = holidaySetting(isHoliday = true, isVisible = true)
        val ordinaryDay = holidaySetting(isHoliday = false, isVisible = true)
        setHolidayItemRows(dayOff, ordinaryDay)

        composeRule
            .onNodeWithText(dayOffLabel, useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule
            .onNode(hasText(dayOff.name) and hasText(dayOffLabel))
            .assertExists()
        composeRule
            .onNode(hasText(ordinaryDay.name) and hasText(dayOffLabel))
            .assertDoesNotExist()
    }

    private fun setHolidayItemRows(
        vararg holidaySetting: HolidaySetting,
        onClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                Column {
                    holidaySetting.forEach { item ->
                        SettingHolidayItemRow(
                            holidaySetting = item,
                            onClick = onClick,
                        )
                    }
                }
            }
        }
    }
}
