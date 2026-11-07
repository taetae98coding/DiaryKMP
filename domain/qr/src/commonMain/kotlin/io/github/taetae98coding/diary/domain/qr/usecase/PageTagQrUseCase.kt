package io.github.taetae98coding.diary.domain.qr.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.qr.repository.AccountTagQrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class PageTagQrUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagQrRepository: AccountTagQrRepository,
) : FlowUseCase<PageTagQrUseCase.Parameter, PagingData<Qr>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Qr>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountTagQrRepository
                .page(
                    account = account,
                    tagId = parameter.tagId,
                    scope = parameter.scope,
                ).map { pagingData -> Result.success(pagingData) }
        }

    public data class Parameter(
        val tagId: Uuid,
        val scope: TagScope,
    )
}
