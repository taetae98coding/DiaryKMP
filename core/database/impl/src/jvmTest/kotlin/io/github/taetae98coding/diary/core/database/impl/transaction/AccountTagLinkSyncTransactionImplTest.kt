package io.github.taetae98coding.diary.core.database.impl.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.datasource.AccountTagLinkSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.entity.AccountTagLinkLocalEntity
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
                .findPending(accountId = accountId)
                .any { pending -> pending.fromTagId == tagLink.fromTagId && pending.toTagId == tagLink.toTagId }

        test("TC-TAG-LINK-DATA-003 해제된 연결도 포함해 현재 계정의 업로드 대기 연결만 조회한다") {
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
                .findPending(accountId = accountId)
                .shouldContainExactlyInAnyOrder(firstPending, secondPending)
        }

        test("TC-DATA-SYNC-DOMAIN-026 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagLink = tagLink()
            insertWithSyncState(accountId, tagLink, isDirty = true)

            transaction.clearPending(accountId = accountId, tagLinkList = listOf(tagLink))

            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DOMAIN-027 업로드 중 수정 시각이 바뀐 연결은 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushed = tagLink(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changed = pushed.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, changed, isDirty = true)

            transaction.clearPending(accountId = accountId, tagLinkList = listOf(pushed))

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(changed)
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

        test("TC-DATA-SYNC-DATA-016 TC-DATA-SYNC-DATA-017 기록된 순번이 없으면 기본 커서를 사용하고 저장이 끝나면 커서가 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG_LINK) shouldBe 0L

            transaction.save(accountId = otherAccountId, tagLinkList = listOf(tagLink()), cursor = 9L)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG_LINK) shouldBe 0L

            transaction.save(accountId = accountId, tagLinkList = listOf(tagLink()), cursor = 3L)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG_LINK) shouldBe 3L

            transaction.save(accountId = accountId, tagLinkList = listOf(tagLink()), cursor = 11L)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG_LINK) shouldBe 11L
        }

        test("TC-DATA-SYNC-DATA-028 태그 연결의 내려받기 위치는 다른 종류와 따로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()

            transaction.save(accountId = accountId, tagLinkList = listOf(tagLink()), cursor = 7L)

            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG_LINK) shouldBe 7L
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG) shouldBe 0L
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.MEMO_TAG) shouldBe 0L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-022 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val local = tagLink(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId, local, isDirty = false)

                transaction.save(accountId = accountId, tagLinkList = listOf(remote), cursor = 5L)

                findTagLink(fromTagId = local.fromTagId, toTagId = local.toTagId) shouldBe remote
            }
        }

        test("TC-DATA-SYNC-DATA-023 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val local = tagLink(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remote = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.save(accountId = accountId, tagLinkList = listOf(remote), cursor = 5L)

            findTagLink(fromTagId = local.fromTagId, toTagId = local.toTagId) shouldBe local
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG_LINK) shouldBe 5L
        }

        test("TC-DATA-SYNC-DATA-025 TC-TAG-LINK-DATA-006 기기에 없던 연결은 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = tagLink()

            transaction.save(accountId = accountId, tagLinkList = listOf(remote), cursor = 5L)

            findTagLink(fromTagId = remote.fromTagId, toTagId = remote.toTagId) shouldBe remote
            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG_LINK) shouldBe 5L
        }

        test("TC-DATA-SYNC-DATA-024 내려받기 저장은 이미 있는 연결의 대기 여부를 덮어쓰지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pending = tagLink(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val synced = tagLink(updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, pending, isDirty = true)
            insertWithSyncState(accountId, synced, isDirty = false)

            transaction.save(
                accountId = accountId,
                tagLinkList = listOf(pending, synced),
                cursor = 5L,
            )

            isPending(pending, accountId) shouldBe true
            isPending(synced, accountId) shouldBe false
        }

        test("TC-TAG-LINK-DATA-001 같은 출발 태그의 다른 연결은 서로 독립적으로 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val fromTagId = fixtureMonkey.giveMeOne<Uuid>()
            val local = tagLink(fromTagId = fromTagId, updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remoteOfLocal = local.copy(isDeleted = !local.isDeleted, updatedAt = Instant.fromEpochMilliseconds(1_000))
            val remoteOfNew = tagLink(fromTagId = fromTagId, updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, local, isDirty = true)

            transaction.save(
                accountId = accountId,
                tagLinkList = listOf(remoteOfLocal, remoteOfNew),
                cursor = 5L,
            )

            findTagLink(fromTagId = fromTagId, toTagId = local.toTagId) shouldBe local
            findTagLink(fromTagId = fromTagId, toTagId = remoteOfNew.toTagId) shouldBe remoteOfNew
        }

        test("TC-DATA-SYNC-DATA-026 내려받기 저장이 실패하면 연결과 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remote = tagLink()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws TagLinkSyncTestException()
            val failingTransaction = AccountTagLinkSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<TagLinkSyncTestException> {
                failingTransaction.save(accountId = accountId, tagLinkList = listOf(remote), cursor = 5L)
            }

            findTagLink(fromTagId = remote.fromTagId, toTagId = remote.toTagId).shouldBeNull()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG_LINK) shouldBe 0L
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

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
