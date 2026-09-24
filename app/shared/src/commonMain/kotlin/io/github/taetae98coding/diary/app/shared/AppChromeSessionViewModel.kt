package io.github.taetae98coding.diary.app.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.compose.web.DiaryWebSession
import io.github.taetae98coding.diary.core.model.browser.ChromeSessionImportState
import io.github.taetae98coding.diary.domain.browser.usecase.GetChromeSessionImportStateUseCase
import io.github.taetae98coding.diary.domain.browser.usecase.RequestChromeSessionImportUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AppChromeSessionViewModel(
    getChromeSessionImportStateUseCase: GetChromeSessionImportStateUseCase,
    private val requestChromeSessionImportUseCase: RequestChromeSessionImportUseCase,
) : ViewModel() {
    val session: StateFlow<DiaryWebSession> =
        getChromeSessionImportStateUseCase(parameter = Unit)
            .mapNotNull { result -> result.getOrNull() }
            .distinctUntilChanged()
            .scan(SessionProgress()) { progress, state -> progress.next(state = state) }
            // scan의 초기값은 상태를 아직 읽기 전이므로 내보내지 않는다.
            .drop(1)
            .map { progress -> progress.toSession() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = DiaryWebSession(),
            )

    fun requestImport() {
        viewModelScope.launch {
            requestChromeSessionImportUseCase(parameter = Unit)
        }
    }
}

private data class SessionProgress(
    val isPreparing: Boolean = false,
    val importCount: Int = 0,
    val failureCount: Int = 0,
    val isFailed: Boolean = false,
) {
    fun next(state: ChromeSessionImportState): SessionProgress {
        val isImporting = state == ChromeSessionImportState.IMPORTING
        val isFailed = state == ChromeSessionImportState.FAILED

        return SessionProgress(
            isPreparing = isImporting,
            importCount = if (isPreparing && !isImporting) importCount + 1 else importCount,
            failureCount = if (isFailed && !this.isFailed) failureCount + 1 else failureCount,
            isFailed = isFailed,
        )
    }

    fun toSession(): DiaryWebSession =
        DiaryWebSession(
            isPreparing = isPreparing,
            importCount = importCount,
            failureId = failureCount.takeIf { isFailed },
        )
}
