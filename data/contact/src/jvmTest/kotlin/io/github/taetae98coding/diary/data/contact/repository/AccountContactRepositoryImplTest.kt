package io.github.taetae98coding.diary.data.contact.repository

import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.datasource.AccountContactLocalDataSource
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.transaction.AccountContactTransaction
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactFilter
import io.github.taetae98coding.diary.core.model.contact.ContactFilterExistence
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.testing.contact.contactDetailCaseWithoutBirthdayCalendar
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountContactRepositoryImplTest :
    FunSpec({
        test("TC-7e927777-fb91-4305-91d6-2edf09cea62d 연락처를 현재 계정 식별자와 로컬 모델로 변환해 로컬 저장소에 저장한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contact = contact()
            val transaction = mockk<AccountContactTransaction>()
            coEvery { transaction.upsert(accountId = account.id, contactList = listOf(contact.toLocal())) } just Runs
            val repository = repository(transaction = transaction)

            repository.upsert(account = account, contact = contact)

            coVerify(exactly = 1) { transaction.upsert(accountId = account.id, contactList = listOf(contact.toLocal())) }
        }

        test("TC-2395f8c2-2d07-437e-883f-9baa369e0049 전화번호는 배치한 순서 그대로 로컬 모델로 변환된다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val phoneNumberList =
                listOf(
                    ContactPhoneNumber(number = "010-1234-5678"),
                    ContactPhoneNumber(number = "02-987-6543"),
                    ContactPhoneNumber(number = "010-1234-5678"),
                )
            val contact = contact().let { contact -> contact.copy(detail = contact.detail.copy(phoneNumberList = phoneNumberList)) }
            val transaction = mockk<AccountContactTransaction>()
            coEvery { transaction.upsert(accountId = any(), contactList = any()) } just Runs
            val repository = repository(transaction = transaction)

            repository.upsert(account = account, contact = contact)

            coVerify(exactly = 1) { transaction.upsert(accountId = account.id, contactList = listOf(contact.toLocal())) }
        }

        test("TC-6d5440d1-b27b-4ea5-b95a-2af5219ae2cb 연락처 목록 페이지 조회는 현재 계정의 로컬 연락처를 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localContactList = List(2) { localContact() }
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.page(accountId = account.id, sort = ListSortLocalEntity.NAME, hasBirthday = null, hasBirthYear = null, hasPhoneNumber = null) } returns pagingSource(localContactList)
            val repository = repository(localDataSource = localDataSource)

            repository.page(account = account, sort = ListSort.NAME, filter = ContactFilter()).first().items() shouldBe
                localContactList.map { local -> local.toDomain() }
        }

        test("TC-d0fa641b-b467-4064-8b76-f5cca4bc6d01 전화번호는 저장한 순서 그대로 함께 조회된다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val phoneNumberList =
                listOf(
                    ContactPhoneNumber(number = "010-1234-5678"),
                    ContactPhoneNumber(number = "02-987-6543"),
                    ContactPhoneNumber(number = "010-1234-5678"),
                )
            val stored = contact().let { value -> value.copy(detail = value.detail.copy(phoneNumberList = phoneNumberList)) }
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.page(accountId = account.id, sort = ListSortLocalEntity.NAME, hasBirthday = null, hasBirthYear = null, hasPhoneNumber = null) } returns
                pagingSource(listOf(stored.toLocal()))
            val repository = repository(localDataSource = localDataSource)

            repository
                .page(account = account, sort = ListSort.NAME, filter = ContactFilter())
                .first()
                .items()
                .single()
                .detail.phoneNumberList shouldBe phoneNumberList
        }

        test("TC-2b544859-ade8-4e3d-9fa1-8e260fc0a5b2 선택한 정렬을 로컬 조회 조건으로 넘긴다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.page(accountId = account.id, sort = any(), hasBirthday = any(), hasBirthYear = any(), hasPhoneNumber = any()) } returns pagingSource(emptyList())
            val repository = repository(localDataSource = localDataSource)

            repository.page(account = account, sort = ListSort.RECENTLY_UPDATED, filter = ContactFilter()).first().items()

            coVerify(exactly = 1) {
                localDataSource.page(accountId = account.id, sort = ListSortLocalEntity.RECENTLY_UPDATED, hasBirthday = null, hasBirthYear = null, hasPhoneNumber = null)
            }
        }

        test("각 정렬은 같은 로컬 조회 조건으로 전달된다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.page(accountId = account.id, sort = any(), hasBirthday = any(), hasBirthYear = any(), hasPhoneNumber = any()) } returns pagingSource(emptyList())
            val repository = repository(localDataSource = localDataSource)

            ListSort.entries.forEach { sort ->
                repository.page(account = account, sort = sort, filter = ContactFilter()).first().items()

                coVerify(exactly = 1) { localDataSource.page(accountId = account.id, sort = sort.toLocal(), hasBirthday = null, hasBirthYear = null, hasPhoneNumber = null) }
            }
        }

        test("TC-b1b5fded-b545-4d4c-ad99-259ee2f08e9e 유무 필터의 각 축을 로컬 조회 조건으로 넘긴다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.page(accountId = account.id, sort = any(), hasBirthday = any(), hasBirthYear = any(), hasPhoneNumber = any()) } returns pagingSource(emptyList())
            val repository = repository(localDataSource = localDataSource)
            val expectedList =
                listOf(
                    ContactFilterExistence.ALL to null,
                    ContactFilterExistence.EXIST to true,
                    ContactFilterExistence.NOT_EXIST to false,
                )

            expectedList.forEach { (birthday, hasBirthday) ->
                expectedList.forEach { (birthYear, hasBirthYear) ->
                    expectedList.forEach { (phoneNumber, hasPhoneNumber) ->
                        repository
                            .page(account = account, sort = ListSort.NAME, filter = ContactFilter(birthday = birthday, birthYear = birthYear, phoneNumber = phoneNumber))
                            .first()
                            .items()

                        coVerify(exactly = 1) {
                            localDataSource.page(
                                accountId = account.id,
                                sort = ListSortLocalEntity.NAME,
                                hasBirthday = hasBirthday,
                                hasBirthYear = hasBirthYear,
                                hasPhoneNumber = hasPhoneNumber,
                            )
                        }
                    }
                }
            }
        }

        test("TC-3fcc976b-4672-46ba-916e-8fbff0c3ad10 대상 연락처 조회는 로컬 저장소만 사용한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val local = localContact()
            val localDataSource = mockk<AccountContactLocalDataSource>()
            val transaction = mockk<AccountContactTransaction>()
            every { localDataSource.find(accountId = account.id, contactId = local.id) } returns flowOf(local)
            val repository = repository(localDataSource = localDataSource, transaction = transaction)

            repository.find(account = account, contactId = local.id).first()

            coVerify(exactly = 0) { transaction.upsert(accountId = any(), contactList = any()) }
        }

        test("TC-1c371ce2-5734-4028-b428-301bc197ab5a 전화번호를 모두 지운 수정을 반영하면 전화번호가 하나도 남지 않는다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val detail = ContactDetail.EMPTY.copy(name = "name-${fixtureMonkey.giveMeOne<String>()}", phoneNumberList = emptyList())
            val updatedAt = fixtureMonkey.giveMeOne<Instant>()
            val transaction = mockk<AccountContactTransaction>()
            coEvery { transaction.updateDetail(accountId = any(), contactId = any(), detail = any(), updatedAt = any()) } returns 1
            val repository = repository(transaction = transaction)

            repository.updateDetail(account = account, contactId = contactId, detail = detail, updatedAt = updatedAt)

            coVerify(exactly = 1) {
                transaction.updateDetail(
                    accountId = account.id,
                    contactId = contactId,
                    detail = match { local -> local.phoneNumberList.isEmpty() },
                    updatedAt = updatedAt,
                )
            }
        }

        test("TC-250658dc-32f8-47a3-8410-3e244df01331 대상 연락처 조회는 로컬 연락처를 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val local = localContact()
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.find(accountId = account.id, contactId = local.id) } returns flowOf(local)
            val repository = repository(localDataSource = localDataSource)

            repository.find(account = account, contactId = local.id).first() shouldBe local.toDomain()
        }

        test("TC-1bb425bf-d53e-490c-ae76-aa9186a9f0de 달력 구분 없이 날짜만 저장된 생일은 양력 생일로 조회한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val case = fixtureMonkey.contactDetailCaseWithoutBirthdayCalendar()
            val local = localContact().copy(detail = case.local)
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.find(accountId = account.id, contactId = local.id) } returns flowOf(local)
            val repository = repository(localDataSource = localDataSource)

            val birthday =
                repository
                    .find(account = account, contactId = local.id)
                    .first()
                    ?.detail
                    ?.birthday

            birthday shouldBe ContactBirthday(date = checkNotNull(case.local.birthday), calendar = ContactBirthdayCalendar.SOLAR)
        }

        test("TC-25a7d438-e5bf-4d56-9541-59bf526b010e 생일과 다른 태어난 연도가 저장되어 있으면 생일의 연도로 조회한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val stored = localContact()
            val birthday = fixtureMonkey.giveMeOne<LocalDate>()
            val local =
                stored.copy(
                    detail = stored.detail.copy(birthday = birthday, birthdayCalendar = ContactBirthdayCalendarLocalEntity.SOLAR, birthYear = birthday.year + 1),
                )
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.find(accountId = account.id, contactId = local.id) } returns flowOf(local)
            val repository = repository(localDataSource = localDataSource)

            val detail =
                repository
                    .find(account = account, contactId = local.id)
                    .first()
                    ?.detail

            detail?.birthday shouldBe ContactBirthday(date = birthday, calendar = ContactBirthdayCalendar.SOLAR)
            detail?.birthYear shouldBe birthday.year
        }

        test("TC-229c2151-ee96-48d8-b3fc-ba0dd5bf1723 조회되는 로컬 연락처가 없으면 없음을 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.find(accountId = account.id, contactId = contactId) } returns flowOf(null)
            val repository = repository(localDataSource = localDataSource)

            repository.find(account = account, contactId = contactId).first().shouldBeNull()
        }

        test("TC-6ec3f675-b8e4-4edc-8e7f-9af3c7691cfa 수정은 계정 식별자와 로컬 모델로 변환해 트랜잭션에 위임한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val detail =
                ContactDetail.EMPTY.copy(
                    name = "name-${fixtureMonkey.giveMeOne<String>()}",
                    phoneNumberList = listOf(ContactPhoneNumber(number = "010-1234-5678"), ContactPhoneNumber(number = "02-1-2")),
                )
            val updatedAt = fixtureMonkey.giveMeOne<Instant>()
            val transaction = mockk<AccountContactTransaction>()
            coEvery {
                transaction.updateDetail(accountId = account.id, contactId = contactId, detail = detail.toLocal(), updatedAt = updatedAt)
            } returns 1
            val repository = repository(transaction = transaction)

            repository.updateDetail(account = account, contactId = contactId, detail = detail, updatedAt = updatedAt) shouldBe 1

            coVerify(exactly = 1) {
                transaction.updateDetail(accountId = account.id, contactId = contactId, detail = detail.toLocal(), updatedAt = updatedAt)
            }
        }

        test("TC-92a80131-b9a8-457a-93e9-5aef7d57aea0 대상 식별자와 계정을 만족하지 않으면 수정이 아무것도 바꾸지 않는다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val transaction = mockk<AccountContactTransaction>()
            coEvery { transaction.updateDetail(accountId = any(), contactId = any(), detail = any(), updatedAt = any()) } returns 0
            val repository = repository(transaction = transaction)

            repository.updateDetail(
                account = account,
                contactId = contactId,
                detail = ContactDetail.EMPTY,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
            ) shouldBe 0
        }

        test("TC-21d1c705-308e-4ddc-bb00-1dcffba5d805 즐겨찾기 변경은 계정 식별자와 즐겨찾기 여부를 트랜잭션에 위임한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val updatedAt = fixtureMonkey.giveMeOne<Instant>()
            val transaction = mockk<AccountContactTransaction>()
            listOf(true, false).forEach { isFavorite ->
                coEvery {
                    transaction.updateFavorite(accountId = account.id, contactId = contactId, isFavorite = isFavorite, updatedAt = updatedAt)
                } returns 1
                val repository = repository(transaction = transaction)

                repository.updateFavorite(account = account, contactId = contactId, isFavorite = isFavorite, updatedAt = updatedAt) shouldBe 1

                coVerify(exactly = 1) {
                    transaction.updateFavorite(accountId = account.id, contactId = contactId, isFavorite = isFavorite, updatedAt = updatedAt)
                }
            }
        }

        test("TC-11b8fded-b993-4a0c-8334-0da88e444e17 대상 식별자와 계정을 만족하지 않으면 즐겨찾기 변경이 아무것도 바꾸지 않는다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val transaction = mockk<AccountContactTransaction>()
            coEvery { transaction.updateFavorite(accountId = any(), contactId = any(), isFavorite = any(), updatedAt = any()) } returns 0
            val repository = repository(transaction = transaction)

            repository.updateFavorite(
                account = account,
                contactId = fixtureMonkey.giveMeOne<Uuid>(),
                isFavorite = true,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
            ) shouldBe 0
        }

        test("TC-e5d727a2-80a2-4f39-bc0f-a1295f84684d 즐겨찾기 변경의 로컬 저장이 실패하면 실패를 그대로 전파한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val transaction = mockk<AccountContactTransaction>()
            coEvery { transaction.updateFavorite(accountId = any(), contactId = any(), isFavorite = any(), updatedAt = any()) } throws throwable
            val repository = repository(transaction = transaction)

            shouldThrow<IllegalStateException> {
                repository.updateFavorite(
                    account = account,
                    contactId = fixtureMonkey.giveMeOne<Uuid>(),
                    isFavorite = true,
                    updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                )
            } shouldBe throwable
        }

        test("TC-9aaa0ba4-69e2-4de4-a225-3e64e1f24b30 삭제는 계정 식별자와 삭제 여부를 트랜잭션에 위임한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val updatedAt = fixtureMonkey.giveMeOne<Instant>()
            val transaction = mockk<AccountContactTransaction>()
            coEvery {
                transaction.updateDeleted(accountId = account.id, contactId = contactId, isDeleted = true, updatedAt = updatedAt)
            } returns 1
            val repository = repository(transaction = transaction)

            repository.updateDeleted(account = account, contactId = contactId, isDeleted = true, updatedAt = updatedAt) shouldBe 1

            coVerify(exactly = 1) {
                transaction.updateDeleted(accountId = account.id, contactId = contactId, isDeleted = true, updatedAt = updatedAt)
            }
        }

        test("TC-7ff50880-b5cd-4f40-bde3-44fe777b0439 대상 식별자와 계정을 만족하지 않으면 삭제가 아무것도 바꾸지 않는다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val transaction = mockk<AccountContactTransaction>()
            coEvery { transaction.updateDeleted(accountId = any(), contactId = any(), isDeleted = any(), updatedAt = any()) } returns 0
            val repository = repository(transaction = transaction)

            repository.updateDeleted(
                account = account,
                contactId = fixtureMonkey.giveMeOne<Uuid>(),
                isDeleted = true,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
            ) shouldBe 0
        }

        test("TC-1baea3d4-f655-46b8-8d62-64d922ba1d95 수정과 삭제의 로컬 저장이 실패하면 실패를 그대로 전파한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val transaction = mockk<AccountContactTransaction>()
            coEvery { transaction.updateDetail(accountId = any(), contactId = any(), detail = any(), updatedAt = any()) } throws throwable
            coEvery {
                transaction.updateDeleted(accountId = any(), contactId = any(), isDeleted = any(), updatedAt = any())
            } throws throwable
            val repository = repository(transaction = transaction)
            val updatedAt = fixtureMonkey.giveMeOne<Instant>()

            shouldThrow<IllegalStateException> {
                repository.updateDetail(
                    account = account,
                    contactId = fixtureMonkey.giveMeOne<Uuid>(),
                    detail = ContactDetail.EMPTY,
                    updatedAt = updatedAt,
                )
            } shouldBeSameInstanceAs throwable

            shouldThrow<IllegalStateException> {
                repository.updateDeleted(
                    account = account,
                    contactId = fixtureMonkey.giveMeOne<Uuid>(),
                    isDeleted = true,
                    updatedAt = updatedAt,
                )
            } shouldBeSameInstanceAs throwable
        }

        test("TC-05a1f42a-bfb1-4a3d-b30b-7594415bdedf 기기 저장이 실패하면 추가를 성공으로 다루지 않고 실패를 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contact = contact()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val transaction = mockk<AccountContactTransaction>()
            coEvery { transaction.upsert(accountId = account.id, contactList = listOf(contact.toLocal())) } throws throwable
            val repository = repository(transaction = transaction)

            shouldThrow<IllegalStateException> {
                repository.upsert(account = account, contact = contact)
            } shouldBeSameInstanceAs throwable
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun repository(
            localDataSource: AccountContactLocalDataSource = mockk(),
            transaction: AccountContactTransaction = mockk(),
        ): AccountContactRepositoryImpl =
            AccountContactRepositoryImpl(
                accountContactLocalDataSource = localDataSource,
                accountContactTransaction = transaction,
            )

        private fun pagingSource(contactList: List<ContactLocalEntity>): PagingSource<Int, ContactLocalEntity> =
            mockk(relaxed = true) {
                coEvery { load(any()) } returns
                    PagingSource.LoadResult.Page(
                        data = contactList,
                        prevKey = null,
                        nextKey = null,
                    )
            }

        private suspend fun <T : Any> PagingData<T>.items(): List<T> =
            coroutineScope {
                val presenter =
                    object : PagingDataPresenter<T>(mainContext = coroutineContext) {
                        override suspend fun presentPagingDataEvent(event: PagingDataEvent<T>) = Unit
                    }
                val collection = launch { presenter.collectFrom(this@items) }

                presenter.loadStateFlow
                    .filterNotNull()
                    .first { loadStates -> loadStates.refresh is LoadState.NotLoading }
                val result = presenter.snapshot().items

                collection.cancelAndJoin()
                result
            }

        private fun localContact(): ContactLocalEntity = contact().toLocal()

        private fun contact(): Contact =
            Contact(
                id = fixtureMonkey.giveMeOne<Uuid>(),
                detail = ContactDetail.EMPTY.copy(name = "name-${fixtureMonkey.giveMeOne<String>()}"),
                isFavorite = false,
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                createdAt = fixtureMonkey.giveMeOne<Instant>(),
            )
    }
}
