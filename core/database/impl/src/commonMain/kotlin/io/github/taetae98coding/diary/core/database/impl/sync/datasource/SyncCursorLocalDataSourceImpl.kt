package io.github.taetae98coding.diary.core.database.impl.sync.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class SyncCursorLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : SyncCursorLocalDataSource {
    override suspend fun read(
        accountId: Uuid,
        kind: SyncKindLocalEntity,
    ): Long =
        database.syncCursorDao().find(
            accountId = accountId,
            kind = kind.persistentValue,
        ) ?: SyncCursorLocalDataSource.DEFAULT_USN
}
