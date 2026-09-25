package io.github.taetae98coding.diary.core.database.impl.memotag.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memotag.datasource.AccountMemoTagSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memotag.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class MemoTagSyncTestException : RuntimeException()

class AccountMemoTagSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountMemoTagSyncTransactionImpl
        lateinit var syncDataSource: AccountMemoTagSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountMemoTagSyncTransactionImpl(database = database)
            syncDataSource = AccountMemoTagSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            memoTag: MemoTagLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.memoTagDao().upsert(memoTag)
                database.accountMemoTagSyncDao().upsert(
                    AccountMemoTagLocalEntity(
                        accountId = accountId,
                        memoId = memoTag.memoId,
                        tagId = memoTag.tagId,
                        isDirty = isDirty,
                    ),
                )
            }
        }

        suspend fun findMemoTag(
            memoId: Uuid,
            tagId: Uuid,
        ): MemoTagLocalEntity? =
            database
                .memoTagDao()
                .findByMemoIdList(listOf(memoId))
                .firstOrNull { memoTag -> memoTag.tagId == tagId }

        suspend fun isPending(
            memoTag: MemoTagLocalEntity,
            accountId: Uuid,
        ): Boolean =
            syncDataSource
                .findPending(accountId = accountId)
                .any { pending -> pending.memoId == memoTag.memoId && pending.tagId == memoTag.tagId }

        test("TC-MEMO-TAG-DATA-006 해제된 연결도 업로드 대상에서 빠지지 않고 현재 계정의 업로드 대기 관계만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstPending = memoTag()
            val secondPending = memoTag(isDeleted = true)
            val synced = memoTag()
            val otherAccountPending = memoTag()
            insertWithSyncState(accountId, firstPending, isDirty = true)
            insertWithSyncState(accountId, secondPending, isDirty = true)
            insertWithSyncState(accountId, synced, isDirty = false)
            insertWithSyncState(otherAccountId, otherAccountPending, isDirty = true)

            syncDataSource
                .findPending(accountId = accountId)
                .shouldContainExactlyInAnyOrder(firstPending, secondPending)
        }

        test("TC-DATA-SYNC-DOMAIN-087 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 메모와 태그의 연결은 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val guestEntity = memoTag()
            val accountEntity = memoTag()
            insertWithSyncState(accountId = Uuid.NIL, memoTag = guestEntity, isDirty = true)
            insertWithSyncState(accountId = accountId, memoTag = accountEntity, isDirty = true)

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(accountEntity)
        }

        test("TC-DATA-SYNC-DOMAIN-026 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memoTag = memoTag()
            insertWithSyncState(accountId, memoTag, isDirty = true)

            transaction.clearPending(accountId = accountId, memoTagList = listOf(memoTag))

            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DOMAIN-027 업로드 중 수정 시각이 바뀐 관계는 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushed = memoTag(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changed = pushed.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, changed, isDirty = true)

            transaction.clearPending(accountId = accountId, memoTagList = listOf(pushed))

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(changed)
        }

        test("대기 해제는 요청한 계정의 연결만 바꾼다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memoTag = memoTag()
            insertWithSyncState(accountId, memoTag, isDirty = true)
            insertWithSyncState(otherAccountId, memoTag, isDirty = true)

            transaction.clearPending(accountId = accountId, memoTagList = listOf(memoTag))

            isPending(memoTag, accountId) shouldBe false
            isPending(memoTag, otherAccountId) shouldBe true
        }

        test("TC-DATA-SYNC-DATA-016 TC-DATA-SYNC-DATA-017 기록된 순번이 없으면 기본 커서를 사용하고 저장이 끝나면 커서가 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_TAG) shouldBe 0L

            transaction.save(accountId = otherAccountId, memoTagList = listOf(memoTag()), cursor = 9L)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_TAG) shouldBe 0L

            transaction.save(accountId = accountId, memoTagList = listOf(memoTag()), cursor = 3L)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_TAG) shouldBe 3L

            transaction.save(accountId = accountId, memoTagList = listOf(memoTag()), cursor = 11L)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_TAG) shouldBe 11L
        }

        test("TC-DATA-SYNC-DATA-028 메모 커서와 태그 커서는 관계 커서와 따로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            transaction.save(accountId = accountId, memoTagList = listOf(memoTag()), cursor = 7L)

            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_TAG) shouldBe 7L
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO) shouldBe 0L
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG) shouldBe 0L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-022 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val local = memoTag(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId, local, isDirty = false)

                transaction.save(accountId = accountId, memoTagList = listOf(remote), cursor = 5L)

                findMemoTag(memoId = local.memoId, tagId = local.tagId) shouldBe remote
            }
        }

        test("TC-DATA-SYNC-DATA-023 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = memoTag(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.save(accountId = accountId, memoTagList = listOf(remote), cursor = 5L)

            findMemoTag(memoId = local.memoId, tagId = local.tagId) shouldBe local
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_TAG) shouldBe 5L
        }

        test("TC-DATA-SYNC-DATA-025 기기에 없던 관계는 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = memoTag()

            transaction.save(accountId = accountId, memoTagList = listOf(remote), cursor = 5L)

            findMemoTag(memoId = remote.memoId, tagId = remote.tagId) shouldBe remote
            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-MEMO-TAG-DATA-007 서버 수정 시각이 기기보다 늦은 연결은 응답대로 저장되고 내려받기 위치가 갱신된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = memoTag(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, local, isDirty = false)

            transaction.save(accountId = accountId, memoTagList = listOf(remote), cursor = 8L)

            findMemoTag(memoId = local.memoId, tagId = local.tagId) shouldBe remote
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_TAG) shouldBe 8L
        }

        test("TC-DATA-SYNC-DATA-024 내려받기 저장은 이미 있는 연결의 대기 여부를 덮어쓰지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pending = memoTag(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val synced = memoTag(updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, pending, isDirty = true)
            insertWithSyncState(accountId, synced, isDirty = false)

            transaction.save(
                accountId = accountId,
                memoTagList = listOf(pending, synced),
                cursor = 5L,
            )

            isPending(pending, accountId) shouldBe true
            isPending(synced, accountId) shouldBe false
        }

        test("같은 메모의 다른 태그 관계는 서로 독립적으로 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val local =
                memoTag(memoId = memoId, updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remoteOfLocal = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))
            val remoteOfNew = memoTag(memoId = memoId, updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.save(
                accountId = accountId,
                memoTagList = listOf(remoteOfLocal, remoteOfNew),
                cursor = 5L,
            )

            findMemoTag(memoId = memoId, tagId = local.tagId) shouldBe local
            findMemoTag(memoId = memoId, tagId = remoteOfNew.tagId) shouldBe remoteOfNew
        }

        test("TC-DATA-SYNC-DATA-026 내려받기 저장이 실패하면 관계와 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = memoTag()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws MemoTagSyncTestException()
            val failingTransaction = AccountMemoTagSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<MemoTagSyncTestException> {
                failingTransaction.save(accountId = accountId, memoTagList = listOf(remote), cursor = 5L)
            }

            findMemoTag(memoId = remote.memoId, tagId = remote.tagId).shouldBeNull()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_TAG) shouldBe 0L
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memoTag(
            memoId: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            tagId: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            isDeleted: Boolean = false,
            updatedAt: Instant = instant(),
        ): MemoTagLocalEntity =
            MemoTagLocalEntity(
                memoId = memoId,
                tagId = tagId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
                createdAt = instant(),
            )

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
