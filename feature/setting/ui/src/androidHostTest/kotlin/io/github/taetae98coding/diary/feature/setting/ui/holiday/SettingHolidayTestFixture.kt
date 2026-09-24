package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountrySetting
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.uuid.Uuid

internal const val DEFAULT_NAVIGATE_UP_DESCRIPTION: String = "Navigate up"
internal const val DEFAULT_SELECT_ALL: String = "Select all"
internal const val DEFAULT_DESELECT_ALL: String = "Deselect all"
internal const val DEFAULT_SELECT_DAYS_OFF: String = "Select days off"
internal const val DEFAULT_DAY_OFF_LABEL: String = "Day off"
internal const val DEFAULT_LOADING_DESCRIPTION: String = "Loading holidays"
internal const val DEFAULT_SEARCH_PLACEHOLDER: String = "Search for a holiday"
internal const val DEFAULT_SEARCH_EMPTY: String = "No search results"
internal const val DEFAULT_CLEAR_DESCRIPTION: String = "Clear text"
internal const val DEFAULT_BULK_ACTION_DESCRIPTION: String = "Bulk select"
internal const val DEFAULT_BULK_ACTION_CLOSE_DESCRIPTION: String = "Close bulk select"

internal const val DEFAULT_COUNTRY_TITLE: String = "Country"
internal const val DEFAULT_COUNTRY_DEVICE: String = "Device setting"
internal const val DEFAULT_COUNTRY_KOREA: String = "South Korea"
internal const val DEFAULT_COUNTRY_UNITED_STATES: String = "United States"
internal const val DEFAULT_COUNTRY_UNSUPPORTED_REGION: String = "Unsupported region"
internal const val DEFAULT_HOLIDAY_LIST_TITLE: String = "Holidays"

internal const val KOREAN_NAVIGATE_UP_DESCRIPTION: String = "뒤로가기"
internal const val KOREAN_SELECT_ALL: String = "전체 선택"
internal const val KOREAN_DESELECT_ALL: String = "전체 해제"
internal const val KOREAN_SELECT_DAYS_OFF: String = "쉬는 날만 선택"
internal const val KOREAN_DAY_OFF_LABEL: String = "쉬는 날"
internal const val KOREAN_LOADING_DESCRIPTION: String = "공휴일 확인 중"
internal const val KOREAN_SEARCH_PLACEHOLDER: String = "공휴일을 검색하세요"
internal const val KOREAN_SEARCH_EMPTY: String = "검색 결과가 없습니다"
internal const val KOREAN_CLEAR_DESCRIPTION: String = "지우기"
internal const val KOREAN_BULK_ACTION_DESCRIPTION: String = "일괄 선택"
internal const val KOREAN_BULK_ACTION_CLOSE_DESCRIPTION: String = "일괄 선택 닫기"

internal val settingHolidayFixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal fun holidaySetting(
    isHoliday: Boolean,
    isVisible: Boolean,
    name: String? = null,
): HolidaySetting {
    // 이름은 목록 항목의 LazyColumn key로 쓰여 중복되면 안 되므로 난수 대신 고유한 값으로 만든다.
    val identifier = Uuid.random().toString()

    return settingHolidayFixtureMonkey
        .giveMeOne<HolidaySetting>()
        .copy(
            name = name ?: "Holiday $identifier",
            isHoliday = isHoliday,
            isVisible = isVisible,
        )
}

internal fun holidayCountrySetting(
    selectedOptionSet: Set<HolidayCountryOption> = setOf(HolidayCountryOption.DEVICE),
    deviceCountry: HolidayCountry? = HolidayCountry.KOREA,
): HolidayCountrySetting =
    HolidayCountrySetting(
        selectedOptionSet = selectedOptionSet,
        deviceCountry = deviceCountry,
    )

internal fun loadedUiState(
    holidaySettingList: List<HolidaySetting>,
    countrySetting: HolidayCountrySetting = holidayCountrySetting(),
): SettingHolidayUiState.Loaded =
    SettingHolidayUiState.Loaded(
        countrySetting = countrySetting,
        holidaySettingList = holidaySettingList,
    )

internal fun hasRole(role: Role): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)

internal fun ComposeContentTestRule.searchInputField(): SemanticsNodeInteraction = onNode(hasSetTextAction())

internal fun ComposeContentTestRule.holidayItemNode(name: String): SemanticsNodeInteraction = onNode(hasText(name) and hasRole(Role.Checkbox))

internal fun ComposeContentTestRule.scrollToText(text: String) {
    onNode(hasScrollToNodeAction()).performScrollToNode(hasText(text))
    waitForIdle()
}

internal fun ComposeContentTestRule.searchClearButton(): SemanticsNodeInteraction = onNodeWithContentDescription(DEFAULT_CLEAR_DESCRIPTION)

internal fun ComposeContentTestRule.textNodeCount(text: String): Int = onAllNodesWithText(text).fetchSemanticsNodes().size

internal fun ComposeContentTestRule.expandBulkAction() {
    onNodeWithContentDescription(DEFAULT_BULK_ACTION_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun screenTestViewModel(uiState: SettingHolidayUiState): SettingHolidayViewModel {
    val viewModel = mockk<SettingHolidayViewModel>(relaxUnitFun = true)
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    return viewModel
}

internal fun ComposeContentTestRule.setSettingHolidayScaffold(
    uiState: SettingHolidayUiState =
        loadedUiState(
            holidaySettingList = listOf(holidaySetting(isHoliday = true, isVisible = true)),
        ),
    onEvent: (SettingHolidayScaffoldEvent) -> Unit = {},
) {
    setContent {
        DiaryTheme {
            SettingHolidayScaffold(
                onEvent = onEvent,
                uiStateProvider = { uiState },
            )
        }
    }
}

internal fun ComposeContentTestRule.setSettingHolidayScreen(
    viewModel: SettingHolidayViewModel,
    navigateUp: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            SettingHolidayScreen(
                navigateUp = navigateUp,
                viewModel = viewModel,
                componentVisibleProvider = { SettingHolidayScaffoldComponentVisible() },
            )
        }
    }
}
