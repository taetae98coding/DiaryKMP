package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.repository.UserDataRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class RefreshUserDataUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val userDataRepository: UserDataRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        val account = getAccountUseCase(parameter = Unit).first().getOrNull()

        if (account !is Account.User) return

        userDataRepository.refresh()
    }
}
