package io.github.taetae98coding.diary.core.database.impl.memoplace.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memoplace.datasource.AccountMemoPlaceSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memoplace.entity.AccountMemoPlaceLocalEntity
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

private class MemoPlaceSyncTestException : RuntimeException()

class AccountMemoPlaceSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountMemoPlaceSyncTransactionImpl
        lateinit var syncDataSource: AccountMemoPlaceSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountMemoPlaceSyncTransactionImpl(database = database)
            syncDataSource = AccountMemoPlaceSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            memoPlace: MemoPlaceLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.memoPlaceDao().upsert(listOf(memoPlace))
                database.accountMemoPlaceDao().upsert(
                    listOf(
                        AccountMemoPlaceLocalEntity(
                            accountId = accountId,
                            memoId = memoPlace.memoId,
                            placeId = memoPlace.placeId,
                            isDirty = isDirty,
                        ),
                    ),
                )
            }
        }

        suspend fun findMemoPlace(memoId: Uuid): List<MemoPlaceLocalEntity> = database.memoPlaceDao().findByMemoIdList(listOf(memoId))

        suspend fun isPending(
            accountId: Uuid,
            memoPlace: MemoPlaceLocalEntity,
        ): Boolean =
            syncDataSource.findPending(accountId = accountId).any { pending ->
                pending.memoId == memoPlace.memoId && pending.placeId == memoPlace.placeId
            }

        test("TC-MEMO-PLACE-DATA-005 현재 계정의 업로드 대기 연결만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstPending = memoPlace()
            val secondPending = memoPlace()
            val synced = memoPlace()
            val otherAccountPending = memoPlace()
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
            val memoPlace = memoPlace()
            insertWithSyncState(accountId, memoPlace, isDirty = true)

            transaction.clearPending(accountId = accountId, memoPlaceList = listOf(memoPlace))

            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DOMAIN-027 업로드 중 수정 시각이 바뀐 연결은 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushed = memoPlace(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changed = pushed.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, changed, isDirty = true)

            transaction.clearPending(accountId = accountId, memoPlaceList = listOf(pushed))

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(changed)
        }

        test("TC-DATA-SYNC-DATA-016 내려받기 저장이 끝나면 서버 변경 순번이 커서로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            transaction.save(accountId = accountId, memoPlaceList = listOf(memoPlace()), cursor = 3L)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_PLACE) shouldBe 3L

            transaction.save(accountId = accountId, memoPlaceList = listOf(memoPlace()), cursor = 11L)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_PLACE) shouldBe 11L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-022 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val local = memoPlace(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId, local, isDirty = false)

                transaction.save(accountId = accountId, memoPlaceList = listOf(remote), cursor = 5L)

                findMemoPlace(memoId = local.memoId) shouldBe listOf(remote)
            }
        }

        test("TC-DATA-SYNC-DATA-023 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = memoPlace(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.save(accountId = accountId, memoPlaceList = listOf(remote), cursor = 5L)

            findMemoPlace(memoId = local.memoId) shouldBe listOf(local)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_PLACE) shouldBe 5L
        }

        test("TC-DATA-SYNC-DATA-024 내려받기는 업로드 대기 여부를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = memoPlace(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remote = local.copy(updatedAt = Instant.fromEpochMilliseconds(3_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.save(accountId = accountId, memoPlaceList = listOf(remote), cursor = 5L)

            isPending(accountId = accountId, memoPlace = local) shouldBe true
        }

        test("TC-MEMO-PLACE-DATA-008 기기에 없던 연결은 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = memoPlace()

            transaction.save(accountId = accountId, memoPlaceList = listOf(remote), cursor = 5L)

            findMemoPlace(memoId = remote.memoId) shouldBe listOf(remote)
            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DATA-026 내려받기 저장이 실패하면 연결과 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = memoPlace()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws MemoPlaceSyncTestException()
            val failingTransaction = AccountMemoPlaceSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<MemoPlaceSyncTestException> {
                failingTransaction.save(accountId = accountId, memoPlaceList = listOf(remote), cursor = 5L)
            }

            findMemoPlace(memoId = remote.memoId).shouldBeEmpty()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_PLACE) shouldBe 0L
        }

        test("대기 해제는 요청한 계정의 연결만 바꾼다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memoPlace = memoPlace()
            insertWithSyncState(accountId, memoPlace, isDirty = true)
            insertWithSyncState(otherAccountId, memoPlace, isDirty = true)

            transaction.clearPending(accountId = accountId, memoPlaceList = listOf(memoPlace))

            isPending(accountId = accountId, memoPlace = memoPlace) shouldBe false
            isPending(accountId = otherAccountId, memoPlace = memoPlace) shouldBe true
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memoPlace(updatedAt: Instant = instant()): MemoPlaceLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoPlaceLocalEntity>()
                .setExp(MemoPlaceLocalEntity::updatedAt, updatedAt)
                .setExp(MemoPlaceLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
