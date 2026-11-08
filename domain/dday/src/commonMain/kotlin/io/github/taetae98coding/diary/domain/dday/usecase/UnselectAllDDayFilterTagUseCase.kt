package io.github.taetae98coding.diary.domain.dday.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayFilterRepository
import org.koin.core.annotation.Factory

@Factory
public class UnselectAllDDayFilterTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountDDayFilterRepository: AccountDDayFilterRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        val account = getAccountUseCase.requireAccount()

        accountDDayFilterRepository.deleteAll(account = account)
    }
}
