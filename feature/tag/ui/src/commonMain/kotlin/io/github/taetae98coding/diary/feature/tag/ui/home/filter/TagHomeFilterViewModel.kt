package io.github.taetae98coding.diary.feature.tag.ui.home.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.tag.usecase.DisableTopLevelTagFilterUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.EnableTopLevelTagFilterUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetTopLevelTagFilterUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class TagHomeFilterViewModel(
    getTopLevelTagFilterUseCase: GetTopLevelTagFilterUseCase,
    private val enableTopLevelTagFilterUseCase: EnableTopLevelTagFilterUseCase,
    private val disableTopLevelTagFilterUseCase: DisableTopLevelTagFilterUseCase,
) : ViewModel() {
    val uiState: StateFlow<TagHomeFilterUiState> =
        getTopLevelTagFilterUseCase(parameter = Unit)
            .map { result -> TagHomeFilterUiState(isTopLevelOnly = result.getOrDefault(false)) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = TagHomeFilterUiState(),
            )

    fun enableTopLevelOnly() {
        viewModelScope.launch {
            enableTopLevelTagFilterUseCase(parameter = Unit)
        }
    }

    fun disableTopLevelOnly() {
        viewModelScope.launch {
            disableTopLevelTagFilterUseCase(parameter = Unit)
        }
    }
}
