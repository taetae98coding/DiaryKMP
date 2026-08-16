package io.github.taetae98coding.diary.data.contact.repository

import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.datasource.AccountContactLocalDataSource
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.transaction.AccountContactTransaction
import io.github.taetae98coding.diary.core.database.api.list.entity.ListSortLocalEntity
import io.github.taetae98coding.diary.core.mapper.contact.toDomain
import io.github.taetae98coding.diary.core.mapper.contact.toLocal
import io.github.taetae98coding.diary.core.mapper.list.toLocal
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.list.ListSort
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
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AccountContactRepositoryImplTest :
    FunSpec({
        test("TC-CONTACT-ADD-DATA-001 연락처를 현재 계정 식별자와 로컬 모델로 변환해 로컬 저장소에 저장한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contact = contact()
            val transaction = mockk<AccountContactTransaction>()
            coEvery { transaction.upsert(accountId = account.id, contactList = listOf(contact.toLocal())) } just Runs
            val repository = repository(transaction = transaction)

            repository.upsert(account = account, contact = contact)

            coVerify(exactly = 1) { transaction.upsert(accountId = account.id, contactList = listOf(contact.toLocal())) }
        }

        test("TC-CONTACT-ADD-DATA-002 전화번호는 배치한 순서 그대로 로컬 모델로 변환된다") {
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

        test("TC-CONTACT-HOME-DATA-001 연락처 목록 페이지 조회는 현재 계정의 로컬 연락처를 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localContactList = List(2) { localContact() }
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.page(accountId = account.id, sort = ListSortLocalEntity.NAME) } returns pagingSource(localContactList)
            val repository = repository(localDataSource = localDataSource)

            repository.page(account = account, sort = ListSort.NAME).first().items() shouldBe
                localContactList.map { local -> local.toDomain() }
        }

        test("TC-CONTACT-HOME-DATA-002 전화번호는 저장한 순서 그대로 함께 조회된다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val phoneNumberList =
                listOf(
                    ContactPhoneNumber(number = "010-1234-5678"),
                    ContactPhoneNumber(number = "02-987-6543"),
                    ContactPhoneNumber(number = "010-1234-5678"),
                )
            val stored = contact().let { value -> value.copy(detail = value.detail.copy(phoneNumberList = phoneNumberList)) }
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.page(accountId = account.id, sort = ListSortLocalEntity.NAME) } returns
                pagingSource(listOf(stored.toLocal()))
            val repository = repository(localDataSource = localDataSource)

            repository
                .page(account = account, sort = ListSort.NAME)
                .first()
                .items()
                .single()
                .detail.phoneNumberList shouldBe phoneNumberList
        }

        test("TC-CONTACT-HOME-DATA-005 선택한 정렬을 로컬 조회 조건으로 넘긴다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.page(accountId = account.id, sort = any()) } returns pagingSource(emptyList())
            val repository = repository(localDataSource = localDataSource)

            repository.page(account = account, sort = ListSort.RECENTLY_UPDATED).first().items()

            coVerify(exactly = 1) {
                localDataSource.page(accountId = account.id, sort = ListSortLocalEntity.RECENTLY_UPDATED)
            }
        }

        test("TC-CONTACT-HOME-DOMAIN-005 최근 수정순은 로컬 조회의 최근 수정순 조건으로 전달된다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.page(accountId = account.id, sort = any()) } returns pagingSource(emptyList())
            val repository = repository(localDataSource = localDataSource)

            ListSort.entries.forEach { sort ->
                repository.page(account = account, sort = sort).first().items()

                coVerify(exactly = 1) { localDataSource.page(accountId = account.id, sort = sort.toLocal()) }
            }
        }

        test("TC-CONTACT-DETAIL-DATA-003 대상 연락처 조회는 로컬 저장소만 사용한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val local = localContact()
            val localDataSource = mockk<AccountContactLocalDataSource>()
            val transaction = mockk<AccountContactTransaction>()
            every { localDataSource.find(accountId = account.id, contactId = local.id) } returns flowOf(local)
            val repository = repository(localDataSource = localDataSource, transaction = transaction)

            repository.find(account = account, contactId = local.id).first()

            coVerify(exactly = 0) { transaction.upsert(accountId = any(), contactList = any()) }
        }

        test("TC-CONTACT-DETAIL-DATA-005 전화번호를 모두 지운 수정을 반영하면 전화번호가 하나도 남지 않는다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val detail = ContactDetail.EMPTY.copy(name = "name-${fixtureMonkey.giveMeOne<String>()}", phoneNumberList = emptyList())
            val updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
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

        test("TC-CONTACT-DETAIL-DATA-001 대상 연락처 조회는 로컬 연락처를 도메인 모델로 변환해 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val local = localContact()
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.find(accountId = account.id, contactId = local.id) } returns flowOf(local)
            val repository = repository(localDataSource = localDataSource)

            repository.find(account = account, contactId = local.id).first() shouldBe local.toDomain()
        }

        test("TC-CONTACT-DETAIL-DATA-002 조회되는 로컬 연락처가 없으면 없음을 전달한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val localDataSource = mockk<AccountContactLocalDataSource>()
            every { localDataSource.find(accountId = account.id, contactId = contactId) } returns flowOf(null)
            val repository = repository(localDataSource = localDataSource)

            repository.find(account = account, contactId = contactId).first().shouldBeNull()
        }

        test("TC-CONTACT-DETAIL-DATA-004 수정은 계정 식별자와 로컬 모델로 변환해 트랜잭션에 위임한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val detail =
                ContactDetail.EMPTY.copy(
                    name = "name-${fixtureMonkey.giveMeOne<String>()}",
                    phoneNumberList = listOf(ContactPhoneNumber(number = "010-1234-5678"), ContactPhoneNumber(number = "02-1-2")),
                )
            val updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
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

        test("TC-CONTACT-DETAIL-DATA-006 대상 식별자와 계정을 만족하지 않으면 수정이 아무것도 바꾸지 않는다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val transaction = mockk<AccountContactTransaction>()
            coEvery { transaction.updateDetail(accountId = any(), contactId = any(), detail = any(), updatedAt = any()) } returns 0
            val repository = repository(transaction = transaction)

            repository.updateDetail(
                account = account,
                contactId = contactId,
                detail = ContactDetail.EMPTY,
                updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            ) shouldBe 0
        }

        test("TC-CONTACT-DETAIL-DATA-007 삭제는 계정 식별자와 삭제 여부를 트랜잭션에 위임한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
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

        test("TC-CONTACT-DETAIL-DATA-008 대상 식별자와 계정을 만족하지 않으면 삭제가 아무것도 바꾸지 않는다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val transaction = mockk<AccountContactTransaction>()
            coEvery { transaction.updateDeleted(accountId = any(), contactId = any(), isDeleted = any(), updatedAt = any()) } returns 0
            val repository = repository(transaction = transaction)

            repository.updateDeleted(
                account = account,
                contactId = fixtureMonkey.giveMeOne<Uuid>(),
                isDeleted = true,
                updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            ) shouldBe 0
        }

        test("TC-CONTACT-DETAIL-DATA-009 수정과 삭제의 로컬 저장이 실패하면 실패를 그대로 전파한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val transaction = mockk<AccountContactTransaction>()
            coEvery { transaction.updateDetail(accountId = any(), contactId = any(), detail = any(), updatedAt = any()) } throws throwable
            coEvery {
                transaction.updateDeleted(accountId = any(), contactId = any(), isDeleted = any(), updatedAt = any())
            } throws throwable
            val repository = repository(transaction = transaction)
            val updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

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

        test("TC-CONTACT-ADD-DATA-003 로컬 저장이 실패하면 실패를 그대로 전파한다") {
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
                isDeleted = false,
                updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            )
    }
}
