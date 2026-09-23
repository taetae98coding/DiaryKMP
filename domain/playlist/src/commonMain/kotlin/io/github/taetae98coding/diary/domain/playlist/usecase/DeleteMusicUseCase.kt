package io.github.taetae98coding.diary.domain.playlist.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.playlist.repository.AccountMusicRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class DeleteMusicUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountMusicRepository: AccountMusicRepository,
    private val clock: Clock,
) : UseCase<Uuid, Int>() {
    override suspend fun execute(parameter: Uuid): Int {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        val updatedCount =
            accountMusicRepository.updateDeleted(
                account = account,
                musicId = parameter,
                isDeleted = true,
                updatedAt = clock.now(),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return updatedCount
    }
}
