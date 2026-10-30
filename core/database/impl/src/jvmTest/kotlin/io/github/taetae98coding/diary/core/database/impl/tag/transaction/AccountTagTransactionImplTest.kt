package io.github.taetae98coding.diary.core.database.impl.tag.transaction

import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.AccountTagSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.taglink.datasource.AccountTagLinkLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.taglink.datasource.AccountTagLinkSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountTagTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountTagTransactionImpl
        lateinit var syncDataSource: AccountTagSyncLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountTagTransactionImpl(database = database)
            syncDataSource = AccountTagSyncLocalDataSourceImpl(database = database)
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

        suspend fun assertPending(
            accountId: Uuid,
            tagId: Uuid,
        ) {
            syncDataSource
                .readPendingList(accountId = accountId)
                .any { tag -> tag.id == tagId } shouldBe true
        }

        suspend fun countRows(table: String): Long =
            database.useReaderConnection { transactor ->
                transactor.usePrepared("SELECT COUNT(*) FROM $table") { statement ->
                    statement.step()
                    statement.getLong(0)
                }
            }

        test("TC-a9ca601b-2e0a-4754-a20f-47aabd8a2397 새 태그와 계정 연결, 고른 태그로 향하는 연결을 모두 기기에 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstToTag = tag().copy(isDeleted = false)
            val secondToTag = tag().copy(isDeleted = false)
            val newTag = tag()
            insertWithSyncState(accountId, firstToTag, isDirty = false)
            insertWithSyncState(accountId, secondToTag, isDirty = false)
            val linkList = listOf(firstToTag, secondToTag).map { toTag -> link(fromTagId = newTag.id, toTagId = toTag.id) }

            transaction.upsert(accountId = accountId, tagList = listOf(newTag), tagLinkList = linkList)

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(newTag)
            AccountTagLinkLocalDataSourceImpl(database = database)
                .getTagList(accountId = accountId, fromTagId = newTag.id)
                .first()
                .map { tag -> tag.id } shouldContainExactlyInAnyOrder listOf(firstToTag.id, secondToTag.id)
        }

        test("TC-a660dc59-56d9-4824-99f4-aa8a8d9bb043 연결을 저장하지 못하면 새 태그와 계정 연결, 연결 중 어느 것도 남지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val toTag = tag()
            val newTag = tag()
            insertWithSyncState(accountId, toTag, isDirty = false)
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val failingDatabase = spyk(database)
            every { failingDatabase.accountTagLinkDao() } throws throwable
            val failingTransaction = AccountTagTransactionImpl(database = failingDatabase)

            shouldThrow<IllegalStateException> {
                failingTransaction.upsert(
                    accountId = accountId,
                    tagList = listOf(newTag),
                    tagLinkList = listOf(link(fromTagId = newTag.id, toTagId = toTag.id)),
                )
            }.message shouldBe throwable.message

            countRows("tag") shouldBe 1
            countRows("account_tag") shouldBe 1
            countRows("tag_link") shouldBe 0
            countRows("account_tag_link") shouldBe 0
            syncDataSource.readPendingList(accountId = accountId) shouldBe emptyList()
        }

        test("TC-11e51a31-7a79-4ddc-8eac-ef6b45a9caa5 태그 생성·수정·완료·다시 시작·삭제·실행 취소는 업로드 대기 상태가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val updatedAt = instant()

            transaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            assertPending(accountId = accountId, tagId = tag.id)

            insertWithSyncState(accountId, tag, isDirty = false)
            transaction.updateDetail(
                accountId = accountId,
                tagId = tag.id,
                detail = fixtureMonkey.giveMeOne<TagDetailLocalEntity>(),
                updatedAt = updatedAt,
            )
            assertPending(accountId = accountId, tagId = tag.id)

            insertWithSyncState(accountId, tag.copy(isFinished = false), isDirty = false)
            transaction.updateFinished(accountId, tag.id, isFinished = true, updatedAt = updatedAt)
            assertPending(accountId = accountId, tagId = tag.id)

            insertWithSyncState(accountId, tag.copy(isFinished = true), isDirty = false)
            transaction.updateFinished(accountId, tag.id, isFinished = false, updatedAt = updatedAt)
            assertPending(accountId = accountId, tagId = tag.id)

            insertWithSyncState(accountId, tag.copy(isDeleted = false), isDirty = false)
            transaction.updateDeleted(accountId, tag.id, isDeleted = true, updatedAt = updatedAt)
            assertPending(accountId = accountId, tagId = tag.id)

            insertWithSyncState(accountId, tag.copy(isDeleted = true), isDirty = false)
            transaction.updateDeleted(accountId, tag.id, isDeleted = false, updatedAt = updatedAt)
            assertPending(accountId = accountId, tagId = tag.id)
        }

        test("TC-fc0efa55-9c70-44bb-ae31-8eeaec53668f 새 태그를 기기에 저장하면 태그와 함께 만든 연결도 업로드 대기로 기록된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val toTag = tag()
            val newTag = tag()
            val link = TagLinkLocalEntity(fromTagId = newTag.id, toTagId = toTag.id, isDeleted = false, updatedAt = instant(), createdAt = instant())
            insertWithSyncState(accountId, toTag, isDirty = false)

            transaction.upsert(accountId = accountId, tagList = listOf(newTag), tagLinkList = listOf(link))

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(newTag)
            AccountTagLinkSyncLocalDataSourceImpl(database = database)
                .readPendingList(accountId = accountId)
                .map { pending -> pending.fromTagId to pending.toTagId } shouldBe listOf(newTag.id to toTag.id)
        }

        test("TC-8da41335-53f4-4825-a1cd-5fcda367cda2 같은 태그를 반복 변경해도 업로드 대상은 한 건이다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val changedTag = tag.copy(detail = fixtureMonkey.giveMeOne())

            transaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            transaction.upsert(accountId = accountId, tagList = listOf(changedTag), tagLinkList = emptyList())

            syncDataSource.readPendingList(accountId = accountId) shouldBe listOf(changedTag)
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()

        private fun link(
            fromTagId: Uuid,
            toTagId: Uuid,
        ): TagLinkLocalEntity = TagLinkLocalEntity(fromTagId = fromTagId, toTagId = toTagId, isDeleted = false, updatedAt = instant(), createdAt = instant())

        private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
    }
}
