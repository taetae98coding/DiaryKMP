package io.github.taetae98coding.diary.app.shared.integrity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.integrity.usecase.LogPlayIntegrityUseCase
import kotlinx.coroutines.launch

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
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
