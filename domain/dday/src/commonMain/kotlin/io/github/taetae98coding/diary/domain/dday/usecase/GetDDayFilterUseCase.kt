package io.github.taetae98coding.diary.domain.dday.usecase

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetDDayFilterUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountDDayFilterRepository: AccountDDayFilterRepository,
) : FlowUseCase<Unit, List<Tag>>() {
    override fun execute(parameter: Unit): Flow<Result<List<Tag>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountDDayFilterRepository.getTagList(account = account).map { tagList ->
                Result.success(tagList)
            }
        }
}
