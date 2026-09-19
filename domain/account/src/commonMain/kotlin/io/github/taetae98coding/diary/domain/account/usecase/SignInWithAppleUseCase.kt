package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.domain.account.repository.SessionRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import org.koin.core.annotation.Factory

@Factory
public class SignInWithAppleUseCase internal constructor(
    private val sessionRepository: SessionRepository,
) : UseCase<AppleCredential, Unit>() {
    override suspend fun execute(parameter: AppleCredential) {
        sessionRepository.create(credential = parameter)
    }

    override fun onFailure(throwable: Throwable) {
        DiaryLogger.log(log = CrashlyticsLog(message = "${this::class.simpleName.orEmpty()} 실패", throwable = throwable))
    }
}
