package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.repository.FcmTokenRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class SubmitFcmTokenUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val fcmTokenRepository: FcmTokenRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        when (val account = getAccountUseCase(parameter = Unit).first().getOrThrow()) {
            is Account.Guest -> fcmTokenRepository.delete()
            is Account.User -> if (account.isSessionValid) fcmTokenRepository.upsert()
        }
    }

    override fun onFailure(throwable: Throwable) {
        DiaryLogger.log(log = CrashlyticsLog(message = "${this::class.simpleName.orEmpty()} 실패", throwable = throwable))
    }
}
