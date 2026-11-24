package io.github.taetae98coding.diary.core.database.impl.tagfilter.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.tagfilter.datasource.AccountTagFilterLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tagfilter.entity.TagFilterLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
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
