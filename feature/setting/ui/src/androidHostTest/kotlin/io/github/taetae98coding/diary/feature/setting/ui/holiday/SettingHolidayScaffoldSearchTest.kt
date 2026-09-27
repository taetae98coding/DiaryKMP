package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting
import io.github.taetae98coding.diary.feature.setting.ui.holiday.search.rememberSettingHolidaySearchResult
import io.github.taetae98coding.diary.library.coroutines.flow.INPUT_IDLE_DELAY
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingHolidayScaffoldSearchTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 검색어 입력과 검색 결과 없음 문구를 제공한다`() {
        composeRule.setSettingHolidayScaffold(
            uiState =
                loadedUiState(
                    holidaySettingList = listOf(holidaySetting(isHoliday = true, isVisible = true, name = SEOLLAL_HOLIDAY_NAME)),
                ),
        )

        composeRule.onNodeWithText(KOREAN_SEARCH_PLACEHOLDER).assertExists()

        composeRule.searchInputField().performTextInput(NO_MATCH_QUERY)

        composeRule.onNodeWithText(KOREAN_SEARCH_EMPTY).assertIsDisplayed()
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-021 검색어를 입력하면 일치하는 항목만 순서와 선택 상태 그대로 표시한다`() {
        val firstMatched = holidaySetting(isHoliday = true, isVisible = true, name = SEOLLAL_HOLIDAY_NAME)
        val secondMatched = holidaySetting(isHoliday = false, isVisible = false, name = "설날 대체 공휴일")
        val unmatched = holidaySetting(isHoliday = true, isVisible = true, name = MEMORIAL_DAY_NAME)
        composeRule.setSettingHolidayScaffold(
            uiState =
                loadedUiState(
                    holidaySettingList = listOf(firstMatched, unmatched, secondMatched),
                ),
        )

        composeRule.searchInputField().performTextInput(SEOLLAL_QUERY)

        composeRule.holidayItemNode(firstMatched.name).assertIsOn()
        composeRule.holidayItemNode(secondMatched.name).assertIsOff()
        composeRule.textNodeCount(unmatched.name) shouldBe 0
        composeRule.onNodeWithText(firstMatched.name).getUnclippedBoundsInRoot().top shouldBeLessThan
            composeRule.onNodeWithText(secondMatched.name).getUnclippedBoundsInRoot().top
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-044 검색어는 입력을 멈출 때까지 기다리지 않고 곧바로 목록에 반영된다`() {
        val matched = holidaySetting(isHoliday = true, isVisible = true, name = SEOLLAL_HOLIDAY_NAME)
        val unmatched = holidaySetting(isHoliday = true, isVisible = true, name = MEMORIAL_DAY_NAME)
        lateinit var state: SettingHolidayScaffoldState
        var searchResult: List<HolidaySetting> = emptyList()
        composeRule.setContent {
            state = rememberSettingHolidayScaffoldState()
            searchResult = rememberSettingHolidaySearchResult(query = state.query, holidaySettingList = listOf(matched, unmatched))
        }
        composeRule.runOnIdle { searchResult shouldBe listOf(matched, unmatched) }
        composeRule.mainClock.autoAdvance = false

        composeRule.runOnUiThread {
            state.queryState.setTextAndPlaceCursorAtEnd(SEOLLAL_QUERY)
            Snapshot.sendApplyNotifications()
        }
        composeRule.mainClock.advanceTimeBy(INPUT_IDLE_DELAY.inWholeMilliseconds / 2)

        composeRule.runOnUiThread { searchResult shouldBe listOf(matched) }
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-022 검색 결과가 없으면 빈 상태 안내를 표시하고 검색어를 수정할 수 있다`() {
        val holidaySetting = holidaySetting(isHoliday = true, isVisible = true, name = SEOLLAL_HOLIDAY_NAME)
        composeRule.setSettingHolidayScaffold(
            uiState = loadedUiState(holidaySettingList = listOf(holidaySetting)),
        )

        composeRule.onNodeWithText(DEFAULT_SEARCH_PLACEHOLDER).assertExists()

        composeRule.searchInputField().performTextInput(NO_MATCH_QUERY)

        composeRule.onNodeWithText(DEFAULT_SEARCH_EMPTY).assertIsDisplayed()
        composeRule.textNodeCount(holidaySetting.name) shouldBe 0

        composeRule.searchInputField().performTextReplacement(SEOLLAL_QUERY)

        composeRule.textNodeCount(DEFAULT_SEARCH_EMPTY) shouldBe 0
        composeRule.onNodeWithText(holidaySetting.name).assertIsDisplayed()
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-023 검색어를 모두 지우면 전체 목록으로 돌아간다`() {
        val matched = holidaySetting(isHoliday = true, isVisible = true, name = SEOLLAL_HOLIDAY_NAME)
        val unmatched = holidaySetting(isHoliday = false, isVisible = true, name = MEMORIAL_DAY_NAME)
        composeRule.setSettingHolidayScaffold(
            uiState = loadedUiState(holidaySettingList = listOf(matched, unmatched)),
        )

        composeRule.searchInputField().performTextInput(SEOLLAL_QUERY)
        composeRule.textNodeCount(unmatched.name) shouldBe 0

        composeRule.searchInputField().performTextClearance()

        composeRule.onNodeWithText(matched.name).assertIsDisplayed()
        composeRule.onNodeWithText(unmatched.name).assertIsDisplayed()
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-024 일괄 선택은 검색어가 비어 있을 때만 제공한다`() {
        composeRule.setSettingHolidayScaffold()

        composeRule.searchInputField().performTextInput(SEOLLAL_QUERY)

        composeRule.onNodeWithContentDescription(DEFAULT_BULK_ACTION_DESCRIPTION).assertDoesNotExist()

        composeRule.searchInputField().performTextClearance()

        composeRule.onNodeWithContentDescription(DEFAULT_BULK_ACTION_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-025 필터링된 목록에서도 선택 상태를 바꿀 수 있다`() {
        val holidaySetting = holidaySetting(isHoliday = true, isVisible = true, name = SEOLLAL_HOLIDAY_NAME)
        val eventList = mutableListOf<SettingHolidayScaffoldEvent>()
        val uiState = mutableStateOf(loadedUiState(holidaySettingList = listOf(holidaySetting)))
        composeRule.setContent {
            DiaryTheme {
                SettingHolidayScaffold(
                    onEvent = { event ->
                        eventList += event
                        uiState.value = loadedUiState(holidaySettingList = listOf(holidaySetting.copy(isVisible = false)))
                    },
                    uiStateProvider = { uiState.value },
                )
            }
        }

        composeRule.searchInputField().performTextInput(SEOLLAL_QUERY)
        composeRule.holidayItemNode(holidaySetting.name).performClick()

        eventList shouldBe listOf(SettingHolidayScaffoldEvent.ToggleHoliday(name = holidaySetting.name))
        composeRule.searchInputField().assert(hasText(SEOLLAL_QUERY))
        composeRule.holidayItemNode(holidaySetting.name).assertIsOff()
    }

    @Test
    fun `지우기 버튼을 누르면 검색어를 지우고 초점을 검색어 입력에 남긴다`() {
        composeRule.setSettingHolidayScaffold()

        composeRule.searchInputField().performTextInput(SEOLLAL_QUERY)
        composeRule.searchClearButton().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_SEARCH_PLACEHOLDER).assertExists()
        composeRule.searchInputField().assertIsFocused()
    }

    @Test
    fun `TC-SETTING-HOLIDAY-DOMAIN-011 공백뿐인 검색어는 검색어가 없는 것으로 본다`() {
        val holidaySettingList =
            listOf(
                holidaySetting(isHoliday = true, isVisible = true, name = SEOLLAL_HOLIDAY_NAME),
                holidaySetting(isHoliday = false, isVisible = true, name = MEMORIAL_DAY_NAME),
            )
        composeRule.setSettingHolidayScaffold(
            uiState = loadedUiState(holidaySettingList = holidaySettingList),
        )

        composeRule.searchInputField().performTextInput("   ")

        holidaySettingList.forEach { holidaySetting ->
            composeRule.scrollToText(holidaySetting.name)
            composeRule.onNodeWithText(holidaySetting.name).assertIsDisplayed()
        }
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-026 화면에 진입해도 검색어 입력에 초점을 두지 않는다`() {
        composeRule.setSettingHolidayScaffold()

        composeRule.waitForIdle()

        composeRule.searchInputField().assertIsNotFocused()
    }

    // 사라지는 목록이 걸러진 뒤의 빈 목록을 그리면 교차 없이 즉시 사라지므로, 안내가 나타난 시점에 항목이 남아 있는지 확인한다.
    @Test
    fun `검색 결과가 없어지는 전환 도중에도 사라지는 목록이 항목을 보여준다`() {
        composeRule.setSettingHolidayScaffold(
            uiState =
                loadedUiState(
                    holidaySettingList = listOf(holidaySetting(isHoliday = true, isVisible = true, name = SEOLLAL_HOLIDAY_NAME)),
                ),
        )
        composeRule.onNodeWithText(SEOLLAL_HOLIDAY_NAME).assertIsDisplayed()

        composeRule.mainClock.autoAdvance = false
        composeRule.searchInputField().performTextInput(NO_MATCH_QUERY)
        composeRule.mainClock.advanceTimeUntil { composeRule.textNodeCount(DEFAULT_SEARCH_EMPTY) == 1 }

        composeRule.onNodeWithText(SEOLLAL_HOLIDAY_NAME).assertExists()
    }

    companion object {
        private const val SEOLLAL_HOLIDAY_NAME = "설날 연휴"
        private const val MEMORIAL_DAY_NAME = "현충일"
        private const val SEOLLAL_QUERY = "설날"
        private const val NO_MATCH_QUERY = "추석"
    }
}
