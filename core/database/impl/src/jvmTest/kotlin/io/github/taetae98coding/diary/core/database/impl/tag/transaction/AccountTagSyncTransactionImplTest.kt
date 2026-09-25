package io.github.taetae98coding.diary.core.database.impl.tag.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.AccountTagSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
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

private class TagSyncTestException : RuntimeException()

class AccountTagSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountTagSyncTransactionImpl
        lateinit var syncDataSource: AccountTagSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl
        lateinit var accountTransaction: AccountTagTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountTagSyncTransactionImpl(database = database)
            syncDataSource = AccountTagSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
            accountTransaction = AccountTagTransactionImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            tag: TagLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.tagDao().upsert(listOf(tag))
                database.accountTagDao().upsert(
                    listOf(
                        AccountTagLocalEntity(
                            accountId = accountId,
                            tagId = tag.id,
                            isDirty = isDirty,
                        ),
                    ),
                )
            }
        }

        suspend fun findTag(
            accountId: Uuid,
            tagId: Uuid,
        ): TagLocalEntity? = database.accountTagDao().find(accountId = accountId, tagId = tagId).first()

        suspend fun isPending(
            accountId: Uuid,
            tagId: Uuid,
        ): Boolean = syncDataSource.findPending(accountId = accountId).any { tag -> tag.id == tagId }

        test("TC-DATA-SYNC-DOMAIN-009 현재 계정의 업로드 대기 태그를 조회하고 동기화 완료 태그는 제외한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstPendingTag = tag()
            val secondPendingTag = tag()
            val syncedTag = tag()
            val otherAccountTag = tag()
            insertWithSyncState(accountId, firstPendingTag, isDirty = true)
            insertWithSyncState(accountId, secondPendingTag, isDirty = true)
            insertWithSyncState(accountId, syncedTag, isDirty = false)
            insertWithSyncState(otherAccountId, otherAccountTag, isDirty = true)

            syncDataSource
                .findPending(accountId = accountId)
                .shouldContainExactlyInAnyOrder(firstPendingTag, secondPendingTag)
        }

        test("TC-DATA-SYNC-DOMAIN-087 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 태그는 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val guestTag = tag()
            val accountTag = tag()
            insertWithSyncState(Uuid.NIL, guestTag, isDirty = true)
            insertWithSyncState(accountId, accountTag, isDirty = true)

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(accountTag)
        }

        test("TC-DATA-SYNC-DOMAIN-026 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            insertWithSyncState(accountId, tag, isDirty = true)

            transaction.clearPending(accountId = accountId, tagList = listOf(tag))

            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DOMAIN-027 업로드 중 수정 시각이 바뀐 태그는 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushedTag = tag(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changedTag = pushedTag.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId, changedTag, isDirty = true)

            transaction.clearPending(accountId = accountId, tagList = listOf(pushedTag))

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(changedTag)
        }

        test("TC-DATA-SYNC-DOMAIN-030 태그가 업로드 대기가 되어도 내려받기 위치는 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val cursor = 7L
            transaction.save(accountId = accountId, tagList = listOf(tag), cursor = cursor)

            accountTransaction.updateDetail(
                accountId = accountId,
                tagId = tag.id,
                detail = fixtureMonkey.giveMeOne<TagDetailLocalEntity>(),
                updatedAt = Instant.fromEpochMilliseconds(2_000),
            )

            isPending(accountId = accountId, tagId = tag.id) shouldBe true
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG) shouldBe cursor
        }

        test("TC-DATA-SYNC-DATA-016 내려받기 저장이 끝나면 서버 변경 순번이 커서로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstCursor = 3L
            val secondCursor = 11L

            transaction.save(accountId = accountId, tagList = listOf(tag()), cursor = firstCursor)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG) shouldBe firstCursor

            transaction.save(accountId = accountId, tagList = listOf(tag()), cursor = secondCursor)
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG) shouldBe secondCursor
        }

        test("TC-DATA-SYNC-DATA-017 기록된 순번이 없으면 기본 커서를 사용한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            transaction.save(accountId = otherAccountId, tagList = listOf(tag()), cursor = 9L)

            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG) shouldBe 0L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-022 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localTag = tag(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remoteTag =
                    localTag.copy(
                        detail = fixtureMonkey.giveMeOne(),
                        isFinished = !localTag.isFinished,
                        isDeleted = !localTag.isDeleted,
                        updatedAt = remoteUpdatedAt,
                    )
                insertWithSyncState(accountId, localTag, isDirty = false)

                transaction.save(accountId = accountId, tagList = listOf(remoteTag), cursor = 5L)

                findTag(accountId = accountId, tagId = localTag.id) shouldBe remoteTag
            }
        }

        test("TC-DATA-SYNC-DATA-023 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val localTag = tag(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remoteTag =
                localTag.copy(
                    detail = fixtureMonkey.giveMeOne(),
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                )
            insertWithSyncState(accountId, localTag, isDirty = true)
            val cursor = 5L

            transaction.save(accountId = accountId, tagList = listOf(remoteTag), cursor = cursor)

            findTag(accountId = accountId, tagId = localTag.id) shouldBe localTag
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG) shouldBe cursor
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(3_000),
            "같음" to Instant.fromEpochMilliseconds(2_000),
            "이른" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-024 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localTag = tag(updatedAt = Instant.fromEpochMilliseconds(2_000))
                val remoteTag = localTag.copy(detail = fixtureMonkey.giveMeOne(), updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId, localTag, isDirty = true)

                transaction.save(accountId = accountId, tagList = listOf(remoteTag), cursor = 5L)

                isPending(accountId = accountId, tagId = localTag.id) shouldBe true
            }
        }

        test("TC-DATA-SYNC-DATA-025 기기에 없던 태그는 새로 저장되고 동기화 완료로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remoteTag = tag()

            transaction.save(accountId = accountId, tagList = listOf(remoteTag), cursor = 5L)

            findTag(accountId = accountId, tagId = remoteTag.id) shouldBe remoteTag
            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DATA-026 내려받기 저장이 실패하면 태그와 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remoteTag = tag()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws TagSyncTestException()
            val failingTransaction = AccountTagSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<TagSyncTestException> {
                failingTransaction.save(accountId = accountId, tagList = listOf(remoteTag), cursor = 5L)
            }

            findTag(accountId = accountId, tagId = remoteTag.id).shouldBeNull()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.TAG) shouldBe 0L
        }

        test("TC-DATA-SYNC-DATA-027 업로드한 태그가 같은 내용으로 다시 내려와도 기기 내용은 그대로다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            insertWithSyncState(accountId, tag, isDirty = false)

            transaction.save(accountId = accountId, tagList = listOf(tag), cursor = 5L)

            findTag(accountId = accountId, tagId = tag.id) shouldBe tag
        }

        test("내려받기 저장은 이미 있는 연결의 대기 여부를 덮어쓰지 않고 커서만 갱신한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pendingTag = tag(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val syncedTag = tag(updatedAt = Instant.fromEpochMilliseconds(1_000))
            insertWithSyncState(accountId, pendingTag, isDirty = true)
            insertWithSyncState(accountId, syncedTag, isDirty = false)

            transaction.save(
                accountId = accountId,
                tagList = listOf(pendingTag, syncedTag),
                cursor = 5L,
            )

            isPending(accountId = accountId, tagId = pendingTag.id) shouldBe true
            isPending(accountId = accountId, tagId = syncedTag.id) shouldBe false
        }

        test("대기 해제는 요청한 계정의 연결만 바꾼다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            insertWithSyncState(accountId, tag, isDirty = true)
            insertWithSyncState(otherAccountId, tag, isDirty = true)

            transaction.clearPending(accountId = accountId, tagList = listOf(tag))

            isPending(accountId = accountId, tagId = tag.id) shouldBe false
            isPending(accountId = otherAccountId, tagId = tag.id) shouldBe true
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(updatedAt: Instant = instant()): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::updatedAt, updatedAt)
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
