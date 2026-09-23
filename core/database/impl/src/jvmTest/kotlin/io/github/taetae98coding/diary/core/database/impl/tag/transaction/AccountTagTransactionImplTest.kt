package io.github.taetae98coding.diary.core.database.impl.tag.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.tag.datasource.AccountTagSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
                .findPending(accountId = accountId)
                .any { tag -> tag.id == tagId } shouldBe true
        }

        test("TC-DATA-SYNC-DOMAIN-001 태그 생성·수정·완료·삭제·실행 취소는 업로드 대기 상태가 된다") {
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

            insertWithSyncState(accountId, tag.copy(isDeleted = false), isDirty = false)
            transaction.updateDeleted(accountId, tag.id, isDeleted = true, updatedAt = updatedAt)
            assertPending(accountId = accountId, tagId = tag.id)

            insertWithSyncState(accountId, tag.copy(isFinished = true), isDirty = false)
            transaction.updateFinished(accountId, tag.id, isFinished = false, updatedAt = updatedAt)
            assertPending(accountId = accountId, tagId = tag.id)

            insertWithSyncState(accountId, tag.copy(isDeleted = true), isDirty = false)
            transaction.updateDeleted(accountId, tag.id, isDeleted = false, updatedAt = updatedAt)
            assertPending(accountId = accountId, tagId = tag.id)
        }

        test("TC-DATA-SYNC-DOMAIN-003 같은 태그를 반복 변경해도 업로드 대상은 한 건이다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tag = tag()
            val changedTag = tag.copy(detail = fixtureMonkey.giveMeOne())

            transaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            transaction.upsert(accountId = accountId, tagList = listOf(changedTag), tagLinkList = emptyList())

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(changedTag)
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

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
