package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.memo.exception.MemoTitleBlankException
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddMemoUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountMemoRepository: AccountMemoRepository,
    private val clock: Clock,
) : UseCase<AddMemoUseCase.Parameter, Uuid>() {
    override suspend fun execute(parameter: Parameter): Uuid {
        if (parameter.detail.title.isBlank()) throw MemoTitleBlankException()

        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()
        val now = clock.now()
        val tagIdSet = parameter.tagIdSet + setOfNotNull(parameter.primaryTagId)
        val memo =
            Memo(
                id = Uuid.random(),
                detail = parameter.detail,
                primaryTagId = parameter.primaryTagId,
                isFinished = false,
                isDeleted = false,
                updatedAt = now,
                createdAt = now,
            )

        accountMemoRepository.upsert(
            account = account,
            memo = memo,
            tagIdSet = tagIdSet,
            placeIdSet = parameter.placeIdSet,
            webIdSet = parameter.webIdSet,
            contactIdSet = parameter.contactIdSet,
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return memo.id
    }

    public data class Parameter(
        val detail: MemoDetail,
        val primaryTagId: Uuid? = null,
        val tagIdSet: Set<Uuid> = emptySet(),
        val placeIdSet: Set<Uuid> = emptySet(),
        val webIdSet: Set<Uuid> = emptySet(),
        val contactIdSet: Set<Uuid> = emptySet(),
    )
}
