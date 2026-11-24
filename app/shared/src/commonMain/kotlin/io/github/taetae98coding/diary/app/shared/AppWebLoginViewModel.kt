package io.github.taetae98coding.diary.app.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.compose.web.DiaryWebSession
import io.github.taetae98coding.diary.core.model.web.WebLoginRestoreState
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.web.usecase.RestoreWebLoginUseCase
import io.github.taetae98coding.diary.domain.web.usecase.SaveWebLoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class AppWebLoginViewModel(
    private val restoreWebLoginUseCase: RestoreWebLoginUseCase,
    private val saveWebLoginUseCase: SaveWebLoginUseCase,
) : ViewModel() {
    val session: StateFlow<DiaryWebSession>
        field = MutableStateFlow(DiaryWebSession(isRestoring = true))

    private var isRestoreStarted = false
    private var isSaving = false

    fun restore() {
        if (isRestoreStarted) return
        isRestoreStarted = true

        viewModelScope.launch {
            try {
                restoreWebLoginUseCase().collect { result ->
                    session.update { DiaryWebSession(isRestoring = result.getOrNull() == WebLoginRestoreState.RESTORING) }
                }
            } finally {
                session.update { DiaryWebSession(isRestoring = false) }
            }
        }
    }

    fun save() {
        if (isSaving) return
        isSaving = true

        viewModelScope.launch {
            try {
                saveWebLoginUseCase()
            } finally {
                isSaving = false
            }
        }
    }
}
