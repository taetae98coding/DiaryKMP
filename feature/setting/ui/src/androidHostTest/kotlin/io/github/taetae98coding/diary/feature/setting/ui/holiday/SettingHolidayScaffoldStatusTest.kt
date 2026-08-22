package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
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
class SettingHolidayScaffoldStatusTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-013 확인 중에는 진행 상태만 표시하고 선택할 항목이 없다`() {
        composeRule.setSettingHolidayScaffold(uiState = SettingHolidayUiState.Loading)

        composeRule.onNodeWithContentDescription(DEFAULT_LOADING_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_DESELECT_ALL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SELECT_DAYS_OFF).assertDoesNotExist()
        composeRule.onAllNodes(hasRole(Role.Checkbox)).assertCountEquals(0)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-SETTING-HOLIDAY-FEATURE-013 한국어 확인 중 접근성 이름을 표시한다`() {
        composeRule.setSettingHolidayScaffold(uiState = SettingHolidayUiState.Loading)

        composeRule.onNodeWithContentDescription(KOREAN_LOADING_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-032 확인 중에도 일괄 선택 동작을 펼쳐 실행할 수 있다`() {
        val eventList = mutableListOf<SettingHolidayScaffoldEvent>()
        composeRule.setSettingHolidayScaffold(
            uiState = SettingHolidayUiState.Loading,
            onEvent = eventList::add,
        )

        composeRule.expandBulkAction()

        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_DESELECT_ALL).assert(hasClickAction())
        composeRule.onNodeWithText(DEFAULT_SELECT_DAYS_OFF).performClick()

        eventList shouldBe listOf(SettingHolidayScaffoldEvent.ClickSelectDaysOff)
    }
}
