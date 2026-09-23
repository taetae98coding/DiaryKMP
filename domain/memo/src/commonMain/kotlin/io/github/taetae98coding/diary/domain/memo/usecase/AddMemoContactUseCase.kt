package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoContactRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddMemoContactUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountMemoContactRepository: AccountMemoContactRepository,
    private val clock: Clock,
) : UseCase<AddMemoContactUseCase.Parameter, Unit>() {
    override suspend fun execute(parameter: Parameter) {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        accountMemoContactRepository.upsert(
            account = account,
            memoId = parameter.memoId,
            contactId = parameter.contactId,
            isDeleted = false,
            updatedAt = clock.now(),
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
    }

    public data class Parameter(
        val memoId: Uuid,
        val contactId: Uuid,
    )
}
