package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountCalendarFilterRepository
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class SelectCalendarFilterTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountCalendarFilterRepository: AccountCalendarFilterRepository,
) : UseCase<Uuid, Unit>() {
    override suspend fun execute(parameter: Uuid) {
        val account = getAccountUseCase.requireAccount()

        accountCalendarFilterRepository.upsert(
            account = account,
            tagId = parameter,
        )
    }
}
