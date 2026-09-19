package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.domain.account.repository.SessionRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import org.koin.core.annotation.Factory

@Factory
public class SignOutUseCase internal constructor(
    private val sessionRepository: SessionRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        sessionRepository.delete()
    }
}
