package io.github.taetae98coding.diary.domain.checklist.usecase

import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.checklist.exception.ChecklistTitleBlankException
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

@Factory
public class AddChecklistUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountChecklistRepository: AccountChecklistRepository,
    private val clock: Clock,
) : UseCase<AddChecklistUseCase.Parameter, Uuid>() {
    override suspend fun execute(parameter: Parameter): Uuid {
        if (parameter.detail.title.isBlank()) throw ChecklistTitleBlankException()

        val account = getAccountUseCase.requireAccount()
        val now = clock.now()
        val checklist =
            Checklist(
                id = Uuid.random(),
                detail = parameter.detail,
                isDeleted = false,
                updatedAt = now,
                createdAt = now,
            )
        val itemList =
            parameter.itemContentList
                .filter { content -> content.isNotBlank() }
                .mapIndexed { index, content ->
                    val createdAt = now + index.milliseconds

                    ChecklistItem(
                        id = Uuid.random(),
                        checklistId = checklist.id,
                        content = content,
                        isChecked = false,
                        isDeleted = false,
                        updatedAt = createdAt,
                        createdAt = createdAt,
                    )
                }

        accountChecklistRepository.upsert(
            account = account,
            checklist = checklist,
            itemList = itemList,
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return checklist.id
    }

    public data class Parameter(
        val detail: ChecklistDetail,
        val itemContentList: List<String>,
    )
}
