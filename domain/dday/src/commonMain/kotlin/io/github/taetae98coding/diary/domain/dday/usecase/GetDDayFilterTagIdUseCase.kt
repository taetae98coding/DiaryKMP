package io.github.taetae98coding.diary.domain.dday.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class GetDDayFilterTagIdUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountDDayFilterRepository: AccountDDayFilterRepository,
) : FlowUseCase<Unit, Set<Uuid>>() {
    override fun execute(parameter: Unit): Flow<Result<Set<Uuid>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountDDayFilterRepository.getTagIdSet(account = account).map { tagIdSet ->
                Result.success(tagIdSet)
            }
        }
}
