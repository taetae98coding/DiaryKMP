package io.github.taetae98coding.diary.domain.routine.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class PageRoutineRecordUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountRoutineRecordRepository: AccountRoutineRecordRepository,
) : FlowUseCase<Uuid, PagingData<RoutineRecord>>() {
    override fun execute(parameter: Uuid): Flow<Result<PagingData<RoutineRecord>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountRoutineRecordRepository
                .page(
                    account = account,
                    routineId = parameter,
                ).map { pagingData -> Result.success(pagingData) }
        }
}
