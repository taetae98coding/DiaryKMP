package io.github.taetae98coding.diary.domain.tag.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class FinishTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountTagRepository: AccountTagRepository,
    private val clock: Clock,
) : UseCase<Uuid, Int>() {
    override suspend fun execute(parameter: Uuid): Int {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        val count =
            accountTagRepository.updateFinished(
                account = account,
                tagId = parameter,
                isFinished = true,
                updatedAt = clock.now(),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return count
    }
}
