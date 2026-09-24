package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import io.github.taetae98coding.diary.domain.holiday.usecase.DeselectAllHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetHolidayCountrySettingUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetSettingHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.SelectAllHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.SelectDaysOffHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.ToggleHolidayCountryOptionUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.ToggleHolidayVisibilityUseCase
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
    private val deselectAllHolidayUseCase: DeselectAllHolidayUseCase,
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
                SettingHolidayUiState.Loaded(
                    countrySetting = countrySetting,
                    holidaySettingList = holidaySettingList,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SettingHolidayUiState.Loading,
        )

    fun toggleCountryOption(option: HolidayCountryOption) {
        viewModelScope.launch {
            toggleHolidayCountryOptionUseCase(parameter = option)
        }
    }

    fun toggleHoliday(name: String) {
        viewModelScope.launch {
            toggleHolidayVisibilityUseCase(parameter = name)
        }
    }

    fun selectAll() {
        viewModelScope.launch {
            selectAllHolidayUseCase(parameter = Unit)
        }
    }

    fun deselectAll() {
        viewModelScope.launch {
            deselectAllHolidayUseCase(parameter = Unit)
        }
    }

    fun selectDaysOff() {
        viewModelScope.launch {
            selectDaysOffHolidayUseCase(parameter = Unit)
        }
    }
}
