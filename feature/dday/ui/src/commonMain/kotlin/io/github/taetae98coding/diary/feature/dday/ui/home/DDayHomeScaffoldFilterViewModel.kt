package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.GetDDayFilterUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class DDayHomeScaffoldFilterViewModel(
    getDDayFilterUseCase: GetDDayFilterUseCase,
    getDDayFilterTagIdUseCase: GetDDayFilterTagIdUseCase,
) : ViewModel() {
    val uiState: StateFlow<DDayHomeScaffoldFilterUiState> =
        combine(
            getDDayFilterUseCase(),
            getDDayFilterTagIdUseCase(),
        ) { tagListResult, storedTagIdSetResult ->
            DDayHomeScaffoldFilterUiState(
                selectedTagIdSet =
                    tagListResult
                        .getOrDefault(emptyList())
                        .map { tag -> tag.id }
                        .toSet(),
                storedTagIdSet = storedTagIdSetResult.getOrDefault(emptySet()),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = DDayHomeScaffoldFilterUiState(isLoaded = false),
        )
}
