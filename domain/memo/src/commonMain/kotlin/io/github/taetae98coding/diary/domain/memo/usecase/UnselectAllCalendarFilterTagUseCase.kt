package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountCalendarFilterRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class UnselectAllCalendarFilterTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountCalendarFilterRepository: AccountCalendarFilterRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        accountCalendarFilterRepository.deleteAll(account = account)
    }
}
