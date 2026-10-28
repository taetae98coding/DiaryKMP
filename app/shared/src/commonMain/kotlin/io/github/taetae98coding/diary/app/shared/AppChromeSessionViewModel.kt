package io.github.taetae98coding.diary.app.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.compose.web.DiaryWebSession
import io.github.taetae98coding.diary.core.model.browser.ChromeSessionImportState
import io.github.taetae98coding.diary.domain.browser.usecase.GetChromeSessionImportStateUseCase
import io.github.taetae98coding.diary.domain.browser.usecase.RequestChromeSessionImportUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class AppChromeSessionViewModel(
    getChromeSessionImportStateUseCase: GetChromeSessionImportStateUseCase,
    private val requestChromeSessionImportUseCase: RequestChromeSessionImportUseCase,
) : ViewModel() {
    private val effectChannel = Channel<AppChromeSessionEffect>(capacity = Channel.BUFFERED)
    val effect: Flow<AppChromeSessionEffect> = effectChannel.receiveAsFlow()

    val session: StateFlow<DiaryWebSession> =
        getChromeSessionImportStateUseCase(parameter = Unit)
            .mapNotNull { result -> result.getOrNull() }
            .distinctUntilChanged()
            .scan(SessionProgress()) { progress, state -> progress.next(state = state) }
            // scan의 초기값은 상태를 아직 읽기 전이므로 내보내지 않는다.
            .drop(1)
            .onEach { progress -> if (progress.isNewlyFailed) effectChannel.send(AppChromeSessionEffect.ImportFailed) }
            .map { progress -> progress.toSession() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = DiaryWebSession(),
            )

    private var isRequesting = false

    fun requestImport() {
        if (isRequesting) return
        isRequesting = true

        viewModelScope.launch {
            try {
                requestChromeSessionImportUseCase(parameter = Unit)
            } finally {
                isRequesting = false
            }
        }
    }
}

// state는 바로 앞에 읽은 상태로, 처음 읽은 상태는 새로 끝난 결과가 아니므로 실패로 알리지 않는다.
private data class SessionProgress(
    val state: ChromeSessionImportState? = null,
    val importCount: Int = 0,
    val isNewlyFailed: Boolean = false,
) {
    val isPreparing: Boolean
        get() = state == ChromeSessionImportState.IMPORTING

    fun next(state: ChromeSessionImportState): SessionProgress =
        SessionProgress(
            state = state,
            importCount = if (isPreparing && state != ChromeSessionImportState.IMPORTING) importCount + 1 else importCount,
            isNewlyFailed = this.state != null && state == ChromeSessionImportState.FAILED,
        )

    fun toSession(): DiaryWebSession =
        DiaryWebSession(
            isPreparing = isPreparing,
            importCount = importCount,
        )
}
