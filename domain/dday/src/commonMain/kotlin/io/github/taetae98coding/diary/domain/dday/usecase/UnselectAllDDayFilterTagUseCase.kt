package io.github.taetae98coding.diary.domain.dday.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayFilterRepository

@Inject
public class UnselectAllDDayFilterTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountDDayFilterRepository: AccountDDayFilterRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        val account = getAccountUseCase.requireAccount()

        accountDDayFilterRepository.deleteAll(account = account)
    }
}
