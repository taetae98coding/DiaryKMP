package io.github.taetae98coding.diary.domain.contact.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class DeleteContactUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 저장된 연락처가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactId = Uuid.random()
            val isDeletedSlot = slot<Boolean>()
            val updatedAtSlot = slot<Instant>()
            val accountContactRepository = mockk<AccountContactRepository>()
            coEvery {
                accountContactRepository.updateDeleted(
                    account = account,
                    contactId = contactId,
                    isDeleted = capture(isDeletedSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val now = instant()
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase(account = account),
                    requestSyncUseCase = requestSyncUseCase,
                    accountContactRepository = accountContactRepository,
                    now = now,
                )

            When("연락처를 삭제한다") {
                Then("TC-CONTACT-DETAIL-DOMAIN-010 삭제 여부와 삭제 시점을 반영한다") {
                    useCase(parameter = contactId).shouldBeSuccess(1)

                    isDeletedSlot.captured shouldBe true
                    updatedAtSlot.captured shouldBe now
                }

                Then("TC-CONTACT-DETAIL-DATA-010 로컬 저장 결과로 성공을 판단하고 동기화를 요청한다") {
                    useCase(parameter = contactId).shouldBeSuccess(1)

                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("게스트 계정이 준비되어 있다") {
            val contactId = Uuid.random()
            val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase(account = Account.Guest),
                    requestSyncUseCase = requestSyncUseCase(),
                    accountContactRepository = accountContactRepository,
                )

            When("연락처를 삭제한다") {
                Then("TC-CONTACT-DETAIL-DATA-011 게스트 계정 기준으로 기기에만 삭제를 반영한다") {
                    useCase(parameter = contactId).shouldBeSuccess()

                    coVerify(exactly = 1) {
                        accountContactRepository.updateDeleted(
                            account = Account.Guest,
                            contactId = contactId,
                            isDeleted = true,
                            updatedAt = any(),
                        )
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountContactRepository = accountContactRepository,
                )

            When("연락처를 삭제한다") {
                Then("계정 조회 실패를 전달하고 삭제를 저장하지 않는다") {
                    useCase(parameter = Uuid.random())
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) {
                        accountContactRepository.updateDeleted(
                            account = any(),
                            contactId = any(),
                            isDeleted = any(),
                            updatedAt = any(),
                        )
                    }
                }
            }
        }

        Given("삭제 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val accountContactRepository = mockk<AccountContactRepository>()
            coEvery {
                accountContactRepository.updateDeleted(account = account, contactId = any(), isDeleted = any(), updatedAt = any())
            } throws throwable
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                useCase(
                    getAccountUseCase = getAccountUseCase(account = account),
                    requestSyncUseCase = requestSyncUseCase,
                    accountContactRepository = accountContactRepository,
                )

            When("연락처를 삭제한다") {
                Then("TC-CONTACT-DETAIL-DATA-009 실패를 그대로 전달하고 동기화를 요청하지 않는다") {
                    useCase(parameter = Uuid.random())
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun useCase(
            getAccountUseCase: GetAccountUseCase,
            requestSyncUseCase: RequestSyncUseCase,
            accountContactRepository: AccountContactRepository,
            now: Instant? = null,
        ): DeleteContactUseCase {
            val clock =
                if (now == null) {
                    Clock.System
                } else {
                    mockk<Clock>().also { clock -> every { clock.now() } returns now }
                }

            return DeleteContactUseCase(
                getAccountUseCase = getAccountUseCase,
                requestSyncUseCase = requestSyncUseCase,
                accountContactRepository = accountContactRepository,
                clock = clock,
            )
        }

        private fun getAccountUseCase(account: Account): GetAccountUseCase {
            val useCase = mockk<GetAccountUseCase>()
            every { useCase(parameter = Unit) } returns flowOf(Result.success(account))

            return useCase
        }

        private fun requestSyncUseCase(): RequestSyncUseCase {
            val useCase = mockk<RequestSyncUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)

            return useCase
        }

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
