package io.github.taetae98coding.diary.core.database.impl.datasource

import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entity.SyncCursorLocalEntity
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class SyncCursorLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : SyncCursorLocalDataSource {
    override suspend fun find(
        accountId: Uuid,
        kind: SyncKind,
    ): Long =
        database.syncCursorDao().find(
            accountId = accountId,
            kind = SyncCursorLocalEntity.column(kind = kind),
        ) ?: SyncCursorLocalDataSource.DEFAULT_USN
}
