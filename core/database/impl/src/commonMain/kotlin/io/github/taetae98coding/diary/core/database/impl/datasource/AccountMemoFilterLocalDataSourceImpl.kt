package io.github.taetae98coding.diary.core.database.impl.datasource

import io.github.taetae98coding.diary.core.database.api.memofilter.datasource.AccountMemoFilterLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.MemoFilterTagLocalEntity
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoFilterLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoFilterLocalDataSource {
    override fun getTagList(accountId: Uuid): Flow<List<TagLocalEntity>> = database.memoFilterTagDao().getTagList(accountId = accountId)

    override suspend fun upsert(
        accountId: Uuid,
        tagId: Uuid,
    ) {
        database.memoFilterTagDao().upsert(
            entity =
                MemoFilterTagLocalEntity(
                    accountId = accountId,
                    tagId = tagId,
                ),
        )
    }

    override suspend fun delete(
        accountId: Uuid,
        tagId: Uuid,
    ) {
        database.memoFilterTagDao().delete(
            accountId = accountId,
            tagId = tagId,
        )
    }

    override suspend fun deleteAll(accountId: Uuid) {
        database.memoFilterTagDao().deleteAll(accountId = accountId)
    }
}
