package io.github.taetae98coding.diary.domain.memo.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Inject
public class MoveMemoUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountMemoRepository: AccountMemoRepository,
    private val clock: Clock,
) : UseCase<MoveMemoUseCase.Parameter, Unit>() {
    override suspend fun execute(parameter: Parameter) {
        val account = getAccountUseCase.requireAccount()
        val memo = accountMemoRepository.find(account = account, memoId = parameter.id).first()

        if (memo == null || memo.isDeleted) return

        accountMemoRepository.updateDetail(
            account = account,
            memoId = parameter.id,
            detail = memo.detail.copy(dateTime = parameter.fromDateTime.moveTo(dateRange = parameter.toDateRange)),
            updatedAt = clock.now(),
        )
        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
    }

    public data class Parameter(
        val id: Uuid,
        val fromDateTime: MemoDateTime,
        val toDateRange: LocalDateRange,
    )
}

private fun MemoDateTime.moveTo(dateRange: LocalDateRange): MemoDateTime =
    when (this) {
        is MemoDateTime.AllDay -> MemoDateTime.AllDay(dateRange = dateRange)

        is MemoDateTime.DateTime ->
            MemoDateTime.DateTime(
                start = LocalDateTime(date = dateRange.start, time = start.time),
                endInclusive = LocalDateTime(date = dateRange.endInclusive, time = endInclusive.time),
            )
    }
