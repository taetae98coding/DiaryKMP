package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineFilterRepository
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class UnselectRoutineFilterTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountRoutineFilterRepository: AccountRoutineFilterRepository,
) : UseCase<Uuid, Unit>() {
    override suspend fun execute(parameter: Uuid) {
        val account = getAccountUseCase.requireAccount()

        accountRoutineFilterRepository.delete(
            account = account,
            tagId = parameter,
        )
    }
}
