package io.github.taetae98coding.diary.domain.routine.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
public class GetRoutineFilterUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountRoutineFilterRepository: AccountRoutineFilterRepository,
) : FlowUseCase<Unit, List<Tag>>() {
    override fun execute(parameter: Unit): Flow<Result<List<Tag>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountRoutineFilterRepository.getTagList(account = account).map { tagList ->
                Result.success(tagList)
            }
        }
}
