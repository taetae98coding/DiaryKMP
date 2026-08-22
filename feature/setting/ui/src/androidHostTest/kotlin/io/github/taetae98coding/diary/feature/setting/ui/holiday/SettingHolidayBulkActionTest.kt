package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingHolidayBulkActionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `TC-SETTING-HOLIDAY-FEATURE-005 한국어 환경에서 동작 버튼을 펼치면 일괄 선택 문구를 표시한다`() {
        composeRule.setSettingHolidayScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_BULK_ACTION_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_SELECT_ALL).assert(hasClickAction())
        composeRule.onNodeWithText(KOREAN_DESELECT_ALL).assert(hasClickAction())
        composeRule.onNodeWithText(KOREAN_SELECT_DAYS_OFF).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(KOREAN_BULK_ACTION_CLOSE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-005 기본 환경에서 동작 버튼을 펼치면 일괄 선택 문구를 표시한다`() {
        composeRule.setSettingHolidayScaffold()

        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).assertDoesNotExist()

        composeRule.expandBulkAction()

        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_DESELECT_ALL).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_SELECT_DAYS_OFF).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_BULK_ACTION_CLOSE_DESCRIPTION).assertExists()
    }

    @Test
    fun `세 일괄 선택을 선택하면 각각의 이벤트를 전달한다`() {
        val eventList = mutableListOf<SettingHolidayScaffoldEvent>()
        composeRule.setSettingHolidayScaffold(onEvent = eventList::add)

        composeRule.expandBulkAction()
        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).performClick()
        composeRule.expandBulkAction()
        composeRule.onNodeWithText(DEFAULT_DESELECT_ALL).performClick()
        composeRule.expandBulkAction()
        composeRule.onNodeWithText(DEFAULT_SELECT_DAYS_OFF).performClick()

        eventList shouldBe
            listOf(
                SettingHolidayScaffoldEvent.ClickSelectAll,
                SettingHolidayScaffoldEvent.ClickDeselectAll,
                SettingHolidayScaffoldEvent.ClickSelectDaysOff,
            )
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-030 일괄 선택 동작을 고르면 펼친 동작을 접는다`() {
        composeRule.setSettingHolidayScaffold()

        composeRule.expandBulkAction()
        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_DESELECT_ALL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SELECT_DAYS_OFF).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_BULK_ACTION_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-031 동작 버튼을 다시 선택하면 아무 동작도 전달하지 않고 접는다`() {
        val eventList = mutableListOf<SettingHolidayScaffoldEvent>()
        composeRule.setSettingHolidayScaffold(onEvent = eventList::add)

        composeRule.expandBulkAction()
        composeRule.onNodeWithContentDescription(DEFAULT_BULK_ACTION_CLOSE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_DESELECT_ALL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SELECT_DAYS_OFF).assertDoesNotExist()
        eventList shouldBe emptyList()
    }
}
