package io.github.taetae98coding.diary.core.database.impl.memo.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.memo.datasource.AccountMemoSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memo.entity.AccountMemoLocalEntity
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
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class MemoSyncTestException : RuntimeException()

class AccountMemoSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountMemoSyncTransactionImpl
        lateinit var syncDataSource: AccountMemoSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl
        lateinit var accountTransaction: AccountMemoTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountMemoSyncTransactionImpl(database = database)
            syncDataSource = AccountMemoSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
            accountTransaction = AccountMemoTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            memo: MemoLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.memoDao().upsert(listOf(memo))
                database.accountMemoDao().upsert(
                    listOf(
                        AccountMemoLocalEntity(
                            accountId = accountId,
                            memoId = memo.id,
                            isDirty = isDirty,
                        ),
                    ),
                )
            }
        }

        suspend fun findMemo(
            accountId: Uuid,
            memoId: Uuid,
        ): MemoLocalEntity? = database.accountMemoDao().find(accountId = accountId, memoId = memoId).first()

        suspend fun isPending(
            accountId: Uuid,
            memoId: Uuid,
        ): Boolean = syncDataSource.findPending(accountId = accountId).any { memo -> memo.id == memoId }

        test("TC-DATA-SYNC-DOMAIN-009 현재 계정의 업로드 대기 메모를 조회하고 동기화 완료 메모는 제외한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstPendingMemo = memo()
            val secondPendingMemo = memo()
            val syncedMemo = memo()
            val otherAccountMemo = memo()
            insertWithSyncState(accountId, firstPendingMemo, isDirty = true)
            insertWithSyncState(accountId, secondPendingMemo, isDirty = true)
            insertWithSyncState(accountId, syncedMemo, isDirty = false)
            insertWithSyncState(otherAccountId, otherAccountMemo, isDirty = true)

            syncDataSource
                .findPending(accountId = accountId)
                .shouldContainExactlyInAnyOrder(firstPendingMemo, secondPendingMemo)
        }

        test("TC-DATA-SYNC-DOMAIN-087 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 메모는 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val guestMemo = memo()
            val accountMemo = memo()
            insertWithSyncState(Uuid.NIL, guestMemo, isDirty = true)
            insertWithSyncState(accountId, accountMemo, isDirty = true)

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(accountMemo)
        }

        test("TC-DATA-SYNC-DOMAIN-026 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertWithSyncState(accountId, memo, isDirty = true)

            transaction.clearPending(accountId = accountId, memoList = listOf(memo))

            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DOMAIN-027 업로드 중 수정 시각이 바뀐 메모는 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushedMemo = memo(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changedMemo = pushedMemo.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, changedMemo, isDirty = true)

            transaction.clearPending(accountId = accountId, memoList = listOf(pushedMemo))

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(changedMemo)
        }

        test("TC-DATA-SYNC-DOMAIN-030 메모가 업로드 대기가 되어도 내려받기 위치는 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val cursor = 7L
            transaction.save(accountId = accountId, memoList = listOf(memo), cursor = cursor)

            accountTransaction.updateDetail(
                accountId = accountId,
                memoId = memo.id,
                detail = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>(),
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            )

            isPending(accountId = accountId, memoId = memo.id) shouldBe true
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO) shouldBe cursor
        }

        test("TC-DATA-SYNC-DATA-016 내려받기 저장이 끝나면 서버 변경 순번이 커서로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstCursor = 3L
            val secondCursor = 11L

            transaction.save(accountId = accountId, memoList = listOf(memo()), cursor = firstCursor)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO) shouldBe firstCursor

            transaction.save(accountId = accountId, memoList = listOf(memo()), cursor = secondCursor)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO) shouldBe secondCursor
        }

        test("TC-DATA-SYNC-DATA-017 기록된 순번이 없으면 기본 커서를 사용한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            transaction.save(accountId = otherAccountId, memoList = listOf(memo()), cursor = 9L)

            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO) shouldBe 0L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-022 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localMemo = memo(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remoteMemo =
                    localMemo.copy(
                        detail = fixtureMonkey.giveMeOne(),
                        isFinished = !localMemo.isFinished,
                        isDeleted = !localMemo.isDeleted,
                        updatedAt = remoteUpdatedAt,
                    )
                insertWithSyncState(accountId, localMemo, isDirty = false)

                transaction.save(accountId = accountId, memoList = listOf(remoteMemo), cursor = 5L)

                findMemo(accountId = accountId, memoId = localMemo.id) shouldBe remoteMemo
            }
        }

        test("TC-DATA-SYNC-DATA-023 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val localMemo = memo(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remoteMemo =
                localMemo.copy(
                    detail = fixtureMonkey.giveMeOne(),
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                )
            insertWithSyncState(accountId, localMemo, isDirty = true)
            val cursor = 5L

            transaction.save(accountId = accountId, memoList = listOf(remoteMemo), cursor = cursor)

            findMemo(accountId = accountId, memoId = localMemo.id) shouldBe localMemo
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO) shouldBe cursor
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(3_000),
            "같음" to Instant.fromEpochMilliseconds(2_000),
            "이른" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-024 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localMemo = memo(updatedAt = Instant.fromEpochMilliseconds(2_000))
                val remoteMemo = localMemo.copy(detail = fixtureMonkey.giveMeOne(), updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId, localMemo, isDirty = true)

                transaction.save(accountId = accountId, memoList = listOf(remoteMemo), cursor = 5L)

                isPending(accountId = accountId, memoId = localMemo.id) shouldBe true
            }
        }

        test("TC-DATA-SYNC-DATA-025 기기에 없던 메모는 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remoteMemo = memo()

            transaction.save(accountId = accountId, memoList = listOf(remoteMemo), cursor = 5L)

            findMemo(accountId = accountId, memoId = remoteMemo.id) shouldBe remoteMemo
            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DATA-026 내려받기 저장이 실패하면 메모와 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remoteMemo = memo()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws MemoSyncTestException()
            val failingTransaction = AccountMemoSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<MemoSyncTestException> {
                failingTransaction.save(accountId = accountId, memoList = listOf(remoteMemo), cursor = 5L)
            }

            findMemo(accountId = accountId, memoId = remoteMemo.id).shouldBeNull()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO) shouldBe 0L
        }

        test("TC-DATA-SYNC-DATA-027 업로드한 메모가 같은 내용으로 다시 내려와도 기기 내용은 그대로다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertWithSyncState(accountId, memo, isDirty = false)

            transaction.save(accountId = accountId, memoList = listOf(memo), cursor = 5L)

            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo
        }

        listOf(
            "대표 태그가 없으면" to null,
            "대표 태그가 있으면" to fixtureMonkey.giveMeOne<Uuid>(),
        ).forEach { (label, remotePrimaryTagId) ->
            test("TC-MEMO-PRIMARY-TAG-DATA-004 내려받은 메모의 $label 그 대표 태그가 기기에 반영된다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localMemo =
                    memo(updatedAt = Instant.fromEpochMilliseconds(1_000))
                        .copy(primaryTagId = fixtureMonkey.giveMeOne<Uuid>())
                val remoteMemo =
                    localMemo.copy(
                        primaryTagId = remotePrimaryTagId,
                        updatedAt = Instant.fromEpochMilliseconds(2_000),
                    )
                insertWithSyncState(accountId, localMemo, isDirty = false)

                transaction.save(accountId = accountId, memoList = listOf(remoteMemo), cursor = 5L)

                findMemo(accountId = accountId, memoId = localMemo.id)?.primaryTagId shouldBe remotePrimaryTagId
            }
        }

        test("내려받기 저장은 이미 있는 연결의 대기 여부를 덮어쓰지 않고 커서만 갱신한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pendingMemo = memo(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val syncedMemo = memo(updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, pendingMemo, isDirty = true)
            insertWithSyncState(accountId, syncedMemo, isDirty = false)

            transaction.save(
                accountId = accountId,
                memoList = listOf(pendingMemo, syncedMemo),
                cursor = 5L,
            )

            isPending(accountId = accountId, memoId = pendingMemo.id) shouldBe true
            isPending(accountId = accountId, memoId = syncedMemo.id) shouldBe false
        }

        test("대기 해제는 요청한 계정의 연결만 바꾼다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertWithSyncState(accountId, memo, isDirty = true)
            insertWithSyncState(otherAccountId, memo, isDirty = true)

            transaction.clearPending(accountId = accountId, memoList = listOf(memo))

            isPending(accountId = accountId, memoId = memo.id) shouldBe false
            isPending(accountId = otherAccountId, memoId = memo.id) shouldBe true
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(updatedAt: Instant = instant()): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::updatedAt, updatedAt)
                .setExp(MemoLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
