package io.github.taetae98coding.diary.core.database.impl.taglink.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.taglink.datasource.AccountTagLinkSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.taglink.entity.AccountTagLinkLocalEntity
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

private class TagLinkSyncTestException : RuntimeException()

class AccountTagLinkSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountTagLinkSyncTransactionImpl
        lateinit var syncDataSource: AccountTagLinkSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountTagLinkSyncTransactionImpl(database = database)
            syncDataSource = AccountTagLinkSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            tagLink: TagLinkLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.tagLinkDao().upsert(tagLink)
                database.accountTagLinkSyncDao().upsert(
                    AccountTagLinkLocalEntity(
                        accountId = accountId,
                        fromTagId = tagLink.fromTagId,
                        toTagId = tagLink.toTagId,
                        isDirty = isDirty,
                    ),
                )
            }
        }

        suspend fun findTagLink(
            fromTagId: Uuid,
            toTagId: Uuid,
        ): TagLinkLocalEntity? =
            database
                .tagLinkDao()
                .findByFromTagIdList(listOf(fromTagId))
                .firstOrNull { tagLink -> tagLink.toTagId == toTagId }

        suspend fun isPending(
            tagLink: TagLinkLocalEntity,
            accountId: Uuid,
        ): Boolean =
            syncDataSource
                .readPendingList(accountId = accountId)
                .any { pending -> pending.fromTagId == tagLink.fromTagId && pending.toTagId == tagLink.toTagId }

        test("TC-35a8b5fb-8bd9-4359-b6c2-bc8ab178409b TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 그 계정에서 연결하거나 해제한 연결은 그 계정의 업로드 대기로만 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val linkTransaction = AccountTagLinkTransactionImpl(database = database)
            val linked = tagLink()
            val released = tagLink()
            insertWithSyncState(accountId, released, isDirty = false)
            insertWithSyncState(otherAccountId, released, isDirty = false)

            linkTransaction.upsert(
                accountId = accountId,
                fromTagId = linked.fromTagId,
                toTagId = linked.toTagId,
                isDeleted = false,
                updatedAt = instant(),
            )
            linkTransaction.upsert(
                accountId = accountId,
                fromTagId = released.fromTagId,
                toTagId = released.toTagId,
                isDeleted = true,
                updatedAt = instant(),
            )

            syncDataSource
                .readPendingList(accountId = accountId)
                .map { pending -> pending.fromTagId to pending.toTagId }
                .shouldContainExactlyInAnyOrder(linked.fromTagId to linked.toTagId, released.fromTagId to released.toTagId)
            syncDataSource.readPendingList(accountId = otherAccountId).shouldBeEmpty()
        }

        test("해제된 연결도 포함해 현재 계정의 업로드 대기 연결만 조회한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstPending = tagLink()
            val secondPending = tagLink(isDeleted = true)
            val synced = tagLink()
            val otherAccountPending = tagLink()
            insertWithSyncState(accountId, firstPending, isDirty = true)
            insertWithSyncState(accountId, secondPending, isDirty = true)
            insertWithSyncState(accountId, synced, isDirty = false)
            insertWithSyncState(otherAccountId, otherAccountPending, isDirty = true)

            syncDataSource
                .readPendingList(accountId = accountId)
                .shouldContainExactlyInAnyOrder(firstPending, secondPending)
        }

        test("TC-36179c3a-4170-42bc-beda-73aa6e1b75a0 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 태그와 태그의 연결은 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val guestEntity = tagLink()
            val accountEntity = tagLink()
            insertWithSyncState(accountId = Uuid.NIL, tagLink = guestEntity, isDirty = true)
            insertWithSyncState(accountId = accountId, tagLink = accountEntity, isDirty = true)

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(accountEntity)
        }

        test("TC-f5c45486-bd23-45b1-bd7f-493816468e58 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagLink = tagLink()
            insertWithSyncState(accountId, tagLink, isDirty = true)

            transaction.clearPending(accountId = accountId, tagLinkList = listOf(tagLink))

            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
        }

        test("TC-882adb8e-cd96-4862-a529-585bb17af19a 업로드 중 수정 시각이 바뀐 연결은 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushed = tagLink(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changed = pushed.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, changed, isDirty = true)

            transaction.clearPending(accountId = accountId, tagLinkList = listOf(pushed))

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(changed)
        }

        test("대기 해제는 요청한 계정의 연결만 바꾼다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagLink = tagLink()
            insertWithSyncState(accountId, tagLink, isDirty = true)
            insertWithSyncState(otherAccountId, tagLink, isDirty = true)

            transaction.clearPending(accountId = accountId, tagLinkList = listOf(tagLink))

            isPending(tagLink, accountId) shouldBe false
            isPending(tagLink, otherAccountId) shouldBe true
        }

        test("TC-85437a34-b1a2-4fa0-ae7a-ec766c5b8952 TC-cce8783a-21bb-4316-bdb7-74d4d2fe8853 기록된 순번이 없으면 기본 커서를 사용하고 저장이 끝나면 커서가 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.TAG_LINK) shouldBe 0L

            transaction.upsert(accountId = otherAccountId, tagLinkList = listOf(tagLink()), cursor = 9L)
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.TAG_LINK) shouldBe 0L

            transaction.upsert(accountId = accountId, tagLinkList = listOf(tagLink()), cursor = 3L)
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.TAG_LINK) shouldBe 3L

            transaction.upsert(accountId = accountId, tagLinkList = listOf(tagLink()), cursor = 11L)
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.TAG_LINK) shouldBe 11L
        }

        test("TC-5dd13cc9-5011-41cf-bfae-1314fe0084b6 태그 연결의 내려받기 위치는 다른 종류와 따로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            transaction.upsert(accountId = accountId, tagLinkList = listOf(tagLink()), cursor = 7L)

            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.TAG_LINK) shouldBe 7L
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.TAG) shouldBe 0L
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.MEMO_TAG) shouldBe 0L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-7fe36220-e35f-4c2b-9172-094e9bf95870 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val local = tagLink(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId, local, isDirty = false)

                transaction.upsert(accountId = accountId, tagLinkList = listOf(remote), cursor = 5L)

                findTagLink(fromTagId = local.fromTagId, toTagId = local.toTagId) shouldBe remote
            }
        }

        test("TC-2f3591f7-e9cb-4759-a9aa-01fbf0331179 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = tagLink(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.upsert(accountId = accountId, tagLinkList = listOf(remote), cursor = 5L)

            findTagLink(fromTagId = local.fromTagId, toTagId = local.toTagId) shouldBe local
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.TAG_LINK) shouldBe 5L
        }

        test("TC-c334819a-dc0e-4f1e-8d02-4a932cb77b25 TC-4b6965f3-bda3-4b32-9b08-009f2a266eea 기기에 없던 연결은 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = tagLink()

            transaction.upsert(accountId = accountId, tagLinkList = listOf(remote), cursor = 5L)

            findTagLink(fromTagId = remote.fromTagId, toTagId = remote.toTagId) shouldBe remote
            syncDataSource.readPendingList(accountId = accountId).shouldBeEmpty()
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.TAG_LINK) shouldBe 5L
        }

        test("TC-84a29c64-490b-482a-acab-32fae5dbba26 내려받기 저장은 이미 있는 연결의 대기 여부를 덮어쓰지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pending = tagLink(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val synced = tagLink(updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, pending, isDirty = true)
            insertWithSyncState(accountId, synced, isDirty = false)

            transaction.upsert(
                accountId = accountId,
                tagLinkList = listOf(pending, synced),
                cursor = 5L,
            )

            isPending(pending, accountId) shouldBe true
            isPending(synced, accountId) shouldBe false
        }

        test("TC-e623e29b-3475-44c8-ae2d-19db4830a3ac 같은 출발 태그의 다른 연결은 서로 독립적으로 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTagId = fixtureMonkey.giveMeOne<Uuid>()
            val local = tagLink(fromTagId = fromTagId, updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remoteOfLocal = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))
            val remoteOfNew = tagLink(fromTagId = fromTagId, updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.upsert(
                accountId = accountId,
                tagLinkList = listOf(remoteOfLocal, remoteOfNew),
                cursor = 5L,
            )

            findTagLink(fromTagId = fromTagId, toTagId = local.toTagId) shouldBe local
            findTagLink(fromTagId = fromTagId, toTagId = remoteOfNew.toTagId) shouldBe remoteOfNew
        }

        test("TC-81241ed9-5c4f-4176-a243-e818fb676fd3 내려받기 저장이 실패하면 연결과 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = tagLink()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws TagLinkSyncTestException()
            val failingTransaction = AccountTagLinkSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<TagLinkSyncTestException> {
                failingTransaction.upsert(accountId = accountId, tagLinkList = listOf(remote), cursor = 5L)
            }

            findTagLink(fromTagId = remote.fromTagId, toTagId = remote.toTagId).shouldBeNull()
            syncCursorDataSource.read(accountId = accountId, kind = SyncKindLocalEntity.TAG_LINK) shouldBe 0L
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tagLink(
            fromTagId: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            toTagId: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            isDeleted: Boolean = false,
            updatedAt: Instant = instant(),
        ): TagLinkLocalEntity =
            TagLinkLocalEntity(
                fromTagId = fromTagId,
                toTagId = toTagId,
                isDeleted = isDeleted,
                updatedAt = updatedAt,
                createdAt = instant(),
            )

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
