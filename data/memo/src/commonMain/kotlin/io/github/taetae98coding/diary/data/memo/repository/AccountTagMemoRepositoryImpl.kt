package io.github.taetae98coding.diary.data.memo.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountTagMemoLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.domain.memo.repository.AccountTagMemoRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagMemoRepositoryImpl(
    private val accountTagMemoLocalDataSource: AccountTagMemoLocalDataSource,
) : AccountTagMemoRepository {
    override fun page(
        account: Account,
        tagId: Uuid,
        scope: TagScope,
        sort: ListSort,
    ): Flow<PagingData<Memo>> =
        pagingFlow(
            pagingSourceFactory = {
                accountTagMemoLocalDataSource.page(
                    accountId = account.id,
                    tagId = tagId,
                    scope = scope.toLocal(),
                    sort = sort.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )

    override fun pageFinished(
        account: Account,
        tagId: Uuid,
        sort: ListSort,
    ): Flow<PagingData<Memo>> =
        pagingFlow(
            pagingSourceFactory = {
                accountTagMemoLocalDataSource.pageFinished(
                    accountId = account.id,
                    tagId = tagId,
                    sort = sort.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )
}
