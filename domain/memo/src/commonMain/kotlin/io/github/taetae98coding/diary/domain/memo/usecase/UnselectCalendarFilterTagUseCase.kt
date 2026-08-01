package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountCalendarFilterRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class UnselectCalendarFilterTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountCalendarFilterRepository: AccountCalendarFilterRepository,
) : UseCase<Uuid, Unit>() {
    override suspend fun execute(parameter: Uuid) {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        accountCalendarFilterRepository.delete(
            account = account,
            tagId = parameter,
        )
    }
}
