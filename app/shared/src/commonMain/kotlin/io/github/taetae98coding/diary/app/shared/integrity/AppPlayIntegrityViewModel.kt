package io.github.taetae98coding.diary.app.shared.integrity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.integrity.usecase.LogPlayIntegrityUseCase
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AppPlayIntegrityViewModel(
    private val logPlayIntegrityUseCase: LogPlayIntegrityUseCase,
) : ViewModel() {
    private var isLogging = false

    fun log() {
        if (isLogging) return
        isLogging = true

        viewModelScope.launch {
            try {
                logPlayIntegrityUseCase()
            } finally {
                isLogging = false
            }
        }
    }
}
