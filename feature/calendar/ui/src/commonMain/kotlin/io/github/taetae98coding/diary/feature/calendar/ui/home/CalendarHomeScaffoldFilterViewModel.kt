package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarFilterUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class CalendarHomeScaffoldFilterViewModel(
    getCalendarFilterUseCase: GetCalendarFilterUseCase,
) : ViewModel() {
    val uiState: StateFlow<CalendarHomeScaffoldFilterUiState> =
        getCalendarFilterUseCase()
            .map { result ->
                CalendarHomeScaffoldFilterUiState(
                    isApplied = result.getOrDefault(emptyList()).isNotEmpty(),
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = CalendarHomeScaffoldFilterUiState(),
            )
}
