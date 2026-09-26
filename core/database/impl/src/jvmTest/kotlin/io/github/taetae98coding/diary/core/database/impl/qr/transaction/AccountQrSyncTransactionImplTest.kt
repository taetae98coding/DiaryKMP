package io.github.taetae98coding.diary.core.database.impl.qr.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
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
        ): Boolean = syncDataSource.findPending(accountId = accountId).any { qr -> qr.id == qrId }

        test("TC-DATA-SYNC-DOMAIN-087 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 QR은 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val guestEntity = fixtureMonkey.localQr(isDeleted = false)
            val accountEntity = fixtureMonkey.localQr(isDeleted = false)
            insertWithSyncState(accountId = Uuid.NIL, qr = guestEntity, isDirty = true)
            insertWithSyncState(accountId = accountId, qr = accountEntity, isDirty = true)

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(accountEntity)
        }

        test("업로드 대기가 아닌 QR은 업로드 대상에 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            insertWithSyncState(accountId = accountId, qr = fixtureMonkey.localQr(isDeleted = false), isDirty = false)

            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-QR-ADD-DATA-003 추가한 QR은 계정의 업로드 대기로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val qr = fixtureMonkey.localQr(isDeleted = false)

            AccountQrTransactionImpl(database = database).upsert(accountId = accountId, qrList = listOf(qr))

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(qr)
        }

        listOf(
            true to "삭제",
            false to "실행 취소",
        ).forEach { (isDeleted, label) ->
            test("TC-QR-HOME-DATA-004 ${label}한 QR은 계정의 업로드 대기로 기록된다") {
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

                syncDataSource.findPending(accountId = accountId) shouldBe listOf(qr.copy(isDeleted = isDeleted, updatedAt = updatedAt))
            }
        }

        test("TC-DATA-SYNC-DOMAIN-026 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val qr = fixtureMonkey.localQr(isDeleted = false)
            insertWithSyncState(accountId = accountId, qr = qr, isDirty = true)

            transaction.clearPending(accountId = accountId, qrList = listOf(qr))

            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DOMAIN-027 업로드 중 수정 시각이 바뀐 QR은 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushedQr = fixtureMonkey.localQr(isDeleted = false).copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changedQr = pushedQr.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId = accountId, qr = changedQr, isDirty = true)

            transaction.clearPending(accountId = accountId, qrList = listOf(pushedQr))

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(changedQr)
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-022 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localQr = fixtureMonkey.localQr(isDeleted = false).copy(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remoteQr =
                    localQr.copy(
                        detail = fixtureMonkey.giveMeOne<QrDetailLocalEntity>(),
                        isDeleted = !localQr.isDeleted,
                        updatedAt = remoteUpdatedAt,
                    )
                insertWithSyncState(accountId = accountId, qr = localQr, isDirty = false)

                transaction.save(accountId = accountId, qrList = listOf(remoteQr), cursor = 5L)

                database.findQrList() shouldBe listOf(remoteQr)
            }
        }

        test("TC-DATA-SYNC-DATA-023 기기의 수정 시각이 더 늦으면 기기 내용을 유지하고 커서는 전진한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val localQr = fixtureMonkey.localQr(isDeleted = false).copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remoteQr =
                localQr.copy(
                    detail = fixtureMonkey.giveMeOne<QrDetailLocalEntity>(),
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                )
            insertWithSyncState(accountId = accountId, qr = localQr, isDirty = true)

            transaction.save(accountId = accountId, qrList = listOf(remoteQr), cursor = 5L)

            database.findQrList() shouldBe listOf(localQr)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.QR) shouldBe 5L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(3_000),
            "같음" to Instant.fromEpochMilliseconds(2_000),
            "이른" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-024 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localQr = fixtureMonkey.localQr(isDeleted = false).copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
                val remoteQr =
                    localQr.copy(
                        detail = fixtureMonkey.giveMeOne<QrDetailLocalEntity>(),
                        updatedAt = remoteUpdatedAt,
                    )
                insertWithSyncState(accountId = accountId, qr = localQr, isDirty = true)

                transaction.save(accountId = accountId, qrList = listOf(remoteQr), cursor = 5L)

                isPending(accountId = accountId, qrId = localQr.id) shouldBe true
            }
        }

        test("TC-DATA-SYNC-DATA-026 내려받기 저장이 실패하면 QR과 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remoteQr = fixtureMonkey.localQr(isDeleted = false)
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws QrSyncTestException()
            val failingTransaction = AccountQrSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<QrSyncTestException> {
                failingTransaction.save(accountId = accountId, qrList = listOf(remoteQr), cursor = 5L)
            }

            database.findQrList().shouldBeEmpty()
            database.findAccountQrList().shouldBeEmpty()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.QR) shouldBe 0L
        }

        test("TC-DATA-SYNC-DATA-025 기기에 없던 QR은 계정과 연결되어 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = fixtureMonkey.localQr(isDeleted = false)

            transaction.save(accountId = accountId, qrList = listOf(remote), cursor = 5L)

            database.findQrList() shouldBe listOf(remote)
            database.findAccountQrList() shouldBe listOf(AccountQrLocalEntity(accountId = accountId, qrId = remote.id, isDirty = false))
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.QR) shouldBe 5L
        }

        test("QR 커서는 다른 종류의 커서와 따로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            transaction.save(accountId = accountId, qrList = listOf(fixtureMonkey.localQr(isDeleted = false)), cursor = 5L)

            SyncKind.entries
                .filter { kind -> kind != SyncKind.QR }
                .forEach { kind -> syncCursorDataSource.find(accountId = accountId, kind = kind) shouldBe 0L }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
