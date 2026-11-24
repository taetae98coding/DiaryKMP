package io.github.taetae98coding.diary.domain.account.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.domain.account.repository.LoginProviderRepository
import io.github.taetae98coding.diary.domain.account.repository.SessionRepository
import io.github.taetae98coding.diary.domain.core.UseCase

@Inject
public class SignOutUseCase internal constructor(
    private val sessionRepository: SessionRepository,
    private val loginProviderRepository: LoginProviderRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        sessionRepository.delete()
        loginProviderRepository.unset()
    }
}
