package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class UpdateMemoUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val findMemoUseCase: FindMemoUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountMemoRepository: AccountMemoRepository,
    private val clock: Clock,
) : UseCase<UpdateMemoUseCase.Parameter, Int>() {
    override suspend fun execute(parameter: Parameter): Int {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()
        val detail =
            parameter.detail.copy(
                title =
                    parameter.detail.title.ifBlank {
                        findMemoUseCase(parameter.id)
                            .first()
                            .getOrThrow()
                            ?.detail
                            ?.title
                            .orEmpty()
                    },
            )

        val count =
            accountMemoRepository.updateDetail(
                account = account,
                memoId = parameter.id,
                detail = detail,
                updatedAt = clock.now(),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return count
    }

    public data class Parameter(
        val id: Uuid,
        val detail: MemoDetail,
    )
}
