package io.github.taetae98coding.diary.core.database.impl.migration

import androidx.room3.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity

// 태어난 연도 컬럼이 생기기 전에 저장한 생일도 같은 연도를 갖도록 채운다. 서버도 같은 기준으로 채우므로 업로드 대기로 바꾸지 않는다.
internal class ContactBirthYearAutoMigrationSpec : AutoMigrationSpec {
    override suspend fun onPostMigrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            UPDATE contact
            SET birth_year = CAST(strftime('%Y', birthday) AS INTEGER)
            WHERE birthday IS NOT NULL
            """,
        )
        // 이전 버전은 태어난 연도 없이 내려받았으므로, 다른 기기에서 기록한 연도를 받도록 연락처만 처음부터 다시 내려받는다.
        connection.execSQL("DELETE FROM sync_cursor WHERE kind = '${SyncKindLocalEntity.CONTACT.persistentValue}'")
    }
}
