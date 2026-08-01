package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoFilterRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class UnselectMemoFilterTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountMemoFilterRepository: AccountMemoFilterRepository,
) : UseCase<Uuid, Unit>() {
    override suspend fun execute(parameter: Uuid) {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        accountMemoFilterRepository.delete(
            account = account,
            tagId = parameter,
        )
    }
}
