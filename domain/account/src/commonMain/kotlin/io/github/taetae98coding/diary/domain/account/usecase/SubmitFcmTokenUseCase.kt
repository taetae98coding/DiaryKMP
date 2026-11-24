package io.github.taetae98coding.diary.domain.account.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.repository.FcmTokenRepository
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase

@Inject
public class SubmitFcmTokenUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val fcmTokenRepository: FcmTokenRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        when (val account = getAccountUseCase.requireAccount()) {
            is Account.Guest -> fcmTokenRepository.delete()
            is Account.User -> if (account.isSessionValid) fcmTokenRepository.upsert()
        }
    }
}
