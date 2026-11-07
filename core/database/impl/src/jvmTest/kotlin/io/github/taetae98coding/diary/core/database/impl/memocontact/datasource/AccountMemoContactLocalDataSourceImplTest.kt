package io.github.taetae98coding.diary.core.database.impl.memocontact.datasource

import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactListItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.contact.transaction.AccountContactSyncTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.contact.transaction.AccountContactTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memo.transaction.AccountMemoTransactionImpl
import io.github.taetae98coding.diary.core.database.impl.memocontact.transaction.AccountMemoContactSyncTransactionImpl
import io.github.taetae98coding.diary.core.testing.contact.contactDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.contact.contactLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.memoLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
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
            contactTransaction.upsert(accountId = accountId, contactList = contactList, contactTagList = emptyList())
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

        suspend fun awaitSelectableContactInvalidated(
            accountId: Uuid,
            change: suspend () -> Unit,
        ) {
            val pagingSource = dataSource.pageSelectableContact(accountId = accountId, query = "")
            pagingSource.load(PagingSource.LoadParams.Refresh(key = null, loadSize = PAGE_SIZE, placeholdersEnabled = false))
            val invalidated = CompletableDeferred<Unit>()
            pagingSource.registerInvalidatedCallback { invalidated.complete(Unit) }

            change()

            withTimeout(INVALIDATION_TIMEOUT_MILLIS) { invalidated.await() }
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

            return (page as PagingSource.LoadResult.Page).data.map { item -> item.contact }
        }

        test("TC-276c3f6e-95ad-47dc-b86d-b00c056a9355 삭제된 연락처는 연결이 남아 있어도 조회되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val contact = contact()
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))

            contactTransaction.upsert(accountId = accountId, contactList = listOf(contact.copy(isDeleted = true)), contactTagList = emptyList())

            dataSource
                .getContactList(accountId = accountId, memoId = memo.id)
                .contacts()
                .first()
                .shouldBeEmpty()
            loadSelectableContact(accountId = accountId).shouldBeEmpty()
        }

        test("TC-b36e07e0-76f3-4fc1-9256-07fc025947a1 TC-b0ea5543-aaa8-4293-a591-3872f9ad852e TC-fdd70132-3ae7-46e0-8cb8-bf4fb62a3a23 조회한 연락처는 연결한 순서와 관계없이 이름 오름차순으로 정렬된다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val lastContact = contact(name = LAST_CONTACT_NAME)
            val firstContact = contact(name = FIRST_CONTACT_NAME)
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(lastContact, firstContact))

            dataSource.getContactList(accountId = accountId, memoId = memo.id).contacts().first() shouldBe listOf(firstContact, lastContact)
        }

        test("TC-3615b0c8-4c70-4d05-9608-96752eba531e 즐겨찾기 여부는 연결된 연락처의 순서를 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val lastFavoriteContact = contact(name = LAST_CONTACT_NAME).copy(isFavorite = true)
            val firstContact = contact(name = FIRST_CONTACT_NAME)
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(lastFavoriteContact, firstContact))

            dataSource.getContactList(accountId = accountId, memoId = memo.id).contacts().first() shouldBe listOf(firstContact, lastFavoriteContact)
        }

        test("TC-f54e50c2-7078-4ba8-909d-230c0786d5f3 가리키는 연락처가 기기에 없는 연결은 저장되지만 조회에 나타나지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val missingContactId = Arb.kotlinUuid().next()
            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoContactList = emptyList(),
            )

            syncTransaction.upsert(
                accountId = accountId,
                memoContactList = listOf(memoContact(memoId = memo.id, contactId = missingContactId)),
                cursor = 1L,
            )

            database.memoContactDao().findByMemoIdList(listOf(memo.id)).size shouldBe 1
            dataSource
                .getContactList(accountId = accountId, memoId = memo.id)
                .contacts()
                .first()
                .shouldBeEmpty()
        }

        test("TC-f54e50c2-7078-4ba8-909d-230c0786d5f3 가리키는 메모가 기기에 없는 연결은 저장되지만 조회에 나타나지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val missingMemoId = Arb.kotlinUuid().next()
            val contact = contact()
            contactTransaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            syncTransaction.upsert(
                accountId = accountId,
                memoContactList = listOf(memoContact(memoId = missingMemoId, contactId = contact.id)),
                cursor = 1L,
            )

            database.memoContactDao().findByMemoIdList(listOf(missingMemoId)).size shouldBe 1
            dataSource
                .getContactList(accountId = accountId, memoId = missingMemoId)
                .contacts()
                .first()
                .shouldBeEmpty()
        }

        test("TC-912afd5f-c6c7-45c5-9193-c1c3099567ee 메모가 내려받아지면 먼저 저장되어 있던 연결의 연락처가 함께 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val contact = contact()
            contactTransaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())
            syncTransaction.upsert(
                accountId = accountId,
                memoContactList = listOf(memoContact(memoId = memo.id, contactId = contact.id)),
                cursor = 1L,
            )
            dataSource
                .getContactList(accountId = accountId, memoId = memo.id)
                .contacts()
                .first()
                .shouldBeEmpty()

            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoContactList = emptyList(),
            )

            dataSource.getContactList(accountId = accountId, memoId = memo.id).contacts().first() shouldBe listOf(contact)
        }

        test("다른 계정의 연락처는 연결이 있어도 조회되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val memo = memo()
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact()))

            dataSource
                .getContactList(accountId = otherAccountId, memoId = memo.id)
                .contacts()
                .first()
                .shouldBeEmpty()
        }

        test("TC-6481be89-d529-4a5b-b929-b76c5ca20617 계정과 연결되지 않은 연락처는 메모의 연결된 연락처로 조회되지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val memo = memo()
            val otherAccountContact = contact()
            contactTransaction.upsert(accountId = otherAccountId, contactList = listOf(otherAccountContact), contactTagList = emptyList())

            memoTransaction.upsert(
                accountId = accountId,
                memoList = listOf(memo),
                memoTagList = emptyList(),
                memoContactList = listOf(memoContact(memoId = memo.id, contactId = otherAccountContact.id)),
            )

            database.memoContactDao().findByMemoIdList(listOf(memo.id)).size shouldBe 1
            dataSource
                .getContactList(accountId = accountId, memoId = memo.id)
                .contacts()
                .first()
                .shouldBeEmpty()
        }

        test("TC-35df8459-e6e4-408f-843a-158ec8afed78 삭제된 연락처의 삭제가 다른 기기에서 받은 내용으로 풀리면 다시 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val contact = contact()
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))
            contactTransaction.upsert(accountId = accountId, contactList = listOf(contact.copy(isDeleted = true)), contactTagList = emptyList())
            dataSource
                .getContactList(accountId = accountId, memoId = memo.id)
                .contacts()
                .first()
                .shouldBeEmpty()

            AccountContactSyncTransactionImpl(database = database).upsert(
                accountId = accountId,
                contactList = listOf(contact.copy(isDeleted = false)),
                cursor = 1L,
            )

            dataSource.getContactList(accountId = accountId, memoId = memo.id).contacts().first() shouldBe listOf(contact)
        }

        test("TC-0f08e8cd-1ab3-4655-a5d4-5391a502ec87 선택할 수 있는 연락처는 계정과 연결된 삭제되지 않은 연락처다") {
            val accountId = Arb.kotlinUuid().next()
            val otherAccountId = Arb.kotlinUuid().next()
            val selectableContact = contact()
            val deletedContact = contact().copy(isDeleted = true)
            val otherAccountContact = contact()
            contactTransaction.upsert(accountId = accountId, contactList = listOf(selectableContact, deletedContact), contactTagList = emptyList())
            contactTransaction.upsert(accountId = otherAccountId, contactList = listOf(otherAccountContact), contactTagList = emptyList())

            loadSelectableContact(accountId = accountId) shouldBe listOf(selectableContact)
        }

        test("TC-769ad661-33e0-4e96-933f-49a4fd7f3651 선택할 수 있는 연락처는 이름 오름차순으로 정렬된다") {
            val accountId = Arb.kotlinUuid().next()
            val lastContact = contact(name = LAST_CONTACT_NAME)
            val firstContact = contact(name = FIRST_CONTACT_NAME)
            contactTransaction.upsert(accountId = accountId, contactList = listOf(lastContact, firstContact), contactTagList = emptyList())

            loadSelectableContact(accountId = accountId) shouldBe listOf(firstContact, lastContact)
        }

        test("TC-5a11ca4c-ab2d-4e86-8dc1-c00ebde17200 즐겨찾기는 선택 목록의 순서를 바꾸지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val lastFavoriteContact = contact(name = LAST_CONTACT_NAME).copy(isFavorite = true)
            val firstContact = contact(name = FIRST_CONTACT_NAME)
            contactTransaction.upsert(accountId = accountId, contactList = listOf(lastFavoriteContact, firstContact), contactTagList = emptyList())

            loadSelectableContact(accountId = accountId) shouldBe listOf(firstContact, lastFavoriteContact)
        }

        test("선택 목록은 수정 시각과 관계없이 이름 오름차순으로 조회된다") {
            val accountId = Arb.kotlinUuid().next()
            val lastContact = contact(name = LAST_CONTACT_NAME).copy(updatedAt = Instant.fromEpochMilliseconds(2_000_000_000))
            val firstContact = contact(name = FIRST_CONTACT_NAME).copy(updatedAt = Instant.fromEpochMilliseconds(1_000_000_000))
            contactTransaction.upsert(accountId = accountId, contactList = listOf(lastContact, firstContact), contactTagList = emptyList())

            loadSelectableContact(accountId = accountId) shouldBe listOf(firstContact, lastContact)
        }

        test("TC-b5ecf46f-f58c-4a23-84dc-4d37c860c9a6 선택했더라도 삭제된 연락처는 선택 목록에 나타나지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val keptContact = contact()
            val deletedContact = contact()
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(keptContact, deletedContact))

            contactTransaction.upsert(accountId = accountId, contactList = listOf(deletedContact.copy(isDeleted = true)), contactTagList = emptyList())

            loadSelectableContact(accountId = accountId) shouldBe listOf(keptContact)
        }

        test("TC-639ee47c-12bd-42c8-9049-82a4876ba114 TC-b6b7f72d-e74e-4337-98b9-a23e0921f9fb 검색어는 이름과 설명으로 판정한다") {
            val accountId = Arb.kotlinUuid().next()
            val nameMatchedContact = contact(name = "$SEARCH_QUERY-name")
            val descriptionMatchedContact =
                contact(name = FIRST_CONTACT_NAME).let { contact ->
                    contact.copy(detail = contact.detail.copy(description = SEARCH_QUERY))
                }
            val phoneNumberMatchedContact =
                contact(name = LAST_CONTACT_NAME).let { contact ->
                    contact.copy(detail = contact.detail.copy(phoneNumberList = listOf(ContactPhoneNumberLocalEntity(number = SEARCH_QUERY))))
                }
            val hometownMatchedContact =
                contact(name = HOMETOWN_CONTACT_NAME).let { contact ->
                    contact.copy(detail = contact.detail.copy(hometown = SEARCH_QUERY))
                }
            val howWeMetMatchedContact =
                contact(name = HOW_WE_MET_CONTACT_NAME).let { contact ->
                    contact.copy(detail = contact.detail.copy(howWeMet = SEARCH_QUERY))
                }
            contactTransaction.upsert(
                accountId = accountId,
                contactList = listOf(nameMatchedContact, descriptionMatchedContact, phoneNumberMatchedContact, hometownMatchedContact, howWeMetMatchedContact),
                contactTagList = emptyList(),
            )

            loadSelectableContact(accountId = accountId, query = SEARCH_QUERY) shouldBe listOf(descriptionMatchedContact, nameMatchedContact)
        }

        test("TC-5c6f6eca-3871-4a9b-9c6a-5ef289053cc4 빈 검색어는 선택 목록을 좁히지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val firstContact = contact(name = FIRST_CONTACT_NAME)
            val lastContact = contact(name = LAST_CONTACT_NAME)
            contactTransaction.upsert(accountId = accountId, contactList = listOf(firstContact, lastContact), contactTagList = emptyList())

            loadSelectableContact(accountId = accountId, query = "") shouldBe listOf(firstContact, lastContact)
        }

        test("TC-8f8caf09-14f9-47d9-abf5-21652601fbda 검색어는 선택한 연락처에도 같은 기준으로 적용된다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val matchedContact = contact(name = "$SEARCH_QUERY-name")
            val selectedContact = contact(name = FIRST_CONTACT_NAME)
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(matchedContact, selectedContact))

            loadSelectableContact(accountId = accountId, query = SEARCH_QUERY) shouldBe listOf(matchedContact)
            dataSource.getContactList(accountId = accountId, memoId = memo.id).contacts().first() shouldBe listOf(selectedContact, matchedContact)
        }

        test("TC-44b6a6a2-3074-4198-9e3e-672ad114ee5f 선택 목록 페이지 조회는 요청한 크기만큼만 가져오고 다음 페이지를 이어서 가져온다") {
            val accountId = Arb.kotlinUuid().next()
            val contactList =
                List(CONTACT_COUNT) { index ->
                    contact(name = "Contact-${index.toString().padStart(length = 3, padChar = '0')}")
                }.reversed()
            val expected = contactList.sortedBy { contact -> contact.detail.name }
            contactTransaction.upsert(accountId = accountId, contactList = contactList, contactTagList = emptyList())

            val firstPage = dataSource.pageSelectableContact(accountId = accountId, query = "").loadPage(loadSize = SMALL_PAGE_SIZE)

            firstPage.data.map { item -> item.contact } shouldBe expected.take(SMALL_PAGE_SIZE)
            firstPage.nextKey shouldBe SMALL_PAGE_SIZE

            val secondPage =
                dataSource
                    .pageSelectableContact(accountId = accountId, query = "")
                    .loadPage(key = firstPage.nextKey, loadSize = SMALL_PAGE_SIZE)

            secondPage.data.map { item -> item.contact } shouldBe expected.drop(SMALL_PAGE_SIZE).take(SMALL_PAGE_SIZE)
        }

        test("TC-fb167467-8ef1-4575-bb94-2d086a298a3d 연락처가 추가되면 열려 있는 선택 목록이 스스로 다시 조회되어 나타난다") {
            val accountId = Arb.kotlinUuid().next()
            val firstContact = contact(name = FIRST_CONTACT_NAME)
            contactTransaction.upsert(accountId = accountId, contactList = listOf(firstContact), contactTagList = emptyList())
            val addedContact = contact(name = LAST_CONTACT_NAME)

            awaitSelectableContactInvalidated(accountId = accountId) {
                contactTransaction.upsert(accountId = accountId, contactList = listOf(addedContact), contactTagList = emptyList())
            }

            loadSelectableContact(accountId = accountId) shouldBe listOf(firstContact, addedContact)
        }

        test("TC-ea5c5abd-977d-4f8f-98ab-cbb802d87138 TC-fb167467-8ef1-4575-bb94-2d086a298a3d 저장된 연락처의 이름이 바뀌면 연결된 연락처 조회와 열려 있는 선택 목록에 스스로 반영된다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val contact = contact(name = FIRST_CONTACT_NAME)
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))
            val renamedContact = contact.copy(detail = contact.detail.copy(name = LAST_CONTACT_NAME))

            dataSource.getContactList(accountId = accountId, memoId = memo.id).contacts().test {
                awaitItem() shouldBe listOf(contact)

                awaitSelectableContactInvalidated(accountId = accountId) {
                    contactTransaction.upsert(accountId = accountId, contactList = listOf(renamedContact), contactTagList = emptyList())
                }

                awaitItem() shouldBe listOf(renamedContact)
                cancelAndIgnoreRemainingEvents()
            }
            loadSelectableContact(accountId = accountId) shouldBe listOf(renamedContact)
        }

        test("TC-fb167467-8ef1-4575-bb94-2d086a298a3d 저장된 연락처의 전화번호가 바뀌면 연결된 연락처 조회와 열려 있는 선택 목록에 스스로 반영된다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val contact = contact(name = FIRST_CONTACT_NAME)
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))
            val phoneNumber = ContactPhoneNumberLocalEntity(number = "phone-${Arb.string().next()}")
            val changedContact = contact.copy(detail = contact.detail.copy(phoneNumberList = listOf(phoneNumber)))

            dataSource.getContactList(accountId = accountId, memoId = memo.id).contacts().test {
                awaitItem() shouldBe listOf(contact)

                awaitSelectableContactInvalidated(accountId = accountId) {
                    contactTransaction.upsert(accountId = accountId, contactList = listOf(changedContact), contactTagList = emptyList())
                }

                awaitItem() shouldBe listOf(changedContact)
                cancelAndIgnoreRemainingEvents()
            }
            loadSelectableContact(accountId = accountId) shouldBe listOf(changedContact)
        }

        test("TC-9a65e00a-646d-4c19-84d0-8de55beb0b49 저장된 연락처가 삭제되면 연결된 연락처 조회와 열려 있는 선택 목록에서 스스로 사라진다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val contact = contact(name = FIRST_CONTACT_NAME)
            val otherContact = contact(name = LAST_CONTACT_NAME)
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact, otherContact))

            dataSource.getContactList(accountId = accountId, memoId = memo.id).contacts().test {
                awaitItem() shouldBe listOf(contact, otherContact)

                awaitSelectableContactInvalidated(accountId = accountId) {
                    contactTransaction.upsert(accountId = accountId, contactList = listOf(contact.copy(isDeleted = true)), contactTagList = emptyList())
                }

                awaitItem() shouldBe listOf(otherContact)
                cancelAndIgnoreRemainingEvents()
            }
            loadSelectableContact(accountId = accountId) shouldBe listOf(otherContact)
        }

        test("TC-fa99f387-6862-4bbe-8294-bfce30918222 이름이 같은 연락처의 구분이 바뀌면 연결된 연락처 조회와 선택 목록에 스스로 반영된다") {
            val accountId = Arb.kotlinUuid().next()
            val memo = memo()
            val name = "name-${Arb.nonBlankString().next()}-name"
            val howWeMet = "how-${Arb.nonBlankString().next()}-met"
            val changedHowWeMet = "changed-$howWeMet"
            val contact = contact(name = name).let { value -> value.copy(detail = value.detail.copy(howWeMet = howWeMet)) }
            val sameNameContact = contact(name = name)
            insertMemoWithContactList(accountId = accountId, memo = memo, contactList = listOf(contact))

            suspend fun selectableQualifier(): String =
                (
                    dataSource
                        .pageSelectableContact(accountId = accountId, query = "")
                        .load(PagingSource.LoadParams.Refresh(key = null, loadSize = PAGE_SIZE, placeholdersEnabled = false)) as PagingSource.LoadResult.Page
                ).data
                    .single { item -> item.contact.id == contact.id }
                    .nameQualifier

            dataSource
                .getContactList(accountId = accountId, memoId = memo.id)
                .map { list -> list.single().nameQualifier }
                .distinctUntilChanged()
                .test {
                    awaitItem() shouldBe ""

                    awaitSelectableContactInvalidated(accountId = accountId) {
                        contactTransaction.upsert(accountId = accountId, contactList = listOf(sameNameContact), contactTagList = emptyList())
                    }
                    awaitItem() shouldBe howWeMet
                    selectableQualifier() shouldBe howWeMet

                    awaitSelectableContactInvalidated(accountId = accountId) {
                        contactTransaction.upsert(accountId = accountId, contactList = listOf(contact.copy(detail = contact.detail.copy(howWeMet = changedHowWeMet))), contactTagList = emptyList())
                    }
                    awaitItem() shouldBe changedHowWeMet
                    selectableQualifier() shouldBe changedHowWeMet

                    awaitSelectableContactInvalidated(accountId = accountId) {
                        contactTransaction.upsert(accountId = accountId, contactList = listOf(sameNameContact.copy(isDeleted = true)), contactTagList = emptyList())
                    }
                    awaitItem() shouldBe ""
                    selectableQualifier() shouldBe ""
                }
        }

        test("검색어의 대소문자는 판정에 영향을 주지 않는다") {
            val accountId = Arb.kotlinUuid().next()
            val contact = contact(name = "AndroidDeveloper")
            contactTransaction.upsert(accountId = accountId, contactList = listOf(contact), contactTagList = emptyList())

            loadSelectableContact(accountId = accountId, query = "androidDEVELOPER") shouldBe listOf(contact)
        }
    }) {
    public companion object {
        private const val FIRST_CONTACT_NAME = "AppleContact"
        private const val LAST_CONTACT_NAME = "ZebraContact"
        private const val HOMETOWN_CONTACT_NAME = "HometownContact"
        private const val HOW_WE_MET_CONTACT_NAME = "HowWeMetContact"
        private const val SEARCH_QUERY = "searchable"
        private const val INVALIDATION_TIMEOUT_MILLIS = 30_000L
        private const val PAGE_SIZE = 20
        private const val SMALL_PAGE_SIZE = 10
        private const val CONTACT_COUNT = 25

        private fun memo(): MemoLocalEntity =
            Arb
                .memoLocalEntity()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

        private fun contact(name: String = "name-${Arb.string().next()}"): ContactLocalEntity =
            Arb
                .contactLocalEntity()
                .next()
                .copy(
                    detail = contactDetail(name = name),
                    // 목록은 즐겨찾기를 앞세우므로, 이름 순서를 확인하는 연락처는 즐겨찾기 여부를 고정한다.
                    isFavorite = false,
                    isDeleted = false,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

        private fun contactDetail(name: String): ContactDetailLocalEntity =
            Arb
                .contactDetailLocalEntity()
                .next()
                .copy(
                    name = name,
                    phoneNumberList = emptyList<Nothing>(),
                )

        private fun memoContact(
            memoId: Uuid,
            contactId: Uuid,
        ): MemoContactLocalEntity =
            MemoContactLocalEntity(
                memoId = memoId,
                contactId = contactId,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
                createdAt = Arb.secondPrecisionInstant().next(),
            )

        private fun Flow<List<ContactListItemLocalEntity>>.contacts(): Flow<List<ContactLocalEntity>> = map { list -> list.map { item -> item.contact } }

        private suspend fun PagingSource<Int, ContactListItemLocalEntity>.loadPage(
            key: Int? = null,
            loadSize: Int = PAGE_SIZE,
        ): PagingSource.LoadResult.Page<Int, ContactListItemLocalEntity> {
            val params: PagingSource.LoadParams<Int> =
                if (key == null) {
                    PagingSource.LoadParams.Refresh(key = null, loadSize = loadSize, placeholdersEnabled = false)
                } else {
                    PagingSource.LoadParams.Append(key = key, loadSize = loadSize, placeholdersEnabled = false)
                }

            return load(params).shouldBeInstanceOf<PagingSource.LoadResult.Page<Int, ContactListItemLocalEntity>>()
        }
    }
}
