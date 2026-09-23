package io.github.taetae98coding.diary.domain.contact.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class UnfavoriteContactUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountContactRepository: AccountContactRepository,
    private val clock: Clock,
) : UseCase<Uuid, Int>() {
    override suspend fun execute(parameter: Uuid): Int {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        val updatedCount =
            accountContactRepository.updateFavorite(
                account = account,
                contactId = parameter,
                isFavorite = false,
                updatedAt = clock.now(),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return updatedCount
    }
}
