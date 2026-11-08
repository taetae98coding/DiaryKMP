package io.github.taetae98coding.diary.domain.dday.usecase

import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class FindDDayUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountDDayRepository: AccountDDayRepository,
) : FlowUseCase<Uuid, DDay?>() {
    override fun execute(parameter: Uuid): Flow<Result<DDay?>> =
        getAccountUseCase.flatMapAccount { account ->
            accountDDayRepository
                .find(
                    account = account,
                    ddayId = parameter,
                ).map { dday -> Result.success(dday) }
        }
}
