package io.github.taetae98coding.diary.domain.dday.usecase

import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayRepository
import io.github.taetae98coding.diary.domain.dday.toDetail
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class UpdateDDayUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val findDDayUseCase: FindDDayUseCase,
    private val accountDDayRepository: AccountDDayRepository,
    private val clock: Clock,
) : UseCase<UpdateDDayUseCase.Parameter, Int>() {
    override suspend fun execute(parameter: Parameter): Int {
        val inputDetail = parameter.input.toDetail()
        val account = getAccountUseCase.requireAccount()
        val detail = inputDetail.withStoredTitleForBlank(id = parameter.id)

        val updatedCount =
            accountDDayRepository.updateDetail(
                account = account,
                ddayId = parameter.id,
                detail = detail,
                updatedAt = clock.now(),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return updatedCount
    }

    private suspend fun DDayDetail.withStoredTitleForBlank(id: Uuid): DDayDetail {
        if (title.isNotBlank()) return this

        val stored =
            findDDayUseCase(parameter = id)
                .first()
                .getOrThrow()
                ?.detail

        return copy(title = stored?.title.orEmpty())
    }

    public data class Parameter(
        val id: Uuid,
        val input: DDayInput,
    )
}
