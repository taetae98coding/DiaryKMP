package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.model.authentication.LoginProvider
import io.github.taetae98coding.diary.domain.account.repository.LoginProviderRepository
import io.github.taetae98coding.diary.domain.account.repository.SessionRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.logCrashlyticsFailure
import org.koin.core.annotation.Factory

@Factory
public class SignInWithAppleUseCase internal constructor(
    private val sessionRepository: SessionRepository,
    private val loginProviderRepository: LoginProviderRepository,
) : UseCase<AppleCredential, Unit>() {
    override suspend fun execute(parameter: AppleCredential) {
        sessionRepository.create(credential = parameter)
        loginProviderRepository.set(provider = LoginProvider.APPLE)
    }

    override fun onFailure(throwable: Throwable) {
        DiaryLogger.logCrashlyticsFailure(source = this, throwable = throwable)
    }
}
