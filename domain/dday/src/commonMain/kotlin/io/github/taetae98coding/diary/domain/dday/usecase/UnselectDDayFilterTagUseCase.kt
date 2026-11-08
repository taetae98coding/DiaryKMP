package io.github.taetae98coding.diary.domain.dday.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayFilterRepository
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class UnselectDDayFilterTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountDDayFilterRepository: AccountDDayFilterRepository,
) : UseCase<Uuid, Unit>() {
    override suspend fun execute(parameter: Uuid) {
        val account = getAccountUseCase.requireAccount()

        accountDDayFilterRepository.delete(
            account = account,
            tagId = parameter,
        )
    }
}
