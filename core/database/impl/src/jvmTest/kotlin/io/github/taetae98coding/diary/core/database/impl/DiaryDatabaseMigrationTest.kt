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
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.AccountChecklistTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.findChecklistItemList
import io.github.taetae98coding.diary.core.database.impl.checklist.transaction.findChecklistList
import io.github.taetae98coding.diary.core.database.impl.memo.datasource.AccountMemoSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.qr.transaction.AccountQrTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.qr.transaction.findQrList
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.testing.checklist.localChecklist
import io.github.taetae98coding.diary.core.testing.checklist.localChecklistItem
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
import kotlin.time.Instant
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

        test("TC-20a28b81-a5c0-457d-821b-b734aa885427 앱을 업데이트해도 기기에 저장된 데이터와 동기화 상태가 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo =
                fixtureMonkey.localMemo(isFinished = false, isDeleted = false, primaryTagId = null).let { memo ->
                    memo.copy(detail = memo.detail.copy(isAllDay = null, start = null, endInclusive = null))
                }
            val usn = fixtureMonkey.giveMeOne<Long>()
            val qr = fixtureMonkey.localQr(isDeleted = false)
            val checklist = fixtureMonkey.localChecklist(isDeleted = false)
            val checklistItem = fixtureMonkey.localChecklistItem(isChecked = true, isDeleted = false, checklistId = checklist.id)

            helper.createDatabase(version = 1).use { connection ->
                connection.insertPendingMemo(accountId = accountId, memo = memo)
                connection.insertMemoCursor(accountId = accountId, usn = usn)
            }
            helper.runMigrationsAndValidate(version = 4).close()

            val database =
                Room
                    .databaseBuilder<DiaryDatabase>(name = databasePath.toString())
                    .setDriver(BundledSQLiteDriver())
                    .build()
            try {
                AccountMemoSyncLocalDataSourceImpl(database = database).readPendingList(accountId = accountId) shouldBe listOf(memo)
                SyncCursorLocalDataSourceImpl(database = database).read(accountId = accountId, kind = SyncKindLocalEntity.MEMO) shouldBe usn

                AccountQrTransactionImpl(database = database).upsert(accountId = accountId, qrList = listOf(qr))
                database.findQrList() shouldBe listOf(qr)

                AccountChecklistTransactionImpl(database = database).upsert(
                    accountId = accountId,
                    checklist = checklist,
                    itemList = listOf(checklistItem),
                )
                database.findChecklistList() shouldBe listOf(checklist)
                database.findChecklistItemList() shouldBe listOf(checklistItem)
            } finally {
                database.close()
            }
        }

        test("TC-0b67c147-01b1-4f5f-8b63-d27db810cc3f 앱을 업데이트하면 연락처만 처음부터 다시 내려받는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            // 지워진 위치는 기본값 0으로 읽히므로, 지워지지 않았을 때 구분되도록 0보다 큰 값을 둔다.
            val contactUsn = fixtureMonkey.giveMeOne<Long>().coerceAtLeast(1L)
            val memoUsn = fixtureMonkey.giveMeOne<Long>()

            helper.createDatabase(version = 2).use { connection ->
                connection.execSQL("INSERT INTO sync_cursor (account_id, kind, usn) VALUES ('$accountId', 'contact', $contactUsn)")
                connection.insertMemoCursor(accountId = accountId, usn = memoUsn)
            }
            helper.runMigrationsAndValidate(version = 3).close()

            val database =
                Room
                    .databaseBuilder<DiaryDatabase>(name = databasePath.toString())
                    .setDriver(BundledSQLiteDriver())
                    .build()
            try {
                val cursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
                cursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.CONTACT) shouldBe 0L
                cursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.MEMO) shouldBe memoUsn
            } finally {
                database.close()
            }
        }

        test("TC-635c851c-6f40-43f2-be72-fbb7849562c1 앱을 업데이트하면 기존 연락처의 태어난 연도를 생일의 연도로 채운다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val birthdayContactId = fixtureMonkey.giveMeOne<Uuid>()
            val noBirthdayContactId = fixtureMonkey.giveMeOne<Uuid>()
            val updatedAt = fixtureMonkey.giveMeOne<Instant>().toEpochMilliseconds()

            helper.createDatabase(version = 2).use { connection ->
                connection.execSQL(
                    "INSERT INTO contact (id, name, birthday, birthday_calendar, updated_at) VALUES ('$birthdayContactId', 'birthday', '1990-12-20', 'lunar', $updatedAt)",
                )
                connection.execSQL("INSERT INTO contact (id, name, updated_at) VALUES ('$noBirthdayContactId', 'no birthday', $updatedAt)")
                listOf(birthdayContactId, noBirthdayContactId).forEach { contactId ->
                    connection.execSQL("INSERT INTO account_contact (account_id, contact_id, is_dirty) VALUES ('$accountId', '$contactId', 0)")
                }
            }

            helper.runMigrationsAndValidate(version = 3).use { connection ->
                connection.readBirthYear(contactId = birthdayContactId) shouldBe 1990
                connection.readBirthYear(contactId = noBirthdayContactId) shouldBe null
                listOf(birthdayContactId, noBirthdayContactId).forEach { contactId ->
                    connection.readLong(sql = "SELECT updated_at FROM contact WHERE id = ?", contactId = contactId) shouldBe updatedAt
                    connection.readLong(sql = "SELECT is_dirty FROM account_contact WHERE contact_id = ?", contactId = contactId) shouldBe 0
                }
            }
        }
    })

private fun SQLiteConnection.readLong(
    sql: String,
    contactId: Uuid,
): Long =
    prepare(sql).use { statement ->
        statement.bindText(1, contactId.toString())
        statement.step()
        statement.getLong(0)
    }

private fun SQLiteConnection.readBirthYear(contactId: Uuid): Long? =
    prepare("SELECT birth_year FROM contact WHERE id = ?").use { statement ->
        statement.bindText(1, contactId.toString())
        statement.step()
        if (statement.isNull(0)) null else statement.getLong(0)
    }

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
