package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.holiday.usecase.DeselectAllHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetSettingHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.SelectAllHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.SelectDaysOffHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.ToggleHolidayVisibilityUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingHolidayViewModel(
    private val getSettingHolidayUseCase: GetSettingHolidayUseCase,
    private val toggleHolidayVisibilityUseCase: ToggleHolidayVisibilityUseCase,
    private val selectAllHolidayUseCase: SelectAllHolidayUseCase,
    private val deselectAllHolidayUseCase: DeselectAllHolidayUseCase,
    private val selectDaysOffHolidayUseCase: SelectDaysOffHolidayUseCase,
) : ViewModel() {
    val uiState: StateFlow<SettingHolidayUiState> =
        getSettingHolidayUseCase(parameter = Unit)
            .map { result ->
                result.fold(
                    onSuccess = { holidaySettingList ->
                        SettingHolidayUiState.Loaded(holidaySettingList = holidaySettingList)
                    },
                    onFailure = { SettingHolidayUiState.Loading },
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SettingHolidayUiState.Loading,
            )

    fun toggleHoliday(key: String) {
        viewModelScope.launch {
            toggleHolidayVisibilityUseCase(parameter = key)
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
