package io.github.taetae98coding.diary.core.database.impl.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.memotag.datasource.AccountMemoTagLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoTagLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoTagLocalDataSource {
    override fun getTagList(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<List<TagLocalEntity>> =
        database.accountMemoTagDao().getTagList(
            accountId = accountId,
            memoId = memoId,
        )

    override fun pageSelectableTag(
        accountId: Uuid,
        memoId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity> =
        database.accountMemoTagDao().pageSelectableTag(
            accountId = accountId,
            memoId = memoId,
            query = query,
        )

    override suspend fun findTagIdList(
        accountId: Uuid,
        memoId: Uuid,
    ): List<Uuid> =
        database.accountMemoTagDao().findTagIdList(
            accountId = accountId,
            memoId = memoId,
        )
}
