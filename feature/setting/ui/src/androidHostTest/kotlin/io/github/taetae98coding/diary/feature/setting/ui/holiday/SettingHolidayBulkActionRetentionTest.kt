package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
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
    fun `TC-467569dd-e127-4197-94db-8290f6f908f5 검색어를 지워 동작 버튼이 다시 나타나면 접힌 상태다`() {
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
    fun `TC-aff3cf1f-05fc-4f81-959e-229e22962593 화면이 재생성되면 펼쳐 둔 일괄 선택이 접힌다`() {
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
    fun `TC-3fa99871-bc87-442e-b30d-05fd0fbb46bb 화면에 다시 들어오거나 앱을 다시 시작하면 일괄 선택이 접힌 상태로 시작한다`() {
        val uiState = loadedUiState(holidaySettingList = listOf(holidaySetting(isHoliday = true, isVisible = true)))
        var nextViewModel = screenTestViewModel(uiState = uiState)
        var launchCount by mutableIntStateOf(0)
        // 화면 재진입과 앱 재시작은 모두 저장된 화면 상태와 ViewModel이 사라지므로, 새 ViewModel로 저장 상태 없이 화면을 다시 구성한다.
        composeRule.setContent {
            DiaryTheme {
                key(launchCount) {
                    val viewModel = remember { nextViewModel }
                    SettingHolidayScreen(
                        navigateUp = {},
                        componentVisibleProvider = { SettingHolidayScaffoldComponentVisible() },
                        viewModel = viewModel,
                    )
                }
            }
        }
        composeRule.expandBulkAction()
        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).assertExists()
        nextViewModel = screenTestViewModel(uiState = uiState)

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_BULK_ACTION_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DEFAULT_SELECT_ALL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_DESELECT_ALL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SELECT_DAYS_OFF).assertDoesNotExist()
    }

    @Test
    fun `TC-2102865f-7e39-4fd2-8507-8b05da0d2b3f 화면이 재생성되어도 검색어와 필터링된 목록이 유지된다`() {
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

    @Test
    fun `TC-54f2ed9a-f024-4dc4-bd09-2b772280745c 앱이 백그라운드에 다녀와도 검색어와 필터링된 목록이 유지된다`() {
        val matched = holidaySetting(isHoliday = true, isVisible = true)
        val unmatched = holidaySetting(isHoliday = true, isVisible = true)
        val viewModel = screenTestViewModel(uiState = loadedUiState(holidaySettingList = listOf(matched, unmatched)))
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                    SettingHolidayScreen(
                        navigateUp = {},
                        componentVisibleProvider = { SettingHolidayScaffoldComponentVisible() },
                        viewModel = viewModel,
                    )
                }
            }
        }
        composeRule.searchInputField().performTextInput(matched.name)
        composeRule.waitForIdle()

        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START) }
        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME) }
        composeRule.waitForIdle()

        composeRule.searchInputField().assert(hasText(matched.name))
        composeRule.holidayItemNode(matched.name).assertExists()
        composeRule.holidayItemNode(unmatched.name).assertDoesNotExist()
    }

    @Test
    fun `TC-e96d3479-a629-4f8f-8bc1-73ca13177f96 화면에 다시 들어오거나 앱을 다시 시작하면 빈 검색어로 시작한다`() {
        val matched = holidaySetting(isHoliday = true, isVisible = true)
        val unmatched = holidaySetting(isHoliday = true, isVisible = true)
        val uiState = loadedUiState(holidaySettingList = listOf(matched, unmatched))
        var nextViewModel = screenTestViewModel(uiState = uiState)
        var launchCount by mutableIntStateOf(0)
        // 화면 재진입과 앱 재시작은 모두 저장된 화면 상태와 ViewModel이 사라지므로, 새 ViewModel로 저장 상태 없이 화면을 다시 구성한다.
        composeRule.setContent {
            DiaryTheme {
                key(launchCount) {
                    val viewModel = remember { nextViewModel }
                    SettingHolidayScreen(
                        navigateUp = {},
                        componentVisibleProvider = { SettingHolidayScaffoldComponentVisible() },
                        viewModel = viewModel,
                    )
                }
            }
        }
        composeRule.searchInputField().performTextInput(matched.name)
        composeRule.waitForIdle()
        composeRule.holidayItemNode(unmatched.name).assertDoesNotExist()
        nextViewModel = screenTestViewModel(uiState = uiState)

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule
            .searchInputField()
            .fetchSemanticsNode()
            .config[SemanticsProperties.EditableText]
            .text shouldBe ""
        composeRule.scrollToText(matched.name)
        composeRule.holidayItemNode(matched.name).assertExists()
        composeRule.scrollToText(unmatched.name)
        composeRule.holidayItemNode(unmatched.name).assertExists()
    }
}
