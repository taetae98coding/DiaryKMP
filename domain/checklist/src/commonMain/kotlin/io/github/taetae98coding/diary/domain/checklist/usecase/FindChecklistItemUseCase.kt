package io.github.taetae98coding.diary.domain.checklist.usecase

import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistItemRepository
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class FindChecklistItemUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountChecklistItemRepository: AccountChecklistItemRepository,
) : FlowUseCase<Uuid, ChecklistItem?>() {
    override fun execute(parameter: Uuid): Flow<Result<ChecklistItem?>> =
        getAccountUseCase.flatMapAccount { account ->
            accountChecklistItemRepository
                .find(
                    account = account,
                    itemId = parameter,
                ).map { item -> Result.success(item) }
        }
}
