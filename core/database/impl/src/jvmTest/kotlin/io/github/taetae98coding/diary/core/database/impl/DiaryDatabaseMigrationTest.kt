@file:OptIn(ExperimentalPathApi::class)

package io.github.taetae98coding.diary.core.database.impl

import androidx.room3.Room
import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.memo.datasource.AccountMemoSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.qr.transaction.AccountQrTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.qr.transaction.findQrList
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.testing.memo.localMemo
import io.github.taetae98coding.diary.core.testing.qr.localQr
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

// Room Gradle 플러그인이 내보낸 스키마를 테스트가 모듈 폴더 기준으로 읽는다.
private val schemaDirectoryPath: Path = Paths.get("schemas")

class DiaryDatabaseMigrationTest :
    FunSpec({
        lateinit var directory: Path
        lateinit var databasePath: Path
        lateinit var helper: MigrationTestHelper

        beforeTest {
            directory = Files.createTempDirectory("diary-migration")
            databasePath = directory.resolve("diary.db")
            helper =
                MigrationTestHelper(
                    schemaDirectoryPath = schemaDirectoryPath,
                    databasePath = databasePath,
                    driver = BundledSQLiteDriver(),
                    databaseClass = DiaryDatabase::class,
                    databaseFactory = { DiaryDatabaseConstructor.initialize() },
                )
        }

        afterTest {
            directory.deleteRecursively()
        }

        test("TC-DATA-SYNC-DATA-053 앱을 업데이트해도 기기에 저장된 데이터와 동기화 상태가 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo =
                fixtureMonkey.localMemo(isFinished = false, isDeleted = false, primaryTagId = null).let { memo ->
                    memo.copy(detail = memo.detail.copy(isAllDay = null, start = null, endInclusive = null))
                }
            val usn = fixtureMonkey.giveMeOne<Long>()
            val qr = fixtureMonkey.localQr(isDeleted = false)

            helper.createDatabase(version = 1).use { connection ->
                connection.insertPendingMemo(accountId = accountId, memo = memo)
                connection.insertMemoCursor(accountId = accountId, usn = usn)
            }
            helper.runMigrationsAndValidate(version = 2).close()

            val database =
                Room
                    .databaseBuilder<DiaryDatabase>(name = databasePath.toString())
                    .setDriver(BundledSQLiteDriver())
                    .build()
            try {
                AccountMemoSyncLocalDataSourceImpl(database = database).findPending(accountId = accountId) shouldBe listOf(memo)
                SyncCursorLocalDataSourceImpl(database = database).find(accountId = accountId, kind = SyncKind.MEMO) shouldBe usn

                AccountQrTransactionImpl(database = database).upsert(accountId = accountId, qrList = listOf(qr))
                database.findQrList() shouldBe listOf(qr)
            } finally {
                database.close()
            }
        }
    })

private fun SQLiteConnection.insertPendingMemo(
    accountId: Uuid,
    memo: MemoLocalEntity,
) {
    prepare(
        """
        INSERT INTO memo (id, title, description, color, is_finished, is_deleted, updated_at, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """,
    ).use { statement ->
        statement.bindText(1, memo.id.toString())
        statement.bindText(2, memo.detail.title)
        statement.bindText(3, memo.detail.description)
        statement.bindLong(4, memo.detail.color)
        statement.bindBoolean(5, memo.isFinished)
        statement.bindBoolean(6, memo.isDeleted)
        statement.bindLong(7, memo.updatedAt.toEpochMilliseconds())
        statement.bindLong(8, memo.createdAt.toEpochMilliseconds())
        statement.step()
    }
    execSQL("INSERT INTO account_memo (account_id, memo_id, is_dirty) VALUES ('$accountId', '${memo.id}', 1)")
}

private fun SQLiteConnection.insertMemoCursor(
    accountId: Uuid,
    usn: Long,
) {
    execSQL("INSERT INTO sync_cursor (account_id, kind, usn) VALUES ('$accountId', 'memo', $usn)")
}
