package io.github.taetae98coding.diary.core.database.impl.qr.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.qr.datasource.AccountQrSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.qr.entity.AccountQrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.testing.qr.localQr
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class QrSyncTestException : RuntimeException()

class AccountQrSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountQrSyncTransactionImpl
        lateinit var syncDataSource: AccountQrSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountQrSyncTransactionImpl(database = database)
            syncDataSource = AccountQrSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            qr: QrLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.qrDao().upsert(listOf(qr))
                database.accountQrDao().upsert(AccountQrLocalEntity(accountId = accountId, qrId = qr.id, isDirty = isDirty))
            }
        }

        suspend fun isPending(
            accountId: Uuid,
            qrId: Uuid,
        ): Boolean = syncDataSource.readPendingList(accountId = accountId).any { qr -> qr.id == qrId }

        test("TC-36179c3a-4170-42bc-beda-73aa6e1b75a0 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 QR은 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val guestEntity = fixtureMonkey.localQr(isDeleted = false)
            val accountEntity = fixtureMonkey.localQr(isDeleted = false)
            insertWithSyncState(accountId = Uuid.NIL, qr = guestEntity, isDirty = true)
            insertWithSyncState(accountId = accountId, qr = accountEntity, isDirty = true)

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(accountEntity)
        }

        test("업로드 대기가 아닌 QR은 업로드 대상에 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            insertWithSyncState(accountId = accountId, qr = fixtureMonkey.localQr(isDeleted = false), isDirty = false)

            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-34019de4-2dbc-40fd-9c1b-c494f2543998 추가한 QR은 계정의 업로드 대기로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val qr = fixtureMonkey.localQr(isDeleted = false)

            AccountQrTransactionImpl(database = database).upsert(accountId = accountId, qrList = listOf(qr))

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(qr)
        }

        listOf(
            true to "삭제",
            false to "실행 취소",
        ).forEach { (isDeleted, label) ->
            test("TC-9f59d662-8251-4eb6-85e1-8fd2875148cf ${label}한 QR은 계정의 업로드 대기로 기록된다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val qr = fixtureMonkey.localQr(isDeleted = !isDeleted)
                val updatedAt = instant()
                insertWithSyncState(accountId = accountId, qr = qr, isDirty = false)

                AccountQrTransactionImpl(database = database).updateDeleted(
                    accountId = accountId,
                    qrId = qr.id,
                    isDeleted = isDeleted,
                    updatedAt = updatedAt,
                ) shouldBe 1

                syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(qr.copy(isDeleted = isDeleted, updatedAt = updatedAt))
            }
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val qr = fixtureMonkey.localQr(isDeleted = false)
            insertWithSyncState(accountId = accountId, qr = qr, isDirty = true)

            transaction.clearPending(accountId = accountId, qrList = listOf(qr))

            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-882adb8e-cd96-4862-a529-585bb17af19a 업로드 중 수정 시각이 바뀐 QR은 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushedQr = fixtureMonkey.localQr(isDeleted = false).copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changedQr = pushedQr.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId = accountId, qr = changedQr, isDirty = true)

            transaction.clearPending(accountId = accountId, qrList = listOf(pushedQr))

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(changedQr)
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-7fe36220-e35f-4c2b-9172-094e9bf95870 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localQr = fixtureMonkey.localQr(isDeleted = false).copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remoteQr =
                    localQr.copy(
                        detail = fixtureMonkey.giveMeOne<QrDetailLocalEntity>(),
                        isDeleted = !localQr.isDeleted,
                        updatedAt = remoteUpdatedAt,
                    )
                insertWithSyncState(accountId = accountId, qr = localQr, isDirty = false)

                transaction.upsert(accountId = accountId, qrList = listOf(remoteQr), cursor = 5L)

                database.findQrList() shouldBe listOf(remoteQr)
            }
        }

        test("TC-2f3591f7-e9cb-4759-a9aa-01fbf0331179 기기의 수정 시각이 더 늦으면 기기 내용을 유지하고 커서는 전진한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val localQr = fixtureMonkey.localQr(isDeleted = false).copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remoteQr =
                localQr.copy(
                    detail = fixtureMonkey.giveMeOne<QrDetailLocalEntity>(),
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                )
            insertWithSyncState(accountId = accountId, qr = localQr, isDirty = true)

            transaction.upsert(accountId = accountId, qrList = listOf(remoteQr), cursor = 5L)

            database.findQrList() shouldBe listOf(localQr)
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.QR) shouldBe 5L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(3_000),
            "같음" to Instant.fromEpochMilliseconds(2_000),
            "이른" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-84a29c64-490b-482a-acab-32fae5dbba26 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localQr = fixtureMonkey.localQr(isDeleted = false).copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
                val remoteQr =
                    localQr.copy(
                        detail = fixtureMonkey.giveMeOne<QrDetailLocalEntity>(),
                        updatedAt = remoteUpdatedAt,
                    )
                insertWithSyncState(accountId = accountId, qr = localQr, isDirty = true)

                transaction.upsert(accountId = accountId, qrList = listOf(remoteQr), cursor = 5L)

                isPending(accountId = accountId, qrId = localQr.id) shouldBe true
            }
        }

        test("TC-81241ed9-5c4f-4176-a243-e818fb676fd3 내려받기 저장이 실패하면 QR과 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remoteQr = fixtureMonkey.localQr(isDeleted = false)
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws QrSyncTestException()
            val failingTransaction = AccountQrSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<QrSyncTestException> {
                failingTransaction.upsert(accountId = accountId, qrList = listOf(remoteQr), cursor = 5L)
            }

            database.findQrList().shouldBeEmpty()
            database.findAccountQrList().shouldBeEmpty()
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.QR) shouldBe 0L
        }

        test("TC-c334819a-dc0e-4f1e-8d02-4a932cb77b25 기기에 없던 QR은 계정과 연결되어 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = fixtureMonkey.localQr(isDeleted = false)

            transaction.upsert(accountId = accountId, qrList = listOf(remote), cursor = 5L)

            database.findQrList() shouldBe listOf(remote)
            database.findAccountQrList() shouldBe listOf(AccountQrLocalEntity(accountId = accountId, qrId = remote.id, isDirty = false))
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.QR) shouldBe 5L
        }

        test("QR 커서는 다른 종류의 커서와 따로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            transaction.upsert(accountId = accountId, qrList = listOf(fixtureMonkey.localQr(isDeleted = false)), cursor = 5L)

            SyncKindLocalEntity.entries
                .filter { kind -> kind != SyncKindLocalEntity.QR }
                .forEach { kind -> syncCursorDataSource.read(accountId = accountId, kind = kind) shouldBe 0L }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
