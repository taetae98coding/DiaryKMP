package io.github.taetae98coding.diary.domain.checklist.usecase

import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistRepository
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class FindChecklistUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountChecklistRepository: AccountChecklistRepository,
) : FlowUseCase<Uuid, Checklist?>() {
    override fun execute(parameter: Uuid): Flow<Result<Checklist?>> =
        getAccountUseCase.flatMapAccount { account ->
            accountChecklistRepository
                .find(
                    account = account,
                    checklistId = parameter,
                ).map { checklist -> Result.success(checklist) }
        }
}
