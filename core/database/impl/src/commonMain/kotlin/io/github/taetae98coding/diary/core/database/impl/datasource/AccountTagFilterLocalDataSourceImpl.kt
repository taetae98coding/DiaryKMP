package io.github.taetae98coding.diary.core.database.impl.datasource

import io.github.taetae98coding.diary.core.database.api.tagfilter.datasource.AccountTagFilterLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tagfilter.entity.TagFilterLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagFilterLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagFilterLocalDataSource {
    override fun find(accountId: Uuid): Flow<TagFilterLocalEntity?> = database.accountTagFilterDao().find(accountId = accountId)

    override suspend fun upsert(
        accountId: Uuid,
        isTopLevelOnly: Boolean,
    ) {
        database.accountTagFilterDao().upsert(
            entity =
                TagFilterLocalEntity(
                    accountId = accountId,
                    isTopLevelOnly = isTopLevelOnly,
                ),
        )
    }
}
