package io.github.taetae98coding.diary.domain.checklist.usecase

import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistItemRepository
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class GetChecklistItemUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountChecklistItemRepository: AccountChecklistItemRepository,
) : FlowUseCase<GetChecklistItemUseCase.Parameter, List<ChecklistItem>>() {
    override fun execute(parameter: Parameter): Flow<Result<List<ChecklistItem>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountChecklistItemRepository
                .get(
                    account = account,
                    checklistId = parameter.checklistId,
                    sort = parameter.sort,
                ).map { itemList -> Result.success(itemList) }
        }

    public data class Parameter(
        val checklistId: Uuid,
        val sort: ChecklistItemSort,
    )
}
