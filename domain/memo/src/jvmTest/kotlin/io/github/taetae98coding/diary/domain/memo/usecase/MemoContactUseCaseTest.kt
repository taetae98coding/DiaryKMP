package io.github.taetae98coding.diary.domain.memo.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoContactRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoContactUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 현재 시각이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val getAccountUseCase = accountUseCase(account = account)
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>(relaxed = true)
            val requestSyncUseCase = mockk<RequestSyncUseCase>(relaxed = true)
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            When("연락처를 추가한다") {
                Then("TC-MEMO-DETAIL-DATA-032 메모와 그 연락처의 연결을 해제되지 않은 상태와 선택 시점으로 저장한다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val contactId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        AddMemoContactUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoContactRepository = accountMemoContactRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddMemoContactUseCase.Parameter(memoId = memoId, contactId = contactId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountMemoContactRepository.upsert(
                            account = account,
                            memoId = memoId,
                            contactId = contactId,
                            isDeleted = false,
                            updatedAt = now,
                        )
                    }
                }

                Then("TC-MEMO-DETAIL-DATA-035 TC-MEMO-CONTACT-DATA-005 연결 변경을 서버와 맞추기 위한 동기화를 요청한다") {
                    val useCase =
                        AddMemoContactUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoContactRepository = accountMemoContactRepository,
                            clock = clock,
                        )

                    val result =
                        useCase(
                            parameter =
                                AddMemoContactUseCase.Parameter(
                                    memoId = fixtureMonkey.giveMeOne<Uuid>(),
                                    contactId = fixtureMonkey.giveMeOne<Uuid>(),
                                ),
                        )

                    result.shouldBeSuccess()
                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }

            When("연락처를 해제한다") {
                Then("TC-MEMO-DETAIL-DATA-033 메모와 그 연락처의 연결을 해제 상태와 해제 시점으로 저장한다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val contactId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        RemoveMemoContactUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoContactRepository = accountMemoContactRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = RemoveMemoContactUseCase.Parameter(memoId = memoId, contactId = contactId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountMemoContactRepository.upsert(
                            account = account,
                            memoId = memoId,
                            contactId = contactId,
                            isDeleted = true,
                            updatedAt = now,
                        )
                    }
                }
            }
        }

        Given("동기화 요청이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = accountUseCase(account = account)
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.failure(throwable)
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>(relaxed = true)
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            When("연락처를 추가한다") {
                Then("TC-MEMO-DETAIL-DATA-036 저장한 연락처 연결을 되돌리지 않는다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val contactId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        AddMemoContactUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoContactRepository = accountMemoContactRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddMemoContactUseCase.Parameter(memoId = memoId, contactId = contactId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountMemoContactRepository.upsert(account = account, memoId = memoId, contactId = contactId, isDeleted = false, updatedAt = now)
                    }
                    coVerify(exactly = 0) {
                        accountMemoContactRepository.upsert(account = account, memoId = memoId, contactId = contactId, isDeleted = true, updatedAt = any())
                    }
                }
            }

            When("연락처를 해제한다") {
                Then("TC-MEMO-DETAIL-DATA-036 저장한 연락처 연결 해제를 되돌리지 않는다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val contactId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        RemoveMemoContactUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoContactRepository = accountMemoContactRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = RemoveMemoContactUseCase.Parameter(memoId = memoId, contactId = contactId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountMemoContactRepository.upsert(account = account, memoId = memoId, contactId = contactId, isDeleted = true, updatedAt = now)
                    }
                    coVerify(exactly = 0) {
                        accountMemoContactRepository.upsert(account = account, memoId = memoId, contactId = contactId, isDeleted = false, updatedAt = any())
                    }
                }
            }
        }

        Given("저장이 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>()
            coEvery {
                accountMemoContactRepository.upsert(
                    account = any(),
                    memoId = any(),
                    contactId = any(),
                    isDeleted = any(),
                    updatedAt = any(),
                )
            } throws throwable
            val requestSyncUseCase = mockk<RequestSyncUseCase>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                AddMemoContactUseCase(
                    getAccountUseCase = accountUseCase(),
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoContactRepository = accountMemoContactRepository,
                    clock = clock,
                )

            When("연락처를 추가한다") {
                Then("실패를 그대로 전달한다") {
                    val result =
                        useCase(
                            parameter =
                                AddMemoContactUseCase.Parameter(
                                    memoId = fixtureMonkey.giveMeOne<Uuid>(),
                                    contactId = fixtureMonkey.giveMeOne<Uuid>(),
                                ),
                        )

                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("로그인한 계정과 메모에 연결된 연락처가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val contactList = List(2) { contact() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>()
            every { accountMemoContactRepository.getContactList(account = account, memoId = memoId) } returns flowOf(contactList)
            val useCase =
                GetMemoContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoContactRepository = accountMemoContactRepository,
                )

            When("메모의 연락처를 조회한다") {
                Then("현재 계정의 메모에 연결된 연락처를 전달한다") {
                    useCase(parameter = memoId).first().shouldBeSuccess(contactList)
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>(relaxed = true)
            val useCase =
                GetMemoContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoContactRepository = accountMemoContactRepository,
                )

            When("메모의 연락처를 조회한다") {
                Then("실패를 그대로 전달한다") {
                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun contact(): Contact =
            fixtureMonkey
                .giveMeKotlinBuilder<Contact>()
                .setExp(Contact::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Contact::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun accountUseCase(account: Account = fixtureMonkey.giveMeOne<Account.User>()): GetAccountUseCase {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))

            return getAccountUseCase
        }
    }
}
