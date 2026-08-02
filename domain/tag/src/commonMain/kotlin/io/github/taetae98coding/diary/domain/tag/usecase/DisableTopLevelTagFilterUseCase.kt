package io.github.taetae98coding.diary.domain.tag.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagFilterRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class DisableTopLevelTagFilterUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagFilterRepository: AccountTagFilterRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        accountTagFilterRepository.upsert(
            account = account,
            isTopLevelOnly = false,
        )
    }
}
