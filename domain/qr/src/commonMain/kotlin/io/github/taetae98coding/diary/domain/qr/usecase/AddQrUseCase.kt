package io.github.taetae98coding.diary.domain.qr.usecase

import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.qr.exception.QrTitleBlankException
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.repository.AccountQrRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddQrUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountQrRepository: AccountQrRepository,
    private val clock: Clock,
) : UseCase<QrDetail, Uuid>() {
    override suspend fun execute(parameter: QrDetail): Uuid {
        parameter.validate()

        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()
        val now = clock.now()
        val qr =
            Qr(
                id = Uuid.random(),
                detail = parameter,
                isDeleted = false,
                updatedAt = now,
                createdAt = now,
            )

        accountQrRepository.upsert(
            account = account,
            qr = qr,
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return qr.id
    }

    private fun QrDetail.validate() {
        if (title.isBlank()) throw QrTitleBlankException()
        if (value.isEmpty()) throw QrValueEmptyException()
    }
}
