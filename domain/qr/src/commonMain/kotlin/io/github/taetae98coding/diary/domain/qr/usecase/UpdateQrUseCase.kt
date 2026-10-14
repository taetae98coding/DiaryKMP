package io.github.taetae98coding.diary.domain.qr.usecase

import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.repository.AccountQrRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class UpdateQrUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val findQrUseCase: FindQrUseCase,
    private val accountQrRepository: AccountQrRepository,
    private val clock: Clock,
) : UseCase<UpdateQrUseCase.Parameter, Int>() {
    override suspend fun execute(parameter: Parameter): Int {
        if (parameter.detail.value.isEmpty()) throw QrValueEmptyException()

        val account = getAccountUseCase.requireAccount()
        val detail = parameter.detail.withStoredTitleForBlank(id = parameter.id)

        val updatedCount =
            accountQrRepository.updateDetail(
                account = account,
                qrId = parameter.id,
                detail = detail,
                updatedAt = clock.now(),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return updatedCount
    }

    private suspend fun QrDetail.withStoredTitleForBlank(id: Uuid): QrDetail {
        if (title.isNotBlank()) return this

        val stored =
            findQrUseCase(parameter = id)
                .first()
                .getOrThrow()
                ?.detail

        return copy(title = stored?.title.orEmpty())
    }

    public data class Parameter(
        val id: Uuid,
        val detail: QrDetail,
    )
}
