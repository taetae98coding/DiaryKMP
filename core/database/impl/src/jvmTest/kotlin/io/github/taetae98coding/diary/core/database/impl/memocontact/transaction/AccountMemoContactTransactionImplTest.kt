package io.github.taetae98coding.diary.core.database.impl.memocontact.transaction

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.datasource.AccountCalendarContactBirthdayLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.contact.transaction.AccountContactTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memo.datasource.AccountMemoSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memocontact.datasource.AccountMemoContactLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.memocontact.datasource.AccountMemoContactSyncLocalDataSourceImpl
import io.github.taetae98coding.diary.core.database.impl.tag.transaction.AccountTagTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

private class MemoContactTestException : RuntimeException()

class AccountMemoContactTransactionImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var memoTransaction: AccountMemoTransactionImpl
        lateinit var contactTransaction: AccountContactTransactionImpl
        lateinit var tagTransaction: AccountTagTransactionImpl
        lateinit var accountMemoContactTransaction: AccountMemoContactTransactionImpl
        lateinit var syncTransaction: AccountMemoContactSyncTransactionImpl
        lateinit var dataSource: AccountMemoContactLocalDataSourceImpl
        lateinit var syncDataSource: AccountMemoContactSyncLocalDataSourceImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            memoTransaction = AccountMemoTransactionImpl(database = database)
            contactTransaction = AccountContactTransactionImpl(database = database)
            tagTransaction = AccountTagTransactionImpl(database = database)
            accountMemoContactTransaction = AccountMemoContactTransactionImpl(database = database)
            syncTransaction = AccountMemoContactSyncTransactionImpl(database = database)
            dataSource = AccountMemoContactLocalDataSourceImpl(database = database)
            syncDataSource = AccountMemoContactSyncLocalDataSourceImpl(database = database)
        }

        afterTest {
            database.close()
        }

        suspend fun insertMemoWithContactList(
            accountId: Uuid,
            memo: MemoLocalEntity,
            contactList: List<ContactLocalEntity>,
        ) {
            contactTransaction.upsert(accountId = accountId, contactList = contactList)
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoContactList = contactList.map { contact -> memoContact(memoId = memo.id, contactId = contact.id, memo = memo) },
            )
        }

        suspend fun findMemo(
            accountId: Uuid,
            memoId: Uuid,
        ): MemoLocalEntity? = database.accountMemoDao().find(accountId = accountId, memoId = memoId).first()

        suspend fun findMemoContactList(memoId: Uuid): List<MemoContactLocalEntity> =
            database.useReaderConnection { transactor ->
                transactor.usePrepared(
                    """
                    SELECT memo_id, contact_id, is_deleted, updated_at, created_at
                    FROM memo_contact
                    WHERE memo_id = '$memoId'
                    ORDER BY contact_id ASC
                    """,
                ) { statement -> statement.readAll { it.toMemoContact() } }
            }

        suspend fun getContactList(
            accountId: Uuid,
            memoId: Uuid,
        ): List<ContactLocalEntity> = dataSource.getContactList(accountId = accountId, memoId = memoId).first()

        suspend fun findPendingContactIdList(accountId: Uuid): List<Uuid> =
            syncDataSource
                .findPending(accountId = accountId)
                .map { memoContact -> memoContact.contactId }

        suspend fun clearAllPending(accountId: Uuid) {
            syncTransaction.clearPending(
                accountId = accountId,
                memoContactList = syncDataSource.findPending(accountId = accountId),
            )
        }

        test("TC-MEMO-CONTACT-DOMAIN-001 하나의 메모에 여러 연락처를 연결해 저장한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val contactList = List(2) { contact() }

            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = contactList)

            getContactList(accountId = accountId, memoId = memo.id) shouldContainExactlyInAnyOrder contactList
        }

        test("TC-MEMO-CONTACT-DOMAIN-002 같은 연락처를 여러 메모에 연결해도 서로 대체되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact()
            val firstMemo = memo()
            val secondMemo = memo()

            listOf(firstMemo, secondMemo).forEach { memo ->
                insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))
            }

            getContactList(accountId = accountId, memoId = firstMemo.id) shouldBe listOf(contact)
            getContactList(accountId = accountId, memoId = secondMemo.id) shouldBe listOf(contact)
        }

        test("TC-MEMO-CONTACT-DOMAIN-003 같은 메모와 연락처의 연결을 다시 저장해도 한 건으로 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val contact = contact()

            repeat(2) {
                insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))
            }

            findMemoContactList(memoId = memo.id) shouldHaveSize 1
        }

        test("TC-MEMO-CONTACT-DOMAIN-004 연결할 연락처가 없으면 연락처 연결 없이 메모만 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()

            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo
            findMemoContactList(memoId = memo.id).shouldBeEmpty()
            getContactList(accountId = accountId, memoId = memo.id).shouldBeEmpty()
        }

        test("TC-MEMO-CONTACT-DOMAIN-005 연락처를 삭제해도 메모와의 연결이 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val contact = contact()
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))

            contactTransaction.upsert(accountId = accountId, contactList = listOf(contact.copy(isDeleted = true)))

            findMemoContactList(memoId = memo.id) shouldBe
                listOf(memoContact(memoId = memo.id, contactId = contact.id, memo = memo))
        }

        test("TC-MEMO-CONTACT-DOMAIN-006 메모를 완료하거나 삭제해도 연락처와의 연결이 유지된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().copy(isFinished = false, isDeleted = false)
            val contact = contact()
            val memoContact = memoContact(memoId = memo.id, contactId = contact.id, memo = memo)
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))

            memoTransaction.updateFinished(accountId = accountId, memoId = memo.id, isFinished = true, updatedAt = instant())
            findMemoContactList(memoId = memo.id) shouldBe listOf(memoContact)

            memoTransaction.updateDeleted(accountId = accountId, memoId = memo.id, isDeleted = true, updatedAt = instant())
            findMemoContactList(memoId = memo.id) shouldBe listOf(memoContact)
        }

        test("TC-MEMO-CONTACT-DOMAIN-007 메모의 제목·설명·컬러·기간 수정은 연락처 연결을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val contact = contact()
            val newDetail = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>()
            val updatedAt = instant()
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))

            memoTransaction.updateDetail(accountId = accountId, memoId = memo.id, detail = newDetail, updatedAt = updatedAt)

            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo.copy(detail = newDetail, updatedAt = updatedAt)
            findMemoContactList(memoId = memo.id) shouldBe
                listOf(memoContact(memoId = memo.id, contactId = contact.id, memo = memo))
            getContactList(accountId = accountId, memoId = memo.id) shouldBe listOf(contact)
        }

        test("TC-MEMO-CONTACT-DOMAIN-007 연락처의 이름·설명·URL·요청 헤더 수정은 연락처 연결을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val contact = contact()
            val changedContact = contact.copy(detail = contactDetail(), updatedAt = instant())
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))

            contactTransaction.upsert(accountId = accountId, contactList = listOf(changedContact))

            findMemoContactList(memoId = memo.id) shouldBe
                listOf(memoContact(memoId = memo.id, contactId = contact.id, memo = memo))
            getContactList(accountId = accountId, memoId = memo.id) shouldBe listOf(changedContact)
        }

        test("TC-MEMO-CONTACT-DOMAIN-008 TC-MEMO-DETAIL-DATA-033 연결을 해제하면 조회에서 제외되고 해제 상태로 남는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val contact = contact()
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))
            val removedAt = instant()

            accountMemoContactTransaction.upsert(accountId = accountId, memoId = memo.id, contactId = contact.id, isDeleted = true, updatedAt = removedAt)

            getContactList(accountId = accountId, memoId = memo.id).shouldBeEmpty()
            findMemoContactList(memoId = memo.id) shouldBe
                listOf(
                    MemoContactLocalEntity(
                        memoId = memo.id,
                        contactId = contact.id,
                        isDeleted = true,
                        updatedAt = removedAt,
                        createdAt = memo.createdAt,
                    ),
                )
        }

        test("TC-MEMO-CONTACT-DOMAIN-009 해제된 연결을 다시 만들면 생성 시각을 유지한 채 되살아난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val contact = contact()
            val createdAt = instant()
            contactTransaction.upsert(accountId = accountId, contactList = listOf(contact))
            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())
            database.memoContactDao().upsert(
                MemoContactLocalEntity(
                    memoId = memo.id,
                    contactId = contact.id,
                    isDeleted = true,
                    updatedAt = instant(),
                    createdAt = createdAt,
                ),
            )
            val restoredAt = instant()

            accountMemoContactTransaction.upsert(accountId = accountId, memoId = memo.id, contactId = contact.id, isDeleted = false, updatedAt = restoredAt)

            getContactList(accountId = accountId, memoId = memo.id) shouldBe listOf(contact)
            findMemoContactList(memoId = memo.id) shouldBe
                listOf(
                    MemoContactLocalEntity(
                        memoId = memo.id,
                        contactId = contact.id,
                        isDeleted = false,
                        updatedAt = restoredAt,
                        createdAt = createdAt,
                    ),
                )
        }

        test("TC-MEMO-CONTACT-DOMAIN-010 삭제된 연락처는 메모의 연결된 연락처 조회에서 빠진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val keptContact = contact()
            val deletedContact = contact()
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(keptContact, deletedContact))

            contactTransaction.upsert(accountId = accountId, contactList = listOf(deletedContact.copy(isDeleted = true)))

            getContactList(accountId = accountId, memoId = memo.id) shouldBe listOf(keptContact)
        }

        test("TC-MEMO-CONTACT-DOMAIN-011 연결된 연락처는 이름 오름차순으로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val firstContact = contact().withName(name = "AAA")
            val secondContact = contact().withName(name = "BBB")
            val thirdContact = contact().withName(name = "CCC")
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(thirdContact, firstContact, secondContact))

            getContactList(accountId = accountId, memoId = memo.id) shouldBe listOf(firstContact, secondContact, thirdContact)
        }

        test("TC-MEMO-CONTACT-DOMAIN-013 연락처 연결은 메모 목록의 노출과 순서를 바꾸지 않는다") {
            listOf("AAA" to "BBB", "BBB" to "AAA").forEach { (linkedTitle, unlinkedTitle) ->
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val linkedMemo = memo().visible().withTitle(title = linkedTitle)
                val unlinkedMemo = memo().visible().withTitle(title = unlinkedTitle)
                val contact = contact()
                insertMemoWithContactList(accountId = accountId, memo = linkedMemo, contactList = listOf(contact))
                memoTransaction.upsert(accountId = accountId, memoList = listOf(unlinkedMemo), memoTagList = emptyList())

                val memoList =
                    database
                        .accountMemoDao()
                        .page(accountId = accountId, sort = "title")
                        .loadAll()

                memoList.map { memo -> memo.id } shouldBe listOf(linkedMemo, unlinkedMemo).sortedBy { memo -> memo.detail.title }.map { memo -> memo.id }
            }
        }

        test("TC-MEMO-CONTACT-DOMAIN-014 메모 연결은 연락처 목록의 노출과 순서를 바꾸지 않는다") {
            listOf("AAA" to "BBB", "BBB" to "AAA").forEach { (linkedTitle, unlinkedTitle) ->
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = memo()
                val linkedContact = contact().withName(name = linkedTitle)
                val unlinkedContact = contact().withName(name = unlinkedTitle)
                insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(linkedContact))
                contactTransaction.upsert(accountId = accountId, contactList = listOf(unlinkedContact))

                val contactList =
                    database
                        .accountContactDao()
                        .page(accountId = accountId, sort = "name")
                        .loadAll()

                contactList.map { contact -> contact.id } shouldBe listOf(linkedContact, unlinkedContact).sortedBy { contact -> contact.detail.name }.map { contact -> contact.id }
            }
        }

        test("TC-MEMO-CONTACT-DOMAIN-015 메모 연결은 캘린더 생일 노출을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val linkedContact = contact().withName(name = "AAA").withBirthday(date = BIRTHDAY_DATE)
            val unlinkedContact = contact().withName(name = "BBB").withBirthday(date = BIRTHDAY_DATE)
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(linkedContact))
            contactTransaction.upsert(accountId = accountId, contactList = listOf(unlinkedContact))

            val birthdayList =
                AccountCalendarContactBirthdayLocalDataSourceImpl(database = database)
                    .get(accountId = accountId, dateRange = BIRTHDAY_DATE..BIRTHDAY_DATE)
                    .first()

            birthdayList.map { birthday -> birthday.contactId } shouldContainExactlyInAnyOrder listOf(linkedContact.id, unlinkedContact.id)
        }

        test("TC-MEMO-CONTACT-DOMAIN-016 연락처 연결은 태그로 메모를 조회한 결과를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo().visible().copy(primaryTagId = null)
            val contact = contact()
            val tag = tag()
            tagTransaction.upsert(accountId = accountId, tagList = listOf(tag), tagLinkList = emptyList())
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))

            val memoList =
                database
                    .accountTagMemoDao()
                    .page(accountId = accountId, tagId = tag.id, scope = TagScopeLocalEntity.SELF.queryValue, sort = "title")
                    .loadAll()

            memoList.shouldBeEmpty()
        }

        test("TC-MEMO-CONTACT-DOMAIN-017 연결된 연락처의 이름은 메모 검색에 쓰이지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val query = "query-${fixtureMonkey.giveMeOne<Uuid>()}"
            val memo = memo().visible().let { value -> value.copy(detail = value.detail.copy(title = "memo-title", description = "memo-description")) }
            val contact = contact().withName(name = "contact-$query")
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))

            val memoList =
                database
                    .searchMemoDao()
                    .page(accountId = accountId, query = query, sort = "title")
                    .loadAll()

            memoList.shouldBeEmpty()
        }

        test("TC-MEMO-CONTACT-DATA-010 TC-DATA-SYNC-DOMAIN-001 연결을 하나 해제하면 그 연결만 업로드 대기가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val removedContact = contact()
            val keptContact = contact()
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(removedContact, keptContact))
            clearAllPending(accountId = accountId)

            accountMemoContactTransaction.upsert(
                accountId = accountId,
                memoId = memo.id,
                contactId = removedContact.id,
                isDeleted = true,
                updatedAt = instant(),
            )

            findPendingContactIdList(accountId = accountId) shouldBe listOf(removedContact.id)
        }

        test("TC-MEMO-CONTACT-DATA-010 TC-DATA-SYNC-DOMAIN-001 연결을 하나 만들면 그 연결만 업로드 대기가 된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val keptContact = contact()
            val addedContact = contact()
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(keptContact))
            contactTransaction.upsert(accountId = accountId, contactList = listOf(addedContact))
            clearAllPending(accountId = accountId)

            accountMemoContactTransaction.upsert(
                accountId = accountId,
                memoId = memo.id,
                contactId = addedContact.id,
                isDeleted = false,
                updatedAt = instant(),
            )

            findPendingContactIdList(accountId = accountId) shouldBe listOf(addedContact.id)
        }

        test("TC-MEMO-CONTACT-DATA-001 저장된 연결의 생성 시각과 수정 시각은 저장 시점으로 서로 같고 해제되지 않은 상태다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val now = instant()
            val memo = memo().copy(updatedAt = now, createdAt = now)
            val contact = contact()

            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))

            findMemoContactList(memoId = memo.id) shouldBe
                listOf(
                    MemoContactLocalEntity(
                        memoId = memo.id,
                        contactId = contact.id,
                        isDeleted = false,
                        updatedAt = now,
                        createdAt = now,
                    ),
                )
        }

        test("TC-MEMO-CONTACT-DATA-002 저장이 실패하면 메모와 계정 연결, 태그 연결, 연락처 연결이 모두 남지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val contact = contact()
            val memoSyncDataSource = AccountMemoSyncLocalDataSourceImpl(database = database)
            val failingDatabase = spyk(database)
            every { failingDatabase.memoContactDao() } throws MemoContactTestException()
            val failingTransaction = AccountMemoTransactionImpl(database = failingDatabase)

            shouldThrowExactly<MemoContactTestException> {
                failingTransaction.upsert(
                    accountId = accountId,
                    memoList = listOf(memo),
                    memoTagList = listOf(memoTag(memoId = memo.id, tagId = tagId, memo = memo)),
                    memoContactList = listOf(memoContact(memoId = memo.id, contactId = contact.id, memo = memo)),
                )
            }

            findMemo(accountId = accountId, memoId = memo.id).shouldBeNull()
            database.memoTagDao().findByMemoIdList(listOf(memo.id)).shouldBeEmpty()
            findMemoContactList(memoId = memo.id).shouldBeEmpty()
            memoSyncDataSource.findPending(accountId = accountId).shouldBeEmpty()
        }

        test("TC-MEMO-CONTACT-DATA-003 같은 연결을 다른 수정 시각으로 저장하면 마지막 내용으로 덮어쓴다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val changedMemo = memo.copy(updatedAt = instant())
            val contact = contact()

            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))
            insertMemoWithContactList(accountId = accountId, memo = changedMemo, contactList = listOf(contact))

            findMemoContactList(memoId = memo.id) shouldBe
                listOf(
                    MemoContactLocalEntity(
                        memoId = memo.id,
                        contactId = contact.id,
                        isDeleted = false,
                        updatedAt = changedMemo.updatedAt,
                        createdAt = changedMemo.createdAt,
                    ),
                )
        }

        test("TC-MEMO-CONTACT-DATA-004 해제되지 않은 연결의 연락처만 메모의 연락처로 조회된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val keptContact = contact()
            val removedContact = contact()
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(keptContact, removedContact))

            accountMemoContactTransaction.upsert(accountId = accountId, memoId = memo.id, contactId = removedContact.id, isDeleted = true, updatedAt = instant())

            getContactList(accountId = accountId, memoId = memo.id) shouldBe listOf(keptContact)
        }

        test("TC-MEMO-DETAIL-DATA-032 연락처를 선택하면 연결이 선택 시점으로 저장되고 메모의 다른 값은 바뀌지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val contact = contact()
            contactTransaction.upsert(accountId = accountId, contactList = listOf(contact))
            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())
            val updatedAt = instant()

            accountMemoContactTransaction.upsert(accountId = accountId, memoId = memo.id, contactId = contact.id, isDeleted = false, updatedAt = updatedAt)

            findMemoContactList(memoId = memo.id) shouldBe
                listOf(
                    MemoContactLocalEntity(
                        memoId = memo.id,
                        contactId = contact.id,
                        isDeleted = false,
                        updatedAt = updatedAt,
                        createdAt = updatedAt,
                    ),
                )
            findMemo(accountId = accountId, memoId = memo.id) shouldBe memo
            getContactList(accountId = accountId, memoId = memo.id) shouldBe listOf(contact)
        }

        test("TC-MEMO-DETAIL-DATA-037 복사본에 원본의 해제되지 않은 연락처 연결만 복사 시점으로 저장된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val source = memo()
            val keptContact = contact()
            val removedContact = contact()
            insertMemoWithContactList(accountId = accountId, memo = source, contactList = listOf(keptContact, removedContact))
            accountMemoContactTransaction.upsert(accountId = accountId, memoId = source.id, contactId = removedContact.id, isDeleted = true, updatedAt = instant())

            val copiedAt = instant()
            val copy = memo().copy(updatedAt = copiedAt, createdAt = copiedAt)
            val sourceContactIdSet =
                dataSource
                    .findContactIdList(accountId = accountId, memoId = source.id)
                    .toSet()
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(copy),
                memoTagList = emptyList(),
                memoContactList =
                    sourceContactIdSet.map { contactId ->
                        MemoContactLocalEntity(
                            memoId = copy.id,
                            contactId = contactId,
                            isDeleted = false,
                            updatedAt = copiedAt,
                            createdAt = copiedAt,
                        )
                    },
            )

            sourceContactIdSet shouldBe setOf(keptContact.id)
            findMemoContactList(memoId = copy.id) shouldBe
                listOf(
                    MemoContactLocalEntity(
                        memoId = copy.id,
                        contactId = keptContact.id,
                        isDeleted = false,
                        updatedAt = copiedAt,
                        createdAt = copiedAt,
                    ),
                )
            getContactList(accountId = accountId, memoId = source.id) shouldBe listOf(keptContact)
        }

        test("TC-MEMO-DETAIL-DATA-034 연락처 연결 변경은 연락처의 내용과 수정 시각을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val contact = contact().copy(isFavorite = true)
            contactTransaction.upsert(accountId = accountId, contactList = listOf(contact))
            memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

            accountMemoContactTransaction.upsert(accountId = accountId, memoId = memo.id, contactId = contact.id, isDeleted = false, updatedAt = instant())
            accountMemoContactTransaction.upsert(accountId = accountId, memoId = memo.id, contactId = contact.id, isDeleted = true, updatedAt = instant())

            database.accountContactDao().find(accountId = accountId, contactId = contact.id).first() shouldBe contact
        }

        test("TC-MEMO-DETAIL-DATA-038 삭제된 연락처를 가리키는 원본 연결도 복사본에 만들어진다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val source = memo()
            val deletedContact = contact()
            insertMemoWithContactList(accountId = accountId, memo = source, contactList = listOf(deletedContact))
            contactTransaction.upsert(accountId = accountId, contactList = listOf(deletedContact.copy(isDeleted = true)))

            val copiedAt = instant()
            val copy = memo().copy(updatedAt = copiedAt, createdAt = copiedAt)
            val sourceContactIdSet =
                dataSource
                    .findContactIdList(accountId = accountId, memoId = source.id)
                    .toSet()
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(copy),
                memoTagList = emptyList(),
                memoContactList =
                    sourceContactIdSet.map { contactId ->
                        MemoContactLocalEntity(
                            memoId = copy.id,
                            contactId = contactId,
                            isDeleted = false,
                            updatedAt = copiedAt,
                            createdAt = copiedAt,
                        )
                    },
            )

            sourceContactIdSet shouldBe setOf(deletedContact.id)
            findMemoContactList(memoId = copy.id).map { memoContact -> memoContact.contactId } shouldBe listOf(deletedContact.id)
        }

        test("TC-MEMO-DETAIL-DATA-046 복사는 원본의 연락처 연결을 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val source = memo()
            val contact = contact()
            insertMemoWithContactList(accountId = accountId, memo = source, contactList = listOf(contact))
            val sourceMemoContactList = findMemoContactList(memoId = source.id)

            val copiedAt = instant()
            val copy = memo().copy(updatedAt = copiedAt, createdAt = copiedAt)
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(copy),
                memoTagList = emptyList(),
                memoContactList =
                    dataSource
                        .findContactIdList(accountId = accountId, memoId = source.id)
                        .map { contactId ->
                            MemoContactLocalEntity(
                                memoId = copy.id,
                                contactId = contactId,
                                isDeleted = false,
                                updatedAt = copiedAt,
                                createdAt = copiedAt,
                            )
                        },
            )

            findMemoContactList(memoId = source.id) shouldBe sourceMemoContactList
            getContactList(accountId = accountId, memoId = source.id) shouldBe listOf(contact)
        }

        listOf(
            "완료된" to { memo: MemoLocalEntity -> memo.copy(isFinished = true, isDeleted = false) },
            "삭제된" to { memo: MemoLocalEntity -> memo.copy(isFinished = false, isDeleted = true) },
        ).forEach { (label, change) ->
            test("TC-MEMO-DETAIL-DOMAIN-012 $label 메모도 연락처 연결을 만들 수 있다") {
                val accountId = fixtureMonkey.giveMeOne<Uuid>()
                val memo = change(memo())
                val contact = contact()
                contactTransaction.upsert(accountId = accountId, contactList = listOf(contact))
                memoTransaction.upsert(accountId = accountId, memoList = listOf(memo), memoTagList = emptyList())

                accountMemoContactTransaction.upsert(accountId = accountId, memoId = memo.id, contactId = contact.id, isDeleted = false, updatedAt = instant())

                getContactList(accountId = accountId, memoId = memo.id) shouldBe listOf(contact)
            }
        }
    }) {
    public companion object {
        private val BIRTHDAY_DATE = LocalDate(year = 1990, month = 3, day = 4)

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::updatedAt, instant())
                .setExp(MemoLocalEntity::createdAt, instant())
                .sample()

        private fun contact(): ContactLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<ContactLocalEntity>()
                .setExp(ContactLocalEntity::detail, contactDetail())
                // 목록은 즐겨찾기를 앞세우므로, 이름 순서를 확인하는 연락처는 즐겨찾기 여부를 고정한다.
                .setExp(ContactLocalEntity::isFavorite, false)
                .setExp(ContactLocalEntity::isDeleted, false)
                .setExp(ContactLocalEntity::updatedAt, instant())
                .setExp(ContactLocalEntity::createdAt, instant())
                .sample()

        private fun contactDetail(): ContactDetailLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<ContactDetailLocalEntity>()
                .setExp(ContactDetailLocalEntity::phoneNumberList, emptyList<Nothing>())
                .sample()

        private fun ContactLocalEntity.withName(name: String): ContactLocalEntity = copy(detail = detail.copy(name = name))

        private fun ContactLocalEntity.withBirthday(date: LocalDate): ContactLocalEntity = copy(detail = detail.copy(birthday = date, birthdayCalendar = ContactBirthdayCalendarLocalEntity.SOLAR))

        private fun MemoLocalEntity.withTitle(title: String): MemoLocalEntity = copy(detail = detail.copy(title = title))

        // 목록 조회는 완료되지 않고 삭제되지 않은 메모만 노출하므로, 노출 기준을 검증하는 메모는 두 상태를 고정한다.
        private fun MemoLocalEntity.visible(): MemoLocalEntity = copy(isFinished = false, isDeleted = false)

        private fun tag(): TagLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<TagLocalEntity>()
                .setExp(TagLocalEntity::isFinished, false)
                .setExp(TagLocalEntity::isDeleted, false)
                .setExp(TagLocalEntity::updatedAt, instant())
                .setExp(TagLocalEntity::createdAt, instant())
                .sample()

        private suspend fun <T : Any> PagingSource<Int, T>.loadAll(): List<T> =
            load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
                .shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, T>>()
                .data

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private fun memoContact(
            memoId: Uuid,
            contactId: Uuid,
            memo: MemoLocalEntity,
        ): MemoContactLocalEntity =
            MemoContactLocalEntity(
                memoId = memoId,
                contactId = contactId,
                isDeleted = false,
                updatedAt = memo.updatedAt,
                createdAt = memo.createdAt,
            )

        private fun memoTag(
            memoId: Uuid,
            tagId: Uuid,
            memo: MemoLocalEntity,
        ): MemoTagLocalEntity =
            MemoTagLocalEntity(
                memoId = memoId,
                tagId = tagId,
                isDeleted = false,
                updatedAt = memo.updatedAt,
                createdAt = memo.createdAt,
            )

        private fun <T> SQLiteStatement.readAll(read: (SQLiteStatement) -> T): List<T> =
            buildList {
                while (step()) {
                    add(read(this@readAll))
                }
            }

        private fun SQLiteStatement.toMemoContact(): MemoContactLocalEntity =
            MemoContactLocalEntity(
                memoId = Uuid.parse(getText(0)),
                contactId = Uuid.parse(getText(1)),
                isDeleted = getBoolean(2),
                updatedAt = Instant.fromEpochMilliseconds(getLong(3)),
                createdAt = Instant.fromEpochMilliseconds(getLong(4)),
            )
    }
}
