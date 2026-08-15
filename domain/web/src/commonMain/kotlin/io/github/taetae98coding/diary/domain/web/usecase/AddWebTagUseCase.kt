package io.github.taetae98coding.diary.domain.web.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.web.repository.AccountWebTagRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddWebTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountWebTagRepository: AccountWebTagRepository,
    private val clock: Clock,
) : UseCase<AddWebTagUseCase.Parameter, Unit>() {
    override suspend fun execute(parameter: Parameter) {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        accountWebTagRepository.upsert(
            account = account,
            webId = parameter.webId,
            tagId = parameter.tagId,
            isDeleted = false,
            updatedAt = clock.now(),
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
    }

    public data class Parameter(
        val webId: Uuid,
        val tagId: Uuid,
    )
}
