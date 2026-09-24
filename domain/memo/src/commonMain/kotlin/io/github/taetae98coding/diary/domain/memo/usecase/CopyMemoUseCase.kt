package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoContactRepository
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoPlaceRepository
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoRepository
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoTagRepository
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoWebRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class CopyMemoUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val findMemoUseCase: FindMemoUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountMemoRepository: AccountMemoRepository,
    private val accountMemoTagRepository: AccountMemoTagRepository,
    private val accountMemoWebRepository: AccountMemoWebRepository,
    private val accountMemoContactRepository: AccountMemoContactRepository,
    private val accountMemoPlaceRepository: AccountMemoPlaceRepository,
    private val clock: Clock,
) : UseCase<Uuid, Uuid>() {
    override suspend fun execute(parameter: Uuid): Uuid {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()
        val source = requireNotNull(findMemoUseCase(parameter).first().getOrThrow())
        val now = clock.now()
        val tagIdSet =
            accountMemoTagRepository.findTagIdSet(
                account = account,
                memoId = source.id,
            ) + setOfNotNull(source.primaryTagId)
        val webIdSet =
            accountMemoWebRepository.findWebIdSet(
                account = account,
                memoId = source.id,
            )
        val contactIdSet =
            accountMemoContactRepository.findContactIdSet(
                account = account,
                memoId = source.id,
            )
        val placeIdSet =
            accountMemoPlaceRepository.findPlaceIdSet(
                account = account,
                memoId = source.id,
            )
        val memo =
            Memo(
                id = Uuid.random(),
                detail = source.detail,
                primaryTagId = source.primaryTagId,
                isFinished = false,
                isDeleted = false,
                updatedAt = now,
                createdAt = now,
            )

        accountMemoRepository.upsert(
            account = account,
            memo = memo,
            tagIdSet = tagIdSet,
            placeIdSet = placeIdSet,
            webIdSet = webIdSet,
            contactIdSet = contactIdSet,
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return memo.id
    }
}
