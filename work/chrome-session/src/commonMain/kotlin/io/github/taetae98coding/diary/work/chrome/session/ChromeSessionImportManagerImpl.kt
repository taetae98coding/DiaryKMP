package io.github.taetae98coding.diary.work.chrome.session

import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.core.model.browser.ChromeSessionImportState
import io.github.taetae98coding.diary.domain.browser.ChromeSessionImportManager
import io.github.taetae98coding.diary.domain.browser.repository.InAppBrowserCookieRepository
import io.github.taetae98coding.diary.domain.browser.usecase.FindChromeSessionImportProfileUseCase
import io.github.taetae98coding.diary.domain.browser.usecase.ImportChromeSessionUseCase
import io.github.taetae98coding.diary.work.chrome.session.di.ChromeSessionScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

@Single
internal class ChromeSessionImportManagerImpl(
    private val findChromeSessionImportProfileUseCase: FindChromeSessionImportProfileUseCase,
    private val importChromeSessionUseCase: ImportChromeSessionUseCase,
    private val inAppBrowserCookieRepository: InAppBrowserCookieRepository,
    @param:ChromeSessionScope private val scope: CoroutineScope,
) : ChromeSessionImportManager {
    override val state: StateFlow<ChromeSessionImportState>
        field = MutableStateFlow(ChromeSessionImportState.IDLE)

    // 가져오는 중이 아닐 때의 마지막 상태. 가져오는 중에 취소된 작업은 상태를 정리하지 않으므로, 다음 작업이 할 일이 없을 때 여기로 되돌린다.
    private var settledState = ChromeSessionImportState.IDLE

    // 이전 작업의 취소가 끝난 뒤에만 새 작업이 상태를 만지도록 한 번에 하나만 실행한다.
    private val mutex = Mutex()
    private var job: Job? = null

    override fun requestImport(clearsBefore: Boolean) {
        job?.cancel()
        job = scope.launch { mutex.withLock { run(clearsBefore = clearsBefore) } }
    }

    private suspend fun run(clearsBefore: Boolean) {
        if (clearsBefore && !clear()) return

        findChromeSessionImportProfileUseCase(parameter = Unit)
            .fold(
                onSuccess = { profile -> import(profile = profile) },
                onFailure = { throwable ->
                    throwable.rethrowIfCancellation()
                    settle(next = ChromeSessionImportState.FAILED)
                },
            )
    }

    private suspend fun import(profile: ChromeProfile?) {
        if (profile == null) {
            // 가져오는 중에 취소된 뒤 할 일이 없으면 가져오기 전 상태로 되돌린다.
            if (state.value == ChromeSessionImportState.IMPORTING) state.value = settledState
            return
        }

        state.value = ChromeSessionImportState.IMPORTING

        importChromeSessionUseCase(parameter = profile)
            .fold(
                onSuccess = { settle(next = ChromeSessionImportState.IMPORTED) },
                onFailure = { throwable ->
                    throwable.rethrowIfCancellation()
                    settle(next = ChromeSessionImportState.FAILED)
                },
            )
    }

    private suspend fun clear(): Boolean =
        try {
            inAppBrowserCookieRepository.deleteAll()
            settle(next = ChromeSessionImportState.IDLE)
            true
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Throwable) {
            settle(next = ChromeSessionImportState.FAILED)
            false
        }

    private fun settle(next: ChromeSessionImportState) {
        settledState = next
        state.value = next
    }

    private fun Throwable.rethrowIfCancellation() {
        if (this is CancellationException) throw this
    }
}
