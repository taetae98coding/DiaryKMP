package io.github.taetae98coding.diary.domain.web.usecase

import io.github.taetae98coding.diary.core.model.web.WebLoginRestoreState
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.web.repository.WebLoginRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Factory

@Factory
public class RestoreWebLoginUseCase internal constructor(
    private val webLoginRepository: WebLoginRepository,
) : FlowUseCase<Unit, WebLoginRestoreState>() {
    override fun execute(parameter: Unit): Flow<Result<WebLoginRestoreState>> =
        flow {
            webLoginRepository.setRestoring()

            try {
                val savedLogin = webLoginRepository.readSavedLogin()

                if (savedLogin.sessionCookieList.isNotEmpty()) {
                    emit(Result.success(WebLoginRestoreState.RESTORING))
                    webLoginRepository.upsertWebViewLogin(login = savedLogin)
                }
            } finally {
                webLoginRepository.unsetRestoring()
            }

            emit(Result.success(WebLoginRestoreState.DONE))
        }
}
