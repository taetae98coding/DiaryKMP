package io.github.taetae98coding.diary.core.database.impl.memocontact.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.transaction.AccountContactTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memocontact.transaction.AccountMemoContactSyncTransactionImpl
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountMemoContactLocalDataSourceImplTest :
    FunSpec({
        lateinit var database: DiaryDatabase
        lateinit var dataSource: AccountMemoContactLocalDataSourceImpl
        lateinit var memoTransaction: AccountMemoTransactionImpl
        lateinit var contactTransaction: AccountContactTransactionImpl
        lateinit var syncTransaction: AccountMemoContactSyncTransactionImpl

        beforeTest {
            database =
                Room
                    .inMemoryDatabaseBuilder<DiaryDatabase>()
                    .setDriver(BundledSQLiteDriver())
                    .build()
            dataSource = AccountMemoContactLocalDataSourceImpl(database = database)
            memoTransaction = AccountMemoTransactionImpl(database = database)
            contactTransaction = AccountContactTransactionImpl(database = database)
            syncTransaction = AccountMemoContactSyncTransactionImpl(database = database)
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
                memoContactList =
                    contactList.map { contact ->
                        MemoContactLocalEntity(
                            memoId = memo.id,
                            contactId = contact.id,
                            isDeleted = false,
                            updatedAt = memo.updatedAt,
                            createdAt = memo.createdAt,
                        )
                    },
            )
        }

        suspend fun loadSelectableContact(
            accountId: Uuid,
            query: String = "",
        ): List<ContactLocalEntity> {
            val page =
                dataSource.pageSelectableContact(accountId = accountId, query = query).load(
                    PagingSource.LoadParams.Refresh(
                        key = null,
                        loadSize = PAGE_SIZE,
                        placeholdersEnabled = false,
                    ),
                )

            return (page as PagingSource.LoadResult.Page).data
        }

        test("TC-MEMO-DETAIL-DOMAIN-013 삭제된 연락처는 연결이 남아 있어도 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val contact = contact()
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))

            contactTransaction.upsert(accountId = accountId, contactList = listOf(contact.copy(isDeleted = true)))

            dataSource.getContactList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()
            loadSelectableContact(accountId = accountId).shouldBeEmpty()
        }

        test("TC-MEMO-CONTACT-DOMAIN-011 조회한 연락처는 이름 오름차순으로 정렬된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val lastContact = contact(name = LAST_CONTACT_NAME)
            val firstContact = contact(name = FIRST_CONTACT_NAME)
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(lastContact, firstContact))

            dataSource.getContactList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(firstContact, lastContact)
        }

        test("TC-MEMO-CONTACT-DOMAIN-012 즐겨찾기 여부는 연결된 연락처의 순서를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val lastFavoriteContact = contact(name = LAST_CONTACT_NAME).copy(isFavorite = true)
            val firstContact = contact(name = FIRST_CONTACT_NAME)
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(lastFavoriteContact, firstContact))

            dataSource.getContactList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(firstContact, lastFavoriteContact)
        }

        test("TC-MEMO-CONTACT-DATA-009 가리키는 연락처가 기기에 없는 연결은 저장되지만 조회에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val missingContactId = fixtureMonkey.giveMeOne<Uuid>()
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoContactList = emptyList(),
            )

            syncTransaction.save(
                accountId = accountId,
                memoContactList = listOf(memoContact(memoId = memo.id, contactId = missingContactId)),
                cursor = 1L,
            )

            database.memoContactDao().findByMemoIdList(listOf(memo.id)).size shouldBe 1
            dataSource.getContactList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-MEMO-CONTACT-DATA-009 가리키는 메모가 기기에 없는 연결은 저장되지만 조회에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val missingMemoId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact()
            contactTransaction.upsert(accountId = accountId, contactList = listOf(contact))

            syncTransaction.save(
                accountId = accountId,
                memoContactList = listOf(memoContact(memoId = missingMemoId, contactId = contact.id)),
                cursor = 1L,
            )

            database.memoContactDao().findByMemoIdList(listOf(missingMemoId)).size shouldBe 1
            dataSource.getContactList(accountId = accountId, memoId = missingMemoId).first().shouldBeEmpty()
        }

        test("메모가 내려받아지면 먼저 저장되어 있던 연결의 연락처가 함께 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val contact = contact()
            contactTransaction.upsert(accountId = accountId, contactList = listOf(contact))
            syncTransaction.save(
                accountId = accountId,
                memoContactList = listOf(memoContact(memoId = memo.id, contactId = contact.id)),
                cursor = 1L,
            )
            dataSource.getContactList(accountId = accountId, memoId = memo.id).first().shouldBeEmpty()

            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoContactList = emptyList(),
            )

            dataSource.getContactList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(contact)
        }

        test("다른 계정의 연락처는 연결이 있어도 조회되지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact()))

            dataSource.getContactList(accountId = otherAccountId, memoId = memo.id).first().shouldBeEmpty()
        }

        test("TC-MEMO-CONTACT-INPUT-DOMAIN-001 선택할 수 있는 연락처는 계정과 연결된 삭제되지 않은 연락처다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val otherAccountId = fixtureMonkey.giveMeOne<Uuid>()
            val selectableContact = contact()
            val deletedContact = contact().copy(isDeleted = true)
            val otherAccountContact = contact()
            contactTransaction.upsert(accountId = accountId, contactList = listOf(selectableContact, deletedContact))
            contactTransaction.upsert(accountId = otherAccountId, contactList = listOf(otherAccountContact))

            loadSelectableContact(accountId = accountId) shouldBe listOf(selectableContact)
        }

        test("TC-MEMO-CONTACT-INPUT-DOMAIN-002 선택할 수 있는 연락처는 이름 오름차순으로 정렬된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val lastContact = contact(name = LAST_CONTACT_NAME)
            val firstContact = contact(name = FIRST_CONTACT_NAME)
            contactTransaction.upsert(accountId = accountId, contactList = listOf(lastContact, firstContact))

            loadSelectableContact(accountId = accountId) shouldBe listOf(firstContact, lastContact)
        }

        test("TC-MEMO-CONTACT-INPUT-DOMAIN-003 즐겨찾기는 선택 목록의 순서를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val lastFavoriteContact = contact(name = LAST_CONTACT_NAME).copy(isFavorite = true)
            val firstContact = contact(name = FIRST_CONTACT_NAME)
            contactTransaction.upsert(accountId = accountId, contactList = listOf(lastFavoriteContact, firstContact))

            loadSelectableContact(accountId = accountId) shouldBe listOf(firstContact, lastFavoriteContact)
        }

        test("TC-MEMO-CONTACT-INPUT-DOMAIN-004 연락처 목록의 정렬 선택은 선택 목록의 순서를 바꾸지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            // 선택 목록 조회는 정렬 기준을 받지 않으므로, 수정 시각이 뒤집혀 있어도 이름 오름차순 그대로 나온다.
            val lastContact = contact(name = LAST_CONTACT_NAME).copy(updatedAt = Instant.fromEpochMilliseconds(2_000_000_000))
            val firstContact = contact(name = FIRST_CONTACT_NAME).copy(updatedAt = Instant.fromEpochMilliseconds(1_000_000_000))
            contactTransaction.upsert(accountId = accountId, contactList = listOf(lastContact, firstContact))

            loadSelectableContact(accountId = accountId) shouldBe listOf(firstContact, lastContact)
        }

        test("TC-MEMO-CONTACT-INPUT-DOMAIN-005 선택했더라도 삭제된 연락처는 선택 목록에 나타나지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val keptContact = contact()
            val deletedContact = contact()
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(keptContact, deletedContact))

            contactTransaction.upsert(accountId = accountId, contactList = listOf(deletedContact.copy(isDeleted = true)))

            loadSelectableContact(accountId = accountId) shouldBe listOf(keptContact)
        }

        test("TC-MEMO-CONTACT-INPUT-DOMAIN-009 TC-MEMO-CONTACT-INPUT-DATA-003 검색어는 이름만으로 판정한다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val nameMatchedContact = contact(name = "$SEARCH_QUERY-name")
            val descriptionMatchedContact =
                contact(name = FIRST_CONTACT_NAME).let { contact ->
                    contact.copy(detail = contact.detail.copy(description = SEARCH_QUERY))
                }
            val phoneNumberMatchedContact =
                contact(name = LAST_CONTACT_NAME).let { contact ->
                    contact.copy(detail = contact.detail.copy(phoneNumberList = listOf(ContactPhoneNumberLocalEntity(number = SEARCH_QUERY))))
                }
            contactTransaction.upsert(
                accountId = accountId,
                contactList = listOf(nameMatchedContact, descriptionMatchedContact, phoneNumberMatchedContact),
            )

            loadSelectableContact(accountId = accountId, query = SEARCH_QUERY) shouldBe listOf(nameMatchedContact)
        }

        test("TC-MEMO-CONTACT-INPUT-DOMAIN-010 빈 검색어는 선택 목록을 좁히지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstContact = contact(name = FIRST_CONTACT_NAME)
            val lastContact = contact(name = LAST_CONTACT_NAME)
            contactTransaction.upsert(accountId = accountId, contactList = listOf(firstContact, lastContact))

            loadSelectableContact(accountId = accountId, query = "") shouldBe listOf(firstContact, lastContact)
        }

        test("TC-MEMO-CONTACT-INPUT-DOMAIN-011 검색어는 선택한 연락처에도 같은 기준으로 적용된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val matchedContact = contact(name = "$SEARCH_QUERY-name")
            val selectedContact = contact(name = FIRST_CONTACT_NAME)
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(matchedContact, selectedContact))

            // 선택한 연락처가어도 검색어를 만족하지 않으면 선택 목록에서 빠지고, 연결된 연락처 조회에는 그대로 남는다.
            loadSelectableContact(accountId = accountId, query = SEARCH_QUERY) shouldBe listOf(matchedContact)
            dataSource.getContactList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(selectedContact, matchedContact)
        }

        test("TC-MEMO-CONTACT-INPUT-DATA-001 선택 목록 페이지 조회는 요청한 크기만큼만 가져오고 다음 페이지를 이어서 가져온다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contactList =
                List(CONTACT_COUNT) { index ->
                    contact(name = "Contact-${index.toString().padStart(length = 3, padChar = '0')}")
                }.reversed()
            val expected = contactList.sortedBy { contact -> contact.detail.name }
            contactTransaction.upsert(accountId = accountId, contactList = contactList)

            val firstPage = dataSource.pageSelectableContact(accountId = accountId, query = "").loadPage(loadSize = SMALL_PAGE_SIZE)

            firstPage.data shouldBe expected.take(SMALL_PAGE_SIZE)
            firstPage.nextKey shouldBe SMALL_PAGE_SIZE

            val secondPage =
                dataSource
                    .pageSelectableContact(accountId = accountId, query = "")
                    .loadPage(key = firstPage.nextKey, loadSize = SMALL_PAGE_SIZE)

            secondPage.data shouldBe expected.drop(SMALL_PAGE_SIZE).take(SMALL_PAGE_SIZE)
        }

        test("TC-MEMO-CONTACT-INPUT-DATA-005 연락처가 추가되면 선택 목록 조회 결과에 나타난다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val firstContact = contact(name = FIRST_CONTACT_NAME)
            contactTransaction.upsert(accountId = accountId, contactList = listOf(firstContact))
            loadSelectableContact(accountId = accountId) shouldBe listOf(firstContact)

            val addedContact = contact(name = LAST_CONTACT_NAME)
            contactTransaction.upsert(accountId = accountId, contactList = listOf(addedContact))

            loadSelectableContact(accountId = accountId) shouldBe listOf(firstContact, addedContact)
        }

        test("TC-MEMO-CONTACT-INPUT-DATA-005 저장된 연락처의 이름이 바뀌면 연결된 연락처 조회와 선택 목록에 함께 반영된다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val memo = memo()
            val contact = contact(name = FIRST_CONTACT_NAME)
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))
            val renamedContact = contact.copy(detail = contact.detail.copy(name = LAST_CONTACT_NAME))

            contactTransaction.upsert(accountId = accountId, contactList = listOf(renamedContact))

            dataSource.getContactList(accountId = accountId, memoId = memo.id).first() shouldBe listOf(renamedContact)
            loadSelectableContact(accountId = accountId) shouldBe listOf(renamedContact)
        }

        test("검색어의 대소문자는 판정에 영향을 주지 않는다") {
            val accountId = fixtureMonkey.giveMeOne<Uuid>()
            val contact = contact(name = "AndroidDeveloper")
            contactTransaction.upsert(accountId = accountId, contactList = listOf(contact))

            loadSelectableContact(accountId = accountId, query = "androidDEVELOPER") shouldBe listOf(contact)
        }
    }) {
    public companion object {
        private const val FIRST_CONTACT_NAME = "AppleContact"
        private const val LAST_CONTACT_NAME = "ZebraContact"
        private const val SEARCH_QUERY = "searchable"
        private const val PAGE_SIZE = 20
        private const val SMALL_PAGE_SIZE = 10
        private const val CONTACT_COUNT = 25

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(): MemoLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<MemoLocalEntity>()
                .setExp(MemoLocalEntity::updatedAt, instant())
                .setExp(MemoLocalEntity::createdAt, instant())
                .sample()

        private fun contact(name: String = "name-${fixtureMonkey.giveMeOne<String>()}"): ContactLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<ContactLocalEntity>()
                .setExp(ContactLocalEntity::detail, contactDetail(name = name))
                // 목록은 즐겨찾기를 앞세우므로, 이름 순서를 확인하는 연락처는 즐겨찾기 여부를 고정한다.
                .setExp(ContactLocalEntity::isFavorite, false)
                .setExp(ContactLocalEntity::isDeleted, false)
                .setExp(ContactLocalEntity::updatedAt, instant())
                .setExp(ContactLocalEntity::createdAt, instant())
                .sample()

        private fun contactDetail(name: String): ContactDetailLocalEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<ContactDetailLocalEntity>()
                .setExp(ContactDetailLocalEntity::name, name)
                .setExp(ContactDetailLocalEntity::phoneNumberList, emptyList<Nothing>())
                .sample()

        private fun memoContact(
            memoId: Uuid,
            contactId: Uuid,
        ): MemoContactLocalEntity =
            MemoContactLocalEntity(
                memoId = memoId,
                contactId = contactId,
                isDeleted = false,
                updatedAt = instant(),
                createdAt = instant(),
            )

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

        private suspend fun PagingSource<Int, ContactLocalEntity>.loadPage(
            key: Int? = null,
            loadSize: Int = PAGE_SIZE,
        ): PagingSource.LoadResult.Page<Int, ContactLocalEntity> {
            val params: PagingSource.LoadParams<Int> =
                if (key == null) {
                    PagingSource.LoadParams.Refresh(key = null, loadSize = loadSize, placeholdersEnabled = false)
                } else {
                    PagingSource.LoadParams.Append(key = key, loadSize = loadSize, placeholdersEnabled = false)
                }

            return load(params).shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, ContactLocalEntity>>()
        }
    }
}
