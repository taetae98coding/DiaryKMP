package io.github.taetae98coding.diary.data.memo.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountContactMemoLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.domain.memo.repository.AccountContactMemoRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountContactMemoRepositoryImpl(
    private val accountContactMemoLocalDataSource: AccountContactMemoLocalDataSource,
) : AccountContactMemoRepository {
    override fun page(
        account: Account,
        contactId: Uuid,
        sort: ListSort,
    ): Flow<PagingData<Memo>> =
        pagingFlow(
            pagingSourceFactory = {
                accountContactMemoLocalDataSource.page(
                    accountId = account.id,
                    contactId = contactId,
                    sort = sort.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )
}
