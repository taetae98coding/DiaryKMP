package io.github.taetae98coding.diary.domain.tag.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.repository.AccountEntityTagRepository
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class RemoveEntityTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountEntityTagRepository: AccountEntityTagRepository,
    private val clock: Clock,
) : UseCase<RemoveEntityTagUseCase.Parameter, Unit>() {
    override suspend fun execute(parameter: Parameter) {
        val account = getAccountUseCase.requireAccount()

        accountEntityTagRepository.upsert(
            account = account,
            kind = parameter.kind,
            entityId = parameter.entityId,
            tagId = parameter.tagId,
            isDeleted = true,
            updatedAt = clock.now(),
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
    }

    public data class Parameter(
        val kind: EntityTagKind,
        val entityId: Uuid,
        val tagId: Uuid,
    )
}
