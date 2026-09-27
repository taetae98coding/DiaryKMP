package io.github.taetae98coding.diary.app.shared.integrity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.integrity.usecase.LogPlayIntegrityUseCase
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AppPlayIntegrityViewModel(
    private val logPlayIntegrityUseCase: LogPlayIntegrityUseCase,
) : ViewModel() {
    fun log() {
        viewModelScope.launch {
            logPlayIntegrityUseCase(parameter = Unit)
        }
    }
}
