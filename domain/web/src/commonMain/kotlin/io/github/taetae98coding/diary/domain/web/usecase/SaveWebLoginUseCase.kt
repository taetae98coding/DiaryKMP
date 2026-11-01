package io.github.taetae98coding.diary.domain.web.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.web.repository.WebLoginRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class SaveWebLoginUseCase internal constructor(
    private val webLoginRepository: WebLoginRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        webLoginRepository.getRestoring().first { isRestoring -> !isRestoring }

        try {
            val savedLogin = webLoginRepository.readSavedLogin()
            val currentLogin = webLoginRepository.readWebViewLogin(siteUrlSet = savedLogin.siteUrlSet) ?: return

            webLoginRepository.submitSavedLogin(login = currentLogin)
        } finally {
            webLoginRepository.flushWebViewLogin()
        }
    }
}
