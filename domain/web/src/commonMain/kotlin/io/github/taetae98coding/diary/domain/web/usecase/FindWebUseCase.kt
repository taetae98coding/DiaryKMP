package io.github.taetae98coding.diary.domain.web.usecase

import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.web.repository.AccountWebRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class FindWebUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountWebRepository: AccountWebRepository,
) : FlowUseCase<Uuid, Web?>() {
    override fun execute(parameter: Uuid): Flow<Result<Web?>> =
        getAccountUseCase.flatMapAccount { account ->
            accountWebRepository
                .find(
                    account = account,
                    webId = parameter,
                ).map { web -> Result.success(web) }
        }
}
