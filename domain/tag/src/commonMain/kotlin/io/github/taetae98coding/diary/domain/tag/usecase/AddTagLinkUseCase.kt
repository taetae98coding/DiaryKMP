package io.github.taetae98coding.diary.domain.tag.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.tag.exception.TagLinkSelfException
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagLinkRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddTagLinkUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountTagLinkRepository: AccountTagLinkRepository,
    private val clock: Clock,
) : UseCase<AddTagLinkUseCase.Parameter, Unit>() {
    override suspend fun execute(parameter: Parameter) {
        if (parameter.fromTagId == parameter.toTagId) throw TagLinkSelfException()

        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        accountTagLinkRepository.upsert(
            account = account,
            fromTagId = parameter.fromTagId,
            toTagId = parameter.toTagId,
            isDeleted = false,
            updatedAt = clock.now(),
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
    }

    public data class Parameter(
        val fromTagId: Uuid,
        val toTagId: Uuid,
    )
}
