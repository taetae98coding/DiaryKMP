package io.github.taetae98coding.diary.feature.setting.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `뒤로가기 동작을 선택하면 화면 전환을 한 번 요청한다`() {
        var navigateUpCount = 0
        setSettingHomeScreen(navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-adb9f475-c36e-422f-9fd3-c87d3b48faf1 공휴일 항목을 선택하면 SettingHoliday 화면으로 이동한다`() {
        assertNavigatesOnlyTo(itemLabel = DEFAULT_HOLIDAY_ITEM_LABEL, expected = SettingHomeItem.HOLIDAY)
    }

    @Test
    fun `TC-b8935a44-5fd9-4439-b908-b81b2378503a 지도 항목을 선택하면 SettingMap 화면으로 이동한다`() {
        assertNavigatesOnlyTo(itemLabel = DEFAULT_MAP_ITEM_LABEL, expected = SettingHomeItem.MAP)
    }

    @Test
    fun `TC-24b65985-c74c-45dd-8b59-f3e604b1a0e2 Gemini 항목을 선택하면 SettingGemini 화면으로 이동한다`() {
        assertNavigatesOnlyTo(itemLabel = DEFAULT_GEMINI_ITEM_LABEL, expected = SettingHomeItem.GEMINI)
    }

    @Test
    fun `TC-0f6496a7-770e-4119-ab1b-5525f71ff982 파일 항목을 선택하면 SettingFile 화면으로 이동한다`() {
        assertNavigatesOnlyTo(itemLabel = DEFAULT_FILE_ITEM_LABEL, expected = SettingHomeItem.FILE)
    }

    private fun assertNavigatesOnlyTo(
        itemLabel: String,
        expected: SettingHomeItem,
    ) {
        val navigatedItemList = mutableListOf<SettingHomeItem>()
        setSettingHomeScreen(
            navigateToHoliday = { navigatedItemList += SettingHomeItem.HOLIDAY },
            navigateToMap = { navigatedItemList += SettingHomeItem.MAP },
            navigateToGemini = { navigatedItemList += SettingHomeItem.GEMINI },
            navigateToFile = { navigatedItemList += SettingHomeItem.FILE },
        )

        composeRule.onNodeWithText(itemLabel).performClick()
        composeRule.waitForIdle()

        navigatedItemList shouldBe listOf(expected)
    }

    private fun setSettingHomeScreen(
        navigateUp: () -> Unit = {},
        navigateToHoliday: () -> Unit = {},
        navigateToMap: () -> Unit = {},
        navigateToGemini: () -> Unit = {},
        navigateToFile: () -> Unit = {},
        viewModel: SettingHomeViewModel = screenTestViewModel(SettingHomeUiState.Content(itemList = settingHomeItemList)),
    ) {
        composeRule.setContent {
            DiaryTheme {
                SettingHomeScreen(
                    navigateUp = navigateUp,
                    navigateToHoliday = navigateToHoliday,
                    navigateToMap = navigateToMap,
                    navigateToGemini = navigateToGemini,
                    navigateToFile = navigateToFile,
                    viewModel = viewModel,
                )
            }
        }
    }

    public companion object {
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_HOLIDAY_ITEM_LABEL = "Holiday"
        private const val DEFAULT_MAP_ITEM_LABEL = "Map"
        private const val DEFAULT_GEMINI_ITEM_LABEL = "Gemini"
        private const val DEFAULT_FILE_ITEM_LABEL = "Files"

        private fun screenTestViewModel(uiState: SettingHomeUiState): SettingHomeViewModel {
            val viewModel = mockk<SettingHomeViewModel>()
            every { viewModel.uiState } returns MutableStateFlow(uiState)
            return viewModel
        }
    }
}
