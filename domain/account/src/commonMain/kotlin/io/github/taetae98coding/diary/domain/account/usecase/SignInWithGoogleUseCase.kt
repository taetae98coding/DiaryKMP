package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.domain.account.repository.SessionRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import org.koin.core.annotation.Factory

@Factory
public class SignInWithGoogleUseCase internal constructor(
    private val sessionRepository: SessionRepository,
) : UseCase<GoogleCredential, Unit>() {
    override suspend fun execute(parameter: GoogleCredential) {
        sessionRepository.create(credential = parameter)
    }

    override fun onFailure(throwable: Throwable) {
        DiaryLogger.log(log = CrashlyticsLog(message = "${this::class.simpleName.orEmpty()} 실패", throwable = throwable))
    }
}
