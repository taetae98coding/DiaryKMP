package io.github.taetae98coding.diary.core.database.impl.migration

import androidx.room3.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity

internal class ContactHowWeMetAutoMigrationSpec : AutoMigrationSpec {
    override suspend fun onPostMigrate(connection: SQLiteConnection) {
        // 이전 버전은 알게 된 경로 없이 내려받았으므로, 다른 기기에서 기록한 값을 받도록 연락처만 처음부터 다시 내려받는다.
        connection.execSQL("DELETE FROM sync_cursor WHERE kind = '${SyncKindLocalEntity.CONTACT.persistentValue}'")
    }
}
