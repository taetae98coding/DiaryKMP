package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.core.model.authentication.LoginProvider
import io.github.taetae98coding.diary.domain.account.repository.LoginProviderRepository
import io.github.taetae98coding.diary.domain.account.repository.UserDataRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.logCrashlyticsFailure
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class CheckAppleReauthenticationRequiredUseCase internal constructor(
    private val loginProviderRepository: LoginProviderRepository,
    private val userDataRepository: UserDataRepository,
) : UseCase<Unit, Boolean>() {
    override suspend fun execute(parameter: Unit): Boolean =
        when (loginProviderRepository.read()) {
            LoginProvider.APPLE -> true

            LoginProvider.GOOGLE -> false

            null ->
                userDataRepository
                    .get()
                    .first()
                    ?.loginProviderSet
                    .orEmpty()
                    .contains(LoginProvider.APPLE)
        }

    override fun onFailure(throwable: Throwable) {
        DiaryLogger.logCrashlyticsFailure(source = this, throwable = throwable)
    }
}
