package io.github.taetae98coding.diary.domain.web.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.web.repository.AccountWebRepository
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

class DeleteWebUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 저장된 웹 항목이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val id = Uuid.random()
            val isDeletedSlot = slot<Boolean>()
            val updatedAtSlot = slot<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountWebRepository = mockk<AccountWebRepository>()
            coEvery {
                accountWebRepository.updateDeleted(
                    account = account,
                    webId = id,
                    isDeleted = capture(isDeletedSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val now = fixtureMonkey.giveMeOne<Instant>()
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                DeleteWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountWebRepository = accountWebRepository,
                    clock = clock,
                )

            When("웹 항목을 삭제한다") {
                Then("TC-375d5840-d17c-45d8-8b9a-12f781170d80 TC-001a88e9-7b13-4e39-9d07-befa161eded9 삭제 여부와 수정 시각만 바꾼다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    isDeletedSlot.captured shouldBe true
                    updatedAtSlot.captured shouldBe now
                }

                Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-335846fb-942b-4b3d-aa52-2bb3b2e38aa4 TC-c6828a34-6fdc-4319-b79c-5a9a022b2ea7 TC-aa0e220a-eb33-46de-bde8-0921df1278c6 로컬 저장 결과로 성공을 판단하고 동기화를 요청한다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("게스트 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.Guest>()
            val id = Uuid.random()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountWebRepository = mockk<AccountWebRepository>()
            coEvery {
                accountWebRepository.updateDeleted(account = account, webId = id, isDeleted = any(), updatedAt = any())
            } returns 1
            val useCase =
                DeleteWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountWebRepository = accountWebRepository,
                    clock = Clock.System,
                )

            When("웹 항목을 삭제한다") {
                Then("TC-fc0c7076-0894-4610-802f-ee24fb9612ac TC-ca23453a-d461-4d5e-a800-98d0920e3fc3 게스트 계정 기준으로 기기에만 삭제를 반영한다") {
                    useCase(parameter = id).shouldBeSuccess(1)

                    coVerify(exactly = 1) {
                        accountWebRepository.updateDeleted(account = account, webId = id, isDeleted = true, updatedAt = any())
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountWebRepository = mockk<AccountWebRepository>(relaxed = true)
            val useCase =
                DeleteWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountWebRepository = accountWebRepository,
                    clock = Clock.System,
                )

            When("웹 항목을 삭제한다") {
                Then("계정 조회 실패를 전달하고 삭제를 저장하지 않는다") {
                    useCase(parameter = Uuid.random())
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) {
                        accountWebRepository.updateDeleted(account = any(), webId = any(), isDeleted = any(), updatedAt = any())
                    }
                }
            }
        }

        Given("삭제 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountWebRepository = mockk<AccountWebRepository>()
            coEvery {
                accountWebRepository.updateDeleted(account = account, webId = any(), isDeleted = any(), updatedAt = any())
            } throws throwable
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                DeleteWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountWebRepository = accountWebRepository,
                    clock = Clock.System,
                )

            When("웹 항목을 삭제한다") {
                Then("TC-be1a3fd8-4d68-4566-af4d-b8c476e628f2 실패를 그대로 전달하고 동기화를 요청하지 않는다") {
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

        private fun requestSyncUseCase(): RequestSyncUseCase {
            val useCase = mockk<RequestSyncUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)

            return useCase
        }
    }
}
