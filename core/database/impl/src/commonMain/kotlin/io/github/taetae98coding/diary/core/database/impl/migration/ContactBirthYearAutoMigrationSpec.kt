package io.github.taetae98coding.diary.core.database.impl.migration

import androidx.room3.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

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
    }
}
