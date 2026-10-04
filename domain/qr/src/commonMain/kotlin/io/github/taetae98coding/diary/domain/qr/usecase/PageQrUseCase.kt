package io.github.taetae98coding.diary.domain.qr.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.qr.repository.AccountQrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class PageQrUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountQrRepository: AccountQrRepository,
) : FlowUseCase<Unit, PagingData<Qr>>() {
    override fun execute(parameter: Unit): Flow<Result<PagingData<Qr>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountQrRepository
                .page(account = account)
                .map { pagingData -> Result.success(pagingData) }
        }
}
