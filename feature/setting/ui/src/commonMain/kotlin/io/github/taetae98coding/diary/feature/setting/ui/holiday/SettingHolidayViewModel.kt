package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountryOption
import io.github.taetae98coding.diary.domain.holiday.usecase.GetHolidayCountrySettingUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetSettingHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.SelectAllHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.SelectDaysOffHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.ToggleHolidayCountryOptionUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.ToggleHolidayVisibilityUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.UnselectAllHolidayUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingHolidayViewModel(
    private val getHolidayCountrySettingUseCase: GetHolidayCountrySettingUseCase,
    private val getSettingHolidayUseCase: GetSettingHolidayUseCase,
    private val toggleHolidayCountryOptionUseCase: ToggleHolidayCountryOptionUseCase,
    private val toggleHolidayVisibilityUseCase: ToggleHolidayVisibilityUseCase,
    private val selectAllHolidayUseCase: SelectAllHolidayUseCase,
    private val unselectAllHolidayUseCase: UnselectAllHolidayUseCase,
    private val selectDaysOffHolidayUseCase: SelectDaysOffHolidayUseCase,
) : ViewModel() {
    val uiState: StateFlow<SettingHolidayUiState> =
        combine(
            getHolidayCountrySettingUseCase(parameter = Unit),
            getSettingHolidayUseCase(parameter = Unit),
        ) { countrySettingResult, holidaySettingListResult ->
            val countrySetting = countrySettingResult.getOrNull()
            val holidaySettingList = holidaySettingListResult.getOrNull()

            if (countrySetting == null || holidaySettingList == null) {
                SettingHolidayUiState.Loading
            } else {
                SettingHolidayUiState.Content(
                    countrySetting = countrySetting,
                    holidaySettingList = holidaySettingList,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SettingHolidayUiState.Loading,
        )

    private val inProgressKeySet = mutableSetOf<InProgressKey>()

    fun toggleCountryOption(option: HolidayCountryOption) {
        launchGuarded(key = InProgressKey.CountryOption(option = option)) {
            toggleHolidayCountryOptionUseCase(parameter = option)
        }
    }

    fun toggleHoliday(name: String) {
        launchGuarded(key = InProgressKey.Holiday(name = name)) {
            toggleHolidayVisibilityUseCase(parameter = name)
        }
    }

    fun selectAll() {
        launchGuarded(key = InProgressKey.SelectAll) {
            selectAllHolidayUseCase(parameter = Unit)
        }
    }

    fun deselectAll() {
        launchGuarded(key = InProgressKey.DeselectAll) {
            unselectAllHolidayUseCase(parameter = Unit)
        }
    }

    fun selectDaysOff() {
        launchGuarded(key = InProgressKey.SelectDaysOff) {
            selectDaysOffHolidayUseCase(parameter = Unit)
        }
    }

    private fun launchGuarded(
        key: InProgressKey,
        block: suspend () -> Unit,
    ) {
        if (!inProgressKeySet.add(key)) return

        viewModelScope.launch {
            try {
                block()
            } finally {
                inProgressKeySet.remove(key)
            }
        }
    }

    private sealed interface InProgressKey {
        data class CountryOption(
            val option: HolidayCountryOption,
        ) : InProgressKey

        data class Holiday(
            val name: String,
        ) : InProgressKey

        data object SelectAll : InProgressKey

        data object DeselectAll : InProgressKey

        data object SelectDaysOff : InProgressKey
    }
}
