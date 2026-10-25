package io.github.taetae98coding.diary.core.database.impl.memoplace.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
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
            syncDataSource.readPendingList(accountId = accountId).any { pending ->
                pending.memoId == memoPlace.memoId && pending.placeId == memoPlace.placeId
            }

        test("TC-c12bf850-fe4e-4750-94ff-ceb9f75066b4 현재 계정의 업로드 대기 연결만 조회한다") {
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
                .readPendingList(accountId = accountId)
                .shouldContainExactlyInAnyOrder(firstPending, secondPending)
        }

        test("TC-36179c3a-4170-42bc-beda-73aa6e1b75a0 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 메모와 장소의 연결은 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val guestEntity = memoPlace()
            val accountEntity = memoPlace()
            insertWithSyncState(accountId = Uuid.NIL, memoPlace = guestEntity, isDirty = true)
            insertWithSyncState(accountId = accountId, memoPlace = accountEntity, isDirty = true)

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(accountEntity)
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memoPlace = memoPlace()
            insertWithSyncState(accountId, memoPlace, isDirty = true)

            transaction.clearPending(accountId = accountId, memoPlaceList = listOf(memoPlace))

            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-882adb8e-cd96-4862-a529-585bb17af19a 업로드 중 수정 시각이 바뀐 연결은 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushed = memoPlace(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changed = pushed.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, changed, isDirty = true)

            transaction.clearPending(accountId = accountId, memoPlaceList = listOf(pushed))

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(changed)
        }

        test("TC-85437a34-b1a2-4fa0-ae7a-ec766c5b8952 내려받기 저장이 끝나면 서버 변경 순번이 커서로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            transaction.upsert(accountId = accountId, memoPlaceList = listOf(memoPlace()), cursor = 3L)
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.MEMO_PLACE) shouldBe 3L

            transaction.upsert(accountId = accountId, memoPlaceList = listOf(memoPlace()), cursor = 11L)
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.MEMO_PLACE) shouldBe 11L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-7fe36220-e35f-4c2b-9172-094e9bf95870 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val local = memoPlace(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId, local, isDirty = false)

                transaction.upsert(accountId = accountId, memoPlaceList = listOf(remote), cursor = 5L)

                findMemoPlace(memoId = local.memoId) shouldBe listOf(remote)
            }
        }

        test("TC-2f3591f7-e9cb-4759-a9aa-01fbf0331179 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = memoPlace(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.upsert(accountId = accountId, memoPlaceList = listOf(remote), cursor = 5L)

            findMemoPlace(memoId = local.memoId) shouldBe listOf(local)
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.MEMO_PLACE) shouldBe 5L
        }

        test("TC-84a29c64-490b-482a-acab-32fae5dbba26 내려받기는 업로드 대기 여부를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = memoPlace(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remote = local.copy(updatedAt = Instant.fromEpochMilliseconds(3_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.upsert(accountId = accountId, memoPlaceList = listOf(remote), cursor = 5L)

            isPending(accountId = accountId, memoPlace = local) shouldBe true
        }

        test("TC-c334819a-dc0e-4f1e-8d02-4a932cb77b25 기기에 없던 연결은 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = memoPlace()

            transaction.upsert(accountId = accountId, memoPlaceList = listOf(remote), cursor = 5L)

            findMemoPlace(memoId = remote.memoId) shouldBe listOf(remote)
            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-28a55eb8-f222-4f68-a83b-854bcf648ca2 서버 수정 시각이 기기보다 늦은 연결은 응답대로 저장되고 내려받기 위치가 갱신된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = memoPlace(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, local, isDirty = false)

            transaction.upsert(accountId = accountId, memoPlaceList = listOf(remote), cursor = 8L)

            findMemoPlace(memoId = local.memoId) shouldBe listOf(remote)
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.MEMO_PLACE) shouldBe 8L
        }

        test("TC-81241ed9-5c4f-4176-a243-e818fb676fd3 내려받기 저장이 실패하면 연결과 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = memoPlace()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws MemoPlaceSyncTestException()
            val failingTransaction = AccountMemoPlaceSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<MemoPlaceSyncTestException> {
                failingTransaction.upsert(accountId = accountId, memoPlaceList = listOf(remote), cursor = 5L)
            }

            findMemoPlace(memoId = remote.memoId).shouldBeEmpty()
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.MEMO_PLACE) shouldBe 0L
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

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
