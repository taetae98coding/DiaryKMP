package io.github.taetae98coding.diary.core.database.impl.ddayfilter.datasource

import io.github.taetae98coding.diary.core.database.api.ddayfilter.datasource.AccountDDayFilterLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.ddayfilter.entity.DDayFilterTagLocalEntity
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountDDayFilterLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountDDayFilterLocalDataSource {
    override fun getTagList(accountId: Uuid): Flow<List<TagLocalEntity>> = database.ddayFilterTagDao().getTagList(accountId = accountId)

    override fun getTagIdList(accountId: Uuid): Flow<List<Uuid>> = database.ddayFilterTagDao().getTagIdList(accountId = accountId)

    override suspend fun upsert(
        accountId: Uuid,
        tagId: Uuid,
    ) {
        database.ddayFilterTagDao().upsert(
            entity =
                DDayFilterTagLocalEntity(
                    accountId = accountId,
                    tagId = tagId,
                ),
        )
    }

    override suspend fun delete(
        accountId: Uuid,
        tagId: Uuid,
    ) {
        database.ddayFilterTagDao().delete(
            accountId = accountId,
            tagId = tagId,
        )
    }

    override suspend fun deleteAll(accountId: Uuid) {
        database.ddayFilterTagDao().deleteAll(accountId = accountId)
    }
}
