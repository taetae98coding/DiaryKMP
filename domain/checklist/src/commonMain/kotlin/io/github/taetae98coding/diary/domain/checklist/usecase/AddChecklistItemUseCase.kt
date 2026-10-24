package io.github.taetae98coding.diary.domain.checklist.usecase

import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.checklist.exception.ChecklistItemContentBlankException
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistItemRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddChecklistItemUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountChecklistItemRepository: AccountChecklistItemRepository,
    private val clock: Clock,
) : UseCase<AddChecklistItemUseCase.Parameter, Int>() {
    override suspend fun execute(parameter: Parameter): Int {
        if (parameter.content.isBlank()) throw ChecklistItemContentBlankException()

        val account = getAccountUseCase.requireAccount()
        val now = clock.now()

        val addedCount =
            accountChecklistItemRepository.upsert(
                account = account,
                item =
                    ChecklistItem(
                        id = Uuid.random(),
                        checklistId = parameter.checklistId,
                        content = parameter.content,
                        isChecked = false,
                        isDeleted = false,
                        updatedAt = now,
                        createdAt = now,
                    ),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return addedCount
    }

    public data class Parameter(
        val checklistId: Uuid,
        val content: String,
    )
}
