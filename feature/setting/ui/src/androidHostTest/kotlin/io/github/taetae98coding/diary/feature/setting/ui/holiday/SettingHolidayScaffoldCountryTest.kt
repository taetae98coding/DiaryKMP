package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingHolidayScaffoldCountryTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-034 국가 선택지를 정해진 순서로 선택 상태와 함께 공휴일보다 먼저 표시한다`() {
        val holidaySetting = holidaySetting(isHoliday = true, isVisible = true)
        composeRule.setSettingHolidayScaffold(
            uiState =
                loadedUiState(
                    holidaySettingList = listOf(holidaySetting),
                    countrySetting = holidayCountrySetting(selectedOptionSet = setOf(HolidayCountryOption.DEVICE, HolidayCountryOption.UNITED_STATES)),
                ),
        )

        composeRule.countryNode(DEFAULT_COUNTRY_DEVICE).assertIsOn()
        composeRule.countryNode(DEFAULT_COUNTRY_KOREA).assertIsOff()
        composeRule.countryNode(DEFAULT_COUNTRY_UNITED_STATES).assertIsOn()

        val deviceTop = composeRule.countryNode(DEFAULT_COUNTRY_DEVICE).getUnclippedBoundsInRoot().top
        val koreaTop = composeRule.countryNode(DEFAULT_COUNTRY_KOREA).getUnclippedBoundsInRoot().top
        val unitedStatesTop = composeRule.countryNode(DEFAULT_COUNTRY_UNITED_STATES).getUnclippedBoundsInRoot().top
        val holidayTop = composeRule.holidayItemNode(holidaySetting.name).getUnclippedBoundsInRoot().top
        deviceTop shouldBeLessThan koreaTop
        koreaTop shouldBeLessThan unitedStatesTop
        unitedStatesTop shouldBeLessThan holidayTop
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-035 국가 선택지를 누르면 그 선택지의 변경을 요청한다`() {
        val labelToOption =
            listOf(
                DEFAULT_COUNTRY_DEVICE to HolidayCountryOption.DEVICE,
                DEFAULT_COUNTRY_KOREA to HolidayCountryOption.KOREA,
                DEFAULT_COUNTRY_UNITED_STATES to HolidayCountryOption.UNITED_STATES,
            )
        val eventList = mutableListOf<SettingHolidayScaffoldEvent>()
        composeRule.setSettingHolidayScaffold(onEvent = eventList::add)

        labelToOption.forEach { (label, _) ->
            composeRule.countryNode(label).performClick()
            composeRule.waitForIdle()
        }

        eventList shouldBe labelToOption.map { (_, option) -> SettingHolidayScaffoldEvent.ToggleCountryOption(option = option) }
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-036 기기 지역이 한국이면 기기값 선택지에 한국을 보여 준다`() {
        assertDeviceSupportingText(deviceCountry = HolidayCountry.KOREA, expected = DEFAULT_COUNTRY_KOREA)
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-036 기기 지역이 미국이면 기기값 선택지에 미국을 보여 준다`() {
        assertDeviceSupportingText(deviceCountry = HolidayCountry.UNITED_STATES, expected = DEFAULT_COUNTRY_UNITED_STATES)
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-036 기기 지역이 지원하지 않는 지역이면 기기값 선택지에 알린다`() {
        assertDeviceSupportingText(deviceCountry = null, expected = DEFAULT_COUNTRY_UNSUPPORTED_REGION)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 국가 선택 문구를 제공한다`() {
        composeRule.setSettingHolidayScaffold(
            uiState =
                loadedUiState(
                    holidaySettingList = listOf(holidaySetting(isHoliday = true, isVisible = true)),
                    countrySetting = holidayCountrySetting(deviceCountry = null),
                ),
        )

        composeRule.onNodeWithText(KOREAN_COUNTRY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_HOLIDAY_LIST_TITLE).assertExists()
        composeRule.countryNode(KOREAN_COUNTRY_DEVICE).assertExists()
        composeRule.countryNode(KOREAN_COUNTRY_KOREA).assertExists()
        composeRule.countryNode(KOREAN_COUNTRY_UNITED_STATES).assertExists()
        composeRule.onNode(hasText(KOREAN_COUNTRY_UNSUPPORTED_REGION) and isToggleable()).assertExists()
    }

    @Test
    fun `TC-SETTING-HOLIDAY-FEATURE-037 검색어가 있는 동안에는 국가 선택지를 제공하지 않는다`() {
        val holidaySetting = holidaySetting(isHoliday = true, isVisible = true, name = "설날")
        composeRule.setSettingHolidayScaffold(uiState = loadedUiState(holidaySettingList = listOf(holidaySetting)))

        composeRule.searchInputField().performTextInput("설날")
        composeRule.waitForIdle()

        composeRule.textNodeCount(DEFAULT_COUNTRY_TITLE) shouldBe 0
        composeRule.textNodeCount(DEFAULT_COUNTRY_DEVICE) shouldBe 0
        composeRule.textNodeCount(DEFAULT_COUNTRY_KOREA) shouldBe 0
        composeRule.textNodeCount(DEFAULT_COUNTRY_UNITED_STATES) shouldBe 0
        composeRule.holidayItemNode(holidaySetting.name).assertExists()

        composeRule.searchInputField().performTextClearance()
        composeRule.waitForIdle()

        composeRule.countryNode(DEFAULT_COUNTRY_DEVICE).assertExists()
        composeRule.countryNode(DEFAULT_COUNTRY_KOREA).assertExists()
        composeRule.countryNode(DEFAULT_COUNTRY_UNITED_STATES).assertExists()
    }

    private fun assertDeviceSupportingText(
        deviceCountry: HolidayCountry?,
        expected: String,
    ) {
        composeRule.setSettingHolidayScaffold(
            uiState =
                loadedUiState(
                    holidaySettingList = listOf(holidaySetting(isHoliday = true, isVisible = true)),
                    countrySetting = holidayCountrySetting(deviceCountry = deviceCountry),
                ),
        )

        composeRule.onNode(hasText(DEFAULT_COUNTRY_DEVICE) and hasText(expected) and isToggleable()).assertExists()
    }

    private companion object {
        const val KOREAN_COUNTRY_TITLE = "국가"
        const val KOREAN_COUNTRY_DEVICE = "기기값"
        const val KOREAN_COUNTRY_KOREA = "한국"
        const val KOREAN_COUNTRY_UNITED_STATES = "미국"
        const val KOREAN_COUNTRY_UNSUPPORTED_REGION = "지원하지 않는 지역"
        const val KOREAN_HOLIDAY_LIST_TITLE = "공휴일"
    }
}

private fun ComposeContentTestRule.countryNode(label: String): SemanticsNodeInteraction = onNode(hasLabel(label) and isToggleable())

// 기기값 줄은 보조 문구로 국가 이름을 함께 담으므로 줄의 첫 문구로 선택지를 가린다.
private fun hasLabel(label: String): SemanticsMatcher =
    SemanticsMatcher("First text is '$label'") { node ->
        node.config
            .getOrNull(SemanticsProperties.Text)
            ?.firstOrNull()
            ?.text == label
    }
