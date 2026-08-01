package io.github.taetae98coding.diary.data.memo.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountTagMemoLocalDataSource
import io.github.taetae98coding.diary.core.mapper.list.toLocal
import io.github.taetae98coding.diary.core.mapper.memo.toDomain
import io.github.taetae98coding.diary.core.mapper.tag.toLocal
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.memo.repository.AccountTagMemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountTagMemoLocalDataSource.page(
                    accountId = account.id,
                    tagId = tagId,
                    scope = scope.toLocal(),
                    sort = sort.toLocal(),
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    override fun pageFinished(
        account: Account,
        tagId: Uuid,
        sort: ListSort,
    ): Flow<PagingData<Memo>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = {
                accountTagMemoLocalDataSource.pageFinished(
                    accountId = account.id,
                    tagId = tagId,
                    sort = sort.toLocal(),
                )
            },
        ).flow.map { pagingData ->
            pagingData.map { local -> local.toDomain() }
        }

    private companion object {
        const val PAGE_SIZE: Int = 20
    }
}
