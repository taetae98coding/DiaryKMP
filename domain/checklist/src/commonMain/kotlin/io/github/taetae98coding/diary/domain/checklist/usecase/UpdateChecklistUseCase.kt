package io.github.taetae98coding.diary.domain.checklist.usecase

import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class UpdateChecklistUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val findChecklistUseCase: FindChecklistUseCase,
    private val accountChecklistRepository: AccountChecklistRepository,
    private val clock: Clock,
) : UseCase<UpdateChecklistUseCase.Parameter, Int>() {
    override suspend fun execute(parameter: Parameter): Int {
        val account = getAccountUseCase.requireAccount()
        val detail = parameter.detail.withStoredTitleForBlank(id = parameter.id)

        val updatedCount =
            accountChecklistRepository.updateDetail(
                account = account,
                checklistId = parameter.id,
                detail = detail,
                updatedAt = clock.now(),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return updatedCount
    }

    private suspend fun ChecklistDetail.withStoredTitleForBlank(id: Uuid): ChecklistDetail {
        if (title.isNotBlank()) return this

        val stored =
            findChecklistUseCase(parameter = id)
                .first()
                .getOrThrow()
                ?.detail

        return copy(title = stored?.title.orEmpty())
    }

    public data class Parameter(
        val id: Uuid,
        val detail: ChecklistDetail,
    )
}
