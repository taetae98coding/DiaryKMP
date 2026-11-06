package io.github.taetae98coding.diary.data.qr.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.qr.datasource.AccountTagQrLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.data.qr.mapper.toDomain
import io.github.taetae98coding.diary.domain.qr.repository.AccountTagQrRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagQrRepositoryImpl(
    private val accountTagQrLocalDataSource: AccountTagQrLocalDataSource,
) : AccountTagQrRepository {
    override fun page(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
    ): Flow<PagingData<Qr>> =
        pagingFlow(
            pagingSourceFactory = {
                accountTagQrLocalDataSource.page(
                    accountId = account.id,
                    tagId = tagId,
                    scope = scope.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )
}
