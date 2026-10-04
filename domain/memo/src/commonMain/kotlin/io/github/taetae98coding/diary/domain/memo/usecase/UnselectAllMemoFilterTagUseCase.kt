package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoFilterRepository
import org.koin.core.annotation.Factory

@Factory
public class UnselectAllMemoFilterTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountMemoFilterRepository: AccountMemoFilterRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        val account = getAccountUseCase.requireAccount()

        accountMemoFilterRepository.deleteAll(account = account)
    }
}
