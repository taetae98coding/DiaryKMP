package io.github.taetae98coding.diary.core.database.impl.memoweb.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
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
            syncDataSource.readPendingList(accountId = accountId).any { pending ->
                pending.memoId == memoWeb.memoId && pending.webId == memoWeb.webId
            }

        test("TC-16582126-e239-4cef-91b8-c8ac38486eda 현재 계정의 업로드 대기 연결만 조회한다") {
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
                .readPendingList(accountId = accountId)
                .shouldContainExactlyInAnyOrder(firstPending, secondPending)
        }

        test("TC-36179c3a-4170-42bc-beda-73aa6e1b75a0 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 메모와 웹 항목의 연결은 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val guestEntity = memoWeb()
            val accountEntity = memoWeb()
            insertWithSyncState(accountId = Uuid.NIL, memoWeb = guestEntity, isDirty = true)
            insertWithSyncState(accountId = accountId, memoWeb = accountEntity, isDirty = true)

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(accountEntity)
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memoWeb = memoWeb()
            insertWithSyncState(accountId, memoWeb, isDirty = true)

            transaction.clearPending(accountId = accountId, memoWebList = listOf(memoWeb))

            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-882adb8e-cd96-4862-a529-585bb17af19a 업로드 중 수정 시각이 바뀐 연결은 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushed = memoWeb(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changed = pushed.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, changed, isDirty = true)

            transaction.clearPending(accountId = accountId, memoWebList = listOf(pushed))

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(changed)
        }

        test("TC-85437a34-b1a2-4fa0-ae7a-ec766c5b8952 내려받기 저장이 끝나면 서버 변경 순번이 커서로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            transaction.upsert(accountId = accountId, memoWebList = listOf(memoWeb()), cursor = 3L)
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.MEMO_WEB) shouldBe 3L

            transaction.upsert(accountId = accountId, memoWebList = listOf(memoWeb()), cursor = 11L)
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.MEMO_WEB) shouldBe 11L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-7fe36220-e35f-4c2b-9172-094e9bf95870 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val local = memoWeb(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId, local, isDirty = false)

                transaction.upsert(accountId = accountId, memoWebList = listOf(remote), cursor = 5L)

                findMemoWeb(memoId = local.memoId) shouldBe listOf(remote)
            }
        }

        test("TC-2f3591f7-e9cb-4759-a9aa-01fbf0331179 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = memoWeb(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.upsert(accountId = accountId, memoWebList = listOf(remote), cursor = 5L)

            findMemoWeb(memoId = local.memoId) shouldBe listOf(local)
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.MEMO_WEB) shouldBe 5L
        }

        test("TC-84a29c64-490b-482a-acab-32fae5dbba26 내려받기는 업로드 대기 여부를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = memoWeb(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remote = local.copy(updatedAt = Instant.fromEpochMilliseconds(3_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.upsert(accountId = accountId, memoWebList = listOf(remote), cursor = 5L)

            isPending(accountId = accountId, memoWeb = local) shouldBe true
        }

        test("TC-c334819a-dc0e-4f1e-8d02-4a932cb77b25 기기에 없던 연결은 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = memoWeb()

            transaction.upsert(accountId = accountId, memoWebList = listOf(remote), cursor = 5L)

            findMemoWeb(memoId = remote.memoId) shouldBe listOf(remote)
            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-eaf7d13b-f467-4046-8e78-fdf35129ad57 서버 수정 시각이 기기보다 늦은 연결은 응답대로 저장되고 내려받기 위치가 갱신된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = memoWeb(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, local, isDirty = false)

            transaction.upsert(accountId = accountId, memoWebList = listOf(remote), cursor = 8L)

            findMemoWeb(memoId = local.memoId) shouldBe listOf(remote)
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.MEMO_WEB) shouldBe 8L
        }

        test("TC-81241ed9-5c4f-4176-a243-e818fb676fd3 내려받기 저장이 실패하면 연결과 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = memoWeb()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws MemoWebSyncTestException()
            val failingTransaction = AccountMemoWebSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<MemoWebSyncTestException> {
                failingTransaction.upsert(accountId = accountId, memoWebList = listOf(remote), cursor = 5L)
            }

            findMemoWeb(memoId = remote.memoId).shouldBeEmpty()
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.MEMO_WEB) shouldBe 0L
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

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
