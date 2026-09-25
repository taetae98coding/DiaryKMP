package io.github.taetae98coding.diary.core.database.impl.memoweb.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memoweb.datasource.AccountMemoWebSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memoweb.entity.AccountMemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class MemoWebSyncTestException : RuntimeException()

class AccountMemoWebSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountMemoWebSyncTransactionImpl
        lateinit var syncDataSource: AccountMemoWebSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountMemoWebSyncTransactionImpl(database = database)
            syncDataSource = AccountMemoWebSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            memoWeb: MemoWebLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.memoWebDao().upsert(listOf(memoWeb))
                database.accountMemoWebDao().upsert(
                    listOf(
                        AccountMemoWebLocalEntity(
                            accountId = accountId,
                            memoId = memoWeb.memoId,
                            webId = memoWeb.webId,
                            isDirty = isDirty,
                        ),
                    ),
                )
            }
        }

        suspend fun findMemoWeb(memoId: Uuid): List<MemoWebLocalEntity> = database.memoWebDao().findByMemoIdList(listOf(memoId))

        suspend fun isPending(
            accountId: Uuid,
            memoWeb: MemoWebLocalEntity,
        ): Boolean =
            syncDataSource.findPending(accountId = accountId).any { pending ->
                pending.memoId == memoWeb.memoId && pending.webId == memoWeb.webId
            }

        test("TC-MEMO-WEB-DATA-005 현재 계정의 업로드 대기 연결만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstPending = memoWeb()
            val secondPending = memoWeb()
            val synced = memoWeb()
            val otherAccountPending = memoWeb()
            insertWithSyncState(accountId, firstPending, isDirty = true)
            insertWithSyncState(accountId, secondPending, isDirty = true)
            insertWithSyncState(accountId, synced, isDirty = false)
            insertWithSyncState(otherAccountId, otherAccountPending, isDirty = true)

            syncDataSource
                .findPending(accountId = accountId)
                .shouldContainExactlyInAnyOrder(firstPending, secondPending)
        }

        test("TC-DATA-SYNC-DOMAIN-026 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memoWeb = memoWeb()
            insertWithSyncState(accountId, memoWeb, isDirty = true)

            transaction.clearPending(accountId = accountId, memoWebList = listOf(memoWeb))

            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DOMAIN-027 업로드 중 수정 시각이 바뀐 연결은 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushed = memoWeb(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changed = pushed.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, changed, isDirty = true)

            transaction.clearPending(accountId = accountId, memoWebList = listOf(pushed))

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(changed)
        }

        test("TC-DATA-SYNC-DATA-016 내려받기 저장이 끝나면 서버 변경 순번이 커서로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            transaction.save(accountId = accountId, memoWebList = listOf(memoWeb()), cursor = 3L)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_WEB) shouldBe 3L

            transaction.save(accountId = accountId, memoWebList = listOf(memoWeb()), cursor = 11L)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_WEB) shouldBe 11L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-022 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val local = memoWeb(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId, local, isDirty = false)

                transaction.save(accountId = accountId, memoWebList = listOf(remote), cursor = 5L)

                findMemoWeb(memoId = local.memoId) shouldBe listOf(remote)
            }
        }

        test("TC-DATA-SYNC-DATA-023 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = memoWeb(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.save(accountId = accountId, memoWebList = listOf(remote), cursor = 5L)

            findMemoWeb(memoId = local.memoId) shouldBe listOf(local)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_WEB) shouldBe 5L
        }

        test("TC-DATA-SYNC-DATA-024 내려받기는 업로드 대기 여부를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = memoWeb(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remote = local.copy(updatedAt = Instant.fromEpochMilliseconds(3_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.save(accountId = accountId, memoWebList = listOf(remote), cursor = 5L)

            isPending(accountId = accountId, memoWeb = local) shouldBe true
        }

        test("TC-DATA-SYNC-DATA-025 기기에 없던 연결은 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = memoWeb()

            transaction.save(accountId = accountId, memoWebList = listOf(remote), cursor = 5L)

            findMemoWeb(memoId = remote.memoId) shouldBe listOf(remote)
            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-MEMO-WEB-DATA-008 서버 수정 시각이 기기보다 늦은 연결은 응답대로 저장되고 내려받기 위치가 갱신된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = memoWeb(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, local, isDirty = false)

            transaction.save(accountId = accountId, memoWebList = listOf(remote), cursor = 8L)

            findMemoWeb(memoId = local.memoId) shouldBe listOf(remote)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_WEB) shouldBe 8L
        }

        test("TC-DATA-SYNC-DATA-026 내려받기 저장이 실패하면 연결과 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = memoWeb()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws MemoWebSyncTestException()
            val failingTransaction = AccountMemoWebSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<MemoWebSyncTestException> {
                failingTransaction.save(accountId = accountId, memoWebList = listOf(remote), cursor = 5L)
            }

            findMemoWeb(memoId = remote.memoId).shouldBeEmpty()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_WEB) shouldBe 0L
        }

        test("대기 해제는 요청한 계정의 연결만 바꾼다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memoWeb = memoWeb()
            insertWithSyncState(accountId, memoWeb, isDirty = true)
            insertWithSyncState(otherAccountId, memoWeb, isDirty = true)

            transaction.clearPending(accountId = accountId, memoWebList = listOf(memoWeb))

            isPending(accountId = accountId, memoWeb = memoWeb) shouldBe false
            isPending(accountId = otherAccountId, memoWeb = memoWeb) shouldBe true
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memoWeb(updatedAt: Instant = instant()): MemoWebLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoWebLocalEntity>()
                .setExp(MemoWebLocalEntity::updatedAt, updatedAt)
                .setExp(MemoWebLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
