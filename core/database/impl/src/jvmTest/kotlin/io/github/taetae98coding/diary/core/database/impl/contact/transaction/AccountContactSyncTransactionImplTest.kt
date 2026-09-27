package io.github.taetae98coding.diary.core.database.impl.contact.transaction

import androidx.room3.Room
import androidx.room3.withWriteTransaction
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.datasource.AccountContactLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.contact.datasource.AccountContactSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.contact.entity.AccountContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.datasource.SyncCursorLocalDataSourceImpl
import io.github.taetae98coding.diary.core.testing.contact.contactPhoneNumberCaseList
import io.github.taetae98coding.diary.core.testing.contact.localContact
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class ContactSyncTestException : RuntimeException()

class AccountContactSyncTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var transaction: AccountContactSyncTransactionImpl
        lateinit var dataSource: AccountContactLocalDataSourceImpl
        lateinit var syncDataSource: AccountContactSyncLocalDataSourceImpl
        lateinit var syncCursorDataSource: SyncCursorLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            transaction = AccountContactSyncTransactionImpl(database = database)
            dataSource = AccountContactLocalDataSourceImpl(database = database)
            syncDataSource = AccountContactSyncLocalDataSourceImpl(database = database)
            syncCursorDataSource = SyncCursorLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertWithSyncState(
            accountId: Uuid,
            contact: ContactLocalEntity,
            isDirty: Boolean,
        ) {
            database.withWriteTransaction {
                database.contactDao().upsert(listOf(contact))
                database.accountContactDao().upsert(
                    AccountContactLocalEntity(accountId = accountId, contactId = contact.id, isDirty = isDirty),
                )
            }
        }

        suspend fun isPending(
            accountId: Uuid,
            contactId: Uuid,
        ): Boolean = syncDataSource.findPending(accountId = accountId).any { contact -> contact.id == contactId }

        test("TC-DATA-SYNC-DOMAIN-087 로그인한 계정의 업로드 대상에 게스트 상태에서 만든 연락처는 포함되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val guestEntity = contact()
            val accountEntity = contact()
            insertWithSyncState(accountId = Uuid.NIL, contact = guestEntity, isDirty = true)
            insertWithSyncState(accountId = accountId, contact = accountEntity, isDirty = true)

            syncDataSource.findPending(accountId = accountId) shouldBe listOf(accountEntity)
        }

        test("TC-DATA-SYNC-DOMAIN-026 업로드한 수정 시각이 그대로면 동기화 완료가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact()
            insertWithSyncState(accountId = accountId, contact = contact, isDirty = true)

            transaction.clearPending(accountId = accountId, contactList = listOf(contact))

            syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-DATA-SYNC-DOMAIN-027 업로드 중 수정 시각이 바뀐 연락처는 업로드 대기로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val pushedContact = contact(updatedAt = Instant.fromEpochMilliseconds(1_000))
            val changedContact = pushedContact.copy(updatedAt = Instant.fromEpochMilliseconds(2_000))
            insertWithSyncState(accountId = accountId, contact = changedContact, isDirty = true)

            transaction.clearPending(accountId = accountId, contactList = listOf(pushedContact))

            isPending(accountId = accountId, contactId = changedContact.id) shouldBe true
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(2_000),
            "같음" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-022 서버의 수정 시각이 기기보다 $label 이면 서버 내용을 반영한다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localContact = contact(updatedAt = Instant.fromEpochMilliseconds(1_000))
                val remoteContact =
                    localContact.copy(
                        detail = contact().detail,
                        isFavorite = !localContact.isFavorite,
                        isDeleted = !localContact.isDeleted,
                        updatedAt = remoteUpdatedAt,
                    )
                insertWithSyncState(accountId = accountId, contact = localContact, isDirty = false)

                transaction.save(accountId = accountId, contactList = listOf(remoteContact), cursor = 5L)

                dataSource.find(accountId = accountId, contactId = localContact.id).first() shouldBe remoteContact
            }
        }

        test("TC-DATA-SYNC-DATA-023 기기의 수정 시각이 더 늦으면 기기 내용을 유지한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val localContact = contact(updatedAt = Instant.fromEpochMilliseconds(2_000))
            val remoteContact =
                localContact.copy(
                    detail = contact().detail,
                    updatedAt = Instant.fromEpochMilliseconds(1_000),
                )
            insertWithSyncState(accountId = accountId, contact = localContact, isDirty = true)

            transaction.save(accountId = accountId, contactList = listOf(remoteContact), cursor = 5L)

            dataSource.find(accountId = accountId, contactId = localContact.id).first() shouldBe localContact
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.CONTACT) shouldBe 5L
        }

        listOf(
            "늦음" to Instant.fromEpochMilliseconds(3_000),
            "같음" to Instant.fromEpochMilliseconds(2_000),
            "이른" to Instant.fromEpochMilliseconds(1_000),
        ).forEach { (label, remoteUpdatedAt) ->
            test("TC-DATA-SYNC-DATA-024 서버의 수정 시각이 기기보다 $label 이어도 업로드 대기 여부는 바뀌지 않는다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val localContact = contact(updatedAt = Instant.fromEpochMilliseconds(2_000))
                val remoteContact = localContact.copy(detail = contact().detail, updatedAt = remoteUpdatedAt)
                insertWithSyncState(accountId = accountId, contact = localContact, isDirty = true)

                transaction.save(accountId = accountId, contactList = listOf(remoteContact), cursor = 5L)

                isPending(accountId = accountId, contactId = localContact.id) shouldBe true
            }
        }

        test("TC-DATA-SYNC-DATA-026 내려받기 저장이 실패하면 연락처와 커서가 모두 반영되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val remoteContact = contact()
            val failingDatabase = spyk(database)
            every { failingDatabase.syncCursorDao() } throws ContactSyncTestException()
            val failingTransaction = AccountContactSyncTransactionImpl(database = failingDatabase)

            shouldThrowExactly<ContactSyncTestException> {
                failingTransaction.save(accountId = accountId, contactList = listOf(remoteContact), cursor = 5L)
            }

            dataSource.find(accountId = accountId, contactId = remoteContact.id).first().shouldBeNull()
            syncCursorDataSource.find(accountId = accountId, kind = SyncKind.CONTACT) shouldBe 0L
        }

        test("TC-DATA-SYNC-DATA-025 기기에 없던 연락처는 계정과 연결되어 새로 저장되고 동기화 완료로 기록된다") {
            fixtureMonkey.contactPhoneNumberCaseList().forEach { numberList ->
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val cursor = fixtureMonkey.giveMeOne<Long>()
                val remote =
                    fixtureMonkey.localContact(
                        numberList = numberList,
                        hasMeasure = fixtureMonkey.giveMeOne<Boolean>(),
                        isFavorite = fixtureMonkey.giveMeOne<Boolean>(),
                        isDeleted = false,
                    )

                transaction.save(accountId = accountId, contactList = listOf(remote), cursor = cursor)

                dataSource.find(accountId = accountId, contactId = remote.id).first() shouldBe remote
                syncDataSource.findPending(accountId = accountId).shouldBeEmpty()
                syncCursorDataSource.find(accountId = accountId, kind = SyncKind.CONTACT) shouldBe cursor
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun contact(updatedAt: Instant = fixtureMonkey.giveMeOne<Instant>()): ContactLocalEntity =
            fixtureMonkey
                .localContact(
                    numberList = fixtureMonkey.contactPhoneNumberCaseList().random(),
                    hasMeasure = fixtureMonkey.giveMeOne<Boolean>(),
                    isFavorite = fixtureMonkey.giveMeOne<Boolean>(),
                    isDeleted = fixtureMonkey.giveMeOne<Boolean>(),
                ).copy(updatedAt = updatedAt)
    }
}
