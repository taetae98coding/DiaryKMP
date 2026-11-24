package io.github.taetae98coding.diary.domain.routine.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.requireAccount
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineFilterRepository
import kotlin.uuid.Uuid

@Inject
public class SelectRoutineFilterTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountRoutineFilterRepository: AccountRoutineFilterRepository,
) : UseCase<Uuid, Unit>() {
    override suspend fun execute(parameter: Uuid) {
        val account = getAccountUseCase.requireAccount()

        accountRoutineFilterRepository.upsert(
            account = account,
            tagId = parameter,
        )
    }
}
